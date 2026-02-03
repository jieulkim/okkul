<script setup>
import { ref, computed, onMounted, onUnmounted, inject } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { examApi } from '@/api';
import okkulPng from '@/assets/images/okkul.png';

const router = useRouter();
const route = useRoute();

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
const initialDifficulty = ref(1); // 초기 난이도 상태 추가
const isSaving = ref(false); // 저장 중 상태 추가

// 제약 조건 관련 상태
const playCount = ref(0); // 재생 횟수 (0: 초기, 1: 1회 재생, 2: 2회 재생/완료)
const canReplay = ref(false); // 다시 듣기 가능 여부 (5초 윈도우)
const isSubmitted = ref(false); // 답변 제출 완료 여부 (재녹음 방지)
let replayTimer = null; // 다시 듣기 5초 타이머

let mediaRecorder = null;

let audioChunks = [];
let recordingTimer = null;
let volumeMonitor = null;
const totalExamTime = ref(0); // 전체 시험 경과 시간
let totalTimer = null; // 전체 타이머

// 포맷팅된 시간 (전체 시험 시간)
const formattedTotalTime = computed(() => {
  const mins = Math.floor(totalExamTime.value / 60);
  const secs = totalExamTime.value % 60;
  return `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
});

// 포맷팅된 시간 (녹음 시간)
const formattedRecordingTime = computed(() => {
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

// 백엔드 레이스 컨디션 대응: 문제 생성 폴링
const pollForQuestions = async (targetExamId, retries = 5, delay = 1000) => {
  for (let i = 0; i < retries; i++) {
    try {
      console.log(`[ExamQuestionView] 질문 생성 확인 중... (${i + 1}/${retries})`);
      const response = await examApi.getExamInfo(targetExamId);
      const examData = response.data;
      
      if (examData && examData.questions && examData.questions.length > 0) {
        console.log(`[ExamQuestionView] 질문 생성 완료! (${examData.questions.length}개)`);
        
        questions.value = examData.questions;
        totalQuestions.value = examData.totalQuestions || 15;
        currentQuestionIndex.value = 0;
        
        // 문제 로드 후 저장
        saveProgress();
        return;
      }
    } catch (e) {
      console.warn(`[ExamQuestionView] 폴링 중 에러:`, e);
    }
    
    // 대기
    await new Promise(resolve => setTimeout(resolve, delay));
  }
  
  throw new Error('문제를 불러오지 못했습니다. (Timeout)');
};

// 시험 시작/재개
const initializeExam = async () => {
  try {
    isLoading.value = true;
    
    // 1. examId 먼저 확보
    const { examId: queryExamId } = route.query;
    if (queryExamId) {
      examId.value = parseInt(queryExamId);
    } else {
      // 쿼리에 없으면 로컬스토리지나 setupData 등에서 추론해야 할 수도 있으나,
      // 현재 흐름상 query로 setupView가 넘겨줌.
      // 없을 경우 에러 처리
      console.error('[ExamQuestionView] examId가 없습니다.');
    }

    // 2. 로컬 스토리지에서 진행 상황 확인
    // examId.value가 있어야 키를 만들 수 있음
    let savedData = null;
    if (examId.value) {
      savedData = localStorage.getItem(`exam_${examId.value}`);
    }
    
    // 설문 데이터 확인 (이어하기가 아닐 때 필요)
    const setupData = localStorage.getItem('setupData');
    
    if (savedData) {
      // 이어하기
      const data = JSON.parse(savedData);
      questions.value = data.questions;
      currentQuestionIndex.value = data.currentIndex;
      totalQuestions.value = data.totalQuestions;
      initialDifficulty.value = data.initialDifficulty || 1;
      
      console.log('[ExamQuestionView] 저장된 진행 상황 로드:', data);
    } else {
      // 처음 시작
      if (!examId.value) {
         throw new Error("Exam ID not found");
      }
      
      // 설문에서 설정한 기본 난이도 가져오기
      if (setupData) {
        const parsedSetup = JSON.parse(setupData);
        if (parsedSetup.initialDifficulty) {
          initialDifficulty.value = parsedSetup.initialDifficulty;
        }
      }
      
      console.log('[ExamQuestionView] 새 시험 시작. 난이도:', initialDifficulty.value);
      
      // 첫 7문제 로드 (백엔드 레이스 컨디션 대응 폴링)
      await pollForQuestions(examId.value);
    }
    
    isLoading.value = false;
    
  } catch (error) {
    console.error('[ExamQuestionView] 시험 초기화 실패:', error);
    errorMessage.value = '시험을 불러오는데 실패했습니다.';
    alert('시험을 불러오는데 실패했습니다.');
    router.push('/exam');
  }
};

// 오디오 재생
const togglePlay = () => {
  if (!currentQuestion.value?.audioUrl) return;
  
  // 이미 재생 중이면 정지 (수동 정지)
  if (isPlaying.value && currentAudio.value) {
    currentAudio.value.pause();
    isPlaying.value = false;
    return;
  }

  // 재생 시작 조건 체크
  // 1. 이미 2번 들었으면 불가
  if (playCount.value >= 2) {
    return;
  }
  
  // 2. 1번 들었는데 5초가 지났으면 불가
  if (playCount.value === 1 && !canReplay.value) {
    return;
  }

  // 재생 시작
  if (currentAudio.value) {
    currentAudio.value.pause();
  }
  
  currentAudio.value = new Audio(currentQuestion.value.audioUrl);
  
  // onended 설정 - 질문이 끝난 후 5초 타이머 시작
  currentAudio.value.onended = () => {
    isPlaying.value = false;
    
    // 첫 재생이 끝났을 때만 5초 타이머 시작
    if (playCount.value === 1) {
      canReplay.value = true;
      if (replayTimer) clearTimeout(replayTimer);
      
      replayTimer = setTimeout(() => {
        canReplay.value = false;
      }, 5000); // 질문 종료 후 5초간만 다시 듣기 허용
    }
  };

  // 재생 시작 (Promise 에러 핸들링 추가)
  currentAudio.value.play()
    .then(() => {
      isPlaying.value = true;
      playCount.value++; // 카운트 증가
    })
    .catch(err => {
      console.warn('[ExamQuestionView] 오디오 재생 실패 (유효하지 않은 URL 등):', err);
      isPlaying.value = false;
    });
};

// 문제 변경 시 상태 초기화 (제약 조건 리셋)
const resetQuestionState = () => {
  playCount.value = 0;
  canReplay.value = false;
  isSubmitted.value = false;
  
  if (replayTimer) clearTimeout(replayTimer);
  
  resetRecordingState();
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
    console.error('[ExamQuestionView] 녹음 시작 실패:', error);
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
      console.error('[ExamQuestionView] 현재 문제 정보가 없습니다.');
      return;
    }
    
    isSaving.value = true; // 저장 시작 표시

    console.log('[ExamQuestionView] 답변 제출:', {
      examId: examId.value,
      questionOrder: currentQuestion.value.order,
      audioBlobSize: audioBlob.size
    });

    const formData = new FormData();
    formData.append('file', audioBlob, 'answer.webm'); // 필드명: audioFile -> file 수정
    formData.append('sttText', ''); // 필수 필드 추가
    formData.append('duration', String(recordingTime.value)); // 실제 녹음 시간 전송
    
    // API 호출: examApi.submitAnswer(examId, questionOrder, formData)
    await examApi.submitAnswer(
      examId.value,
      currentQuestion.value.order, // questionOrder 파라미터
      formData
    );
    
    console.log('[ExamQuestionView] 답변 제출 성공');
    
    isSubmitted.value = true; // 제출 완료 상태 설정 (재녹음 방지)
    
    // 진행 상황 저장
    saveProgress();
    
  } catch (error) {
    console.error('[ExamQuestionView] 답변 제출 실패:', error);
    if (error.response) {
      console.error('[ExamQuestionView] 에러 응답:', error.response.data);
    }
    alert('답변 제출에 실패했습니다.');
  } finally {
    isSaving.value = false; // 저장 종료
  }
};

// 다음 문제
const goNext = () => {
  if (currentQuestionIndex.value === 6) {
    // 7번 문제 종료 - 난이도 재조정
    showRelevelModal.value = true;
  } else if (currentQuestionIndex.value < questions.value.length - 1) {
    currentQuestionIndex.value++;
    resetQuestionState(); // 문제 상태 초기화 (제약 조건 포함)
    saveProgress(); // 진행상황 저장
  } else {
    // 시험 종료
    completeExam();
  }
};

// 난이도 재조정
const setRelevel = async (choice) => {
  const difficultyMap = {
    easy: -1,
    same: 0,
    hard: 1
  };
  
  // 저장된 초기 난이도 사용 (state variable)
  const baseLevel = initialDifficulty.value;

  // 절대 레벨 계산 (기본 레벨 + 변화량)
  const targetLevel = baseLevel + difficultyMap[choice];
  
  // 유효성 검사 (1~6 범위 - 비즈니스 로직상 +/- 1만 허용되므로 이미 map으로 제한됨)
  // 단, 결과가 1 미만이나 6 초과가 되지 않도록 방어
  const finalLevel = Math.max(1, Math.min(6, targetLevel));

  adjustedDifficulty.value = finalLevel;
  showRelevelModal.value = false;
  
  try {
    console.log('[ExamQuestionView] 난이도 재조정 요청:', {
      examId: examId.value,
      initialDifficulty: baseLevel,
      choice,
      adjustedDifficulty: adjustedDifficulty.value
    });
    
    // API 호출: examApi.updateAdjustedDifficulty(examId, { adjustedDifficulty })
    const response = await examApi.updateAdjustedDifficulty(
      examId.value,
      { adjustedDifficulty: adjustedDifficulty.value },
    );
    
    console.log('[ExamQuestionView] 난이도 재조정 응답:', response.data);
    
    // 나머지 문제 추가
    // 현재 문제까지 유지하고, 이후 문제(난이도 조절 전 생성된 더미/이전 문제들)는 제거
    const currentQuestions = questions.value.slice(0, currentQuestionIndex.value + 1);
    const newQuestions = response.data.questions || [];
    
    // 전체 문제 수가 15개를 넘지 않도록 조정 (기존 문제 + 새 문제 합쳐서 15개까지만)
    const combinedQuestions = [...currentQuestions, ...newQuestions];
    questions.value = combinedQuestions.slice(0, 15);
    
    console.log('[ExamQuestionView] 문제 추가 완료:', {
      totalQuestions: questions.value.length,
      newQuestions: newQuestions.length
    });
    
    currentQuestionIndex.value++;
    resetQuestionState();
    saveProgress();
    
  } catch (error) {
    console.error('[ExamQuestionView] 난이도 재조정 실패:', error);
    if (error.response) {
      console.error('[ExamQuestionView] 에러 응답:', error.response.data);
    }
    alert('난이도 재조정에 실패했습니다.');
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
    totalQuestions: totalQuestions.value,
    initialDifficulty: initialDifficulty.value,
    timestamp: new Date().toISOString()
  };
  
  localStorage.setItem(`exam_${examId.value}`, JSON.stringify(data));
  localStorage.setItem('incompleteExam', JSON.stringify({
    examId: examId.value,
    currentQuestion: currentQuestionIndex.value + 1,
    remainingTime: formattedTotalTime.value
  }));
  
  console.log('[ExamQuestionView] 진행상황 저장:', data);
};

// 시험 종료
const completeExam = async () => {
  if (!confirm('시험을 종료하시겠습니까?')) {
    return;
  }
  
  try {
    console.log('[ExamQuestionView] 시험 종료 요청:', examId.value);
    
    // API 호출: examApi.completeExam(examId)
    await examApi.completeExam(examId.value);
    
    console.log('[ExamQuestionView] 시험 종료 성공');
    
    // 로컬스토리지 정리
    localStorage.removeItem(`exam_${examId.value}`);
    localStorage.removeItem('incompleteExam');
    
    alert('시험이 종료되었습니다. AI 분석이 시작됩니다.');
    router.push({ path: '/exam/feedback', query: { examId: examId.value } });
    
  } catch (error) {
    console.error('[ExamQuestionView] 시험 종료 실패:', error);
    if (error.response) {
      console.error('[ExamQuestionView] 에러 응답:', error.response.data);
    }
    alert('시험 종료에 실패했습니다.');
  }
};

// 시험 나가기
const exitExam = () => {
  if (confirm('시험을 종료하고 나가시겠습니까? 진행 상황이 저장됩니다.')) {
    saveProgress();
    router.push('/exam');
  }
};

onMounted(() => {
  initializeExam().then(() => {
    // 첫 문제에 대한 타이머 시작
    resetQuestionState();
    
    // 전체 시험 타이머 시작
    totalTimer = setInterval(() => {
      totalExamTime.value++;
    }, 1000);
  });
});

onUnmounted(() => {
  if (mediaRecorder && isRecording.value) {
    stopRecording();
  }
  if (currentAudio.value) {
    currentAudio.value.pause();
  }
  clearInterval(recordingTimer);
  clearInterval(volumeMonitor);
  clearInterval(totalTimer); // 전체 타이머 정리
  if (replayTimer) clearTimeout(replayTimer);
});
</script>

<template>
  <div class="exam-container">
    <!-- 로딩 상태 -->
    <div v-if="isLoading" class="loading-overlay">
      <div class="loading-spinner"></div>
      <p>시험 데이터를 불러오는 중...</p>
    </div>

    <!-- 에러 상태 -->
    <div v-else-if="errorMessage" class="error-container">
      <p>{{ errorMessage }}</p>
      <button @click="router.push('/exam')" class="back-to-exam-btn">돌아가기</button>
    </div>

    <!-- 메인 화면 -->
    <div v-else class="exam-content">
      <!-- 헤더 -->
      <header class="exam-header">
        <button @click="exitExam" class="exit-btn">
          <span class="material-icons">close</span>
          나가기
        </button>
        <!-- <div class="question-number">
          Question {{ currentQuestionIndex + 1 }} / {{ totalQuestions }}
        </div> -->
        <div class="time-display">
          <span class="material-icons">timer</span>
          {{ formattedTotalTime }}
        </div>
      </header>

      <!-- 메인 콘텐츠 -->
      <main class="content-wrapper">
        <!-- 질문 섹션 -->
        <section class="question-section">
          <div class="question-card">
              <div class="question-header">
                <h2>Question {{ currentQuestionIndex + 1 }} of {{ totalQuestions }}</h2>
              </div>
              <div class="audio-controls" v-if="currentQuestion?.audioUrl">
                <img :src="okkulPng" alt="OKKUL" class="okkul-img">
                <button @click="togglePlay" class="play-button" 
                        :class="{ playing: isPlaying }"
                        :disabled="!isPlaying && (playCount >= 2 || (playCount === 1 && !canReplay))">
                  <span class="material-icons">{{ isPlaying ? 'stop' : 'play_arrow' }}</span>
                  {{ isPlaying ? 'Stop' : (playCount === 0 ? 'Play Audio' : (playCount === 1 && canReplay ? '다시 듣기' : 'Played')) }}
                </button>
              </div>
          </div>
        </section>

        <!-- 녹음 섹션 -->
        <section class="recording-section">
          <div class="recording-card">
            <div class="recording-header">
              <!-- 저장 중 표시 -->
              <div v-if="isSaving" class="status-indicator saving">
                <span class="material-icons spin">sync</span>
                SAVING...
              </div>
              <!-- 녹음 중/대기 표시 -->
              <div v-else class="status-indicator" :class="{ recording: isRecording, submitted: isSubmitted }">
                <span class="status-dot"></span>
                {{ isSubmitted ? 'SUBMITTED' : (isRecording ? 'RECORDING' : 'READY') }}
              </div>
              <div class="recording-timer" v-if="!isSubmitted">
                <span class="material-icons" style="font-size: 20px; color: var(--text-secondary);">schedule</span>
                {{ formattedRecordingTime }}
              </div>
            </div>

            <!-- 볼륨 미터 -->
            <div class="volume-meter">
              <div v-for="i in 10" :key="i" 
                   class="volume-bar" 
                   :class="{
                     active: i <= volumeLevel,
                     low: i <= volumeLevel && i <= 4,
                     mid: i <= volumeLevel && i > 4 && i <= 7,
                     high: i <= volumeLevel && i > 7
                   }">
              </div>
            </div>
            <!-- 녹음 컨트롤 -->
            <div class="recording-controls">
              <button v-if="!isRecording" @click="startRecording" class="record-btn" :disabled="isSubmitted">
                <span class="material-icons">{{ isSubmitted ? 'check' : 'mic' }}</span>
                {{ isSubmitted ? 'Submitted' : 'Recording' }}
              </button>
              <button v-else @click="stopRecording" class="stop-btn">
                <span class="material-icons">stop</span>
                Stop & Submit
              </button>
            </div>
          </div>
        </section>
      </main>

      <!-- 다음 버튼 (카드 외부로 이동하여 중앙 정렬) -->
      <div class="navigation-controls">
        <button @click="goNext" class="next-btn">
          Next Question
          <span class="material-icons">arrow_forward</span>
        </button>
      </div>
    </div>

    <!-- 난이도 재조정 모달 -->
    <div v-if="showRelevelModal" class="modal-overlay">
      <div class="modal-card">
        <div class="modal-header">
          <h3>난이도 조정</h3>
          <p class="subtitle">다음 문제들의 난이도를 선택해주세요</p>
        </div>
        <div class="difficulty-options">
          <button @click="setRelevel('easy')" class="difficulty-btn">
            <span class="material-icons">trending_down</span>
            <span class="label">쉽게</span>
          </button>
          <button @click="setRelevel('same')" class="difficulty-btn">
            <span class="material-icons">trending_flat</span>
            <span class="label">동일</span>
          </button>
          <button @click="setRelevel('hard')" class="difficulty-btn">
            <span class="material-icons">trending_up</span>
            <span class="label">어렵게</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.exam-container {
  height: calc(100vh - var(--header-height, 60px));
  min-height: 0 !important;
  background: #FFFFFF;
  margin: 0;
  padding: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* 로딩 & 에러 */
.loading-overlay, .error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  gap: 24px;
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 4px solid var(--bg-tertiary);
  border-top-color: var(--primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.back-to-exam-btn {
  padding: 12px 32px;
  background: var(--primary-color);
  color: #212529;
  border: none;
  border-radius: 12px;
  font-weight: 700;
  cursor: pointer;
}

/* 메인 콘텐츠 */
.exam-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 0 40px 16px;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
  min-height: 0;
  padding-top: 0;
  overflow-y: auto;
}

/* 헤더 */
.exam-header {
  width: 100%;
  max-width: 1280px;
  margin: 12px auto 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 32px;
  background: var(--bg-secondary);
  border-radius: 20px;
  box-shadow: var(--shadow-md);
  flex-shrink: 0;
}

.exit-btn {
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

.exit-btn:hover {
  background: #e2e8f0;
  color: var(--text-primary);
}

.question-number {
  font-size: 1.125rem;
  font-weight: 800;
  color: var(--text-primary);
}

.time-display {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 24px;
  background: var(--primary-color);
  border-radius: 10px;
  font-weight: 700;
  color: #212529;
}

/* 메인 콘텐츠 */
.content-wrapper {
  width: 100%;
  max-width: 1100px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  min-height: 0;
  align-items: start;
  flex: 1;
  min-width: 0;
}

@media (max-width: 992px) {
  .content-wrapper {
    grid-template-columns: 1fr;
  }
}

/* 질문 섹션 */
.question-section {
  display: flex;
  flex-direction: column;
}

.question-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border-primary);
  border-radius: 24px;
  padding: 40px;
  box-shadow: var(--shadow-md);
  height: 540px;
  display: flex;
  flex-direction: column;
}

.question-header {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  flex-shrink: 0;
}

.question-header h2 {
  font-size: 1.5rem;
  font-weight: 800;
  color: var(--text-primary);
  line-height: 1;
  margin: 0;
  white-space: nowrap;
  flex-shrink: 0;
}

.audio-controls {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 24px;
}

.okkul-img {
  width: 180px;
  height: auto;
}

.play-button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 14px 28px;
  background: var(--primary-color);
  border: none;
  border-radius: 16px;
  font-size: 1rem;
  font-weight: 700;
  color: #212529;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: var(--shadow-md);
  width: 180px;
  flex-shrink: 0;
}

.question-card::after {
  content: "";
  height: 80px;
  width: 100%;
  display: block;
  flex-shrink: 0;
}

.play-button:hover {
  background: var(--primary-hover);
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.play-button.playing {
  background: #ef4444;
  color: white;
}

/* 녹음 섹션 */
.recording-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border-primary);
  border-radius: 24px;
  padding: 40px;
  display: flex;
  flex-direction: column;
  box-shadow: var(--shadow-md);
  height: 540px;
}

.recording-header {
  height: 60px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-bottom: 24px;
  flex-shrink: 0;
  position: relative;
  gap: 12px;
  padding-right: 140px;
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 20px;
  background: var(--bg-tertiary);
  border-radius: 50px;
  font-weight: 700;
  color: var(--text-tertiary);
  font-size: 0.8rem;
  letter-spacing: 0.05em;
}

.status-indicator.recording {
  background: #fee2e2;
  color: #ef4444;
}

.status-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #cbd5e1;
}

.status-indicator.recording .status-dot {
  background: #ef4444;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.4; transform: scale(1.2); }
}

/* 볼륨 미터 */
.volume-meter {
  display: flex;
  justify-content: center;
  align-items: flex-end;
  gap: 6px;
  height: 60px;
  padding: 12px;
  background: var(--bg-tertiary);
  border-radius: 12px;
  margin-bottom: 16px;
  flex-shrink: 0;
  margin-top: 60px; /* Play Audio 버튼과 높이 맞추기 - 조정 */
}

.volume-bar {
  width: 10px;
  height: 6px;
  background: var(--bg-secondary);
  border-radius: 3px;
  transition: all 0.1s ease;
}

.volume-bar.active {
  height: 100%;
}

.volume-bar.active.low { background: #10b981; }
.volume-bar.active.mid { background: #fbbf24; }
.volume-bar.active.high { background: #ef4444; }

/* 녹음 컨트롤 */
.recording-controls {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.record-btn, .stop-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 14px 28px;
  border: none;
  border-radius: 16px;
  font-size: 1rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: var(--shadow-md);
  width: 180px; /* Match Play Audio button width */
  flex-shrink: 0;
}

.record-btn {
  background: var(--primary-color);
  color: #212529;
  white-space: nowrap;
}

.record-btn:hover {
  background: var(--primary-hover);
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.stop-btn {
  background: #ef4444;
  color: white;
  white-space: nowrap;
}

.stop-btn:hover {
  background: #dc2626;
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

.record-btn:disabled,
.play-button:disabled {
  background: var(--bg-tertiary);
  color: var(--text-tertiary);
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.recording-timer {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.9rem;
  font-weight: 700;
  color: var(--text-secondary);
  font-variant-numeric: tabular-nums;
}

.status-indicator.submitted {
  background: #dbeafe;
  color: #2563eb;
}

.status-indicator.submitted .status-dot {
  background: #2563eb;
}

/* 네비게이션 */
.navigation-controls {
  display: flex;
  justify-content: center;
  padding: 16px 0;
  flex-shrink: 0;
  width: 100%;
  margin-top: auto;
}

.next-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 40px;
  background: var(--primary-color);
  color: #212529;
  border: none;
  border-radius: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: var(--shadow-md);
  width: 280px;
  justify-content: center;
}

.next-btn:hover {
  background: var(--primary-hover);
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

/* 모달 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-card {
  background: white;
  border-radius: 24px;
  max-width: 600px;
  width: 90%;
  padding: 56px 48px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
  text-align: center;
}

.modal-header {
  margin-bottom: 40px;
}

.modal-header h3 {
  font-size: 2rem;
  font-weight: 800;
  margin-bottom: 16px;
  color: var(--text-primary);
}

.subtitle {
  font-size: 1rem;
  color: var(--text-secondary);
  line-height: 1.6;
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
  background: #ffffff; /* 흰색 배경으로 버튼감 살리기 */
  border: 2px solid #e2e8f0; /* 테두리 추가 */
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06); /* 그림자 강화 */
}

.difficulty-btn:hover {
  background: var(--bg-secondary);
  border-color: var(--primary-color);
  transform: translateY(-4px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
}

.difficulty-btn:active {
  transform: translateY(0);
  box-shadow: none;
}

.difficulty-btn.active {
  background: var(--primary-light);
  border-color: var(--primary-color);
  color: #8B7300;
}

.difficulty-btn .label {
  font-weight: 700;
  font-size: 0.95rem;
}

/* 저장 중 스타일 */
.status-indicator.saving {
  background: #dbeafe;
  color: #2563eb;
}

.spin {
  animation: spin 1s linear infinite;
  font-size: 1.1rem;
}
</style>