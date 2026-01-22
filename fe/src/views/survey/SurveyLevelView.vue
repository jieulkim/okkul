<script setup>
import { ref, inject } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const isDarkMode = inject('isDarkMode', ref(false))

// 선택된 레벨
const selectedLevel = ref(null)

// 레벨 옵션 (test_level.html 기준)
const levels = [
  {
    id: 243,
    level: 1,
    audioUrl: '/Audio/EN/0.mp3',
    description: '나는 10단어 이하의 단어로 말할 수 있습니다.'
  },
  {
    id: 244,
    level: 2,
    audioUrl: '/Audio/EN/1.mp3',
    description: '나는 기본적인 물건, 색깔, 요일, 음식, 의류, 숫자 등을 말할 수 있습니다. 나는 항상 완벽한 문장을 구사하지는 못하고 간단한 질문도 하기 어렵습니다.'
  },
  {
    id: 245,
    level: 3,
    audioUrl: '/Audio/EN/2.mp3',
    description: '나는 나 자신, 직장, 친숙한 사람과 장소, 일상에 대한 기본적인 정보를 간단한 문장으로 전달할 수 있습니다. 간단한 질문을 할 수 있습니다.'
  },
  {
    id: 246,
    level: 4,
    audioUrl: '/Audio/EN/3.mp3',
    description: '나는 나 자신, 일상, 일/학교, 취미에 대해 간단한 대화를 할 수 있습니다. 나는 이런 친숙한 주제와 일상에 대해 일련의 간단한 문장들을 쉽게 만들어 낼 수 있습니다. 내가 필요한 것을 얻기 위한 질문도 할 수 있습니다.'
  },
  {
    id: 247,
    level: 5,
    audioUrl: '/Audio/EN/4.mp3',
    description: '나는 친숙한 주제와 가정, 일/학교, 개인 및 사회적 관심사에 대해 대화할 수 있습니다. 나는 일어난 일과 일어나고 있는 일, 일어날 일에 대해 문장을 연결하여 말할 수 있습니다. 필요한 경우 설명도 할 수 있습니다. 일상 생활에서 예기치 못한 상황이 발생하더라도 임기응변으로 대처할 수 있습니다.'
  },
  {
    id: 248,
    level: 6,
    audioUrl: '/Audio/EN/5.mp3',
    description: '나는 일/학교, 개인적인 관심사, 시사 문제에 대한 어떤 대화나 토론에도 자신 있게 참여할 수 있습니다. 나는 대부분의 주제에 관해 높은 수준의 정확성과 폭넓은 어휘로 상세히 설명할 수 있습니다.'
  }
]

// 오디오 재생
const playAudio = (audioUrl) => {
  const audio = new Audio(audioUrl)
  audio.play().catch(e => console.error('오디오 재생 실패:', e))
}

// 이전/다음
const goBack = () => {
  router.push('/survey')
}

const goNext = () => {
  if (selectedLevel.value) {
    // TODO: API에 레벨 저장
    console.log('Selected level:', selectedLevel.value)
    router.push('/exam/setup')
  }
}

// 안내 팝업
const showGuide = ref(false)
</script>

<template>
  <div class="assessment-page">
    <header class="assessment-header">
      <div class="info-section">
        <button @click="showGuide = true" class="info-btn">
          <span class="material-icons">info</span>
        </button>
      </div>

      <!-- Step Progress -->
      <nav class="step-progress">
        <div class="step completed">
          <div class="step-content">
            <span class="step-number">Step 1 <span class="material-icons check-icon">check_circle</span></span>
            <span class="step-label">Background Survey</span>
          </div>
        </div>
        <div class="step active">
          <div class="step-content">
            <span class="step-number">Step 2</span>
            <span class="step-label">Self Assessment</span>
          </div>
        </div>
        <div class="step">
          <div class="step-content">
            <span class="step-number">Step 3</span>
            <span class="step-label">Setup</span>
          </div>
        </div>
        <div class="step">
          <div class="step-content">
            <span class="step-number">Step 4</span>
            <span class="step-label">Sample Question</span>
          </div>
        </div>
        <div class="step last">
          <div class="step-content">
            <span class="step-number">Step 5</span>
            <span class="step-label">Begin Test</span>
          </div>
        </div>
      </nav>

      <h1 class="page-title">Self Assessment</h1>
      <div class="instructions">
        <p>본 Self Assessment에 대한 응답을 기초로 개인별 문항이 출제됩니다.</p>
        <p>설명을 잘 읽고 본인의 English 말하기 능력과 비슷한 수준을 선택하시기 바랍니다.</p>
      </div>
    </header>

    <main class="assessment-main">
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
      <button @click="goBack" class="nav-btn back-btn">
        <span class="material-icons">chevron_left</span>
        Back
      </button>
      <button 
        @click="goNext" 
        class="nav-btn next-btn"
        :disabled="!selectedLevel"
      >
        Next
        <span class="material-icons">chevron_right</span>
      </button>
    </footer>

    <!-- 안내 모달 -->
    <transition name="fade">
      <div v-if="showGuide" class="modal-overlay" @click="showGuide = false">
        <div class="modal-card" @click.stop>
          <div class="modal-header">
            <h3>Guide</h3>
          </div>
          <div class="modal-body">
            <ul class="guide-list">
              <li>· Self Assessment 화면입니다.</li>
              <li>· 선택한 내용에 따라 시험 문항의 난이도가 결정됩니다. 반드시 본인의 실력과 가장 근접하다고 생각되는 수준을 선택하십시오.</li>
              <li>· 선택을 완료한 후 Next를 누르면 이전 단계로 되돌릴 수 없으니 신중하게 선택하시기 바랍니다.</li>
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
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;400;500;700&display=swap');
@import url('https://fonts.googleapis.com/icon?family=Material+Icons');

.assessment-page {
  min-height: 100vh;
  background: #FFFFFF;
  font-family: 'Noto Sans KR', sans-serif;
  display: flex;
  flex-direction: column;
  padding-bottom: 100px;
}

.dark-mode .assessment-page {
  background: #121212;
  color: #f1f5f9;
}

.assessment-header {
  max-width: 1280px;
  margin: 0 auto;
  width: 100%;
  padding: 32px 16px 0;
}

.info-section {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
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

.material-icons {
  font-family: 'Material Icons';
  font-size: 24px;
}

/* Step Progress - Hexagon style from code.html */
.step-progress {
  display: flex;
  height: 56px;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 32px;
}

.step {
  position: relative;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fa;
  color: #94a3b8;
  clip-path: polygon(0% 0%, 90% 0%, 100% 50%, 90% 100%, 0% 100%, 10% 50%);
  border-right: 1px solid white;
}

.dark-mode .step {
  background: #1E1E1E;
  border-right-color: #374155;
}

.step:first-child {
  clip-path: polygon(0% 0%, 90% 0%, 100% 50%, 90% 100%, 0% 100%);
}

.step.last {
  clip-path: polygon(0% 0%, 100% 0%, 100% 100%, 0% 100%, 10% 50%);
}

.step.completed {
  background: #f8f9fa;
  opacity: 0.6;
}

.step.active {
  background: #FFD700;
  color: #1e293b;
  z-index: 10;
}

.step-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.step-number {
  font-weight: 700;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.check-icon {
  font-size: 14px !important;
  color: #4CAF50;
}

.step-label {
  font-size: 10px;
  opacity: 0.9;
}

/* Header */
.page-title {
  font-size: 24px;
  font-weight: 700;
  padding-bottom: 16px;
  border-bottom: 1px solid #e2e8f0;
  margin-bottom: 16px;
  color: #1e293b;
}

.dark-mode .page-title {
  color: white;
  border-bottom-color: #374155;
}

.instructions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 32px;
}

.instructions p {
  color: #64748b;
  line-height: 1.6;
}

.dark-mode .instructions p {
  color: #94a3b8;
}

/* Main Content */
.assessment-main {
  max-width: 1280px;
  margin: 0 auto;
  width: 100%;
  padding: 0 16px;
}

.levels-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.level-option {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 16px;
  border-radius: 12px;
  border: 2px solid transparent;
  background: #f8f9fa;
  cursor: pointer;
  transition: all 0.2s;
}

.dark-mode .level-option {
  background: #1E1E1E;
}

.level-option:hover {
  border-color: rgba(255, 215, 0, 0.3);
}

.level-option.selected {
  border-color: #FFD700;
  background: #fffef0;
}

.dark-mode .level-option.selected {
  background: #422006;
}

.radio-section {
  display: flex;
  align-items: center;
  padding-top: 4px;
}

.level-radio {
  width: 20px;
  height: 20px;
  accent-color: #FFD700;
  cursor: pointer;
}

.level-number {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  color: #f59e0b;
  border-right: 1px solid #e2e8f0;
  padding-right: 16px;
}

.dark-mode .level-number {
  border-right-color: #374155;
}

.level-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
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
  background: #FFD94D;
  color: #374151;
  padding: 6px 12px;
  border-radius: 8px;
  border: none;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
  white-space: nowrap;
  align-self: flex-start;
}

.audio-btn:hover {
  background: #FFCA1A;
}

.audio-btn .material-icons {
  font-size: 16px !important;
}

.level-description {
  flex: 1;
  color: #1e293b;
  line-height: 1.6;
  font-size: 15px;
  margin: 0;
}

.dark-mode .level-description {
  color: #f1f5f9;
}

/* Footer */
.assessment-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  border-top: 1px solid #e2e8f0;
  padding: 16px 24px;
  z-index: 20;
}

.dark-mode .assessment-footer {
  background: #121212;
  border-top-color: #374155;
}

.assessment-footer {
  display: flex;
  justify-content: space-between;
  max-width: 1280px;
  margin: 0 auto;
}

.nav-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 24px;
  border-radius: 12px;
  border: none;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 15px;
}

.back-btn {
  background: #f1f5f9;
  color: #64748b;
}

.dark-mode .back-btn {
  background: #1E1E1E;
  color: #94a3b8;
}

.back-btn:hover {
  background: #e2e8f0;
}

.dark-mode .back-btn:hover {
  background: #374155;
}

.next-btn {
  background: #FFD700;
  color: #1e293b;
  box-shadow: 0 2px 8px rgba(255, 215, 0, 0.3);
}

.next-btn:hover:not(:disabled) {
  background: #E6C200;
}

.next-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
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
  background: white;
  border-radius: 16px;
  max-width: 500px;
  width: 90%;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.dark-mode .modal-card {
  background: #1e293b;
}

.modal-header {
  padding: 20px 24px;
  border-bottom: 1px solid #e2e8f0;
}

.dark-mode .modal-header {
  border-bottom-color: #374155;
}

.modal-header h3 {
  font-size: 18px;
  font-weight: 700;
  margin: 0;
  color: #1e293b;
}

.dark-mode .modal-header h3 {
  color: #f1f5f9;
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
  color: #64748b;
  line-height: 1.6;
}

.dark-mode .guide-list li {
  color: #94a3b8;
}

.modal-footer {
  padding: 16px 24px;
  display: flex;
  justify-content: flex-end;
}

.close-btn {
  padding: 8px 24px;
  background: #f1f5f9;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.dark-mode .close-btn {
  background: #374155;
  color: #f1f5f9;
}

.close-btn:hover {
  background: #e2e8f0;
}

.dark-mode .close-btn:hover {
  background: #475569;
}

/* Animations */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>