<script setup>
import { ref, computed, watch, inject } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const isDarkMode = inject('isDarkMode', ref(false))

// 현재 파트 (1-4)
const currentPart = ref(1)
const totalParts = 4

// 설문 답변 저장
const answers = ref({
  leisure: [],
  hobbies: [],
  sports: [],
  vacation: []
})

// 선택된 항목 개수 추적 (Part 4용)
const selectedLeisureCount = computed(() => answers.value.leisure?.length || 0)
const selectedHobbyCount = computed(() => answers.value.hobbies?.length || 0)
const selectedSportsCount = computed(() => answers.value.sports?.length || 0)
const selectedVacationCount = computed(() => answers.value.vacation?.length || 0)

// Part 1: 직업 관련 조건부 질문 표시 상태
const showTeachingLocation = ref(false)
const showJobStatus_Business = ref(false)
const showJobStatus_Home = ref(false)
const showJobStatus_Teacher = ref(false)
const showWorkPeriod_Business = ref(false)
const showWorkPeriod_Home = ref(false)
const showWorkPeriod_Teacher = ref(false)
const showManagement_Business = ref(false)
const showManagement_Home = ref(false)
const showManagement_Teacher = ref(false)

// Part 2: 학생 관련 조건부 질문
const showCurrentCourse = ref(false)
const showPastCourse = ref(false)

// Part 1 질문 데이터
const jobFieldOptions = [
  { id: 1727, label: '사업/회사' },
  { id: 1728, label: '재택근무/재택사업' },
  { id: 1729, label: '교사/교육자' },
  { id: 1730, label: '일 경험 없음' }
]

const teachingLocations = [
  { id: 1741, label: '대학 이상' },
  { id: 1742, label: '초등/중/고등학교' },
  { id: 1743, label: '평생교육' }
]

const yesNoOptions = [
  { label: '예', value: 'yes' },
  { label: '아니요', value: 'no' }
]

const workPeriods_Business = [
  { id: 1731, label: '첫직장- 2개월 미만' },
  { id: 1732, label: '첫직장- 2개월 이상' },
  { id: 1733, label: '첫직장 아님 - 경험 많음' }
]

const workPeriods_Home = [
  { id: 1836, label: '첫직장- 2개월 미만' },
  { id: 1837, label: '첫직장- 2개월 이상' },
  { id: 1838, label: '첫직장 아님 - 경험 많음' }
]

const workPeriods_Teacher = [
  { id: 1747, label: '2개월 미만 - 첫직장' },
  { id: 1748, label: '2개월 미만 - 교직은 처음이지만 이전에 다른 직업을 가진 적이 있음' },
  { id: 1749, label: '2개월 이상' }
]

// Part 2: 학생/거주지
const studentOptions = [
  { id: 1971, label: '예' },
  { id: 1972, label: '아니요' }
]

const currentCourseOptions = [
  { id: 1964, label: '학위 과정 수업' },
  { id: 1965, label: '전문 기술 향상을 위한 평생 학습' },
  { id: 1966, label: '어학수업' }
]

const pastCourseOptions = [
  { id: 1967, label: '학위 과정 수업' },
  { id: 1968, label: '전문 기술 향상을 위한 평생 학습' },
  { id: 1969, label: '어학수업' },
  { id: 1970, label: '수강 후 5년 이상 지남' }
]

const residenceOptions = [
  { id: 1705, label: '개인주택이나 아파트에 홀로 거주' },
  { id: 1706, label: '친구나 룸메이트와 함께 주택이나 아파트에 거주' },
  { id: 1707, label: '가족(배우자/자녀/기타 가족 일원)과 함께 주택이나 아파트에 거주' },
  { id: 1708, label: '학교 기숙사' },
  { id: 1709, label: '군대 막사' }
]

// Part 4: 여가 활동
const leisureActivities = [
  { id: 1753, label: '영화보기' },
  { id: 1754, label: '클럽/나이트클럽 가기' },
  { id: 1755, label: '공연보기' },
  { id: 1756, label: '콘서트보기' },
  { id: 1757, label: '박물관가기' },
  { id: 1758, label: '공원가기' },
  { id: 1759, label: '캠핑하기' },
  { id: 1760, label: '해변가기' },
  { id: 1761, label: '스포츠 관람' },
  { id: 1768, label: '주거 개선' }
]

// Part 4: 취미
const hobbies = [
  { id: 1770, label: '아이에게 책 읽어주기' },
  { id: 1771, label: '음악 감상하기' },
  { id: 1772, label: '악기 연주하기' },
  { id: 1773, label: '혼자 노래부르거나 합창하기' },
  { id: 1776, label: '춤추기' },
  { id: 1777, label: '글쓰기(편지, 단문, 시 등)' },
  { id: 1778, label: '그림 그리기' },
  { id: 1781, label: '요리하기' },
  { id: 1783, label: '애완동물 기르기' }
]

// Part 4: 운동
const sports = [
  { id: 1784, label: '농구' },
  { id: 1785, label: '야구/소프트볼' },
  { id: 1786, label: '축구' },
  { id: 1787, label: '미식축구' },
  { id: 1790, label: '하키' },
  { id: 1791, label: '크리켓' },
  { id: 1792, label: '골프' },
  { id: 1793, label: '배구' },
  { id: 1794, label: '테니스' },
  { id: 1795, label: '배드민턴' },
  { id: 1796, label: '탁구' },
  { id: 1797, label: '수영' },
  { id: 1798, label: '자전거' },
  { id: 1801, label: '스키/스노우보드' },
  { id: 1803, label: '아이스 스케이트' },
  { id: 1806, label: '조깅' },
  { id: 1807, label: '걷기' },
  { id: 1809, label: '요가' },
  { id: 1810, label: '하이킹/트레킹' },
  { id: 1811, label: '낚시' },
  { id: 1813, label: '헬스' },
  { id: 1815, label: '운동을 전혀 하지 않음' }
]

// Part 4: 휴가/출장
const vacationTypes = [
  { id: 1816, label: '국내출장' },
  { id: 1817, label: '해외출장' },
  { id: 1818, label: '집에서 보내는 휴가' },
  { id: 1819, label: '국내 여행' },
  { id: 1820, label: '해외 여행' }
]

// Part 1 직업 선택 핸들러
const handleJobFieldChange = (optionId) => {
  // 모든 하위 질문 초기화
  showTeachingLocation.value = false
  showJobStatus_Business.value = false
  showJobStatus_Home.value = false
  showJobStatus_Teacher.value = false
  showWorkPeriod_Business.value = false
  showWorkPeriod_Home.value = false
  showWorkPeriod_Teacher.value = false
  showManagement_Business.value = false
  showManagement_Home.value = false
  showManagement_Teacher.value = false
  
  // 답변 초기화
  answers.value.teachingLocation = null
  answers.value.hasJob_Business = null
  answers.value.hasJob_Home = null
  answers.value.hasJob_Teacher = null
  answers.value.workPeriod_Business = null
  answers.value.workPeriod_Home = null
  answers.value.workPeriod_Teacher = null
  answers.value.isManagement_Business = null
  answers.value.isManagement_Home = null
  answers.value.isManagement_Teacher = null
  
  if (optionId === 1727) {
    // 사업/회사
    showJobStatus_Business.value = true
  } else if (optionId === 1728) {
    // 재택근무/재택사업
    showJobStatus_Home.value = true
  } else if (optionId === 1729) {
    // 교사/교육자
    showTeachingLocation.value = true
  }
}

// 교사 - 근무 장소 선택
const handleTeachingLocationChange = () => {
  showJobStatus_Teacher.value = true
}

// 사업/회사 - 직업 여부
const handleJobStatus_Business = (value) => {
  showWorkPeriod_Business.value = false
  showManagement_Business.value = false
  answers.value.workPeriod_Business = null
  answers.value.isManagement_Business = null
  
  if (value === 'yes') {
    showWorkPeriod_Business.value = true
  }
}

// 재택근무 - 직업 여부
const handleJobStatus_Home = (value) => {
  showWorkPeriod_Home.value = false
  showManagement_Home.value = false
  answers.value.workPeriod_Home = null
  answers.value.isManagement_Home = null
  
  if (value === 'yes') {
    showWorkPeriod_Home.value = true
  }
}

// 교사 - 직업 여부
const handleJobStatus_Teacher = (value) => {
  showWorkPeriod_Teacher.value = false
  showManagement_Teacher.value = false
  answers.value.workPeriod_Teacher = null
  answers.value.isManagement_Teacher = null
  
  if (value === 'yes') {
    showWorkPeriod_Teacher.value = true
  }
}

// 사업/회사 - 근무 기간
const handleWorkPeriod_Business = (optionId) => {
  showManagement_Business.value = false
  answers.value.isManagement_Business = null
  
  if (optionId === 1732 || optionId === 1733) {
    // 2개월 이상 또는 경험 많음
    showManagement_Business.value = true
  }
}

// 재택근무 - 근무 기간
const handleWorkPeriod_Home = (optionId) => {
  showManagement_Home.value = false
  answers.value.isManagement_Home = null
  
  if (optionId === 1837 || optionId === 1838) {
    showManagement_Home.value = true
  }
}

// 교사 - 근무 기간
const handleWorkPeriod_Teacher = (optionId) => {
  showManagement_Teacher.value = false
  answers.value.isManagement_Teacher = null
  
  if (optionId === 1749) {
    // 2개월 이상
    showManagement_Teacher.value = true
  }
}

// Part 2: 학생 선택 핸들러
const handleStudentChange = (optionId) => {
  showCurrentCourse.value = false
  showPastCourse.value = false
  answers.value.currentCourse = null
  answers.value.pastCourse = null
  
  if (optionId === 1971) {
    // 예 - 학생임
    showCurrentCourse.value = true
  } else if (optionId === 1972) {
    // 아니요 - 학생 아님
    showPastCourse.value = true
  }
}

// 다음 버튼 활성화 여부
const canGoNext = computed(() => {
  if (currentPart.value === 1) {
    if (!answers.value.jobField) return false
    
    // 사업/회사
    if (answers.value.jobField === 1727) {
      if (!showJobStatus_Business.value || !answers.value.hasJob_Business) return false
      if (showWorkPeriod_Business.value && !answers.value.workPeriod_Business) return false
      if (showManagement_Business.value && !answers.value.isManagement_Business) return false
    }
    
    // 재택근무
    if (answers.value.jobField === 1728) {
      if (!showJobStatus_Home.value || !answers.value.hasJob_Home) return false
      if (showWorkPeriod_Home.value && !answers.value.workPeriod_Home) return false
      if (showManagement_Home.value && !answers.value.isManagement_Home) return false
    }
    
    // 교사
    if (answers.value.jobField === 1729) {
      if (!answers.value.teachingLocation) return false
      if (!showJobStatus_Teacher.value || !answers.value.hasJob_Teacher) return false
      if (showWorkPeriod_Teacher.value && !answers.value.workPeriod_Teacher) return false
      if (showManagement_Teacher.value && !answers.value.isManagement_Teacher) return false
    }
    
    return true
  }
  
  if (currentPart.value === 2) {
    if (!answers.value.student) return false
    if (showCurrentCourse.value && !answers.value.currentCourse) return false
    if (showPastCourse.value && !answers.value.pastCourse) return false
    return true
  }
  
  if (currentPart.value === 3) {
    return answers.value.residence !== null && answers.value.residence !== undefined
  }
  
  if (currentPart.value === 4) {
    const totalSelected = selectedLeisureCount.value + selectedHobbyCount.value + selectedSportsCount.value + selectedVacationCount.value
    return totalSelected >= 12 && 
           selectedLeisureCount.value >= 2 && 
           selectedHobbyCount.value >= 1 && 
           selectedSportsCount.value >= 1 &&
           selectedVacationCount.value >= 1
  }
  
  return false
})

// 다음 파트로
const goNext = () => {
  if (!canGoNext.value) return
  
  if (currentPart.value < totalParts) {
    currentPart.value++
  } else {
    submitSurvey()
  }
}

// 이전 파트로
const goBack = () => {
  if (currentPart.value > 1) {
    currentPart.value--
  }
}

// 설문 제출
const submitSurvey = async () => {
  try {
    console.log('설문 답변:', answers.value)
    // 난이도 선택으로 이동
    router.push('/survey/level')
  } catch (error) {
    console.error('설문 제출 실패:', error)
  }
}
</script>

<template>
  <div class="survey-page">
    <header class="survey-header">
      <button class="info-btn">
        <span class="material-symbols-outlined">info</span>
      </button>
    </header>

    <main class="survey-main">
      <div class="survey-container">
        <!-- Step Progress -->
        <div class="step-progress">
          <div class="step active">
            <div class="step-content">
              <span class="step-number">1</span>
              <span class="step-label">Background Survey</span>
            </div>
          </div>
          <div class="step">
            <div class="step-content">
              <span class="step-number">2</span>
              <span class="step-label">Self Assessment</span>
            </div>
          </div>
          <div class="step">
            <div class="step-content">
              <span class="step-number">3</span>
              <span class="step-label">Setup</span>
            </div>
          </div>
          <div class="step">
            <div class="step-content">
              <span class="step-number">4</span>
              <span class="step-label">Sample Question</span>
            </div>
          </div>
          <div class="step">
            <div class="step-content">
              <span class="step-number">5</span>
              <span class="step-label">Begin Test</span>
            </div>
          </div>
        </div>

        <!-- Survey Content -->
        <section class="survey-content">
          <h1 class="survey-title">Background Survey</h1>
          <p class="survey-instructions">
            질문을 읽고 정확히 답변해 주시기 바랍니다. 설문에 대한 응답을 기초로 개인별 문항이 출제됩니다.
          </p>

          <h2 class="part-title">Part {{ currentPart }} of {{ totalParts }}</h2>

          <!-- Part 1: 직업 -->
          <div v-if="currentPart === 1" class="question-section">
            <!-- 질문 1: 직업 분야 -->
            <div class="question-block">
              <h3 class="question-title">현재 귀하는 어느 분야에 종사하고 계십니까?</h3>
              <div class="options-list">
                <label 
                  v-for="option in jobFieldOptions" 
                  :key="option.id"
                  class="option-label"
                >
                  <input 
                    type="radio" 
                    :value="option.id"
                    v-model="answers.jobField"
                    @change="handleJobFieldChange(option.id)"
                    class="option-input"
                  />
                  <span class="option-text">{{ option.label }}</span>
                </label>
              </div>
            </div>

            <!-- 교사 선택 시: 근무 장소 -->
            <transition name="fade-slide">
              <div v-if="showTeachingLocation" class="question-block">
                <h3 class="question-title">현재 귀하는 어디에서 학생을 가르치십니까?</h3>
                <div class="options-list">
                  <label 
                    v-for="option in teachingLocations" 
                    :key="option.id"
                    class="option-label"
                  >
                    <input 
                      type="radio" 
                      :value="option.id"
                      v-model="answers.teachingLocation"
                      @change="handleTeachingLocationChange"
                      class="option-input"
                    />
                    <span class="option-text">{{ option.label }}</span>
                  </label>
                </div>
              </div>
            </transition>

            <!-- 사업/회사: 직업 여부 -->
            <transition name="fade-slide">
              <div v-if="showJobStatus_Business" class="question-block">
                <h3 class="question-title">현재 귀하는 직업이 있으십니까?</h3>
                <div class="options-list">
                  <label 
                    v-for="option in yesNoOptions" 
                    :key="option.value"
                    class="option-label"
                  >
                    <input 
                      type="radio" 
                      :value="option.value"
                      v-model="answers.hasJob_Business"
                      @change="handleJobStatus_Business(option.value)"
                      class="option-input"
                    />
                    <span class="option-text">{{ option.label }}</span>
                  </label>
                </div>
              </div>
            </transition>

            <!-- 사업/회사: 근무 기간 -->
            <transition name="fade-slide">
              <div v-if="showWorkPeriod_Business" class="question-block">
                <h3 class="question-title">귀하의 근무 기간은 얼마나 되십니까?</h3>
                <div class="options-list">
                  <label 
                    v-for="option in workPeriods_Business" 
                    :key="option.id"
                    class="option-label"
                  >
                    <input 
                      type="radio" 
                      :value="option.id"
                      v-model="answers.workPeriod_Business"
                      @change="handleWorkPeriod_Business(option.id)"
                      class="option-input"
                    />
                    <span class="option-text">{{ option.label }}</span>
                  </label>
                </div>
              </div>
            </transition>

            <!-- 사업/회사: 관리직 여부 -->
            <transition name="fade-slide">
              <div v-if="showManagement_Business" class="question-block">
                <h3 class="question-title">귀하는 부하직원을 관리하는 관리직을 맡고 있습니까?</h3>
                <div class="options-list">
                  <label 
                    v-for="option in yesNoOptions" 
                    :key="'mgmt-bus-' + option.value"
                    class="option-label"
                  >
                    <input 
                      type="radio" 
                      :value="option.value"
                      v-model="answers.isManagement_Business"
                      class="option-input"
                    />
                    <span class="option-text">{{ option.label }}</span>
                  </label>
                </div>
              </div>
            </transition>

            <!-- 재택근무: 직업 여부 -->
            <transition name="fade-slide">
              <div v-if="showJobStatus_Home" class="question-block">
                <h3 class="question-title">현재 귀하는 직업이 있으십니까?</h3>
                <div class="options-list">
                  <label 
                    v-for="option in yesNoOptions" 
                    :key="'job-home-' + option.value"
                    class="option-label"
                  >
                    <input 
                      type="radio" 
                      :value="option.value"
                      v-model="answers.hasJob_Home"
                      @change="handleJobStatus_Home(option.value)"
                      class="option-input"
                    />
                    <span class="option-text">{{ option.label }}</span>
                  </label>
                </div>
              </div>
            </transition>

            <!-- 재택근무: 근무 기간 -->
            <transition name="fade-slide">
              <div v-if="showWorkPeriod_Home" class="question-block">
                <h3 class="question-title">귀하의 근무 기간은 얼마나 되십니까?</h3>
                <div class="options-list">
                  <label 
                    v-for="option in workPeriods_Home" 
                    :key="option.id"
                    class="option-label"
                  >
                    <input 
                      type="radio" 
                      :value="option.id"
                      v-model="answers.workPeriod_Home"
                      @change="handleWorkPeriod_Home(option.id)"
                      class="option-input"
                    />
                    <span class="option-text">{{ option.label }}</span>
                  </label>
                </div>
              </div>
            </transition>

            <!-- 재택근무: 관리직 여부 -->
            <transition name="fade-slide">
              <div v-if="showManagement_Home" class="question-block">
                <h3 class="question-title">귀하는 부하직원을 관리하는 관리직을 맡고 있습니까?</h3>
                <div class="options-list">
                  <label 
                    v-for="option in yesNoOptions" 
                    :key="'mgmt-home-' + option.value"
                    class="option-label"
                  >
                    <input 
                      type="radio" 
                      :value="option.value"
                      v-model="answers.isManagement_Home"
                      class="option-input"
                    />
                    <span class="option-text">{{ option.label }}</span>
                  </label>
                </div>
              </div>
            </transition>

            <!-- 교사: 직업 여부 -->
            <transition name="fade-slide">
              <div v-if="showJobStatus_Teacher" class="question-block">
                <h3 class="question-title">현재 귀하는 직업이 있으십니까?</h3>
                <div class="options-list">
                  <label 
                    v-for="option in yesNoOptions" 
                    :key="'job-teacher-' + option.value"
                    class="option-label"
                  >
                    <input 
                      type="radio" 
                      :value="option.value"
                      v-model="answers.hasJob_Teacher"
                      @change="handleJobStatus_Teacher(option.value)"
                      class="option-input"
                    />
                    <span class="option-text">{{ option.label }}</span>
                  </label>
                </div>
              </div>
            </transition>

            <!-- 교사: 근무 기간 -->
            <transition name="fade-slide">
              <div v-if="showWorkPeriod_Teacher" class="question-block">
                <h3 class="question-title">귀하의 근무 기간은 얼마나 되십니까?</h3>
                <div class="options-list">
                  <label 
                    v-for="option in workPeriods_Teacher" 
                    :key="option.id"
                    class="option-label"
                  >
                    <input 
                      type="radio" 
                      :value="option.id"
                      v-model="answers.workPeriod_Teacher"
                      @change="handleWorkPeriod_Teacher(option.id)"
                      class="option-input"
                    />
                    <span class="option-text">{{ option.label }}</span>
                  </label>
                </div>
              </div>
            </transition>

            <!-- 교사: 관리직 여부 -->
            <transition name="fade-slide">
              <div v-if="showManagement_Teacher" class="question-block">
                <h3 class="question-title">귀하는 부하직원을 관리하는 관리직을 맡고 있습니까?</h3>
                <div class="options-list">
                  <label 
                    v-for="option in yesNoOptions" 
                    :key="'mgmt-teacher-' + option.value"
                    class="option-label"
                  >
                    <input 
                      type="radio" 
                      :value="option.value"
                      v-model="answers.isManagement_Teacher"
                      class="option-input"
                    />
                    <span class="option-text">{{ option.label }}</span>
                  </label>
                </div>
              </div>
            </transition>
          </div>

          <!-- Part 2: 학생/강의 -->
          <div v-if="currentPart === 2" class="question-section">
            <div class="question-block">
              <h3 class="question-title">현재 당신은 학생입니까?</h3>
              <div class="options-list">
                <label 
                  v-for="option in studentOptions" 
                  :key="option.id"
                  class="option-label"
                >
                  <input 
                    type="radio" 
                    :value="option.id"
                    v-model="answers.student"
                    @change="handleStudentChange(option.id)"
                    class="option-input"
                  />
                  <span class="option-text">{{ option.label }}</span>
                </label>
              </div>
            </div>

            <!-- 학생이면: 현재 강의 -->
            <transition name="fade-slide">
              <div v-if="showCurrentCourse" class="question-block">
                <h3 class="question-title">현재 어떤 강의를 듣고 있습니까?</h3>
                <div class="options-list">
                  <label 
                    v-for="option in currentCourseOptions" 
                    :key="option.id"
                    class="option-label"
                  >
                    <input 
                      type="radio" 
                      :value="option.id"
                      v-model="answers.currentCourse"
                      class="option-input"
                    />
                    <span class="option-text">{{ option.label }}</span>
                  </label>
                </div>
              </div>
            </transition>

            <!-- 학생 아니면: 최근 강의 -->
            <transition name="fade-slide">
              <div v-if="showPastCourse" class="question-block">
                <h3 class="question-title">최근 어떤 강의를 수강했습니까?</h3>
                <div class="options-list">
                  <label 
                    v-for="option in pastCourseOptions" 
                    :key="option.id"
                    class="option-label"
                  >
                    <input 
                      type="radio" 
                      :value="option.id"
                      v-model="answers.pastCourse"
                      class="option-input"
                    />
                    <span class="option-text">{{ option.label }}</span>
                  </label>
                </div>
              </div>
            </transition>
          </div>

          <!-- Part 3: 거주지 -->
          <div v-if="currentPart === 3" class="question-section">
            <div class="question-block">
              <h3 class="question-title">현재 귀하는 어디에 살고 계십니까?</h3>
              <div class="options-list">
                <label 
                  v-for="option in residenceOptions" 
                  :key="'part3-' + option.id"
                  class="option-label"
                >
                  <input 
                    type="radio" 
                    :value="option.id"
                    v-model="answers.residence"
                    class="option-input"
                  />
                  <span class="option-text">{{ option.label }}</span>
                </label>
              </div>
            </div>
          </div>

          <!-- Part 4: 여가/취미/운동/휴가 -->
          <div v-if="currentPart === 4" class="question-section">
            <p class="selection-instruction">
              아래의 설문에서 총 <span class="highlight">12개</span> 이상의 항목을 선택하십시오.
              <span class="selection-count" :class="{ 'count-valid': (selectedLeisureCount + selectedHobbyCount + selectedSportsCount + selectedVacationCount) >= 12 }">
                <span class="highlight">{{ selectedLeisureCount + selectedHobbyCount + selectedSportsCount + selectedVacationCount }}개</span> 항목을 선택했습니다.
              </span>
            </p>

            <!-- 여가 활동 -->
            <div class="question-block">
              <h3 class="question-title">귀하는 여가 활동으로 주로 무엇을 하십니까? (두 개 이상 선택)</h3>
              <p v-if="selectedLeisureCount < 2" class="error-msg">여가 활동에서 2개 이상을 고르세요.</p>
              <div class="options-list">
                <label 
                  v-for="activity in leisureActivities" 
                  :key="activity.id"
                  class="option-label"
                >
                  <input 
                    type="checkbox" 
                    :value="activity.id"
                    v-model="answers.leisure"
                    class="option-input"
                  />
                  <span class="option-text">{{ activity.label }}</span>
                </label>
              </div>
            </div>

            <!-- 취미 -->
            <div class="question-block">
              <h3 class="question-title">귀하의 취미나 관심사는 무엇입니까? (한 개 이상 선택)</h3>
              <p v-if="selectedHobbyCount < 1" class="error-msg">취미나 관심사 중 1개 이상을 고르세요.</p>
              <div class="options-list">
                <label 
                  v-for="hobby in hobbies" 
                  :key="hobby.id"
                  class="option-label"
                >
                  <input 
                    type="checkbox" 
                    :value="hobby.id"
                    v-model="answers.hobbies"
                    class="option-input"
                  />
                  <span class="option-text">{{ hobby.label }}</span>
                </label>
              </div>
            </div>

            <!-- 운동 -->
            <div class="question-block">
              <h3 class="question-title">귀하는 주로 어떤 운동을 즐기십니까? (한 개 이상 선택)</h3>
              <p v-if="selectedSportsCount < 1" class="error-msg">운동 중 1개 이상을 고르세요.</p>
              <div class="options-list">
                <label 
                  v-for="sport in sports" 
                  :key="sport.id"
                  class="option-label"
                >
                  <input 
                    type="checkbox" 
                    :value="sport.id"
                    v-model="answers.sports"
                    class="option-input"
                  />
                  <span class="option-text">{{ sport.label }}</span>
                </label>
              </div>
            </div>

            <!-- 휴가/출장 -->
            <div class="question-block">
              <h3 class="question-title">귀하는 어떤 휴가나 출장을 다녀온 경험이 있습니까? (한 개 이상 선택)</h3>
              <p v-if="selectedVacationCount < 1" class="error-msg">휴가나 출장 중 1개 이상을 고르세요.</p>
              <div class="options-list">
                <label 
                  v-for="vacation in vacationTypes" 
                  :key="vacation.id"
                  class="option-label"
                >
                  <input 
                    type="checkbox" 
                    :value="vacation.id"
                    v-model="answers.vacation"
                    class="option-input"
                  />
                  <span class="option-text">{{ vacation.label }}</span>
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
            :disabled="currentPart === 1"
          >
            <span class="material-symbols-outlined">chevron_left</span>
            Back
          </button>
          <button 
            @click="goNext" 
            class="nav-btn next-btn"
            :disabled="!canGoNext"
          >
            Next
            <span class="material-symbols-outlined">chevron_right</span>
          </button>
        </footer>
      </div>
    </main>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;400;500;700&display=swap');
@import url('https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:wght@100..700&display=swap');

.survey-page {
  min-height: 100vh;
  background: #FFFFFF;
  font-family: 'Noto Sans KR', sans-serif;
  display: flex;
  flex-direction: column;
}

.dark-mode .survey-page {
  background: #121212;
  color: #e2e8f0;
}

.survey-header {
  display: flex;
  justify-content: flex-end;
  padding: 16px;
}

.info-btn {
  background: none;
  border: none;
  color: #94a3b8;
  cursor: pointer;
  transition: color 0.2s;
}

.info-btn:hover {
  color: #FFD700;
}

.material-symbols-outlined {
  font-family: 'Material Symbols Outlined';
  font-size: 24px;
}

.survey-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 16px;
}

.survey-container {
  width: 100%;
  max-width: 1280px;
}

/* Step Progress */
.step-progress {
  display: flex;
  border: 1px solid #e2e8f0;
  height: 60px;
  background: white;
  overflow: hidden;
  margin-bottom: 48px;
}

.dark-mode .step-progress {
  background: #1E1E1E;
  border-color: #374155;
}

.step {
  position: relative;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 0 24px 0 40px;
  background: #f8f9fa;
  color: #94a3b8;
  z-index: 1;
}

.dark-mode .step {
  background: #1E1E1E;
}

.step::after {
  content: '';
  position: absolute;
  top: 0;
  right: -20px;
  width: 0;
  height: 0;
  border-top: 30px solid transparent;
  border-bottom: 30px solid transparent;
  border-left: 20px solid #f8f9fa;
  z-index: 2;
}

.dark-mode .step::after {
  border-left-color: #1E1E1E;
}

.step.active {
  background: #FFD700;
  color: #1e293b;
  z-index: 10;
}

.step.active::after {
  border-left-color: #FFD700;
}

.step:first-child {
  padding-left: 24px;
}

.step-content {
  display: flex;
  flex-direction: column;
}

.step-number {
  font-size: 14px;
  font-weight: 700;
}

.step-label {
  font-size: 12px;
}

/* Survey Content */
.survey-content {
  margin-bottom: 64px;
}

.survey-title {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 24px;
  color: #1e293b;
}

.dark-mode .survey-title {
  color: white;
}

.survey-instructions {
  font-size: 18px;
  margin-bottom: 32px;
  color: #64748b;
  line-height: 1.6;
}

.dark-mode .survey-instructions {
  color: #94a3b8;
}

.part-title {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 24px;
}

.question-section {
  display: flex;
  flex-direction: column;
  gap: 40px;
}

.question-block {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.question-title {
  font-size: 18px;
  font-weight: 500;
  color: #1e293b;
}

.dark-mode .question-title {
  color: #f1f5f9;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.option-label {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 8px;
  border-radius: 4px;
  transition: all 0.2s;
}

.option-label:hover {
  background: #f8fafc;
}

.dark-mode .option-label:hover {
  background: #1e293b;
}

.option-label:hover .option-text {
  color: #FFD700;
}

.option-input {
  width: 20px;
  height: 20px;
  accent-color: #FFD700;
  cursor: pointer;
}

.option-text {
  font-size: 15px;
  transition: color 0.2s;
}

/* Selection Count */
.selection-instruction {
  font-size: 15px;
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.selection-count {
  color: #ef4444;
  font-weight: 500;
}

.selection-count.count-valid {
  color: #10b981;
}

.highlight {
  color: #ff6633;
  font-weight: 600;
}

.error-msg {
  color: #ef4444;
  font-size: 14px;
  margin-top: -8px;
}

.info-text {
  color: #64748b;
  font-size: 16px;
}

/* Navigation */
.survey-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 64px;
  margin-bottom: 80px;
}

.nav-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 40px;
  background: #FFD700;
  border: none;
  font-weight: 700;
  color: #1e293b;
  cursor: pointer;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  transition: all 0.2s;
  font-size: 15px;
}

.nav-btn:hover:not(:disabled) {
  background: #E6C200;
  transform: scale(0.98);
}

.nav-btn:active:not(:disabled) {
  transform: scale(0.95);
}

.nav-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
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
</style>