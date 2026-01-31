<script setup>
import { ref, computed, inject } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useSurveyStore } from "@/stores/survey";

const router = useRouter();
const route = useRoute();
const isDarkMode = inject("isDarkMode", ref(false));

const currentStep = ref(1); // 1, 2, 3, 4
const totalSteps = 4;

// API 요청 데이터 구조 (SurveyCreateRequest 기준)
const surveyData = ref({
  occupationAnswerId: null, // Part 1: 직군 (1727, 1728, 1729, 1730)
  hasJob: null, // Part 1: 직업 유무 (boolean)
  workPeriodAnswerId: null, // Part 1: 근무 기간 (1731-1733, 1836-1838, 1747-1749)
  teachAnswerId: null, // Part 1: 교육 분야 (1741, 1742, 1743)
  manager: null, // Part 1: 관리직 여부 (boolean)
  student: null, // Part 2: 학생 여부 (boolean)
  classTypeAnswerId: null, // Part 2: 수강 종류 (1964-1966, 1967-1970)
  residenceAnswerId: null, // Part 3: 거주지 (1705-1709)
  leisure: [], // Part 4: 여가 활동 topic ID 목록
  hobby: [], // Part 4: 취미 topic ID 목록
  exercise: [], // Part 4: 운동 topic ID 목록
  holiday: [], // Part 4: 휴가 topic ID 목록
  level: null, // 자가진단 레벨 (1~6)
});

// 조건부 질문 표시 상태
const showSubQuestions = ref({
  teachLocation: false, // 교사 선택 시
  hasJob_company: false, // 사업/회사 선택 시
  hasJob_home: false, // 재택근무 선택 시
  hasJob_teacher: false, // 교사 선택 후
  workPeriod_company: false, // 직업 있음 선택 후
  workPeriod_home: false,
  workPeriod_teacher: false,
  manager_company: false, // 근무기간 2개월 이상 선택 후
  manager_home: false,
  manager_teacher: false,
  currentCourse: false, // 학생 예 선택 시
  pastCourse: false, // 학생 아니요 선택 시
});

// Part 1: 직군 옵션
const occupationOptions = [
  { id: 1, text: "사업/회사", code: "COMPANY" },
  { id: 2, text: "재택근무/재택사업", code: "HOME" },
  { id: 3, text: "교사/교육자", code: "EDUCATION" },
  { id: 4, text: "일 경험 없음", code: "NONE" },
];

// Part 1: 교육 분야
const teachOptions = [
  { id: 1, text: "대학 이상" },
  { id: 2, text: "초등/중/고등학교" },
  { id: 3, text: "평생교육" },
];

// Part 1: 근무 기간 - 사업/회사 (1~3)
const workPeriodOptionsCompany = [
  { id: 1, text: "첫직장- 2개월 미만" },
  { id: 2, text: "첫직장- 2개월 이상" },
  { id: 3, text: "첫직장 아님 - 경험 많음" },
];

// Part 1: 근무 기간 - 재택 (1~3)
const workPeriodOptionsHome = [
  { id: 1, text: "첫직장- 2개월 미만" },
  { id: 2, text: "첫직장- 2개월 이상" },
  { id: 3, text: "첫직장 아님 - 경험 많음" },
];

// Part 1: 근무 기간 - 교사 (1~3)
const workPeriodOptionsTeacher = [
  { id: 1, text: "2개월 미만 - 첫직장" },
  {
    id: 2,
    text: "2개월 미만 - 교직은 처음이지만 이전에 다른 직업을 가진 적이 있음",
  },
  { id: 3, text: "2개월 이상" },
];

// Part 2: 수강 종류 - 현재 학생
const currentCourseOptions = [
  { id: 1, text: "학위 과정 수업" },
  { id: 2, text: "전문 기술 향상을 위한 평생 학습" },
  { id: 3, text: "어학수업" },
];

// Part 2: 수강 종류 - 과거 학생
const pastCourseOptions = [
  { id: 1, text: "학위 과정 수업" },
  { id: 2, text: "전문 기술 향상을 위한 평생 학습" },
  { id: 3, text: "어학수업" },
  { id: 4, text: "수강 후 5년 이상 지남" },
];

// Part 3: 거주지
const residenceOptions = [
  { id: 1, text: "개인주택이나 아파트에 홀로 거주" },
  { id: 2, text: "친구나 룸메이트와 함께 주택이나 아파트에 거주" },
  {
    id: 3,
    text: "가족(배우자/자녀/기타 가족 일원)과 함께 주택이나 아파트에 거주",
  },
  { id: 4, text: "학교 기숙사" },
  { id: 5, text: "군대 막사" },
];

// Part 4: 여가 활동 (기본값: 하드코딩)
const leisureOptions = ref([
  { id: 101, text: "영화보기" },
  { id: 102, text: "클럽/나이트클럽 가기" },
  { id: 103, text: "공연보기" },
  { id: 104, text: "콘서트보기" },
  { id: 105, text: "박물관가기" },
  { id: 106, text: "공원가기" },
  { id: 107, text: "캠핑하기" },
  { id: 108, text: "해변가기" },
  { id: 109, text: "스포츠 관람" },
  { id: 110, text: "주거 개선" },
]);

// Part 4: 취미 (기본값)
const hobbyOptions = ref([
  { id: 201, text: "아이에게 책 읽어주기" },
  { id: 202, text: "음악 감상하기" },
  { id: 203, text: "악기 연주하기" },
  { id: 204, text: "혼자 노래부르거나 합창하기" },
  { id: 205, text: "춤추기" },
  { id: 206, text: "글쓰기(편지, 단문, 시 등)" },
  { id: 207, text: "그림 그리기" },
  { id: 208, text: "요리하기" },
  { id: 209, text: "애완동물 기르기" },
]);

// Part 4: 운동 (기본값)
const exerciseOptions = ref([
  { id: 301, text: "농구" },
  { id: 302, text: "야구/소프트볼" },
  { id: 303, text: "축구" },
  { id: 304, text: "미식축구" },
  { id: 305, text: "하키" },
  { id: 306, text: "크리켓" },
  { id: 307, text: "골프" },
  { id: 308, text: "배구" },
  { id: 309, text: "테니스" },
  { id: 310, text: "배드민턴" },
  { id: 311, text: "탁구" },
  { id: 312, text: "수영" },
  { id: 313, text: "자전거" },
  { id: 314, text: "스키/스노우보드" },
  { id: 315, text: "아이스 스케이트" },
  { id: 316, text: "조깅" },
  { id: 317, text: "걷기" },
  { id: 318, text: "요가" },
  { id: 319, text: "하이킹/트레킹" },
  { id: 320, text: "낚시" },
  { id: 321, text: "헬스" },
  { id: 322, text: "운동을 전혀 하지 않음" },
]);

// Part 4: 휴가/출장 (기본값)
const holidayOptions = ref([
  { id: 401, text: "국내출장" },
  { id: 402, text: "해외출장" },
  { id: 403, text: "집에서 보내는 휴가" },
  { id: 404, text: "국내 여행" },
  { id: 405, text: "해외 여행" },
]);

// Part 4: 총 선택 개수
const totalSelected = computed(() => {
  return (
    surveyData.value.leisure.length +
    surveyData.value.hobby.length +
    surveyData.value.exercise.length +
    surveyData.value.holiday.length
  );
});

// Part 1: 직군 선택 핸들러
const handleOccupationChange = (optionId) => {
  // 모든 하위 질문 초기화
  Object.keys(showSubQuestions.value).forEach((key) => {
    if (
      key.startsWith("hasJob") ||
      key.startsWith("workPeriod") ||
      key.startsWith("manager") ||
      key === "teachLocation"
    ) {
      showSubQuestions.value[key] = false;
    }
  });

  surveyData.value.hasJob = null;
  surveyData.value.workPeriodAnswerId = null;
  surveyData.value.manager = null;
  surveyData.value.teachAnswerId = null;

  // 선택에 따라 하위 질문 표시
  if (optionId === 1) {
    showSubQuestions.value.hasJob_company = true;
  } else if (optionId === 2) {
    showSubQuestions.value.hasJob_home = true;
  } else if (optionId === 3) {
    showSubQuestions.value.teachLocation = true;
  }
};

// 교육 분야 선택 후
const handleTeachChange = () => {
  showSubQuestions.value.hasJob_teacher = true;
};

// 직업 유무 선택 핸들러
const handleHasJobChange = (value, type) => {
  // 하위 질문 초기화
  showSubQuestions.value[`workPeriod_${type}`] = false;
  showSubQuestions.value[`manager_${type}`] = false;
  surveyData.value.workPeriodAnswerId = null;
  surveyData.value.manager = null;

  if (value) {
    showSubQuestions.value[`workPeriod_${type}`] = true;
  }
};

// 근무기간 선택 핸들러
const handleWorkPeriodChange = (optionId, type) => {
  showSubQuestions.value[`manager_${type}`] = false;
  surveyData.value.manager = null;

  // 2개월 이상 선택 시 관리직 질문 표시
  const showManagerIds = [2, 3];
  if (showManagerIds.includes(optionId)) {
    showSubQuestions.value[`manager_${type}`] = true;
  }
};

// Part 2: 학생 여부 선택
const handleStudentChange = (value) => {
  showSubQuestions.value.currentCourse = false;
  showSubQuestions.value.pastCourse = false;
  surveyData.value.classTypeAnswerId = null;

  if (value) {
    showSubQuestions.value.currentCourse = true;
  } else {
    showSubQuestions.value.pastCourse = true;
  }
};

// 다음 버튼 활성화 여부
const canGoNext = computed(() => {
  if (currentStep.value === 1) {
    // Part 1 검증
    if (!surveyData.value.occupationAnswerId) return false;

    // 사업/회사(1)
    if (surveyData.value.occupationAnswerId === 1) {
      if (surveyData.value.hasJob === null) return false;
      if (surveyData.value.hasJob) {
        if (!surveyData.value.workPeriodAnswerId) return false;
        if (showSubQuestions.value.manager_company && surveyData.value.manager === null) return false;
      }
    }

    // 재택(2)
    if (surveyData.value.occupationAnswerId === 2) {
      if (surveyData.value.hasJob === null) return false;
      if (surveyData.value.hasJob) {
        if (!surveyData.value.workPeriodAnswerId) return false;
        if (showSubQuestions.value.manager_home && surveyData.value.manager === null) return false;
      }
    }

    // 교사(3)
    if (surveyData.value.occupationAnswerId === 3) {
      if (!surveyData.value.teachAnswerId) return false;
      if (surveyData.value.hasJob === null) return false;
      if (surveyData.value.hasJob) {
        if (!surveyData.value.workPeriodAnswerId) return false;
        if (showSubQuestions.value.manager_teacher && surveyData.value.manager === null) return false;
      }
    }

    return true;
  }

  if (currentStep.value === 2) {
    // Part 2 검증
    if (surveyData.value.student === null) return false;
    if (
      showSubQuestions.value.currentCourse &&
      !surveyData.value.classTypeAnswerId
    )
      return false;
    if (
      showSubQuestions.value.pastCourse &&
      !surveyData.value.classTypeAnswerId
    )
      return false;
    return true;
  }

  if (currentStep.value === 3) {
    // Part 3 검증
    return surveyData.value.residenceAnswerId !== null;
  }

  if (currentStep.value === 4) {
    // Part 4 검증
    const leisureOk = surveyData.value.leisure.length >= 2;
    const hobbyOk = surveyData.value.hobby.length >= 1;
    const exerciseOk = surveyData.value.exercise.length >= 1;
    const holidayOk = surveyData.value.holiday.length >= 1;
    return (
      leisureOk &&
      hobbyOk &&
      exerciseOk &&
      holidayOk &&
      totalSelected.value >= 12
    );
  }

  return false;
});

const goBack = () => {
  if (currentStep.value > 1) {
    currentStep.value--;
  } else {
    // 1단계에서 뒤로가기 시 홈 또는 이전 페이지로 이동
    if (confirm('설문 조사를 중단하고 홈으로 이동하시겠습니까?')) {
      router.push('/');
    }
  }
};

const goNext = () => {
  if (currentStep.value < totalSteps) {
    currentStep.value++;
  } else {
    submitSurvey();
  }
};

// 설문 임시 저장 및 레벨 선택 페이지로 이동
const submitSurvey = () => {
  try {
    // 선택지 순서(1, 2, 3, 4...)
    // 프론트엔드 내부 ID를 그대로 전송

    // [Backend Compatibility Workaround]
    // 백엔드 이슈: SurveyMapper.java에서 `teachAt`(강사 근무처) 필드에 
    // `teachAnswerId`가 아닌 `classTypeAnswerId`(수강 강의) 값을 잘못 매핑하고 있습니다.
    // `classTypeAnswerId`가 4(5년 이상)일 경우, `TeachingLevel` Enum(1~3) 범위를 벗어나 400 에러가 발생합니다.
    // 이를 방지하기 위해 4번 값은 null로 변환하여 전송합니다. (백엔드 수정 시 해당 로직 제거 가능)
    let safeClassTypeAnswerId = surveyData.value.classTypeAnswerId;
    if (safeClassTypeAnswerId === 4) {
      safeClassTypeAnswerId = null;
    }

    const payload = {
      occupationAnswerId: surveyData.value.occupationAnswerId, // 1, 2, 3, 4
      hasJob: surveyData.value.hasJob,
      workPeriodAnswerId: surveyData.value.workPeriodAnswerId, // 1, 2, 3
      teachAnswerId: surveyData.value.teachAnswerId, // 1, 2, 3
      manager: surveyData.value.manager,
      student: surveyData.value.student,
      classTypeAnswerId: safeClassTypeAnswerId, // Sanitized Value
      residenceAnswerId: surveyData.value.residenceAnswerId, // 1, 2, 3, 4, 5
      leisure: surveyData.value.leisure,
      hobby: surveyData.value.hobby,
      exercise: surveyData.value.exercise,
      holiday: surveyData.value.holiday,
    };

    console.log("설문 데이터 저장 (전달용):", payload);
    
    // Store에 데이터 저장
    const surveyStore = useSurveyStore();
    surveyStore.setSurveyData(payload);

    // 레벨 선택 페이지로 이동
    router.push({
      path: "/survey/level",
      query: { ...route.query },
    });
  } catch (error) {
    console.error("설문 임시 저장 실패:", error);
    alert("오류가 발생했습니다. 다시 시도해주세요.");
  }
};

const showGuide = ref(false);


</script>

<template>
  <div class="page-container">
    <header class="survey-header">
      <div class="info-section">
        <button @click="showGuide = true" class="info-btn">
          <span class="material-symbols-outlined">info</span>
        </button>
      </div>
    </header>

    <main class="page-content">
      <div class="survey-container">
        <!-- Step Progress -->
        <nav class="step-progress">
          <div class="step active">
            <div class="step-content">
              <span class="step-number">Step 1</span>
              <span class="step-label">Background Survey</span>
            </div>
          </div>

          <div class="step">
            <div class="step-content">
              <span class="step-number">Step 2</span>
              <span class="step-label">Self Assessment</span>
            </div>
          </div>

          <div class="step last">
            <div class="step-content">
              <span class="step-number">Step 3</span>
              <span class="step-label">Setup</span>
            </div>
          </div>

        </nav>

        <section class="survey-content">
          <h1 class="survey-title">Background Survey</h1>
          <p class="survey-instructions">
            질문을 읽고 정확히 답변해 주시기 바랍니다. 설문에 대한 응답을 기초로
            개인별 문항이 출제됩니다.
          </p>

          <h2 class="part-title">Part {{ currentStep }} of {{ totalSteps }}</h2>

          <!-- Part 1: 직업 -->
          <div v-show="currentStep === 1" class="questions-container">
            <!-- 1-1. 직군 -->
            <div class="question-block">
              <p class="question-text">
                현재 귀하는 어느 분야에 종사하고 계십니까?
              </p>
              <div
                class="form-check"
                v-for="option in occupationOptions"
                :key="option.id"
              >
                <label class="form-check-label">
                  <input
                    type="radio"
                    :value="option.id"
                    v-model="surveyData.occupationAnswerId"
                    @change="handleOccupationChange(option.id)"
                    class="form-check-input"
                  />
                  <span>{{ option.text }}</span>
                </label>
              </div>
            </div>

            <!-- 1-2. 교육 분야 (교사 선택 시) -->
            <transition name="fade-slide">
              <div v-if="showSubQuestions.teachLocation" class="question-block">
                <p class="question-text">
                  현재 귀하는 어디에서 학생을 가르치십니까?
                </p>
                <div
                  class="form-check"
                  v-for="option in teachOptions"
                  :key="option.id"
                >
                  <label class="form-check-label">
                    <input
                      type="radio"
                      :value="option.id"
                      v-model="surveyData.teachAnswerId"
                      @change="handleTeachChange"
                      class="form-check-input"
                    />
                    <span>{{ option.text }}</span>
                  </label>
                </div>
              </div>
            </transition>

            <!-- 1-3. 직업 유무 (사업/회사) -->
            <transition name="fade-slide">
              <div
                v-if="showSubQuestions.hasJob_company"
                class="question-block"
              >
                <p class="question-text">현재 귀하는 직업이 있으십니까?</p>
                <div class="form-check">
                  <label class="form-check-label">
                    <input
                      type="radio"
                      :value="true"
                      v-model="surveyData.hasJob"
                      @change="handleHasJobChange(true, 'company')"
                      class="form-check-input"
                    />
                    <span>예</span>
                  </label>
                </div>
                <div class="form-check">
                  <label class="form-check-label">
                    <input
                      type="radio"
                      :value="false"
                      v-model="surveyData.hasJob"
                      @change="handleHasJobChange(false, 'company')"
                      class="form-check-input"
                    />
                    <span>아니요</span>
                  </label>
                </div>
              </div>
            </transition>

            <!-- 1-4. 직업 유무 (재택) -->
            <transition name="fade-slide">
              <div v-if="showSubQuestions.hasJob_home" class="question-block">
                <p class="question-text">현재 귀하는 직업이 있으십니까?</p>
                <div class="form-check">
                  <label class="form-check-label">
                    <input
                      type="radio"
                      :value="true"
                      v-model="surveyData.hasJob"
                      @change="handleHasJobChange(true, 'home')"
                      class="form-check-input"
                    />
                    <span>예</span>
                  </label>
                </div>
                <div class="form-check">
                  <label class="form-check-label">
                    <input
                      type="radio"
                      :value="false"
                      v-model="surveyData.hasJob"
                      @change="handleHasJobChange(false, 'home')"
                      class="form-check-input"
                    />
                    <span>아니요</span>
                  </label>
                </div>
              </div>
            </transition>

            <!-- 1-5. 직업 유무 (교사) -->
            <transition name="fade-slide">
              <div
                v-if="showSubQuestions.hasJob_teacher"
                class="question-block"
              >
                <p class="question-text">현재 귀하는 직업이 있으십니까?</p>
                <div class="form-check">
                  <label class="form-check-label">
                    <input
                      type="radio"
                      :value="true"
                      v-model="surveyData.hasJob"
                      @change="handleHasJobChange(true, 'teacher')"
                      class="form-check-input"
                    />
                    <span>예</span>
                  </label>
                </div>
                <div class="form-check">
                  <label class="form-check-label">
                    <input
                      type="radio"
                      :value="false"
                      v-model="surveyData.hasJob"
                      @change="handleHasJobChange(false, 'teacher')"
                      class="form-check-input"
                    />
                    <span>아니요</span>
                  </label>
                </div>
              </div>
            </transition>

            <!-- 1-6. 근무 기간 (사업/회사) -->
            <transition name="fade-slide">
              <div
                v-if="showSubQuestions.workPeriod_company"
                class="question-block"
              >
                <p class="question-text">귀하의 근무 기간은 얼마나 되십니까?</p>
                <div
                  class="form-check"
                  v-for="option in workPeriodOptionsCompany"
                  :key="option.id"
                >
                  <label class="form-check-label">
                    <input
                      type="radio"
                      :value="option.id"
                      v-model="surveyData.workPeriodAnswerId"
                      @change="handleWorkPeriodChange(option.id, 'company')"
                      class="form-check-input"
                    />
                    <span>{{ option.text }}</span>
                  </label>
                </div>
              </div>
            </transition>

            <!-- 1-7. 근무 기간 (재택) -->
            <transition name="fade-slide">
              <div
                v-if="showSubQuestions.workPeriod_home"
                class="question-block"
              >
                <p class="question-text">귀하의 근무 기간은 얼마나 되십니까?</p>
                <div
                  class="form-check"
                  v-for="option in workPeriodOptionsHome"
                  :key="option.id"
                >
                  <label class="form-check-label">
                    <input
                      type="radio"
                      :value="option.id"
                      v-model="surveyData.workPeriodAnswerId"
                      @change="handleWorkPeriodChange(option.id, 'home')"
                      class="form-check-input"
                    />
                    <span>{{ option.text }}</span>
                  </label>
                </div>
              </div>
            </transition>

            <!-- 1-8. 근무 기간 (교사) -->
            <transition name="fade-slide">
              <div
                v-if="showSubQuestions.workPeriod_teacher"
                class="question-block"
              >
                <p class="question-text">귀하의 근무 기간은 얼마나 되십니까?</p>
                <div
                  class="form-check"
                  v-for="option in workPeriodOptionsTeacher"
                  :key="option.id"
                >
                  <label class="form-check-label">
                    <input
                      type="radio"
                      :value="option.id"
                      v-model="surveyData.workPeriodAnswerId"
                      @change="handleWorkPeriodChange(option.id, 'teacher')"
                      class="form-check-input"
                    />
                    <span>{{ option.text }}</span>
                  </label>
                </div>
              </div>
            </transition>

            <!-- 1-9. 관리직 여부 (사업/회사) -->
            <transition name="fade-slide">
              <div
                v-if="showSubQuestions.manager_company"
                class="question-block"
              >
                <p class="question-text">
                  귀하는 부하직원을 관리하는 관리직을 맡고 있습니까?
                </p>
                <div class="form-check">
                  <label class="form-check-label">
                    <input
                      type="radio"
                      :value="true"
                      v-model="surveyData.manager"
                      class="form-check-input"
                    />
                    <span>예</span>
                  </label>
                </div>
                <div class="form-check">
                  <label class="form-check-label">
                    <input
                      type="radio"
                      :value="false"
                      v-model="surveyData.manager"
                      class="form-check-input"
                    />
                    <span>아니요</span>
                  </label>
                </div>
              </div>
            </transition>

            <!-- 1-10. 관리직 여부 (재택) -->
            <transition name="fade-slide">
              <div v-if="showSubQuestions.manager_home" class="question-block">
                <p class="question-text">
                  귀하는 부하직원을 관리하는 관리직을 맡고 있습니까?
                </p>
                <div class="form-check">
                  <label class="form-check-label">
                    <input
                      type="radio"
                      :value="true"
                      v-model="surveyData.manager"
                      class="form-check-input"
                    />
                    <span>예</span>
                  </label>
                </div>
                <div class="form-check">
                  <label class="form-check-label">
                    <input
                      type="radio"
                      :value="false"
                      v-model="surveyData.manager"
                      class="form-check-input"
                    />
                    <span>아니요</span>
                  </label>
                </div>
              </div>
            </transition>

            <!-- 1-11. 관리직 여부 (교사) -->
            <transition name="fade-slide">
              <div
                v-if="showSubQuestions.manager_teacher"
                class="question-block"
              >
                <p class="question-text">
                  귀하는 부하직원을 관리하는 관리직을 맡고 있습니까?
                </p>
                <div class="form-check">
                  <label class="form-check-label">
                    <input
                      type="radio"
                      :value="true"
                      v-model="surveyData.manager"
                      class="form-check-input"
                    />
                    <span>예</span>
                  </label>
                </div>
                <div class="form-check">
                  <label class="form-check-label">
                    <input
                      type="radio"
                      :value="false"
                      v-model="surveyData.manager"
                      class="form-check-input"
                    />
                    <span>아니요</span>
                  </label>
                </div>
              </div>
            </transition>
          </div>

          <!-- Part 2: 학생 -->
          <div v-show="currentStep === 2" class="questions-container">
            <!-- 2-1. 학생 여부 -->
            <div class="question-block">
              <p class="question-text">현재 당신은 학생입니까?</p>
              <div class="form-check">
                <label class="form-check-label">
                  <input
                    type="radio"
                    :value="true"
                    v-model="surveyData.student"
                    @change="handleStudentChange(true)"
                    class="form-check-input"
                  />
                  <span>예</span>
                </label>
              </div>
              <div class="form-check">
                <label class="form-check-label">
                  <input
                    type="radio"
                    :value="false"
                    v-model="surveyData.student"
                    @change="handleStudentChange(false)"
                    class="form-check-input"
                  />
                  <span>아니요</span>
                </label>
              </div>
            </div>

            <!-- 2-2. 현재 수강 중 -->
            <transition name="fade-slide">
              <div v-if="showSubQuestions.currentCourse" class="question-block">
                <p class="question-text">현재 어떤 강의를 듣고 있습니까?</p>
                <div
                  class="form-check"
                  v-for="option in currentCourseOptions"
                  :key="option.id"
                >
                  <label class="form-check-label">
                    <input
                      type="radio"
                      :value="option.id"
                      v-model="surveyData.classTypeAnswerId"
                      class="form-check-input"
                    />
                    <span>{{ option.text }}</span>
                  </label>
                </div>
              </div>
            </transition>

            <!-- 2-3. 과거 수강 -->
            <transition name="fade-slide">
              <div v-if="showSubQuestions.pastCourse" class="question-block">
                <p class="question-text">최근 어떤 강의를 수강했습니까?</p>
                <div
                  class="form-check"
                  v-for="option in pastCourseOptions"
                  :key="option.id"
                >
                  <label class="form-check-label">
                    <input
                      type="radio"
                      :value="option.id"
                      v-model="surveyData.classTypeAnswerId"
                      class="form-check-input"
                    />
                    <span>{{ option.text }}</span>
                  </label>
                </div>
              </div>
            </transition>
          </div>

          <!-- Part 3: 거주지 -->
          <div v-show="currentStep === 3" class="questions-container">
            <div class="question-block">
              <p class="question-text">현재 귀하는 어디에 살고 계십니까?</p>
              <div
                class="form-check"
                v-for="option in residenceOptions"
                :key="option.id"
              >
                <label class="form-check-label">
                  <input
                    type="radio"
                    :value="option.id"
                    v-model="surveyData.residenceAnswerId"
                    class="form-check-input"
                  />
                  <span>{{ option.text }}</span>
                </label>
              </div>
            </div>
          </div>

          <!-- Part 4: 취미/관심사 -->
          <div v-show="currentStep === 4" class="questions-container">
            <p class="group-instruction">
              아래의 설문에서 총 <span class="highlight">12개</span> 이상의
              항목을 선택하십시오.
              <span
                class="selection-count"
                :class="{ valid: totalSelected >= 12 }"
              >
                <span class="highlight">{{ totalSelected }}개</span> 항목을
                선택했습니다.
              </span>
            </p>

            <!-- 4-1. 여가 활동 -->
            <div class="question-block">
              <p class="question-text">
                귀하는 여가 활동으로 주로 무엇을 하십니까? (두 개 이상 선택)
              </p>
              <div v-if="surveyData.leisure.length < 2" class="error-msg">
                여가 활동에서 2개 이상을 고르세요.
              </div>
              <div
                class="form-check"
                v-for="option in leisureOptions"
                :key="option.id"
              >
                <label class="form-check-label">
                  <input
                    type="checkbox"
                    :value="option.id"
                    v-model="surveyData.leisure"
                    class="form-check-input"
                  />
                  <span>{{ option.text }}</span>
                </label>
              </div>
            </div>

            <!-- 4-2. 취미 -->
            <div class="question-block">
              <p class="question-text">
                귀하의 취미나 관심사는 무엇입니까? (한 개 이상 선택)
              </p>
              <div v-if="surveyData.hobby.length < 1" class="error-msg">
                취미나 관심사 중 1개 이상을 고르세요.
              </div>
              <div
                class="form-check"
                v-for="option in hobbyOptions"
                :key="option.id"
              >
                <label class="form-check-label">
                  <input
                    type="checkbox"
                    :value="option.id"
                    v-model="surveyData.hobby"
                    class="form-check-input"
                  />
                  <span>{{ option.text }}</span>
                </label>
              </div>
            </div>

            <!-- 4-3. 운동 -->
            <div class="question-block">
              <p class="question-text">
                귀하는 주로 어떤 운동을 즐기십니까?(한 개 이상 선택)
              </p>
              <div v-if="surveyData.exercise.length < 1" class="error-msg">
                운동 중 1개 이상을 고르세요.
              </div>
              <div
                class="form-check"
                v-for="option in exerciseOptions"
                :key="option.id"
              >
                <label class="form-check-label">
                  <input
                    type="checkbox"
                    :value="option.id"
                    v-model="surveyData.exercise"
                    class="form-check-input"
                  />
                  <span>{{ option.text }}</span>
                </label>
              </div>
            </div>

            <!-- 4-4. 휴가/출장 -->
            <div class="question-block">
              <p class="question-text">
                귀하는 어떤 휴가나 출장을 다녀온 경험이 있습니까?(한 개 이상
                선택)
              </p>
              <div v-if="surveyData.holiday.length < 1" class="error-msg">
                휴가 나 출장 중 1개 이상을 고르세요.
              </div>
              <div
                class="form-check"
                v-for="option in holidayOptions"
                :key="option.id"
              >
                <label class="form-check-label">
                  <input
                    type="checkbox"
                    :value="option.id"
                    v-model="surveyData.holiday"
                    class="form-check-input"
                  />
                  <span>{{ option.text }}</span>
                </label>
              </div>
            </div>
          </div>
        </section>

        <!-- Navigation Buttons -->
        <footer class="survey-footer">
          <button
            @click="goBack"
            class="nav-btn back-btn"
          >
            Back
          </button>
          <button @click="router.push('/')" class="nav-btn quit-btn">Quit</button>
          <button
            @click="goNext"
            class="nav-btn next-btn"
            :disabled="!canGoNext"
          >
            Next
          </button>
        </footer>
      </div>
    </main>

    <!-- Guide Modal -->
    <transition name="fade">
      <div v-if="showGuide" class="modal-overlay" @click="showGuide = false">
        <div class="modal-card" @click.stop>
          <div class="modal-header">
            <h3>Guide</h3>
          </div>
          <div class="modal-body">
            <ul class="guide-list">
              <li>· Background Survey 화면입니다.</li>
              <li>
                · 선택하신 항목을 기초로 개인별 문항이 출제되니, 모든 설문에
                빠짐없이 답변해 주십시오.
              </li>
              <li>· Part 4에서는 총 12개 이상의 항목을 선택해야 합니다.</li>
              <li>
                · 시험 문항은 설문에 포함되지 않은 일반 주제의 질문도 출제되니
                유의바랍니다.
              </li>
            </ul>
          </div>
          <div class="modal-footer">
            <button @click="showGuide = false" class="close-btn">닫기</button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;400;500;700&display=swap");
@import url("https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght@100..700&display=swap");

.survey-page {
  min-height: 100vh;
  background: var(--bg-primary);
  font-family: "Noto Sans KR", sans-serif;
  display: flex;
  flex-direction: column;
}

.page-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 32px 64px;
}

@media (max-width: 1024px) {
  .page-content {
    padding: 24px 32px;
  }
}

@media (max-width: 768px) {
  .page-content {
    padding: 16px 24px;
  }
}

/* Step Progress */
.step-progress {
  display: flex;
  height: 44px;
  margin-bottom: 40px;
  width: 100%;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}

.step {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-secondary);
  color: var(--text-tertiary);
  font-size: 12px;
  margin-right: 1px;
  transition: all 0.3s ease;
}

.step.active {
  background: var(--primary-color) !important;
  color: #212529 !important;
  font-weight: 700;
}

.step.completed {
  background: var(--primary-light);
  color: #8B7300;
}

.step-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.step-number {
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 4px;
}
.step-label {
  font-size: 10px;
}

/* Survey Content */
.survey-content {
  margin-bottom: 80px;
}

.survey-title {
  font-size: 1.75rem;
  font-weight: 800;
  margin-bottom: 12px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-primary);
  color: var(--text-primary);
}

.survey-instructions {
  font-size: 1rem;
  margin-bottom: 40px;
  color: var(--text-secondary);
  line-height: 1.6;
}

.part-title {
  font-size: 1.25rem;
  font-weight: 700;
  margin-bottom: 24px;
  color: var(--primary-color);
}

.questions-container {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.question-block {
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: var(--bg-secondary);
  padding: 24px;
  border-radius: 20px;
  border: var(--border-primary);
  box-shadow: var(--shadow-sm);
}

.question-text {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.form-check {
  margin-bottom: 4px;
}

.form-check-label {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 14px 20px;
  border-radius: 12px;
  transition: all 0.2s;
  background: var(--bg-tertiary);
  border: 1px solid transparent;
}

.form-check-label:hover {
  background: var(--bg-secondary);
  border-color: var(--primary-color);
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
}

.form-check-label:has(input:checked) {
  background: var(--primary-light);
  border-color: var(--primary-color);
  color: #8B7300;
}

.form-check-input {
  width: 20px;
  height: 20px;
  accent-color: var(--primary-color);
  cursor: pointer;
}

.form-check-label span {
  font-size: 0.95rem;
  font-weight: 500;
}

/* Navigation */
.survey-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 20px 40px;
  background: var(--bg-secondary);
  z-index: 100;
  border-top: 1px solid var(--border-primary);
  box-shadow: 0 -4px 12px rgba(0, 0, 0, 0.05);
}

.nav-btn {
  padding: 12px 32px;
  border-radius: 12px;
  border: none;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-size: 0.95rem;
  min-width: 120px;
}

.back-btn {
  background: var(--bg-tertiary);
  color: var(--text-secondary);
}

.back-btn:hover:not(:disabled) {
  background: #e2e8f0;
  color: var(--text-primary);
}

.next-btn {
  background: var(--primary-color);
  color: #212529;
  box-shadow: var(--shadow-sm);
}

.next-btn:hover:not(:disabled) {
  background: var(--primary-hover);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.next-btn:disabled,
.back-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.quit-btn {
  background: #fee2e2;
  color: #ef4444;
}

.quit-btn:hover {
  background: #fecaca;
}

/* Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.modal-card {
  background: var(--bg-secondary);
  border-radius: 20px;
  max-width: 500px;
  width: 90%;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  border: var(--border-primary);
}

.modal-header {
  padding: 24px;
  border-bottom: 1px solid var(--border-primary);
}

.modal-header h3 {
  font-size: 1.25rem;
  font-weight: 700;
  margin: 0;
  color: var(--text-primary);
}

.modal-body {
  padding: 24px;
}

.guide-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.guide-list li {
  color: var(--text-secondary);
  line-height: 1.6;
  position: relative;
  padding-left: 20px;
}

.guide-list li::before {
  content: "•";
  position: absolute;
  left: 0;
  color: var(--primary-color);
  font-weight: bold;
}

.modal-footer {
  padding: 16px 24px;
  display: flex;
  justify-content: flex-end;
}

.close-btn {
  padding: 10px 24px;
  background: var(--bg-tertiary);
  border: none;
  border-radius: 10px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--text-primary);
}

.close-btn:hover {
  background: #e2e8f0;
}

/* Animations */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>