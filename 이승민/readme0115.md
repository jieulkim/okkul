OPIc 대비 AI 에이전트 개발 가이드
1. 프로젝트 개요
Python과 LangChain을 활용하여 OPIc 면접관 'Eva'를 시뮬레이션하는 AI 챗봇을 개발합니다. 사용자의 답변을 듣고 **피드백(교정)**과 꼬리 질문을 제공하는 것이 핵심 기능입니다.

2. 개발 환경 준비
• 언어: Python 3.10+

• 필수 라이브러리: `langchain`, `langchain-openai`, `python-dotenv`

• API Key: OpenAI API Key (GPT-4o 모델 권장)

3. 구현 단계
Step 1: 라이브러리 설치

`requirements.txt` 파일을 생성하고 다음 내용을 작성합니다.

```

langchain

langchain-openai

python-dotenv

```

터미널에서 설치: `pip install -r requirements.txt`

Step 2: 환경 변수 설정

`.env` 파일을 생성하여 API 키를 저장합니다.

```

OPENAI_API_KEY=sk-your-openai-api-key-here

```

Step 3: 메인 코드 작성 (`opic_bot.py`)

AI의 페르소나(Eva)를 설정하고 대화 루프를 구현합니다.

```

import os

from dotenv import load_dotenv

from langchain_openai import ChatOpenAI

from langchain_core.messages import HumanMessage, SystemMessage, AIMessage

# 환경 설정 로드

load_dotenv()

llm = ChatOpenAI(model="gpt-4o", temperature=0.7)

def main():

    print("🎙️ OPIc AI 면접관 Eva 시작...")

    topic = input("주제를 입력하세요 (예: 자기소개, 음악): ")

    # 페르소나 설정

    system_prompt = f"""

    당신은 OPIc 면접관 'Eva'입니다. 주제는 '{topic}'입니다.

    1. 질문을 하나 던지세요.

    2. 사용자 답변 후, 칭찬과 'Better Expression'(교정)을 제공하세요.

    3. 꼬리 질문을 이어가세요.

    """

    messages = [SystemMessage(content=system_prompt)]

    # 초기 질문

    res = llm.invoke(messages)

    print(f"Eva: {res.content}")

    messages.append(AIMessage(content=res.content))

    # 대화 루프

    while True:

        user_input = input("You ('q' 종료): ")

        if user_input in ['q', 'exit']: break

        

        messages.append(HumanMessage(content=user_input))

        res = llm.invoke(messages)

        print(f"Eva: {res.content}")

        messages.append(AIMessage(content=res.content))

if __name__ == "__main__":

    main()

```

4. 실행 및 확장
• 실행: `python opic_bot.py`

• 확장 기능:

  • STT/TTS: `Whisper API`를 추가하여 음성 대화 구현.

  • RAG: 모범 답안 데이터를 연동하여 학습 자료 제공.
