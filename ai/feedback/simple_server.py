import os
import asyncio
import json
import time
import io
import wave
import numpy as np
from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from fastapi.middleware.cors import CORSMiddleware
from dotenv import load_dotenv
from openai import AsyncOpenAI
from pathlib import Path

# 현재 파일(__file__)의 디렉토리 기준으로 .env 로드
env_path = Path(__file__).parent / ".env"
load_dotenv(dotenv_path=env_path)

app = FastAPI(title="Simple Whisper STT Server")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# OpenAI 클라이언트 (환경변수 OPENAI_API_KEY 필요)
client = AsyncOpenAI(api_key=os.getenv("OPENAI_API_KEY"))

async def transcribe_audio_async(audio_bytes):
    """
    비동기 방식으로 OpenAI API를 호출하여 STT를 수행합니다.
    """
    try:
        buffer = io.BytesIO()
        buffer.name = "audio.wav"
        
        with wave.open(buffer, 'wb') as wf:
            wf.setnchannels(1)
            wf.setsampwidth(2) 
            wf.setframerate(16000)
            wf.writeframes(audio_bytes)
        
        buffer.seek(0)
        start_time = time.time()
        
        whisper_prompt = os.getenv("WHISPER_PROMPT")
        whisper_language = os.getenv("WHISPER_LANGUAGE", "en")

        request_kwargs = {
            "model": "whisper-1",
            "file": buffer,
            "language": whisper_language,
            "temperature": 0,
        }
        if whisper_prompt:
            request_kwargs["prompt"] = whisper_prompt

        transcript = await client.audio.transcriptions.create(**request_kwargs)
        print(f"🚀 [Whisper] OpenAI 응답 완료 ({time.time() - start_time:.2f}s)")
        return transcript.text

    except Exception as e:
        print(f"❌ [Whisper] OpenAI API Error: {e}")
        return None

@app.websocket("/ws/transcribe")
async def websocket_endpoint(websocket: WebSocket):
    await websocket.accept()
    print(f"🔌 [WS] Client Connected")
    
    audio_buffer = bytearray()
    silence_start_time = None
    is_speaking = False
    
    # VAD parameters
    SILENCE_THRESHOLD = float(os.getenv("VAD_SILENCE_THRESHOLD", "0.05"))
    SILENCE_DURATION = float(os.getenv("VAD_SILENCE_DURATION", "1.2"))
    MIN_AUDIO_LENGTH = int(16000 * 2 * float(os.getenv("VAD_MIN_AUDIO_SEC", "0.8")))
    NOISE_FLOOR_INIT = float(os.getenv("VAD_NOISE_FLOOR_INIT", "0.01"))
    NOISE_FLOOR_ALPHA = float(os.getenv("VAD_NOISE_FLOOR_ALPHA", "0.95"))
    SPEECH_MARGIN = float(os.getenv("VAD_SPEECH_MARGIN", "2.5"))
    MAX_DYNAMIC_THRESHOLD = float(os.getenv("VAD_MAX_DYNAMIC_THRESHOLD", "0.2"))

    noise_floor = NOISE_FLOOR_INIT

    try:
        while True:
            # Non-blocking 수신
            audio_bytes = await websocket.receive_bytes()
            
            # 오디오 처리 (Numpy 변환)
            audio_float32 = np.frombuffer(audio_bytes, dtype=np.float32)
            audio_int16 = (audio_float32 * 32767).astype(np.int16)
            if len(audio_float32) > 0:
                rms = float(np.sqrt(np.mean(np.square(audio_float32), dtype=np.float64)))
            else:
                rms = 0.0

            if not is_speaking:
                noise_floor = (NOISE_FLOOR_ALPHA * noise_floor) + ((1.0 - NOISE_FLOOR_ALPHA) * rms)

            dynamic_threshold = max(
                SILENCE_THRESHOLD,
                min(MAX_DYNAMIC_THRESHOLD, noise_floor * SPEECH_MARGIN),
            )
            
            # VAD Logic
            if rms > dynamic_threshold:
                if not is_speaking:
                    is_speaking = True
                    print("🎤 [Talk] Speaking started...")
                silence_start_time = None
                audio_buffer.extend(audio_int16.tobytes())
            
            else:
                if is_speaking:
                    audio_buffer.extend(audio_int16.tobytes())
                    if silence_start_time is None:
                        silence_start_time = time.time()
                    
                    elif time.time() - silence_start_time > SILENCE_DURATION:
                        # 문장 종료라고 판단 (Silence Duration 초과)
                        print("🛑 [Talk] Sentence ended. Requesting Whisper...")
                        
                        if len(audio_buffer) > MIN_AUDIO_LENGTH:
                            # Whisper 요청 (비동기)
                            text = await transcribe_audio_async(audio_buffer)
                            
                            if text:
                                print(f"📝 [STT Result] {text}")
                                await websocket.send_json({"type": "full", "text": text})
                        
                        # 버퍼 초기화
                        audio_buffer = bytearray()
                        is_speaking = False
                        silence_start_time = None

    except WebSocketDisconnect:
        print("🔌 [WS] Client Disconnected")
    except Exception as e:
        print(f"❌ [WS] Error: {e}")
