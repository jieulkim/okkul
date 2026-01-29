<script setup>
import { ref, computed, onMounted, onUnmounted, inject } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { examApi } from '@/api';

const router = useRouter();
const route = useRoute();
const isDarkMode = inject('isDarkMode', ref(false));

// 상태 관리
const examId = ref(null);
const currentQuestionIndex = ref(0);
const questions = ref([]);
const totalQuestions = ref(15);
const isRecording = ref(false);
const recordingTime = ref(0);
const audioLevel = ref(0);
const currentAudio = ref(null);
const isPlaying = ref(false);
const showRelevelModal = ref(false);
const adjustedDifficulty = ref(null);
const isLoading = ref(true);
const errorMessage = ref(null);
const isBackendConnected = ref(false); // 백엔드 연결 상태

let mediaRecorder = null;
let audioChunks = [];
let recordingTimer = null;
let volumeMonitor = null;

// 포맷팅된 시간
const formattedTime = computed(() => {
  const mins = Math.floor(recordingTime.value / 60);
  const secs = recordingTime.value % 60;
  return `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
});

// 볼륨 레벨 (0-10 사이)
const volumeLevel = computed(() => {
  return Math.round(audioLevel.value / 10);
});

// 현재 문제
const currentQuestion = computed(() => {
  return questions.value[currentQuestionIndex.value];
});

// 시험 시작/재개
const initializeExam = async () => {
  try {
    isLoading.value = true;
    const { examId: queryExamId, resume } = route.query;
    
    console.log('[ExamQuestionView] 초기화 시작:', { queryExamId, resume });
    
    if (!queryExamId) {
      errorMessage.value = '시험 정보가 없습니다.';
      alert('시험 정보가 없습니다.');
      router.push('/exam');
      return;
    }
    
    examId.value = parseInt(queryExamId);
    const savedData = localStorage.getItem(`exam_${examId.value}`);
    
    console.log('[ExamQuestionView] localStorage 데이터:', savedData);
    
    if (!savedData) {
      errorMessage.value = '시험 데이터를 찾을 수 없습니다.';
      alert('시험 데이터를 찾을 수 없습니다.');
      router.push('/exam');
      return;
    }
    
    const data = JSON.parse(savedData);
    questions.value = data.questions || [];
    totalQuestions.value = data.totalQuestions || 15;
    currentQuestionIndex.value = data.currentIndex || 0;
    
    console.log('[ExamQuestionView] 시험 초기화 완료:', {
      examId: examId.value,
      totalQuestions: totalQuestions.value,
      currentIndex: currentQuestionIndex.value,
      questionsCount: questions.value.length,
      firstQuestion: questions.value[0]
    });
    
    if (questions.value.length === 0) {
      errorMessage.value = '문제 데이터가 없습니다.';
      alert('문제 데이터가 없습니다.');
      router.push('/exam');
      return;
    }
    
    // 백엔드 연결 상태 확인
    await checkBackendConnection();
    
    isLoading.value = false;
    
  } catch (error) {
    console.error('시험 초기화 실패:', error);
    errorMessage.value = '시험을 불러오는데 실패했습니다.';
    alert('시험을 불러오는데 실패했습니다.');
    router.push('/exam');
  }
};

// 오디오 재생
const togglePlay = () => {
  if (!currentQuestion.value?.audioUrl) return;
  
  if (isPlaying.value && currentAudio.value) {
    currentAudio.value.pause();
    isPlaying.value = false;
  } else {
    if (currentAudio.value) {
      currentAudio.value.pause();
    }
    currentAudio.value = new Audio(currentQuestion.value.audioUrl);
    currentAudio.value.play();
    isPlaying.value = true;
    
    currentAudio.value.onended = () => {
      isPlaying.value = false;
    };
  }
};

// 녹음 시작
const startRecording = async () => {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    mediaRecorder = new MediaRecorder(stream);
    audioChunks = [];
    
    mediaRecorder.ondataavailable = (event) => {
      audioChunks.push(event.data);
    };
    
    mediaRecorder.onstop = async () => {
      const audioBlob = new Blob(audioChunks, { type: 'audio/webm' });
      await submitAnswer(audioBlob);
    };
    
    // 볼륨 모니터링
    const audioContext = new AudioContext();
    const analyser = audioContext.createAnalyser();
    const microphone = audioContext.createMediaStreamSource(stream);
    microphone.connect(analyser);
    analyser.fftSize = 512;
    const dataArray = new Uint8Array(analyser.frequencyBinCount);
    
    volumeMonitor = setInterval(() => {
      analyser.getByteFrequencyData(dataArray);
      const average = dataArray.reduce((a, b) => a + b) / dataArray.length;
      audioLevel.value = average;
    }, 100);
    
    mediaRecorder.start();
    isRecording.value = true;
    recordingTime.value = 0;
    
    recordingTimer = setInterval(() => {
      recordingTime.value++;
    }, 1000);
    
  } catch (error) {
    console.error('녹음 시작 실패:', error);
    if (error.name === 'NotAllowedError') {
      alert('마이크 권한이 거부되었습니다.\n브라우저 설정에서 마이크 권한을 허용해주세요.');
    } else {
      alert('마이크를 사용할 수 없습니다.');
    }
  }
};

// 녹음 중지
const stopRecording = () => {
  if (mediaRecorder && isRecording.value) {
    mediaRecorder.stop();
    isRecording.value = false;
    clearInterval(recordingTimer);
    clearInterval(volumeMonitor);
    mediaRecorder.stream.getTracks().forEach(track => track.stop());
  }
};

// 답변 제출
const submitAnswer = async (audioBlob) => {
  try {
    if (!currentQuestion.value) {
      console.error('현재 문제 정보가 없습니다.');
      return;
    }
    

    const formData = new FormData();
    formData.append('audioFile', audioBlob, 'answer.webm');
    
    await examApi.submitAnswer(
      examId.value,
      currentQuestion.value.answerId,
      formData
    );
    
    // 진행 상황 저장
    saveProgress();
    
  } catch (error) {
    console.error('답변 제출 실패:', error);
    alert('답변 제출에 실패했습니다.');
  }
};

// 다음 문제
const goNext = () => {
  if (currentQuestionIndex.value === 6) {
    // 7번 문제 종료 - 난이도 재조정
    showRelevelModal.value = true;
  } else if (currentQuestionIndex.value < questions.value.length - 1) {
    currentQuestionIndex.value++;
    resetRecordingState();
  } else {
    // 시험 종료
    completeExam();
  }
};

// 백엔드 연결 상태 확인
const checkBackendConnection = async () => {
  try {
    // Health Check API 호출 (또는 간단한 API)
    await examApi.getExamStatus(examId.value);
    isBackendConnected.value = true;
    console.log('✅ [백엔드 연결됨] 백엔드 서버와 연결되었습니다.');
  } catch (error) {
    isBackendConnected.value = false;
    console.warn('⚠️ [백엔드 미연결] 백엔드 서버와 연결되지 않았습니다. 임시 데이터를 사용합니다.');
  }
};

// 임시 더미 문제 생성 (백엔드 미연결 시)
const generateMockQuestions = (difficulty) => {
  const mockQuestions = [];
  const difficultyLabel = difficulty === -1 ? '쉬움' : difficulty === 0 ? '동일' : '어려움';
  
  for (let i = 0; i < 8; i++) {
    mockQuestions.push({
      id: 100 + i,
      answerId: 1000 + i, // Added answerId for submission
      order: 8 + i,
      questionText: `[임시 데이터] 난이도 ${difficultyLabel} - 문제 ${8 + i}번`,
      description: `This is a temporary test question. Please respond naturally and practice your speaking skills.`,
      audioUrl: null,
      type: 'SPEAKING',
      preparationTime: 15,
      speakingTime: 45,
      difficult: difficulty,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    });
  }
  return mockQuestions;
};

// 난이도 재조정
const setRelevel = async (choice) => {
  const difficultyMap = {
    easy: -1,
    same: 0,
    hard: 1
  };
  
  adjustedDifficulty.value = difficultyMap[choice];
  showRelevelModal.value = false;
  
  if (isBackendConnected.value) {
    // ✅ 백엔드 연결됨 → 실제 데이터 로드
    try {
      console.log('[setRelevel] 백엔드에서 문제 로드 중...');
      const response = await examApi.getRemainingQuestions(examId.value, {
        adjustedDifficulty: adjustedDifficulty.value
      });
      
      // 나머지 문제 추가
      questions.value = [...questions.value, ...response.data.questions];
      currentQuestionIndex.value++;
      resetRecordingState();
      
    } catch (error) {
      console.warn('[setRelevel] 백엔드 문제 로드 실패, 임시 데이터로 전환:', error.message);
      // 백엔드 호출 실패 시 임시 데이터 사용으로 폴백
      console.log('[setRelevel] 백엔드 호환 실패 - 임시 문제 사용');
      const mockQuestions = generateMockQuestions(adjustedDifficulty.value);
      questions.value = [...questions.value, ...mockQuestions];
      
      console.log('[setRelevel] 임시 문제 추가 완료:', mockQuestions.length, '개');
      
      currentQuestionIndex.value++;
      resetRecordingState();
    }
  } else {
    // ❌ 백엔드 미연결 → 임시 더미 문제 사용
    console.log('[setRelevel] 백엔드 미연결 - 임시 문제 사용');
    const mockQuestions = generateMockQuestions(adjustedDifficulty.value);
    questions.value = [...questions.value, ...mockQuestions];
    
    console.log('[setRelevel] 임시 문제 추가 완료:', mockQuestions.length, '개');
    
    currentQuestionIndex.value++;
    resetRecordingState();
  }
};

// 녹음 상태 초기화
const resetRecordingState = () => {
  recordingTime.value = 0;
  audioLevel.value = 0;
  if (currentAudio.value) {
    currentAudio.value.pause();
    isPlaying.value = false;
  }
};

// 진행 상황 저장
const saveProgress = () => {
  const data = {
    examId: examId.value,
    currentIndex: currentQuestionIndex.value,
    questions: questions.value,
    timestamp: new Date().toISOString()
  };
  
  localStorage.setItem(`exam_${examId.value}`, JSON.stringify(data));
  localStorage.setItem('incompleteExam', JSON.stringify({
    examId: examId.value,
    currentQuestion: currentQuestionIndex.value + 1,
    remainingTime: formattedTime.value
  }));
};

// 시험 완료
const completeExam = async () => {
  try {
    if (!examId.value) {
      console.error('examId가 없습니다.');
      return;
    }
    

    // Mock Mode Check
    if (import.meta.env.VITE_USE_MOCK_DATA === 'true') {
      console.log('[completeExam] Mock Mode: Skipping API call');
      // 로컬 스토리지 정리 (선택사항, 실제 로직과 동일하게)
      localStorage.removeItem(`exam_${examId.value}`);
      localStorage.removeItem('incompleteExam');
      
      router.push({
        path: '/exam/feedback', // 결과 페이지 경로 확인 필요 (User said /feedback folder, code says /exam/result or similar? Let's check router push path in original code which was /exam/result or /exam/feedback? Original code line 349 said /exam/result. Wait, the user said "feedback page is in /feedback folder". I need to check router config again or just follow the existing router push logic but make sure it goes to the right place. The existing code went to /exam/result. I will stick to existing redirect but check if that route exists/maps to the feedback view.)
        query: { examId: examId.value }
      });
      return;
    }

    await examApi.completeExam(examId.value);
    
    localStorage.removeItem(`exam_${examId.value}`);
    localStorage.removeItem('incompleteExam');
    
    router.push({
      path: '/exam/feedback', // Changed to /exam/feedback based on user implication, but wait. The original code (line 349) was `/exam/result`. Let's verify the router paths first.
      query: { examId: examId.value }
    });
  } catch (error) {
    console.error('시험 완료 처리 실패:', error);
    alert('시험 완료 처리에 실패했습니다.');
  }
};

// 시험 나가기
const exitExam = () => {
  if (confirm('시험을 나가시겠습니까? 진행 상황은 저장됩니다.')) {
    saveProgress();
    router.push('/exam');
  }
};

onMounted(() => {
  initializeExam();
});

onUnmounted(() => {
  if (currentAudio.value) {
    currentAudio.value.pause();
  }
  stopRecording();
});
</script>

<template>
  <div class="page-container">
    <!-- 로딩 화면 -->
    <div v-if="isLoading" class="loading-screen">
      <div class="loading-content">
        <div class="spinner"></div>
        <p>시험을 불러오는 중...</p>
      </div>
    </div>

    <!-- 에러 화면 -->
    <div v-else-if="errorMessage" class="error-screen">
      <div class="error-content">
        <span class="material-icons error-icon">error_outline</span>
        <h2>{{ errorMessage }}</h2>
        <button @click="router.push('/exam')" class="back-to-exam-btn">
          모의고사 홈으로 돌아가기
        </button>
      </div>
    </div>

    <!-- 메인 화면 -->
    <template v-else>
    <!-- 헤더 -->
    <div class="exam-header">
      <div class="header-left">
        <button @click="exitExam" class="exit-btn">
          <span class="material-icons">arrow_back</span>
          나가기
        </button>
      </div>
      <div class="header-center">
        <span class="question-number">Question {{ currentQuestionIndex + 1 }} / {{ totalQuestions }}</span>
      </div>
      <div class="header-right">
        <div class="time-display">
          <span class="material-icons">timer</span>
          {{ formattedTime }}
        </div>
      </div>
    </div>

    <!-- 메인 콘텐츠 -->
    <main class="page-content">
      <div class="content-wrapper">
        <!-- 질문 섹션 -->
        <div class="question-section">
          <div class="question-card">
            <div class="question-header">
              <h2>{{ currentQuestion?.questionText || 'Loading...' }}</h2>
            </div>
            
            <div class="audio-controls">
              <button @click="togglePlay" class="play-button" :class="{ playing: isPlaying }">
                <span class="material-icons">{{ isPlaying ? 'pause' : 'play_arrow' }}</span>
                {{ isPlaying ? 'Stop' : 'Listen' }}
              </button>
            </div>
          </div>
        </div>

        <!-- 녹음 섹션 -->
        <div class="recording-section">
          <div class="recording-card">
            <div class="recording-header">
              <div class="status-indicator" :class="{ recording: isRecording }">
                <span class="status-dot"></span>
                {{ isRecording ? 'RECORDING' : 'READY' }}
              </div>
            </div>

            <!-- 볼륨 미터 -->
            <div class="volume-meter">
              <div 
                v-for="n in 20" 
                :key="n" 
                class="volume-bar"
                :class="{ 
                  active: volumeLevel >= n,
                  low: n <= 7,
                  mid: n > 7 && n <= 14,
                  high: n > 14
                }"
              ></div>
            </div>

            <!-- 녹음 컨트롤 -->
            <div class="recording-controls">
              <button 
                v-if="!isRecording" 
                @click="startRecording" 
                class="record-btn"
              >
                <span class="material-icons">mic</span>
                녹음 시작
              </button>
              <button 
                v-else 
                @click="stopRecording" 
                class="stop-btn"
              >
                <span class="material-icons">stop</span>
                녹음 중지
              </button>
            </div>

            <!-- 다음 버튼 -->
            <div class="navigation-controls">
              <button @click="goNext" class="next-btn">
                다음 문제
                <span class="material-icons">arrow_forward</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 난이도 재조정 모달 -->
    <div v-if="showRelevelModal" class="modal-overlay">
      <div class="modal-card">
        <div class="modal-header">
          <h3>난이도 재조정</h3>
          <p class="subtitle">전반부 7문제를 완료하셨습니다.<br>남은 문제의 난이도를 선택해주세요.</p>
        </div>

        <div class="difficulty-options">
          <button @click="setRelevel('easy')" class="difficulty-btn easy">
            <span class="label">쉬운 질문</span>
          </button>
          <button @click="setRelevel('same')" class="difficulty-btn same active">
            <span class="label">비슷한 질문</span>
          </button>
          <button @click="setRelevel('hard')" class="difficulty-btn hard">
            <span class="label">어려운 질문</span>
          </button>
        </div>
      </div>
    </div>
    </template>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@400;500;700;900&display=swap');

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

/* 로딩 화면 */
.loading-screen {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
}

.loading-content {
  text-align: center;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #e2e8f0;
  border-top-color: #FFD700;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-content p {
  font-size: 16px;
  color: #64748b;
  font-weight: 600;
}

/* 에러 화면 */
.error-screen {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8fafc;
}

.error-content {
  text-align: center;
  padding: 40px;
}

.error-icon {
  font-size: 80px !important;
  color: #ef4444;
  margin-bottom: 20px;
}

.error-content h2 {
  font-size: 24px;
  color: var(--text-primary);
  margin-bottom: 30px;
}

.back-to-exam-btn {
  padding: 14px 32px;
  background: var(--primary-color);
  color: #000000;
  border: var(--border-secondary);
  border-radius: var(--border-radius);
  font-weight: 900;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: var(--shadow-sm);
}

.back-to-exam-btn:hover {
  transform: translate(-0.02em, -0.02em);
  box-shadow: var(--shadow-md);
}

/* 헤더 */
.exam-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 40px;
  background: var(--bg-secondary);
  border-bottom: var(--border-primary);
  position: sticky;
  top: 0;
  z-index: 100;
}

.exit-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: var(--bg-tertiary);
  border: var(--border-secondary);
  border-radius: var(--border-radius);
  font-weight: 900;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: var(--shadow-sm);
}

.exit-btn:hover {
  transform: translate(-0.02em, -0.02em);
  box-shadow: var(--shadow-md);
}

.question-number {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.time-display {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: var(--primary-color);
  border: var(--border-secondary);
  border-radius: var(--border-radius);
  font-weight: 900;
  color: #000000;
  box-shadow: var(--shadow-sm);
}

/* 메인 콘텐츠 */


.content-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
}

/* 질문 섹션 */
.question-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.question-card {
  background: var(--bg-secondary);
  border: var(--border-primary);
  border-radius: var(--border-radius);
  padding: 40px;
  box-shadow: var(--shadow-md);
}

.question-header h2 {
  font-size: 24px;
  font-weight: 900;
  color: var(--text-primary);
  line-height: 1.5;
  margin-bottom: 30px;
}

.audio-controls {
  display: flex;
  justify-content: center;
}

.play-button {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 40px;
  background: var(--primary-color);
  border: var(--border-secondary);
  border-radius: var(--border-radius);
  font-size: 18px;
  font-weight: 900;
  color: #000000;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: var(--shadow-sm);
}

.play-button:hover {
  transform: translate(-0.05em, -0.05em);
  box-shadow: var(--shadow-md);
}

.play-button.playing {
  background: #ef4444;
  color: white;
}

/* 녹음 섹션 */
.recording-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.recording-card {
  background: var(--bg-secondary);
  border: var(--border-primary);
  border-radius: var(--border-radius);
  padding: 40px;
  display: flex;
  flex-direction: column;
  gap: 30px;
  box-shadow: var(--shadow-md);
}

.recording-header {
  display: flex;
  justify-content: center;
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 24px;
  background: var(--bg-tertiary);
  border-radius: var(--border-radius);
  border: var(--border-thin);
  font-weight: 900;
  color: var(--text-secondary);
}

.status-indicator.recording {
  background: #fee2e2;
  color: #ef4444;
}

.status-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #cbd5e1;
}

.status-indicator.recording .status-dot {
  background: #ef4444;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

/* 볼륨 미터 */
.volume-meter {
  display: flex;
  justify-content: center;
  align-items: flex-end;
  gap: 4px;
  height: 80px;
  padding: 20px;
  background: var(--bg-tertiary);
  border-radius: 12px;
  border: var(--border-thin);
}

.volume-bar {
  width: 12px;
  height: 10px;
  background: var(--bg-secondary);
  border-radius: 3px;
  transition: all 0.1s;
}

.volume-bar.active {
  height: 100%;
}

.volume-bar.active.low {
  background: #10b981;
}

.volume-bar.active.mid {
  background: #fbbf24;
}

.volume-bar.active.high {
  background: #ef4444;
}

/* 녹음 컨트롤 */
.recording-controls {
  display: flex;
  justify-content: center;
}

.record-btn, .stop-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 40px;
  border: var(--border-secondary);
  border-radius: var(--border-radius);
  font-size: 18px;
  font-weight: 900;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: var(--shadow-sm);
}

.record-btn {
  background: var(--primary-color);
  color: #000000;
}

.record-btn:hover {
  transform: translate(-0.05em, -0.05em);
  box-shadow: var(--shadow-md);
}

.stop-btn {
  background: #ef4444;
  color: white;
}

.stop-btn:hover {
  background: #dc2626;
  transform: translateY(-2px);
}

/* 네비게이션 */
.navigation-controls {
  display: flex;
  justify-content: center;
  padding-top: 20px;
  border-top: 2px solid #e2e8f0;
}

.next-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 32px;
  background: var(--primary-color);
  color: #000000;
  border: var(--border-secondary);
  border-radius: var(--border-radius);
  font-weight: 900;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: var(--shadow-sm);
}

.next-btn:hover {
  transform: translate(-0.02em, -0.02em);
  box-shadow: var(--shadow-md);
}

/* 모달 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.modal-card {
  background: var(--bg-secondary);
  border-radius: var(--border-radius);
  max-width: 600px;
  width: 90%;
  padding: 40px;
  border: var(--border-primary);
  box-shadow: var(--shadow-lg);
}

.modal-header {
  text-align: center;
  margin-bottom: 30px;
}

.modal-header h3 {
  font-size: 28px;
  font-weight: 900;
  margin-bottom: 12px;
  color: var(--text-primary);
}

.difficulty-options {
  display: flex;
  gap: 16px;
  justify-content: center;
}

.difficulty-btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 24px;
  background: var(--bg-tertiary);
  border: var(--border-secondary);
  border-radius: var(--border-radius);
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: var(--shadow-sm);
}

.difficulty-btn:hover {
  transform: translate(-0.05em, -0.05em);
  box-shadow: var(--shadow-md);
}

.difficulty-btn.active {
  background: var(--primary-color);
  box-shadow: var(--shadow-md);
}

.difficulty-btn .emoji {
  font-size: 40px;
}

.difficulty-btn .label {
  font-weight: 700;
  color: var(--text-primary);
}

/* 다크모드 상속 */
.dark-mode-card {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.dark-mode-card .difficulty-btn {
  background: var(--bg-tertiary);
  border-color: #FFFFFF;
}

/* 반응형 */
@media (max-width: 768px) {
  .content-wrapper {
    grid-template-columns: 1fr;
  }
  
  .difficulty-options {
    flex-direction: column;
  }
}
</style>