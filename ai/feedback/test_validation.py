from pydantic import BaseModel, Field, ValidationError
from typing import List
import json

# Reproduce the model definition from total_feedback.py
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

# Sample data mimicking what the user claims is being sent (snake_case)
sample_data = [
    {
        "question_order": 1,
        "stt_script": "I like pizza",
        "improved_answer": "I enjoy pizza",
        "relevance_score": 90,
        "logic_score": 85,
        "vocab_score": 70,
        "grammar_score": 80,
        "fluency_score": 75,
        "logic_feedback": "Good job"
    }
]

try:
    # Attempt to validate a list of these objects
    # Note: The API endpoint takes List[ExamQuestionResult]
    validated = [ExamQuestionResult(**item) for item in sample_data]
    print("✅ Validation Successful!")
    print(validated)
except ValidationError as e:
    print("❌ Validation Failed!")
    print(e.json())
