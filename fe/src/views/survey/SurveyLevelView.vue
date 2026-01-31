<script setup>
import { ref, inject, onBeforeUnmount } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useSurveyStore } from "@/stores/survey";
import { surveysApi } from '@/api'

const router = useRouter();
const route = useRoute();
const isDarkMode = inject("isDarkMode", ref(false));

const currentStep = ref(2);
const selectedLevel = ref(null);

// 현재 재생 중인 오디오를 추적
const currentAudio = ref(null);
const isSubmitting = ref(false);

// 레벨 옵션
const levels = [
  {
    id: 1,
    level: 1,
    audioUrl: "https://opickoreademo.multicampus.com/Audio/EN/0.mp3",
    description: "나는 10단어 이하의 단어로 말할 수 있습니다.",
  },
  {
    id: 2,
    level: 2,
    audioUrl: "https://opickoreademo.multicampus.com/Audio/EN/1.mp3",
    description:
      "나는 기본적인 물건, 색깔, 요일, 음식, 의류, 숫자 등을 말할 수 있습니다. 나는 항상 완벽한 문장을 구사하지는 못하고 간단한 질문도 하기 어렵습니다.",
  },
  {
    id: 3,
    level: 3,
    audioUrl: "https://opickoreademo.multicampus.com/Audio/EN/2.mp3",
    description:
      "나는 나 자신, 직장, 친숙한 사람과 장소, 일상에 대한 기본적인 정보를 간단한 문장으로 전달할 수 있습니다. 간단한 질문을 할 수 있습니다.",
  },
  {
    id: 4,
    level: 4,
    audioUrl: "https://opickoreademo.multicampus.com/Audio/EN/3.mp3",
    description:
      "나는 나 자신, 일상, 일/학교, 취미에 대해 간단한 대화를 할 수 있습니다. 나는 이런 친숙한 주제와 일상에 대해 일련의 간단한 문장들을 쉽게 만들어 낼 수 있습니다. 내가 필요한 것을 얻기 위한 질문도 할 수 있습니다.",
  },
  {
    id: 5,
    level: 5,
    audioUrl: "https://opickoreademo.multicampus.com/Audio/EN/4.mp3",
    description:
      "나는 친숙한 주제와 가정, 일/학교, 개인 및 사회적 관심사에 대해 대화할 수 있습니다. 나는 일어난 일과 일어나고 있는 일, 일어날 일에 대해 문장을 연결하여 말할 수 있습니다. 필요한 경우 설명도 할 수 있습니다. 일상 생활에서 예기치 못한 상황이 발생하더라도 임기응변으로 대처할 수 있습니다.",
  },
  {
    id: 6,
    level: 6,
    audioUrl: "https://opickoreademo.multicampus.com/Audio/EN/5.mp3",
    description:
      "나는 일/학교, 개인적인 관심사, 시사 문제에 대한 어떤 대화나 토론에도 자신 있게 참여할 수 있습니다. 나는 대부분의 주제에 관해 높은 수준의 정확성과 폭넓은 어휘로 상세히 설명할 수 있습니다.",
  },
];

// 오디오 재생
const playAudio = (url) => {
  if (currentAudio.value) {
    currentAudio.value.pause();
    currentAudio.value = null;
  }
  currentAudio.value = new Audio(url);
  currentAudio.value.play();
};

// 컴포넌트 언마운트 시 오디오 정지
onBeforeUnmount(() => {
  if (currentAudio.value) {
    currentAudio.value.pause();
    currentAudio.value = null;
  }
});

// Next 버튼 클릭 - from 파라미터에 따라 분기
const goNext = async () => {
  if (!selectedLevel.value) {
    alert("레벨을 선택해주세요.");
    return;
  }

  // 오디오 정지
  if (currentAudio.value) {
    currentAudio.value.pause();
    currentAudio.value = null;
  }

  // 시험 모드나 일반 모드 상관없이 먼저 설문을 서버에 저장합니다.
  try {
    isSubmitting.value = true;
    
    // 1. Store에서 설문 데이터 가져오기
    const surveyStore = useSurveyStore();
    const surveyData = surveyStore.surveyData;
    console.log("[SurveyLevelView] Store Data:", surveyData);

    let surveyId;

    if (surveyData) {
      // 2. 레벨 정보 추가 (selectedLevel.value는 1~6의 id값)
      const finalSurveyData = {
        ...surveyData,
        level: selectedLevel.value // API는 1~6 정수를 기대
      };
      console.log("[SurveyLevelView] Final Data to Submit:", finalSurveyData);

      // 3. API 호출 (설문 생성)
      let response;
      if (import.meta.env.VITE_USE_MOCK_DATA === 'true') {
        console.log("[SurveyLevelView] Mock Mode: Skipping API call");
        response = {
          data: {
            surveyId: 999
          }
        };
      } else {
        console.log("[SurveyLevelView] API 호출 시작");
        response = await surveysApi.createSurvey(finalSurveyData);
        console.log("[SurveyLevelView] API 응답:", response);
      }
      
      // response.data가 실제 응답 객체 (Axios 기준)
      surveyId = response.data?.surveyId;
      
      if (!surveyId) {
        console.error("[SurveyLevelView] surveyId를 받지 못했습니다:", response.data);
        alert("설문 저장에 실패했습니다. (surveyId 없음)");
        return;
      }

      console.log("[SurveyLevelView] 설문 생성 성공:", surveyId);

      // 성공적으로 저장되었다면 로컬 스토리지에 표시 (리다이렉트 루프 방지용)
      localStorage.setItem('survey_completed', 'true');
      
      // Store 초기화
      surveyStore.clearSurveyData();
    } else {
      console.warn("[SurveyLevelView] 설문 데이터가 없습니다. (새로고침 등)");
      alert("설문 데이터를 찾을 수 없습니다. 처음부터 다시 진행해주세요.");
      router.push('/');
      return;
    }

    // 4. 다음 페이지로 이동
    if (route.query.from === 'exam') {
      // 시험 모드에서 진입한 경우 기기 설정(Setup) 페이지로 이동
      console.log("[SurveyLevelView] 시험 모드 - Setup으로 이동");
      router.push({
        path: '/exam/setup',
        query: { 
          surveyId: surveyId 
        }
      });
    } else {
      // 일반 연습 모드인 경우 PracticeView로 이동
      console.log("[SurveyLevelView] 연습 모드 - Practice로 이동");
      router.push({
        path: '/practice',
        query: { 
          surveyId: surveyId,
          type: route.query.type
        }
      });
    }
  } catch (error) {
    console.error("[SurveyLevelView] 설문 생성 실패:", error);
    
    let errorMsg = "설문 저장 중 오류가 발생했습니다.";
    
    if (error.response) {
      // 서버 응답이 있는 경우
      console.error("[SurveyLevelView] Error Response:", error.response);
      console.error("[SurveyLevelView] Error Data:", error.response.data);
      console.error("[SurveyLevelView] Error Status:", error.response.status);
      
      // 상세 에러 메시지 추가
      const errorData = error.response.data;
      if (typeof errorData === 'string') {
        errorMsg += `\n${errorData}`;
      } else if (errorData.message) {
        errorMsg += `\n${errorData.message}`;
      } else {
        errorMsg += `\n${JSON.stringify(errorData)}`;
      }
      
      errorMsg += `\n(HTTP ${error.response.status})`;
    } else if (error.request) {
      // 요청은 보냈으나 응답이 없는 경우
      console.error("[SurveyLevelView] No Response:", error.request);
      errorMsg += "\n서버로부터 응답이 없습니다. 네트워크 연결을 확인해주세요.";
    } else {
      // 요청 설정 중 에러
      console.error("[SurveyLevelView] Error:", error.message);
      errorMsg += `\n${error.message}`;
    }
    
    alert(errorMsg);
  } finally {
    isSubmitting.value = false;
  }
};

const showGuide = ref(false);
</script>

<template>
  <div class="page-container">
    <header class="assessment-header">
      <div class="info-section">
        <button @click="showGuide = true" class="info-btn">
          <span class="material-icons">info</span>
        </button>
      </div>

      <nav class="step-progress">
        <div class="step completed">
          <div class="step-content">
            <span class="step-number">
              Step 1
              <span class="material-symbols-outlined check-icon"
                >check_circle</span
              >
            </span>
            <span class="step-label">Background Survey</span>
          </div>
        </div>

        <div class="step active">
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

      <h1 class="page-title">Self Assessment</h1>
      <div class="instructions">
        <p>본 Self Assessment에 대한 응답을 기초로 개인별 문항이 출제됩니다.</p>
        <p>
          설명을 잘 읽고 본인의 English 말하기 능력과 비슷한 수준을 선택하시기
          바랍니다.
        </p>
      </div>
    </header>

    <main class="page-content">
      <div class="levels-container">
        <label
          v-for="level in levels"
          :key="level.id"
          class="level-option"
          :class="{ selected: selectedLevel === level.id }"
        >
          <div class="radio-section">
            <input
              type="radio"
              :value="level.id"
              v-model="selectedLevel"
              class="level-radio"
            />
          </div>

          <div class="level-number">{{ level.level }}</div>

          <div class="level-content">
            <button
              type="button"
              @click.prevent="playAudio(level.audioUrl)"
              class="audio-btn"
            >
              <span class="material-icons">volume_up</span>
              Sample Audio
            </button>
            <p class="level-description">{{ level.description }}</p>
          </div>
        </label>
      </div>
    </main>

    <footer class="assessment-footer">
      <button @click="router.back()" class="nav-btn back-btn" :disabled="isSubmitting">Back</button>
      <button @click="router.push('/')" class="nav-btn quit-btn" :disabled="isSubmitting">Quit</button>
      <button
        @click="goNext"
        class="nav-btn next-btn"
        :disabled="!selectedLevel || isSubmitting"
      >
        {{ isSubmitting ? '저장 중...' : 'Next' }}
      </button>
    </footer>

    <transition name="fade">
      <div v-if="showGuide" class="modal-overlay" @click="showGuide = false">
        <div class="modal-card" @click.stop>
          <div class="modal-header">
            <h3>Guide</h3>
          </div>
          <div class="modal-body">
            <ul class="guide-list">
              <li>· Self Assessment 화면입니다.</li>
              <li>
                · 선택한 내용에 따라 시험 문항의 난이도가 결정됩니다. 반드시
                본인의 실력과 가장 근접하다고 생각되는 수준을 선택하십시오.
              </li>
              <li>
                · 선택을 완료한 후 Next를 누르면 이전 단계로 되돌릴 수 없으니
                신중하게 선택하시기 바랍니다.
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

.assessment-header {
  max-width: 1280px;
  margin: 0 auto;
  width: 100%;
}

.info-section {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}

.info-btn {
  background: none;
  border: none;
  color: var(--text-tertiary);
  cursor: pointer;
  transition: color 0.2s;
}

.info-btn:hover {
  color: var(--primary-color);
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

.page-title {
  font-size: 1.75rem;
  font-weight: 800;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-primary);
  margin-bottom: 16px;
  color: var(--text-primary);
}

.instructions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 32px;
}

.instructions p {
  color: var(--text-secondary);
  line-height: 1.6;
}

/* Main Content */
.levels-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.level-option {
  display: flex;
  align-items: flex-start;
  gap: 20px;
  padding: 24px;
  border-radius: 20px;
  border: var(--border-primary);
  background: var(--bg-secondary);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: var(--shadow-sm);
  position: relative;
}

.level-option:hover:not(.selected) {
  background: var(--bg-tertiary);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  border-color: var(--primary-color);
}

.level-option.selected {
  background: var(--primary-light);
  border-color: var(--primary-color);
  box-shadow: var(--shadow-md);
  color: #8B7300;
}

.radio-section {
  display: flex;
  align-items: center;
  padding-top: 6px;
}

.level-radio {
  width: 22px;
  height: 22px;
  accent-color: var(--primary-color);
  cursor: pointer;
}

.level-number {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  color: var(--primary-color);
  font-size: 1.25rem;
  margin-right: 12px;
}

.selected .level-number {
  color: #8B7300;
}

.level-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

@media (min-width: 768px) {
  .level-content {
    flex-direction: row;
    align-items: flex-start;
  }
}

.audio-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  background: var(--bg-tertiary);
  color: var(--text-primary);
  padding: 8px 16px;
  border-radius: 10px;
  border: none;
  font-size: 0.75rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
  align-self: flex-start;
  box-shadow: var(--shadow-sm);
}

.audio-btn:hover {
  background: var(--primary-light);
  color: #8B7300;
}

.selected .audio-btn {
  background: white;
  color: #8B7300;
}

.level-description {
  flex: 1;
  color: var(--text-primary);
  line-height: 1.6;
  font-size: 0.95rem;
  margin: 0;
}

.selected .level-description {
  color: #8B7300;
}

/* Footer */
.assessment-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 20px 40px;
  background: var(--bg-secondary);
  border-top: 1px solid var(--border-primary);
  z-index: 100;
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
.back-btn:disabled,
.quit-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.quit-btn {
  background: #fee2e2;
  color: #ef4444;
}

.quit-btn:hover:not(:disabled) {
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
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>