import requests
import json

def test_ai_analysis():
    url = "http://localhost:8000/v1/analyze"
    payload = {
        "question_text": "How often do you go hiking and who do you usually go with?",
        "user_answer": "I go to hiking once a week. I usually go with my friends. It was very fun."
    }
    
    print("🚀 AI 분석 요청 중...")
    response = requests.post(url, json=payload)
    
    if response.status_code == 200:
        result = response.json()
        print("\n✅ [종합 피드백]")
        print(f"개선된 전체 답변: {result['improved_answer']}")
        print(f"논리성: {result['logic_feedback']}")
        
        print("\n✅ [문장별 상세 교정]")
        for detail in result['sentence_details']:
            print(f"- 원래 문장: {detail['target_sentence']}")
            print(f"  수정 구간: {detail['target_text']} -> {detail['improved_text']}")
            print(f"  이유: {detail['feedback']}")
    else:
        print(f"❌ 에러 발생: {response.text}")

if __name__ == "__main__":
    test_ai_analysis()