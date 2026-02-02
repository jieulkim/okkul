/* eslint-disable */
/* tslint:disable */
// @ts-nocheck
/*
 * ---------------------------------------------------------------
 * ## THIS FILE WAS GENERATED VIA SWAGGER-TYPESCRIPT-API        ##
 * ##                                                           ##
 * ## AUTHOR: acacode                                           ##
 * ## SOURCE: https://github.com/acacode/swagger-typescript-api ##
 * ---------------------------------------------------------------
 */

/** 회원 목표 등급 업데이트 요청 DTO */
export interface UpdateTargetLevelRequest {
  /**
   * 변경할 목표 등급
   * @example "INTERMEDIATE_HIGH"
   */
  targetLevel?:
    | "ADVANCED_LOW"
    | "INTERMEDIATE_HIGH"
    | "INTERMEDIATE_MID_3"
    | "INTERMEDIATE_MID_2"
    | "INTERMEDIATE_MID_1"
    | "INTERMEDIATE_LOW"
    | "NOVICE_HIGH"
    | "NOVICE_MID"
    | "NOVICE_LOW";
}

/** 회원 정보 응답 DTO */
export interface UserResponse {
  /**
   * 회원 고유 아이디
   * @format int64
   * @example 1
   */
  id?: number;
  /**
   * 연동 제공자
   * @example "GOOGLE"
   */
  provider?: string;
  /**
   * 회원 이름
   * @example "홍길동"
   */
  name?: string;
  /**
   * 프로필 이미지 URL
   * @example "https://example.com/avatar.png"
   */
  profileImageUrl?: string;
  /**
   * 이메일 주소
   * @example "user@example.com"
   */
  email?: string;
  /**
   * 현재 레벨 (Enum 이름)
   * @example "BEGINNER"
   */
  currentLevel?: string;
  /**
   * 목표 레벨 (Enum 이름)
   * @example "ADVANCED"
   */
  targetLevel?: string;
  /**
   * 권한 목록
   * @uniqueItems true
   * @example ["ROLE_USER","ROLE_ADMIN"]
   */
  roles?: ("USER" | "ADMIN")[];
}

/** 회원 닉네임 업데이트 요청 DTO */
export interface UpdateNicknameRequest {
  /**
   * 변경할 닉네임
   * @example "new_nickname"
   */
  nickname?: string;
}

/** 설문조사 생성 요청 DTO */
export interface SurveyCreateRequest {
  /**
   * 직군(현재 귀하는 어느 분야에 종사하고 계십니까?)
   * @format int32
   * @example 1
   */
  occupationAnswerId?: number;
  /**
   * 직업 유무(귀하는 직업이 있으십니까?)
   * @example true
   */
  hasJob?: boolean;
  /**
   * 근무 기간(귀하의 근무 기간은 얼마나 되십니까?)
   * @format int32
   * @example 1
   */
  workPeriodAnswerId?: number;
  /**
   * 교육 분야(현재 귀하는 어디에서 학생을 가르치십니까?)
   * @format int32
   */
  teachAnswerId?: number;
  /**
   * 관리직 여부(귀하는 부하직원을 관리하는 관리직을 맡고 있습니까?)
   * @example false
   */
  manager?: boolean;
  /**
   * 학생 여부(당신은 학생입니까?)
   * @example false
   */
  student?: boolean;
  /**
   * 수강 종류(최근 어떤 강의를 수강했습니까?)
   * @format int32
   */
  classTypeAnswerId?: number;
  /**
   * 거주지(현재 당신은 어디에 살고 계십니까?)
   * @format int32
   * @example 3
   */
  residenceAnswerId?: number;
  /**
   * 여가 활동 토픽 ID 목록
   * @example [101,102]
   */
  leisure?: number[];
  /**
   * 취미나 관심사 토픽 ID 목록
   * @example [201,202,203]
   */
  hobby?: number[];
  /**
   * 운동 토픽 ID 목록
   * @example [301,302]
   */
  exercise?: number[];
  /**
   * 휴가나 출장 토픽 ID 목록
   * @example [401,402,403,404,405]
   */
  holiday?: number[];
  /**
   * 자가진단 레벨 (1~6)
   * @format int32
   * @example 4
   */
  level?: number;
}

/** 설문조사 생성 응답 DTO */
export interface SurveyCreateResponse {
  /**
   * 생성된 설문조사 ID
   * @format int64
   * @example 1
   */
  surveyId?: number;
}

/** 문항 세트 생성/수정 요청 */
export interface QuestionSetRequest {
  /**
   * 난이도 (1~6)
   * @format int32
   * @example 3
   */
  level?: number;
  /**
   * 연관 토픽 ID
   * @format int64
   * @example 101
   */
  topicId?: number;
  /**
   * 문제 유형 ID
   * @format int64
   * @example 1
   */
  typeId?: number;
}

/** 개별 문항 상세 정보 */
export interface QuestionDetailResponse {
  /**
   * 문항 ID
   * @format int64
   * @example 1
   */
  id?: number;
  /**
   * 질문 텍스트
   * @example "Tell me about your house."
   */
  questionText?: string;
  /**
   * 질문 음성 파일 URL
   * @example "https://cdn.okkul.site/audio/q1.mp3"
   */
  audioUrl?: string;
  /**
   * 세트 내 출력 순서
   * @format int32
   * @example 1
   */
  order?: number;
}

/** 문항 세트 응답 정보 */
export interface QuestionSetResponse {
  /**
   * 세트 ID
   * @format int64
   * @example 10
   */
  setId?: number;
  /**
   * 난이도 (1~6)
   * @format int32
   * @example 4
   */
  level?: number;
  /**
   * 세트에 포함된 문항 수
   * @format int32
   * @example 3
   */
  questionCnt?: number;
  /**
   * 토픽 이름
   * @example "영화 보기"
   */
  topicName?: string;
  /**
   * 문제 유형 코드
   * @example "COMBO3"
   */
  typeCode?: string;
  /** 포함된 상세 문항 리스트 */
  questions?: QuestionDetailResponse[];
  /**
   * 생성 일시
   * @format date-time
   */
  createdAt?: string;
}

/** 문제 내용 */
export interface QuestionRequest {
  /**
   * 질문 텍스트
   * @example "Tell me about your favorite park."
   */
  questionText: string;
  /**
   * 질문 음성 파일 URL
   * @example "https://cdn.okkul.site/audio/q1.mp3"
   */
  audioUrl: string;
  /**
   * 세트 내 출력 순서
   * @format int32
   * @example 1
   */
  order: number;
}

/** 유형 연습 생성 API의 응답 결과 */
export interface PracticeCreateResponse {
  /**
   * 생성된 유형연습의 ID
   * @format int64
   * @example 1
   */
  practiceId?: number;
  /**
   * 유형 연습 시작 날짜
   * @format date-time
   * @example "2024-01-22T12:30:00Z"
   */
  createdAt?: string;
}

/** 스크립트 정보 (JSON) */
export interface PracticeFeedbackRequest {
  /**
   * 문제 ID
   * @format int64
   * @example 7
   */
  questionId?: number;
  /**
   * 사용자 한국어 스크립트
   * @example "제가 가장 자주 가는 단골 바는 집 근처 골목에 숨겨진 '아지트'라는 작은 펍이에요. 이곳은 전체적으로 어두운 조명에 빈티지한 벽돌로 꾸며져 있어서, 들어서자마자 굉장히 아늑하고 편안한 느낌을 줘요. 특히 벽면 가득한 LP판에서 흘러나오는 재즈 음악과 은은한 위스키 향이 어우러져서, 하루의 피로를 풀며 친구들과 진솔한 대화를 나누기에 정말 최고의 장소입니다."
   */
  koreanScript?: string;
  /**
   * 사용자 영어 스크립트
   * @example "My favorite place to grab a drink is a cozy, neighborhood bar called \"The Hideout\" located just a few blocks from my house. The interior is dimly lit with vintage brick walls and comfortable leather booths, creating a very warm and inviting atmosphere. It’s the perfect spot to unwind because the music is always at a perfect volume, allowing for great conversations with friends. Would you like me to adjust the vocabulary level or add more specific details to make it sound more like your personal style?"
   */
  englishScript: string;
}

/** 생성된 유형별 연습 답변 ID 응답 */
export interface PracticeAnswerIdResponse {
  /**
   * 생성된 유형별 연습 답변 ID
   * @format int64
   * @example 1
   */
  practiceAnswerId?: number;
}

/** 시험 생성 요청 객체 */
export interface ExamCreateRequest {
  /**
   * DB에 저장된 사용자의 설문조사 완료 데이터 ID
   * @format int64
   * @min 1
   * @max 6
   * @example 5001
   */
  surveyId: number;
}

/** 모의고사용 문제 정보 */
export interface ExamDetailResponse {
  /**
   * 시험 ID
   * @format int64
   */
  id?: number;
  /**
   * 처음 선택한 난이도
   * @format int32
   * @example 5
   */
  initialDifficulty?: number;
  /**
   * 변경한 난이도
   * @format int32
   * @example 6
   */
  adjustedDifficulty?: number;
  /**
   * 생성시간
   * @format date-time
   */
  createdAt?: string;
  /** 현재 할당된 문제들 */
  questions?: QuestionResponse[];
}

/** 사용자에게 반환되는 퀘스트(문제) 정보 */
export interface QuestionResponse {
  /**
   * 문제 ID
   * @format int64
   * @example 1
   */
  questionId?: number;
  /**
   * 문제 텍스트
   * @example "다음 중 올바른 것은 무엇인가요?"
   */
  questionText?: string;
  /**
   * 문제에 대한 오디오 파일 URL
   * @example "https://example.com/audio/1.mp3"
   */
  audioUrl?: string;
  /**
   * 모의고사 문제 순서 (Answer 저장 시 복합키로 활용됨)
   * @format int32
   * @example 1
   */
  order?: number;
}

/** 답변 저장요청 객체 */
export interface ExamQuestionAnswerRequest {
  /** @format binary */
  file?: File;
  /** STT 텍스트 */
  sttText?: string;
  /**
   * 답변 길이(초)
   * @format int32
   * @example 45
   */
  duration?: number;
}

/** 토큰 재발급 요청 */
export interface TokenReissueRequest {
  /**
   * 리프레시 토큰
   * @example "eyJhbG..."
   */
  refreshToken: string;
}

/** 액세스 토큰 응답 */
export interface AccessTokenResponse {
  /**
   * 새로 발급된 액세스 토큰
   * @example "eyJhbG..."
   */
  accessToken?: string;
}

/** 설문조사용 카테고리별 토픽 목록 응답 DTO */
export interface CategoryTopicResponse {
  /** 카테고리 목록 */
  categories?: Type카테고리정보[];
}

/** 토픽 정보 */
export interface TopicInfo {
  /**
   * 토픽 ID
   * @format int64
   * @example 101
   */
  id?: number;
  /**
   * 토픽 이름
   * @example "여행가기"
   */
  name?: string;
}

/** 카테고리 목록 */
export interface Type카테고리정보 {
  /**
   * @format int64
   * @example 1
   */
  "카테고리 ID"?: number;
  /** @example "학생여부" */
  "카테고리 이름"?: string;
  "카테고리에 속한 토픽 목록"?: TopicInfo[];
}

/** 사용자의 설문조사 리스트 응답 DTO */
export interface SurveyListResponse {
  surveySummaryResponses?: SurveySummaryResponse[];
}

/** 사용자의 설문조사 목록 조회용 요약 정보 응답 DTO */
export interface SurveySummaryResponse {
  /**
   * 설문조사 ID
   * @format int64
   * @example 1
   */
  surveyId?: number;
  /**
   * 생성일 (UTC)
   * @format date-time
   * @example "2024-01-22T12:30:00Z"
   */
  createdAt?: string;
  /**
   * 직군
   * @example "COMPANY"
   */
  occupation?: string;
  /**
   * 자가진단 레벨
   * @format int32
   * @example 4
   */
  level?: number;
  /**
   * 직업 유무
   * @example true
   */
  hasJob?: boolean;
  /**
   * 학생 여부
   * @example false
   */
  student?: boolean;
  /**
   * 근무 기간
   * @example "MANY"
   */
  workPeriod?: string;
  /**
   * 관리직 여부
   * @example true
   */
  manager?: boolean;
  /**
   * 거주지 형태
   * @example "FAMILY"
   */
  residence?: string;
  /**
   * 소재 목록
   * @example [101,102,201]
   */
  topicList?: number[];
}

/**
 * 설문조사 생성 응답 DTO
 * @example [101,102,201]
 */
export interface SelectedTopic {
  /**
   * 토픽 ID
   * @format int64
   * @example 101
   */
  topicId?: number;
  /**
   * 토픽 이름
   * @example "여행가기"
   */
  topicName?: string;
}

/** 사용자 설문조사 결과 조회 응답 DTO */
export interface SurveyResponse {
  /**
   * 설문조사 ID
   * @format int64
   * @example 1
   */
  surveyId?: number;
  /**
   * 생성일 (UTC)
   * @format date-time
   * @example "2024-01-22T12:30:00Z"
   */
  createdAt?: string;
  /**
   * 직군
   * @example "COMPANY"
   */
  occupation?: string;
  /**
   * 자가진단 레벨
   * @format int32
   * @example 4
   */
  level?: number;
  /**
   * 직업 유무
   * @example true
   */
  hasJob?: boolean;
  /**
   * 학생 여부
   * @example false
   */
  student?: boolean;
  /**
   * 근무 기간
   * @example "MANY"
   */
  workPeriod?: string;
  /**
   * 관리직 여부
   * @example true
   */
  manager?: boolean;
  /**
   * 거주지 형태
   * @example "FAMILY"
   */
  residence?: string;
  /**
   * 교육 장소
   * @example "SCHOOL"
   */
  teachAt?: string;
  /**
   * 강의 수강 유형
   * @example "DEGREE"
   */
  classType?: string;
  /**
   * 선택된 토픽 목록
   * @example [101,102,201]
   */
  selectedTopics?: SelectedTopic[];
}

export interface Type선택된토픽목록 {
  selectedTopics?: SelectedTopic[];
}

export interface PageMetadata {
  /** @format int64 */
  size?: number;
  /** @format int64 */
  number?: number;
  /** @format int64 */
  totalElements?: number;
  /** @format int64 */
  totalPages?: number;
}

export interface PagedModelQuestionSetResponse {
  content?: QuestionSetResponse[];
  page?: PageMetadata;
}

/** 유형별 연습 모드 질문 상세 정보 */
export interface PracticeQuestionInfo {
  /**
   * 문제 ID
   * @format int64
   * @example 1201
   */
  questionId?: number;
  /**
   * 질문 순서
   * @format int32
   * @example 1
   */
  questionOrder?: number;
  /**
   * 질문 텍스트 (화면 표시용)
   * @example "Let's start the interview. Tell me about yourself."
   */
  questionText?: string;
  /**
   * 질문 음성 파일 경로 (S3 등 스토리지 URL)
   * @example "https://cdn.okkul.site/audio/q1.mp3"
   */
  audioUrl?: string;
}

export interface PracticeQuestionResponse {
  /**
   * 할당된 문제 세트의 ID
   * @format int64
   * @example 10
   */
  setId?: number;
  /** 연습에 포함된 질문 목록 */
  questions?: PracticeQuestionInfo[];
}

/** AI 피드백 결과 상세 */
export interface PracticeAIFeedbackResult {
  /** 피드백 응답 상태 반환 */
  feedbackStatus?: "PENDING" | "PROCESSING" | "COMPLETED" | "FAILED";
  /** 문법 및 어휘 교정 제안 목록 */
  scriptCorrections?: SentenceCorrection[];
  /**
   * 주제 적합성 피드백
   * @example "전반적으로 훌륭한 답변입니다! 몇 가지 사소한 문법 오류를 수정하면 더욱 자연스러워질 것입니다."
   */
  relevanceFeedback?: string;
  /**
   * 논리성 피드백
   * @example "전반적으로 훌륭한 답변입니다! 몇 가지 사소한 문법 오류를 수정하면 더욱 자연스러워질 것입니다."
   */
  logicFeedback?: string;
  /**
   * 유창성 피드백
   * @example "전반적으로 훌륭한 답변입니다! 몇 가지 사소한 문법 오류를 수정하면 더욱 자연스러워질 것입니다."
   */
  fluencyFeedback?: string;
  /**
   * AI 개선 종합 답변
   * @example "That's my ~~"
   */
  aiImprovedAnswer?: string;
}

/** 스크립트 교정 항목 */
export interface SentenceCorrection {
  /**
   * 타겟 문장
   * @example "I am usually go to the school in morning."
   */
  targetSentence?: string;
  /**
   * 수정이 필요한 원본 문구
   * @example "I am go to the school."
   */
  originalSegment?: string;
  /**
   * 교정된 문구
   * @example "I go to school."
   */
  correctedSegment?: string;
  /**
   * 교정 이유 또는 설명
   * @example "불필요한 'am'이 사용되었습니다."
   */
  comment?: string;
  /**
   * 문장 순서
   * @format int32
   * @example 1
   */
  sentenceOrder?: number;
}

export interface PagedModelPracticeHistorySummary {
  content?: PracticeHistorySummary[];
  page?: PageMetadata;
}

/** 유형별 연습 히스토리 목록 조회용 요약 정보 */
export interface PracticeHistorySummary {
  /**
   * 연습 ID (PK)
   * @format int64
   * @example 101
   */
  practiceId?: number;
  /**
   * 연습 시작 일시
   * @format date-time
   */
  startedAt?: string;
  /**
   * 선택한 토픽 ID
   * @format int64
   * @example 50
   */
  topicId?: number;
  /** 선택한 토픽 이름 */
  topic?: string;
  /**
   * 문제 유형 ID
   * @format int64
   * @example 3
   */
  typeId?: number;
  /** 문제 유형 */
  typeName?: string;
}

/** AI 피드백 상세 정보 */
export interface PracticeAiFeedback {
  /**
   * 피드백 상태
   * @example "COMPLETED"
   */
  status?: "PENDING" | "PROCESSING" | "COMPLETED" | "FAILED";
  /**
   * AI가 제안한 더 좋은 답변
   * @example "I enjoy traveling."
   */
  improvedAnswer?: string;
  /**
   * 유창성 피드백
   * @example "전반적으로 자연스럽습니다."
   */
  fluencyFeedback?: string;
  /**
   * 논리성 피드백
   * @example "주장이 명확합니다."
   */
  logicFeedback?: string;
  /**
   * 적합성 피드백
   * @example "주제에 맞는 답변입니다."
   */
  relevanceFeedback?: string;
  /** 문장별 상세 피드백 리스트 */
  sentenceDetails?: PracticeSentenceFeedbackResponse[];
}

/** 사용자 답변 정보 (스크립트 및 녹음) */
export interface PracticeAnswer {
  /**
   * 한글 스크립트
   * @example "저는 여행을 좋아합니다."
   */
  koreanScript?: string;
  /**
   * 영어 스크립트 (STT)
   * @example "I like travel."
   */
  englishScript?: string;
  /**
   * 녹음 파일 URL
   * @example "https://s3.aws.../record.mp3"
   */
  recordUrl?: string;
}

/** 연습 문항별 상세 사이클 정보 (질문 -> 답변 -> 피드백) */
export interface PracticeCycleDetail {
  /**
   * 시도 순서 (1차 시도, 2차 시도...)
   * @format int32
   * @example 1
   */
  attemptOrder?: number;
  /** 사용자 답변 정보 (스크립트 및 녹음) */
  userAnswer?: PracticeAnswer;
  /** AI 피드백 상세 정보 */
  feedback?: PracticeAiFeedback;
}

/** 유형별 연습 히스토리 상세 조회 응답 (메타데이터 + 전체 문답 사이클) */
export interface PracticeHistoryDetailResponse {
  /**
   * 연습 세션 ID
   * @format int64
   * @example 101
   */
  practiceId?: number;
  /**
   * 토픽 ID
   * @format int64
   * @example 50
   */
  topicId?: number;
  /**
   * 선택한 토픽 이름
   * @example "영화보기"
   */
  topicTitle?: string;
  /**
   * 문제 유형 ID
   * @format int64
   * @example 3
   */
  typeId?: number;
  /**
   * 문제 유형 코드
   * @example "COMBO3"
   */
  typeName?: string;
  /**
   * 연습 시작 일시
   * @format date-time
   * @example "2024-05-21T05:30:00Z"
   */
  startedAt?: string;
  /** 질문별 상세 내역 리스트 */
  questions?: PracticeQuestionDetail[];
}

/** 연습 내의 개별 질문 정보와 답변 시도 리스트 */
export interface PracticeQuestionDetail {
  /**
   * 문항 ID
   * @format int64
   * @example 7
   */
  questionId?: number;
  /**
   * 질문 순서 (Q1, Q2...)
   * @format int32
   * @example 1
   */
  questionOrder?: number;
  /**
   * 질문 내용
   * @example "당신이 가장 좋아하는 공원에 대해 묘사해주세요."
   */
  questionText?: string;
  /** 해당 질문에 대한 답변 시도(Cycles) 리스트 */
  attempts?: PracticeCycleDetail[];
}

/** 문장별 상세 피드백 리스트 */
export interface PracticeSentenceFeedbackResponse {
  /**
   * 타겟 문장 (원본 전체)
   * @example "I enjoy to travel."
   */
  targetSentence?: string;
  /**
   * 대상 텍스트
   * @example "to travel"
   */
  targetSegment?: string;
  /**
   * 개선 텍스트
   * @example "traveling"
   */
  correctedSegment?: string;
  /**
   * 피드백 텍스트
   * @example "동명사를 사용하는 것이 더 자연스럽습니다."
   */
  comment?: string;
}

export interface ExamHistorySummary {
  /** @format int64 */
  examId?: number;
  /** @format date-time */
  createdAt?: string;
  /** @format date-time */
  endAt?: string;
  /** @format int32 */
  initialDifficulty?: number;
  /** @format int32 */
  adjustedDifficulty?: number;
  grade?: string;
}

export interface PagedModelExamHistorySummary {
  content?: ExamHistorySummary[];
  page?: PageMetadata;
}

/** 모의고사 히스토리 상세 응답 데이터 */
export interface ExamHistoryDetailResponse {
  /** @format int64 */
  examId?: number;
  /** @format date-time */
  createdAt?: string;
  /** @format date-time */
  endAt?: string;
  /** 모의고사 총체 리포트 */
  examReport?: ExamReport;
}

/** 모의고사 총체 리포트 */
export interface ExamReport {
  totalScore?: number;
  grade?: string;
  avgGrammar?: number;
  avgVocab?: number;
  avgLogic?: number;
  avgFluency?: number;
  avgRelevance?: number;
  comment?: string;
  strengthTypes?: string[];
  weaknessTypes?: string[];
  /** @format date-time */
  createdAt?: string;
}

/** 항목별 피드백 */
export interface CategoryFeedback {
  /** 주제 적합성 피드백 */
  relevanceFeedback?: string;
  /** 논리성 피드백 */
  logicFeedback?: string;
  /** 유창성 피드백 */
  fluencyFeedback?: string;
}

/** 모의고사 문항(답변) 상세 피드백 응답 */
export interface ExamAnswerResponse {
  /**
   * 모의고사 ID
   * @format int64
   * @example 12
   */
  examId?: number;
  /**
   * 답변 ID
   * @format int64
   * @example 301
   */
  answerId?: number;
  /**
   * 시험 내 문항 순서
   * @format int32
   * @example 3
   */
  questionOrder?: number;
  /** STT로 변환된 원본 답변 스크립트 */
  sttScript?: string;
  /** AI 개선 답변 텍스트 */
  improvedAnswer?: string;
  /** 항목별 피드백 */
  categoryFeedback?: CategoryFeedback;
  sentenceFeedbacks?: SentenceFeedback[];
  /**
   * 답변 생성 일시
   * @format date-time
   */
  createdAt?: string;
}

/** 문장 단위 교정 피드백 */
export interface SentenceFeedback {
  /**
   * 문장 피드백 ID
   * @format int64
   * @example 9001
   */
  feedbackId?: number;
  /** 원본 문장 */
  targetSentence?: string;
  /** 문제 구간 */
  targetSegment?: string;
  /** 교정된 표현 */
  correctedSegment?: string;
  /** 피드백 텍스트 */
  comment?: string;
  /**
   * 문장 순서
   * @format int32
   * @example 1
   */
  sentenceOrder?: number;
}

export type UpdateTargetLevelData = UserResponse;

export interface UpdateProfileImagePayload {
  /**
   * 업로드할 이미지 파일 (jpg, png)
   * @format binary
   */
  file: File;
}

export type UpdateProfileImageData = UserResponse;

export type UpdateNicknameData = UserResponse;

export type GetSurveyListData = SurveyListResponse;

export type CreateSurveyData = SurveyCreateResponse;

export type GetQuestionSetsData = PagedModelQuestionSetResponse;

export type CreateQuestionSetData = QuestionSetResponse;

export type AddQuestionData = QuestionDetailResponse;

export type StartPracticeData = PracticeCreateResponse;

export interface SavePracticeSessionPayload {
  /** 스크립트 정보 (JSON) */
  request: PracticeFeedbackRequest;
  /**
   * 사용자 영어 녹음 파일
   * @format binary
   */
  audio: File;
}

export type SavePracticeSessionData = PracticeAnswerIdResponse;

export type StartExamData = ExamDetailResponse;

export type CompleteExamData = any;

export type SubmitAnswerData = any;

export type ReissueData = AccessTokenResponse;

export type GetQuestionSetData = QuestionSetResponse;

export type DeleteQuestionSetData = any;

export type UpdateQuestionSetData = QuestionSetResponse;

export type DeleteQuestionData = any;

export type UpdateQuestionData = QuestionDetailResponse;

export type UpdateAdjustedDifficultyData = ExamDetailResponse;

export type GetMyInfoData = UserResponse;

export type GetSurveyTopicsData = CategoryTopicResponse;

export type GetSurveyByIdData = SurveyResponse;

export type GetSurveyTopicByIdData = Type선택된토픽목록;

export type GetPracticeProblemData = PracticeQuestionResponse;

export type GetPracticeFeedbackData = PracticeAIFeedbackResult;

export type GetPracticeHistoriesData = PagedModelPracticeHistorySummary;

export type GetPracticeHistoryDetailData = PracticeHistoryDetailResponse;

export type GetExamHistoriesData = PagedModelExamHistorySummary;

export type GetExamHistoryDetailData = ExamHistoryDetailResponse;

export type GetExamAnswerDetailData = ExamAnswerResponse;

export type HealthCheckData = string;

export type HealthCheckError = string;

export type GetExamInfoData = ExamDetailResponse;
