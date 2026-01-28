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
    
    const response = await examApi.startExam({
      examSetId: 1,
      surveyId: surveyId
    })
    
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
  <div class="assessment-page" :class="{ 'dark-mode': isDarkMode }">
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

    <main class="assessment-main">
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

.assessment-page {
  min-height: 100vh;
  background: #ffffff;
  color: #1e293b;
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
  background: #eee;
  color: #94a3b8;
  font-size: 12px;
  clip-path: polygon(0% 0%, 90% 0%, 100% 50%, 90% 100%, 0% 100%, 10% 50%);
  margin-right: -2px;
}

.step:first-child { clip-path: polygon(0% 0%, 90% 0%, 100% 50%, 90% 100%, 0% 100%); }
.step.last { clip-path: polygon(0% 0%, 100% 0%, 100% 100%, 0% 100%, 10% 50%); }

.step.active { background: #FFD700; color: #1e293b; font-weight: bold; }
.step.completed { background: #e2e8f0; color: #64748b; opacity: 0.8; }

.step-content { display: flex; flex-direction: column; align-items: center; gap: 2px; }
.step-number { font-weight: 700; display: flex; align-items: center; gap: 4px; }
.check-icon { font-size: 14px !important; }

.dark-mode { background: #0f172a; color: #f1f5f9; }
.dark-mode .step { background: #1e293b; color: #64748b; }
.dark-mode .step.active { background: #FFD700; color: #0f172a; }
.dark-mode .character-card { background: #1e293b; border-color: #334155; }
.dark-mode .assessment-footer { background: #0f172a; border-top-color: #334155; }

.assessment-header { max-width: 1280px; margin: 0 auto; padding: 32px 16px; }
.page-title { font-size: 24px; font-weight: 800; margin-top: 20px; }
.setup-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 40px; max-width: 1000px; margin: 0 auto; }
.character-card { background: #f8f9fa; border-radius: 20px; padding: 60px; text-align: center; }
.play-btn { background: #FFD700; border: none; padding: 12px 24px; border-radius: 30px; cursor: pointer; display: flex; align-items: center; gap: 8px; margin: 30px auto 0; font-weight: bold; }

.btns { display: flex; gap: 10px; margin-top: 20px; }
.rec-btn { background: #ef4444; color: white; border: none; padding: 12px 20px; border-radius: 8px; cursor: pointer; }
.check-btn { background: #3b82f6; color: white; border: none; padding: 12px 20px; border-radius: 8px; cursor: pointer; }

.assessment-footer { position: fixed; bottom: 0; left: 0; right: 0; padding: 20px 40px; display: flex; justify-content: space-between; border-top: 1px solid #e2e8f0; background: white; z-index: 100; }
.dark-mode .assessment-footer { background: #0f172a; }
.nav-btn { padding: 12px 30px; border-radius: 12px; border: none; font-weight: bold; cursor: pointer; }
.nav-btn:disabled { opacity: 0.5; cursor: not-allowed; }
.back-btn { background: #f1f5f9; color: #64748b; }
.next-btn { background: #FFD700; color: #0f172a; }
</style>