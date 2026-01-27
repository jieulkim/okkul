# graph.py

import time
import json # json 추가
from langchain_community.callbacks import get_openai_callback
from langchain_openai import ChatOpenAI
from langchain_core.prompts import ChatPromptTemplate
from langgraph.graph import StateGraph, START, END
from dotenv import load_dotenv

from schemas import GraphState, OpicContent, OpicSQLSet, ValidationResult
from prompts import get_content_prompt, VALIDATOR_SYSTEM_PROMPT

load_dotenv()

# 모델 설정 (gpt-5-nano 유지 또는 gpt-4o-mini 등 가벼운 모델 권장)
llm = ChatOpenAI(model="gpt-4o-mini", temperature=0.7) 

# ============================================================
# Helper Function: Python으로 SQL 생성 (0.01초 소요)
# ============================================================
def convert_to_sql_fast(content: OpicContent) -> str:
    # 1. Topic ID, Type ID를 가져오는 서브쿼리 준비
    topic_subquery = f"(SELECT topic_id FROM Topic WHERE topic_name = '{content.topic}' LIMIT 1)"
    type_subquery = f"(SELECT type_id FROM questiontype WHERE type_code = '{content.gen_mode}' LIMIT 1)"
    
    # 2. Values 문자열 생성
    # 텍스트 내의 싱글 쿼트(')만 이스케이프 처리 (' -> '')
    values_list = []
    for q in content.questions:
        safe_text = q.text.replace("'", "''")
        values_list.append(f"('{safe_text}', {q.order})")
    
    values_str = ",\n  ".join(values_list)

    # 3. 최종 SQL 조립
    sql = f"""
WITH new_set AS (
  INSERT INTO question_set (level, question_cnt, topic_id, type_id)
  VALUES ({content.difficulty}, {len(content.questions)}, {topic_subquery}, {type_subquery})
  RETURNING set_id
)
INSERT INTO question_bank (question_text, audio_url, "order", set_id)
SELECT q.question_text, '' AS audio_url, q."order", new_set.set_id
FROM (VALUES 
  {values_str}
) AS q(question_text, "order")
CROSS JOIN new_set;
    """
    return sql.strip()

# ============================================================
# Nodes
# ============================================================

def content_node(state: GraphState):
    """Step 1: 문제 내용 생성"""
    print(f"\n--- [1. CONTENT] Designing Questions ({state['gen_mode']}) ---")
    start_time = time.time()
    
    structured_llm = llm.with_structured_output(OpicContent)
    
    # 피드백 반영
    feedback_msg = ""
    if state.get("validation_result") and not state["validation_result"].is_valid:
        feedback_msg = f"\n[FEEDBACK]: {state['validation_result'].feedback}\n(Please fix the content logic.)"

    final_prompt = state["content_prompt"] + feedback_msg
    
    chain = ChatPromptTemplate.from_messages([
        ("system", final_prompt),
        ("user", "Generate the questions now.")
    ]) | structured_llm

    with get_openai_callback() as cb:
        result = chain.invoke({})
    
    elapsed = round(time.time() - start_time, 2)
    
    # [NEW] 여기서 바로 SQL 변환 수행 (LLM 안씀)
    print(f"⚡ Converting to SQL (Code)...")
    sql_text = convert_to_sql_fast(result)
    generated_sql_obj = OpicSQLSet(sql_query=sql_text)

    # 로그 저장
    new_log = {
        "step": "1. Content + SQL",
        "time_sec": elapsed,
        "total_tokens": cb.total_tokens,
        "cost_usd": f"${cb.total_cost:.5f}"
    }
    
    current_logs = state.get("logs", []) or []
    current_logs.append(new_log)

    # generated_sql까지 한 번에 반환
    return {
        "generated_content": result, 
        "generated_sql": generated_sql_obj,
        "retry_count": state["retry_count"] + 1, 
        "logs": current_logs
    }

def validate_node(state: GraphState):
    """Step 2: 로직 검수 (SQL 문법 검사는 제외, 내용만 검수)"""
    print("--- [2. VALIDATOR] Reviewing Content Logic ---")
    start_time = time.time()

    structured_llm = llm.with_structured_output(ValidationResult)
    
    # 검수 대상: 만들어진 '질문 내용'만 확인하면 됨 (SQL은 코드로 짰으니 문법 오류 없음)
    content = state["generated_content"]
    questions_text = "\n".join([f"Order {q.order}: {q.text}" for q in content.questions])

    check_input = f"""
    Target Mode: {state['gen_mode']}
    Questions Generated:
    {questions_text}
    
    Check if the 'Order Rules' are followed correctly (e.g., Past Experience at the correct order).
    """
    
    chain = ChatPromptTemplate.from_messages([
        ("system", VALIDATOR_SYSTEM_PROMPT),
        ("user", check_input)
    ]) | structured_llm
    
    with get_openai_callback() as cb:
        result = chain.invoke({})
    
    elapsed = round(time.time() - start_time, 2)
    print(f"Result: {result.is_valid} | Msg: {result.feedback}")

    new_log = {
        "step": "2. Validator",
        "time_sec": elapsed,
        "total_tokens": cb.total_tokens,
        "cost_usd": f"${cb.total_cost:.5f}"
    }
    
    current_logs = state.get("logs", [])
    current_logs.append(new_log)

    return {"validation_result": result, "logs": current_logs}

# ============================================================
# Graph Construction
# ============================================================
MAX_RETRIES = 3

def route_after_validation(state: GraphState):
    if state["validation_result"].is_valid:
        return "end"
    if state["retry_count"] >= MAX_RETRIES:
        print("--- Max retries reached. ---")
        return "end"
    return "retry"

workflow = StateGraph(GraphState)

# 노드가 3개에서 2개로 줄었습니다!
workflow.add_node("content_generator", content_node)
workflow.add_node("validator", validate_node)

workflow.add_edge(START, "content_generator")
workflow.add_edge("content_generator", "validator")

workflow.add_conditional_edges(
    "validator",
    route_after_validation,
    {"retry": "content_generator", "end": END}
)

app = workflow.compile()