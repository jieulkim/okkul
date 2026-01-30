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

/** 질문 유형 */
export interface QuestionTypeRequest {
  /**
   * 유형 코드
   * @example "RP1"
   */
  typeCode?: string;
  /**
   * 설명
   * @example "롤플레잉"
   */
  description?: string;
}

export interface QuestionType {
  /** @format int64 */
  id?: number;
  typeCode?: string;
  description?: string;
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

/** 질문 상세 정보 */
export interface QuestionResponse {
  /**
   * 문항 매핑 ID (답변 제출 시 식별자로 사용)
   * @format int64
   * @example 1201
   */
  answerId?: number;
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

/** 모의고사 시작 요청 데이터 */
export interface ExamStartRequest {
  /**
   * 선택한 시험 세트의 고유 ID
   * @format int64
   * @example 101
   */
  examSetId: number;
  /**
   * DB에 저장된 사용자의 설문조사 완료 데이터 ID
   * @format int64
   * @example 5001
   */
  surveyId: number;
}

/** 모의고사 시작 응답 데이터 */
export interface ExamStartResponse {
  /**
   * 생성된 모의고사 ID
   * @format int64
   * @example 1
   */
  examId?: number;
  /**
   * 전체 문항 수
   * @format int32
   * @example 15
   */
  totalQuestions?: number;
  /** 초기 문항 리스트 (1~7번) */
  questions?: QuestionResponse[];
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

export interface PagedModelQuestionType {
  content?: QuestionType[];
  page?: PageMetadata;
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

/** AI 분석 진행 상태 응답 */
export interface ExamStatusResponse {
  /**
   * 현재 분석 완료된 문항 수
   * @format int32
   * @example 12
   */
  completedQuestions?: number;
  /**
   * 전체 문항 수
   * @format int32
   * @example 15
   */
  totalQuestions?: number;
  /**
   * 예상 남은 시간 (초)
   * @format int32
   * @example 10
   */
  estimatedRemainingSeconds?: number;
  allAnalyzed?: boolean;
}

/** 오각형 차트 데이터 (각 0~100점 기준) */
export interface CategoryScoresResponse {
  /**
   * 문법 점수
   * @format int32
   * @example 80
   */
  grammar?: number;
  /**
   * 어휘 점수
   * @format int32
   * @example 75
   */
  vocabulary?: number;
  /**
   * 논리 점수
   * @format int32
   * @example 90
   */
  logic?: number;
  /**
   * 유창성 점수
   * @format int32
   * @example 85
   */
  fluency?: number;
  /**
   * 주제 적합성 점수
   * @format int32
   * @example 70
   */
  relevance?: number;
}

/** 모의고사 최종 결과 응답 데이터 */
export interface ExamResultResponse {
  /**
   * 모의고사 ID
   * @format int64
   * @example 550
   */
  examId?: number;
  /**
   * 시험 제목
   * @example "2024-03-21 실전 대비 모의고사"
   */
  title?: string;
  /**
   * 시험 응시 일시
   * @format date-time
   */
  createdAt?: string;
  /** 시험 총체적 분석 리포트 */
  summary?: ExamSummaryResponse;
  /** 문항별 상세 분석 결과 리스트 */
  questionResults?: QuestionResultDetailResponse[];
}

/** 시험 총체적 분석 리포트 */
export interface ExamSummaryResponse {
  /**
   * 전체 점수 (0-100)
   * @format double
   * @example 85.5
   */
  totalScore?: number;
  /**
   * 예측 등급
   * @example "IH"
   */
  grade?: string;
  /** 오각형 차트 데이터 (각 0~100점 기준) */
  categoryScores?: CategoryScoresResponse;
  /** 전체 총평 내용 */
  comment?: string;
  /** 전체적인 강점 유형 */
  strengths?: string;
  /** 전체적인 약점 유형 */
  weakness?: string;
}

/** 영역별 상세 피드백 정보 */
export interface FeedbackSetResponse {
  /**
   * 영역 점수 (1-5 또는 0-100)
   * @format int32
   * @example 4
   */
  score?: number;
  /**
   * 영역별 맞춤 피드백 내용
   * @example "과거 시제 활용이 매우 정확했습니다."
   */
  feedback?: string;
}

/** 문항별 상세 분석 결과 */
export interface QuestionResultDetailResponse {
  /**
   * 출제 순서
   * @format int32
   * @example 3
   */
  questionOrder?: number;
  /** 질문 텍스트 */
  questionText?: string;
  /** 사용자 답변 음성 URL */
  audioUrl?: string;
  /**
   * 답변 길이(초)
   * @format int32
   * @example 45
   */
  duration?: number;
  /** 사용자 답변 STT 결과 */
  sttScript?: string;
  /** AI가 제안하는 교정 스크립트 (오꿀쌤의 교정 스크립트) */
  enhancedScript?: string;
  /** 영역별 상세 피드백 정보 */
  grammar?: FeedbackSetResponse;
  /** 영역별 상세 피드백 정보 */
  vocabulary?: FeedbackSetResponse;
  /** 영역별 상세 피드백 정보 */
  logic?: FeedbackSetResponse;
  /** 영역별 상세 피드백 정보 */
  fluency?: FeedbackSetResponse;
  /** 영역별 상세 피드백 정보 */
  relevance?: FeedbackSetResponse;
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

export type GetQuestionTypesData = PagedModelQuestionType;

export type CreateQuestionTypeData = QuestionType;

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

export type GetRemainingQuestionsData = QuestionResponse[];

export type StartExamData = ExamStartResponse;

export interface SubmitAnswerPayload {
  /** @format binary */
  file: File;
}

export type SubmitAnswerData = any;

export type CompleteExamData = any;

export type ReissueData = AccessTokenResponse;

export type GetQuestionTypeData = QuestionType;

export type DeleteQuestionTypeData = any;

export type UpdateQuestionTypeData = QuestionType;

export type GetQuestionSetData = QuestionSetResponse;

export type DeleteQuestionSetData = any;

export type UpdateQuestionSetData = QuestionSetResponse;

export type DeleteQuestionData = any;

export type UpdateQuestionData = QuestionDetailResponse;

export type GetMyInfoData = UserResponse;

export type GetSurveyTopicsData = CategoryTopicResponse;

export type GetSurveyByIdData = SurveyResponse;

export type GetSurveyTopicByIdData = Type선택된토픽목록;

export type GetPracticeProblemData = PracticeQuestionResponse;

export type GetPracticeFeedbackData = PracticeAIFeedbackResult;

export type HealthCheckData = string;

export type HealthCheckError = string;

export type GetExamStatusData = ExamStatusResponse;

export type GetExamResultData = ExamResultResponse;
