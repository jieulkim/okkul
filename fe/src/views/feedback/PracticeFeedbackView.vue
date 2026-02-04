<script setup>
import { ref, computed, onMounted, inject } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { historyApi } from '@/api';
import okkulSvg from '@/assets/images/okkul.svg';

const router = useRouter();
const route = useRoute();

// 피드백 데이터 상태
const errorMessage = ref('');
const allQuestions = ref([]); // 연습 세션의 모든 문항
const selectedQuestionIndex = ref(0); // 현재 선택된 문항 인덱스
const selectedAttemptIndex = ref(0); // 현재 선택된 시도 인덱스
const selectedCorrectionIndex = ref(null);
const isLoading = ref(false);

// 현재 선택된 문항 정보
const currentQuestion = computed(() => allQuestions.value[selectedQuestionIndex.value] || null);

// 현재 선택된 시도(Attempt) 정보
const currentAttempt = computed(() => {
  const q = currentQuestion.value;
  if (!q || !q.attempts || q.attempts.length === 0) return null;
  return q.attempts[selectedAttemptIndex.value] || q.attempts[q.attempts.length - 1];
});

// 피드백 데이터 매핑 (computed로 전환하여 선택 변경 시 자동 업데이트)
const feedbackData = computed(() => {
  const attempt = currentAttempt.value;
  if (!attempt) return null;

  return {
    userEnglishScript: attempt.userAnswer?.englishScript || '',
    feedbackResult: {
      overallComment: [
        attempt.feedback?.fluencyFeedback,
        attempt.feedback?.logicFeedback,
        attempt.feedback?.relevanceFeedback
      ].filter(Boolean).join('\n\n') || '종합 평가가 아직 준비되지 않았습니다.',
      scriptCorrections: attempt.feedback?.sentenceDetails?.map(detail => ({
        originalSegment: detail.targetSegment,
        correctedSegment: detail.correctedSegment,
        comment: detail.comment
      })) || []
    }
  };
});

// 하이라이트 처리를 위한 데이터
const highlightedWords = computed(() => {
  const corrections = feedbackData.value?.feedbackResult?.scriptCorrections || [];
  const script = feedbackData.value?.feedbackResult?.overallComment || ''; // 실제론 improved script가 필요할 수 있음
  
  // 현재 구조에 맞게 교정된 텍스트 위주로 간단히 구성 (실제 개선 스크립트 필드 필요 시 보완)
  const improvedScript = currentAttempt.value?.feedback?.improvedAnswer || ''; 
  if (!improvedScript) return [];

  // 교정 항목들을 하이라이트 처리하기 위한 로직
  // 여기서는 단순히 모든 단어를 파싱하고, 교정된 구간이 포함된 경우 하이라이트
  return improvedScript.split(/\s+/).map(word => {
    const isHighlighted = corrections.some(c => c.correctedSegment.includes(word));
    return { text: word, highlighted: isHighlighted };
  });
});

// 컴포넌트 마운트 시 데이터 로드
onMounted(() => {
  loadFeedback();
});

const loadFeedback = async () => {
  const practiceId = route.query.practiceId;
  // questionId 파싱 안전하게 처리
  let qParam = route.query.questionId;
  let questionId = qParam ? parseInt(qParam) : null;
  
  if (!practiceId) {
    errorMessage.value = '잘못된 접근입니다. 연습 ID가 필요합니다.';
    return;
  }

  try {
    isLoading.value = true;
    errorMessage.value = '';

    // Mock Mode Support
    if (import.meta.env.MODE === 'mock') {
         console.warn('Mock Mode: Using dummy practice feedback data');
         await new Promise(resolve => setTimeout(resolve, 500));
         feedbackData.value = {
             userEnglishScript: "I like watching movies. My favorite movie is Interstellar. It is very interesting.",
             feedbackResult: {
                 overallComment: "전반적으로 자연스러운 답변입니다. 다만, 형용사 사용을 더 다양하게 하면 좋겠습니다.",
                 scriptCorrections: [
                     {
                         originalSegment: "It is very interesting",
                         correctedSegment: "It is absolutely fascinating",
                         comment: "'Interesting'보다 'fascinating'이 더 강렬한 인상을 줍니다."
                     }
                 ]
             }
         };
         return;
    }

    // 1. 연습 히스토리 상세 조회
    console.log('[PracticeFeedback] Fetching detail for practiceId:', practiceId);
    const { data } = await historyApi.getPracticeHistoryDetail(practiceId);
    console.log('[PracticeFeedback] API Response:', data);
    
    if (!data || !data.questions || data.questions.length === 0) {
        throw new Error('이 연습에 대한 질문 데이터가 존재하지 않습니다.');
    }

    allQuestions.value = data.questions;

    // 2. 초기 문항 및 시도 설정
    if (questionId) {
        const idx = data.questions.findIndex(q => q.questionId === questionId);
        if (idx !== -1) {
            selectedQuestionIndex.value = idx;
        }
    } else {
        selectedQuestionIndex.value = 0;
    }

    // 기본적으로 마지막 시도 선택
    const q = allQuestions.value[selectedQuestionIndex.value];
    if (q && q.attempts) {
        selectedAttemptIndex.value = q.attempts.length - 1;
    }

  } catch (error) {
    console.error('피드백 로드 실패:', error);
    errorMessage.value = error.message || '피드백 데이터를 불러오는 중 오류가 발생했습니다.';
  } finally {
      isLoading.value = false;
  }
};
// 시도 변경
const nextAttempt = () => {
  if (selectedAttemptIndex.value < currentQuestion.value.attempts.length - 1) {
    selectedAttemptIndex.value++;
    selectedCorrectionIndex.value = null;
  }
};

const prevAttempt = () => {
  if (selectedAttemptIndex.value > 0) {
    selectedAttemptIndex.value--;
    selectedCorrectionIndex.value = null;
  }
};

const setAttempt = (index) => {
  selectedAttemptIndex.value = index;
  selectedCorrectionIndex.value = null;
};

// 문항 변경
const setQuestion = (index) => {
  selectedQuestionIndex.value = index;
  selectedAttemptIndex.value = allQuestions.value[index].attempts.length - 1;
  selectedCorrectionIndex.value = null;
};

// 교정 항목 선택
const selectCorrection = (index) => {
  selectedCorrectionIndex.value = selectedCorrectionIndex.value === index ? null : index;
};
</script>

<template>
  <div class="practice-feedback-page">
    <!-- 로딩 상태 -->
    <div v-if="isLoading" class="loading-container">
      <div class="spinner"></div>
      <p>피드백을 불러오는 중입니다...</p>
    </div>

    <div v-else-if="feedbackData" class="feedback-container">
        <div class="feedback-header">
          <button @click="router.push('/feedback?category=PRACTICE')" class="back-btn">
            <span class="material-icons">arrow_back</span>
            목록으로
          </button>
        <div class="header-text-group">
            <h1 class="feedback-title">유형별 연습 피드백</h1>
            <p class="practice-topic">{{ allQuestions[0]?.topicTitle || '연습 결과' }}</p>
          </div>
        </div>

        <!-- 1. 문항 선택 탭 -->
        <div class="question-selector">
          <button 
            v-for="(q, idx) in allQuestions" 
            :key="q.questionId"
            class="q-tab-btn"
            :class="{ active: selectedQuestionIndex === idx }"
            @click="setQuestion(idx)"
          >
            Question {{ idx + 1 }}
          </button>
        </div>

        <div class="feedback-main-content">
          <!-- 2. 시도(Attempt) 캐러셀 컨트롤 -->
          <div class="attempt-carousel-header">
            <div class="attempt-info">
              <span class="attempt-label">답변 시도</span>
              <span class="attempt-count">{{ selectedAttemptIndex + 1 }} / {{ currentQuestion.attempts.length }}</span>
            </div>
            <div class="carousel-controls">
              <div class="carousel-indicators">
                <span 
                  v-for="(_, idx) in currentQuestion.attempts" 
                  :key="idx"
                  class="indicator-dot"
                  :class="{ active: selectedAttemptIndex === idx }"
                  @click="setAttempt(idx)"
                ></span>
              </div>
            </div>
          </div>

          <!-- 질문 텍스트 -->
          <div class="question-display-card">
            <span class="q-badge">Q {{ selectedQuestionIndex + 1 }}</span>
            <p class="question-text">{{ currentQuestion.questionText }}</p>
          </div>

          <div class="attempt-content-wrapper">
            <!-- 캐러셀 이동 버튼 (오버레이 스타일 - 사용자 요청) -->
            <button 
              class="side-nav-btn prev" 
              v-if="selectedAttemptIndex > 0"
              @click="prevAttempt"
            >
              <span class="material-icons">chevron_left</span>
            </button>

            <div class="enhanced-section">
                <!-- 오꿀쌤 추천 답변 (상단 캐릭터 + 하단 100% 너비 말풍선) -->
                <div class="okkul-feedback-container full-width-layout">
                  <div class="okkul-character-header">
                    <img :src="okkulSvg" alt="오꿀쌤" class="okkul-img-small" />
                    <span class="okkul-name">오꿀쌤의 교정 스크립트</span>
                  </div>
                  <div class="speech-bubble-full">
                    <p class="section-description">노란색 표시는 개선된 부분입니다. 단어를 클릭하면 상세 설명을 볼 수 있습니다.</p>
                    <div class="script-box enhanced-script">
                      <span 
                        v-for="(word, idx) in highlightedWords" 
                        :key="idx"
                        :class="{ 'highlighted-word': word.highlighted }"
                        class="script-word"
                      >
                        {{ word.text }}
                      </span>
                    </div>
                  </div>
                </div>

                <!-- 원본 스크립트 비교 (100% 너비) -->
                <div class="section-card original-card-full">
                    <h3 class="subsection-title">내 원본 답변</h3>
                    <div class="script-box original-script">
                      <p>{{ feedbackData.userEnglishScript }}</p>
                    </div>
                </div>
            </div>

            <button 
              class="side-nav-btn next" 
              v-if="selectedAttemptIndex < currentQuestion.attempts.length - 1"
              @click="nextAttempt"
            >
              <span class="material-icons">chevron_right</span>
            </button>
          </div>
        </div>

        <!-- 교정 항목 상세 -->
        <div class="corrections-section">
          <h2 class="section-title">
            <span class="material-icons">edit_note</span>
            문법 및 어휘 교정
          </h2>
          
          <div v-if="feedbackData.feedbackResult?.scriptCorrections?.length > 0" class="corrections-list">
            <div 
              v-for="(correction, index) in feedbackData.feedbackResult.scriptCorrections"
              :key="index"
              class="correction-item"
              :class="{ 'selected': selectedCorrectionIndex === index }"
              @click="selectCorrection(index)"
            >
              <div class="correction-number">{{ index + 1 }}</div>
              <div class="correction-content">
                <div class="correction-comparison">
                  <div class="before">
                    <span class="label">수정 전</span>
                    <span class="text original">{{ correction.originalSegment }}</span>
                  </div>
                  <span class="material-icons arrow">arrow_forward</span>
                  <div class="after">
                    <span class="label">수정 후</span>
                    <span class="text corrected">{{ correction.correctedSegment }}</span>
                  </div>
                </div>
                <div class="correction-comment">
                  <span class="material-icons">lightbulb</span>
                  {{ correction.comment }}
                </div>
              </div>
            </div>
          </div>
          <div v-else class="no-corrections">
            <span class="material-icons">check_circle</span>
            <p>완벽합니다! 교정이 필요한 부분이 없습니다.</p>
          </div>
        </div>

        <!-- 종합 피드백 -->
        <div class="overall-section">
          <div class="okkul-feedback-container full-width-layout">
            <div class="okkul-character-header">
              <img :src="okkulSvg" alt="오꿀쌤" class="okkul-img-small" />
              <span class="okkul-name">오꿀쌤의 종합 평가</span>
            </div>
            <div class="speech-bubble-full">
              <p class="bubble-text">{{ feedbackData.feedbackResult?.overallComment || '종합 평가가 아직 준비되지 않았습니다.' }}</p>
            </div>
          </div>
        </div>

        <!-- 액션 버튼 -->
        <div class="action-buttons">
          <button @click="router.push('/practice')" class="action-btn retry-btn">
            <span class="material-icons">refresh</span>
            다른 유형 연습하기
          </button>
          <button @click="router.push('/')" class="action-btn home-btn">
            <span class="material-icons">home</span>
            홈으로
          </button>
        </div>
    </div>

    <!-- 에러 화면 -->
    <div v-else class="error-screen">
      <span class="material-icons error-icon">error_outline</span>
      <p class="error-message">{{ errorMessage || '피드백 데이터를 불러올 수 없습니다.' }}</p>
      <button @click="router.push('/practice')" class="back-link btn-secondary">돌아가기</button>
    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@400;500;700;900&display=swap');
@import url('https://fonts.googleapis.com/icon?family=Material+Icons');

/* 컨테이너 및 기본 공백 */
.practice-feedback-page {
  min-height: 100vh;
  background: #FDFBF5; /* Soft beige background */
  padding: 48px 24px;
}

.feedback-container {
  max-width: 1200px; /* Increased from 1000px */
  margin: 0 auto;
  animation: slideUpFade 0.6s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

/* 헤더 그룹 */
.feedback-header {
  margin-bottom: 40px;
  display: flex;
  align-items: center;
  gap: 24px;
}

.header-text-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.practice-topic {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--primary-color);
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: var(--bg-secondary);
  border: 1px solid var(--border-primary);
  border-radius: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--text-secondary);
}

.back-btn:hover {
  background: var(--bg-tertiary);
  color: var(--text-primary);
  transform: translateY(-2px);
}

.feedback-title {
  font-size: 2rem;
  font-weight: 900;
  color: var(--text-primary);
}

/* 문항 선택기 (탭) */
.question-selector {
  display: flex;
  gap: 8px;
  margin-bottom: 32px;
  padding: 6px;
  background: #f1f5f9;
  border-radius: 16px;
  width: fit-content;
}

.q-tab-btn {
  padding: 10px 24px;
  border: none;
  background: transparent;
  border-radius: 10px;
  font-weight: 700;
  color: var(--text-tertiary);
  cursor: pointer;
  transition: all 0.2s;
}

.q-tab-btn.active {
  background: #FFFFFF;
  color: #212529;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

/* 캐러셀 헤더 (시도 정보 + 컨트롤) */
.attempt-carousel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 0 8px;
}

.attempt-info {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.attempt-label {
  font-size: 0.9rem;
  font-weight: 700;
  color: var(--text-tertiary);
}

.attempt-count {
  font-size: 1.25rem;
  font-weight: 900;
  color: var(--text-primary);
}

.carousel-controls {
  display: flex;
  align-items: center;
  gap: 16px;
}

.carousel-nav-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 1px solid #e2e8f0;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.carousel-nav-btn:hover:not(:disabled) {
  background: #f8fafc;
  border-color: var(--primary-color);
}

.carousel-nav-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.carousel-indicators {
  display: flex;
  gap: 6px;
}

.indicator-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #cbd5e1;
  cursor: pointer;
  transition: all 0.3s;
}

.indicator-dot.active {
  background: var(--primary-color);
  width: 20px;
  border-radius: 4px;
}

/* 질문 표시 카드 */
.question-display-card {
  background: #FFFFFF;
  padding: 32px;
  border-radius: 20px;
  border: 1px solid var(--border-primary);
  margin-bottom: 32px;
  display: flex;
  align-items: flex-start; /* Badge at top even if text is long */
  gap: 16px;
  box-shadow: var(--shadow-sm);
}

.q-badge {
  background: #212529;
  color: white;
  padding: 6px 14px;
  border-radius: 8px;
  font-weight: 800;
  font-size: 0.9rem;
  flex-shrink: 0;
  margin-top: 2px;
}

.question-text {
  font-size: 1.15rem;
  font-weight: 500;
  color: var(--text-primary);
  line-height: 1.6;
}

/* 캐러셀 내용 래퍼 */
.attempt-content-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  gap: 24px;
}

.side-nav-btn {
  position: absolute;
  top: 40%;
  transform: translateY(-50%);
  width: 56px;
  height: 56px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 8px 16px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 10;
  transition: all 0.3s;
}

.side-nav-btn:hover {
  background: white;
  transform: translateY(-50%) scale(1.1);
  box-shadow: 0 12px 24px rgba(0,0,0,0.15);
}

.side-nav-btn.prev {
  left: -72px; /* Pull further out to avoid overlap */
}

.side-nav-btn.next {
  right: -72px; /* Pull further out to avoid overlap */
}

.side-nav-btn .material-icons {
  font-size: 32px;
  color: var(--primary-color);
}

.feedback-main-content {
  width: 100%;
}

/* 오꿀쌤 피드백 컨테이너 (100% 너비 레이아웃) */
.okkul-feedback-container.full-width-layout {
  display: block;
  width: 100%;
  margin-bottom: 24px;
}

.okkul-character-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  padding-left: 8px;
}

.okkul-img-small {
  width: 44px;
  height: 44px;
  object-fit: contain;
}

.okkul-name {
  font-size: 1.1rem;
  font-weight: 800;
  color: #8B7300;
}

.speech-bubble-full {
  background: #FFF9EB; /* Soft cream color */
  border: 2px solid #F5EAD8;
  border-radius: 20px;
  padding: 32px;
  width: 100%;
  box-shadow: var(--shadow-sm);
  position: relative;
}

/* 말풍선 꼬리 (상단으로 변경) */
.speech-bubble-full::after {
  content: '';
  position: absolute;
  top: -12px;
  left: 20px;
  width: 20px;
  height: 20px;
  background: #FFF9EB;
  border-top: 2px solid #F5EAD8;
  border-left: 2px solid #F5EAD8;
  transform: rotate(45deg);
  border-radius: 4px 0 0 0;
}

.bubble-text {
  font-size: 1.15rem;
  line-height: 1.8;
  color: #333;
  margin: 0;
  white-space: pre-wrap;
  font-weight: 500;
}

/* 카드공통 */
.section-card {
  background: #FFF9EB; /* Soft cream color */
  border-radius: 20px;
  padding: 32px;
  box-shadow: var(--shadow-sm);
  border: 2px solid #F5EAD8;
  margin-bottom: 24px;
}

/* 원본 답변 카드 (100% 너비 유지) */
.original-card-full {
  width: 100%;
}

.subsection-title {
  font-size: 1.15rem;
  font-weight: 800;
  margin-bottom: 20px;
  color: #8B7300;
}

.script-box {
  padding: 24px;
  border-radius: 12px;
  line-height: 1.8;
  font-size: 1.15rem;
  background: #FFFFFF; /* White for content readability on cream card */
}

.enhanced-script {
  border: 1px solid #f5ead8;
}

.original-script {
  color: var(--text-secondary);
  border: 1px solid #e2e8f0;
}

.highlighted-word {
  background: #fffce0;
  color: #E65100;
  padding: 2px 4px;
  border-bottom: 2px solid #ffd700;
  font-weight: 700;
}

/* 교정 섹션 */
.corrections-section {
  margin-top: 48px;
}

.correction-item {
  display: flex;
  gap: 24px;
  background: #FFF;
  border-radius: 20px;
  padding: 24px;
  border: 1px solid #e2e8f0;
  margin-bottom: 16px;
  cursor: pointer;
  transition: all 0.2s;
}

.correction-item:hover {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-md);
}

.correction-number {
  width: 36px;
  height: 36px;
  background: var(--primary-color);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
}

.correction-comparison {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.text {
  padding: 8px 16px;
  border-radius: 8px;
  font-weight: 600;
}

.original { background: #fee2e2; color: #ef4444; text-decoration: line-through; }
.corrected { background: #dcfce7; color: #16a34a; }

.correction-comment {
  display: flex;
  gap: 8px;
  color: var(--text-secondary);
  font-size: 0.95rem;
}

/* 액션 버튼 */
.action-buttons {
  display: flex;
  gap: 16px;
  margin-top: 64px;
  justify-content: center;
}

.action-btn {
  padding: 16px 32px;
  border-radius: 14px;
  font-weight: 700;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s;
}

.retry-btn {
  background: var(--primary-color);
  border: none;
}

.home-btn {
  background: #FFFFFF;
  border: 1px solid #e2e8f0;
}

/* 에러 뷰 */
.error-screen {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 100px;
  gap: 20px;
}

@keyframes spin { to { transform: rotate(360deg); } }
.spinner { width: 40px; height: 40px; border: 4px solid #f3f3f3; border-top: 4px solid var(--primary-color); border-radius: 50%; animation: spin 1s linear infinite; }

@keyframes slideUpFade { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }
</style>