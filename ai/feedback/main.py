import os
import asyncio
import json
import time
import base64
import httpx
import numpy as np
import io
import wave
import re
from typing import List
from fastapi import FastAPI, HTTPException, WebSocket, WebSocketDisconnect, Request
from fastapi.exceptions import RequestValidationError
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from pydantic import BaseModel, Field
from dotenv import load_dotenv
import websockets
from websockets.exceptions import ConnectionClosed
from prometheus_fastapi_instrumentator import Instrumentator

load_dotenv()

app = FastAPI(title="오픽꿀잼 AI 분석 & STT 서버")

# CORS 미들웨어 추가 (WebSocket 및 API 접근 허용)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Prometheus metrics endpoint for Grafana scraping
Instrumentator().instrument(app).expose(app, endpoint="/metrics")

@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    """
    422 에러 발생 시 상세 로그를 출력하는 핸들러
    """
    body = await request.body()
    try:
        body_json = json.loads(body)
        print(f"❌ [422 Error] Details: {exc.errors()}")
        print(f"❌ [422 Error] Body Key Check: {list(body_json.keys()) if isinstance(body_json, dict) else 'Not a dict'}")
        # 주의: 전체 Body를 찍으면 로그가 너무 길어질 수 있으므로, 필요시 주석 해제하여 사용
        print(f"❌ [422 Error] Full Body: {body_json}")
    except Exception:
        print(f"❌ [422 Error] Body Raw: {body.decode('utf-8')}")
        
    return JSONResponse(
        status_code=422,
        content={"detail": exc.errors(), "body": str(body)},
    )

GMS_KEY = os.environ.get("GMS_KEY")
GEMINI_URL = "https://gms.ssafy.io/gmsapi/generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent"
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")
OPENAI_REALTIME_WS_URL = os.getenv("OPENAI_REALTIME_WS_URL", "wss://api.openai.com/v1/realtime")
OPENAI_REALTIME_MODEL = os.getenv("OPENAI_REALTIME_MODEL", "gpt-4o-transcribe")
OPENAI_REALTIME_LANGUAGE = os.getenv("OPENAI_REALTIME_LANGUAGE", "en")
OPENAI_REALTIME_SAMPLE_RATE = int(os.getenv("OPENAI_REALTIME_SAMPLE_RATE", "24000"))


class AnalysisRequest(BaseModel):
    question_text: str = Field(..., description="오픽 질문 텍스트", example="Tell me about your favorite park.")
    user_answer: str = Field(..., description="사용자가 발화한 영어 답변", example="I like Central Park...")
    user_korean_script: str = Field(..., description="사용자가 말하고 싶었던 한국어 의도", example="나는 센트럴 파크를 좋아해. 거기 가면 마음이 편해지거든.")

class SentenceFeedback(BaseModel):
    target_sentence: str = Field(..., description="원본 문장")
    target_text: str = Field(..., description="수정이 필요한 부분")
    improved_text: str = Field(..., description="교정된 전체 문장")
    feedback: str = Field(..., description="교정 이유 및 AL 팁")
    sentence_order: int = Field(..., description="문장 순서")

class CombinedResponse(BaseModel):
    improved_answer: str
    relevance_feedback: str
    logic_feedback: str
    fluency_feedback: str
    sentence_details: List[SentenceFeedback]

# --- 에이전트 함수 ---

async def analyze_sentences_gemini(text: str):
    """1단계: 문장 단위 교정 및 필러(Filler) 주입"""
    start_time = time.perf_counter()
    headers = {"Content-Type": "application/json", "x-goog-api-key": GMS_KEY}
    
    prompt = f"""
    당신은 오픽 문장 교정 전문가입니다. 아래 사용자 답변을 분석하여 AL 등급에 맞게 교정하세요.
    
    [교정 가이드라인]
    중요: 해당 문장이 어느정도 완벽하면 교정하지 않습니다.
    1. 문법 오류 수정은 기본입니다.
    2. 문장 사이에 자연스러운 Filler(you know, I mean, I gotta say, What I'm trying to say is, Well, actually I've never thought about it...) 등을 필요하다면 추가하세요.
    3. 구어체 형태로, 감정 표현을 더욱 풍부하게 만들어주세요. (numerous, incredibly, crystal clear, stunning, go-to, laid back, relaxing, striking, challenging, various, truly, tasty 등 사용)
    4. 롤플레이나 과거와 현재를 비교할 땐 완료 시제를 사용하세요.

    응답 형식 (JSON):
    {{
      "sentences": [
        {{
          "target_sentence": "원래 문장 전체",
          "target_text": "오류/개선 구간",
          "improved_text": "교정된 문장 전체",
          "feedback": "교정 이유 및 AL 팁 (100자 이내)",
          "sentence_order": 1
        }}
      ]
    }}
    사용자 답변: {text}
    """
    
    payload = {
        "contents": [{"parts": [{"text": prompt}]}],
        "generationConfig": {"response_mime_type": "application/json"}
    }

    async with httpx.AsyncClient() as client:
        response = await client.post(GEMINI_URL, headers=headers, json=payload, timeout=30.0)
        res_json = response.json()
    
    content_text = res_json['candidates'][0]['content']['parts'][0]['text']
    sentences = json.loads(content_text).get("sentences", [])
    
    print(f"📊 [Step 1] 문장 교정 완료 ({time.perf_counter() - start_time:.2f}s)")
    return sentences

async def analyze_overall_gemini(question: str, user_answer: str, corrected_text: str, user_korean_script: str):
    """2단계: 교정본으로 모범 답안을 생성하되, 한국어 스크립트 대조 및 원본 기준 평가 수행"""
    start_time = time.perf_counter()
    headers = {"Content-Type": "application/json", "x-goog-api-key": GMS_KEY}
    
    prompt = f"""
    당신은 오픽(OPIc) AL 전문 채점관입니다. 
    아래 제공된 [원본 답변] 및 [한국어 의도]를 분석하여 피드백하고, [교정된 문장들]을 활용해 최종 학습용 모범 답안을 만드세요.

    [입력 데이터]
    - 질문: {question}
    - 한국어 의도(사용자가 말하고 싶었던 내용): {user_korean_script}
    - 원본 답변(사용자 실제 발화): {user_answer}
    - 교정된 문장들(1단계 결과물): {corrected_text}

    [작성 규칙 - 반드시 JSON 포맷으로 응답하세요]
    1. improved_answer: [교정된 문장들]의 내용을 유지하며, 전체 흐름이 자연스럽도록 연결 어구만 추가하여 완성하세요.
       - 서두에 확실한 Main Point(MP)가 드러나야 합니다.
       - 한국어 의도에는 있으나 영어 답변에서 누락된 핵심 내용이 있다면 자연스럽게 포함시켜 완성하세요.
       - COMBO 문제거나, 이전에 말한 내용이 언급될 땐 "As I told you before"와 같은 연결 고리를 넣으세요.
       - 그 외에 문장 연결 간에 필요한 필러를 문맥상 적절히 사용하세요. (I think that's all I can say about me, That's all I wanted to say, What I'm trying to say, To put detail~ , At the end of the day, Or something, Obviously, Currently, basically, You see, I mean, In fact, what else- , What I really love about is that, The reason why, what am I trying to say, anyway, I gotta tell you, Wow... It's quite a tough question, That's tricky, That is a reason why)
       - 앞 뒤 문장의 맥락이 달라질 땐 By the way 등의 접속사를 적절하게 사용하세요. 

    2. relevance_feedback: [원본 답변]이 질문의 의도에 부합하는지 한국어로 평가하세요. 반드시 원본 답변 기반으로. 
       (교정본이 아닌, 사용자가 처음에 말한 내용이 질문에 맞는지 확인해야 합니다.)

    3. logic_feedback: [원본 답변]의 논리 전개를 한국어로 평가하세요. 반드시 원본 답변 기반으로.
       - 오픽에서 높은 등급(IH/AL)을 받으려면 단순 문장 나열이 아닌, Main Point(핵심 문장)를 먼저 제시하고 그에 대한 구체적인 근거, 경험, 묘사, 결론을 덧붙이는 구조화된 논리적 답변이 필수적입니다. 이에 관해 원본 답변이 부족한 부분을 피드백하세요.
       - 원본 답변이 '주제에 대한 답변, 당시의 감정, 이유'의 구조인지 러프하게 확인하고, 그게 아니라면 그렇게 고치는게 좋다고 조언하세요. 비슷한 구조를 따른다면 칭찬만 해도 좋습니다.

    4. fluency_feedback: [원본 답변]의 발화량과 유창성을 한국어로 평가하세요. 반드시 원본 답변 기반으로.
       - [한국어 의도]와 비교했을 때 영어 답변에서 빠진 부분이나 왜곡된 내용이 있는지 대조 분석을 포함하세요.
       - 만약 [한국어 의도]가 비어있다면, [원본 답변]의 분량과, 어휘의 지나친 반복 여부만으로 평가하세요.
       - 2문장 이하: 심각한 지적, 4문장 이하: 보강 조언, 5문장 이상: 칭찬.
       - 표현의 다양성은 지나치게 엄격하지 않게, 격려 위주로 작성하세요. 너무 단조롭다면 그때만 지적하세요.
    
    주의: relevance_feedback, logic_feedback, fluency_feedback은 절대로 [교정된 문장들] 기준이 아닌, [원본 답변]의 수준을 바탕으로 작성해야 합니다. fluency_feedback 항목은 [원본 답변]과 [한국어 의도] 사이의 간극도 체크하세요.
    """
    
    payload = {
        "contents": [{"parts": [{"text": prompt}]}],
        "generationConfig": {
            "response_mime_type": "application/json",
            "response_schema": {
                "type": "object",
                "properties": {
                    "improved_answer": {"type": "string"},
                    "relevance_feedback": {"type": "string"},
                    "logic_feedback": {"type": "string"},
                    "fluency_feedback": {"type": "string"}
                },
                "required": ["improved_answer", "relevance_feedback", "logic_feedback", "fluency_feedback"]
            }
        }
    }

    async with httpx.AsyncClient() as client:
        response = await client.post(GEMINI_URL, headers=headers, json=payload, timeout=30.0)
        res_json = response.json()
    
    print(f"📊 [Step 2] 원본 및 의도 대조 종합 피드백 완료 ({time.perf_counter() - start_time:.2f}s)")
    return json.loads(res_json['candidates'][0]['content']['parts'][0]['text'])

# --- API 엔드포인트 ---

@app.post(
    "/v1/analyze", 
    response_model=CombinedResponse,
    tags=["Practice"],
    summary="사용자 답변 AI 종합 분석",
    description="사용자의 영어 답변과 한국어 의도를 바탕으로 문법 교정 및 AL 기준 피드백을 생성합니다."
)
async def analyze_voice_text(request: AnalysisRequest):
    total_start = time.perf_counter()
    try:
        # 1. 문법 및 필러 교정
        sentence_details = await analyze_sentences_gemini(request.user_answer)
        
        # 교정된 문장들을 하나의 텍스트로 결합
        corrected_text = " ".join([s['improved_text'] for s in sentence_details])
        
        # 2. 한국어 의도 대조 및 종합 피드백 생성 (순차 실행)
        overall_res = await analyze_overall_gemini(
            request.question_text, 
            request.user_answer, 
            corrected_text,
            request.user_korean_script
        )
        
        total_duration = time.perf_counter() - total_start
        print(f"✨ [전체 분석 완료] 총 소요 시간: {total_duration:.2f}s")
        
        return {**overall_res, "sentence_details": sentence_details}
        
    except Exception as e:
        print(f"❌ Error: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

# --- 라우터 등록 ---
from exam_feedback import router as exam_router
from total_feedback import router as total_router

app.include_router(exam_router, prefix="/v1")
app.include_router(total_router, prefix="/v1")

# ==========================================
# OpenAI Realtime Transcription & WebSocket Logic
# ==========================================

def _build_context_tail(existing: str, incoming: str, max_words: int = 32) -> str:
    merged = " ".join([part.strip() for part in [existing, incoming] if part and part.strip()])
    if not merged:
        return ""
    words = merged.split()
    return " ".join(words[-max_words:])

# whister API 문제로 발생하는 불필요한 발화 필터링
def _is_unwanted_transcript(text: str) -> bool:
    normalized = (text or "").strip().lower()
    if not normalized:
        return True
    blocked_patterns = [
        r"\bwww\.",
        r"\.com\b",
        r"\bengvid\b",
        r"\bsubscribe\b",
        r"\blike and subscribe\b",
        r"\bfree course\b",
    ]
    return any(re.search(pattern, normalized) for pattern in blocked_patterns)


def _segment_is_valid(audio_len_bytes: int, voiced_samples: int, total_samples: int, min_audio_bytes: int) -> bool:
    if audio_len_bytes <= min_audio_bytes:
        return False
    if total_samples <= 0:
        return False
    voiced_ratio = voiced_samples / total_samples
    if voiced_samples < int(16000 * 0.35):
        return False
    if voiced_ratio < 0.22:
        return False
    return True


async def transcribe_audio_async(audio_bytes, prompt_text: str = ""):
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
        
        prompt = normalize_prompt = (prompt_text or "").strip()
        if prompt:
            prompt = (
                "This is a continuous spoken English answer. "
                f"Keep lexical continuity with previous context: {normalize_prompt}"
            )

        transcript = await client.audio.transcriptions.create(
            model="whisper-1",
            file=buffer,
            language="en",
            prompt=prompt if prompt else None,
        )
        print(f"🚀 [Whisper] OpenAI 응답 완료 ({time.time() - start_time:.2f}s)")
        return transcript.text


@app.websocket("/ws/transcribe")
async def websocket_endpoint(websocket: WebSocket):
    if not OPENAI_API_KEY:
        await websocket.close(code=1011, reason="OPENAI_API_KEY is missing")
        return

    await websocket.accept()
    print("[WS] Client Connected")

    audio_buffer = bytearray()
    silence_start_time = None
    is_speaking = False
    segment_seq = 0
    packet_count = 0
    transcript_context = ""
    voiced_samples = 0
    total_samples = 0

    # VAD parameters
    SILENCE_THRESHOLD = 0.1
    SILENCE_DURATION = 1
    MIN_AUDIO_LENGTH = int(16000 * 2 * 0.6)

    try:
        while True:
            message = await websocket.receive()
            if message.get("type") == "websocket.disconnect":
                raise WebSocketDisconnect()

            text_payload = message.get("text")
            if text_payload is not None:
                try:
                    payload = json.loads(text_payload)
                except json.JSONDecodeError:
                    continue

                if payload.get("event") == "eof":
                    print(
                        f"[WS] EOF received. Flushing... buffer_bytes={len(audio_buffer)} seq={segment_seq} "
                        f"voiced_samples={voiced_samples} total_samples={total_samples}"
                    )
                    if len(audio_buffer) > 0 and _segment_is_valid(
                        len(audio_buffer), voiced_samples, total_samples, MIN_AUDIO_LENGTH
                    ):
                        flush_start = time.time()
                        text = await transcribe_audio_async(audio_buffer, transcript_context)
                        if text and not _is_unwanted_transcript(text):
                            segment_seq += 1
                            transcript_context = _build_context_tail(transcript_context, text)
                            print(f"[STT Result#{segment_seq}] flush chars={len(text)} took={time.time() - flush_start:.2f}s")
                            await websocket.send_json({"type": "full", "seq": segment_seq, "text": text})
                        elif text:
                            print(f"[STT Filtered] flush text dropped: {text}")
                    elif len(audio_buffer) > 0:
                        print(
                            f"[Talk] EOF segment skipped by quality gate: "
                            f"bytes={len(audio_buffer)} voiced={voiced_samples} total={total_samples}"
                        )

                    print(f"[WS] Sending done seq={segment_seq}")
                    await websocket.send_json({"type": "done", "seq": segment_seq})
                    audio_buffer = bytearray()
                    is_speaking = False
                    silence_start_time = None
                    voiced_samples = 0
                    total_samples = 0
                continue

            audio_bytes = message.get("bytes")
            if audio_bytes is None:
                continue

            packet_count += 1
            if packet_count % 200 == 0:
                print(f"[WS] packets={packet_count} buffer_bytes={len(audio_buffer)} speaking={is_speaking}")

            audio_float32 = np.frombuffer(audio_bytes, dtype=np.float32)
            audio_int16 = (audio_float32 * 32767).astype(np.int16)
            volume = np.max(np.abs(audio_float32)) if len(audio_float32) > 0 else 0

            if volume > SILENCE_THRESHOLD:
                if not is_speaking:
                    is_speaking = True
                    print(f"[Talk] Speaking started... packet={packet_count}")
                silence_start_time = None
                audio_buffer.extend(audio_int16.tobytes())
                voiced_samples += len(audio_int16)
                total_samples += len(audio_int16)
            else:
                if is_speaking:
                    audio_buffer.extend(audio_int16.tobytes())
                    total_samples += len(audio_int16)
                    if silence_start_time is None:
                        silence_start_time = time.time()
                    elif time.time() - silence_start_time > SILENCE_DURATION:
                        print(
                            f"[Talk] Sentence ended. Requesting Whisper... buffer_bytes={len(audio_buffer)} "
                            f"voiced_samples={voiced_samples} total_samples={total_samples}"
                        )

                        if _segment_is_valid(len(audio_buffer), voiced_samples, total_samples, MIN_AUDIO_LENGTH):
                            segment_start = time.time()
                            text = await transcribe_audio_async(audio_buffer, transcript_context)

                            if text and not _is_unwanted_transcript(text):
                                segment_seq += 1
                                transcript_context = _build_context_tail(transcript_context, text)
                                print(f"[STT Result#{segment_seq}] segment chars={len(text)} took={time.time() - segment_start:.2f}s")
                                await websocket.send_json({"type": "full", "seq": segment_seq, "text": text})
                            elif text:
                                print(f"[STT Filtered] segment text dropped: {text}")
                        else:
                            print(
                                f"[Talk] Segment skipped by quality gate: "
                                f"bytes={len(audio_buffer)} voiced={voiced_samples} total={total_samples}"
                            )

                        audio_buffer = bytearray()
                        is_speaking = False
                        silence_start_time = None
                        voiced_samples = 0
                        total_samples = 0

    except WebSocketDisconnect:
        print("[WS] Client Disconnected")
    except Exception as e:
        print(f"[WS] Error: {e}")
