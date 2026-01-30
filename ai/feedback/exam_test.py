import requests
import json
import time

def test_exam_report_column_details():
    url = "http://localhost:8000/v1/exam/report"
    
    # 더미 데이터 생성 (15개 문항)
    dummy_payload = [
        {
            "question_order": i,
            "stt_script": "I go hiking once a week and I think it is good for my health.",
            "improved_answer": "Well, I usually go hiking once a week because I believe it's beneficial for my health.",
            "relevance_score": 85,
            "logic_score": 80,
            "vocab_score": 75,
            "grammar_score": 70,
            "fluency_score": 90,
            "logic_feedback": "전반적으로 양호하나 시제 일관성이 부족함."
        } for i in range(1, 16)
    ]

    print(f"🚀 [Dummy Data] 리포트 생성 및 컬럼별 데이터 분석 요청 중...")
    
    try:
        response = requests.post(url, json=dummy_payload, timeout=60.0)
        
        if response.status_code == 200:
            result = response.json()
            
            print("\n" + "=".center(60, "="))
            print(f" DATABASE COLUMN MAPPING REPORT ".center(60, " "))
            print("=".center(60, "="))
            
            # 1. 기본 정보 및 예측 결과
            print(f"▶ [grade] 예측 등급        : {result.get('grade')}")
            print(f"▶ [total_score] 전체 점수   : {result.get('total_score'):.2f}")
            
            print("-" * 60)
            
            # 2. 평균 점수 (Numerical Metrics)
            print(f"▶ [avg_grammar] 문법 평균   : {result.get('avg_grammar'):.2f}")
            print(f"▶ [avg_vocab] 어휘 평균     : {result.get('avg_vocab'):.2f}")
            print(f"▶ [avg_logic] 논리 평균     : {result.get('avg_logic'):.2f}")
            print(f"▶ [avg_fluency] 유창성 평균 : {result.get('avg_fluency'):.2f}")
            print(f"▶ [avg_relevance] 적합성 평균: {result.get('avg_relevance'):.2f}")
            
            print("-" * 60)
            
            # 3. 텍스트 분석 결과 (Text Analysis)
            print(f"▶ [strength_type] 전체적 강점 유형 :")
            print(f"   => {result.get('strength_type')}")
            
            print(f"\n▶ [weakness_type] 전체적 약점 유형 :")
            print(f"   => {result.get('weakness_type')}")
            
            print(f"\n▶ [comment] 전체 총평 내용 :")
            # 긴 텍스트 가독성을 위해 줄바꿈 처리
            wrapped_comment = result.get('comment', '').replace('. ', '.\n      ')
            print(f"      {wrapped_comment}")
            
            print("=".center(60, "="))
            print(f" 분석 완료 시각: {time.strftime('%Y-%m-%d %H:%M:%S')} ".center(60, " "))
            print("=".center(60, "="))
            
        else:
            print(f"❌ 서버 에러 발생 ({response.status_code}): {response.text}")
            
    except requests.exceptions.RequestException as e:
        print(f"❌ 연결 에러: {e}")

if __name__ == "__main__":
    test_exam_report_column_details()