<script setup>
import { ref, onMounted, inject } from "vue";
import { useRouter } from "vue-router";
import SurveySelectModal from "@/components/common/SurveySelectModal.vue";
import { surveysApi, examApi } from "@/api";
import { useSurveyStore } from "@/stores/survey";

const router = useRouter();
const surveyStore = useSurveyStore();
const isDarkMode = inject("isDarkMode", ref(false));

const showSurveySelectModal = ref(false);
const showResumeModal = ref(false);
const existingSurveys = ref([]);
const incompleteExam = ref(null);

const fetchExistingSurveys = async () => {
  try {
    const response = await surveysApi.getSurveyList();

    const surveyList =
      response.data?.surveySummaryResponses ||
      (Array.isArray(response.data) ? response.data : []);
    // 백엔드 데이터 중 최근 3개만 선택하여 표시
    existingSurveys.value = surveyList.slice(0, 3).map(s => ({
      ...s,
      occupation: s.occupation || 'N/A',
      topics: s.topicList || []
    }));
  } catch (error) {
    console.error("설문 목록 로드 실패", error);
    existingSurveys.value = [];
  }
};

const openSurveyModal = () => {
  const savedExam = localStorage.getItem("incompleteExam");
  if (savedExam) {
    incompleteExam.value = JSON.parse(savedExam);
    showResumeModal.value = true;
  } else {
    showSurveySelectModal.value = true;
  }
};

const startNewExam = () => {
  showResumeModal.value = false;
  showSurveySelectModal.value = true;
};

const resumeExam = () => {
  if (incompleteExam.value) {
    router.push({
      path: "/exam/question",
      query: {
        examId: incompleteExam.value.examId,
        resume: "true",
      },
    });
  }
};

const discardExam = () => {
  localStorage.removeItem("incompleteExam");
  incompleteExam.value = null;
  showResumeModal.value = false;
  showSurveySelectModal.value = true;
};

const startNewSurvey = () => {
  router.push({ path: "/survey", query: { from: "exam" } });
};

const useSelectedSurvey = (surveyId) => {
  router.push({
    path: "/exam/setup",
    query: { surveyId: surveyId },
  });
};

const useRecommendedSurvey = (surveyData) => {
  console.log('[ExamView] Using recommended survey:', surveyData);
  // Store에 추천 설문 데이터 저장
  surveyStore.setSurveyData(surveyData);
  // 레벨 선택 페이지로 이동 (시험 모드)
  router.push({ 
    path: '/survey/level', 
    query: { from: 'exam' } 
  });
};

// Deletion handler removed

onMounted(async () => {
  await fetchExistingSurveys();
});
</script>

<template>
  <div class="page-container">
    <main class="page-content">
      <div class="hero-section fade-in">
        <h1 class="page-title">실전 모의고사</h1>
        <p class="subtitle">
          실제 OPIc 시험과 동일한 환경에서<br>
          <span class="highlight">난이도별 모의고사</span>를 풀어보세요.
        </p>

        <div class="features-grid">
          <div class="feature-card delay-1">
            <div class="feature-icon">
              <span class="material-icons">edit_document</span>
            </div>
            <h3>실전 시뮬레이션</h3>
            <p><span class="highlight">실제 시험</span>과 동일한 <span class="highlight">문제 세트</span> 구성</p>
          </div>
          <!-- <div class="feature-card delay-2">
            <div class="feature-icon">
              <span class="material-icons">track_changes</span>
            </div>
            <h3>난이도 자동 조정</h3>
            <p>7번 문제 후 난이도 재설정</p>
          </div> -->
          <div class="feature-card delay-3">
            <div class="feature-icon">
              <span class="material-icons">auto_awesome</span>
            </div>
            <h3>AI 분석</h3>
            <p><span class="highlight">문법, 어휘, 유창성</span> 등 종합 평가</p>
          </div>
          <div class="feature-card delay-4">
            <div class="feature-icon">
              <span class="material-icons">insights</span>
            </div>
            <h3>상세 피드백</h3>
            <p>문항별 <span class="highlight">강점</span>과 <span class="highlight">약점</span> 분석</p>
          </div>
        </div>

        <button @click="openSurveyModal" class="start-exam-btn delay-5">
          시험 시작하기
        </button>
      </div>
    </main>

    <div v-if="showResumeModal" class="modal-overlay fade-in">
      <div class="modal-card resume-modal">
        <div class="modal-header">
          <h3>진행 중인 시험이 있습니다</h3>
          <p class="subtitle-sm">이전에 진행하던 모의고사를 계속하시겠습니까?</p>
        </div>

        <div class="resume-info" v-if="incompleteExam">
          <div class="info-row">
            <span class="label">문항 진행도</span>
            <span class="value">{{ incompleteExam.currentQuestion }} 번</span>
          </div>
          <div class="info-row">
            <span class="label">남은 시간</span>
            <span class="value">{{ incompleteExam.remainingTime || "정보 없음" }}</span>
          </div>
        </div>

        <div class="modal-footer">
          <button @click="resumeExam" class="primary-btn">이어서 하기</button>
          <button @click="startNewExam" class="secondary-btn">새로 시작</button>
          <button @click="discardExam" class="cancel-btn">기록 삭제</button>
        </div>
      </div>
    </div>

    <SurveySelectModal
      :isVisible="showSurveySelectModal"
      :existingSurveys="existingSurveys"
      @start-new="startNewSurvey"
      @use-selected="useSelectedSurvey"
      @use-recommended="useRecommendedSurvey"
      @close="showSurveySelectModal = false"
    />
  </div>
</template>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;400;500;700;900&display=swap");

.page-container {
  height: calc(100vh - var(--header-height, 60px));
  min-height: 0 !important;
  padding: 0 !important;
  max-height: 100vh;
  overflow: hidden;
  background: var(--bg-color);
  display: flex;
  align-items: center;
  justify-content: center;
  padding-bottom: 12vh;
}

.page-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  width: 100%;
}

/* Animation */
.fade-in {
  opacity: 0;
  animation: firstName 0.6s cubic-bezier(0.2, 0.8, 0.2, 1) forwards;
}

.delay-1 { animation-delay: 0.1s; }
.delay-2 { animation-delay: 0.2s; }
.delay-3 { animation-delay: 0.3s; }
.delay-4 { animation-delay: 0.4s; }
.delay-5 { animation-delay: 0.5s; }

@keyframes firstName {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.hero-section {
  text-align: center;
  margin-top: -6vh; 
  display: flex;
  flex-direction: column;
  align-items: center;
}

.page-title {
  font-size: 2.6rem;
  font-weight: 800;
  color: var(--text-main);
  margin-bottom: 24px;
}

.subtitle {
  font-size: 1rem;
  color: var(--text-sub);
  margin-bottom: 50px;
  line-height: 1.6;
}

.highlight {
  color: #F9A825;
  font-weight: 700;
  position: relative;
  display: inline-block;
  z-index: 1;
}

.highlight::after {
  content: "";
  position: absolute;
  bottom: 2px;
  left: 0;
  width: 100%;
  height: 8px;
  background: var(--honey-200);
  z-index: -1;
  opacity: 0.8;
}

.start-exam-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 20px 64px;
  background: var(--primary-color);
  color: #3E2723;
  border: none;
  border-radius: var(--radius-full);
  font-size: 1.3rem;
  font-weight: 800;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 10px 25px rgba(255, 215, 0, 0.3);
  margin-top: 60px;
  opacity: 0;
  animation: firstName 0.6s cubic-bezier(0.2, 0.8, 0.2, 1) forwards;
}

.start-exam-btn:hover {
  transform: translateY(-4px);
  background: var(--primary-hover);
  box-shadow: 0 15px 35px rgba(255, 215, 0, 0.4);
}

.features-grid {
  display: flex;
  justify-content: center;
  align-items: stretch;
  gap: 20px;
  flex-wrap: nowrap;
  width: 100%;
  max-width: 1100px;
}

.feature-card {
  flex: 1;
  min-width: 0;
  background: #FFF;
  border: 1px solid #F1F5F9;
  border-radius: var(--radius-lg);
  padding: 28px 16px;
  text-align: center;
  transition: all 0.3s ease;
  box-shadow: 0 4px 6px rgba(0,0,0,0.02);
  opacity: 0;
  animation: firstName 0.6s cubic-bezier(0.2, 0.8, 0.2, 1) forwards;
}

.feature-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(0,0,0,0.08);
  border-color: var(--honey-200);
}

.feature-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 20px;
  background: var(--honey-50);
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #FBC02D;
}

.feature-icon .material-icons {
  font-size: 32px;
}

.feature-card h3 {
  font-size: 1.2rem;
  font-weight: 700;
  margin-bottom: 10px;
  color: var(--text-main);
}

.feature-card p {
  font-size: 0.95rem;
  color: var(--text-sub);
  line-height: 1.5;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(8px);
}

.modal-card {
  background: #FFF;
  border-radius: var(--radius-lg);
  max-width: 460px;
  width: 90%;
  box-shadow: 0 25px 50px rgba(0,0,0,0.2);
  padding: 40px;
  text-align: center;
}

.modal-header h3 {
  font-size: 1.5rem;
  font-weight: 800;
  margin-bottom: 12px;
  color: var(--text-main);
}

.subtitle-sm {
  font-size: 1rem;
  color: var(--text-sub);
  margin-bottom: 30px;
}

.resume-info {
  background: var(--honey-50);
  border-radius: var(--radius-md);
  padding: 24px;
  margin-bottom: 30px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px dashed rgba(0,0,0,0.1);
}

.info-row:last-child {
  border-bottom: none;
}

.label {
  font-weight: 600;
  color: var(--text-sub);
}

.value {
  font-weight: 700;
  color: var(--text-main);
}

.modal-footer {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

button {
  padding: 16px;
  border-radius: var(--radius-full);
  border: none;
  font-weight: 700;
  cursor: pointer;
  font-size: 1rem;
  transition: all 0.2s;
}

.primary-btn {
  background: var(--honey-300);
  color: #3E2723;
}
.primary-btn:hover {
  background: var(--honey-400);
  transform: translateY(-2px);
}

.secondary-btn {
  background: #F1F5F9;
  color: var(--text-sub);
}
.secondary-btn:hover {
  background: #E2E8F0;
}

.cancel-btn {
  background: transparent;
  color: #EF5350;
  border: 1px solid #FFE5E5;
}
.cancel-btn:hover {
  background: #FFEBEE;
}
</style>