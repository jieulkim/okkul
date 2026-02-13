Phase 1: "분석 엔진" 만들기 (가장 중요)
이 프로젝트의 심장입니다. UI 없이 Python 스크립트 상에서 오디오 파일을 넣으면 분석 결과가 나오는 것까지 만드는 단계입니다.

환경 설정:

Django 프로젝트 생성 (config, apps/analysis 앱 생성).

필요 라이브러리 설치: openai-whisper, pyenchant (영어 사전), google-generativeai (Gemini).

STT 모듈 구현 (stt_service.py):

Whisper API를 호출하여 text뿐만 아니라 words(단어별 타임스탬프, 확률) 데이터를 받아오는 함수 작성.

단어 판별기 구현 (filter_service.py):

[핵심] 아까 짠 로직(사전 체크 → 신뢰도 체크 → 태깅)을 코드로 구현.

결과물: I want to be a [UNCERTAIN:prgr]... 형태의 가공된 텍스트 리턴.

Phase 2: "AI 튜터" 두뇌 이식 (LLM Integration)
분석된 텍스트를 바탕으로 Gemini가 선생님처럼 피드백을 주도록 만듭니다.

프롬프트 엔지니어링 (llm_service.py):

시스템 프롬프트 작성: "너는 OPIc 채점관이야. [UNCERTAIN:xxx] 태그가 붙은 단어는 문맥을 추론해서 복원해줘."

Output 형식을 JSON으로 강제하여 프론트엔드에서 처리하기 쉽게 만듭니다.

예시 데이터: {"original": "prgr", "fixed": "programmer", "reason": "문맥상 직업이 와야 함"}

단위 테스트:

일부러 웅얼거린 녹음 파일을 넣고 AI가 "Programmer"로 잘 고쳐주는지 테스트.

Phase 3: 데이터 저장 및 API (Backend)
웹 서비스의 뼈대를 만듭니다.

DB 모델링 (models.py):

Exam: 시험 회차 정보.

Question: 오픽 문제 (자기소개, 롤플레이 등).

UserAnswer: 사용자의 녹음 파일(FileField), 원본 스크립트(TextField), AI 분석 JSON(JSONField).

REST API 구현 (DRF):

POST /api/exam/submit/: 오디오 파일 업로드 및 분석 요청 처리.

GET /api/exam/result/{id}/: 분석 결과(스크립트 + 점수 + 피드백) 반환.

Phase 4: 사용자가 보는 화면 (Frontend)
React 또는 Vue.js를 사용하여 사용자가 녹음하고 결과를 보는 화면을 만듭니다.

녹음 인터페이스:

브라우저 마이크 권한 획득 (MediaRecorder API).

녹음 파형 시각화 (선택 사항).

결과 리포트 페이지 (Highlighting):

시각적 피드백 구현:

정상 단어: 검은색.

[UNCERTAIN] 태그 단어: 빨간색 밑줄 + 클릭 시 AI 추천 단어("Programmer") 툴팁 표시.

AI가 교정한 문장: 파란색 텍스트로 비교.

Phase 5: 디테일 잡기 (Refinement)
실패 케이스 방어: 잡음이 너무 심해서 아예 인식이 안 된 경우 "다시 녹음해주세요" 처리.

응답 속도 개선: STT와 LLM 분석 시간이 5초 이상 걸릴 수 있으므로 로딩 애니메이션 추가.