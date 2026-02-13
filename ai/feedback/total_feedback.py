import os
import json
from typing import List, Optional
from fastapi import APIRouter, HTTPException
from pydantic import BaseModel, Field, ConfigDict
from dotenv import load_dotenv

load_dotenv()

# --- APIRouter 설정 ---
router = APIRouter(
    tags=["Exam"],
    responses={404: {"description": "Not found"}},
)

GMS_KEY = os.environ.get("GMS_KEY")
GEMINI_URL = "https://gms.ssafy.io/gmsapi/generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent"

# --- Pydantic 모델 정의 (설명 필드 추가) ---

class ExamQuestionResult(BaseModel):
    """개별 문항 결과 (입력 데이터 형식)"""
    question_order: int = Field(..., description="문항 번호 (1~15)", example=1)
    stt_script: Optional[str] = Field("", description="사용자 원본 발화 텍스트")
    improved_answer: Optional[str] = Field("", description="AI 교정 모범 답안")
    relevance_score: Optional[int] = Field(0, description="질문 부합도 점수 (0-100)", example=85)
    logic_score: Optional[int] = Field(0, description="논리 전개 점수 (0-100)", example=80)
    vocab_score: Optional[int] = Field(0, description="어휘 사용 점수 (0-100)", example=75)
    grammar_score: Optional[int] = Field(0, description="문법 정확도 점수 (0-100)", example=70)
    fluency_score: Optional[int] = Field(0, description="발화 유창성 점수 (0-100)", example=90)
    logic_feedback: Optional[str] = Field("", description="문항별 개별 피드백 내용")

class ExamReportResponse(BaseModel):
    """최종 생성된 종합 리포트 응답 형식"""
    total_score: int = Field(..., description="전체 종합 점수 (0-100)", example=85)
    predicted_level: str = Field(..., description="최종 예상 등급 (AL, IH 등)", example="IH")
    
    average_grammar_score: int = Field(..., description="전체 평균 문법 점수")
    average_vocab_score: int = Field(..., description="전체 평균 어휘 점수")
    average_logic_score: int = Field(..., description="전체 평균 논리 점수")
    average_fluency_score: int = Field(..., description="전체 평균 유창성 점수")
    average_relevance_score: int = Field(..., description="전체 평균 질문 부합도")
    
    overall_evaluation: str = Field(..., description="종합 총평: 반드시 한국어로 작성.)")
    strengths: List[str] = Field(..., description="강점 리스트: 키워드/짧은 문장, 반드시 한국어로 작성.")
    improvements: List[str] = Field(..., description="보완점 리스트: 키워드/짧은 문장, 반드시 한국어로 작성.")

# --- 에이전트 함수 ---

async def generate_exam_report_gemini(all_results: List[ExamQuestionResult]):
    """Gemini를 사용하여 15개 문항의 결과를 종합 분석"""
    import httpx
    
    # 1. 입력 데이터 요약
    summary_data = "\n".join([
        f"Q{r.question_order}: 점수(R:{r.relevance_score}, L:{r.logic_score}, V:{r.vocab_score}, G:{r.grammar_score}, F:{r.fluency_score}) / 피드백: {r.logic_feedback}"
        for r in all_results
    ])

    prompt = f"""
    당신은 오픽(OPIc) 전문 채점 위원입니다. 15개 문항의 모의고사 결과를 종합하여 최종 리포트를 작성하세요.
    - 등급(predicted_level)은 실제 OPIc 기준(AL, IH, IM3 ~ NH)으로 평가하세요.
    - 점수는 모두 정수(Integer)로 반환하세요.
    - **중요: total_score는 5개 평가 항목(Grammar, Vocab, Logic, Fluency, Relevance)의 평균 점수들을 모두 더한 뒤 5로 나눈 '최종 평균 점수' (100점 만점)입니다.**
    - overall_evaluation은 반드시 한국어로, 문항별 개별 피드백 내용을 꼭 참고해서, 전체적인 평가를 답변하세요.
    - 강점(strengths)과 보완점(improvements)은 반드시 한국어 답변으로, 각각 3개 내의 짧은 요약 문장 리스트로 작성하세요.
    
    [입력 데이터]
    {summary_data}
    """
    
    payload = {
        "contents": [{"parts": [{"text": prompt}]}],
        "generationConfig": {
            "response_mime_type": "application/json",
            "response_schema": {
                "type": "object",
                "properties": {
                    "total_score": {"type": "integer"},
                    "predicted_level": {"type": "string"},
                    "average_grammar_score": {"type": "integer"},
                    "average_vocab_score": {"type": "integer"},
                    "average_logic_score": {"type": "integer"},
                    "average_fluency_score": {"type": "integer"},
                    "average_relevance_score": {"type": "integer"},
                    "overall_evaluation": {"type": "string"},
                    "strengths": {"type": "array", "items": {"type": "string"}},
                    "improvements": {"type": "array", "items": {"type": "string"}}
                },
                "required": [
                    "total_score", "predicted_level", "overall_evaluation", "strengths", "improvements",
                    "average_grammar_score", "average_vocab_score", "average_logic_score", "average_fluency_score", "average_relevance_score"
                ]
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
    "/analyze/exam-feedback", 
    response_model=ExamReportResponse,
    summary="모의고사 종합 피드백 생성 (POST)",
    description="15개 문항의 결과 리스트를 받아 종합 피드백 생성"
)
async def create_exam_report(results: List[ExamQuestionResult]):
    """
    Client로부터 15개 문항의 분석 결과 리스트를 받아
    Gemini를 통해 종합 피드백을 생성하여 반환합니다.
    (DB 저장 로직 등은 필요에 따라 추가)
    """
    try:
        report_res = await generate_exam_report_gemini(results)
        return report_res

    except Exception as e:
        print(f"❌ Error: {str(e)}")
        raise HTTPException(status_code=500, detail="리포트 생성 중 오류가 발생했습니다.")
