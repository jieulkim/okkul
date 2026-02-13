import os
import datetime
from database import get_db_connection

# ==========================================
# [1] ERD 구조 정의 (정석 버전)
# ==========================================

# 부모 테이블 (question_set)
CREATE_TABLE_SET = """
CREATE TABLE IF NOT EXISTS question_set (
    set_id BIGINT PRIMARY KEY,      -- 🟡 노란키 (PK)
    level INT NOT NULL,
    question_cnt INT,
    topic_id BIGINT NOT NULL,       -- 🔴 빨간키 (논리적 FK)
    type_id BIGINT NOT NULL         -- 🔴 빨간키 (논리적 FK)
);
"""

# 자식 테이블 (question_bank)
CREATE_TABLE_BANK = """
CREATE TABLE IF NOT EXISTS question_bank (
    question_id BIGINT PRIMARY KEY, -- 🟡 노란키 (PK)
    question_text TEXT NOT NULL,
    audio_url TEXT NOT NULL,
    "order" INT,                    -- 예약어라 따옴표 처리
    set_id BIGINT NOT NULL,         -- 🔴 빨간키 (물리적 FK)
    created_at TIMESTAMP NOT NULL,
    
    -- 🔗 관계 설정 (빨간키 기능 구현)
    CONSTRAINT fk_question_set
        FOREIGN KEY(set_id) 
        REFERENCES question_set(set_id)
        ON DELETE CASCADE
);
"""

def format_value(value):
    """
    Python 값을 SQL 쿼리용 문자열로 변환
    - 날짜(datetime)는 자동으로 따옴표('')를 씌워줍니다.
    """
    if value is None:
        return "NULL"
    
    if isinstance(value, str):
        # 문자열 내의 작은따옴표(')를 SQL에서 인식하도록 두 개('')로 변경
        safe_str = value.replace("'", "''")
        return f"'{safe_str}'"
    
    if isinstance(value, (datetime.date, datetime.datetime)):
        # ★ [핵심] 날짜 객체는 자동으로 따옴표를 씌워서 반환
        return f"'{value}'"
    
    return str(value)

def save_table_to_file(cursor, table_name, create_sql, filename):
    """
    특정 테이블의 스키마(CREATE)와 데이터(INSERT)를 SQL 파일로 저장
    """
    print(f"📝 '{filename}' 저장 중...")
    
    with open(filename, "w", encoding="utf-8") as f:
        # 헤더 작성
        f.write(f"-- Backup for table: {table_name}\n")
        f.write(f"-- Date: {datetime.datetime.now()}\n\n")
        
        # 1. 테이블 삭제 (재생성을 위해)
        # CASCADE: 이 테이블을 참조하는 다른 제약조건도 같이 삭제 (에러 방지)
        f.write(f"DROP TABLE IF EXISTS {table_name} CASCADE;\n\n") 
        
        # 2. 테이블 생성
        f.write(create_sql + "\n\n")
        
        # 3. 데이터 조회 (PK 기준 정렬)
        cursor.execute(f"SELECT * FROM {table_name} ORDER BY 1 ASC")
        rows = cursor.fetchall()
        
        # 컬럼명 가져오기 ("order" 예약어 처리)
        col_names = [f'"{d[0]}"' if d[0]=="order" else d[0] for d in cursor.description]
        cols_str = ", ".join(col_names)
        
        # 4. INSERT 문 작성
        if rows:
            f.write(f"-- Data ({len(rows)} rows)\n")
            for row in rows:
                vals = [format_value(v) for v in row]
                f.write(f"INSERT INTO {table_name} ({cols_str}) VALUES ({', '.join(vals)});\n")
            print(f"   👉 {len(rows)}개 데이터 저장 완료")
        else:
            f.write("-- No data found.\n")
            print("   👉 데이터 없음")

    print(f"✅ 완료: {filename}\n")

def main():
    conn = get_db_connection()
    if not conn:
        print("❌ DB 연결 실패")
        return

    try:
        with conn.cursor() as cur:
            print("🚀 데이터 백업을 시작합니다 (파일 2개 생성)...")
            print("=" * 40)
            
            # [파일 1] Set 저장 (부모)
            save_table_to_file(cur, "question_set", CREATE_TABLE_SET, "backup_question_set.sql")
            
            # [파일 2] Bank 저장 (자식)
            save_table_to_file(cur, "question_bank", CREATE_TABLE_BANK, "backup_question_bank.sql")
            
            print("=" * 40)
            
    except Exception as e:
        print(f"❌ 오류 발생: {e}")
    finally:
        conn.close()
        print("✨ 모든 작업이 끝났습니다.")

if __name__ == "__main__":
    main()