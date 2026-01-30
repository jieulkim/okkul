import requests
import json
import time

def test_question_analysis_api():
    # 1. URL 확인: 서버의 prefix 설정(/v1)과 엔드포인트(/analyze/exam)가 합쳐진 주소
    url = "http://localhost:8000/v1/analyze/exam"
    
    test_cases = [
        {
            "desc": "잘한 답변 (Good Case)",
            "payload": {
                "original_question": "Tell me about your favorite musician.",
                "user_answer": "I am a huge fan of BTS. They are not just musicians but cultural icons. Their lyrics always inspire me to love myself. Especially, the song 'Spring Day' has a beautiful melody and deep meaning. I've been to their concert last year, and it was absolutely stunning. You know, their performance is world-class.",
                "question_type": "Description",
                "difficulty": 5,
                "question_order": 1,
                "audio_url": "http://example.com/audio/test.mp3" # 추가된 필드
            }
        },
        {
            "desc": "부족한 답변 (Poor Case)",
            "payload": {
                "original_question": "Tell me about your favorite musician.",
                "user_answer": "My favorite musician is BTS. I like them. They sing very well. I listen every day. Good good.",
                "question_type": "Description",
                "difficulty": 5,
                "question_order": 2,
                "audio_url": None
            }
        }
    ]

    print(f"🚀 [Test] 통합 분석 API (/analyze/exam) 테스트 시작...\n")
    
    for case in test_cases:
        print(f"▶ Case: {case['desc']}")
        
        try:
            start_time = time.time()
            response = requests.post(url, json=case['payload'], timeout=60.0)
            duration = time.time() - start_time
            
            if response.status_code == 200:
                result = response.json()
                print(f"   ✅ Success ({duration:.2f}s)")
                
                # 2. 통합된 5대 점수 출력
                print(f"   [점수 결과]")
                print(f"   - Grammar   : {result['grammarScore']}")
                print(f"   - Vocab     : {result['vocabScore']}")
                print(f"   - Logic     : {result['logicScore']}")
                print(f"   - Fluency   : {result['fluencyScore']}")
                print(f"   - Relevance : {result['relevanceScore']}")
                
                # 3. 추가된 텍스트 피드백 출력
                print(f"\n   [항목별 상세 피드백]")
                print(f"   - Logic Feedback: {result['logicFeedback'][:100]}...")
                print(f"   - Fluency Feedback: {result['fluencyFeedback'][:100]}...")
                print(f"   - Relevance Feedback: {result['relevanceFeedback'][:100]}...")
                
                # 4. 모범 답안 출력
                print(f"\n   [AI AL-Level 모범 답안]")
                print(f"   => {result['improvedAnswer']}")
                
                # 5. 문장별 교정 내역 출력 (sentenceFeedbacks)
                fbs = result.get('sentenceFeedbacks', [])
                print(f"\n   [문장 교정 상세 ({len(fbs)}개)]")
                for fb in fbs[:2]: # 상위 2개만 예시 출력
                    print(f"   - [{fb['sentence_order']}] {fb['target_text']} → {fb['improved_text']}")
                    print(f"     (이유: {fb['feedback']})")
                    
            else:
                print(f"   ❌ Failed ({response.status_code})")
                print(f"   Msg: {response.text}")
                
        except Exception as e:
            print(f"   ❌ Connection Error: {e}")
            
        print("-" * 60)

if __name__ == "__main__":
    test_question_analysis_api()