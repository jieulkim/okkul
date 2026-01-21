<script setup>
import { ref, computed, onUnmounted, onMounted, watch } from 'vue'

// ============================================
// Props 정의 (부모 컴포넌트에서 받을 데이터)
// ============================================
const props = defineProps({
  // 사용자 정보
  userId: {
    type: Number,
    required: true
  },
  // 연습 세션 정보
  practiceSession: {
    type: Object,
    default: () => ({
      practice_id: null,
      type_id: null,
      topic_id: null,
      set_id: null,
      started_at: null
    })
  },
  // 선택 가능한 주제 목록 (Topic 테이블에서)
  availableTopics: {
    type: Array,
    default: () => []
  },
  // 현재 문제 세트 정보 (question_set 테이블)
  currentQuestionSet: {
    type: Object,
    default: () => ({
      set_id: null,
      level: null,
      question_cnt: null,
      questions: [] // question_bank 테이블의 질문들
    })
  },
  // 이전에 저장된 답변 (있는 경우)
  savedAnswer: {
    type: Object,
    default: () => null
  }
})

// ============================================
// Emits 정의 (부모 컴포넌트로 보낼 이벤트)
// ============================================
const emit = defineEmits([
  'topic-changed',      // 주제 변경 시
  'answer-submitted',   // 답변 제출 시
  'question-changed'    // 질문 변경 시
])

// ============================================
// 1. 주제 관리 (Topic 테이블 기반)
// ============================================
const currentTopic = ref(null) // 선택된 topic_id
const isTopicExpanded = ref(false)

// 표시할 주제 목록 (12개씩 페이징)
const displayedTopics = computed(() => {
  return isTopicExpanded.value 
    ? props.availableTopics 
    : props.availableTopics.slice(0, 12)
})

// 주제 선택 핸들러
const selectTopic = (topicId) => {
  currentTopic.value = topicId
  emit('topic-changed', topicId)
}

// ============================================
// 2. 질문 관리 (question_bank 테이블 기반)
// ============================================
const currentQuestionIndex = ref(0)

// 현재 질문 정보
const currentQuestion = computed(() => {
  if (!props.currentQuestionSet.questions || 
      props.currentQuestionSet.questions.length === 0) {
    return null
  }
  return props.currentQuestionSet.questions[currentQuestionIndex.value]
})

const showQuestionText = ref(false)

// 오디오 재생 (question_bank의 audio_url)
const playQuestionAudio = () => {
  if (currentQuestion.value?.audio_url) {
    const audio = new Audio(currentQuestion.value.audio_url)
    audio.play().catch(e => console.error("오디오 재생 실패:", e))
  }
}

// 질문 번호 표시 (question_bank의 order 필드 사용)
const questionNumber = computed(() => {
  return currentQuestion.value?.order || (currentQuestionIndex.value + 1)
})

// ============================================
// 3. 답변 작성 (Practice_answers 테이블로 저장될 데이터)
// ============================================
const koreanScript = ref('')
const maxChars = 1000
const sttResult = ref("")
const finalTranscriptAccumulated = ref("")
const isRecording = ref(false)
const recordingTime = ref(0)
let recognition = null
let timerInterval = null
let audioRecorder = null
let audioChunks = []

// 저장된 답변 불러오기
watch(() => props.savedAnswer, (newAnswer) => {
  if (newAnswer) {
    koreanScript.value = newAnswer.korean_script || ''
    sttResult.value = newAnswer.english_script || ''
  }
}, { immediate: true })

// STT 초기화
const initRecognition = () => {
  if (recognition) return
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
  if (!SpeechRecognition) return alert("크롬 브라우저를 사용해 주세요.")
  
  recognition = new SpeechRecognition()
  recognition.continuous = true
  recognition.interimResults = true
  recognition.lang = 'en-US'

  recognition.onresult = (event) => {
    let interimTranscript = ''
    let newFinalTranscript = ''

    for (let i = event.resultIndex; i < event.results.length; ++i) {
      if (event.results[i].isFinal) {
        // 새로 확정된 문장을 누적 변수에 추가
        finalTranscriptAccumulated.value += event.results[i][0].transcript + ' '
      } else {
        // 아직 인식 중인 문장
        interimTranscript += event.results[i][0].transcript
      }
    }
    // 최종 표시용 변수 = (이전까지 확정된 것들) + (방금 확정된 것들) + (현재 말하고 있는 중인 것)
    sttResult.value = finalTranscriptAccumulated.value + interimTranscript
  }
  recognition.onerror = () => { isRecording.value = false }
  recognition.onend = () => { if (isRecording.value) recognition.start() }
}

// 오디오 녹음 초기화
const initAudioRecording = async () => {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    audioRecorder = new MediaRecorder(stream)
    audioChunks = []

    audioRecorder.ondataavailable = (event) => {
      audioChunks.push(event.data)
    }

    audioRecorder.onstop = () => {
      const audioBlob = new Blob(audioChunks, { type: 'audio/wav' })
      // 여기서 audioBlob을 서버로 전송하여 저장
      // Practice_answers 테이블의 english_record_url에 저장
    }

    audioRecorder.start()
  } catch (e) {
    console.error("오디오 녹음 초기화 실패:", e)
  }
}

// 녹음 토글
const toggleRecording = async () => {
  if (isRecording.value) {
    if (recognition) recognition.stop()
    if (audioRecorder && audioRecorder.state === 'recording') {
      audioRecorder.stop()
    }
    clearInterval(timerInterval)
    isRecording.value = false
  } else {
    initRecognition()
    await initAudioRecording()
    
    sttResult.value = ""
    recordingTime.value = 0
    
    try {
      recognition.start()
      isRecording.value = true
      timerInterval = setInterval(() => {
        recordingTime.value++
        if (recordingTime.value >= 180) toggleRecording()
      }, 1000)
    } catch (e) {
      console.error(e)
    }
  }
}

// ============================================
// 4. AI 분석 및 피드백 (Type_feedbacks 테이블)
// ============================================
const isAnalyzed = ref(false)
const currentTab = ref('sentence')
const selectedSentenceIndex = ref(null)
const currentPage = ref(0)
const itemsPerPage = 2

// 피드백 데이터 (API 응답으로 채워질 예정)
const feedbackData = ref([])
const overallFeedback = ref('')

// API 호출: 답변 분석 요청
const analyze = async () => {
  if (!currentQuestion.value) return

  // Practice_answers 테이블에 저장할 데이터 준비
  const answerData = {
    practice_id: props.practiceSession.practice_id,
    set_id: props.currentQuestionSet.set_id,
    question_id: currentQuestion.value.question_id,
    korean_script: koreanScript.value,
    english_script: sttResult.value,
    // english_record_url은 audioBlob 업로드 후 받은 URL
  }

  try {
    // API 호출 (예시)
    // const response = await fetch('/api/practice/analyze', {
    //   method: 'POST',
    //   headers: { 'Content-Type': 'application/json' },
    //   body: JSON.stringify(answerData)
    // })
    // const result = await response.json()
    
    // 더미데이터 제거됨 - 실제 API 연동 필요
    feedbackData.value = []
    overallFeedback.value = ""
    
    // isAnalyzed.value = true
    // currentPage.value = 0

    // 부모 컴포넌트로 답변 제출 알림
    emit('answer-submitted', answerData)
  } catch (error) {
    console.error("분석 요청 실패:", error)
    alert("분석에 실패했습니다. 다시 시도해주세요.")
  }
}

// 페이지네이션
const totalPages = computed(() => Math.ceil(feedbackData.value.length / itemsPerPage))
const paginatedFeedback = computed(() => {
  const start = currentPage.value * itemsPerPage
  const end = start + itemsPerPage
  return feedbackData.value.slice(start, end)
})

const goToNextPage = () => {
  if (currentPage.value < totalPages.value - 1) {
    currentPage.value++
    selectedSentenceIndex.value = null
  }
}

const goToPrevPage = () => {
  if (currentPage.value > 0) {
    currentPage.value--
    selectedSentenceIndex.value = null
  }
}

const highlightFromCard = (index) => {
  selectedSentenceIndex.value = currentPage.value * itemsPerPage + index
}

// ============================================
// 5. 초기화 및 정리
// ============================================
onMounted(() => {
  // 이전에 선택한 주제가 있으면 설정
  if (props.practiceSession.topic_id) {
    currentTopic.value = props.practiceSession.topic_id
  } else if (props.availableTopics.length > 0) {
    currentTopic.value = props.availableTopics[0].topic_id
  }
})

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval)
  if (recognition) recognition.stop()
  if (audioRecorder && audioRecorder.state === 'recording') {
    audioRecorder.stop()
  }
})
</script>

<template>
  <div class="practice-container">
    <!-- 주제 선택 (Topic 테이블 기반) -->
    <nav class="topic-section">
      <div class="topic-grid" :class="{ 'expanded': isTopicExpanded }">
        <button 
          v-for="topic in displayedTopics" 
          :key="topic.topic_id" 
          :class="['tab-btn', { active: currentTopic === topic.topic_id }]" 
          @click="selectTopic(topic.topic_id)">
          {{ topic.topic_name }}
        </button>
      </div>
      <button class="expand-btn" @click="isTopicExpanded = !isTopicExpanded">
        {{ isTopicExpanded ? '접기 ▲' : '주제 더보기 ▼' }}
      </button>
    </nav>

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
            <button class="toggle-q-btn" @click="showQuestionText = !showQuestionText">
              {{ showQuestionText ? '질문 숨기기' : '질문 텍스트 보기' }}
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
            <span class="count">{{ koreanScript.length }} / {{ maxChars }}</span>
          </div>
          <textarea 
            v-model="koreanScript" 
            :maxlength="maxChars" 
            placeholder="이곳에 한글로 작성하세요">
          </textarea>
        </div>

        <!-- 영어 답변 녹음 -->
        <div class="card">
          <div class="label-row">
            <label class="input-label">🎙️ 영어로 대답해보세요</label>
            <div class="mic-group">
              <span v-if="isRecording" class="timer">
                {{ Math.floor(recordingTime/60) }}:{{ (recordingTime%60).toString().padStart(2,'0') }}
              </span>
              <button @click="toggleRecording" :class="['mic-btn', { recording: isRecording }]">
                <span class="material-icons">{{ isRecording ? 'stop' : 'mic' }}</span>
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
          <button class="analyze-btn" @click="analyze">AI 분석하기</button>
        </div>
      </section>

      <!-- 피드백 결과 (Type_feedbacks 테이블 기반) -->
      <section class="analysis-area" v-if="isAnalyzed">
        <div class="bookmark-tabs">
          <button :class="['bookmark', { active: currentTab === 'sentence' }]" @click="currentTab = 'sentence'">
            문장 피드백
          </button>
          <button :class="['bookmark', { active: currentTab === 'overall' }]" @click="currentTab = 'overall'">
            종합 피드백
          </button>
        </div>

        <div class="feedback-card">
          <h3 class="result-title">오꿀 피드백</h3>
          
          <div class="okkul-left-align">
            <div class="okkul-mini-container" :class="{ 'jump-anim': selectedSentenceIndex !== null }">
              <div class="platypus-body">
                <div class="platypus-hat"></div>
                <div class="platypus-eye left"></div>
                <div class="platypus-eye right"></div>
                <div class="platypus-bill"></div>
                <div class="platypus-arm-right wave"></div>
              </div>
            </div>
          </div>

          <div v-if="currentTab === 'sentence'">
            <div class="report-box">
              <span v-for="(item, idx) in feedbackData" :key="idx" 
                :class="['report-span', { 'highlighted': selectedSentenceIndex === idx }]">
                {{ item.improved }}
              </span>
            </div>
            
            <div class="detail-list">
              <div v-for="(item, idx) in paginatedFeedback" :key="idx" 
                class="detail-item" @click="highlightFromCard(idx)"
                :class="{ 'selected-card': selectedSentenceIndex === (currentPage * itemsPerPage + idx) }">
                <div class="sentence-row"><span class="badge orig">기존</span> {{ item.original }}</div>
                <div class="sentence-row"><span class="badge impr">개선</span> {{ item.improved }}</div>
                <div class="reason-text">💡 {{ item.reason }}</div>
              </div>
            </div>

            <!-- 페이지네이션 -->
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
            <div class="overall-box">{{ overallFeedback }}</div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.practice-container { max-width: 1000px; margin: 40px auto; padding: 0 20px; color: var(--text-primary); }

/* 질문 영역 스타일 */
.question-container { margin-bottom: 20px; }
.question-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px; }
.q-id-group { display: flex; align-items: center; gap: 12px; }
.q-number { font-size: 32px; font-weight: 900; color: var(--text-primary); margin: 0; }
.audio-btn { 
  width: 40px; height: 40px; border-radius: 50%; border: 2px solid var(--text-primary); 
  background: var(--bg-primary); cursor: pointer; display: flex; align-items: center; justify-content: center;
  box-shadow: 2px 2px 0 var(--text-primary);
  color: var(--text-primary);
}
.audio-btn:active { transform: translate(1px, 1px); box-shadow: 1px 1px 0 var(--text-primary); }
.toggle-q-btn { background: none; border: none; color: var(--text-secondary); font-size: 13px; font-weight: bold; cursor: pointer; text-decoration: underline; }
.question-text-card { 
  background: var(--bg-secondary); border: 1px solid var(--border-color); border-radius: 12px; padding: 15px;
  font-size: 15px; font-weight: 600; color: var(--text-primary); line-height: 1.5;
}

/* 1. 주제 선택 */
.topic-section { margin-bottom: 30px; width: 100%; }
.topic-grid { 
  display: grid; 
  grid-template-columns: repeat(6, minmax(0, 1fr)); 
  gap: 10px; 
  max-height: 96px; 
  overflow: hidden; 
  transition: max-height 0.3s ease; 
}
.topic-grid.expanded { max-height: 600px; }

.tab-btn { 
  width: 100%; height: 43px; padding: 0 8px; border-radius: 10px; border: 1px solid var(--border-color); 
  background: var(--bg-primary); cursor: pointer; font-size: 13px; font-weight: 700; white-space: nowrap;
  overflow: hidden; text-overflow: ellipsis; display: flex; align-items: center; justify-content: center; box-sizing: border-box;
  color: var(--text-secondary);
}
.tab-btn.active { background: #FFD700; border-color: #000; box-shadow: 2px 2px 0 #000; color: #000; }
.expand-btn { display: block; margin: 15px auto 0; background: none; border: none; color: var(--text-secondary); cursor: pointer; font-weight: bold; text-decoration: underline; }

/* 2. 레이아웃 및 카드 */
.main-grid { display: flex; flex-direction: column; gap: 30px; }
.card { background: var(--bg-primary); border-radius: 20px; padding: 20px; border: 1px solid var(--border-color); margin-bottom: 20px; }
.label-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.input-label { font-weight: 800; font-size: 15px; color: var(--text-primary); }
.count { color: var(--text-secondary); }
textarea { 
  width: 100%; height: 120px; border: none; background: var(--bg-secondary); 
  padding: 15px; border-radius: 12px; resize: none; box-sizing: border-box; 
  font-size: 15px; color: var(--text-primary); 
}
textarea::placeholder { color: var(--text-secondary); opacity: 0.7; }

/* 3. STT 박스 */
.stt-box { 
  min-height: 100px; background: var(--bg-secondary); border: 2px dashed #FFD700; 
  border-radius: 12px; padding: 15px; font-size: 15px; color: var(--text-primary); 
}
:global(.dark-mode) .stt-box { background: rgba(255, 215, 0, 0.05); }

.recording-border { border-style: solid; border-color: #ef4444; }
.mic-btn { 
  width: 44px; height: 44px; border-radius: 50%; border: none; 
  background: var(--bg-secondary); cursor: pointer; display: flex; align-items: center; justify-content: center;
  color: var(--text-secondary); 
}
.mic-group { display: flex; align-items: center; gap: 10px; }
.timer { color: #ef4444; font-weight: bold; font-family: monospace; }
.mic-btn.recording { background: #ef4444; color: white; }

/* 4. 오꿀이 스타일*/
.okkul-left-align { display: flex; justify-content: flex-start; margin: 15px 0; }
.okkul-mini-container { width: 65px; height: 65px; position: relative; animation: float 3s infinite ease-in-out; }
.platypus-body { position: relative; width: 65px; height: 65px; background: #C59358; border: 3px solid #000; border-radius: 50%; }
.platypus-hat { position: absolute; top: -10px; left: 50%; transform: translateX(-50%); width: 28px; height: 12px; background: #333; border: 2.5px solid #000; border-radius: 4px; }
.platypus-eye { position: absolute; top: 26px; width: 6px; height: 6px; background: #000; border-radius: 50%; }
.platypus-eye.left { left: 18px; }
.platypus-eye.right { right: 18px; }
.platypus-bill { position: absolute; top: 34px; left: 50%; transform: translateX(-50%); width: 34px; height: 12px; background: #333; border: 2.5px solid #000; border-radius: 12px; }
.platypus-arm-right { position: absolute; right: -20px; top: 32px; width: 20px; height: 9px; background: #C59358; border: 2.5px solid #000; border-radius: 10px; transform-origin: left center; }
.wave { animation: wave-motion 0.8s infinite alternate ease-in-out; }
@keyframes wave-motion { from { transform: rotate(10deg); } to { transform: rotate(-50deg); } }
@keyframes float { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-8px); } }

/* 5. 분석 결과 섹션 */
.bookmark-tabs { display: flex; gap: 5px; margin-bottom: -1px; }
.bookmark { 
  padding: 10px 20px; background: var(--bg-secondary); border: 1px solid var(--border-color); 
  border-bottom: none; border-radius: 12px 12px 0 0; cursor: pointer; 
  font-size: 14px; font-weight: bold; color: var(--text-secondary); 
}
.bookmark.active { background: var(--bg-primary); border-bottom: 2px solid var(--bg-primary); z-index: 2; color: var(--text-primary); }
.feedback-card { background: var(--bg-primary); border-radius: 0 20px 20px 20px; padding: 25px; border: 1px solid var(--border-color); }
.result-title { font-size: 22px; font-weight: 900; border-bottom: 4px solid #FFD700; display: inline-block; padding-bottom: 2px; margin-bottom: 10px; color: var(--text-primary); }
.report-box { background: var(--bg-secondary); padding: 20px; border-radius: 15px; line-height: 1.8; border: 1px solid var(--border-color); margin-bottom: 20px; color: var(--text-primary); }
.badge { font-size: 10px; padding: 2px 6px; border-radius: 4px; margin-right: 5px; font-weight: bold; }
.badge.orig { background: var(--bg-secondary); color: var(--text-secondary); border: 1px solid var(--border-color); }
.badge.impr { background: #fff7ed; color: #ea580c; }
:global(.dark-mode) .badge.impr { background: #431407; color: #fb923c; }

.highlighted { background: #FFD700; font-weight: 700; color: #000; }
.detail-item { 
  padding: 15px; border-radius: 16px; border: 1px solid var(--bg-secondary); 
  margin-bottom: 10px; cursor: pointer; background: var(--bg-primary); 
  box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}
.sentence-row { color: var(--text-primary); margin-bottom: 6px; }
.reason-text { font-size: 13px; color: var(--text-secondary); margin-top: 8px; }

.selected-card { border: 2px solid #FFD700; background: #fffef0; }
:global(.dark-mode) .selected-card { background: #422006; }

.overall-box { 
  background: var(--bg-secondary); padding: 25px; border-radius: 15px; 
  border-left: 5px solid #FFD700; line-height: 1.6; color: var(--text-primary); 
}

/* 페이지네이션 */
.pagination { 
  display: flex; align-items: center; justify-content: center; gap: 15px; 
  margin-top: 20px; padding-top: 15px; border-top: 1px solid var(--border-color);
}
.page-btn { 
  width: 36px; height: 36px; border-radius: 50%; border: 2px solid var(--text-primary); 
  background: var(--bg-primary); cursor: pointer; display: flex; align-items: center; 
  justify-content: center; box-shadow: 2px 2px 0 var(--text-primary); transition: all 0.2s;
  color: var(--text-primary);
}
.page-btn:hover:not(:disabled) { background: #FFD700; border-color: #000; color: #000; box-shadow: 2px 2px 0 #000; }
.page-btn:active:not(:disabled) { transform: translate(1px, 1px); box-shadow: 1px 1px 0 var(--text-primary); }
.page-btn:disabled { opacity: 0.3; cursor: not-allowed; box-shadow: none; }
.page-info { font-size: 14px; font-weight: 700; color: var(--text-primary); min-width: 50px; text-align: center; }

/* AI 분석 버튼 */
.analyze-btn-wrapper { display: flex; justify-content: center; margin-top: 10px; }
.analyze-btn { 
  width: 180px; padding: 14px; background: #FFD700; border: 2px solid #000; 
  border-radius: 50px; font-weight: 800; cursor: pointer; box-shadow: 0 4px 0 #000;
  color: #000; 
}
.analyze-btn:active { transform: translateY(2px); box-shadow: 0 2px 0 #000; }
</style>