import os
import psycopg2
from dotenv import load_dotenv

load_dotenv()

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

def get_all_topic_codes():
    """
    DB에서 모든 Topic Code 리스트를 반환
    """
    conn = get_db_connection()
    if not conn:
        print("❌ DB 연결 실패")
        return []
        
    cur = conn.cursor()
    try:
        sql = "SELECT topic_code FROM Topic ORDER BY topic_id"
        cur.execute(sql)
        results = cur.fetchall()
        return [row[0] for row in results]
    finally:
        cur.close()
        conn.close()

def execute_generated_sql(generated_sql: str):
    """
    AI가 생성한 SQL 쿼리를 받아 실행하는 함수
    (수정됨: 세미콜론 분리 로직 제거 - 텍스트 내부의 ;로 인한 오류 방지)
    """
    conn = get_db_connection()
    if not conn: 
        return False

    try:
        with conn:
            with conn.cursor() as cur:
                # 🛠️ [FIX] split(';') 제거! 
                # 프롬프트가 하나의 완결된 WITH 문을 생성하므로 통째로 실행합니다.
                # 혹시라도 뒤에 무의미한 세미콜론이 있어도 psycopg2가 처리하거나 무시합니다.
                query_to_run = generated_sql.strip()
                
                # 만약 끝에 세미콜론이 있으면 제거 (psycopg2는 1개의 명령만 실행 선호)
                if query_to_run.endswith(';'):
                    query_to_run = query_to_run[:-1]

                print(f"🚀 Executing Query...")
                cur.execute(query_to_run)
                
        print("✅ Generated SQL executed successfully!")
        return True

    except Exception as e:
        print(f"\n❌ [SQL Error] Failed Query:\n{generated_sql}")
        print(f"❌ Error Message: {e}")
        return False
    finally:
        if conn:
            conn.close()