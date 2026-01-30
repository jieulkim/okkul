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
    const filteredList = surveyStore.filterSurveys(surveyList);

    existingSurveys.value = filteredList.map(s => ({
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

const handleDeleteSurvey = (surveyId) => {
  surveyStore.deleteSurvey(surveyId);
  existingSurveys.value = surveyStore.filterSurveys(existingSurveys.value);
};

onMounted(async () => {
  await fetchExistingSurveys();
});
</script>

<template>
  <div class="page-container">
    <main class="page-content">
      <div class="hero-section">
        <h1 class="page-title">실전 모의고사</h1>
        <p class="subtitle">
          실제 OPIc 시험과 동일한 환경에서 15개 문항을 풀어보세요
        </p>

        <button @click="openSurveyModal" class="start-exam-btn">
          시험 시작하기
        </button>

        <div class="features-grid">
          <div class="feature-card">
            <div class="feature-icon">
              <span class="material-icons">edit_document</span>
            </div>
            <h3>실전 시뮬레이션</h3>
            <p>실제 시험과 동일한 15문항 구성</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <span class="material-icons">track_changes</span>
            </div>
            <h3>난이도 자동 조정</h3>
            <p>7번 문제 후 난이도 재설정</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <span class="material-icons">auto_awesome</span>
            </div>
            <h3>AI 분석</h3>
            <p>문법, 어휘, 유창성 등 종합 평가</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <span class="material-icons">insights</span>
            </div>
            <h3>상세 피드백</h3>
            <p>문항별 강점과 약점 분석</p>
          </div>
        </div>
      </div>
    </main>

    <div v-if="showResumeModal" class="modal-overlay">
      <div
        class="modal-card resume-modal"
        :class="{ 'dark-mode-card': isDarkMode }"
      >
        <div class="modal-header">
          <h3>진행 중인 시험이 있습니다</h3>
          <p class="subtitle">이전에 진행하던 모의고사를 계속하시겠습니까?</p>
        </div>

        <div class="resume-info" v-if="incompleteExam">
          <div class="info-row">
            <span class="label">문항 진행도:</span>
            <span class="value">{{ incompleteExam.currentQuestion }} / 15</span>
          </div>
          <div class="info-row">
            <span class="label">남은 시간:</span>
            <span class="value">{{
              incompleteExam.remainingTime || "정보 없음"
            }}</span>
          </div>
        </div>

        <div class="modal-footer">
          <button @click="resumeExam" class="primary-btn">이어서 하기</button>
          <button
            @click="startNewExam"
            class="secondary-btn"
            :class="{ 'dark-mode-btn': isDarkMode }"
          >
            새로 시작
          </button>
          <button
            @click="discardExam"
            class="cancel-btn"
          >
            진행 중 시험 삭제
          </button>
        </div>
      </div>
    </div>

    <SurveySelectModal
      :isVisible="showSurveySelectModal"
      :existingSurveys="existingSurveys"
      @start-new="startNewSurvey"
      @use-selected="useSelectedSurvey"
      @delete-survey="handleDeleteSurvey"
      @close="showSurveySelectModal = false"
    />
  </div>
</template>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;400;500;700;900&display=swap");

.page-container {
  min-height: 100vh;
  background: var(--bg-primary);
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

.hero-section {
  text-align: center;
}

.page-title {
  font-size: var(--font-size-xl);
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 32px;
}

.subtitle {
  font-size: 1.125rem;
  color: var(--text-secondary);
  margin-bottom: 40px;
}

.start-exam-btn {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  padding: 16px 40px;
  background: var(--primary-color);
  color: #212529;
  border: none;
  border-radius: var(--border-radius);
  font-size: 1.25rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: var(--shadow-md);
  margin-bottom: 60px;
}

.start-exam-btn:hover {
  background: var(--primary-hover);
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.start-exam-btn .btn-icon {
  font-size: 24px;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 24px;
  margin-top: 40px;
}

.feature-card {
  background: var(--bg-secondary);
  border: var(--border-primary);
  border-radius: 24px;
  padding: 32px 24px;
  text-align: center;
  transition: all 0.3s ease;
  box-shadow: var(--shadow-sm);
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
  border-color: var(--primary-color);
}

.feature-icon {
  margin-bottom: 16px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.feature-icon .material-icons {
  font-size: 48px;
  color: var(--primary-color);
}

.feature-card h3 {
  font-size: 20px;
  font-weight: 900;
  margin-bottom: 8px;
  color: var(--text-primary);
}

.feature-card p {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
}

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
  border-radius: var(--border-radius);
  max-width: 500px;
  width: 90%;
  border: var(--border-primary);
  box-shadow: var(--shadow-lg);
}

.resume-modal .modal-header {
  padding: 32px 32px 24px;
  text-align: center;
}

.modal-header h3 {
  font-size: 24px;
  font-weight: 800;
  margin-bottom: 8px;
  color: var(--text-primary);
}

.resume-info {
  padding: 24px 32px;
  background: var(--bg-tertiary);
  margin: 0 32px 24px;
  border-radius: var(--border-radius);
  border: var(--border-thin);
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #e2e8f0;
}

.info-row:last-child {
  border-bottom: none;
}

.label {
  font-weight: 600;
  color: #64748b;
}

.value {
  font-weight: 700;
  color: var(--text-primary);
}

.modal-footer {
  padding: 0 32px 32px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

button {
  padding: 16px;
  border-radius: 12px;
  border: none;
  font-weight: 700;
  cursor: pointer;
  font-size: 15px;
  transition: all 0.2s;
}

.primary-btn {
  background: var(--primary-color);
  color: #212529;
  border: none;
  font-weight: 700;
}

.primary-btn:hover {
  transform: translate(-0.02em, -0.02em);
  box-shadow: var(--shadow-sm);
}

.secondary-btn {
  background: #f1f5f9;
  color: #64748b;
}

.secondary-btn:hover {
  background: #e2e8f0;
}

.cancel-btn {
  background: white;
  border: 1.5px solid #e2e8f0;
  color: #ef4444;
}

.cancel-btn:hover {
  background: #fef2f2;
  border-color: #ef4444;
}

.dark-mode {
  background: var(--bg-primary);
  color: #f1f5f9;
}

.dark-mode .feature-card {
  background: var(--bg-secondary);
  border-color: #FFFFFF;
}

.dark-mode-card {
  background: var(--bg-secondary);
  color: var(--text-primary);
  border-color: #FFFFFF;
}

.dark-mode-card .resume-info {
  background: var(--bg-tertiary);
}
</style>