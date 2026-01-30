import os
import asyncio
import json
import time
import httpx
from typing import List
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field
from dotenv import load_dotenv

load_dotenv()

app = FastAPI(title="오픽꿀잼 AI 분석 서버 (Sequential Gemini 버전)")

GMS_KEY = os.environ.get("GMS_KEY")
GEMINI_URL = "https://gms.ssafy.io/gmsapi/generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent"

# --- Pydantic 모델 정의 ---

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

    2. relevance_feedback: [원본 답변]이 질문의 의도에 부합하는지 한국어로 평가하세요. 
       (교정본이 아닌, 사용자가 처음에 말한 내용이 질문에 맞는지 확인해야 합니다.)

    3. logic_feedback: [원본 답변]의 논리 전개를 한국어로 평가하세요. 
       - '주제에 대한 답변 + 당시의 감정 + 이유'의 구조가 아니라면, 그렇게 고치는게 좋다고 조언하세요.
       - 부족하다면 위 구조를 참고하라는 가이드를 포함하세요.

    4. fluency_feedback: [원본 답변]의 발화량과 유창성을 한국어로 평가하세요.
       - [한국어 의도]와 비교했을 때 영어 답변에서 빠진 부분이나 왜곡된 내용이 있는지 대조 분석을 포함하세요.
       - [한국어 의도]의 분량에 비해 영어 답변이 현저히 짧다면 유창성 부족을 지목하세요.
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
app.include_router(exam_router, prefix="/v1")