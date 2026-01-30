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
            examSetId: 1,
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
@import url('https://fonts.googleapis.com/icon?family=Material+Icons');

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

.step-progress {
  display: flex;
  height: 48px;
  margin-bottom: 30px;
  width: 100%;
}

.step {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-tertiary);
  color: var(--text-tertiary);
  font-size: 12px;
  clip-path: polygon(0% 0%, 90% 0%, 100% 50%, 90% 100%, 0% 100%, 10% 50%);
  margin-right: -2px;
  border: var(--border-thin);
}

.step:first-child { clip-path: polygon(0% 0%, 90% 0%, 100% 50%, 90% 100%, 0% 100%); }
.step.last { clip-path: polygon(0% 0%, 100% 0%, 100% 100%, 0% 100%, 10% 50%); }

.step.active { background: var(--primary-color); color: #000000; font-weight: 900; }
.step.completed { background: var(--bg-secondary); color: var(--text-secondary); opacity: 0.8; }

.step-content { display: flex; flex-direction: column; align-items: center; gap: 2px; }
.step-number { font-weight: 700; display: flex; align-items: center; gap: 4px; }
.check-icon { font-size: 14px !important; }

.dark-mode { background: var(--bg-primary); color: #f1f5f9; }
.dark-mode .step { background: var(--bg-tertiary); }
.dark-mode .step.active { background: var(--primary-color); }
.dark-mode .character-card { background: var(--bg-secondary); border-color: #FFFFFF; }
.dark-mode .assessment-footer { background: var(--bg-secondary); border-top-color: #FFFFFF; }

.assessment-header { max-width: 1280px; margin: 0 auto; padding: 32px 16px; }
.page-title { font-size: 24px; font-weight: 800; margin-top: 20px; }
.setup-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 40px; max-width: 1000px; margin: 0 auto; }
.character-card { 
  background: var(--bg-secondary);
  border: var(--border-primary);
  border-radius: var(--border-radius);
  padding: 60px;
  text-align: center;
  box-shadow: var(--shadow-md);
}
.play-btn { 
  background: var(--primary-color);
  border: var(--border-secondary);
  padding: 12px 24px;
  border-radius: var(--border-radius);
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 30px auto 0;
  font-weight: 900;
  color: #000000;
  box-shadow: var(--shadow-sm);
  transition: all 0.2s;
}

.play-btn:hover {
  transform: translate(-0.02em, -0.02em);
  box-shadow: var(--shadow-md);
}

.btns { display: flex; gap: 10px; margin-top: 20px; }
.rec-btn { background: #ef4444; color: white; border: var(--border-thin); padding: 12px 20px; border-radius: var(--border-radius); cursor: pointer; font-weight: 900; box-shadow: var(--shadow-sm); }
.check-btn { background: #3b82f6; color: white; border: var(--border-thin); padding: 12px 20px; border-radius: var(--border-radius); cursor: pointer; font-weight: 900; box-shadow: var(--shadow-sm); }

.assessment-footer { position: fixed; bottom: 0; left: 0; right: 0; padding: 20px 40px; display: flex; justify-content: space-between; border-top: var(--border-primary); background: var(--bg-secondary); z-index: 100; }
.dark-mode .assessment-footer { background: var(--bg-secondary); }
.nav-btn { padding: 12px 30px; border-radius: var(--border-radius); border: var(--border-secondary); font-weight: 900; cursor: pointer; box-shadow: var(--shadow-sm); transition: all 0.2s; }
.nav-btn:hover:not(:disabled) { transform: translate(-0.02em, -0.02em); box-shadow: var(--shadow-md); }
.nav-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.back-btn { background: var(--bg-tertiary); color: var(--text-secondary); }
.next-btn { background: var(--primary-color); color: #000000; }
</style>