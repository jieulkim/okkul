import requests
import json
import time

def test_exam_report_column_details():
    url = "http://localhost:8000/v1/analyze/exam-feedback"
    
    # 더미 데이터 생성 (15개 문항 중 3개 예시)
    dummy_payload = [
        {
            "question_order": i,
            "stt_script": "I go hiking once a week and I think it is good for my health.",
            "improved_answer": "Well, I usually go hiking once a week because I believe it's beneficial for my health.",
            "relevance_score": 55,
            "logic_score": 57,
            "vocab_score": 59,
            "grammar_score": 61,
            "fluency_score": 63,
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
            print(f"▶ [predicted_level] 예측 등급   : {result.get('predicted_level')}")
            print(f"▶ [total_score] 전체 점수       : {result.get('total_score')}")
            
            print("-" * 60)
            
            # 2. 평균 점수 (Numerical Metrics)
            print(f"▶ [average_grammar_score] 문법 평균 : {result.get('average_grammar_score')}")
            print(f"▶ [average_vocab_score] 어휘 평균   : {result.get('average_vocab_score')}")
            print(f"▶ [average_logic_score] 논리 평균   : {result.get('average_logic_score')}")
            print(f"▶ [average_fluency_score] 유창성 평균: {result.get('average_fluency_score')}")
            print(f"▶ [average_relevance_score] 적합성 평균: {result.get('average_relevance_score')}")
            
            print("-" * 60)
            
            # 3. 텍스트 분석 결과 (Text Analysis)
            print(f"▶ [strengths] 강점 리스트 :")
            for s in result.get('strengths', []):
                print(f"   - {s}")
            
            print(f"\n▶ [improvements] 보완점 리스트 :")
            for w in result.get('improvements', []):
                print(f"   - {w}")
            
            print(f"\n▶ [overall_evaluation] 전체 총평 :")
            # 긴 텍스트 가독성을 위해 줄바꿈 처리
            wrapped_comment = result.get('overall_evaluation', '').replace('. ', '.\n      ')
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