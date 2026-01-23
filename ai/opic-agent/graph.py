from langchain_openai import ChatOpenAI
from langchain_core.prompts import ChatPromptTemplate
from langgraph.graph import StateGraph, START, END
from dotenv import load_dotenv

# 위에서 만든 파일들 불러오기
from schemas import GraphState, OpicQuestionSet, ValidationResult
from prompts import GENERATOR_SYSTEM_PROMPT, VALIDATOR_SYSTEM_PROMPT

load_dotenv() # .env 파일에서 환경변수(API KEY) 로드

# ============================================================
# [변경됨] 모델 설정: gpt-5-nano 적용 (temperature 제거)
# ============================================================
llm = ChatOpenAI(
    model="gpt-5-nano",
    # gpt-5-nano는 temperature 파라미터를 지원하지 않으므로 제거함
)

# --- Nodes ---
def generate_node(state: GraphState):
    print(f"\n--- [GENERATOR] Generating Questions (Attempt: {state['retry_count'] + 1}) ---")
    
    # gpt-5-nano의 강력한 JSON Mode/Structured Output 활용
    structured_llm = llm.with_structured_output(OpicQuestionSet)
    
    feedback_context = ""
    if state.get("validation_result") and not state["validation_result"].is_valid:
        feedback_context = f"\n<Previous Feedback>\n{state['validation_result'].feedback}\n</Previous Feedback>\nReflect this feedback and regenerate."

    prompt = ChatPromptTemplate.from_messages([
        ("system", GENERATOR_SYSTEM_PROMPT),
        ("user", "Topic: {topic}, Difficulty: {difficulty}" + feedback_context)
    ])
    
    chain = prompt | structured_llm
    result = chain.invoke({"topic": state["topic"], "difficulty": state["difficulty"]})
    
    return {"generated_output": result, "retry_count": state["retry_count"] + 1}

def validate_node(state: GraphState):
    print("\n--- [VALIDATOR] Reviewing Generated Questions ---")
    structured_llm = llm.with_structured_output(ValidationResult)
    
    questions_json = state["generated_output"].model_dump_json(indent=2)
    
    # [수정 전] f-string 사용으로 인해 JSON의 {}가 LangChain 변수로 오인됨
    # prompt = ChatPromptTemplate.from_messages([
    #     ("system", VALIDATOR_SYSTEM_PROMPT),
    #     ("user", f"Analyze this data:\n{questions_json}") 
    # ])
    # chain = prompt | structured_llm
    # result = chain.invoke({})

    # [수정 후] 데이터를 변수({input_data})로 처리하여 안전하게 주입
    prompt = ChatPromptTemplate.from_messages([
        ("system", VALIDATOR_SYSTEM_PROMPT),
        ("user", "Analyze this data:\n{input_data}") 
    ])
    
    chain = prompt | structured_llm
    
    # invoke 할 때 데이터를 딕셔너리로 전달
    result = chain.invoke({"input_data": questions_json})
    
    print(f"Validation Result: {result.is_valid} | Feedback: {result.feedback}")
    return {"validation_result": result}

# --- Edges ---
MAX_RETRIES = 3

def route_after_validation(state: GraphState):
    if state["validation_result"].is_valid:
        return "end"
    if state["retry_count"] >= MAX_RETRIES:
        print("--- Max retries reached. Stopping. ---")
        return "end"
    return "retry"

# --- Graph Construction ---
workflow = StateGraph(GraphState)
workflow.add_node("generator", generate_node)
workflow.add_node("validator", validate_node)

workflow.add_edge(START, "generator")
workflow.add_edge("generator", "validator")

workflow.add_conditional_edges(
    "validator",
    route_after_validation,
    {"retry": "generator", "end": END}
)

app = workflow.compile()