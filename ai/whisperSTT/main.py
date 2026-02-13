import asyncio
import numpy as np
import os
import io
import wave
import time
from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from fastapi.middleware.cors import CORSMiddleware
from openai import AsyncOpenAI # [핵심] 비동기 클라이언트 사용
from dotenv import load_dotenv

load_dotenv()

# [핵심] AsyncOpenAI 사용
# 이렇게 하면 OpenAI 응답을 기다리는 동안 다른 사용자의 요청을 처리할 수 있음
client = AsyncOpenAI(api_key=os.getenv("OPENAI_API_KEY"))

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ==========================================
# Async OpenAI Wrapper
# ==========================================
async def transcribe_audio_async(audio_bytes):
    """
    비동기 방식으로 OpenAI API를 호출합니다.
    서버가 멈추지 않고(Non-blocking) 다른 요청을 동시에 처리합니다.
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
        
        # [핵심] await 키워드 사용 -> 기다리는 동안 CPU를 다른 사용자에게 양보함
        transcript = await client.audio.transcriptions.create(
            model="whisper-1", 
            file=buffer,
            language="en"
        )
        print(f"🚀 OpenAI 응답 완료 ({time.time() - start_time:.2f}s)")
        return transcript.text

    except Exception as e:
        print(f"OpenAI API Error: {e}")
        return None

# ==========================================
# 웹소켓 핸들러
# ==========================================
@app.websocket("/ws/transcribe")
async def websocket_endpoint(websocket: WebSocket):
    await websocket.accept()
    
    # 각 클라이언트마다 독립적인 변수 공간을 가집니다. (병렬 처리 OK)
    audio_buffer = bytearray()
    silence_start_time = None
    is_speaking = False
    
    # VAD 설정
    SILENCE_THRESHOLD = 0.05
    SILENCE_DURATION = 1.0
    MIN_AUDIO_LENGTH = 16000 * 2 * 0.5 

    try:
        while True:
            # 1. 오디오 수신 (Non-blocking)
            # 여기서 대기할 때도 다른 사용자 처리가 가능함
            audio_bytes = await websocket.receive_bytes()
            
            # numpy 연산은 CPU를 쓰지만 매우 짧아서 괜찮음
            audio_float32 = np.frombuffer(audio_bytes, dtype=np.float32)
            audio_int16 = (audio_float32 * 32767).astype(np.int16)
            volume = np.max(np.abs(audio_float32))
            
            # VAD 로직
            if volume > SILENCE_THRESHOLD:
                if not is_speaking:
                    is_speaking = True
                silence_start_time = None
                audio_buffer.extend(audio_int16.tobytes())
            
            else:
                if is_speaking:
                    audio_buffer.extend(audio_int16.tobytes())
                    if silence_start_time is None:
                        silence_start_time = time.time()
                    
                    elif time.time() - silence_start_time > SILENCE_DURATION:
                        # 문장 종료 감지
                        if len(audio_buffer) > MIN_AUDIO_LENGTH:
                            # [핵심] 비동기 함수 호출 (await)
                            # 여기서 API 응답을 기다리는 1초 동안, 
                            # 서버는 다른 사용자의 audio_bytes를 받을 수 있음!
                            text = await transcribe_audio_async(audio_buffer)
                            
                            if text:
                                await websocket.send_json({"type": "full", "text": text})
                        
                        audio_buffer = bytearray()
                        is_speaking = False
                        silence_start_time = None

    except WebSocketDisconnect:
        pass
    except Exception as e:
        print(f"Error: {e}")