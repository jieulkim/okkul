import os
import json
from typing import List
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, Field
from dotenv import load_dotenv

load_dotenv()

# --- APIRouter 설정 ---
router = APIRouter(
    tags=["Exam Report"],
    responses={404: {"description": "Not found"}},
)

GMS_KEY = os.environ.get("GMS_KEY")
GEMINI_URL = "https://gms.ssafy.io/gmsapi/generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent"

# --- Pydantic 모델 정의 (설명 필드 추가) ---

class ExamQuestionResult(BaseModel):
    """개별 문항 결과 (입력 데이터 형식)"""
    question_order: int = Field(..., description="문항 번호 (1~15)", example=1)
    stt_script: str = Field(..., description="사용자 원본 발화 텍스트")
    improved_answer: str = Field(..., description="AI 교정 모범 답안")
    relevance_score: int = Field(..., description="질문 부합도 점수 (0-100)", example=85)
    logic_score: int = Field(..., description="논리 전개 점수 (0-100)", example=80)
    vocab_score: int = Field(..., description="어휘 사용 점수 (0-100)", example=75)
    grammar_score: int = Field(..., description="문법 정확도 점수 (0-100)", example=70)
    fluency_score: int = Field(..., description="발화 유창성 점수 (0-100)", example=90)
    logic_feedback: str = Field(..., description="문항별 개별 피드백 내용")

class ExamReportResponse(BaseModel):
    """최종 생성된 종합 리포트 응답 형식"""
    total_score: float = Field(..., description="전체 종합 점수 (100점 만점 환산)", example=82.5)
    grade: str = Field(..., description="최종 예상 등급 (AL, IH, IM3, IM2, IM1, NH)", example="IH")
    avg_grammar: float = Field(..., description="전체 문항 평균 문법 점수")
    avg_vocab: float = Field(..., description="전체 문항 평균 어휘 점수")
    avg_logic: float = Field(..., description="전체 문항 평균 논리 점수")
    avg_fluency: float = Field(..., description="전체 문항 평균 유창성 점수")
    avg_relevance: float = Field(..., description="전체 문항 평균 질문 부합도 점수")
    comment: str = Field(..., description="전체 모의고사에 대한 종합 총평 (한국어)")
    strength_type: str = Field(..., description="사용자의 핵심 장점 키워드", example="풍부한 감정 표현과 자연스러운 필러 사용")
    weakness_type: str = Field(..., description="사용자의 보완점 키워드", example="과거 시제와 현재 시제의 혼용")

# --- 에이전트 함수 ---

async def generate_exam_report_gemini(all_results: List[ExamQuestionResult]):
    """Gemini를 사용하여 15개 문항의 결과를 종합 분석"""
    import httpx
    
    # 1. 입력 데이터 요약 (토큰 절약 및 구조화)
    summary_data = "\n".join([
        f"Q{r.question_order}: 점수(R:{r.relevance_score}, L:{r.logic_score}, V:{r.vocab_score}, G:{r.grammar_score}, F:{r.fluency_score}) / 피드백: {r.logic_feedback}"
        for r in all_results
    ])

    prompt = f"""
    당신은 오픽(OPIc) 전문 채점 위원입니다. 15개 문항의 모의고사 결과를 종합하여 최종 리포트를 작성하세요.
    
    [입력 데이터: 15문항 요약]
    {summary_data}

    [요청 사항]
    위 데이터를 바탕으로 다음 JSON 스키마에 맞춰 종합 분석 결과를 생성하세요.
    - 등급(grade)은 실제 OPIc 기준(AL, IH, IM3 ~ NH)으로 조금 후하게 판정하세요.
    - 총평(comment)은 사용자가 다음 시험에서 더 좋은 점수를 받기 위한 구체적이고 실질적인 조언을 포함해야 합니다.
    - 강점(strength_type)과 약점(weakness_type)은 문장 형태로 요약하세요.
    """
    
    payload = {
        "contents": [{"parts": [{"text": prompt}]}],
        "generationConfig": {
            "response_mime_type": "application/json",
            "response_schema": {
                "type": "object",
                "properties": {
                    "total_score": {"type": "number"},
                    "grade": {"type": "string"},
                    "avg_grammar": {"type": "number"},
                    "avg_vocab": {"type": "number"},
                    "avg_logic": {"type": "number"},
                    "avg_fluency": {"type": "number"},
                    "avg_relevance": {"type": "number"},
                    "comment": {"type": "string"},
                    "strength_type": {"type": "string"},
                    "weakness_type": {"type": "string"}
                },
                "required": ["total_score", "grade", "comment", "strength_type", "weakness_type"]
            }
        }
    }
    
    headers = {"Content-Type": "application/json", "x-goog-api-key": GMS_KEY}
    async with httpx.AsyncClient() as client:
        response = await client.post(GEMINI_URL, headers=headers, json=payload, timeout=60.0)
        res_json = response.json()
        
    return json.loads(res_json['candidates'][0]['content']['parts'][0]['text'])

# --- API 엔드포인트 ---

@router.post(
    "/exam/report", 
    response_model=ExamReportResponse,
    summary="모의고사 종합 리포트 생성 (POST 방식)",
    description="15개 문항의 결과 리스트를 받아 종합 리포트를 생성합니다."
)
async def create_exam_report(results: List[ExamQuestionResult]):
    """
    Client로부터 15개 문항의 분석 결과 리스트를 받아
    Gemini를 통해 종합 리포트를 생성하여 반환합니다.
    (DB 저장 로직 등은 필요에 따라 추가)
    """
    try:
        report_res = await generate_exam_report_gemini(results)
        return report_res

    except Exception as e:
        print(f"❌ Error: {str(e)}")
        raise HTTPException(status_code=500, detail="리포트 생성 중 오류가 발생했습니다.")
