import os
import asyncio
import json
import time
import httpx
from typing import List
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from dotenv import load_dotenv

load_dotenv()

app = FastAPI(title="오픽꿀잼 AI 분석 서버 (Gemini 통합 버전)")

GMS_KEY = os.environ.get("GMS_KEY")
# Gemini 전용 URL
GEMINI_URL = "https://gms.ssafy.io/gmsapi/generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent"

# --- Pydantic 모델 정의 ---
class AnalysisRequest(BaseModel):
    question_text: str
    user_answer: str

class SentenceFeedback(BaseModel):
    target_sentence: str
    target_text: str
    improved_text: str
    feedback: str
    sentence_order: int

class CombinedResponse(BaseModel):
    improved_answer: str
    relevance_feedback: str
    logic_feedback: str
    fluency_feedback: str
    sentence_details: List[SentenceFeedback]

# --- 에이전트 함수 (Gemini 통합) ---

async def analyze_sentences_gemini(text: str):
    """Gemini-2.5-flash-lite를 활용한 문장 단위 교정"""
    start_time = time.perf_counter()
    headers = {"Content-Type": "application/json", "x-goog-api-key": GMS_KEY}
    
    prompt = f"""
    당신은 오픽 문장 교정 전문가입니다. 아래 사용자 답변을 분석하여 반드시 지정된 JSON 형식을 지켜 '핵심만' 응답하세요.
    응답 형식:
    {{
      "sentences": [
        {{
          "target_sentence": "원래 문장 전체",
          "target_text": "오류 구간",
          "improved_text": "교정 구간",
          "feedback": "이유(100자 이내)",
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
    
    duration = time.perf_counter() - start_time
    usage = res_json.get('usageMetadata', {})
    in_tokens = usage.get('promptTokenCount', 0)
    out_tokens = usage.get('candidatesTokenCount', 0)
    
    print(f"\n📊 [Gemini Flash - 문장교정 Log]")
    print(f"⏱️ 소요 시간: {duration:.2f}s | 🎫 토큰: {in_tokens}/{out_tokens}")
    
    content_text = res_json['candidates'][0]['content']['parts'][0]['text']
    return json.loads(content_text).get("sentences", [])

async def analyze_overall_gemini(question: str, answer: str):
    """Gemini-2.5-flash-lite를 활용한 종합 피드백"""
    start_time = time.perf_counter()
    headers = {"Content-Type": "application/json", "x-goog-api-key": GMS_KEY}
    
    prompt = f"""
    당신은 오픽(OPIc) 채점관입니다. 아래 규칙을 엄격히 지켜 JSON으로 응답하세요.

    규칙:
    1. improved_answer: 사용자의 답변을 토대로 AL 등급 수준의 '영어' 모범 답안을 작성하세요. (절대 한국어 금지)
    2. relevance_feedback: 질문 적합성을 '한국어'로 평가하세요.
    3. logic_feedback: 논리 전개를 '한국어'로 평가하세요.
    4. fluency_feedback: 유창성을 '한국어'로 평가하세요.

    질문: {question}
    사용자 답변: {answer}
    """
    
    payload = {
        "contents": [{"parts": [{"text": prompt}]}],
        "generationConfig": {"response_mime_type": "application/json"}
    }

    async with httpx.AsyncClient() as client:
        response = await client.post(GEMINI_URL, headers=headers, json=payload, timeout=30.0)
        res_json = response.json()
    
    duration = time.perf_counter() - start_time
    usage = res_json.get('usageMetadata', {})
    
    print(f"\n📊 [Gemini Flash - 종합피드백 Log]")
    print(f"⏱️ 소요 시간: {duration:.2f}s | 🎫 토큰: {usage.get('promptTokenCount')}/{usage.get('candidatesTokenCount')}")
    
    content_text = res_json['candidates'][0]['content']['parts'][0]['text']
    return json.loads(content_text)

# --- API 엔드포인트 ---

@app.post("/v1/analyze", response_model=CombinedResponse)
async def analyze_voice_text(request: AnalysisRequest):
    total_start = time.perf_counter()
    try:
        # 병렬 실행 (두 노드 모두 Gemini 사용)
        sentence_task = analyze_sentences_gemini(request.user_answer)
        overall_task = analyze_overall_gemini(request.question_text, request.user_answer)
        
        sentence_res, overall_res = await asyncio.gather(sentence_task, overall_task)
        
        total_duration = time.perf_counter() - total_start
        print(f"\n✨ [전체 분석 완료] 총 소요 시간: {total_duration:.2f}s")
        
        return {**overall_res, "sentence_details": sentence_res}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))