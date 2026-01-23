import sys
from graph import app
from database import save_question_set, get_db_connection

# -------------------------------------------------------------------------
# [Helper] DB에서 주제 정보(ID, 이름) 가져오기
# -------------------------------------------------------------------------
def get_topic_info(topic_code_input):
    """
    Topic Code(예: 'PARK')를 입력받아 DB의 ID(106)와 한글명('공원가기')을 반환
    """
    conn = get_db_connection()
    if not conn:
        print("❌ DB 연결 실패")
        return None
        
    cur = conn.cursor()
    try:
        # Topic 테이블 조회
        sql = "SELECT topic_id, topic_name FROM Topic WHERE topic_code = %s"
        cur.execute(sql, (topic_code_input,))
        result = cur.fetchone()
        
        if result:
            return {"id": result[0], "name": result[1]}
        else:
            return None
    finally:
        cur.close()
        conn.close()

# -------------------------------------------------------------------------
# [Main] 실행 로직
# -------------------------------------------------------------------------
if __name__ == "__main__":

    # =====================================================================
    # 🎛️ [설정 패널] 여기서 원하는 문제 설정을 바꾸세요!
    # =====================================================================
    TARGET_CODE = "WATCH_MOVIE"      # DB에 있는 Topic Code (예: PARK, MUSIC, JOGGING)
    TARGET_DIFF = "IH"        # 난이도 (IM, IH, AL)
    
    # 생성 모드 선택: "COMBO" | "ROLEPLAY" | "ADVANCE" | "INTRO"
    GEN_MODE = "COMBO" 
    # =====================================================================

    # 1. DB에서 주제 정보 조회
    print(f"🔍 DB에서 '{TARGET_CODE}' 주제 검색 중...")
    topic_info = get_topic_info(TARGET_CODE)
    
    if not topic_info:
        print(f"❌ Error: DB에서 주제 코드 '{TARGET_CODE}'를 찾을 수 없습니다.")
        print("   (힌트: DB의 Topic 테이블에 해당 코드가 있는지 확인하세요.)")
        sys.exit()

    topic_id = topic_info['id']
    topic_name_kr = topic_info['name']
    
    print(f"👉 타겟 주제: {topic_name_kr} (ID: {topic_id})")
    print(f"👉 생성 모드: {GEN_MODE} | 난이도: {TARGET_DIFF}")

    # 2. 에이전트에게 전달할 '프롬프트용 주제 텍스트' 가공
    # (에이전트가 모드를 인식할 수 있도록 힌트를 붙여줍니다)
    prompt_topic_text = topic_name_kr
    
    if GEN_MODE == "ROLEPLAY":
        prompt_topic_text += " (Create a Roleplay set)"
    elif GEN_MODE == "ADVANCE":
        prompt_topic_text += " (Create an Advance set)"
    elif GEN_MODE == "INTRO":
        prompt_topic_text = "Self Introduction" # 자기소개는 주제명이 고정됨

    # 3. LangGraph 초기 상태 설정
    initial_state = {
        "topic": prompt_topic_text,
        "difficulty": TARGET_DIFF,
        "retry_count": 0,
        "generated_output": None,
        "validation_result": None
    }
    
    # 4. 그래프 실행 (생성 -> 검증 루프)
    print("\n🤖 에이전트 작업 시작...")
    final_state = app.invoke(initial_state)
    
    # 5. 결과 확인 및 DB 저장
    if final_state["validation_result"] and final_state["validation_result"].is_valid:
        print("\n✅ [성공] 유효한 문제 세트가 생성되었습니다!")
        
        output_data = final_state["generated_output"]
        
        # 결과 미리보기 출력
        print("-" * 50)
        print(f"📄 주제: {output_data.topic}")
        print(f"📊 대표유형 ID: {output_data.dominant_type_id}")
        for q in output_data.questions:
            print(f"   Q{q.order} [{q.type_id}]: {q.question_en[:50]}...")
        print("-" * 50)

        # DB 저장 함수 호출
        print(f"💾 Database(PostgreSQL)에 저장 중... (Topic ID: {topic_id})")
        save_question_set(topic_id, output_data)
        
    else:
        print("\n❌ [실패] 재시도 횟수를 초과했거나 생성에 실패했습니다.")
        if final_state["validation_result"]:
            print(f"   사유: {final_state['validation_result'].feedback}")