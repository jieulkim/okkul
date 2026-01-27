<script setup>
import { ref, computed, onMounted, onBeforeUnmount, inject } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const isDarkMode = inject('isDarkMode', ref(false))

// 시험 정보
const examId = ref(null)
const surveyId = ref(null)
const currentQuestionIndex = ref(1)
const totalQuestions = ref(15)
const questions = ref([])
const currentQuestion = ref(null)

// 상태 관리
const isPlaying = ref(false)
const isRecording = ref(false)
const hasReplayed = ref(false)
const canReplay = ref(false)
const replayTimeout = ref(null)
const recordingStartTime = ref(null)
const recordingDuration = ref(0)
const recordingInterval = ref(null)

// 오디오/비디오
const videoElement = ref(null)
const currentAudio = ref(null)

// 미디어 레코더
const mediaRecorder = ref(null)
const audioChunks = ref([])
const recordedBlob = ref(null)

// Step Progress (Setup 완료 후 Sample Question)
const currentStep = ref(5)

// 난이도 재조정 모달
const showDifficultyAdjustModal = ref(false)
const adjustedDifficulty = ref(null)

// Guide 모달
const showGuide = ref(false)

// 시험 시작
onMounted(async () => {
  surveyId.value = route.query.surveyId
  
  if (!surveyId.value) {
    alert('설문 정보가 없습니다. 처음부터 다시 시작해주세요.')
    router.push('/exam')
    return
  }
  
  await startExam()
})

// 시험 시작 API 호출
const startExam = async () => {
  try {
    const response = await fetch('https://api.dev.okkul.site/exam/start', {
      method: 'POST',
      headers: { 
        'Content-Type': 'application/json',
        // TODO: JWT 토큰 추가
      },
      body: JSON.stringify({
        examSetId: 1, // TODO: 실제 examSetId
        surveyId: parseInt(surveyId.value)
      })
    })
    
    if (!response.ok) throw new Error('시험 시작 실패')
    
    const data = await response.json()
    examId.value = data.examId
    totalQuestions.value = data.totalQuestions
    questions.value = data.questions // 1~7번 문항
    currentQuestion.value = questions.value[0]
    
    console.log('시험 시작:', data)
  } catch (error) {
    console.error('시험 시작 오류:', error)
    alert('시험 시작에 실패했습니다.')
  }
}

// Play 버튼
const playQuestion = () => {
  if (!currentQuestion.value) return
  
  isPlaying.value = true
  canReplay.value = false
  hasReplayed.value = false
  
  // 비디오 재생 (아바타)
  if (videoElement.value) {
    videoElement.value.play()
  }
  
  // 오디오 재생
  if (currentAudio.value) {
    currentAudio.value.pause()
    currentAudio.value = null
  }
  
  currentAudio.value = new Audio(currentQuestion.value.audioUrl)
  currentAudio.value.play()
  
  currentAudio.value.onended = () => {
    isPlaying.value = false
    
    // 5초 안에 Replay 가능
    canReplay.value = true
    replayTimeout.value = setTimeout(() => {
      canReplay.value = false
      startRecording()
    }, 5000)
  }
}

// Replay 버튼
const replayQuestion = () => {
  if (!canReplay.value || hasReplayed.value) return
  
  clearTimeout(replayTimeout.value)
  hasReplayed.value = true
  canReplay.value = false
  
  playQuestion()
}

// 녹음 시작
const startRecording = async () => {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    mediaRecorder.value = new MediaRecorder(stream)
    audioChunks.value = []
    
    mediaRecorder.value.ondataavailable = (event) => {
      audioChunks.value.push(event.data)
    }
    
    mediaRecorder.value.onstop = async () => {
      recordedBlob.value = new Blob(audioChunks.value, { type: 'audio/webm' })
      await submitAnswer()
    }
    
    mediaRecorder.value.start()
    isRecording.value = true
    recordingStartTime.value = Date.now()
    
    // 녹음 시간 카운트
    recordingInterval.value = setInterval(() => {
      recordingDuration.value = Math.floor((Date.now() - recordingStartTime.value) / 1000)
    }, 1000)
    
  } catch (error) {
    console.error('녹음 시작 실패:', error)
    alert('마이크 접근 권한이 필요합니다.')
  }
}

// 녹음 중지
const stopRecording = () => {
  if (mediaRecorder.value && isRecording.value) {
    mediaRecorder.value.stop()
    isRecording.value = false
    clearInterval(recordingInterval.value)
    
    // 스트림 정지
    mediaRecorder.value.stream.getTracks().forEach(track => track.stop())
  }
}

// 답변 제출
const submitAnswer = async () => {
  if (!recordedBlob.value) return
  
  try {
    const formData = new FormData()
    formData.append('file', recordedBlob.value, 'answer.webm')
    
    const response = await fetch(
      `https://api.dev.okkul.site/exam/exam/${examId.value}/questions/${currentQuestion.value.answerId}/answer`,
      {
        method: 'POST',
        body: formData
      }
    )
    
    if (!response.ok) throw new Error('답변 제출 실패')
    
    console.log('답변 제출 성공')
    
    // 7번 문항 완료 시 난이도 재조정
    if (currentQuestionIndex.value === 7) {
      showDifficultyAdjustModal.value = true
    } else {
      goNextQuestion()
    }
    
  } catch (error) {
    console.error('답변 제출 오류:', error)
    alert('답변 제출에 실패했습니다.')
  }
}

// 난이도 선택 후 나머지 문항 가져오기
const selectDifficulty = async (difficulty) => {
  adjustedDifficulty.value = difficulty
  showDifficultyAdjustModal.value = false
  
  try {
    const response = await fetch(
      `https://api.dev.okkul.site/exam/${examId.value}/questions/current?adjustedDifficulty=${difficulty}`,
      { method: 'POST' }
    )
    
    if (!response.ok) throw new Error('문항 조회 실패')
    
    const remainingQuestions = await response.json()
    questions.value = [...questions.value, ...remainingQuestions]
    
    goNextQuestion()
    
  } catch (error) {
    console.error('나머지 문항 조회 오류:', error)
    alert('문항을 불러오는데 실패했습니다.')
  }
}

// 다음 문제로
const goNextQuestion = () => {
  recordedBlob.value = null
  recordingDuration.value = 0
  
  if (currentQuestionIndex.value < totalQuestions.value) {
    currentQuestionIndex.value++
    currentQuestion.value = questions.value[currentQuestionIndex.value - 1]
  } else {
    completeExam()
  }
}

// 시험 완료
const completeExam = async () => {
  try {
    const response = await fetch(
      `https://api.dev.okkul.site/exam/exam/${examId.value}/complete`,
      { method: 'POST' }
    )
    
    if (!response.ok) throw new Error('시험 완료 처리 실패')
    
    console.log('시험 완료')
    
    // 결과 대기 페이지로 이동
    router.push({
      path: '/exam/result',
      query: { examId: examId.value }
    })
    
  } catch (error) {
    console.error('시험 완료 오류:', error)
    alert('시험 완료 처리에 실패했습니다.')
  }
}

// 정리
onBeforeUnmount(() => {
  if (currentAudio.value) {
    currentAudio.value.pause()
    currentAudio.value = null
  }
  stopRecording()
  clearTimeout(replayTimeout.value)
  clearInterval(recordingInterval.value)
})

// 녹음 시간 포맷
const formattedDuration = computed(() => {
  const minutes = Math.floor(recordingDuration.value / 60)
  const seconds = recordingDuration.value % 60
  return `${minutes}:${seconds.toString().padStart(2, '0')}`
})

// 프롬프트 상태 텍스트
const promptStatusText = computed(() => {
  if (isRecording.value) return 'Recording'
  if (isPlaying.value) return 'Playing'
  if (canReplay.value && !hasReplayed.value) return 'Click REPLAY to listen again (5 sec)'
  return 'Click PLAY button to Listen'
})
</script>

<template>
  <div class="exam-page" :class="{ 'dark-mode': isDarkMode }">
    <!-- Header -->
    <header class="exam-header">
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
        <div class="step completed">
          <div class="step-content">
            <span class="step-number">Step 2 <span class="material-icons check-icon">check_circle</span></span>
            <span class="step-label">Self Assessment</span>
          </div>
        </div>
        <div class="step completed">
          <div class="step-content">
            <span class="step-number">Step 3 <span class="material-icons check-icon">check_circle</span></span>
            <span class="step-label">Setup</span>
          </div>
        </div>
        <div class="step completed">
          <div class="step-content">
            <span class="step-number">Step 4 <span class="material-icons check-icon">check_circle</span></span>
            <span class="step-label">Sample Question</span>
          </div>
        </div>
        <div class="step active last">
          <div class="step-content">
            <span class="step-number">Step 5</span>
            <span class="step-label">Begin Test</span>
          </div>
        </div>
      </nav>

      <h1 class="page-title">OPIc Test</h1>
      <h2 class="question-header">Question {{ currentQuestionIndex }} of {{ totalQuestions }}</h2>
    </header>

    <!-- Main Content -->
    <main class="exam-main">
      <div class="exam-container">
        <!-- 왼쪽: 비디오 & 컨트롤 -->
        <div class="video-section">
          <div class="video-card">
            <video
              ref="videoElement"
              class="avatar-video"
              poster="https://opickoreademo.multicampus.com/images/NewEuroAvatarCaptured.png"
              muted
              loop
            >
              <source src="https://opickoreademo.multicampus.com/file/NewEuroStandBy" type="video/mp4">
            </video>
          </div>

          <!-- 컨트롤 버튼 -->
          <div class="controls">
            <button
              v-if="!isPlaying && !canReplay"
              @click="playQuestion"
              class="control-btn play-btn"
              :disabled="isRecording"
            >
              <span class="material-icons">play_arrow</span>
            </button>
            
            <button
              v-if="canReplay && !hasReplayed"
              @click="replayQuestion"
              class="control-btn replay-btn"
            >
              <span class="material-icons">replay</span>
            </button>

            <div class="playback-bar">
              <div class="playback-track"></div>
            </div>
          </div>

          <!-- 상태 표시 -->
          <div class="status-indicator" :class="{ 
            'status-playing': isPlaying,
            'status-recording': isRecording,
            'status-replay': canReplay
          }">
            {{ promptStatusText }}
          </div>
        </div>

        <!-- 오른쪽: 문항 진행 & 안내 -->
        <div class="info-section-right">
          <h3 class="section-title">문항 진행:</h3>
          <ul class="question-progress">
            <li
              v-for="i in totalQuestions"
              :key="i"
              :class="{ 
                active: i === currentQuestionIndex,
                completed: i < currentQuestionIndex
              }"
            >
              {{ i }}
            </li>
          </ul>

          <!-- 안내 메시지 -->
          <div v-if="!isPlaying && !isRecording && !canReplay" class="alert-box blue">
            <b>Play</b> 아이콘(▶)을 눌러 질문을 청취하십시오.<br><br>
            <b>중요!</b> 5초 이내에 버튼을 누르면 질문 다시듣기가 가능하며, 재청취는 <b>한번</b>만 가능합니다.
          </div>

          <div v-if="canReplay && !hasReplayed" class="alert-box blue">
            5초 이내에 <b>Replay</b> 아이콘을 눌러야만 질문 다시듣기가 가능합니다.
          </div>

          <div v-if="isRecording" class="alert-box purple">
            <b>중요!</b> 문항 재생 후 자동으로 답변 녹음이 시작됩니다.<br>
            Recording 표시 확인 후 답변을 시작하십시오.<br>
            문항별 답변 제한시간은 없습니다.
          </div>

          <!-- 녹음 시간 -->
          <div v-if="isRecording" class="recording-timer">
            <h3>답변 시간:</h3>
            <div class="timer-display">{{ formattedDuration }}</div>
            <button @click="stopRecording" class="stop-btn">
              답변 완료
            </button>
          </div>
        </div>
      </div>
    </main>

    <!-- 난이도 재조정 모달 -->
    <transition name="fade">
      <div v-if="showDifficultyAdjustModal" class="modal-overlay">
        <div class="modal-card difficulty-modal">
          <div class="modal-header">
            <h3>난이도 재조정</h3>
          </div>
          <div class="modal-body">
            <p class="modal-text">
              7번 문항까지 완료하셨습니다.<br>
              나머지 문항의 난이도를 선택해주세요.
            </p>
            <div class="difficulty-options">
              <button
                v-for="level in [1, 2, 3, 4, 5, 6]"
                :key="level"
                @click="selectDifficulty(level)"
                class="difficulty-btn"
              >
                난이도 {{ level }}
              </button>
            </div>
          </div>
        </div>
      </div>
    </transition>

    <!-- Guide 모달 -->
    <transition name="fade">
      <div v-if="showGuide" class="modal-overlay" @click="showGuide = false">
        <div class="modal-card" @click.stop>
          <div class="modal-header">
            <h3>Guide</h3>
          </div>
          <div class="modal-body">
            <ul class="guide-list">
              <li>· OPIc 모의고사 화면입니다.</li>
              <li>· Play 버튼을 눌러 질문을 청취하고, 자동으로 녹음이 시작됩니다.</li>
              <li>· 5초 이내에 Replay 버튼을 누르면 질문을 다시 들을 수 있습니다 (1회만).</li>
              <li>· 7번 문항 이후 난이도 재조정이 진행됩니다.</li>
              <li>· 모든 문항에 성실히 답변해주세요.</li>
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

.exam-page {
  min-height: 100vh;
  background: #ffffff;
  color: #1e293b;
  padding: 40px 20px;
  font-family: 'Noto Sans KR', sans-serif;
}

.dark-mode {
  background: #0f172a;
  color: #f1f5f9;
}

/* Header */
.exam-header {
  max-width: 1280px;
  margin: 0 auto 40px;
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

/* Step Progress */
.step-progress {
  display: flex;
  height: 48px;
  margin-bottom: 30px;
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

.step.active {
  background: #FFD700 !important;
  color: #1e293b !important;
  font-weight: bold;
}

.step.completed {
  background: #e2e8f0;
  color: #64748b;
}

.dark-mode .step { background: #1e293b; }
.dark-mode .step.active { background: #FFD700 !important; }
.dark-mode .step.completed { background: #334155; }

.step-content { display: flex; flex-direction: column; align-items: center; }
.step-number { font-weight: 700; display: flex; align-items: center; gap: 4px; }
.step-label { font-size: 10px; }
.check-icon { font-size: 14px !important; }

.page-title {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 8px;
  color: #1e293b;
}

.dark-mode .page-title {
  color: #f1f5f9;
}

.question-header {
  font-size: 20px;
  font-weight: 600;
  color: #64748b;
  margin-bottom: 32px;
}

.dark-mode .question-header {
  color: #94a3b8;
}

/* Main Content */
.exam-main {
  max-width: 1280px;
  margin: 0 auto;
}

.exam-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
}

@media (max-width: 968px) {
  .exam-container {
    grid-template-columns: 1fr;
  }
}

/* 비디오 섹션 */
.video-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.video-card {
  background: #f8f9fa;
  border-radius: 16px;
  overflow: hidden;
  aspect-ratio: 4/5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dark-mode .video-card {
  background: #1e293b;
}

.avatar-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 컨트롤 */
.controls {
  display: flex;
  align-items: center;
  gap: 16px;
}

.control-btn {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  border: none;
  background: #FFD700;
  color: #1e293b;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 4px 12px rgba(255, 215, 0, 0.3);
}

.control-btn:hover:not(:disabled) {
  background: #E6C200;
  transform: scale(1.05);
}

.control-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.control-btn .material-icons {
  font-size: 32px;
}

.playback-bar {
  flex: 1;
  height: 8px;
  background: #e2e8f0;
  border-radius: 4px;
  overflow: hidden;
}

.dark-mode .playback-bar {
  background: #334155;
}

.playback-track {
  height: 100%;
  background: #FFD700;
  width: 0%;
  transition: width 0.3s;
}

/* 상태 표시 */
.status-indicator {
  text-align: center;
  padding: 12px;
  background: #f1f5f9;
  border-radius: 8px;
  font-weight: 500;
  color: #64748b;
}

.dark-mode .status-indicator {
  background: #1e293b;
  color: #94a3b8;
}

.status-indicator.status-playing {
  background: #dbeafe;
  color: #1e40af;
}

.status-indicator.status-recording {
  background: #fce7f3;
  color: #be123c;
  animation: pulse 1.5s infinite;
}

.status-indicator.status-replay {
  background: #fef3c7;
  color: #92400e;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

/* 오른쪽 정보 섹션 */
.info-section-right {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  margin: 0;
}

.question-progress {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  list-style: none;
  padding: 0;
  margin: 0;
}

.question-progress li {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  color: #64748b;
  transition: all 0.2s;
}

.dark-mode .question-progress li {
  background: #1e293b;
  color: #94a3b8;
}

.question-progress li.active {
  background: #FFD700;
  color: #1e293b;
  transform: scale(1.1);
}

.question-progress li.completed {
  background: #e2e8f0;
  color: #10b981;
}

.dark-mode .question-progress li.completed {
  background: #334155;
}

/* 알림 박스 */
.alert-box {
  padding: 16px;
  border-radius: 12px;
  line-height: 1.6;
}

.alert-box.blue {
  background: #dbeafe;
  color: #1e40af;
  border: 2px solid #3b82f6;
}

.alert-box.purple {
  background: #f3e8ff;
  color: #6b21a8;
  border: 2px solid #a855f7;
}

.dark-mode .alert-box.blue {
  background: #1e3a8a;
  color: #bfdbfe;
}

.dark-mode .alert-box.purple {
  background: #581c87;
  color: #e9d5ff;
}

/* 녹음 타이머 */
.recording-timer {
  padding: 24px;
  background: #fce7f3;
  border: 2px solid #f472b6;
  border-radius: 12px;
  text-align: center;
}

.dark-mode .recording-timer {
  background: #831843;
  border-color: #f472b6;
}

.recording-timer h3 {
  font-size: 16px;
  margin-bottom: 12px;
}

.timer-display {
  font-size: 32px;
  font-weight: 700;
  color: #be123c;
  margin-bottom: 16px;
}

.dark-mode .timer-display {
  color: #fda4af;
}

.stop-btn {
  padding: 12px 32px;
  background: #dc2626;
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.stop-btn:hover {
  background: #b91c1c;
}

/* 모달 */
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
  max-width: 600px;
  width: 90%;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.dark-mode .modal-card {
  background: #1e293b;
}

.modal-header {
  padding: 24px;
  border-bottom: 1px solid #e2e8f0;
}

.dark-mode .modal-header {
  border-bottom-color: #374155;
}

.modal-header h3 {
  font-size: 20px;
  font-weight: 700;
  margin: 0;
}

.modal-body {
  padding: 24px;
}

.modal-text {
  text-align: center;
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 24px;
  color: #64748b;
}

.dark-mode .modal-text {
  color: #94a3b8;
}

.difficulty-options {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.difficulty-btn {
  padding: 16px;
  background: #f1f5f9;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.dark-mode .difficulty-btn {
  background: #0f172a;
  border-color: #334155;
  color: #f1f5f9;
}

.difficulty-btn:hover {
  background: #FFD700;
  border-color: #FFD700;
  color: #1e293b;
  transform: scale(1.05);
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

/* Animations */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>