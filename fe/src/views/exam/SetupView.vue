<script setup>
import { ref, onUnmounted, inject, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { examApi } from '@/api';

const router = useRouter()
const route = useRoute()
const isDarkMode = inject('isDarkMode', ref(false))

const fromMode = computed(() => route.query.from || 'exam')
const currentStep = ref(3) 

const isPlaying = ref(false)
const isRecording = ref(false)
const hasRecording = ref(false)
const isLoading = ref(false)

const audio = new Audio('https://opickoreademo.multicampus.com/Audio/EN/0.mp3')

const togglePlay = () => {
  if (isPlaying.value) {
    audio.pause()
    audio.currentTime = 0
  } else {
    audio.play()
    audio.onended = () => isPlaying.value = false
  }
  isPlaying.value = !isPlaying.value
}

const startRec = () => {
  isRecording.value = true
  setTimeout(() => {
    isRecording.value = false
    hasRecording.value = true
  }, 3000)
}

const handleNext = async () => {
  if (fromMode.value === 'practice') {
    router.push('/practice/question')
  } else {
    await startExam()
  }
}

const startExam = async () => {
  try {
    isLoading.value = true
    const surveyId = parseInt(route.query.surveyId)
    
    if (!surveyId) {
      alert('설문 정보가 없습니다.')
      router.push('/exam')
      return
    }
    
    let response;
    // Mock Mode
    if (import.meta.env.VITE_USE_MOCK_DATA === 'true') {
        const mockQuestions = Array.from({ length: 7 }, (_, i) => ({
            id: 100 + i,
            answerId: 500 + i,
            order: i + 1,
            questionText: `[Mock] Question ${i + 1}`,
            audioUrl: null,
            type: 'SPEAKING',
            preparationTime: 10,
            speakingTime: 30
        }));

        response = {
            data: {
                examId: 999,
                questions: mockQuestions,
                totalQuestions: 15
            }
        };
    } else {
        response = await examApi.startExam({
            surveyId: surveyId
        })
    }
    
    const { examId, questions, totalQuestions } = response.data
    
    const examData = {
      examId,
      questions,
      totalQuestions,
      currentIndex: 0,
      surveyId,
      startedAt: new Date().toISOString()
    }
    
    localStorage.setItem(`exam_${examId}`, JSON.stringify(examData))
    
    router.push({
      path: '/exam/question',
      query: { examId }
    })
    
  } catch (error) {
    console.error('시험 시작 실패:', error)
    alert('시험을 시작하는데 실패했습니다.')
  } finally {
    isLoading.value = false
  }
}

onUnmounted(() => audio.pause())
</script>

<template>
  <div class="page-container">
    <header class="assessment-header">
      <nav class="step-progress">
        <div class="step" :class="{ completed: currentStep > 1, active: currentStep === 1 }">
          <div class="step-content">
            <span class="step-number">Step 1 <span v-if="currentStep > 1" class="material-icons check-icon">check_circle</span></span>
            <span class="step-label">Background Survey</span>
          </div>
        </div>
        <div class="step" :class="{ completed: currentStep > 2, active: currentStep === 2 }">
          <div class="step-content">
            <span class="step-number">Step 2 <span v-if="currentStep > 2" class="material-icons check-icon">check_circle</span></span>
            <span class="step-label">Self Assessment</span>
          </div>
        </div>
        <div class="step last" :class="{ completed: currentStep > 3, active: currentStep === 3 }">
          <div class="step-content">
            <span class="step-number">Step 3 <span v-if="currentStep > 3" class="material-icons check-icon">check_circle</span></span>
            <span class="step-label">Setup</span>
          </div>
        </div>
      </nav>
      <h1 class="page-title">Device Setup</h1>
    </header>

    <main class="page-content">
      <div class="setup-grid">
        <div class="character-card">
          <div class="okkul-mini-container">
            <div class="platypus-body">
              <div class="platypus-hat"></div>
              <div class="platypus-eye left"></div>
              <div class="platypus-eye right"></div>
              <div class="platypus-bill"></div>
              <div class="platypus-arm-right" :class="{ 'wave': isPlaying }"></div>
            </div>
          </div>
          <button @click="togglePlay" class="play-btn" :class="{ active: isPlaying }">
            <span class="material-icons">{{ isPlaying ? 'stop' : 'play_arrow' }}</span>
            {{ isPlaying ? 'Stop' : 'Test Audio' }}
          </button>
        </div>

        <div class="control-card">
          <ul class="steps">
            <li>1. 소리가 잘 들리는지 버튼을 눌러 확인하세요.</li>
            <li>2. 마이크 녹음 버튼을 눌러 목소리를 점검하세요.</li>
          </ul>
          <div class="btns">
            <button @click="startRec" class="rec-btn" :disabled="isRecording">
              {{ isRecording ? 'Recording...' : 'Start Recording' }}
            </button>
            <button v-if="hasRecording" class="check-btn">Play Recorded Voice</button>
          </div>
        </div>
      </div>
    </main>

    <footer class="assessment-footer">
      <button @click="router.back()" class="nav-btn back-btn" :disabled="isLoading">Back</button>
      <button @click="handleNext" class="nav-btn next-btn" :disabled="isLoading">
        {{ isLoading ? '시작 중...' : 'Next' }}
      </button>
    </footer>
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

.assessment-header {
  max-width: 1280px;
  margin: 0 auto;
  width: 100%;
}

.page-title {
  font-size: 1.75rem;
  font-weight: 800;
  margin-top: 24px;
  color: var(--text-primary);
  border-bottom: 1px solid var(--border-primary);
  padding-bottom: 16px;
}

.setup-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
  max-width: 1100px;
  margin: 40px auto 0;
}

@media (max-width: 768px) {
  .setup-grid {
    grid-template-columns: 1fr;
  }
}

.character-card { 
  background: var(--bg-secondary);
  border: var(--border-primary);
  border-radius: 24px;
  padding: 48px;
  text-align: center;
  box-shadow: var(--shadow-md);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.play-btn { 
  background: var(--bg-tertiary);
  border: 1px solid var(--border-primary);
  padding: 12px 28px;
  border-radius: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-top: 32px;
  font-weight: 700;
  color: var(--text-primary);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: var(--shadow-sm);
}

.play-btn:hover {
  background: var(--primary-light);
  border-color: var(--primary-color);
  color: #8B7300;
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.play-btn.active {
  background: var(--primary-color);
  color: #212529;
  border-color: var(--primary-color);
}

.control-card {
  background: var(--bg-secondary);
  border: var(--border-primary);
  border-radius: 24px;
  padding: 40px;
  box-shadow: var(--shadow-md);
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.steps {
  list-style: none;
  padding: 0;
  margin: 0 0 32px 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.steps li {
  font-size: 1rem;
  color: var(--text-secondary);
  line-height: 1.6;
}

.btns {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.rec-btn {
  background: #fee2e2;
  color: #ef4444;
  border: none;
  padding: 14px 24px;
  border-radius: 12px;
  cursor: pointer;
  font-weight: 700;
  transition: all 0.2s;
  font-size: 1rem;
}

.rec-btn:hover:not(:disabled) {
  background: #fecaca;
  transform: translateY(-2px);
}

.check-btn {
  background: #dbeafe;
  color: #2563eb;
  border: none;
  padding: 14px 24px;
  border-radius: 12px;
  cursor: pointer;
  font-weight: 700;
  transition: all 0.2s;
  font-size: 1rem;
}

.check-btn:hover {
  background: #bfdbfe;
  transform: translateY(-2px);
}

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

.nav-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}
</style>
