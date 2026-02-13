# 🎙️ AI-Based OPIc Tutor (Agent Version)

단순한 스크립트 암기를 넘어, 사용자와 **상호작용하며 답변을 코칭하는 에이전트(Agent) 기반 OPIc 튜터** 프로젝트입니다.

## 🎯 Project Goal
기존의 OPIc 연습 서비스는 단순히 내 말을 받아적고(STT) 문법 오류만 잡아주는 수동적인 형태였습니다.
이 프로젝트는 **LangChain**과 **LLM**을 활용해, 마치 실제 과외 선생님처럼 **내 의도를 파악하고, 막힌 부분은 힌트를 주며, 실력에 맞춰 난이도를 조절하는 능동적인 튜터**를 구현합니다.

---

## 💡 Core Tech: AI Agent & ReAct
이 서비스의 핵심은 단순히 질문에 대답하는 챗봇이 아니라, 상황을 판단하고 도구를 사용하는 **AI 에이전트**라는 점입니다.

**작동 메커니즘 (ReAct: Reasoning + Acting)**
사용자가 답변을 얼버무릴 때(예: "I want to be a... prgr..."), 에이전트는 다음과 같이 사고합니다.

1.  **관찰:** 'prgr'라는 불완전한 단어 감지. 신뢰도(Confidence) 낮음.
2.  **판단:** 바로 수정하지 않고 사전(Dictionary) 도구로 확인. 검색 결과 없음.
3.  **행동:** 문맥상 'Programmer'로 추정되나, 확실하지 않으므로 사용자에게 되물어보기로 결정.
4.  **발화:** "혹시 Programmer라고 하려던 거야? 다시 한번 문장으로 만들어볼까?"

---

## 🚀 Key Features

### 1. Exam Mode (실전 모의고사)
* 실제 시험 환경(Eva)과 동일한 UI/UX.
* 답변 녹음 및 STT 실시간 변환.
* **Focus:** 끊김 없는 시험 경험 제공 (에이전트 개입 최소화).

### 2. Coaching Mode (AI 튜터링)
시험 종료 후, 에이전트와 함께 하는 1:1 복습 모드입니다.

* **Intent Recovery (의도 파악):** 발음이 뭉개지거나 끊긴 단어를 문맥으로 유추하여 복원 제안.
* **Scaffolding (답변 유도):** 침묵이 길어지면 한국어 키워드 입력을 유도하고, 이를 영어 문장으로 확장해줌.
* **Active Adaptation (난이도 조절):** 답변 수준이 높으면 돌발 질문(꼬리 질문)을 던져 고득점(AL) 대비 훈련 제공.

---

## 🛠 Tech Stack

* **Backend:** Python 3.11+, Django 5.0
* **AI/LLM:** Google Gemini Pro, LangChain (LangGraph)
* **Speech:** OpenAI Whisper (API or Local model)
* **DB:** PostgreSQL
* **Infra:** Docker, AWS (Optional)

---

## 📂 System Architecture
(간략화된 데이터 흐름)

`User Audio` -> `Whisper STT (Timestamp & LogProb)` -> **`Context Filter (사전/문맥 필터링)`** -> `Agent Decision (LangGraph)` -> `Feedback Output`

---

## 📅 Roadmap

**Phase 1: Analysis Engine (MVP)**
- [ ] Django 프로젝트셋업 & Whisper 연동
- [ ] 핵심 필터 로직 구현 (사전 체크 -> 신뢰도 확인 -> 문맥 추론)

**Phase 2: Agent Logic**
- [ ] LangChain 에이전트 설계 및 도구(Tools) 구현
- [ ] 튜터 페르소나(Persona) 프롬프트 최적화

**Phase 3: Web Interface**
- [ ] React 기반 녹음 및 리포트 UI 구현
- [ ] 에이전트와의 채팅 인터페이스 연동