<script setup>
import { ref, onUnmounted, inject, computed } from 'vue'
import { useRouter, useRoute, onBeforeRouteLeave } from 'vue-router'
import { examApi } from '@/api';
import okkulPng from '@/assets/images/okkul.png';

const router = useRouter()
const route = useRoute()
const isDarkMode = inject('isDarkMode', ref(false))

const fromMode = computed(() => route.query.from || 'exam')
const currentStep = ref(3) 

const isPlaying = ref(false)
const isRecording = ref(false)
const hasRecording = ref(false)
const isLoading = ref(false)
const isNoiseDetectionEnabled = ref(false) // 실시간 소음 모드 상태

const audio = new Audio('https://audio-dataset.minio.okkul.site/opic_audio_all/okkul.mp3')
const playbackAudio = ref(null)

const stopTestAudio = () => {
  if (isPlaying.value) {
    audio.pause()
    audio.currentTime = 0
    isPlaying.value = false
  }
}

const stopPlaybackAudio = () => {
  if (playbackAudio.value) {
    playbackAudio.value.pause()
    playbackAudio.value.currentTime = 0
  }
}

const togglePlay = () => {
  stopPlaybackAudio() // 녹음 재생 중이면 중지

  if (isPlaying.value) {
    audio.pause()
    audio.currentTime = 0
  } else {
    audio.play()
    audio.onended = () => isPlaying.value = false
  }
  isPlaying.value = !isPlaying.value
}

const recordedAudioUrl = ref(null)
const mediaRecorder = ref(null)
const audioChunks = ref([])

const startRec = async () => {
  try {
    stopTestAudio()      // 테스트 음성 중지
    stopPlaybackAudio()  // 녹음 재생 중지

    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    mediaRecorder.value = new MediaRecorder(stream)
    audioChunks.value = []

    mediaRecorder.value.ondataavailable = (event) => {
      audioChunks.value.push(event.data)
    }

    mediaRecorder.value.onstop = () => {
      const audioBlob = new Blob(audioChunks.value, { type: 'audio/webm' })
      recordedAudioUrl.value = URL.createObjectURL(audioBlob)
      hasRecording.value = true
      isRecording.value = false
    }

    mediaRecorder.value.start()
    isRecording.value = true

    // 20초 후 자동 정지
    setTimeout(() => {
      if (mediaRecorder.value && mediaRecorder.value.state === 'recording') {
        stopRec()
      }
    }, 20000)

  } catch (error) {
    console.error('마이크 접근 실패:', error)
    alert('마이크를 사용할 수 없습니다.')
  }
}

const stopRec = () => {
  if (mediaRecorder.value && mediaRecorder.value.state === 'recording') {
    mediaRecorder.value.stop()
    mediaRecorder.value.stream.getTracks().forEach(track => track.stop())
    isRecording.value = false
  }
}

const playRecording = () => {
  if (recordedAudioUrl.value) {
    stopTestAudio()
    stopPlaybackAudio()
    
    playbackAudio.value = new Audio(recordedAudioUrl.value)
    playbackAudio.value.play()
  }
}

const handleNext = async () => {
  if (fromMode.value === 'practice') {
    isExiting.value = true;
    router.push('/practice/question')
  } else {
    await startExam()
  }
}

const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms));

// 난이도별 총 문항 수 계산 (Lv1-2: 12문항, Lv3-6: 15문항)
const getTotalQuestionsByLevel = (level) => {
  return level >= 3 ? 15 : 12;
};

const startExam = async () => {
  try {
    isLoading.value = true
    const surveyId = parseInt(route.query.surveyId)
    
    console.log('[SetupView] 시험 시작 요청:', { surveyId })
    
    if (!surveyId) {
      alert('설문 정보가 없습니다.')
      router.push('/exam')
      return
    }
    
    // 1. 시험 생성 요청 (비동기 트리거)
    let initialResponse;
    if (import.meta.env.VITE_USE_MOCK_DATA === 'true') {
        // Mock Mode (즉시 리턴)
        console.log('[SetupView] Mock Mode: 임시 데이터 사용')
         const mockQuestions = Array.from({ length: 7 }, (_, i) => ({
            questionId: 100 + i,
            order: i + 1,
            questionText: `[Mock] Question ${i + 1}`,
            audioUrl: null,
        }));
        initialResponse = {
            data: {
                id: 999,
                questions: mockQuestions,
                totalQuestions: 12 // Mock mode defaults to 12 since initialDifficulty is 1 by default
            }
        };
    } else {
        console.log('[SetupView] API 호출 시작')
        initialResponse = await examApi.startExam({
            surveyId: surveyId
        })
        console.log('[SetupView] API 응답:', initialResponse)
    }

    // 2. ID 확인
    const createdExamId = initialResponse.data?.id;
    const initialDifficulty = initialResponse.data?.initialDifficulty || 1; // 기본값 1

    if (!createdExamId) {
        throw new Error('시험 ID를 받지 못했습니다.');
    }

    // 3. Polling: 질문 생성 대기 (최대 20초: 2초 x 10회)
    let finalQuestions = [];
    let attempts = 0;
    const maxAttempts = 10;

    if (import.meta.env.VITE_USE_MOCK_DATA === 'true') {
        finalQuestions = initialResponse.data.questions;
    } else {
        console.log(`[SetupView] 질문 생성 대기 중... (ID: ${createdExamId})`);
        
        while (attempts < maxAttempts) {
            try {
                // 상세 정보 조회 (질문 포함 여부 확인)
                const detailResponse = await examApi.getExamInfo(createdExamId);
                const detailData = detailResponse.data;

                if (detailData.questions && detailData.questions.length > 0) {
                    console.log(`[SetupView] 질문 생성 완료! (${detailData.questions.length}개)`);
                    finalQuestions = detailData.questions;
                    break;
                }
            } catch (err) {
                console.warn(`[SetupView] Polling 중 오류 발생 (시도 ${attempts + 1}):`, err);
                // 일시적 오류일 수 있으므로 계속 진행
            }

            attempts++;
            console.log(`[SetupView] 질문 생성 대기... (${attempts}/${maxAttempts})`);
            await delay(2000); // 2초 대기
        }
    }
    
    if (!finalQuestions || finalQuestions.length === 0) {
      throw new Error('질문 생성 시간 초과. 잠시 후 다시 시도해주세요.');
    }
    
    const examData = {
      examId: createdExamId,
      questions: finalQuestions,
      totalQuestions: getTotalQuestionsByLevel(initialDifficulty), // 난이도에 따라 전체 문제 수 설정
      currentIndex: 0,
      surveyId,
      initialDifficulty, // 난이도 저장
      startedAt: new Date().toISOString()
    }
    
    // localStorage에 시험 데이터 저장
    localStorage.setItem(`exam_${createdExamId}`, JSON.stringify(examData))
    console.log('[SetupView] localStorage 저장 완료:', `exam_${createdExamId}`)
    
    // 시험 화면으로 이동
    isExiting.value = true;
    router.push({
      path: '/exam/question',
      query: { examId: createdExamId }
    })
    
  } catch (error) {
    console.error('[SetupView] 시험 시작 실패:', error)
    let errorMessage = '시험을 시작하는데 실패했습니다.';
    if (error.response) {
      errorMessage += `\n(${error.response.status}: ${JSON.stringify(error.response.data)})`;
    } else {
      errorMessage += `\n${error.message}`;
    }
    alert(errorMessage);
  } finally {
    isLoading.value = false
  }
}

onUnmounted(() => {
  audio.pause()
  stopPlaybackAudio()
})

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
    <header class="assessment-header">
      <div class="header-top">
        <button @click="handleExit" class="quit-btn" :disabled="isLoading">
          <span class="material-icons">close</span>
          나가기
        </button>
      </div>
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
      <div class="title-row">
        <h1 class="page-title">Device Setup</h1>
        <!-- <div class="noise-toggle-container">
          <span class="toggle-label">실시간 소음 모드</span>
          <button 
            @click="isNoiseDetectionEnabled = !isNoiseDetectionEnabled" 
            class="toggle-switch"
            :class="{ active: isNoiseDetectionEnabled }"
            :aria-label="isNoiseDetectionEnabled ? '소음 모드 끄기' : '소음 모드 켜기'"
          >
            <span class="toggle-slider"></span>
          </button>
        </div> -->
      </div>
    </header>

    <main class="page-content">
      <div class="setup-grid">
        <div class="character-card">
          <div class="okkul-mini-container">
            <img :src="okkulPng" alt="OKKUL" class="okkul-img">
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
            <button v-if="!isRecording" @click="startRec" class="rec-btn">
              Start Recording
            </button>
            <button v-else @click="stopRec" class="rec-btn stop-rec-btn">
              Stop Recording
            </button>
            <button v-if="hasRecording" @click="playRecording" class="check-btn">Play Recorded Voice</button>
          </div>
        </div>
      </div>

      <div class="setup-actions">
        <button @click="handleExit" class="nav-btn back-btn" :disabled="isLoading">Back</button>
        <button @click="handleNext" class="nav-btn next-btn" :disabled="isLoading">
          {{ isLoading ? '시작 중...' : 'Next' }}
        </button>
      </div>
    </main>

    <!-- Exit Confirmation Modal -->
    <transition name="fade">
      <div v-if="showExitModal" class="modal-overlay" @click="showExitModal = false">
        <div class="modal-card exit-modal" @click.stop>
          <div class="modal-header">
            <h3>진행 중단</h3>
            <p class="subtitle">현재 설정을 중단하시겠습니까?<br>준비된 내용이 저장되지 않을 수 있습니다.</p>
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
.page-container {
  height: calc(100vh - var(--header-height, 60px));
  min-height: 0 !important;
  padding: 0 !important;
  overflow: hidden;
  background: var(--bg-color, #FFFFFF);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.page-content {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  /* Lifting content: center but with a slight upward bias */
  padding-bottom: 5vh;
}

/* Step Progress */
.step-progress {
  display: flex;
  height: 44px;
  margin-bottom: 24px;
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
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  padding: 16px 24px 0;
  flex-shrink: 0;
}

.header-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
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

.quit-btn:hover:not(:disabled) {
  background: #fee2e2;
  color: #ef4444;
}

.quit-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
  border-bottom: 1px solid var(--border-primary);
  padding-bottom: 12px;
}

.page-title {
  font-size: 2rem;
  font-weight: 800;
  color: var(--text-primary);
  margin: 0;
}

/* 실시간 소음 모드 토글 */
.noise-toggle-container {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-right: 16px;
}

.toggle-label {
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-secondary);
}

.toggle-switch {
  position: relative;
  width: 46px;
  height: 24px;
  background: var(--bg-tertiary);
  border: 2px solid var(--border-primary);
  border-radius: 12px;
  cursor: pointer;
  padding: 0;
  outline: none;
}

.toggle-switch:hover {
  border-color: var(--primary-color);
}

.toggle-switch.active {
  background: var(--primary-color);
  border-color: var(--primary-color);
}

.toggle-slider {
  position: absolute;
  top: 50%;
  left: 2px;
  transform: translateY(-50%);
  width: 16px;
  height: 16px;
  background: white;
  border-radius: 50%;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.toggle-switch.active .toggle-slider {
  transform: translate(22px, -50%);
}

.setup-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
  max-width: 1100px;
  width: 100%;
  margin: 24px auto 0;
  flex: 1;
  align-items: center;
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
  padding: 32px;
  text-align: center;
  box-shadow: var(--shadow-md);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.okkul-img {
  width: 200px;
  height: auto;
  margin-bottom: 0px;
  display: block;
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

.setup-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 16px 0;
  flex-shrink: 0;
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

.stop-rec-btn {
  background: #ef4444;
  color: white;
}

.stop-rec-btn:hover {
  background: #dc2626;
}

/* Modal Styles */
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
  z-index: 2000;
}

.modal-header {
  padding: 40px 40px 20px;
  text-align: center;
}

.modal-header h3 {
  font-size: 1.5rem;
  font-weight: 800;
  margin: 0 0 12px;
  color: #0f172a;
}

.modal-header .subtitle {
  font-size: 1rem;
  color: #64748b;
  margin: 0;
}

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

.modal-btn.confirm {
  background: #fee2e2;
  color: #ef4444;
}

.modal-btn.confirm:hover {
  background: #fecaca;
  transform: translateY(-2px);
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>