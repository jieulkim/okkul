<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { historyApi } from '@/api';
import okkulSvg from '@/assets/images/okkul.svg';

const router = useRouter();
const route = useRoute();

const errorMessage = ref('');
const allQuestions = ref([]);
const practiceTypeName = ref('');
const practiceTopicTitle = ref('');
const selectedQuestionIndex = ref(0);
const selectedAttemptIndex = ref(0);
const selectedCorrectionIndex = ref(null);
const isLoading = ref(false);
const feedbackViewMode = ref('interactive');
const currentView = ref('sentence');

const currentQuestion = computed(() => allQuestions.value[selectedQuestionIndex.value] || null);
const currentAttempt = computed(() => {
  const q = currentQuestion.value;
  if (!q || !q.attempts || q.attempts.length === 0) return null;
  return q.attempts[selectedAttemptIndex.value] || q.attempts[q.attempts.length - 1];
});

const feedbackData = computed(() => {
  const attempt = currentAttempt.value;
  if (!attempt) return null;
  return {
    userEnglishScript: attempt.userAnswer?.englishScript || '',
    feedbackResult: {
      fluency: attempt.feedback?.fluencyFeedback,
      logic: attempt.feedback?.logicFeedback,
      relevance: attempt.feedback?.relevanceFeedback,
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

const hasUserAnswer = computed(() => {
  return feedbackData.value?.userEnglishScript && feedbackData.value.userEnglishScript.trim().length > 0;
});

const interactiveImprovedScript = computed(() => {
  const improvedScript = currentAttempt.value?.feedback?.improvedAnswer || '';

  if (!hasUserAnswer.value || !feedbackData.value?.feedbackResult?.scriptCorrections?.length) {
    return improvedScript;
  }

  const corrections = feedbackData.value.feedbackResult.scriptCorrections;

  const scriptTokens = [];
  const regex = /[a-zA-Z0-9가-힣'-]+/g;
  let match;
  while ((match = regex.exec(improvedScript)) !== null) {
    scriptTokens.push({
      word: match[0].toLowerCase(),
      start: match.index,
      end: match.index + match[0].length
    });
  }

  const matchedSegments = [];

  corrections.forEach((correction, index) => {
    let targetText = correction.correctedSegment ? correction.correctedSegment.trim() : '';
    if (!targetText) return;

    const targetTokens = targetText.match(/[a-zA-Z0-9가-힣'-]+/g);
    if (!targetTokens || targetTokens.length === 0) return;

    const targetWords = targetTokens.map(w => w.toLowerCase());

    let foundStartIndex = -1;
    let foundEndIndex = -1;

    for (let i = 0; i <= scriptTokens.length - targetWords.length; i++) {
      let isMatch = true;
      for (let j = 0; j < targetWords.length; j++) {
        if (scriptTokens[i + j].word !== targetWords[j]) {
          isMatch = false;
          break;
        }
      }

      if (isMatch) {
        foundStartIndex = scriptTokens[i].start;
        foundEndIndex = scriptTokens[i + targetWords.length - 1].end;
        break;
      }
    }

    if (foundStartIndex !== -1) {
      matchedSegments.push({
        start: foundStartIndex,
        end: foundEndIndex,
        index: index,
        content: improvedScript.substring(foundStartIndex, foundEndIndex)
      });
    }
  });

  matchedSegments.sort((a, b) => a.start - b.start);

  const finalHtmlSegments = [];
  let currentPos = 0;

  matchedSegments.forEach((seg) => {
    if (seg.start < currentPos) return;

    if (seg.start > currentPos) {
      finalHtmlSegments.push(improvedScript.substring(currentPos, seg.start));
    }
    const isSelected = seg.index === selectedCorrectionIndex.value;
    const className = `clickable-correction ${isSelected ? 'selected' : ''}`;
    finalHtmlSegments.push(`<span class="${className}" data-index="${seg.index}">${seg.content}</span>`);
    currentPos = seg.end;
  });

  if (currentPos < improvedScript.length) {
    finalHtmlSegments.push(improvedScript.substring(currentPos));
  }

  return finalHtmlSegments.join('');
});

const highlightedUserScript = computed(() => {
  const userScript = feedbackData.value?.userEnglishScript || '';
  const selectedOriginalSegment = selectedCorrection.value?.originalSegment;

  if (!userScript || !selectedOriginalSegment) return userScript || '';

  const className = 'highlighted-original';
  const escapedSegment = selectedOriginalSegment.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  const regex = new RegExp(escapedSegment, 'g');
  return userScript.replace(regex, `<span class="${className}">${selectedOriginalSegment}</span>`);
});

const selectedCorrection = computed(() => {
  if (selectedCorrectionIndex.value === null) return null;
  return feedbackData.value?.feedbackResult?.scriptCorrections[selectedCorrectionIndex.value] || null;
});

const handleScriptClick = (event) => {
  if (!hasUserAnswer.value) return;

  const target = event.target;
  if (target.classList.contains('clickable-correction')) {
    const index = parseInt(target.dataset.index, 10);
    selectCorrection(index, 'script');
  }
};

const handlePopState = () => {
  window.history.pushState(null, '', window.location.href);
};

onMounted(() => {
  // 브라우저 뒤로가기 처리
  window.history.pushState(null, '', window.location.href);
  window.addEventListener('popstate', handlePopState);
  loadFeedback();
});

onBeforeUnmount(() => {
  window.removeEventListener('popstate', handlePopState);
});

const loadFeedback = async () => {
  const practiceId = route.query.practiceId;
  let qParam = route.query.questionId;
  let questionId = qParam ? parseInt(qParam) : null;

  if (!practiceId) {
    errorMessage.value = '잘못된 접근입니다. 연습 ID가 필요합니다.';
    return;
  }

  isLoading.value = true;
  try {
    const { data } = await historyApi.getPracticeHistoryDetail(practiceId);
    if (!data || !data.questions || data.questions.length === 0) {
      throw new Error('이 연습에 대한 질문 데이터가 존재하지 않습니다.');
    }
    allQuestions.value = data.questions;
    practiceTypeName.value = data.typeName || '';
    practiceTopicTitle.value = data.topicTitle || '';

    const initialQuestionIndex = questionId ? data.questions.findIndex(q => q.questionId === questionId) : 0;
    selectedQuestionIndex.value = initialQuestionIndex !== -1 ? initialQuestionIndex : 0;

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

const nextAttempt = () => { if (selectedAttemptIndex.value < currentQuestion.value.attempts.length - 1) { selectedAttemptIndex.value++; selectedCorrectionIndex.value = null; } };
const prevAttempt = () => { if (selectedAttemptIndex.value > 0) { selectedAttemptIndex.value--; selectedCorrectionIndex.value = null; } };
const setAttempt = (index) => { selectedAttemptIndex.value = index; selectedCorrectionIndex.value = null; };
const setQuestion = (index) => {
  selectedQuestionIndex.value = index;
  const q = allQuestions.value[index];
  selectedAttemptIndex.value = q.attempts ? q.attempts.length - 1 : 0;
  selectedCorrectionIndex.value = null;
  currentView.value = 'sentence';
};
const selectCorrection = (index, source = 'unknown') => {
  if (!hasUserAnswer.value) return;

  const isDeselecting = selectedCorrectionIndex.value === index;
  selectedCorrectionIndex.value = isDeselecting ? null : index;

  if (!isDeselecting) {
    nextTick(() => {
      if (feedbackViewMode.value === 'interactive') {
        const card = document.querySelector('.correction-detail-card');
        if (card) {
          card.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
      } else if (feedbackViewMode.value === 'list') {
        if (source === 'script') {
          const listItem = document.querySelector(`.corrections-list .correction-item.selected`);
          if (listItem) {
            listItem.scrollIntoView({ behavior: 'smooth', block: 'center' });
          }
        } else if (source === 'list') {
          const highlightedElement = document.querySelector(`.enhanced-script .clickable-correction.selected`);
          if (highlightedElement) {
            highlightedElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
          }
        }
      }
    });
  } else {
    // 하이라이트 해제 시 - 스크립트의 해당 위치가 보이도록 스크롤
    nextTick(() => {
      const targetElement = document.querySelector(`.enhanced-script .clickable-correction[data-index="${index}"]`);
      if (targetElement) {
        targetElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
      } else {
        const scriptBox = document.querySelector('.enhanced-script');
        if (scriptBox) {
          scriptBox.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
      }
    });
  }
};

const setFeedbackViewMode = (mode) => {
  feedbackViewMode.value = mode;
  selectedCorrectionIndex.value = null;
};

const showCopyToast = ref(false);
const copiedStatus = ref({});
const handleCopy = async (text, id) => {
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
</script>

<template>
  <div class="practice-feedback-page">
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
          <h1 class="feedback-title">
            {{ (practiceTypeName === 'INTRO' || practiceTypeName === 'INTRODUCTION') ? '자기소개 연습' : (practiceTopicTitle ? practiceTopicTitle + ' 연습' : '연습 결과') }}
          </h1>
        </div>
      </div>

      <div class="question-selector">
        <button v-for="(q, idx) in allQuestions" :key="q.questionId" class="q-tab-btn" :class="{ active: selectedQuestionIndex === idx }" @click="setQuestion(idx)">
          Question {{ idx + 1 }}
        </button>
      </div>

      <div class="feedback-main-content">
        <div class="attempt-carousel-header">
          <div class="attempt-info">
            <span class="attempt-label">답변 시도</span>
            <span class="attempt-count">{{ selectedAttemptIndex + 1 }} / {{ currentQuestion.attempts.length }}</span>
          </div>
          <div class="carousel-controls">
            <div class="carousel-indicators">
              <span v-for="(_, idx) in currentQuestion.attempts" :key="idx" class="indicator-dot" :class="{ active: selectedAttemptIndex === idx }" @click="setAttempt(idx)"></span>
            </div>
          </div>
        </div>

        <div class="question-display-card selectable-text-area">
          <span class="q-badge">Q {{ selectedQuestionIndex + 1 }}</span>
          <p class="question-text">{{ currentQuestion.questionText }}</p>
        </div>

        <div class="feedback-type-toggle">
          <button @click="currentView = 'sentence'" :class="{ active: currentView === 'sentence' }">
            <span class="material-icons">segment</span>
            문장 개별 피드백
          </button>
          <button @click="currentView = 'overall'" :class="{ active: currentView === 'overall' }">
            <span class="material-icons">school</span>
            종합 평가
          </button>
        </div>

        <div v-if="currentView === 'sentence'" class="feedback-section-wrapper" key="sentence-feedback">
          <div class="attempt-content-wrapper">
            <button class="side-nav-btn prev" v-if="selectedAttemptIndex > 0" @click="prevAttempt">
              <span class="material-icons">chevron_left</span>
            </button>

            <div class="enhanced-section-wrapper">
              <div class="enhanced-section selectable-text-area">
                <div class="section-card original-card">
                  <div class="card-header-with-action">
                    <div class="card-title-group">
                      <h3 class="subsection-title">내 원본 답변</h3>
                      <span v-if="hasUserAnswer && feedbackViewMode === 'interactive'" class="guidance-text placeholder">&nbsp;</span>
                    </div>
                    <div class="header-actions">
                      <button @click="handleCopy(feedbackData.userEnglishScript, 'user')" class="copy-btn mini" :class="{ copied: copiedStatus['user'] }" title="복사하기">
                        <span class="material-icons-outlined">{{ copiedStatus['user'] ? 'check' : 'content_copy' }}</span>
                      </button>
                    </div>
                  </div>
                  <div class="script-box original-script">
                    <p v-if="hasUserAnswer" v-html="highlightedUserScript"></p>
                    <p v-else class="empty-script-text">녹음된 영어 답변이 없습니다.</p>
                  </div>
                </div>

                <div class="section-card">
                  <div class="card-header-with-action">
                    <div class="card-title-group">
                      <h3 class="subsection-title">오꿀쌤의 교정 스크립트</h3>
                      <span v-if="hasUserAnswer && feedbackViewMode === 'interactive'" class="guidance-text">빨간색 글씨로 강조된 부분을 누르면 상세 설명을 볼 수 있습니다.</span>
                      <span v-else class="guidance-text placeholder">&nbsp;</span>
                    </div>
                    <div class="header-actions">
                      <div v-if="hasUserAnswer" class="view-mode-toggle">
                        <button @click="setFeedbackViewMode('interactive')" :class="{ active: feedbackViewMode === 'interactive' }">간단히</button>
                        <button @click="setFeedbackViewMode('list')" :class="{ active: feedbackViewMode === 'list' }">전체</button>
                      </div>
                      <button @click="handleCopy(currentAttempt.feedback?.improvedAnswer, 'ai')" class="copy-btn mini" :class="{ copied: copiedStatus['ai'] }" title="복사하기">
                        <span class="material-icons-outlined">{{ copiedStatus['ai'] ? 'check' : 'content_copy' }}</span>
                      </button>
                    </div>
                  </div>
                  <div class="script-box enhanced-script" :class="{ 'no-click': !hasUserAnswer }" v-html="interactiveImprovedScript" @click="handleScriptClick"></div>
                </div>
              </div>

              <div v-if="hasUserAnswer">
                <div v-if="feedbackViewMode === 'interactive' && selectedCorrection" class="correction-detail-card">
                  <div class="correction-comparison">
                    <div class="before">
                      <span class="label">수정 전</span>
                      <span class="text original">{{ selectedCorrection.originalSegment }}</span>
                    </div>
                    <span class="material-icons arrow">arrow_forward</span>
                    <div class="after">
                      <span class="label">수정 후</span>
                      <span class="text corrected">{{ selectedCorrection.correctedSegment }}</span>
                    </div>
                  </div>
                  <div class="correction-comment">
                    <span class="material-icons">lightbulb</span>
                    {{ selectedCorrection.comment }}
                  </div>
                </div>

                <div v-if="feedbackViewMode === 'list'" class="corrections-section-list">
                  <div v-if="feedbackData.feedbackResult?.scriptCorrections?.length > 0" class="corrections-list">
                    <div v-for="(correction, index) in feedbackData.feedbackResult.scriptCorrections" :key="index" class="correction-item" :class="{ 'selected': selectedCorrectionIndex === index }" @click="selectCorrection(index, 'list')">
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
                </div>
              </div>

              <div v-else class="no-answer-guide">
                <span class="material-icons info-icon">info</span>
                <p>답변 내용이 없어 문장별 교정 분석을 제공할 수 없습니다.<br>위의 <strong>오꿀쌤 교정 스크립트</strong>(모범 답안)를 참고하여 연습해보세요!</p>
              </div>

            </div>

            <button class="side-nav-btn next" v-if="selectedAttemptIndex < currentQuestion.attempts.length - 1" @click="nextAttempt">
              <span class="material-icons">chevron_right</span>
            </button>
          </div>
        </div>

        <div v-if="currentView === 'overall'" class="feedback-section-wrapper" key="overall-feedback">
          <div class="okkul-feedback-container full-width-layout">
            <div class="okkul-character-header">
              <img :src="okkulSvg" alt="오꿀쌤" class="okkul-img-small" />
              <span class="okkul-name">오꿀쌤의 종합 평가</span>
            </div>

            <div class="overall-cards-container">
              <div v-if="feedbackData.feedbackResult.fluency" class="section-card overall-card selectable-text-area">
                <h3 class="subsection-title">유창성</h3>
                <p class="bubble-text">{{ feedbackData.feedbackResult.fluency }}</p>
              </div>
              <div v-if="feedbackData.feedbackResult.logic" class="section-card overall-card selectable-text-area">
                <h3 class="subsection-title">논리성</h3>
                <p class="bubble-text">{{ feedbackData.feedbackResult.logic }}</p>
              </div>
              <div v-if="feedbackData.feedbackResult.relevance" class="section-card overall-card selectable-text-area">
                <h3 class="subsection-title">적합성</h3>
                <p class="bubble-text">{{ feedbackData.feedbackResult.relevance }}</p>
              </div>

              <div v-if="!feedbackData.feedbackResult.fluency && !feedbackData.feedbackResult.logic && !feedbackData.feedbackResult.relevance" class="speech-bubble-full selectable-text-area">
                <p class="bubble-text">종합 평가가 아직 준비되지 않았습니다.</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="action-buttons">
        <button @click="router.push('/practice')" class="btn btn-primary">
          <span class="material-icons">refresh</span>
          다른 유형 연습하기
        </button>
        <button @click="router.push('/')" class="btn btn-secondary">
          <span class="material-icons">home</span>
          홈으로
        </button>
      </div>
    </div>

    <div v-else class="error-screen">
      <span class="material-icons error-icon">error_outline</span>
      <p class="error-message">{{ errorMessage || '피드백 데이터를 불러올 수 없습니다.' }}</p>
      <button @click="router.push('/practice')" class="back-link btn-secondary">돌아가기</button>
    </div>

    <Transition name="toast">
      <div v-if="showCopyToast" class="copy-toast">
        <span class="material-icons">check_circle</span>
        클립보드에 복사되었습니다!
      </div>
    </Transition>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@400;500;700;900&display=swap');
@import url('https://fonts.googleapis.com/icon?family=Material+Icons');

.practice-feedback-page {
  min-height: 100vh;
  background: #FDFBF5;
  padding: 40px 100px;
}
.feedback-container {
  max-width: 1200px;
  margin: 0 auto;
  animation: slideUpFade 0.6s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}
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
.question-display-card {
  background: #FFFFFF;
  padding: 32px;
  border-radius: 20px;
  border: 1px solid var(--border-primary);
  margin-bottom: 32px;
  display: flex;
  align-items: flex-start;
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
.attempt-content-wrapper {
  position: relative;
  display: flex;
  align-items: flex-start;
  width: 100%;
}
.side-nav-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 56px;
  height: 56px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 8px 24px rgba(0,0,0,0.12);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  z-index: 100;
  transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
.side-nav-btn:hover {
  background: white;
  transform: translateY(-50%) scale(1.15);
  box-shadow: 0 12px 28px rgba(0,0,0,0.18);
}
.side-nav-btn.prev { left: -80px; }
.side-nav-btn.next { right: -80px; }
.feedback-main-content {
  width: 100%;
  display: flex;
  flex-direction: column;
}
.enhanced-section-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 24px;
}
.enhanced-section {
  width: 100%;
  display: flex;
  gap: 24px;
  align-items: stretch;
}
.okkul-feedback-container.full-width-layout {
  width: 100%;
  margin-bottom: 24px;
}
.okkul-character-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
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
  background: #FFF9EB;
  border: 2px solid #F5EAD8;
  border-radius: 20px;
  padding: 32px;
  width: 100%;
  box-shadow: var(--shadow-sm);
  position: relative;
  min-height: 220px;
  display: flex;
  flex-direction: column;
}
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
  flex: 1;
}
.section-card {
  background: #FFF;
  border-radius: 20px;
  padding: 32px;
  box-shadow: var(--shadow-sm);
  border: 1px solid #e2e8f0;
  margin-bottom: 0;
  min-height: 220px;
  display: flex;
  flex-direction: column;
  flex: 1;
}
.original-card {
  box-sizing: border-box;
}
.overall-section {
  width: 100%;
}
.subsection-title {
  font-size: 1.15rem;
  font-weight: 800;
  color: var(--text-primary);
}
.card-header-with-action {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}
.copy-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 6px 12px;
  background: white;
  border: 1px solid #F5EAD8;
  border-radius: 8px;
  font-size: 0.85rem;
  font-weight: 700;
  color: #8B7300;
  cursor: pointer;
  transition: all 0.2s;
}
.copy-btn:hover {
  background: #FFF9EB;
  border-color: #ffd700;
  transform: translateY(-1px);
}
.copy-btn.copied {
  border-color: #4ade80;
  color: #16a34a;
  background: #f0fdf4 !important;
}
.copy-btn.mini {
  padding: 4px 8px;
  background: transparent;
  border: none;
}
.copy-btn.mini:hover {
  background: rgba(255, 215, 0, 0.1);
}
.copy-btn.mini.copied {
  background: #f0fdf4 !important;
}
.selectable-text-area, .selectable-text-area * {
  user-select: text !important;
  -webkit-user-select: text !important;
}
.selectable-text-area h1,
.selectable-text-area h2,
.selectable-text-area h3,
.selectable-text-area .subsection-title,
.selectable-text-area .q-badge,
.selectable-text-area .copy-btn,
.selectable-text-area .okkul-name {
  user-select: none !important;
  -webkit-user-select: none !important;
}
.copy-toast {
  position: fixed;
  bottom: 40px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(33, 37, 41, 0.9);
  color: white;
  padding: 12px 24px;
  border-radius: 999px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 600;
  z-index: 2000;
  box-shadow: 0 4px 15px rgba(0,0,0,0.2);
}
.copy-toast .material-icons { color: #4ade80; }
.toast-enter-active, .toast-leave-active { transition: all 0.3s ease; }
.toast-enter-from, .toast-leave-to { opacity: 0; transform: translate(-50%, 20px); }
.script-box {
  padding: 24px;
  border-radius: 12px;
  line-height: 1.8;
  font-size: 1.15rem;
  background: #f8fafc;
  flex: 1;
  border: 1px solid #e2e8f0;
}
.script-box.no-click {
  pointer-events: none;
}
.enhanced-script {
  color: var(--text-primary);
}
.original-script {
  color: var(--text-secondary);
}
.empty-script-text {
  color: #94a3b8;
  font-style: italic;
  font-size: 1rem;
}
.action-buttons {
  display: flex;
  gap: 16px;
  margin-top: 64px;
  justify-content: center;
}
.error-screen {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 100px;
  gap: 20px;
}
.spinner { width: 40px; height: 40px; border: 4px solid #f3f3f3; border-top: 4px solid var(--primary-color); border-radius: 50%; animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.overall-cards-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
  width: 100%;
}
.overall-card {
  min-height: auto;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-radius: 24px;
  padding: 32px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.05);
}
.overall-card .subsection-title {
  margin-bottom: 12px;
  color: #8B7300;
  font-weight: 800;
}

.feedback-type-toggle {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  border-bottom: 1px solid #e2e8f0;
}
.feedback-type-toggle button {
  padding: 14px 20px;
  border: none;
  background: transparent;
  font-weight: 700;
  font-size: 1rem;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
  border-bottom: 3px solid transparent;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: -1px;
}
.feedback-type-toggle button:hover {
  color: #334155;
}
.feedback-type-toggle button.active {
  color: var(--primary-color);
  border-bottom-color: var(--primary-color);
}
.feedback-section-wrapper {
  animation: slideUpFade 0.4s ease forwards;
}

.card-title-group {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}
.guidance-text {
  font-size: 0.75rem;
  color: #64748b;
}
.guidance-text.placeholder {
  visibility: hidden;
}
.view-mode-toggle {
  display: flex;
  background-color: #e2e8f0;
  border-radius: 8px;
  padding: 4px;
}
.view-mode-toggle button {
  padding: 6px 12px;
  border: none;
  background: transparent;
  border-radius: 6px;
  font-weight: 700;
  cursor: pointer;
  color: #475569;
  transition: all 0.2s;
}
.view-mode-toggle button.active {
  background: white;
  color: #0f172a;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}
.enhanced-script :deep(.clickable-correction) {
  background: transparent;
  color: #E03131;
  padding: 2px 4px;
  border-radius: 4px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
  border-bottom: 1px dashed rgba(224, 49, 49, 0.3);
}
.enhanced-script :deep(.clickable-correction:hover) {
  background: rgba(224, 49, 49, 0.05);
}
.enhanced-script :deep(.clickable-correction.selected) {
  background: #FFF9C4;
  color: #D32F2F;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}
.original-script :deep(.highlighted-original) {
  background: #FFD54F;
  color: #BF360C;
  padding: 2px 4px;
  border-radius: 4px;
  font-weight: 700;
}
.correction-detail-card {
  background: rgba(255, 255, 255, 0.8) !important;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 2px solid var(--primary-color) !important;
  border-radius: 20px;
  padding: 24px;
  margin-top: 16px;
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1) !important;
  animation: slideUpFade 0.4s ease forwards;
}
.corrections-section-list {
  margin-top: 24px;
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
.correction-item:hover, .correction-item.selected {
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
  flex-shrink: 0;
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
.original { background: transparent; color: var(--text-secondary); text-decoration: none; }
.corrected { background: transparent; color: #16a34a; }
.correction-comment {
  display: flex;
  gap: 8px;
  color: var(--text-secondary);
  font-size: 0.95rem;
}
.no-answer-guide {
  background: #f1f5f9;
  border-radius: 16px;
  padding: 32px;
  text-align: center;
  color: #64748b;
  margin-top: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}
.info-icon {
  font-size: 2rem;
  color: var(--primary-color);
}

@media (max-width: 1024px) {
  .practice-feedback-page {
    padding: 32px 20px;
  }
  .enhanced-section {
    flex-direction: column;
  }
  .side-nav-btn {
    display: none;
  }
  .feedback-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  .action-buttons {
    flex-direction: column;
    width: 100%;
  }
  .action-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>