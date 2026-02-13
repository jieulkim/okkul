<script setup>
import { ref, computed, inject } from "vue";
import { useRouter, useRoute, onBeforeRouteLeave } from "vue-router";
import { useSurveyStore } from "@/stores/survey";

const router = useRouter();
const route = useRoute();

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

// Part 4 유효성 검사 (각 카테고리별 최소 조건 + 전체 12개 이상)
const isPart4Valid = computed(() => {
  const leisureOk = surveyData.value.leisure.length >= 2;
  const hobbyOk = surveyData.value.hobby.length >= 1;
  const exerciseOk = surveyData.value.exercise.length >= 1;
  const holidayOk = surveyData.value.holiday.length >= 1;
  return leisureOk && hobbyOk && exerciseOk && holidayOk && totalSelected.value >= 12;
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
    return isPart4Valid.value;
  }

  return false;
});

const goBack = () => {
  if (currentStep.value > 1) {
    currentStep.value--;
  } else {
    handleExit();
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
    const payload = {
      occupationAnswerId: surveyData.value.occupationAnswerId, // 1, 2, 3, 4
      hasJob: surveyData.value.hasJob,
      workPeriodAnswerId: surveyData.value.workPeriodAnswerId, // 1, 2, 3
      teachAnswerId: surveyData.value.teachAnswerId, // 1, 2, 3
      manager: surveyData.value.manager,
      student: surveyData.value.student,
      classTypeAnswerId: surveyData.value.classTypeAnswerId, // 1, 2, 3, 4
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
    isExiting.value = true;
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
const showExitModal = ref(false);
const isExiting = ref(false);

const handleExit = () => {
  showExitModal.value = true;
};

const confirmExit = () => {
  isExiting.value = true;
  showExitModal.value = false;
  
  const from = route.query.from;
  if (from === 'exam') {
    router.push('/exam');
  } else if (from === 'practice') {
    router.push({ path: '/practice', query: { ...route.query } });
  } else {
    router.push('/');
  }
};

// 브라우저 뒤로가기 및 내비게이션 보호
onBeforeRouteLeave((to, from, next) => {
  if (isExiting.value) {
    next();
  } else {
    showExitModal.value = true;
    next(false);
  }
});


</script>

<template>
  <div class="page-container">
    <main class="page-content">
      <div class="survey-container">
        <div class="survey-header-section">
          <button @click="handleExit" class="quit-btn">
            <span class="material-symbols-outlined">close</span>
            나가기
          </button>
          <button @click="showGuide = true" class="info-btn">
            <span class="material-symbols-outlined">info</span>
          </button>
        </div>

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
                :class="{ valid: isPart4Valid }"
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

        <!-- 플로팅 선택 개수 배지 파트 4 -->
        <transition name="fade-scale">
          <div 
            v-if="currentStep === 4" 
            class="floating-selection-badge"
            :class="{ 'is-valid': isPart4Valid }"
          >
            <div class="badge-content">
              <span class="badge-label">선택 항목</span>
              <span class="badge-count">{{ totalSelected }}</span>
              <span class="badge-target">/ 12</span>
            </div>
            <div v-if="isPart4Valid" class="badge-check">
              <span class="material-icons">check_circle</span>
            </div>
          </div>
        </transition>

        <!-- Navigation Controls -->
        <div class="navigation-controls">
          <button @click="goBack" class="nav-btn back-btn">Back</button>
          <button
            @click="goNext"
            class="nav-btn next-btn"
            :disabled="!canGoNext"
          >
            Next
          </button>
        </div>
      </div>
    </main>

    <!-- Guide Modal -->
    <transition name="fade">
      <div v-if="showGuide" class="modal-overlay" @click="showGuide = false">
        <div class="modal-card" @click.stop>
          <div class="modal-header">
            <h3>가이드</h3>
            <p class="subtitle">설문 작성 안내</p>
          </div>
          <div class="guide-content">
            <div class="guide-item">
              <span class="material-icons guide-icon">assignment</span>
              <div class="guide-text">
                <strong>Background Survey</strong>
                <p>선택하신 항목을 기초로 개인별 문항이 출제됩니다</p>
              </div>
            </div>
            <div class="guide-item">
              <span class="material-icons guide-icon">checklist</span>
              <div class="guide-text">
                <strong>필수 선택 개수</strong>
                <p>Part 4에서는 총 12개 이상의 항목을 선택해야 합니다</p>
              </div>
            </div>
            <div class="guide-item">
              <span class="material-icons guide-icon">info</span>
              <div class="guide-text">
                <strong>추가 문항 안내</strong>
                <p>시험 문항은 설문에 포함되지 않은 일반 주제의 질문도 출제됩니다</p>
              </div>
            </div>
          </div>
          <button @click="showGuide = false" class="modal-close-btn">확인</button>
        </div>
      </div>
    </transition>

    <!-- Exit Confirmation Modal -->
    <transition name="fade">
      <div v-if="showExitModal" class="modal-overlay" @click="showExitModal = false">
        <div class="modal-card exit-modal" @click.stop>
          <div class="modal-header">
            <h3>설문 중단</h3>
            <p class="subtitle">설문 조사를 중단하시겠습니까?<br>지금까지 입력한 정보는 저장되지 않습니다.</p>
          </div>
          <div class="modal-footer">
            <button @click="showExitModal = false" class="modal-btn cancel">계속하기</button>
            <button @click="confirmExit" class="modal-btn confirm">나가기</button>
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
  /* max-height 제거하여 자연스러운 스크롤 허용, 필요시 내부 스크롤로 변경 */
  /* max-height: 100vh; */
  background: var(--bg-primary);
  font-family: "Noto Sans KR", sans-serif;
  display: flex;
  flex-direction: column;
}

.page-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px 64px; /* 상단 패딩 축소 (32px -> 24px) */
  flex: 1;
  width: 100%;
  box-sizing: border-box;
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
  margin-bottom: 32px; /* 하단 여백 축소 (40px -> 32px) */
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

/* Survey Header Section */
.survey-header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px; /* 하단 여백 축소 (24px -> 16px) */
  padding: 0;
}

.quit-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: var(--bg-tertiary);
  border: 1px solid var(--border-primary);
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--text-secondary);
}

.quit-btn:hover {
  background: #fee2e2;
  color: #ef4444;
}

.info-btn {
  background: none;
  border: none;
  color: var(--text-tertiary);
  cursor: pointer;
  transition: color 0.2s;
  padding: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.info-btn:hover {
  color: var(--primary-color);
}

/* Navigation Controls */
.navigation-controls {
  max-width: 1280px;
  margin: 32px auto 0;
  padding: 0;
  display: flex;
  justify-content: center;
  gap: 16px;
}

.nav-btn {
  padding: 12px 32px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-size: 0.95rem;
  min-width: 120px;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.back-btn {
  background: rgba(var(--bg-tertiary-rgb), 0.5) !important;
  color: var(--text-secondary);
}

.back-btn:hover:not(:disabled) {
  background: rgba(226, 232, 240, 0.8) !important;
  color: var(--text-primary);
}

.next-btn {
  background: rgba(253, 216, 53, 0.7) !important;
  color: #212529;
  box-shadow: 0 4px 15px rgba(251, 191, 36, 0.1);
}

.next-btn:hover:not(:disabled) {
  background: rgba(251, 192, 45, 0.8) !important;
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(251, 191, 36, 0.2);
}

.next-btn:disabled,
.back-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

/* 모달 스타일 */
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
  background: #FFFFFF !important;
  border-radius: 24px;
  max-width: 520px;
  width: 90%;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
  border: 1px solid #e2e8f0;
  overflow: hidden;
  padding-bottom: 32px;
  z-index: 2000;
}

.modal-header {
  padding: 40px 40px 20px;
  text-align: center;
  background: inherit;
}

.modal-header h3 {
  font-size: 2rem;
  font-weight: 800;
  margin: 0 0 12px;
  color: #0f172a;
}

.modal-header .subtitle {
  font-size: 1rem;
  color: #64748b;
  margin: 0;
  line-height: 1.5;
}

.guide-content {
  padding: 24px 40px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.guide-item {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.guide-icon {
  color: var(--primary-color);
  font-size: 28px;
  flex-shrink: 0;
}

.guide-text {
  flex: 1;
}

.guide-text strong {
  display: block;
  font-size: 1rem;
  font-weight: 700;
  color: #0f172a;
  margin-bottom: 4px;
}

.guide-text p {
  font-size: 0.9rem;
  color: #64748b;
  line-height: 1.5;
  margin: 0;
}

.modal-close-btn {
  width: calc(100% - 80px); /* 좌우 여백 적용 */
  margin: 0 40px;
  padding: 16px;
  background: var(--primary-color);
  border: none;
  font-weight: 700;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.2s;
  color: #0f172a;
  border-radius: 12px;
}

.modal-close-btn:hover {
  background: var(--primary-hover);
  transform: translateY(-2px);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

/* Modal Footer & Buttons for Exit Modal */
.modal-footer {
  padding: 0 40px 40px;
  display: flex;
  gap: 16px;
  justify-content: center;
}

.modal-btn {
  flex: 1;
  padding: 16px;
  border-radius: 12px;
  font-weight: 700;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.modal-btn.cancel {
  background: #f1f5f9;
  color: #64748b;
}

.modal-btn.cancel:hover {
  background: #e2e8f0;
  color: #475569;
}

.modal-btn.confirm {
  background: #fee2e2;
  color: #ef4444;
}

.modal-btn.confirm:hover {
  background: #fecaca;
  transform: translateY(-2px);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
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

/* 플로팅 선택 개수 배지 스타일 */
.floating-selection-badge {
  position: fixed;
  bottom: 160px; /* 100px -> 160px 로 상향 조정 */
  right: 40px;
  background: rgba(255, 255, 255, 0.85); /* 투명도 조절 */
  backdrop-filter: blur(8px); /* 연한 유리 효과 */
  padding: 12px 24px;
  border-radius: 20px; /* 원형보다는 둥근 사각형 느낌으로 */
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08); /* 그림자 약화 */
  border: 1px solid rgba(var(--primary-color-rgb), 0.2);
  z-index: 100;
  pointer-events: none; /* 버튼처럼 클릭되지 않도록 설정 */
  transition: all 0.4s cubic-bezier(0.23, 1, 0.32, 1);
}

.floating-selection-badge.is-valid {
  background: rgba(255, 249, 196, 0.9); /* var(--primary-light) 느낌의 투명 배경 */
  border-color: var(--primary-color);
  transform: none; /* 스케일 변화 제거 (버튼 느낌 최소화) */
}

.badge-content {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.badge-label {
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text-secondary);
  margin-right: 4px;
}

.badge-count {
  font-size: 1.5rem;
  font-weight: 800;
  color: var(--primary-color);
}

.floating-selection-badge.is-valid .badge-count {
  color: #8B7300;
}

.badge-target {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--text-tertiary);
}

.badge-check {
  color: var(--primary-color);
  display: flex;
  align-items: center;
}

.floating-selection-badge.is-valid .badge-check {
  color: #8B7300;
}

/* Fade Scale Animation for Badge */
.fade-scale-enter-active,
.fade-scale-leave-active {
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.fade-scale-enter-from {
  opacity: 0;
  transform: scale(0.5) translateY(20px);
}

.fade-scale-leave-to {
  opacity: 0;
  transform: scale(0.8) translateY(10px);
}

@media (max-width: 768px) {
  .floating-selection-badge {
    right: 20px;
    bottom: 140px; /* 90px -> 140px 로 상향 조정 */
    padding: 10px 16px;
  }
}
</style>