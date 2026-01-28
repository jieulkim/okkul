import os
from openai import OpenAI
from dotenv import load_dotenv

load_dotenv()

# 1. 환경 변수에서 GMS_KEY 가져오기
GMS_KEY = os.environ.get("GMS_KEY")
print(GMS_KEY)
# 2. 클라이언트 초기화 (성공했던 RAG 코드와 동일한 방식)
# 주의: 마지막에 /v1을 붙여야 라이브러리가 그 뒤의 경로를 제대로 생성합니다.
client = OpenAI(
    api_key=GMS_KEY,
    base_url="https://gms.ssafy.io/gmsapi/api.openai.com/v1"
)

def generate_tts(text, output_file="output.mp3"):
    try:
        print(f"🎙️ GMS를 통해 음성 생성 중: {text[:20]}...")
        
        # 성공했던 방식처럼 SDK 메서드 호출
        response = client.audio.speech.create(
            model="gpt-4o-mini-tts",
            voice="nova",
            input=text
        )

        # 파일 저장
        response.stream_to_file(output_file)
        print(f"✅ 생성 완료: {output_file}")
        
    except Exception as e:
        print(f"❌ TTS 에러 발생: {e}")

# 실행 테스트
if __name__ == "__main__":
    test_text = "Tell me about the living conditions in a military barracks. What are the facilities like and how do they accommodate the soldiers?"
    generate_tts(test_text)