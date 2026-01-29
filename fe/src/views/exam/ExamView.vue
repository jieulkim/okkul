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

// 진행 중인 시험 확인
const checkIncompleteExam = () => {
  const savedExam = localStorage.getItem("incompleteExam");
  if (savedExam) {
    incompleteExam.value = JSON.parse(savedExam);
    showResumeModal.value = true;
  } else {
    showSurveySelectModal.value = true;
  }
};

// 실제 API를 사용하여 설문 목록 로드
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
      topics: s.topicList || [] // Backend might use topicList for IDs or topics for names/objects
    }));
  } catch (error) {
    console.error("설문 목록 로드 실패", error);
    existingSurveys.value = [];
  }
};

const startNewExam = () => {
  showResumeModal.value = false;
  showSurveySelectModal.value = true;
};

const openSurveyModal = () => {
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
  checkIncompleteExam();
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

        <!-- 시작하기 버튼 -->
        <button @click="openSurveyModal" class="start-exam-btn">
          <span class="btn-icon">🚀</span>
          시험 시작하기
        </button>

        <div class="features-grid">
          <div class="feature-card" @click="openSurveyModal">
            <div class="feature-icon">📝</div>
            <h3>실전 시뮬레이션</h3>
            <p>실제 시험과 동일한 15문항 구성</p>
          </div>
          <div class="feature-card" @click="openSurveyModal">
            <div class="feature-icon">🎯</div>
            <h3>난이도 자동 조정</h3>
            <p>7번 문제 후 난이도 재설정</p>
          </div>
          <div class="feature-card" @click="openSurveyModal">
            <div class="feature-icon">🤖</div>
            <h3>AI 분석</h3>
            <p>문법, 어휘, 유창성 등 종합 평가</p>
          </div>
          <div class="feature-card" @click="openSurveyModal">
            <div class="feature-icon">📊</div>
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
  font-size: 2.5rem;
  font-weight: 900;
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
  padding: 20px 48px;
  background: linear-gradient(135deg, #ffd700 0%, #ffa500 100%);
  color: #1e293b;
  border: none;
  border-radius: 16px;
  font-size: 20px;
  font-weight: 900;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 8px 20px rgba(255, 215, 0, 0.3);
  margin-bottom: 60px;
}

.start-exam-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 30px rgba(255, 215, 0, 0.4);
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
  border: 2px solid var(--border-primary);
  border-radius: 24px;
  padding: 32px 24px;
  text-align: center;
  transition: all 0.3s ease;
  cursor: pointer;
}

.feature-card:hover {
  transform: translateY(-4px);
  border-color: var(--primary-color);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.feature-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.feature-card h3 {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 8px;
  color: #1e293b;
}

.feature-card p {
  font-size: 14px;
  color: #64748b;
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
  background: white;
  border-radius: 24px;
  max-width: 500px;
  width: 90%;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.resume-modal .modal-header {
  padding: 32px 32px 24px;
  text-align: center;
}

.modal-header h3 {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 8px;
  color: #1e293b;
}

.resume-info {
  padding: 24px 32px;
  background: #f8fafc;
  margin: 0 32px 24px;
  border-radius: 12px;
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
  color: #1e293b;
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
  background: #ffd700;
  color: #1e293b;
}

.primary-btn:hover {
  background: #ffc800;
  transform: translateY(-2px);
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
  background: #0f172a;
  color: #f1f5f9;
}

.dark-mode .feature-card {
  background: #1e293b;
  border-color: #334155;
}

.dark-mode .feature-card h3 {
  color: #f1f5f9;
}

.dark-mode .feature-card p {
  color: #94a3b8;
}

.dark-mode .start-exam-btn {
  color: #0f172a;
}

.dark-mode-card {
  background: #1e293b;
  color: #f1f5f9;
}

.dark-mode-card .modal-header h3 {
  color: #f1f5f9;
}

.dark-mode-card .resume-info {
  background: #0f172a;
}

.dark-mode-card .value {
  color: #f1f5f9;
}

.dark-mode-btn {
  background: #334155;
  color: #f1f5f9;
}
</style>
