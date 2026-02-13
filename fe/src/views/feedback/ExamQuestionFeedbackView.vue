<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { historyApi } from '@/api';
import okkulSvg from '@/assets/images/okkul.svg';

const router = useRouter();
const route = useRoute();

const examId = ref(route.query.examId);
const questionOrder = ref(route.query.questionOrder);
const num = ref(route.query.num); // 리포트 회차 정보

const detailData = ref(null);
const isLoading = ref(true);
const activeTab = ref('individual'); // 기본값을 'individual'로 변경하여 스크린샷과 맞춤
const feedbackViewMode = ref('interactive');
const selectedCorrectionIndex = ref(null);

const hasUserAnswer = computed(() => {
  return detailData.value?.sttScript && detailData.value.sttScript.trim().length > 0;
});

const interactiveImprovedScript = computed(() => {
  const improvedScript = detailData.value?.improvedAnswer || '';

  if (!hasUserAnswer.value || !detailData.value?.sentenceFeedbacks?.length) {
    return improvedScript;
  }

  const corrections = detailData.value.sentenceFeedbacks;

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
  const userScript = detailData.value?.sttScript || '';
  const selectedOriginalSegment = selectedCorrection.value?.targetSegment;

  if (!userScript || !selectedOriginalSegment) return userScript || '';

  const className = 'highlighted-original';
  const escapedSegment = selectedOriginalSegment.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  const regex = new RegExp(escapedSegment, 'g');
  return userScript.replace(regex, `<span class="${className}">${selectedOriginalSegment}</span>`);
});

const selectedCorrection = computed(() => {
  if (selectedCorrectionIndex.value === null) return null;
  return detailData.value?.sentenceFeedbacks[selectedCorrectionIndex.value] || null;
});

// 데이터 로드
const loadDetail = async () => {
  try {
    isLoading.value = true;
    if (!examId.value || !questionOrder.value) {
      alert('필수 정보가 누락되었습니다.');
      router.back();
      return;
    }

    const response = await historyApi.getExamAnswerDetail(examId.value, questionOrder.value);
    detailData.value = response.data;
  } catch (err) {
    console.error('문항 상세 조회 실패:', err);
    // 에러 시에도 빈 상태로 유지하여 템플릿의 empty-screen이나 no-answer-guide가 작동하도록 함
    detailData.value = null;
  } finally {
    isLoading.value = false;
  }
};

// 브라우저 뒤로가기 금지
const handlePopState = () => {
  window.history.pushState(null, '', window.location.href);
};

onMounted(() => {
  window.scrollTo(0, 0);
  window.history.pushState(null, '', window.location.href);
  window.addEventListener('popstate', handlePopState);
  loadDetail();
});

onBeforeUnmount(() => {
  window.removeEventListener('popstate', handlePopState);
});

// 하이라이트 처리를 위한 스크립트 파싱 (교정된 답변 기준)
const highlightedWords = computed(() => {
  if (!detailData.value?.improvedAnswer) return [];
  
  return detailData.value.improvedAnswer.split(/\s+/).map(word => ({
    text: word,
    highlighted: false
  }));
});

// 복사 기능 관련
const showCopyToast = ref(false);
const copiedStatus = ref({
  user: false,
  ai: false
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

// 문장별 피드백 매핑
const sentenceFeedbacks = computed(() => {
  return detailData.value?.sentenceFeedbacks || [];
});

const selectSentence = (index, source = 'unknown') => {
  if (!hasUserAnswer.value) return;

  const isDeselecting = selectedCorrectionIndex.value === index;
  selectedCorrectionIndex.value = isDeselecting ? null : index;

  nextTick(() => {
    if (!isDeselecting) {
      // 하이라이트 선택 시 - 상세 카드로 스크롤
      if (feedbackViewMode.value === 'interactive') {
        const card = document.querySelector('.correction-detail-card');
        if (card) {
          card.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
      } else if (feedbackViewMode.value === 'list') {
        if (source === 'script') {
          const listItem = document.querySelector(`.feedback-list-section .detail-item-card.selected`);
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
    } else {
      // 하이라이트 해제 시 - 스크립트의 해당 위치가 보이도록 스크롤
      const targetElement = document.querySelector(`.enhanced-script .clickable-correction[data-index="${index}"]`);
      if (targetElement) {
        targetElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
      } else {
        const scriptCard = document.querySelector('.ai-card');
        if (scriptCard) {
          scriptCard.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
      }
    }
  });
};

const handleScriptClick = (event) => {
  if (!hasUserAnswer.value) return;

  const target = event.target;
  if (target.classList.contains('clickable-correction')) {
    const index = parseInt(target.dataset.index, 10);
    selectSentence(index, 'script');
  }
};

const setFeedbackViewMode = (mode) => {
  feedbackViewMode.value = mode;
  selectedCorrectionIndex.value = null;
};

const goBack = () => {
  router.push({ path: '/exam/feedback', query: { examId: examId.value, num: num.value } });
};
</script>

<template>
  <div class="feedback-detail-page">
    <div v-if="isLoading" class="loading-container">
      <div class="spinner"></div>
      <p>상세 피드백을 불러오는 중...</p>
    </div>

    <div v-else-if="detailData" class="content-container fade-in">
      <!-- 헤더 -->
      <header class="header">
        <button @click="goBack" class="back-btn">
          <span class="material-icons">arrow_back</span>
          목록으로
        </button>
        <h1 class="title">Question {{ questionOrder }} 상세 피드백</h1>
      </header>

      <!-- 탭 메뉴 (스크린샷 스타일) -->
      <div class="tab-menu-underlined">
        <button 
          class="tab-btn-underlined" 
          :class="{ active: activeTab === 'individual' }"
          @click="activeTab = 'individual'"
        >
          <span class="material-icons-outlined">menu</span>
          문장 개별 피드백
        </button>
        <button 
          class="tab-btn-underlined" 
          :class="{ active: activeTab === 'comprehensive' }"
          @click="activeTab = 'comprehensive'"
        >
          <span class="material-icons-outlined">school</span>
          종합 평가
        </button>
      </div>

      <!-- 탭 콘텐츠 -->
      <main class="tab-content">
        <!-- 1. 종합 평가 탭 -->
        <div v-if="activeTab === 'comprehensive'" class="comprehensive-tab">
          <div class="okkul-feedback-container full-width-layout">
            <div class="okkul-character-header">
              <img :src="okkulSvg" alt="오꿀쌤" class="okkul-img-small" />
              <span class="okkul-name">오꿀쌤의 종합 평가</span>
            </div>

            <div class="overall-cards-container">
              <template v-if="detailData.categoryFeedback">
                <div v-if="detailData.categoryFeedback.fluencyFeedback" class="section-card overall-card selectable-text-area">
                  <h3 class="subsection-title">유창성</h3>
                  <p class="bubble-text">{{ detailData.categoryFeedback.fluencyFeedback }}</p>
                </div>
                <div v-if="detailData.categoryFeedback.logicFeedback" class="section-card overall-card selectable-text-area">
                  <h3 class="subsection-title">논리성</h3>
                  <p class="bubble-text">{{ detailData.categoryFeedback.logicFeedback }}</p>
                </div>
                <div v-if="detailData.categoryFeedback.relevanceFeedback" class="section-card overall-card selectable-text-area">
                  <h3 class="subsection-title">적합성</h3>
                  <p class="bubble-text">{{ detailData.categoryFeedback.relevanceFeedback }}</p>
                </div>
              </template>
              <div v-else class="speech-bubble-full selectable-text-area">
                <p class="bubble-text">상세 피드백 데이터가 없습니다.</p>
              </div>
            </div>
          </div>
        </div>

        <!-- 2. 문장 개별 피드백 탭 -->
        <div v-else class="individual-tab selectable-text-area" key="individual-feedback">
          <div class="enhanced-section-wrapper">
            <div class="enhanced-section">
              <!-- 내 원본 답변 카드 -->
              <div class="card-clean original-card">
                <div class="card-header-with-action">
                  <div class="card-title-group">
                    <h3 class="subsection-title">내 원본 답변</h3>
                  </div>
                  <div class="header-actions">
                    <button @click="handleCopy(detailData.sttScript, 'user')" class="copy-btn mini" :class="{ copied: copiedStatus['user'] }" title="복사하기">
                      <span class="material-icons-outlined">{{ copiedStatus['user'] ? 'check' : 'content_copy' }}</span>
                    </button>
                  </div>
                </div>
                <div class="script-box-clean">
                  <p v-if="hasUserAnswer" v-html="highlightedUserScript"></p>
                  <p v-else class="empty-script-text">녹음된 영어 답변이 없습니다.</p>
                </div>
              </div>

              <!-- 오꿀쌤 교정 스크립트 카드 -->
              <div class="card-clean ai-card">
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
                    <button @click="handleCopy(detailData.improvedAnswer, 'ai')" class="copy-btn mini" :class="{ copied: copiedStatus['ai'] }" title="복사하기">
                      <span class="material-icons-outlined">{{ copiedStatus['ai'] ? 'check' : 'content_copy' }}</span>
                    </button>
                  </div>
                </div>
                <div class="script-box-clean enhanced-script" :class="{ 'no-click': !hasUserAnswer }" v-html="interactiveImprovedScript" @click="handleScriptClick"></div>
              </div>
            </div>

            <!-- 하단 상세 영역 -->
            <div v-if="hasUserAnswer">
              <!-- 인터랙티브 상세 카드 -->
              <div v-if="feedbackViewMode === 'interactive' && selectedCorrection" class="correction-detail-card">
                <div class="correction-comparison">
                  <div class="before">
                    <span class="label">수정 전</span>
                    <span class="text original">{{ selectedCorrection.targetSegment }}</span>
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

              <!-- 전체 리스트 뷰 -->
              <div v-if="feedbackViewMode === 'list'" class="corrections-section-list">
                <div v-if="detailData.sentenceFeedbacks?.length > 0" class="corrections-list">
                  <div 
                    v-for="(fb, idx) in detailData.sentenceFeedbacks" 
                    :key="idx" 
                    class="correction-item" 
                    :class="{ 'selected': selectedCorrectionIndex === idx }" 
                    @click="selectSentence(idx, 'list')"
                  >
                    <div class="correction-number">{{ idx + 1 }}</div>
                    <div class="correction-content">
                      <div class="correction-comparison">
                        <div class="before">
                          <span class="label">수정 전</span>
                          <span class="text original">{{ fb.targetSegment }}</span>
                        </div>
                        <span class="material-icons arrow">arrow_forward</span>
                        <div class="after">
                          <span class="label">수정 후</span>
                          <span class="text corrected">{{ fb.correctedSegment }}</span>
                        </div>
                      </div>
                      <div class="correction-comment">
                        <span class="material-icons">lightbulb</span>
                        {{ fb.comment }}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div v-else class="no-answer-guide">
              <span class="material-icons info-icon">info</span>
              <p>답변 내용이 없어 문장별 교정 분석을 제공할 수 없습니다.</p>
            </div>
          </div>
        </div>
      </main>
    </div>

    <!-- 에러/데이터 없음 상태 -->
    <div v-else class="empty-screen-glass">
      <div class="glass-card error-card">
        <span class="material-icons icon-large">info</span>
        <h2>피드백 정보 없음</h2>
        <p>답변이 제출되지 않았거나 분석 데이터가 존재하지 않습니다.</p>
        <button @click="goBack" class="back-btn">
          <span class="material-icons">arrow_back</span>
          목록으로 돌아가기
        </button>
      </div>
    </div>

    <!-- 복사 알림 토스트 -->
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

.feedback-detail-page {
  min-height: 100vh;
  padding: 40px 100px;
  background: #FDFBF5; /* PracticeFeedbackView와 통일된 배경색 */
}

.content-container {
  max-width: 1200px;
  margin: 0 auto;
}

.header {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 40px;
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

.title {
  font-size: 2.5rem;
  font-weight: 900;
  color: var(--text-primary);
}

/* 탭 메뉴 (하단 라인) */
.tab-menu-underlined {
  display: flex;
  gap: 32px;
  margin-bottom: 32px;
  border-bottom: 1px solid #e2e8f0;
  padding-left: 16px;
}

.tab-btn-underlined {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 4px;
  border: none;
  background: transparent;
  font-weight: 700;
  font-size: 1rem;
  color: #64748b;
  cursor: pointer;
  position: relative;
  transition: all 0.2s;
  border-bottom: 3px solid transparent;
  margin-bottom: -1px;
}

.tab-btn-underlined:hover {
  color: #334155;
}

.tab-btn-underlined.active {
  color: var(--primary-color);
  border-bottom-color: var(--primary-color);
}

.tab-btn-underlined .material-icons-outlined {
  font-size: 1.25rem;
}

/* 카드 및 레이아웃 (PracticeFeedbackView 스타일) */
.card-clean {
  background: #FFFFFF;
  border-radius: 20px;
  padding: 32px;
  border: 1px solid var(--border-primary);
  box-shadow: var(--shadow-sm);
  margin-bottom: 0;
  min-height: 220px;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.card-header-with-action {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.card-title-group {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}

.subsection-title {
  font-size: 1.15rem;
  font-weight: 800;
  color: var(--text-primary);
}

.guidance-text {
  font-size: 0.75rem;
  color: #64748b;
}

.guidance-text.placeholder {
  visibility: hidden;
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

.view-mode-toggle {
  display: flex;
  background-color: #e2e8f0;
  border-radius: 12px;
  padding: 4px;
}

.view-mode-toggle button {
  padding: 6px 16px;
  border: none;
  background: transparent;
  border-radius: 8px;
  font-weight: 700;
  font-size: 0.9rem;
  cursor: pointer;
  color: #64748b;
  transition: all 0.2s;
}

.view-mode-toggle button.active {
  background: white;
  color: #0f172a;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

/* 스크립트 박스 */
.script-box-clean {
  padding: 24px;
  border-radius: 12px;
  line-height: 1.8;
  font-size: 1.15rem;
  background: #f8fafc;
  flex: 1;
  border: 1px solid #e2e8f0;
  color: var(--text-primary);
  min-height: 220px;
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

.enhanced-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  align-items: stretch;
}

.enhanced-section-wrapper {
  display: flex;
  flex-direction: column;
  gap: 24px;
  animation: slideUpFade 0.4s ease forwards;
}

/* 상세 피드백 카드 */
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

.correction-comparison {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.correction-comparison .label {
  font-size: 0.85rem;
  font-weight: 700;
  color: var(--text-tertiary);
  margin-right: 8px;
}

.correction-comparison .text {
  padding: 8px 16px;
  border-radius: 8px;
  font-weight: 600;
}

.correction-comparison .original { background: transparent; color: var(--text-secondary); text-decoration: none; }
.correction-comparison .corrected { background: transparent; color: #16a34a; }

.correction-comparison .arrow {
  color: #94a3b8;
  font-size: 20px;
}

.correction-comment {
  display: flex;
  gap: 8px;
  color: var(--text-secondary);
  font-size: 0.95rem;
}

/* 전체 리스트 뷰 스타일 */
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

/* 종합 평가 (PracticeFeedbackView 레이아웃 준수) */
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

.bubble-text {
  font-size: 1.15rem;
  line-height: 1.8;
  color: #333;
  margin: 0;
  white-space: pre-wrap;
  font-weight: 500;
  flex: 1;
}

/* 가이드 및 유틸리티 */
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

.empty-script-text {
  color: #94a3b8;
  font-style: italic;
  font-size: 1rem;
}

.selectable-text-area, .selectable-text-area * {
  user-select: text !important;
  -webkit-user-select: text !important;
}

.selectable-text-area h1,
.selectable-text-area h2,
.selectable-text-area h3,
.selectable-text-area .subsection-title,
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

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 80vh;
  gap: 20px;
}

.spinner {
  width: 48px;
  height: 48px;
  border: 4px solid var(--bg-tertiary);
  border-top-color: var(--primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@keyframes slideUpFade {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 1024px) {
  .feedback-detail-page {
    padding: 32px 24px;
  }
}

@media (max-width: 768px) {
  .title {
    font-size: 2rem;
  }
  .header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  .enhanced-section {
    grid-template-columns: 1fr;
  }
}
</style>