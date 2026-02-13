<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import okkulSvg from '@/assets/images/okkul.svg'
import { useRoute, useRouter } from "vue-router";
import { practicesApi, surveysApi } from "@/api";
import { useAuthStore } from "@/stores/auth";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

// ============================================
// 1. 상태 관리 및 ID 관리
// ============================================
const practiceIdRef = ref(null);
const activeSurveyId = ref(null);
const activeSurveyLevel = ref(null);

// 스크롤 이동을 위한 Element Reference
const feedbackSectionRef = ref(null);

// STT & Audio States
const whisperTranscript = ref("");
const tempTranscript = ref("");
const interimTranscript = ref("");
const webSpeechPreviewTranscript = ref("");
const sttResult = ref("");
const whisperSocket = ref(null);
let audioContext = null;
let audioProcessor = null;
let audioSource = null;
let whisperDonePromise = null;
let whisperDoneResolve = null;
let whisperDoneReceived = false;
let lastWhisperSeq = 0;
let isRecognitionStopping = false;
let webSpeechFallbackTimer = null;
const WEB_SPEECH_FALLBACK_MS = 2000;

// ============================================
// 2. 복사 기능 관련 상태
// ============================================
const showCopyToast = ref(false);
const copiedStatus = ref({});

const allImprovedSentences = computed(() => {
  if (!feedbackData.value || feedbackData.value.length === 0) return "";
  return feedbackData.value.map(item => item.improved).join('\n');
});

const handleCopy = async (text, id) => {
  if (!text) return;
  try {
    await navigator.clipboard.writeText(text);
    copiedStatus.value[id] = true;
    showCopyToast.value = true;
    setTimeout(() => {
      copiedStatus.value[id] = false;
      showCopyToast.value = false;
    }, 1000);
  } catch (err) {
    console.error('복사 실패:', err);
  }
};

// ============================================
// 3. 유틸리티 함수
// ============================================
const downsampleBuffer = (buffer, sampleRate, outSampleRate) => {
  if (outSampleRate >= sampleRate) return buffer;
  const sampleRateRatio = sampleRate / outSampleRate;
  const newLength = Math.round(buffer.length / sampleRateRatio);
  const result = new Float32Array(newLength);
  let offsetResult = 0;
  let offsetBuffer = 0;
  while (offsetResult < result.length) {
    const nextOffsetBuffer = Math.round((offsetResult + 1) * sampleRateRatio);
    let accum = 0, count = 0;
    for (let i = offsetBuffer; i < nextOffsetBuffer && i < buffer.length; i++) {
      accum += buffer[i];
      count++;
    }
    result[offsetResult] = accum / count;
    offsetResult++;
    offsetBuffer = nextOffsetBuffer;
  }
  return result;
};

const updateSttDisplay = () => {
  const whisper = normalizeTranscript(whisperTranscript.value);
  const webFinal = normalizeTranscript(tempTranscript.value);
  const webInterim = normalizeTranscript(interimTranscript.value);
  const committed = [whisper, webFinal].filter(Boolean).join(" ");
  const interimTail = trimPreviewOverlap(committed, webInterim);
  const parts = [committed, interimTail].filter(Boolean);

  sttResult.value = parts.join(" ");
  finalTranscriptAccumulated.value = committed ? `${committed} ` : "";
};

const normalizeTranscript = (text) => (text || "").replace(/\s+/g, " ").trim();

const getWordOverlapCount = (baseText, nextText) => {
  const base = normalizeTranscript(baseText);
  const next = normalizeTranscript(nextText);
  if (!base || !next) return 0;

  const baseWords = base.split(" ");
  const nextWords = next.split(" ");
  const maxOverlap = Math.min(baseWords.length, nextWords.length);

  for (let k = maxOverlap; k > 0; k--) {
    const baseSuffix = baseWords.slice(baseWords.length - k).join(" ");
    const nextPrefix = nextWords.slice(0, k).join(" ");
    if (baseSuffix === nextPrefix) return k;
  }
  return 0;
};

const mergeByWordOverlap = (existingText, incomingText) => {
  const existing = normalizeTranscript(existingText);
  const incoming = normalizeTranscript(incomingText);
  if (!incoming) return existing;
  if (!existing) return incoming;

  if (existing.endsWith(incoming)) return existing;

  const overlap = getWordOverlapCount(existing, incoming);
  if (overlap === 0) return `${existing} ${incoming}`.trim();

  const incomingWords = incoming.split(" ");
  return `${existing} ${incomingWords.slice(overlap).join(" ")}`.trim();
};

const trimPreviewOverlap = (committedText, previewText) => {
  const preview = normalizeTranscript(previewText);
  if (!preview) return "";
  const overlap = getWordOverlapCount(committedText, preview);
  if (overlap === 0) return preview;
  const words = preview.split(" ");
  return words.slice(overlap).join(" ").trim();
};

const finalizeWhisperBeforeStop = async () => {
  const socket = whisperSocket.value;
  if (!socket) return;
  console.log("[Practice STT] finalize start", {
    readyState: socket.readyState,
    whisperChars: whisperTranscript.value.length,
    previewChars: webSpeechPreviewTranscript.value.length,
  });

  try {
    if (socket.readyState === WebSocket.OPEN) {
      console.log("[Practice STT] -> eof");
      socket.send(JSON.stringify({ event: "eof" }));
    }
  } catch (e) {
    console.error("[WS] Failed to send eof:", e);
  }

  if (whisperDonePromise) {
    await Promise.race([
      whisperDonePromise,
      new Promise((resolve) => setTimeout(resolve, 1500)),
    ]);
  }

  try {
    socket.close();
  } catch (e) {
    console.error("[WS] Failed to close socket:", e);
  }
  whisperSocket.value = null;
};

const logSttEvent = (tag, payload, style = "background:#0f172a;color:#f8fafc;padding:2px 6px;border-radius:4px;") => {
  console.log(`%c[Practice STT] ${tag}`, style, payload);
};

const clearWebSpeechFallbackTimer = () => {
  if (webSpeechFallbackTimer) {
    clearTimeout(webSpeechFallbackTimer);
    webSpeechFallbackTimer = null;
  }
};

const clearWebSpeechDisplay = (reason = "") => {
  tempTranscript.value = "";
  interimTranscript.value = "";
  webSpeechPreviewTranscript.value = "";
  updateSttDisplay();
  logSttEvent(
    "WEBSPEECH CLEARED",
    { reason, whisperChars: whisperTranscript.value.length },
    "background:#7c2d12;color:#fff;padding:2px 6px;border-radius:4px;"
  );
};

const scheduleWebSpeechFallbackClear = () => {
  clearWebSpeechFallbackTimer();
  webSpeechFallbackTimer = setTimeout(() => {
    if (!isRecording.value) return;
    if (interimTranscript.value.trim()) return;
    if (!tempTranscript.value.trim()) return;
    clearWebSpeechDisplay(`no_whisper_response_${WEB_SPEECH_FALLBACK_MS}ms`);
  }, WEB_SPEECH_FALLBACK_MS);
};

const getDynamicTypeId = (type, level) => {
  if (type === 'INTRO') return 1;
  const safeLevel = level || 1;
  switch (type) {
    case "COMBO": return safeLevel <= 2 ? 2 : 3;
    case "ROLEPLAY": return safeLevel <= 2 ? 4 : (safeLevel <= 4 ? 5 : 6);
    case "ADVANCED": return 7;
    default: return 1;
  }
};

// ============================================
// Props & Emits
// ============================================
const props = defineProps({
  practiceSession: { type: Object, default: () => ({}) },
  availableTopics: { type: Array, default: () => [] },
  currentQuestionSet: { type: Object, default: () => ({ questions: [] }) },
  savedAnswer: { type: Object, default: () => null },
});

const emit = defineEmits(["topic-changed", "answer-submitted", "question-changed"]);

// ============================================
// 4. 주제 및 문제 로드 로직
// ============================================
const currentTopic = ref(null);
const isTopicExpanded = ref(false);
const localTopics = ref([]);
const localQuestions = ref([]);
const currentQuestionIndex = ref(0);
const showQuestionText = ref(false);
const currentAudio = ref(null);

const loadQuestionsForTopic = async (topicId) => {
  console.log(`[Practice] 로드 시작 - TopicID: ${topicId}`);

  resetState();
  if (currentAudio.value) {
    currentAudio.value.pause();
    currentAudio.value = null;
  }

  if (!activeSurveyId.value && route.query.type !== 'INTRO') {
    console.warn("Survey ID 없음");
    return;
  }

  const queryType = route.query.type || 'INTRO';

  try {
    const dynamicTypeId = getDynamicTypeId(queryType, activeSurveyLevel.value);

    const startRes = await practicesApi.startPractice({
      surveyId: activeSurveyId.value,
      topicId: topicId,
      typeId: dynamicTypeId,
    });

    practiceIdRef.value = startRes.data.practiceId;

    const problemRes = await practicesApi.getPracticeProblem(practiceIdRef.value);

    if (problemRes.data && problemRes.data.questions) {
      localQuestions.value = problemRes.data.questions.map((q) => ({
        question_id: q.questionId,
        order: q.questionOrder,
        question_text: q.questionText,
        audio_url: q.audioUrl,
      }));
      currentQuestionIndex.value = 0;
    }
  } catch (error) {
    console.error("API 호출 실패:", error);
    localQuestions.value = [{
      question_id: 999,
      question_text: "문제를 불러오지 못했습니다. 다시 시도해주세요.",
      audio_url: ""
    }];
  }
};

const selectTopic = async (topicId) => {
  if (currentTopic.value === topicId) return;
  currentTopic.value = topicId;
  emit("topic-changed", topicId);
  await loadQuestionsForTopic(topicId);
  setTimeout(() => { isTopicExpanded.value = false; }, 50);
};

const displayedTopics = computed(() => localTopics.value.length > 0 ? localTopics.value : props.availableTopics);

const currentTopicName = computed(() => {
  if (route.query.type === 'INTRO') return null;
  const source = localTopics.value.length > 0 ? localTopics.value : props.availableTopics;
  const found = source.find((t) => (t.topic_id || t.topicId) === currentTopic.value);
  return found ? (found.name || found.topic_name) : "";
});

const currentTypeName = computed(() => {
  const map = { 'INTRO': '자기소개', 'COMBO': '콤보', 'ROLEPLAY': '롤플레잉', 'ADVANCED': '어드밴스' };
  return map[route.query.type] || route.query.type;
});

const currentQuestion = computed(() => {
  if (localQuestions.value.length > 0) return localQuestions.value[currentQuestionIndex.value];
  return null;
});

const questionNumber = computed(() => currentQuestion.value?.order || currentQuestionIndex.value + 1);

// ============================================
// 5. 오디오 & STT 로직
// ============================================
const koreanScript = ref("");
const maxChars = 1000;
const isRecording = ref(false);
const recordingTime = ref(0);
let timerInterval = null;
let audioRecorder = null;
let audioChunks = [];
let sttSessionCounter = 0;
let activeSttSessionId = 0;
const recordedBlob = ref(null);
const recordedDuration = ref(0);
const isAnalyzing = ref(false);

const playQuestionAudio = () => {
  if (currentQuestion.value?.audio_url) {
    if (currentAudio.value) {
      currentAudio.value.pause();
      currentAudio.value.currentTime = 0;
    }
    currentAudio.value = new Audio(currentQuestion.value.audio_url);
    currentAudio.value.play().catch((e) => console.error(e));
  }
};

const getDuration = (blob) => {
  return new Promise((resolve) => {
    const audio = new Audio();
    audio.src = URL.createObjectURL(blob);
    audio.addEventListener("loadedmetadata", () => resolve(audio.duration));
  });
};

watch(() => props.savedAnswer, (newAnswer) => {
  if (newAnswer) {
    koreanScript.value = newAnswer.korean_script || "";
    whisperTranscript.value = newAnswer.english_script || "";
    tempTranscript.value = "";
    interimTranscript.value = "";
    webSpeechPreviewTranscript.value = "";
    updateSttDisplay();
  }
}, { immediate: true });

const initRecognition = () => {
  if (recognition) return;
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
  if (!SpeechRecognition) return alert("크롬 브라우저를 사용해 주세요.");
  recognition = new SpeechRecognition();
  recognition.continuous = true;
  recognition.interimResults = true;
  recognition.lang = "en-US";
  recognition.onresult = (event) => {
    if (!isRecording.value) return;
    let currentInterim = "";
    let newFinal = "";
    for (let i = event.resultIndex; i < event.results.length; ++i) {
      const transcript = normalizeTranscript(event.results[i][0].transcript);
      if (!transcript) continue;
      if (event.results[i].isFinal) {
        newFinal += `${transcript} `;
      } else {
        currentInterim += `${transcript} `;
      }
    }
    tempTranscript.value = mergeByWordOverlap(tempTranscript.value, newFinal);
    interimTranscript.value = normalizeTranscript(currentInterim);
    webSpeechPreviewTranscript.value = interimTranscript.value;
    if (newFinal.trim()) {
      scheduleWebSpeechFallbackClear();
      logSttEvent(
        "WEBSPEECH FINAL",
        {
          chunk: newFinal.trim(),
          tempChars: tempTranscript.value.length,
          interimChars: interimTranscript.value.length,
        },
        "background:#1d4ed8;color:#fff;padding:2px 6px;border-radius:4px;"
      );
    }
    if (currentInterim.trim()) {
      clearWebSpeechFallbackTimer();
      logSttEvent(
        "WEBSPEECH INTERIM",
        {
          chunk: currentInterim.trim(),
          tempChars: tempTranscript.value.length,
          interimChars: interimTranscript.value.length,
        },
        "background:#475569;color:#fff;padding:2px 6px;border-radius:4px;"
      );
    }
    updateSttDisplay();
  };
  recognition.onerror = () => { isRecording.value = false; };
  recognition.onend = () => {
    if (isRecognitionStopping || !isRecording.value) return;
    try {
      recognition.start();
    } catch (e) {
      console.error("[Practice STT] recognition restart failed", e);
    }
  };
};

const initAudioRecording = async (sessionId) => {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
    if (initToken !== recordingInitToken) {
      stream.getTracks().forEach((track) => track.stop());
      return false;
    }
    mediaStream = stream;

    // Whisper WS
    const wsUrl = import.meta.env.VITE_WHISPER_WS_URL;
    if (wsUrl) {
      whisperSocket.value = new WebSocket(wsUrl);
      whisperDoneReceived = false;
      lastWhisperSeq = 0;
      whisperDonePromise = new Promise((resolve) => {
        whisperDoneResolve = resolve;
      });
      whisperSocket.value.onopen = () => console.log("[WS] Connected");
      whisperSocket.value.onmessage = (event) => {
        if (sessionId !== activeSttSessionId) return;
        try {
          const data = JSON.parse(event.data);
          if (data.type === 'full' && data.text) {
            logSttEvent(
              "WHISPER FULL",
              {
                raw: data,
                text: data.text,
                seq: data.seq ?? null,
                textChars: data.text.length,
              },
              "background:#15803d;color:#fff;padding:2px 6px;border-radius:4px;"
            );
            whisperTranscript.value = mergeByWordOverlap(whisperTranscript.value, data.text);
            // Whisper segment is authoritative for the just-finished utterance boundary.
            // Drop pending WebSpeech final to prevent semantic duplicates.
            tempTranscript.value = "";
            interimTranscript.value = "";
            webSpeechPreviewTranscript.value = "";
            clearWebSpeechFallbackTimer();
            updateSttDisplay();
          } else if (data.type === "done") {
            whisperDoneReceived = true;
            console.log("[Practice STT] <- done", { seq: data.seq ?? null });
            if (whisperDoneResolve) whisperDoneResolve();
          }
        } catch (e) { console.error(e); }
      };
      whisperSocket.value.onclose = () => {
        if (!whisperDoneReceived && whisperDoneResolve) whisperDoneResolve();
      };
    }

    // AudioContext
    const localAudioContext = new (window.AudioContext || window.webkitAudioContext)();
    audioContext = localAudioContext;
    audioSource = localAudioContext.createMediaStreamSource(stream);
    try {
      await localAudioContext.audioWorklet.addModule('/audio-processor.js');
      if (initToken !== recordingInitToken || !audioContext || audioContext !== localAudioContext) {
        return false;
      }

      const audioWorkletNode = new AudioWorkletNode(localAudioContext, 'audio-processor');
      audioWorkletNode.port.onmessage = (event) => {
        if (whisperSocket.value && whisperSocket.value.readyState === WebSocket.OPEN && audioContext) {
          const downsampled = downsampleBuffer(event.data, audioContext.sampleRate, 24000);
          whisperSocket.value.send(downsampled);
        }
      };
      audioSource.connect(audioWorkletNode);
      audioWorkletNode.connect(localAudioContext.destination);
      audioProcessor = audioWorkletNode;
    } catch (err) { console.error("Worklet error", err); }

    // MediaRecorder
    const mimeType = MediaRecorder.isTypeSupported("audio/webm;codecs=opus") ? "audio/webm;codecs=opus" : "audio/webm";
    audioRecorder = new MediaRecorder(stream, { mimeType });
    audioChunks = [];
    audioRecorder.ondataavailable = (event) => { if (event.data.size > 0) audioChunks.push(event.data); };
    audioRecorder.onstop = async () => {
      await finalizeWhisperBeforeStop();
      const blob = new Blob(audioChunks, { type: mimeType });
      recordedBlob.value = blob;
      const duration = await getDuration(blob);
      recordedDuration.value = Math.round(duration);
      if (audioProcessor) { audioProcessor.disconnect(); audioSource.disconnect(); audioProcessor = null; audioSource = null; }
      if (audioContext) { audioContext.close(); audioContext = null; }
    };
    audioRecorder.start();
    return true;
  } catch (e) {
    console.error(e);
    alert("마이크 권한 필요");
    await cleanupRecordingResources();
    return false;
  } finally {
    isInitializingRecording = false;
  }
};

const toggleRecording = async () => {
  if (isInitializingRecording) return;

  if (isRecording.value) {
    console.log("[Practice STT] stop requested", {
      recordingTime: recordingTime.value,
      whisperChars: whisperTranscript.value.length,
      previewChars: webSpeechPreviewTranscript.value.length,
    });
    isRecording.value = false;
    isRecognitionStopping = true;
    if (recognition) {
      try {
        recognition.stop();
      } catch (e) {
        console.error("[Practice STT] recognition stop failed", e);
      }
    }
    if (audioRecorder && audioRecorder.state === "recording") audioRecorder.stop();
    clearInterval(timerInterval);
    clearWebSpeechFallbackTimer();
    clearWebSpeechDisplay("user_stop_recording");
  } else {
    activeSttSessionId = ++sttSessionCounter;
    initRecognition();
    await initAudioRecording(activeSttSessionId);
    recordedBlob.value = null;
    recordedDuration.value = 0;
    sttResult.value = "";
    recordingTime.value = 0;
    whisperTranscript.value = "";
    tempTranscript.value = "";
    interimTranscript.value = "";
    webSpeechPreviewTranscript.value = "";
    clearWebSpeechFallbackTimer();
    isRecognitionStopping = false;
    try {
      isRecording.value = true;
      recognition.start();
      timerInterval = setInterval(() => {
        recordingTime.value++;
        if (recordingTime.value >= 180) toggleRecording();
      }, 1000);
    } catch (e) {
      isRecording.value = false;
      console.error(e);
    }
  }
};

// ============================================
// 6. 분석 및 피드백
// ============================================
const isAnalyzed = ref(false);
const currentTab = ref("sentence");
const selectedSentenceIndex = ref(null);
const currentPage = ref(0);
const itemsPerPage = 2;
const feedbackData = ref([]);
const overallFeedback = ref([]);
const aiImprovedAnswer = ref("");
const feedbackError = ref(null);
let pollInterval = null;

const pollForFeedback = (practiceAnswerId) => {
  pollInterval = setInterval(async () => {
    try {
      const res = await practicesApi.getPracticeFeedback(practiceAnswerId);
      const result = res.data;
      if (result.feedbackStatus === "COMPLETED") {
        clearInterval(pollInterval);
        isAnalyzing.value = false;

        feedbackData.value = (result.scriptCorrections || []).map((item) => ({
          original: item.originalSegment,
          improved: item.correctedSegment,
          reason: item.comment || "피드백 없음",
        }));

        overallFeedback.value = [
          { label: "주제 적합성", text: result.relevanceFeedback || "피드백 없음" },
          { label: "논리성", text: result.logicFeedback || "피드백 없음" },
          { label: "유창성", text: result.fluencyFeedback || "피드백 없음" },
        ];
        aiImprovedAnswer.value = result.aiImprovedAnswer || "";

        isAnalyzed.value = true;
        currentPage.value = 0;
        emit("answer-submitted", result);

        // 분석 완료 시 피드백 섹션으로 자동 스크롤
        await nextTick();
        if (feedbackSectionRef.value) {
          feedbackSectionRef.value.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }

      } else if (result.feedbackStatus === "FAILED") {
        clearInterval(pollInterval);
        isAnalyzing.value = false;
        feedbackError.value = "피드백 생성 실패";
      }
    } catch (error) {
      clearInterval(pollInterval);
      isAnalyzing.value = false;
      feedbackError.value = "오류 발생";
    }
  }, 3000);
};

const analyze = async () => {
  if (!currentQuestion.value || !recordedBlob.value) return alert("녹음이 필요합니다.");
  if (!practiceIdRef.value) return alert("연습 세션 ID 오류");

  try {
    isAnalyzing.value = true;
    isAnalyzed.value = false;
    feedbackError.value = null;
    const audioFile = new File([recordedBlob.value], "recording.mp3", { type: "audio/mpeg" });
    const whisperOnlyScript = whisperTranscript.value.trim().replace(/\s+/g, " ");
    const requestData = {
      questionId: currentQuestion.value.question_id,
      koreanScript: koreanScript.value,
      englishScript: whisperOnlyScript,
    };
    console.log("[Practice STT] analyze payload", {
      whisperCharsRaw: whisperTranscript.value.length,
      whisperCharsNormalized: requestData.englishScript.length,
      recordingTime: recordingTime.value,
      hasBlob: !!recordedBlob.value,
    });
    const response = await practicesApi.savePracticeSession(practiceIdRef.value, { request: requestData, audio: audioFile });
    if (response.data.practiceAnswerId) pollForFeedback(response.data.practiceAnswerId);
  } catch (error) {
    isAnalyzing.value = false;
    feedbackError.value = "분석 요청 실패";
  }
};

const totalPages = computed(() => Math.ceil(feedbackData.value.length / itemsPerPage));
const paginatedFeedback = computed(() => {
  const start = currentPage.value * itemsPerPage;
  return feedbackData.value.slice(start, start + itemsPerPage);
});

const goToNextPage = () => { if (currentPage.value < totalPages.value - 1) { currentPage.value++; selectedSentenceIndex.value = null; } };
const goToPrevPage = () => { if (currentPage.value > 0) { currentPage.value--; selectedSentenceIndex.value = null; } };
const highlightFromCard = (index) => {
  const absoluteIndex = currentPage.value * itemsPerPage + index;
  selectedSentenceIndex.value = selectedSentenceIndex.value === absoluteIndex ? null : absoluteIndex;
};

// ============================================
// 7. 초기화 및 리셋
// ============================================
const resetState = (keepKoreanScript = false) => {
  recordingInitToken += 1;
  abortCurrentRecording = true;

  if (!keepKoreanScript) {
    koreanScript.value = "";
  }
  sttResult.value = "";
  recordedBlob.value = null;
  recordedDuration.value = 0;
  recordingTime.value = 0;
  finalTranscriptAccumulated.value = "";
  whisperTranscript.value = "";
  tempTranscript.value = "";
  interimTranscript.value = "";
  webSpeechPreviewTranscript.value = "";

  isAnalyzed.value = false;
  isAnalyzing.value = false;
  feedbackData.value = [];
  overallFeedback.value = [];
  aiImprovedAnswer.value = "";
  feedbackError.value = null;
  currentTab.value = "sentence";
  selectedSentenceIndex.value = null;
  currentPage.value = 0;
  showQuestionText.value = false;

  if (timerInterval) clearInterval(timerInterval);
  if (pollInterval) clearInterval(pollInterval);
  clearWebSpeechFallbackTimer();
  isRecording.value = false;
  if (currentAudio.value) { currentAudio.value.pause(); currentAudio.value = null; }
  void cleanupRecordingResources();
};

const retryQuestion = () => {
  if (confirm("다시 연습하시겠습니까?")) {
    resetState(true); // true = 한국어 스크립트 유지
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
};

const isLastQuestion = computed(() => {
  if (localQuestions.value.length === 0) return true;
  return currentQuestionIndex.value >= localQuestions.value.length - 1;
});

const nextQuestion = () => {
  resetState(); // false (기본값) = 초기화
  currentQuestionIndex.value++;
  window.scrollTo({ top: 0, behavior: 'smooth' });
};

const completePractice = () => {
  router.push('/practice');
};

// ============================================
// 8. Lifecycle Hooks
// ============================================

// 브라우저 뒤로가기 방지 및 처리
let isExiting = false // 종료 중인지 확인하는 플래그

const handlePopState = (event) => {
  if (isExiting) {
    // 이미 종료 중이면 그냥 넘어감
    return
  }
  
  // 뒤로가기 시 확인 창 표시
  if (confirm('연습을 종료하시겠습니까? 지금까지의 진행 상황이 저장되지 않을 수 있습니다.')) {
    // 확인을 누르면 /practice로 이동
    isExiting = true
    window.removeEventListener('popstate', handlePopState)
    router.push('/practice')
  } else {
    // 취소를 누르면 다시 히스토리 앞으로 이동
    history.pushState(null, '', location.href)
  }
}

onMounted(async () => {
  // 브라우저 뒤로가기 방지 설정
  history.pushState(null, '', location.href)
  window.addEventListener('popstate', handlePopState)

  let surveyId = Number(route.query.surveyId);
  const queryType = route.query.type;

  if (queryType === 'INTRO' && !surveyId) {
    try {
      const res = await surveysApi.getSurveyList();
      const list = res.data?.surveySummaryResponses || (Array.isArray(res.data) ? res.data : []);
      if (list.length > 0) surveyId = list[0].surveyId;
    } catch (e) { console.error(e); }
  }

  activeSurveyId.value = surveyId;

  if (activeSurveyId.value) {
    try {
      const response = await surveysApi.getSurveyById(activeSurveyId.value);
      activeSurveyLevel.value = response.data.level;
      if (response.data && response.data.selectedTopics) {
        localTopics.value = response.data.selectedTopics.map((t) => ({
          topic_id: t.topicId,
          topic_name: t.topicName,
        }));
      }
    } catch (error) { console.error(error); }
  }

  if (localTopics.value.length === 0 && props.availableTopics.length === 0) {
    localTopics.value = [
      { topic_id: 101, topic_name: "영화보기" },
      { topic_id: 102, topic_name: "공원 가기" },
    ];
  }

  let initTopicId = Number(route.query.topic);
  if (!initTopicId && props.practiceSession.topic_id) initTopicId = Number(props.practiceSession.topic_id);
  if (!initTopicId && localTopics.value.length > 0) initTopicId = Number(localTopics.value[0].topic_id);
  if (queryType === 'INTRO') initTopicId = 101;

  if (initTopicId) {
    currentTopic.value = initTopicId;
    await loadQuestionsForTopic(initTopicId);
  } else {
    localQuestions.value = [{ question_id: 999, question_text: "주제를 선택해주세요.", audio_url: "" }];
  }
});

onBeforeUnmount(() => {
  // 이벤트 리스너 제거
  window.removeEventListener('popstate', handlePopState)
  resetState();
});
</script>

<template>
  <div class="page-container">
    <nav class="topic-section" v-if="route.query.type !== 'INTRO'">
      <button class="expand-btn" @click="isTopicExpanded = !isTopicExpanded">
        {{ isTopicExpanded ? "접기 ▲" : "주제 더보기 ▼" }}
      </button>

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
    </nav>

    <main class="page-content">
      <div class="main-grid">
        <section class="input-area">
          <div class="question-container" v-if="currentQuestion">
            <div class="type-badge-row">
              <span v-if="currentTypeName" class="current-type-badge">{{ currentTypeName }}</span>
              <span v-if="currentTopicName && route.query.type !== 'INTRO'" class="current-topic-badge">{{ currentTopicName }}</span>
            </div>
            <div class="question-header">
              <div class="q-id-group">
                <h2 class="q-number">Q{{ questionNumber }}</h2>
                <button class="audio-btn" @click="playQuestionAudio">
                  <span class="material-icons">volume_up</span>
                  <span class="audio-label">질문 듣기</span>
                </button>
              </div>
              <button class="toggle-q-btn" @click="showQuestionText = !showQuestionText">
                {{ showQuestionText ? "질문 숨기기" : "질문 텍스트 보기" }}
              </button>
            </div>
            <div v-if="showQuestionText" class="question-text-card">
              {{ currentQuestion.question_text }}
            </div>
          </div>

          <div class="card">
            <div class="label-row">
              <label class="input-label">한국어 스크립트를 작성해보세요</label>
              <span class="count">{{ koreanScript.length }} / {{ maxChars }}</span>
            </div>
            <textarea
                v-model="koreanScript"
                :maxlength="maxChars"
                placeholder="바로 영어로 답하는 게 어려우시다면, 한국어로 먼저 자유롭게 답변을 적어보세요."
            >
          </textarea>
          </div>

          <div class="card">
            <div class="label-row">
              <label class="input-label">영어로 대답해보세요</label>
              <div class="mic-group">
              <span v-if="isRecording" class="timer">
                {{ Math.floor(recordingTime / 60) }}:{{ (recordingTime % 60).toString().padStart(2, "0") }}
              </span>
                <button @click="toggleRecording" :class="['mic-btn', { recording: isRecording }]">
                  <span class="material-icons">{{ isRecording ? "stop" : "mic" }}</span>
                </button>
              </div>
            </div>
            <div class="stt-box" :class="{ 'recording-border': isRecording }">
              <p v-if="finalTranscript" class="stt-final">{{ finalTranscript }}</p>
              <p v-if="partialTranscript" class="stt-partial">{{ partialTranscript }}</p>
              <p v-if="!finalTranscript && !partialTranscript" class="placeholder">말씀하시면 실시간으로 변환됩니다</p>
            </div>
          </div>

          <div class="analyze-btn-wrapper">
            <div v-if="recordedDuration > 0 && !isRecording" class="recording-status">
              <span>녹음 완료 ({{ Math.floor(recordedDuration / 60) }}:{{ (recordedDuration % 60).toString().padStart(2, '0') }})</span>
            </div>
            <button class="analyze-btn" @click="analyze" :disabled="isAnalyzing || !recordedBlob || isRecording">
              {{ isAnalyzing ? '분석 중...' : 'AI 분석하기' }}
            </button>
          </div>
          <div v-if="feedbackError" class="error-box">
            {{ feedbackError }}
          </div>
        </section>

        <section class="analysis-area" v-if="isAnalyzed" ref="feedbackSectionRef">
          <div class="bookmark-tabs">
            <button :class="['bookmark', { active: currentTab === 'sentence' }]" @click="currentTab = 'sentence'">
              문장 피드백
            </button>
            <button :class="['bookmark', { active: currentTab === 'overall' }]" @click="currentTab = 'overall'">
              종합 피드백
            </button>
          </div>

          <div class="feedback-card">
            <h3 class="result-title">오꿀쌤 피드백</h3>

            <div v-if="currentTab === 'sentence'">
              <div class="okkul-feedback-container">
                <div class="okkul-character">
                  <img :src="okkulSvg" alt="오꿀쌤" class="okkul-img" />
                  <span class="okkul-name">오꿀쌤</span>
                </div>
                <div class="speech-bubble">
                  <div class="bubble-header-row">
                    <h4 class="bubble-title">개선 문장</h4>
                    <button
                        @click="handleCopy(allImprovedSentences, 'sentences')"
                        class="copy-btn mini"
                        :class="{ copied: copiedStatus['sentences'] }"
                        title="전체 복사하기"
                    >
                      <span class="material-icons-outlined">{{ copiedStatus['sentences'] ? 'check' : 'content_copy' }}</span>
                    </button>
                  </div>

                  <div class="report-box-inner">
                  <span
                      v-for="(item, idx) in feedbackData"
                      :key="idx"
                      :class="['report-span', { highlighted: selectedSentenceIndex === idx }]"
                      @click="selectedSentenceIndex = selectedSentenceIndex === idx ? null : idx"
                  >
                    {{ item.improved }}
                  </span>
                  </div>
                </div>
              </div>

              <div class="detail-list">
                <div
                    v-for="(item, idx) in paginatedFeedback"
                    :key="idx"
                    class="detail-item"
                    @click="highlightFromCard(idx)"
                    :class="{ 'selected-card': selectedSentenceIndex === currentPage * itemsPerPage + idx }"
                >
                  <div class="sentence-row"><span class="badge orig">기존</span> {{ item.original }}</div>
                  <div class="sentence-row"><span class="badge impr">개선</span> {{ item.improved }}</div>
                  <div class="reason-text">
                    <span class="material-icons reason-icon">lightbulb</span>
                    {{ item.reason }}
                  </div>
                </div>
              </div>

              <div class="pagination" v-if="totalPages > 1">
                <button class="page-btn" @click="goToPrevPage" :disabled="currentPage === 0">
                  <span class="material-icons">chevron_left</span>
                </button>
                <span class="page-info">{{ currentPage + 1 }} / {{ totalPages }}</span>
                <button class="page-btn" @click="goToNextPage" :disabled="currentPage === totalPages - 1">
                  <span class="material-icons">chevron_right</span>
                </button>
              </div>
            </div>

            <div v-if="currentTab === 'overall'" class="overall-section">
              <div class="okkul-feedback-container">
                <div class="okkul-character">
                  <img :src="okkulSvg" alt="오꿀쌤" class="okkul-img" />
                  <span class="okkul-name">오꿀쌤</span>
                </div>
                <div class="speech-bubble">
                  <div class="bubble-header-row">
                    <h4 class="bubble-title">{{ aiImprovedAnswer ? "추천 답변" : "분석 중..." }}</h4>
                    <button
                        v-if="aiImprovedAnswer"
                        @click="handleCopy(aiImprovedAnswer, 'ai_answer')"
                        class="copy-btn mini"
                        :class="{ copied: copiedStatus['ai_answer'] }"
                        title="복사하기"
                    >
                      <span class="material-icons-outlined">{{ copiedStatus['ai_answer'] ? 'check' : 'content_copy' }}</span>
                    </button>
                  </div>
                  <p class="bubble-text">{{ aiImprovedAnswer || "잠시만 기다려주세요..." }}</p>
                </div>
              </div>

              <div v-for="(feedback, index) in overallFeedback" :key="index" class="overall-item">
                <h4 class="overall-label">{{ feedback.label }}</h4>
                <p class="overall-text">{{ feedback.text }}</p>
              </div>
            </div>
          </div>

          <div class="feedback-actions">
            <button class="action-btn secondary" @click="retryQuestion">
              <span class="material-icons">refresh</span> 다시 답변하기
            </button>
            <button v-if="!isLastQuestion" class="action-btn primary" @click="nextQuestion">
              다음 문제로 이동 <span class="material-icons">arrow_forward</span>
            </button>
            <button v-else class="action-btn primary" @click="completePractice">
              연습 종료하기 <span class="material-icons">check</span>
            </button>
          </div>
        </section>

        <Transition name="toast">
          <div v-if="showCopyToast" class="copy-toast">
            <span class="material-icons">check_circle</span>
            클립보드에 복사되었습니다!
          </div>
        </Transition>

      </div>
    </main>
  </div>
</template>

<style scoped>
.page-container {
  min-height: 100vh;
  background: #FDFBF5;
  padding: 24px !important;
  display: block !important;
}

.page-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0;
}

@media (max-width: 1024px) {
  .page-content { padding: 24px 32px; }
}

@media (max-width: 768px) {
  .page-content { padding: 16px 24px; }
}

.input-area {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.question-container { margin-bottom: 20px; }
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
  padding-left: 12px;
}
.q-number {
  font-size: 32px;
  font-weight: 800;
  margin: 0;
}

.type-badge-row {
  margin-bottom: 12px;
  display: flex;
  justify-content: flex-start;
  gap: 8px;
}

.current-topic-badge, .current-type-badge {
  background-color: var(--primary-color);
  color: #212529;
  width: auto;
  min-width: 120px;
  height: 36px;
  padding: 0 20px;
  border-radius: 18px;
  font-weight: 700;
  font-size: 0.95rem;
  box-shadow: var(--shadow-sm);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  white-space: nowrap;
  flex-shrink: 0;
}

.current-type-badge {
  background-color: #99C13A;
  color: #FFFFFF;
}

.audio-btn {
  width: auto;
  min-width: 44px;
  height: 44px;
  padding: 0 16px;
  border-radius: 22px;
  border: none;
  background: var(--primary-color);
  color: #212529;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  box-shadow: var(--shadow-sm);
  transition: all 0.2s;
  animation: pulse-audio 2s infinite;
}

@keyframes pulse-audio {
  0% { box-shadow: 0 0 0 0 rgba(255, 215, 0, 0.6); }
  70% { box-shadow: 0 0 0 10px rgba(255, 215, 0, 0); }
  100% { box-shadow: 0 0 0 0 rgba(255, 215, 0, 0); }
}

.audio-label { font-weight: 700; font-size: 1rem; }
.audio-btn:hover { background: var(--primary-hover); transform: scale(1.05); box-shadow: var(--shadow-md); }
.audio-btn:active { transform: scale(0.95); }

.toggle-q-btn {
  background: none;
  border: none;
  color: var(--text-tertiary);
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  text-decoration: underline;
  transition: color 0.2s;
}
.toggle-q-btn:hover { color: var(--primary-color); }

.question-text-card {
  background: var(--bg-secondary);
  border: var(--border-primary);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: var(--shadow-md);
  font-size: 1rem;
  font-weight: 700;
  line-height: 1.6;
  color: var(--text-primary);
}

/* Topic Section */
.topic-section { margin-bottom: 32px; width: 100%; }
.topic-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
  max-height: 0;
  opacity: 0;
  padding: 0 4px;
  margin: 0;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}
@media (max-width: 1200px) { .topic-grid { grid-template-columns: repeat(4, 1fr); } }
@media (max-width: 768px) { .topic-grid { grid-template-columns: repeat(2, 1fr); gap: 8px; } }

.topic-grid.expanded { max-height: 2000px; opacity: 1; padding: 4px; margin-top: 12px; }

.tab-btn {
  width: 100%;
  height: 44px;
  padding: 0 16px;
  border-radius: 12px;
  border: 1px solid var(--border-color);
  background: #FFFFFF;
  cursor: pointer;
  font-size: clamp(0.7rem, 0.9vw + 0.4rem, 0.875rem);
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  color: var(--text-secondary);
  box-shadow: var(--shadow-sm);
}
.tab-btn:hover:not(.active) { border-color: var(--primary-color); background: var(--bg-tertiary); }
.tab-btn.active { background: var(--primary-color); color: #212529; border-color: var(--primary-color); box-shadow: var(--shadow-md); transform: translateY(-2px); }

.expand-btn {
  display: block;
  margin: 12px auto 0;
  background: var(--bg-tertiary);
  border: none;
  border-radius: 20px;
  padding: 8px 20px;
  font-weight: 700;
  font-size: 0.875rem;
  cursor: pointer;
  color: var(--text-secondary);
  transition: all 0.2s;
}
.expand-btn:hover { background: var(--primary-light); color: #8B7300; }

.main-grid { display: flex; flex-direction: column; gap: 30px; }
.card {
  background: #FFFFFF;
  border-radius: 20px;
  padding: 32px;
  border: var(--border-primary);
  box-shadow: var(--shadow-sm);
}
.label-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.input-label { font-weight: 700; font-size: 1rem; color: var(--text-primary); }
.count { font-size: 0.8125rem; color: var(--text-tertiary); }

textarea {
  width: 100%;
  min-height: 140px;
  border: var(--border-primary);
  border-radius: 12px;
  padding: 10px;
  font-family: inherit;
  font-size: 1rem;
  font-weight: 500;
  background: var(--bg-tertiary);
  color: var(--text-primary);
  resize: vertical;
  transition: all 0.2s;
}
textarea:focus { outline: none; background: var(--bg-secondary); border-color: var(--primary-color); box-shadow: 0 0 0 4px var(--primary-light); }
textarea::placeholder { color: var(--text-tertiary); font-style: italic; font-weight: 400; }

.stt-box {
  min-height: 100px;
  background: var(--bg-tertiary);
  border: 2px dashed var(--primary-light);
  border-radius: 12px;
  padding: 10px;
  font-size: 1rem;
  color: var(--text-primary);
  transition: all 0.2s;
}
.stt-final { margin: 0; white-space: pre-wrap; line-height: 1.6; }
.stt-partial { margin: 8px 0 0; white-space: pre-wrap; line-height: 1.6; color: var(--text-tertiary); font-style: italic; }
.placeholder { color: var(--text-tertiary); font-style: italic; }
.recording-border { border-style: solid; border-color: #ef4444; background: rgba(239, 68, 68, 0.05); }

.mic-btn {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  border: none;
  background: var(--primary-color);
  color: #212529;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: var(--shadow-md);
}
.mic-btn:hover:not(.recording) { background: var(--primary-hover); transform: translateY(-2px); box-shadow: var(--shadow-lg); }
.mic-group { display: flex; align-items: center; gap: 12px; }
.timer { color: #ef4444; font-weight: 700; font-family: 'JetBrains Mono', monospace; font-size: 1.1rem; }
@keyframes pulse { 0% { box-shadow: 0 0 0 0 rgba(239, 68, 68, 0.4); } 70% { box-shadow: 0 0 0 15px rgba(239, 68, 68, 0); } 100% { box-shadow: 0 0 0 0 rgba(239, 68, 68, 0); } }
.mic-btn.recording { background: #ef4444; color: white; animation: pulse 1.5s infinite; transform: scale(1.1); }

/* Feedback Section */
.feedback-actions { display: flex; justify-content: center; gap: 16px; margin-top: 40px; padding-bottom: 40px; }
.action-btn {
  display: flex; align-items: center; gap: 8px; padding: 14px 28px; border-radius: 16px; font-weight: 700; font-size: 1.1rem; cursor: pointer; transition: all 0.2s; border: none;
}
.action-btn .material-icons { font-size: 1.4rem; }
.action-btn.secondary { background: #FFEBEE; border: 2px solid #FFCDD2; color: #D32F2F; }
.action-btn.secondary:hover { background: #FFCDD2; border-color: #EF9A9A; color: #B71C1C; }
.action-btn.primary { background: var(--primary-color); color: #212529; box-shadow: var(--shadow-md); }
.action-btn.primary:hover { background: var(--primary-hover); transform: translateY(-2px); box-shadow: var(--shadow-lg); }

.bookmark-tabs { display: flex; gap: 8px; margin-bottom: -1px; }
.bookmark {
  padding: 12px 24px; background: var(--bg-tertiary); border: var(--border-primary); border-bottom: none; border-radius: 12px 12px 0 0; font-weight: 700; cursor: pointer; transition: all 0.2s; color: var(--text-tertiary);
}
.bookmark:hover:not(.active) { background: var(--bg-secondary); color: var(--text-secondary); }
.bookmark.active { background: var(--primary-color); color: #212529; border-bottom: 2px solid var(--primary-color); transform: translateY(-2px); }

.feedback-card {
  background: var(--bg-secondary); border: var(--border-primary); border-radius: 0 20px 20px 20px; padding: 32px; box-shadow: var(--shadow-lg); position: relative;
}
.result-title { font-size: 1.4rem; font-weight: 800; color: var(--text-primary); display: inline-block; padding-bottom: 4px; margin-bottom: 20px; position: relative; }
.result-title::after { content: ''; position: absolute; bottom: 0; left: 0; width: 100%; height: 4px; background: var(--primary-color); border-radius: 2px; }

.badge {
  display: inline-flex; align-items: center; justify-content: center; padding: 4px 12px; border-radius: 10px; font-weight: 800; font-size: 0.75rem; border: 1.5px solid transparent; box-shadow: 0 2px 4px rgba(0,0,0,0.05); margin-right: 8px; flex-shrink: 0;
}
.badge.orig { background: #FFF5F5; color: #E03131; border: 1px solid #FFA8A8; border-radius: 8px; }
.badge.impr { background: #EBFBEE; color: #2F9E44; border: 1px solid #96F2A7; border-radius: 8px; }

.report-span { transition: all 0.2s; padding: 2px 0; cursor: pointer; }
.report-span:hover { background: var(--primary-light); }
.report-span.highlighted { background: var(--primary-color); color: #212529; padding: 2px 4px; border-radius: 4px; font-weight: 700; }

.detail-list { display: flex; flex-direction: column; gap: 16px; }
.detail-item {
  padding: 28px; background: #FFFFFF; border: 1px solid #F5EAD8; border-radius: 20px; cursor: pointer; transition: all 0.3s var(--ease-smooth); box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03); margin-left: 96px; border: 2px solid var(--border-color);
}
.detail-item:hover { border-color: var(--honey-300); box-shadow: 0 10px 25px rgba(0, 0, 0, 0.06); }
@media (max-width: 768px) { .detail-item { margin-left: 0; } }

.report-box-inner { line-height: 1.8; font-size: 1.05rem; color: var(--text-primary); }
.reason-icon { font-size: 1.1rem; color: var(--primary-color); vertical-align: middle; margin-right: 4px; }
.sentence-row { margin-bottom: 12px; font-size: 0.95rem; line-height: 1.5; display: flex; align-items: center; }
.reason-text { font-size: 0.875rem; color: var(--text-tertiary); margin-top: 12px; padding-top: 12px; border-top: 1px dotted var(--border-primary); }
.detail-item.selected-card { border-color: var(--primary-color); background: var(--bg-secondary); box-shadow: var(--shadow-md); transform: translateX(8px); }

.overall-section { display: flex; flex-direction: column; gap: 24px; }
.overall-item {
  background: #FFFFFF; padding: 28px; border-radius: 20px; border: 1px solid #F5EAD8; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03); transition: all 0.3s var(--ease-smooth);
}
.overall-item:hover { box-shadow: 0 10px 25px rgba(0, 0, 0, 0.06); border-color: var(--honey-300); }
.overall-label {
  display: inline-flex; padding: 6px 14px; background: var(--honey-100); color: #8B7300; border: 1px solid var(--honey-200); border-radius: 12px; font-size: 0.85rem; font-weight: 800; margin: 0 0 16px 0; letter-spacing: -0.01em;
}
.overall-text { font-size: 1.05rem; line-height: 1.75; color: var(--text-primary); margin: 0; white-space: pre-wrap; font-weight: 500; }

/* Okkul Speech Bubble */
.okkul-feedback-container { display: flex; align-items: flex-start; gap: 16px; margin-top: 12px; margin-bottom: 24px; }
.okkul-character { display: flex; flex-direction: column; align-items: center; flex-shrink: 0; width: 80px; }
.okkul-img { width: 60px; height: 60px; object-fit: contain; margin-bottom: 4px; }
.okkul-name { font-size: 0.8rem; font-weight: 700; color: var(--text-secondary); }

.speech-bubble {
  position: relative; background: #FFFFFF; border: 2px solid var(--primary-color); border-radius: 16px; padding: 20px 24px; flex-grow: 1; box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}
.speech-bubble::after {
  content: ''; position: absolute; top: 24px; left: -10px; width: 16px; height: 16px; background: #FFFFFF; border-bottom: 2px solid var(--primary-color); border-left: 2px solid var(--primary-color); transform: rotate(45deg); border-radius: 0 0 0 4px;
}
.bubble-text { font-size: 1rem; line-height: 1.7; color: #333; margin: 0; white-space: pre-wrap; }

/* Bubble Header (Title + Copy Btn) */
.bubble-header-row {
  display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; border-bottom: 1px dashed rgba(0,0,0,0.1); padding-bottom: 8px;
}
.bubble-title { font-size: 1rem; font-weight: 800; color: #8B7300; margin: 0; display: flex; align-items: center; gap: 6px; }

/* Copy Button */
.copy-btn {
  display: flex; align-items: center; justify-content: center; gap: 6px; padding: 4px 8px; background: white; border: 1px solid #F5EAD8; border-radius: 8px; font-size: 0.85rem; font-weight: 700; color: #8B7300; cursor: pointer; transition: all 0.2s;
}
.copy-btn:hover { background: #FFF9EB; border-color: #ffd700; transform: translateY(-1px); }
.copy-btn.copied { border-color: #4ade80; color: #16a34a; background: #f0fdf4 !important; }
.copy-btn.mini { padding: 4px 8px; background: transparent; border: none; }
.copy-btn.mini:hover { background: rgba(255, 215, 0, 0.1); }

/* Toast */
.copy-toast {
  position: fixed; bottom: 40px; left: 50%; transform: translateX(-50%); background: rgba(33, 37, 41, 0.9); color: white; padding: 12px 24px; border-radius: 999px; display: flex; align-items: center; gap: 10px; font-weight: 600; z-index: 2000; box-shadow: 0 4px 15px rgba(0,0,0,0.2);
}
.copy-toast .material-icons { color: #4ade80; }
.toast-enter-active, .toast-leave-active { transition: all 0.3s ease; }
.toast-enter-from, .toast-leave-to { opacity: 0; transform: translate(-50%, 20px); }

/* Pagination */
.pagination { display: flex; align-items: center; justify-content: center; gap: 20px; margin-top: 32px; padding-top: 24px; border-top: 1px solid var(--border-thin); }
.page-btn {
  width: 40px; height: 40px; border-radius: 50%; border: var(--border-primary); background: var(--bg-secondary); cursor: pointer; display: flex; align-items: center; justify-content: center; transition: all 0.2s; color: var(--text-primary); box-shadow: var(--shadow-sm);
}
.page-btn:hover:not(:disabled) { background: var(--primary-color); color: #212529; border-color: var(--primary-color); transform: translateY(-2px); box-shadow: var(--shadow-md); }
.page-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.page-info { font-size: 0.875rem; font-weight: 700; color: var(--text-secondary); }

.analyze-btn-wrapper { display: flex; flex-direction: column; align-items: center; gap: 16px; margin-top: 20px; }
.recording-status {
  background: rgba(16, 185, 129, 0.1); padding: 8px 20px; border-radius: 20px; font-size: 0.875rem; font-weight: 700; color: #10b981; display: flex; align-items: center; gap: 8px;
}
.recording-status::before { content: ""; display: inline-block; width: 8px; height: 8px; background: #10b981; border-radius: 50%; animation: pulse-green 1.5s infinite; }
@keyframes pulse-green { 0% { transform: scale(1); opacity: 1; } 50% { transform: scale(1.5); opacity: 0.5; } 100% { transform: scale(1); opacity: 1; } }

.analyze-btn {
  min-width: 280px; padding: 16px 32px; background: var(--primary-color); color: #212529; border: none; border-radius: 12px; font-size: 1.1rem; font-weight: 700; cursor: pointer; transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1); box-shadow: var(--shadow-md);
}
.analyze-btn:hover:not(:disabled) { background: var(--primary-hover); transform: translateY(-4px); box-shadow: var(--shadow-lg); }
.analyze-btn:disabled { background: var(--bg-tertiary); color: var(--text-tertiary); cursor: not-allowed; box-shadow: none; }

.error-box {
  margin-top: 20px; padding: 16px; background-color: rgba(239, 68, 68, 0.1); color: #ef4444; border: 1px solid rgba(239, 68, 68, 0.2); border-radius: 12px; font-weight: 600; text-align: center;
}
</style>
