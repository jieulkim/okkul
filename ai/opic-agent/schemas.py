from typing import List, Optional, TypedDict, Any, Dict
from pydantic import BaseModel, Field, validator
import re
# 개별 질문 아이템
class QuestionItem(BaseModel):
    order: int = Field(description="The order of the question (1, 2, 3...)")
    text: str = Field(description="The question text in English")
    # type_tag는 필수 아님 (생성자가 굳이 안 만들어도 됨)

# 전체 콘텐츠 구조
class OpicContent(BaseModel):
    topic: str
    difficulty: int
    gen_mode: str
    questions: List[QuestionItem]

    # 🛠️ [핵심] 개수 강제 검증 로직 (여기서 1차 방어)
    @validator('questions')
    def validate_count_by_mode(cls, v, values):
        # 1. gen_mode 가져오기
        gen_mode = values.get('gen_mode', '')
        
        # 2. 모드 이름 끝의 숫자 추출 (AD2 -> 2, COMBO3 -> 3)
        match = re.search(r'(\d+)$', gen_mode)
        
        if match:
            target_count = int(match.group(1))
            
            # 3. 실제 생성된 개수와 비교
            if len(v) != target_count:
                # 에러 발생 -> LangChain이 자동으로 Retry 수행
                raise ValueError(
                    f"❌ COUNT ERROR: Mode '{gen_mode}' requires EXACTLY {target_count} questions, "
                    f"but you generated {len(v)}. "
                    f"Please generate exactly {target_count} items in the list."
                )
        
        return v
# ==========================================
# 2. SQL 변환용 스키마
# ==========================================
class OpicSQLSet(BaseModel):
    sql_query: str = Field(description="Complete SQL INSERT statements to save the question set and questions into the database.")

# ==========================================
# 3. 검증 결과 스키마
# ==========================================
class ValidationResult(BaseModel):
    is_valid: bool = Field(description="True if valid, False if rules are violated")
    feedback: str = Field(description="Feedback explaining the violation or 'Perfect'")

# ==========================================
# 4. LangGraph State (상태 관리)
# ==========================================
class GraphState(TypedDict):
    # Input
    topic: str
    difficulty: str
    gen_mode: str
    
    # Internal Logic
    content_prompt: str
    retry_count: int
    
    # Outputs per Node
    generated_content: Optional[OpicContent]
    generated_sql: Optional[OpicSQLSet]
    validation_result: Optional[ValidationResult]
    
    # [NEW] 성능 지표 로그 (시간, 토큰 등)
    logs: List[Dict[str, Any]]