# 파일명: convert_all_audio.py
import sys
from database import get_db_connection
from text_to_speech import convert_sentences_to_audio

# [수정] 시작 ID와 끝 ID를 인자로 받도록 변경
def fetch_questions_by_range(start_id, end_id):
    """
    DB에서 start_id ~ end_id (양쪽 모두 포함) 범위의 문제들을 조회합니다.
    """
    conn = get_db_connection()
    if not conn:
        print("❌ DB 연결 실패")
        return []

    try:
        with conn.cursor() as cur:
            print(f"🔍 DB에서 ID {start_id}번 부터 {end_id}번 까지 데이터를 조회하는 중...")
            
            # [수정] WHERE 절을 수정하여 특정 범위(BETWEEN)만 가져오게 변경
            sql = """
            SELECT question_id, question_text 
            FROM question_bank 
            WHERE question_id BETWEEN %s AND %s
            ORDER BY question_id ASC;
            """
            cur.execute(sql, (start_id, end_id))
            rows = cur.fetchall()
            
            # [{'id': 1519, 'text': '...'}, ...] 형태로 변환
            questions = [{"id": row[0], "text": row[1]} for row in rows]
            
            print(f"✅ 총 {len(questions)}개의 데이터를 찾았습니다. (ID {start_id} ~ {end_id})")
            return questions

    except Exception as e:
        print(f"❌ 데이터 조회 실패: {e}")
        return []
    finally:
        conn.close()

if __name__ == "__main__":
    # ==========================================
    # ⚡ 여기 숫자를 바꾸면 범위가 바뀝니다.
    # ==========================================
    START_ID = 5401  # <--- [수정] 여기를 2501로 변경했습니다.
    END_ID = 5610   # <--- [수정] 끝 번호를 넉넉하게 늘렸습니다.

    # 1. DB에서 특정 범위의 데이터 가져오기
    target_data = fetch_questions_by_range(START_ID, END_ID)
    
    # 2. 데이터가 있으면 변환 시작
    if target_data:
        # 폴더명은 그대로 유지 ("opic_audio_all") 하거나 원하면 변경
        convert_sentences_to_audio(target_data, output_folder="opic_audio_all")
    else:
        print("⚠️ 변환할 데이터가 없습니다.")