<script setup>
import { ref, computed, onUnmounted, onMounted } from 'vue'

// 1. 주제 관리
const allTopics = ref([
  '영화보기', '공원 가기', '음악 감상하기', '요리하기', '걷기', '독서',
  '여행하기', '사진 찍기', '게임하기', '운동하기', '카페 투어', '쇼핑하기',
  '언어 공부', '명상하기', '드라이브', '등산하기', '그림 그리기', '수영하기'
])

const selectedTopicFromBefore = '등산하기' 
const currentTopic = ref('')
const isTopicExpanded = ref(false)

const displayedTopics = computed(() => {
  // 처음엔 12개(2줄), 펼치면 전체
  return isTopicExpanded.value ? allTopics.value : allTopics.value.slice(0, 12)
})

onMounted(() => {
  currentTopic.value = allTopics.value.includes(selectedTopicFromBefore) 
    ? selectedTopicFromBefore 
    : allTopics.value[0]
})

// 2. 텍스트 및 STT 설정 (안정성 강화)
const koreanScript = ref('')
const maxChars = 1000
const sttResult = ref("")
const isRecording = ref(false)
const recordingTime = ref(0)
let recognition = null
let timerInterval = null

const initRecognition = () => {
  if (recognition) return; 
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
  if (!SpeechRecognition) return alert("크롬 브라우저를 사용해 주세요.")
  
  recognition = new SpeechRecognition()
  recognition.continuous = true
  recognition.interimResults = true
  recognition.lang = 'en-US'

  recognition.onresult = (event) => {
    let interimTranscript = ''
    let finalTranscript = ''
    for (let i = event.resultIndex; i < event.results.length; ++i) {
      if (event.results[i].isFinal) finalTranscript += event.results[i][0].transcript + ' '
      else interimTranscript += event.results[i][0].transcript
    }
    sttResult.value = finalTranscript + interimTranscript
  }
  recognition.onerror = () => { isRecording.value = false }
  recognition.onend = () => { if (isRecording.value) recognition.start() }
}

const toggleRecording = () => {
  if (isRecording.value) {
    recognition.stop()
    clearInterval(timerInterval)
    isRecording.value = false
  } else {
    initRecognition()
    sttResult.value = ""
    recordingTime.value = 0
    recognition.start()
    isRecording.value = true
    timerInterval = setInterval(() => {
      recordingTime.value++
      if (recordingTime.value >= 180) toggleRecording()
    }, 1000)
  }
}

// 3. 분석 및 탭 로직
const isAnalyzed = ref(false)
const currentTab = ref('sentence')
const selectedSentenceIndex = ref(null)

const feedbackData = ref([
  { original: "I go to the movie theater", improved: "I usually go to the movie theater", reason: "빈도 부사 'usually'를 사용하여 자연스러움을 더했습니다." },
  { original: "It was very fun", improved: "It was an absolutely thrilling experience", reason: "어휘 수준을 높여 더 생동감 있게 표현했습니다." },
  { original: "we eat popcorn", improved: "we enjoyed some popcorn", reason: "시제를 과거로 맞추고 감정을 나타내는 동사를 썼습니다." },
  { original: "with my friends", improved: "accompanied by my close friends", reason: "단순한 표현을 고급 어휘로 대체했습니다." }
])

const overallFeedback = ref("전반적인 문장 흐름이 좋습니다. 다만 몇 가지 어휘를 더 세련되게 다듬어 보았습니다.")

const highlightFromCard = (index) => {
  selectedSentenceIndex.value = index
}

const analyze = () => { isAnalyzed.value = true }
onUnmounted(() => { if (timerInterval) clearInterval(timerInterval) })
</script>

<template>
  <div class="practice-container">
    <nav class="topic-section">
      <div class="topic-grid" :class="{ 'expanded': isTopicExpanded }">
        <button v-for="t in displayedTopics" :key="t" 
          :class="['tab-btn', { active: currentTopic === t }]" @click="currentTopic = t">
          {{ t }}
        </button>
      </div>
      <button class="expand-btn" @click="isTopicExpanded = !isTopicExpanded">
        {{ isTopicExpanded ? '접기 ▲' : '주제 더보기 ▼' }}
      </button>
    </nav>

    <div class="main-grid">
      <section class="input-area">
        <div class="card">
          <div class="label-row">
            <label class="input-label">📝 한글로 써 보세요</label>
            <span class="count">{{ koreanScript.length }} / {{ maxChars }}</span>
          </div>
          <textarea v-model="koreanScript" :maxlength="maxChars" placeholder="이곳에 한글로 작성하세요"></textarea>
        </div>

        <div class="card">
          <div class="label-row">
            <label class="input-label">🎙️ 영어로 대답해보세요</label>
            <div class="mic-group">
              <span v-if="isRecording" class="timer">{{ Math.floor(recordingTime/60) }}:{{ (recordingTime%60).toString().padStart(2,'0') }}</span>
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
        <button class="analyze-btn" @click="analyze">AI 분석하기</button>
      </section>

      <section class="analysis-area" v-if="isAnalyzed">
        <div class="bookmark-tabs">
          <button :class="['bookmark', { active: currentTab === 'sentence' }]" @click="currentTab = 'sentence'">문장 피드백</button>
          <button :class="['bookmark', { active: currentTab === 'overall' }]" @click="currentTab = 'overall'">종합 피드백</button>
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
              <div v-for="(item, idx) in feedbackData" :key="idx" 
                class="detail-item" @click="highlightFromCard(idx)"
                :class="{ 'selected-card': selectedSentenceIndex === idx }">
                <div class="sentence-row"><span class="badge orig">기존</span> {{ item.original }}</div>
                <div class="sentence-row"><span class="badge impr">개선</span> {{ item.improved }}</div>
                <div class="reason-text">💡 {{ item.reason }}</div>
              </div>
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
.practice-container { max-width: 1000px; margin: 40px auto; padding: 0 20px; }

/* 1. 주제 선택: 6칸씩 2줄 고정 */
.topic-section { margin-bottom: 30px; width: 100%; }
.topic-grid { 
  display: grid; 
  grid-template-columns: repeat(6, minmax(0, 1fr)); 
  gap: 10px; 
  max-height: 96px; /* 2줄 높이로 제한 (버튼 높이 43px * 2 + gap 10px) */
  overflow: hidden; 
  transition: max-height 0.3s ease; 
}
.topic-grid.expanded { max-height: 600px; }

.tab-btn { 
  width: 100%;
  height: 43px; /* 고정 높이 */
  padding: 0 8px; 
  border-radius: 10px; 
  border: 1px solid #e2e8f0; 
  background: #fff; 
  cursor: pointer; 
  font-size: 13px; 
  font-weight: 700; 
  /* 한 줄로 표시하고 넘치면 ... 처리 */
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}
.tab-btn.active { 
  background: #FFD700; 
  border-color: #000; 
  box-shadow: 2px 2px 0 #000; 
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

/* 2. 레이아웃 */
.main-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 30px; }
@media (max-width: 850px) { .main-grid { grid-template-columns: 1fr; } }

.card { background: #fff; border-radius: 20px; padding: 20px; border: 1px solid #e2e8f0; margin-bottom: 20px; }
.label-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.input-label { font-weight: 800; font-size: 15px; }
textarea { width: 100%; height: 120px; border: none; background: #f8fafc; padding: 15px; border-radius: 12px; resize: none; box-sizing: border-box; font-size: 15px; }

.stt-box { min-height: 100px; background: #fdfcf0; border: 2px dashed #FFD700; border-radius: 12px; padding: 15px; }
.recording-border { border-style: solid; border-color: #ef4444; }

.mic-group { display: flex; align-items: center; gap: 10px; }
.mic-btn { width: 44px; height: 44px; border-radius: 50%; border: none; background: #f1f5f9; cursor: pointer; display: flex; align-items: center; justify-content: center; }
.mic-btn.recording { background: #ef4444; color: white; }
.timer { color: #ef4444; font-weight: bold; font-size: 14px; }

.analyze-btn { width: 100%; padding: 16px; background: #FFD700; border: 2px solid #000; border-radius: 50px; font-weight: 800; cursor: pointer; box-shadow: 0 4px 0 #000; }

/* 3. 오꿀이 스타일 */
.okkul-left-align { display: flex; justify-content: flex-start; margin: 15px 0; }
.okkul-mini-container { width: 65px; height: 65px; position: relative; animation: float 3s infinite ease-in-out; }
.platypus-body { position: relative; width: 65px; height: 65px; background: #C59358; border: 3px solid #000; border-radius: 50%; }
.platypus-hat { position: absolute; top: -10px; left: 50%; transform: translateX(-50%); width: 28px; height: 12px; background: #FFD700; border: 2.5px solid #000; border-radius: 4px; }
.platypus-eye { position: absolute; top: 26px; width: 6px; height: 6px; background: #000; border-radius: 50%; }
.platypus-eye.left { left: 18px; }
.platypus-eye.right { right: 18px; }
.platypus-bill { position: absolute; top: 34px; left: 50%; transform: translateX(-50%); width: 34px; height: 12px; background: #333; border: 2.5px solid #000; border-radius: 12px; }

.platypus-arm-right { 
  position: absolute; 
  right: -15px; 
  top: 32px; 
  width: 20px; 
  height: 9px; 
  background: #C59358; 
  border: 2.5px solid #000; 
  border-radius: 10px; 
  transform-origin: left center; 
}

.wave { animation: wave-motion 0.8s infinite alternate ease-in-out; }
@keyframes wave-motion { from { transform: rotate(10deg); } to { transform: rotate(-50deg); } }
@keyframes float { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-8px); } }
.jump-anim { animation: jump-action 0.4s ease-out; }
@keyframes jump-action { 0%, 100% { transform: scale(1); } 50% { transform: translateY(-20px) scale(1.1); } }

/* 4. 피드백 하단부 */
.bookmark-tabs { display: flex; gap: 5px; margin-bottom: -1px; }
.bookmark { padding: 10px 20px; background: #e2e8f0; border: 1px solid #cbd5e1; border-bottom: none; border-radius: 12px 12px 0 0; cursor: pointer; font-size: 14px; font-weight: bold; }
.bookmark.active { background: #fff; border-bottom: 2px solid #fff; z-index: 2; }
.feedback-card { background: #fff; border-radius: 0 20px 20px 20px; padding: 25px; border: 1px solid #e2e8f0; }
.result-title { font-size: 22px; font-weight: 900; border-bottom: 4px solid #FFD700; display: inline-block; padding-bottom: 2px; margin-bottom: 10px; }
.report-box { background: #f8fafc; padding: 20px; border-radius: 15px; line-height: 1.8; border: 1px solid #e2e8f0; margin-bottom: 20px; }
.highlighted { background: #FFD700; font-weight: 700; }
.detail-item { padding: 15px; border-radius: 16px; border: 1px solid #f1f5f9; margin-bottom: 10px; cursor: pointer; background: #fff; }
.selected-card { border: 2px solid #FFD700; background: #fffef0; }
.overall-box { background: #f8fafc; padding: 25px; border-radius: 15px; border-left: 5px solid #FFD700; line-height: 1.6; }
</style>