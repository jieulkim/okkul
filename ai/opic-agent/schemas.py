from typing import List, Optional, TypedDict
from pydantic import BaseModel, Field

# ==========================================
# 1. Pydantic 모델 (LLM 출력 구조 정의)
# ==========================================

class OpicQuestion(BaseModel):
    order: int = Field(description="문제 순서 (1, 2, 3)")
    type_id: int = Field(description="문제 유형 ID (1:자기소개, 2:묘사, 3:루틴, 4:비교, 5:경험, 6:롤플레이, 7:어드밴스)")
    question_en: str = Field(description="영어 질문 텍스트")
    question_kr: str = Field(description="한국어 질문 해석")

class OpicQuestionSet(BaseModel):
    topic: str = Field(description="주제 이름")
    difficulty: str = Field(description="난이도 (IM, IH, AL 등)")
    
    # [중요] 이 부분이 빠져있어서 에러가 났던 것입니다.
    dominant_type_id: int = Field(description="세트의 대표 유형 ID (콤보는 1번문제 유형, RP는 6, ADV는 7)")
    
    questions: List[OpicQuestion] = Field(description="생성된 문제 리스트")

class ValidationResult(BaseModel):
    is_valid: bool = Field(description="검증 통과 여부 (True: 통과, False: 반려)")
    feedback: str = Field(description="통과 시 'Perfect', 반려 시 구체적인 수정 요청 사항")

# ==========================================
# 2. LangGraph State (상태 관리)
# ==========================================
class GraphState(TypedDict):
    topic: str
    difficulty: str
    retry_count: int
    generated_output: Optional[OpicQuestionSet] # 생성된 문제 세트
    validation_result: Optional[ValidationResult] # 검증 결과