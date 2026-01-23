import os
import psycopg2
from dotenv import load_dotenv

load_dotenv()

# OPIc 난이도 매핑
LEVEL_MAPPING = {"IM": 4, "IH": 7, "AL": 8}

def get_db_connection():
    try:
        return psycopg2.connect(
            host=os.getenv("DB_HOST", "localhost"),
            database=os.getenv("DB_NAME", "opic_db"),
            user=os.getenv("DB_USER", "postgres"),
            password=os.getenv("DB_PASSWORD"),
            port=os.getenv("DB_PORT", "5432")
        )
    except Exception as e:
        print(f"❌ Connection Error: {e}")
        return None

def save_question_set(topic_id: int, generated_data):
    """
    JSON 데이터를 받아 실제 SQL Insert를 수행하는 함수
    """
    conn = get_db_connection()
    if not conn: return

    try:
        cur = conn.cursor()
        
        # ---------------------------------------------------------
        # 1. Question Set (부모) 저장
        # ---------------------------------------------------------
        # 난이도 변환
        level_int = LEVEL_MAPPING.get(generated_data.difficulty, 4)
        
        # dominant_type_id: 콤보인 경우 첫 문제 유형, RP/ADV는 6/7
        dom_type = generated_data.dominant_type_id
        
        insert_set_sql = """
            INSERT INTO question_set (level, question_cnt, topic_id, type_id)
            VALUES (%s, %s, %s, %s)
            RETURNING set_id;
        """
        
        cur.execute(insert_set_sql, (
            level_int, 
            len(generated_data.questions), 
            topic_id, 
            dom_type
        ))
        
        # 방금 생성된 set_id 획득 (이게 있어야 자식 문제들을 연결함)
        set_id = cur.fetchone()[0]
        print(f"✅ [Set Created] ID: {set_id} | Type: {dom_type}")

        # ---------------------------------------------------------
        # 2. Question Bank (자식) 저장
        # ---------------------------------------------------------
        insert_q_sql = """
            INSERT INTO question_bank (question_text, audio_url, "order", set_id)
            VALUES (%s, %s, %s, %s);
        """
        
        # 유형 ID별 라벨 (저장 텍스트용)
        type_labels = {
            1: "Intro", 2: "Desc", 3: "Routine", 
            4: "Comp", 5: "Past", 6: "RP", 7: "Adv"
        }

        for q in generated_data.questions:
            # 텍스트 포맷: "[Desc] 영어질문 (해석: 한글)"
            label = type_labels.get(q.type_id, "Gen")
            formatted_text = f"[{label}] {q.question_en}\n(해석: {q.question_kr})"
            
            # SQL 실행
            cur.execute(insert_q_sql, (formatted_text, "", q.order, set_id))

        # ---------------------------------------------------------
        # 3. Commit (모두 성공 시 저장)
        # ---------------------------------------------------------
        conn.commit()
        print(f"🎉 Saved {len(generated_data.questions)} questions to Set {set_id}!")

    except Exception as e:
        conn.rollback() # 에러 나면 취소
        print(f"❌ DB Save Error: {e}")
    finally:
        cur.close()
        conn.close()