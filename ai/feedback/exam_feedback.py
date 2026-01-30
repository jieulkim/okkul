import os
import json
import time
import httpx
from typing import List, Optional
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, Field
from dotenv import load_dotenv

load_dotenv()

router = APIRouter(
    tags=["Exam Analysis"],
)

GMS_KEY = os.environ.get("GMS_KEY")
GEMINI_URL = "https://gms.ssafy.io/gmsapi/generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent"

# --- Pydantic 모델 정의 ---

class SentenceFeedback(BaseModel):
    """문장별 교정 정보"""
    target_sentence: str = Field(..., description="원래 문장 전체")
    target_text: str = Field(..., description="오류/개선 구간")
    improved_text: str = Field(..., description="필러가 포함되어 교정된 문장 전체")
    feedback: str = Field(..., description="교정 이유 및 AL 팁 (100자 이내)")
    sentence_order: int = Field(..., description="문장 순서")

class QuestionAnalysisRequest(BaseModel):
    """백엔드(Java) 요청 규격에 맞춘 개별 문제 분석 요청"""
    original_question: str = Field(..., description="오픽 질문 텍스트 (기존 question_text)")
    user_answer: str = Field(..., description="사용자 답변 텍스트")
    audio_url: Optional[str] = Field(None, description="녹음된 답변 파일 경로")
    question_type: Optional[str] = Field(None, description="문제 유형 (묘사, 경험, 롤플레이 등)")
    difficulty: Optional[int] = Field(None, description="선택 난이도")
    question_order: int = Field(1, description="문제 번호(순서)")

class QuestionScoreResponse(BaseModel):
    question_order: int
    grammarScore: int
    vocabScore: int
    logicScore: int
    fluencyScore: int
    relevanceScore: int
    improvedAnswer: str
    logicFeedback: str
    fluencyFeedback: str
    relevanceFeedback: str
    sentenceFeedbacks: List[SentenceFeedback]
    
# --- 에이전트 함수 ---

async def analyze_sentences_gemini(user_answer: str) -> List[dict]:
    """1단계: 문장 단위 교정 전문가 (기존 로직 유지)"""
    prompt = f"""
    당신은 오픽 문장 교정 전문가입니다. 아래 사용자 답변을 분석하여 반드시 지정된 JSON 형식을 지켜 '핵심만' 응답하세요.
    
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
          "target_text": "오류 구간",
          "improved_text": "교정 구간",
          "feedback": "이유(100자 이내)",
          "sentence_order": 1
        }}
      ]
    }}
    사용자 답변: {user_answer}
    """
    payload = {
        "contents": [{"parts": [{"text": prompt}]}],
        "generationConfig": {"response_mime_type": "application/json"}
    }
    headers = {"Content-Type": "application/json", "x-goog-api-key": GMS_KEY}
    async with httpx.AsyncClient() as client:
        response = await client.post(GEMINI_URL, headers=headers, json=payload, timeout=30.0)
        res_json = response.json()
        content = res_json['candidates'][0]['content']['parts'][0]['text']
        return json.loads(content).get("sentences", [])

async def analyze_integrated_score_gemini(request: QuestionAnalysisRequest, corrected_text: str) -> dict:
    """2단계: 5대 영역 채점 및 종합 피드백 생성"""
    # 기존 평가 항목 프롬프트 유지
    prompt = f"""
    당신은 오픽(OPIc) 전문 채점관입니다.
    아래 데이터를 분석하여 점수와 상세 피드백을 JSON으로 응답하세요.
    점수는 적당히 후하게 주며, 100점을 만점으로, 객관적이고 냉정하게 OPIc 등급 기준으로 평가하세요.

    [평가 항목]
    1. Grammar (grammarScore): 문법의 정확성, 시제 일치, 문장 구조의 복잡성
    2. Vocab (vocabScore): 어휘의 다양성, 적절한 이디엄 및 표현 사용
    3. Logic (logicScore): 답변의 논리적 흐름, 서론-본론-결론 구조, 근거 제시
    4. Fluency (fluencyScore): 발화의 유창성, 다양한 어휘 사용, 자연스러운 필러를 사용했는지 등
    5. Relevance (relevanceScore): 질문 의도에 대한 적합성, 동문서답 여부

    [입력 데이터]
    - 질문: {request.original_question}
    - 질문 유형: {request.question_type}
    - 사용자 답변: {request.user_answer}
    - 1차 교정 텍스트: {corrected_text}

    [작성 규칙]
    - improvedAnswer: 1차 교정 텍스트를 반드시 똑같이 사용하되, 접속사, 필러 등으로 흐름이 자연스러운 완벽한 AL 답안으로 작성하세요.
    - logicFeedback, fluencyFeedback, relevanceFeedback: 원본 답변을 기준으로 한국어로 상세 조언을 작성하세요.
    """

    payload = {
        "contents": [{"parts": [{"text": prompt}]}],
        "generationConfig": {
            "response_mime_type": "application/json",
            "response_schema": {
                "type": "object",
                "properties": {
                    "grammarScore": {"type": "integer"},
                    "vocabScore": {"type": "integer"},
                    "logicScore": {"type": "integer"},
                    "fluencyScore": {"type": "integer"},
                    "relevanceScore": {"type": "integer"},
                    "improvedAnswer": {"type": "string"},
                    "logicFeedback": {"type": "string"},
                    "fluencyFeedback": {"type": "string"},
                    "relevanceFeedback": {"type": "string"}
                },
                "required": ["grammarScore", "vocabScore", "logicScore", "fluencyScore", "relevanceScore", "improvedAnswer", "logicFeedback", "fluencyFeedback", "relevanceFeedback"]
            }
        }
    }
    headers = {"Content-Type": "application/json", "x-goog-api-key": GMS_KEY}
    async with httpx.AsyncClient() as client:
        response = await client.post(GEMINI_URL, headers=headers, json=payload, timeout=30.0)
        res_json = response.json()
        return json.loads(res_json['candidates'][0]['content']['parts'][0]['text'])

# --- API 엔드포인트 ---

@router.post(
    "/analyze/exam",
    response_model=QuestionScoreResponse,
    summary="개별 문제 통합 분석 (점수 + 피드백 + 교정)",
    description="백엔드 요구사항에 맞춰 질문-답변을 통합 분석하고 점수, 모범답안, 문장별 교정 내역을 반환합니다."
)
async def analyze_exam_integrated(request: QuestionAnalysisRequest):
    try:
        # 1. 문장 단위 교정 실행
        sentence_feedbacks_raw = await analyze_sentences_gemini(request.user_answer)
        corrected_text = " ".join([s['improved_text'] for s in sentence_feedbacks_raw])
        
        # 2. 종합 채점 및 피드백 실행
        overall_result = await analyze_integrated_score_gemini(request, corrected_text)
        
        # 3. 결과 조립
        return QuestionScoreResponse(
            question_order=request.question_order,
            sentenceFeedbacks=[SentenceFeedback(**s) for s in sentence_feedbacks_raw],
            **overall_result
        )
    except Exception as e:
        print(f"❌ Error: {str(e)}")
        raise HTTPException(status_code=500, detail="통합 분석 중 오류가 발생했습니다.")