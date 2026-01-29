<script setup>
import { ref, computed, onUnmounted, onMounted, watch } from "vue";
import { useRoute } from "vue-router";
import { practicesApi, surveysApi } from "@/api";
import { useAuthStore } from "@/stores/auth";

const route = useRoute();
const authStore = useAuthStore();
const userId = computed(() => authStore.user?.id);

// ============================================
// Props 정의 (부모 컴포넌트에서 받을 데이터)
// ============================================
const props = defineProps({
  // 연습 세션 정보
  practiceSession: {
    type: Object,
    default: () => ({
      practice_id: null,
      type_id: null,
      topic_id: null,
      set_id: null,
      started_at: null,
    }),
  },
  // 선택 가능한 주제 목록 (Topic 테이블에서)
  availableTopics: {
    type: Array,
    default: () => [],
  },
  // 현재 문제 세트 정보 (question_set 테이블)
  currentQuestionSet: {
    type: Object,
    default: () => ({
      set_id: null,
      level: null,
      question_cnt: null,
      questions: [], // question_bank 테이블의 질문들
    }),
  },
  // 이전에 저장된 답변 (있는 경우)
  savedAnswer: {
    type: Object,
    default: () => null,
  },
});

// ============================================
// Emits 정의 (부모 컴포넌트로 보낼 이벤트)
// ============================================
const emit = defineEmits([
  "topic-changed", // 주제 변경 시
  "answer-submitted", // 답변 제출 시
  "question-changed", // 질문 변경 시
]);

// ============================================
// 1. 주제 관리 (Topic 테이블 기반)
// ============================================
const currentTopic = ref(null); // 선택된 topic_id
const isTopicExpanded = ref(false);
const localTopics = ref([]); // Props or Mock topics

// 주제 선택 핸들러
const selectTopic = (topicId) => {
  currentTopic.value = topicId;
  emit("topic-changed", topicId);
};

// 표시할 주제 목록 (선택된 주제를 맨 앞으로, 12개씩 페이징)
const displayedTopics = computed(() => {
  const source =
    localTopics.value.length > 0 ? localTopics.value : props.availableTopics;
  
  // 전체 목록 복사
  let sorted = [...source];
  
  if (currentTopic.value) {
    const index = sorted.findIndex(t => (t.topic_id || t.topicId) === currentTopic.value);
    if (index > -1) {
      const selected = sorted.splice(index, 1)[0];
      sorted.unshift(selected);
    }
  }

  return isTopicExpanded.value ? sorted : sorted.slice(0, 12);
});

// ============================================
// 2. 질문 관리 (question_bank 테이블 기반)
// ============================================
const currentQuestionIndex = ref(0);

// 로컬 질문 관리 (Props가 없을 경우 대비)
const localQuestions = ref([]);

// 현재 질문 정보
const currentQuestion = computed(() => {
  // 1. Props에 데이터가 있으면 우선 사용
  if (
    props.currentQuestionSet?.questions &&
    props.currentQuestionSet.questions.length > 0
  ) {
    return props.currentQuestionSet.questions[currentQuestionIndex.value];
  }
  // 2. Props가 없으면 로컬에서 불러온 데이터 사용
  if (localQuestions.value.length > 0) {
    return localQuestions.value[currentQuestionIndex.value];
  }
  return null;
});

const showQuestionText = ref(false);

// 오디오 재생 (question_bank의 audio_url)
const playQuestionAudio = () => {
  if (currentQuestion.value?.audio_url) {
    const audio = new Audio(currentQuestion.value.audio_url);
    audio.play().catch((e) => console.error("오디오 재생 실패:", e));
  }
};

// 질문 번호 표시 (question_bank의 order 필드 사용)
const questionNumber = computed(() => {
  return currentQuestion.value?.order || currentQuestionIndex.value + 1;
});

// ============================================
// 3. 답변 작성 (Practice_answers 테이블로 저장될 데이터)
// ============================================
const koreanScript = ref("");
const maxChars = 1000;
const sttResult = ref("");
const finalTranscriptAccumulated = ref("");
const isRecording = ref(false);
const recordingTime = ref(0);
let recognition = null;
let timerInterval = null;
let audioRecorder = null;
let audioChunks = [];
const recordedBlob = ref(null);
const recordedDuration = ref(0);
const isAnalyzing = ref(false);

// 오디오 지속 시간 측정 유틸리티
const getDuration = (blob) => {
  return new Promise((resolve) => {
    const audio = new Audio();
    audio.src = URL.createObjectURL(blob);
    audio.addEventListener('loadedmetadata', () => {
      resolve(audio.duration); // 초 단위
    });
  });
};

// 저장된 답변 불러오기
watch(
  () => props.savedAnswer,
  (newAnswer) => {
    if (newAnswer) {
      koreanScript.value = newAnswer.korean_script || "";
      sttResult.value = newAnswer.english_script || "";
    }
  },
  { immediate: true },
);

// STT 초기화
const initRecognition = () => {
  if (recognition) return;
  const SpeechRecognition =
    window.SpeechRecognition || window.webkitSpeechRecognition;
  if (!SpeechRecognition) return alert("크롬 브라우저를 사용해 주세요.");

  recognition = new SpeechRecognition();
  recognition.continuous = true;
  recognition.interimResults = true;
  recognition.lang = "en-US";

  recognition.onresult = (event) => {
    let interimTranscript = "";
    let newFinalTranscript = "";

    for (let i = event.resultIndex; i < event.results.length; ++i) {
      if (event.results[i].isFinal) {
        // 새로 확정된 문장을 누적 변수에 추가
        finalTranscriptAccumulated.value +=
          event.results[i][0].transcript + " ";
      } else {
        // 아직 인식 중인 문장
        interimTranscript += event.results[i][0].transcript;
      }
    }
    // 최종 표시용 변수 = (이전까지 확정된 것들) + (방금 확정된 것들) + (현재 말하고 있는 중인 것)
    sttResult.value = finalTranscriptAccumulated.value + interimTranscript;
  };
  recognition.onerror = () => {
    isRecording.value = false;
  };
  recognition.onend = () => {
    if (isRecording.value) recognition.start();
  };
};

// 오디오 녹음 초기화
const initAudioRecording = async () => {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    
    // 브라우저 호환성을 고려하되, 전송 시 mp3로 취급하기 위해 최적의 타입 선택
    const mimeType = MediaRecorder.isTypeSupported('audio/webm;codecs=opus') 
      ? 'audio/webm;codecs=opus' 
      : 'audio/webm';
      
    audioRecorder = new MediaRecorder(stream, { mimeType });
    audioChunks = [];

    audioRecorder.ondataavailable = (event) => {
      if (event.data.size > 0) {
        audioChunks.push(event.data);
      }
    };

    audioRecorder.onstop = async () => {
      // 녹음 중지 시 Blob 생성 및 저장
      const blob = new Blob(audioChunks, { type: mimeType });
      recordedBlob.value = blob;
      
      // 지속 시간 측정
      const duration = await getDuration(blob);
      recordedDuration.value = Math.round(duration);
      
      console.log("녹음 완료:", {
        size: blob.size,
        duration: recordedDuration.value,
        type: blob.type
      });
    };

    audioRecorder.start();
  } catch (e) {
    console.error("오디오 녹음 초기화 실패:", e);
    alert("마이크 접근 권한이 필요합니다.");
  }
};

// 녹음 토글
const toggleRecording = async () => {
  if (isRecording.value) {
    if (recognition) recognition.stop();
    if (audioRecorder && audioRecorder.state === "recording") {
      audioRecorder.stop();
    }
    clearInterval(timerInterval);
    isRecording.value = false;
  } else {
    initRecognition();
    await initAudioRecording();

    recordedBlob.value = null;
    recordedDuration.value = 0;
    sttResult.value = "";
    recordingTime.value = 0;

    try {
      recognition.start();
      isRecording.value = true;
      timerInterval = setInterval(() => {
        recordingTime.value++;
        if (recordingTime.value >= 180) toggleRecording();
      }, 1000);
    } catch (e) {
      console.error(e);
    }
  }
};

// ============================================
// 4. AI 분석 및 피드백 (Type_feedbacks 테이블)
// ============================================
const isAnalyzed = ref(false);
const currentTab = ref("sentence");
const selectedSentenceIndex = ref(null);
const currentPage = ref(0);
const itemsPerPage = 2;

// 피드백 데이터 (API 응답으로 채워질 예정)
const feedbackData = ref([]);
const overallFeedback = ref("");

// API 호출: 답변 분석 요청
const analyze = async () => {
  if (!currentQuestion.value || !recordedBlob.value) {
    return alert("먼저 답변을 녹음해주세요.");
  }

  try {
    isAnalyzing.value = true;

    // 1. Audio Blob으로부터 mp3 파일 객체 생성
    const audioFile = new File([recordedBlob.value], "recording.mp3", {
      type: "audio/mpeg",
    });

    // 2. JSON 데이터 준비
    const requestData = {
      koreanScript: koreanScript.value,
      englishScript: sttResult.value,
    };

    // 3. Payload 생성
    const payload = {
      request: requestData,
      audio: audioFile,
    };

    // 4. API 호출
    const response = await practicesApi.savePracticeSession(
      props.practiceSession.practice_id,
      currentQuestion.value.question_id,
      payload,
    );

    console.log("분석 결과:", response.data);

    // 5. 결과 처리
    if (response.data && response.data.feedbackResult) {
      // API 응답 구조에 맞춰 데이터 매핑
      // feedbackResult: { scriptCorrections: [], overallComment: "" }

      const result = response.data.feedbackResult;

      // 문장 피드백 매핑
      feedbackData.value = (result.scriptCorrections || []).map((item) => ({
        original: item.originalSegment,
        improved: item.correctedSegment,
        reason: item.comment || "피드백이 없습니다.",
      }));

      // 종합 피드백 매핑
      overallFeedback.value =
        result.overallComment || "종합 피드백이 없습니다.";

      isAnalyzed.value = true;
      currentPage.value = 0;

      // 부모 컴포넌트로 알림
      emit("answer-submitted", response.data);
    } else {
      throw new Error("분석 결과 형식이 올바르지 않습니다.");
    }
  } catch (error) {
    console.error("분석 요청 실패:", error);
    alert("분석에 실패했습니다. 다시 시도해주세요.");
  } finally {
    isAnalyzing.value = false;
  }
};

// 페이지네이션
const totalPages = computed(() =>
  Math.ceil(feedbackData.value.length / itemsPerPage),
);
const paginatedFeedback = computed(() => {
  const start = currentPage.value * itemsPerPage;
  const end = start + itemsPerPage;
  return feedbackData.value.slice(start, end);
});

const goToNextPage = () => {
  if (currentPage.value < totalPages.value - 1) {
    currentPage.value++;
    selectedSentenceIndex.value = null;
  }
};

const goToPrevPage = () => {
  if (currentPage.value > 0) {
    currentPage.value--;
    selectedSentenceIndex.value = null;
  }
};

const highlightFromCard = (index) => {
  selectedSentenceIndex.value = currentPage.value * itemsPerPage + index;
};

// ============================================
// 5. 초기화 및 정리
// ============================================

onMounted(async () => {
  // 1. 라우터 쿼리 파라미터 확인
  const queryTopicId = Number(route.query.topic);
  const queryTypeId = route.query.type;
  const surveyId = Number(route.query.surveyId);

  // 2. 주제 데이터 로드 (surveyId가 있으면 해당 설문 토픽 우선)
  if (surveyId) {
    try {
      console.log("[PracticeQuestionView] loading survey details. ID:", surveyId);
      console.log("[PracticeQuestionView] Calling surveysApi.getSurveyById...");
      const response = await surveysApi.getSurveyById(surveyId);
      console.log("[PracticeQuestionView] surveysApi.getSurveyById success:", response.status);
      if (response.data && response.data.selectedTopics) {
        localTopics.value = response.data.selectedTopics.map((t) => ({
          topic_id: t.topicId,
          topic_name: t.topicName,
        }));
      }
    } catch (error) {
      console.error("설문 토픽 로드 실패:", error);
    }
  }

  // 데이터가 여전히 없으면 MOCK 데이터 로드 (테스트용)
  if (localTopics.value.length === 0 && props.availableTopics.length === 0) {
    localTopics.value = [
      { topic_id: 101, topic_name: "영화보기" },
      { topic_id: 102, topic_name: "공원 가기" },
      { topic_id: 103, topic_name: "카페 투어" },
      { topic_id: 201, topic_name: "음악 감상하기" },
      { topic_id: 203, topic_name: "요리하기" },
      { topic_id: 301, topic_name: "조깅" },
      { topic_id: 302, topic_name: "걷기" },
      { topic_id: 401, topic_name: "국내여행" },
      { topic_id: 402, topic_name: "해외여행" },
      { topic_id: 501, topic_name: "독서" },
    ];
  }

  // 3. 연습 세션 시작 및 문제 불러오기 (Props가 없을 때)
  if (
    !props.currentQuestionSet?.questions ||
    props.currentQuestionSet.questions.length === 0
  ) {
    if (surveyId && queryTopicId) {
      try {
        // 연습 세션 시작
        const startRes = await practicesApi.startPractice({
          surveyId,
          topicId: queryTopicId,
        });
        const practiceId = startRes.data.practiceId;

        // 문제 상세 조회
        const problemRes = await practicesApi.getPracticeProblem(practiceId);
        if (problemRes.data && problemRes.data.questions) {
          localQuestions.value = problemRes.data.questions.map((q) => ({
            question_id: q.questionId,
            order: q.questionOrder,
            question_text: q.questionText,
            audio_url: q.audioUrl,
          }));
        }
      } catch (error) {
        console.error("연습 문제 로드 실패:", error);
        // 실패 시 더미 데이터 추가
        localQuestions.value = [
          {
            question_id: 999,
            order: 1,
            question_text:
              "Could you tell me a little bit about yourself? Where do you live and what do you do?",
            audio_url: "",
          },
        ];
      }
    }
  }

  // 4. 초기 주제 선택 강조
  if (queryTopicId) {
    currentTopic.value = Number(queryTopicId); // 숫자 타입으로 강제 변환
  } else if (props.practiceSession.topic_id) {
    currentTopic.value = Number(props.practiceSession.topic_id);
  } else if (localTopics.value.length > 0) {
    currentTopic.value = Number(localTopics.value[0].topic_id);
  }

  // 상단 바 정보 재로드용 강제 반응성 트리거 (필요 시)
  console.log('[PracticeQuestionView] Initial currentTopic:', currentTopic.value);
});

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval);
  if (recognition) recognition.stop();
  if (audioRecorder && audioRecorder.state === "recording") {
    audioRecorder.stop();
  }
});
</script>

<template>
  <div class="page-container">
    <nav class="topic-section">
      <div class="topic-grid" :class="{ expanded: isTopicExpanded }">
        <button
          v-for="topic in displayedTopics"
          :key="topic.topic_id"
          :class="['tab-btn', { active: currentTopic === topic.topic_id }]"
          @click="selectTopic(topic.topic_id)"
        >
          {{ topic.name || topic.topic_name }}
        </button>
      </div>
      <button class="expand-btn" @click="isTopicExpanded = !isTopicExpanded">
        {{ isTopicExpanded ? "접기 ▲" : "주제 더보기 ▼" }}
      </button>
    </nav>

    <main class="page-content">
      <div class="main-grid">
      <section class="input-area">
        <!-- 질문 표시 (question_bank 테이블 기반) -->
        <div class="question-container" v-if="currentQuestion">
          <div class="question-header">
            <div class="q-id-group">
              <h2 class="q-number">Q{{ questionNumber }}</h2>
              <button class="audio-btn" @click="playQuestionAudio">
                <span class="material-icons">volume_up</span>
              </button>
            </div>
            <button
              class="toggle-q-btn"
              @click="showQuestionText = !showQuestionText"
            >
              {{ showQuestionText ? "질문 숨기기" : "질문 텍스트 보기" }}
            </button>
          </div>
          <div v-if="showQuestionText" class="question-text-card">
            {{ currentQuestion.question_text }}
          </div>
        </div>

        <!-- 한글 스크립트 입력 -->
        <div class="card">
          <div class="label-row">
            <label class="input-label">📝 한글로 써 보세요</label>
            <span class="count"
              >{{ koreanScript.length }} / {{ maxChars }}</span
            >
          </div>
          <textarea
            v-model="koreanScript"
            :maxlength="maxChars"
            placeholder="이곳에 한글로 작성하세요"
          >
          </textarea>
        </div>

        <!-- 영어 답변 녹음 -->
        <div class="card">
          <div class="label-row">
            <label class="input-label">🎙️ 영어로 대답해보세요</label>
            <div class="mic-group">
              <span v-if="isRecording" class="timer">
                {{ Math.floor(recordingTime / 60) }}:{{
                  (recordingTime % 60).toString().padStart(2, "0")
                }}
              </span>
              <button
                @click="toggleRecording"
                :class="['mic-btn', { recording: isRecording }]"
              >
                <span class="material-icons">{{
                  isRecording ? "stop" : "mic"
                }}</span>
              </button>
            </div>
          </div>
          <div class="stt-box" :class="{ 'recording-border': isRecording }">
            <p v-if="sttResult">{{ sttResult }}</p>
            <p v-else class="placeholder">말씀하시면 실시간으로 변환됩니다</p>
          </div>
        </div>

        <!-- AI 분석 버튼 -->
        <div class="analyze-btn-wrapper">
          <div v-if="recordedDuration > 0 && !isRecording" class="recording-status">
            <span>녹음 완료 ({{ Math.floor(recordedDuration / 60) }}:{{ (recordedDuration % 60).toString().padStart(2, '0') }})</span>
          </div>
          <button 
            class="analyze-btn" 
            @click="analyze" 
            :disabled="isAnalyzing || !recordedBlob || isRecording"
          >
            {{ isAnalyzing ? '분석 중...' : 'AI 분석하기' }}
          </button>
        </div>
      </section>

      <!-- 피드백 결과 (Type_feedbacks 테이블 기반) -->
      <section class="analysis-area" v-if="isAnalyzed">
        <div class="bookmark-tabs">
          <button
            :class="['bookmark', { active: currentTab === 'sentence' }]"
            @click="currentTab = 'sentence'"
          >
            문장 피드백
          </button>
          <button
            :class="['bookmark', { active: currentTab === 'overall' }]"
            @click="currentTab = 'overall'"
          >
            종합 피드백
          </button>
        </div>

        <div class="feedback-card">
          <h3 class="result-title">오꿀쌤 피드백</h3>

          <div class="okkul-left-align">
            <img src="/okkul.svg" alt="Okkul" style="width: 100px; height: 100px;" />
          </div>

          <div v-if="currentTab === 'sentence'">
            <div class="report-box">
              <span
                v-for="(item, idx) in feedbackData"
                :key="idx"
                :class="[
                  'report-span',
                  { highlighted: selectedSentenceIndex === idx },
                ]"
              >
                {{ item.improved }}
              </span>
            </div>

            <div class="detail-list">
              <div
                v-for="(item, idx) in paginatedFeedback"
                :key="idx"
                class="detail-item"
                @click="highlightFromCard(idx)"
                :class="{
                  'selected-card':
                    selectedSentenceIndex === currentPage * itemsPerPage + idx,
                }"
              >
                <div class="sentence-row">
                  <span class="badge orig">기존</span> {{ item.original }}
                </div>
                <div class="sentence-row">
                  <span class="badge impr">개선</span> {{ item.improved }}
                </div>
                <div class="reason-text">💡 {{ item.reason }}</div>
              </div>
            </div>

            <!-- 페이지네이션 -->
            <div class="pagination" v-if="totalPages > 1">
              <button
                class="page-btn"
                @click="goToPrevPage"
                :disabled="currentPage === 0"
              >
                <span class="material-icons">chevron_left</span>
              </button>
              <span class="page-info"
                >{{ currentPage + 1 }} / {{ totalPages }}</span
              >
              <button
                class="page-btn"
                @click="goToNextPage"
                :disabled="currentPage === totalPages - 1"
              >
                <span class="material-icons">chevron_right</span>
              </button>
            </div>
          </div>

          <div v-if="currentTab === 'overall'" class="overall-section">
            <div class="overall-box">{{ overallFeedback }}</div>
          </div>
        </div>
      </section>
      </div>
    </main>
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

/* 질문 영역 스타일 */
.question-container {
  margin-bottom: 20px;
}
.question-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}
.q-id-group {
  display: flex;
  align-items: center;
  gap: 12px;
}
.q-number {
  font-size: 32px;
  font-weight: 900;
  margin: 0;
}
.audio-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 2px solid #000;
  background: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 2px 2px 0 #000;
}
.audio-btn:active {
  transform: translate(1px, 1px);
  box-shadow: 1px 1px 0 #000;
}
.toggle-q-btn {
  background: none;
  border: none;
  color: #64748b;
  font-size: 13px;
  font-weight: bold;
  cursor: pointer;
  text-decoration: underline;
}
.question-text-card {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 15px;
  font-size: 15px;
  font-weight: 600;
  line-height: 1.5;
}

/* 1. 주제 선택 */
.topic-section {
  margin-bottom: 30px;
  width: 100%;
}
.topic-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 10px;
  max-height: 96px;
  overflow: hidden;
  transition: max-height 0.3s ease;
}
.topic-grid.expanded {
  max-height: 600px;
}

.tab-btn {
  width: 100%;
  height: 48px;
  padding: 0 12px;
  border-radius: 12px;
  border: 2px solid var(--border-primary);
  background: var(--bg-secondary);
  cursor: pointer;
  font-size: 14px;
  font-weight: 700;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  color: var(--text-secondary);
}
.tab-btn.active {
  background: #fff9e6;
  border-color: #d97706;
  color: #d97706;
  box-shadow: 0 4px 12px rgba(217, 119, 6, 0.2);
}
.expand-btn {
  display: block;
  margin: 15px auto 0;
  background: none;
  border: none;
  color: #64748b;
  cursor: pointer;
  font-weight: bold;
  text-decoration: underline;
}

/* 2. 레이아웃 및 카드 */
.main-grid {
  display: flex;
  flex-direction: column;
  gap: 30px;
}
.card {
  background: var(--bg-secondary);
  border-radius: 24px;
  padding: 32px;
  border: 2px solid var(--border-primary);
}
.label-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.input-label {
  font-weight: 800;
  font-size: 15px;
}
textarea {
  width: 100%;
  height: 120px;
  border: none;
  background: #f8fafc;
  padding: 15px;
  border-radius: 12px;
  resize: none;
  box-sizing: border-box;
  font-size: 15px;
}

/* 3. STT 박스 */
.stt-box {
  min-height: 100px;
  background: #f8fafc;
  border: 2px dashed #ffd700;
  border-radius: 12px;
  padding: 15px;
  font-size: 15px;
}

.recording-border {
  border-style: solid;
  border-color: #ef4444;
}
.mic-btn {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: none;
  background: #f8fafc;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
}
.mic-group {
  display: flex;
  align-items: center;
  gap: 10px;
}
.timer {
  color: #ef4444;
  font-weight: bold;
  font-family: monospace;
}
.mic-btn.recording {
  background: #ef4444;
  color: white;
}

/* 5. 분석 결과 섹션 */
.bookmark-tabs {
  display: flex;
  gap: 5px;
  margin-bottom: -1px;
}
.bookmark {
  padding: 10px 20px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-bottom: none;
  border-radius: 12px 12px 0 0;
  cursor: pointer;
  font-size: 14px;
  font-weight: bold;
  color: #64748b;
}
.bookmark.active {
  background: #fff;
  border-bottom: 2px solid #fff;
  z-index: 2;
}
.feedback-card {
  background: #fff;
  border-radius: 0 20px 20px 20px;
  padding: 25px;
  border: 1px solid #e2e8f0;
}
.result-title {
  font-size: 22px;
  font-weight: 900;
  border-bottom: 4px solid #ffd700;
  display: inline-block;
  padding-bottom: 2px;
  margin-bottom: 10px;
}
.report-box {
  background: #f8fafc;
  padding: 20px;
  border-radius: 15px;
  line-height: 1.8;
  border: 1px solid #e2e8f0;
  margin-bottom: 20px;
}
.badge {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
  margin-right: 5px;
  font-weight: bold;
}
.badge.orig {
  background: #f8fafc;
  color: #64748b;
  border: 1px solid #e2e8f0;
}
.badge.impr {
  background: #fff7ed;
  color: #ea580c;
}

.highlighted {
  background: #ffd700;
  font-weight: 700;
  color: #000;
}
.detail-item {
  padding: 15px;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  margin-bottom: 10px;
  cursor: pointer;
  background: #fff;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}
.sentence-row {
  margin-bottom: 6px;
}
.reason-text {
  font-size: 13px;
  color: #64748b;
  margin-top: 8px;
}

.selected-card {
  border: 2px solid var(--primary-color);
  background: #fffef0;
}

.dark-mode .selected-card {
  background: #422006;
  border-color: var(--primary-color);
}

.overall-box {
  background: var(--bg-tertiary);
  padding: 25px;
  border-radius: 15px;
  border-left: 5px solid var(--primary-color);
  line-height: 1.6;
  color: var(--text-primary);
}

/* 페이지네이션 */
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid var(--border-primary);
}
.page-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 2px solid var(--text-primary);
  background: var(--bg-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 2px 2px 0 var(--text-primary);
  transition: all 0.2s;
  color: var(--text-primary);
}
.page-btn:hover:not(:disabled) {
  background: var(--primary-color);
  color: #000;
}
.page-btn:active:not(:disabled) {
  transform: translate(1px, 1px);
  box-shadow: 1px 1px 0 var(--text-primary);
}
.page-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
  box-shadow: none;
}
.page-info {
  font-size: 14px;
  font-weight: 700;
  min-width: 50px;
  text-align: center;
  color: var(--text-primary);
}

.analyze-btn-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  margin-top: 10px;
}
.recording-status {
  background: var(--bg-tertiary);
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 700;
  color: #10b981;
  border: 1px solid #10b981;
  display: flex;
  align-items: center;
  gap: 8px;
}
.recording-status::before {
  content: "";
  display: inline-block;
  width: 8px;
  height: 8px;
  background: #10b981;
  border-radius: 50%;
}
.analyze-btn {
  width: 180px;
  padding: 14px;
  background: var(--primary-color);
  border: 2px solid #000;
  border-radius: 50px;
  font-weight: 800;
  cursor: pointer;
  box-shadow: 0 4px 0 #000;
  color: #000;
}
.analyze-btn:active {
  transform: translateY(2px);
  box-shadow: 0 2px 0 #000;
}

/* Dark Mode Overrides and Extra Global Styles */
:global(.dark-mode) .tab-btn.active {
  background: #422006;
  border-color: var(--primary-color);
  color: var(--primary-color);
}

:global(.dark-mode) .analyze-btn {
  border-color: #f1f5f9;
  box-shadow: 0 4px 0 #f1f5f9;
  color: #000;
}

:global(.dark-mode) .analyze-btn:active {
  box-shadow: 0 2px 0 #f1f5f9;
}
</style>
