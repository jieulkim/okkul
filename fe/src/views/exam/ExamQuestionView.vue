<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRouter, useRoute, onBeforeRouteLeave } from 'vue-router';
import { examApi } from '@/api';
import okkulPng from '@/assets/images/okkul.png';

const router = useRouter();
const route = useRoute();

// 상태 관리
const examId = ref(null);
const currentQuestionIndex = ref(0);
const questions = ref([]);
const totalQuestions = ref(0);
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
const sttResult = ref(""); // STT 결과를 저장할 ref 추가
let finalTranscriptAccumulated = ref(""); // 최종 확정된 STT 결과를 누적
let recognition = null; // Web Speech API SpeechRecognition 객체
const showConfirmNextModal = ref(false); // 답변 없이 다음으로 넘어갈지 묻는 모달
const forceNextQuestion = ref(false); // 모달 확인 후 다음 문제로 넘어갈지 결정
const isNavigatingAwayIntentionally = ref(false); // 의도적인 페이지 이동인지 확인하는 플래그
const isNoiseDetectionEnabled = ref(false); // 실시간 소음 모드 상태

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

// 현재 난이도 레벨 (1-6)
const currentDifficultyLevel = computed(() => {
  return adjustedDifficulty.value || initialDifficulty.value;
});

const currentQuestion = computed(() => {
  return questions.value[currentQuestionIndex.value];
});

// 마지막 문제 여부 확인 (총 문항 수에 도달했는지 확인)
const isLastQuestion = computed(() => {
  return questions.value.length > 0 && (currentQuestionIndex.value + 1) === totalQuestions.value;
});

// 난이도별 총 문항 수 계산 (Lv1-2: 12문항, Lv3-6: 15문항)
const getTotalQuestionsByLevel = (level) => {
  return level >= 3 ? 15 : 12;
};

// 백엔드 레이스 컨디션 대응: 문제 생성 폴링
const pollForQuestions = async (targetExamId, retries = 5, delay = 1000) => {
  for (let i = 0; i < retries; i++) {
    try {
      console.log(`[ExamQuestionView] 질문 생성 확인 중... (${i + 1}/${retries})`);
      const response = await examApi.getExamInfo(targetExamId);
      const examData = response.data;
      
      if (examData && examData.questions && examData.questions.length > 0) {
        console.log(`[ExamQuestionView] 질문 생성 완료! (${examData.questions.length}개)`);
        
        // 난이도에 따라 총 문항 수 결정
        const currentLevel = examData.adjustedDifficulty || examData.initialDifficulty || initialDifficulty.value;
        totalQuestions.value = getTotalQuestionsByLevel(currentLevel);
        
        // 총 문항 수에 맞게 자르기 (안전장치)
        questions.value = examData.questions.slice(0, totalQuestions.value);
        
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
      console.error('[ExamQuestionView] examId가 없습니다.');
    }

    let savedData = null;
    if (examId.value) {
      savedData = localStorage.getItem(`exam_${examId.value}`);
    }
    
    const setupData = localStorage.getItem('setupData');
    
    if (savedData) {
      const data = JSON.parse(savedData);
      questions.value = data.questions;
      currentQuestionIndex.value = data.currentIndex;
      adjustedDifficulty.value = data.adjustedDifficulty || null;
      initialDifficulty.value = data.initialDifficulty || 1;
      totalQuestions.value = getTotalQuestionsByLevel(adjustedDifficulty.value || initialDifficulty.value);
      
      console.log('[ExamQuestionView] 저장된 진행 상황 로드:', data);
      
      // 이미 제출된 상태였다면 처리
      if (data.isSubmitted) {
        isSubmitted.value = true;
        
        // 난이도 재조정 문제(index 6)나 마지막 문제가 아니라면 자동으로 다음 문제로 이동
        const isIndexSix = currentQuestionIndex.value === 6;
        const isLast = (currentQuestionIndex.value + 1) === totalQuestions.value;
        
        if (!isIndexSix && !isLast) {
          currentQuestionIndex.value++;
          isSubmitted.value = false;
          console.log('[ExamQuestionView] 이미 제출된 문제이므로 다음 문제로 자동 이동합니다.');
          saveProgress(); // 이동된 상태 저장
        }
      }
    } else {
      if (!examId.value) {
         throw new Error("Exam ID not found");
      }
      
      if (setupData) {
        const parsedSetup = JSON.parse(setupData);
        if (parsedSetup.initialDifficulty) {
          initialDifficulty.value = parsedSetup.initialDifficulty;
        }
      }
      
      console.log('[ExamQuestionView] 새 시험 시작. 난이도:', initialDifficulty.value);
      
      totalQuestions.value = getTotalQuestionsByLevel(initialDifficulty.value);
      
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
  
  if (isPlaying.value && currentAudio.value) {
    currentAudio.value.pause();
    isPlaying.value = false;
    return;
  }

  if (playCount.value >= 2) {
    return;
  }
  
  if (playCount.value === 1 && !canReplay.value) {
    return;
  }

  if (currentAudio.value) {
    currentAudio.value.pause();
  }
  
  currentAudio.value = new Audio(currentQuestion.value.audioUrl);
  
  currentAudio.value.onended = () => {
    isPlaying.value = false;
    
    if (playCount.value === 1) {
      canReplay.value = true;
      if (replayTimer) clearTimeout(replayTimer);
      
      replayTimer = setTimeout(() => {
        canReplay.value = false;
      }, 5000);
    }
  };

  currentAudio.value.play()
    .then(() => {
      isPlaying.value = true;
      playCount.value++;
    })
    .catch(err => {
      console.warn('[ExamQuestionView] 오디오 재생 실패 (유효하지 않은 URL 등):', err);
      isPlaying.value = false;
    });
};

// 문제 변경 시 상태 초기화
const resetQuestionState = () => {
  playCount.value = 0;
  canReplay.value = false;
  isSubmitted.value = false;
  sttResult.value = "";
  finalTranscriptAccumulated.value = "";
  
  if (replayTimer) clearTimeout(replayTimer);
  
  resetRecordingState();
};

// 녹음 시작
const startRecording = async () => {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    
    // 오디오 재생 중이면 중지 및 다시 듣기 불가 처리
    if (currentAudio.value) {
      currentAudio.value.pause();
      isPlaying.value = false;
    }
    canReplay.value = false;
    playCount.value = 2; // 재생 기회 소진 (버튼 비활성화)
    
    mediaRecorder = new MediaRecorder(stream);
    audioChunks = [];
    sttResult.value = "";

    if (!recognition) {
      const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
      if (!SpeechRecognition) {
        alert('STT를 지원하지 않는 브라우저입니다.');
        return;
      }
      recognition = new SpeechRecognition();
      recognition.continuous = true;
      recognition.interimResults = true;
      recognition.lang = 'en-US';
    }

    recognition.onresult = (event) => {
      let interimTranscript = '';
      for (let i = event.resultIndex; i < event.results.length; ++i) {
        if (event.results[i].isFinal) {
          finalTranscriptAccumulated.value += event.results[i][0].transcript + ' ';
        } else {
          interimTranscript += event.results[i][0].transcript;
        }
      }
      sttResult.value = finalTranscriptAccumulated.value + interimTranscript;
    };

    recognition.onend = () => {
      if (isRecording.value) {
        recognition.start();
      }
    };
    
    mediaRecorder.ondataavailable = (event) => {
      audioChunks.push(event.data);
    };
    
    mediaRecorder.onstop = async () => {
      const audioBlob = new Blob(audioChunks, { type: 'audio/webm' });
      await submitAnswer(audioBlob);
    };
    
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
    recognition.start();
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
    if (recognition) {
      recognition.stop();
    }
  }
};

// 답변 제출
const submitAnswer = async (audioBlob) => {
  try {
    if (!currentQuestion.value) {
      console.error('[ExamQuestionView] 현재 문제 정보가 없습니다.');
      return;
    }
    
    isSaving.value = true;

    const formData = new FormData();
    formData.append('file', audioBlob, 'answer.webm');
    formData.append('sttText', sttResult.value);
    formData.append('duration', String(recordingTime.value));
    
    await examApi.submitAnswer(
      examId.value,
      currentQuestion.value.order,
      formData
    );
    
    isSubmitted.value = true;
    saveProgress();
    
  } catch (error) {
    console.error('[ExamQuestionView] 답변 제출 실패:', error);
    alert('답변 제출에 실패했습니다.');
  } finally {
    isSaving.value = false;
  }
};

// 실제 다음 문제로 진행하는 내부 함수
const _proceedToNextQuestion = (isForced = false) => {
  if (currentQuestionIndex.value === 6) {
    showRelevelModal.value = true;
  } else if (currentQuestionIndex.value < questions.value.length - 1) {
    currentQuestionIndex.value++;
    resetQuestionState();
    saveProgress();
  } else {
    completeExam(isForced);
  }
};

// 다음 문제 버튼 핸들러
const goNext = () => {
  if (isSubmitted.value) {
    _proceedToNextQuestion(false);
  } else {
    showConfirmNextModal.value = true;
  }
};

// 답변 없이 다음 문제로 넘어가는 것을 확정
const confirmGoNext = () => {
  showConfirmNextModal.value = false;
  _proceedToNextQuestion(true);
};

// 답변 없이 다음 문제로 넘어가는 것을 취소
const cancelGoNext = () => {
  showConfirmNextModal.value = false;
};

// 난이도 재조정
const setRelevel = async (choice) => {
  const difficultyMap = { easy: -1, same: 0, hard: 1 };
  const baseLevel = Number(initialDifficulty.value);
  const targetLevel = baseLevel + difficultyMap[choice];
  const finalLevel = Math.max(1, Math.min(6, targetLevel));

  adjustedDifficulty.value = finalLevel;
  showRelevelModal.value = false;
  
  try {
    const response = await examApi.updateAdjustedDifficulty(
      examId.value,
      { adjustedDifficulty: adjustedDifficulty.value },
    );
    
    // 전체 문제를 다시 불러와서 확실하게 동기화 (문제 누락 방지)
    const infoResponse = await examApi.getExamInfo(examId.value);
    const examData = infoResponse.data;
    
    if (examData && examData.questions) {
      questions.value = examData.questions;
      // 서버에서 확정된 난이도로 다시 설정
      if (examData.adjustedDifficulty) {
        adjustedDifficulty.value = examData.adjustedDifficulty;
      }
      totalQuestions.value = getTotalQuestionsByLevel(adjustedDifficulty.value);
      
      // 서버에서 받은 문제를 난이도별 개수에 맞게 자르기
      questions.value = examData.questions.slice(0, totalQuestions.value);
      
      console.log(`[ExamQuestionView] 난이도 재조정 완료. 총 문제 수: ${totalQuestions.value}, 로드된 문제 수: ${questions.value.length}`);
    } else {
      // 만약 재조회 실패 시 기존 방식 fallback (혹은 에러 처리)
       const currentQuestions = questions.value.slice(0, currentQuestionIndex.value + 1);
       const newQuestions = response.data.questions || [];
       const combinedQuestions = [...currentQuestions, ...newQuestions];
       questions.value = combinedQuestions;
       totalQuestions.value = getTotalQuestionsByLevel(adjustedDifficulty.value);
    }
    
    currentQuestionIndex.value++;
    resetQuestionState();
    saveProgress();
    
  } catch (error) {
    console.error('[ExamQuestionView] 난이도 재조정 실패:', error);
    alert(`난이도 재조정에 실패했습니다. (${error.message || 'Unknown Error'})`);
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
    adjustedDifficulty: adjustedDifficulty.value,
    isSubmitted: isSubmitted.value,
    timestamp: new Date().toISOString()
  };
  
  localStorage.setItem(`exam_${examId.value}`, JSON.stringify(data));
  
  // 중단된 시험 정보 저장 (홈 화면에서 '이어하기' 버튼 활성화용)
  localStorage.setItem('incompleteExam', JSON.stringify({
    examId: examId.value,
    currentQuestion: currentQuestionIndex.value + 1,
    remainingTime: formattedTotalTime.value,
    isSubmitted: isSubmitted.value,
    currentIndex: currentQuestionIndex.value,
    totalQuestions: totalQuestions.value
  }));
  
  console.log('[ExamQuestionView] 진행상황 저장:', data);
};

// 시험 종료
const completeExam = async (bypassConfirmation = false) => {
  if (!bypassConfirmation && !confirm('시험을 종료하시겠습니까?')) {
    return;
  }
  
  try {
    await examApi.completeExam(examId.value);
    
    localStorage.removeItem(`exam_${examId.value}`);
    localStorage.removeItem('incompleteExam'); // 시험 완료 시 이어하기 데이터 삭제
    
    isNavigatingAwayIntentionally.value = true; // 플래그 설정
    alert('시험이 종료되었습니다. AI 분석이 시작됩니다.');
    router.push({ path: '/exam/feedback', query: { examId: examId.value } });
    
  } catch (error) {
    console.error('[ExamQuestionView] 시험 종료 실패:', error);
    alert('시험 종료에 실패했습니다.');
  }
};

// 시험 나가기 버튼 핸들러
const exitExam = () => {
  if (confirm('시험을 종료하고 나가시겠습니까? 진행 상황이 저장됩니다.')) {
    saveProgress();
    isNavigatingAwayIntentionally.value = true; // 플래그 설정
    router.push('/exam');
  }
};

// 페이지를 떠나기 전에 확인 (Vue Router 가드)
onBeforeRouteLeave((to, from, next) => {
  if (isNavigatingAwayIntentionally.value) {
    isNavigatingAwayIntentionally.value = false; // 플래그 초기화
    next();
    return;
  }

  // 사용자가 시험을 완료하고 피드백 페이지로 이동하는 경우는 예외 처리
  if (to.path.includes('/exam/feedback')) {
    next();
    return;
  }

  const answer = window.confirm('시험을 종료하시겠습니까? 답변 내용은 저장됩니다.');
  if (answer) {
    saveProgress();
    isNavigatingAwayIntentionally.value = true; // 플래그 설정
    next({ path: '/exam' }); // 메인 페이지로 리디렉션
  } else {
    next(false); // 내비게이션 취소
  }
});

onMounted(() => {
  initializeExam().then(() => {
    resetQuestionState();
    
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
  clearInterval(totalTimer);
  if (replayTimer) clearTimeout(replayTimer);

  if (recognition) {
    recognition.stop();
    recognition = null;
  }
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
        <div class="header-right">
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
          <div class="time-display">
            <span class="material-icons">timer</span>
            {{ formattedTotalTime }}
          </div>
        </div>
      </header>

      <!-- 메인 콘텐츠 -->
      <main class="content-wrapper">
        <!-- 질문 섹션 -->
        <section class="question-section">
          <div class="question-card">
              <div class="question-header">
                <h2>Question {{ currentQuestionIndex + 1 }}</h2>
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
              <div v-if="isSaving" class="status-indicator saving">
                <span class="material-icons spin">sync</span>
                SAVING...
              </div>
              <div v-else class="status-indicator" :class="{ recording: isRecording, submitted: isSubmitted }">
                <span class="status-dot"></span>
                {{ isSubmitted ? 'SUBMITTED' : (isRecording ? 'RECORDING' : 'READY') }}
              </div>
              <div class="recording-timer" v-if="!isSubmitted">
                <span class="material-icons" style="font-size: 20px; color: var(--text-secondary);">schedule</span>
                {{ formattedRecordingTime }}
              </div>
            </div>

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

      <!-- 다음 버튼 -->
      <div class="navigation-controls">
        <button @click="goNext" class="next-btn">
          {{ isLastQuestion ? 'Finish' : 'Next Question' }}
          <span class="material-icons">{{ isLastQuestion ? 'check_circle' : 'arrow_forward' }}</span>
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
          <button v-if="currentDifficultyLevel > 1" @click="setRelevel('easy')" class="difficulty-btn">
            <span class="material-icons">trending_down</span>
            <span class="label">쉽게</span>
          </button>
          <button @click="setRelevel('same')" class="difficulty-btn">
            <span class="material-icons">trending_flat</span>
            <span class="label">동일</span>
          </button>
          <button v-if="currentDifficultyLevel < 6" @click="setRelevel('hard')" class="difficulty-btn">
            <span class="material-icons">trending_up</span>
            <span class="label">어렵게</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 다음 문제 확인 모달 -->
    <div v-if="showConfirmNextModal" class="modal-overlay">
      <div class="modal-card">
        <div class="modal-header">
          <h3>답변 미제출</h3>
          <p class="subtitle">답변을 제출하지 않고 다음 문제로 넘어가시겠습니까?</p>
        </div>
        <div class="difficulty-options">
          <button @click="cancelGoNext" class="difficulty-btn">
            <span class="material-icons">cancel</span>
            <span class="label">아니오</span>
          </button>
          <button @click="confirmGoNext" class="difficulty-btn">
            <span class="material-icons">check_circle</span>
            <span class="label">네</span>
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

.time-display {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 1.25rem;
  font-weight: 800;
  color: var(--primary-color);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-right: 16px;
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
  margin-top: 60px;
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
  width: 180px;
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

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
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
  background: #ffffff;
  border: 2px solid #e2e8f0;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
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

.status-indicator.saving {
  background: #dbeafe;
  color: #2563eb;
}

.spin {
  animation: spin 1s linear infinite;
  font-size: 1.1rem;
}
</style>