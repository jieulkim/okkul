<script setup>
import { ref, computed, onMounted } from 'vue';
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
const activeTab = ref('comprehensive'); // 'comprehensive' | 'individual'
const selectedSentenceIndex = ref(null);

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
    alert('상세 정보를 불러오지 못했습니다.');
    router.back();
  } finally {
    isLoading.value = false;
  }
};

onMounted(() => {
  loadDetail();
});

// 하이라이트 처리를 위한 스크립트 파싱 (교정된 답변 기준)
const highlightedWords = computed(() => {
  if (!detailData.value?.improvedAnswer) return [];
  
  // 간단한 화이트스페이스 기반 파싱 (실제론 문장 단위 피드백과 매칭 필요)
  // 여기서는 단순히 모든 단어를 객체화하여 반환
  return detailData.value.improvedAnswer.split(/\s+/).map(word => ({
    text: word,
    highlighted: false // 전체 교정 스크립트이므로 개별 하이라이트 로직은 문장 단위로 처리
  }));
});

// 문장별 피드백 매핑
const sentenceFeedbacks = computed(() => {
  return detailData.value?.sentenceFeedbacks || [];
});

const selectSentence = (index) => {
  selectedSentenceIndex.value = index;
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

    <div v-else-if="detailData" class="content-container">
      <!-- 헤더 -->
      <header class="header">
        <button @click="goBack" class="back-btn">
          <span class="material-icons">arrow_back</span>
          목록으로
        </button>
        <h1 class="title">Question {{ questionOrder }} 상세 피드백</h1>
      </header>

      <!-- 탭 메뉴 -->
      <div class="tab-menu">
        <button 
          class="tab-btn" 
          :class="{ active: activeTab === 'comprehensive' }"
          @click="activeTab = 'comprehensive'"
        >
          종합 피드백
        </button>
        <button 
          class="tab-btn" 
          :class="{ active: activeTab === 'individual' }"
          @click="activeTab = 'individual'"
        >
          문제 개별 피드백
        </button>
      </div>

      <!-- 탭 콘텐츠 -->
      <main class="tab-content">
        <!-- 1. 종합 피드백 탭 -->
        <div v-if="activeTab === 'comprehensive'" class="comprehensive-tab">
          <div class="answer-comparison">
            <div class="card answer-card">
              <h3 class="card-title">내 답변 (STT)</h3>
              <div class="script-box user-answer">
                {{ detailData.sttScript || '답변 데이터가 없습니다.' }}
              </div>
            </div>
            <div class="card answer-card ai-card">
              <h3 class="card-title">오꿀쌤의 추천 답변</h3>
              <div class="script-box ai-answer">
                {{ detailData.improvedAnswer || '교정된 답변이 없습니다.' }}
              </div>
            </div>
          </div>

          <div class="card feedback-summary-card">
            <h3 class="card-title">종합 피드백 내용</h3>
            <div class="okkul-feedback">
              <img :src="okkulSvg" alt="Okkul" class="okkul-img" />
              <div class="feedback-texts">
                <template v-if="detailData.categoryFeedback">
                  <div v-if="detailData.categoryFeedback.relevanceFeedback" class="feedback-item">
                    <span class="label">주제 적합성</span>
                    <p>{{ detailData.categoryFeedback.relevanceFeedback }}</p>
                  </div>
                  <div v-if="detailData.categoryFeedback.logicFeedback" class="feedback-item">
                    <span class="label">논리성</span>
                    <p>{{ detailData.categoryFeedback.logicFeedback }}</p>
                  </div>
                  <div v-if="detailData.categoryFeedback.fluencyFeedback" class="feedback-item">
                    <span class="label">유창성</span>
                    <p>{{ detailData.categoryFeedback.fluencyFeedback }}</p>
                  </div>
                </template>
                <div v-else class="no-feedback">
                    <p>상세 피드백 데이터가 없습니다.</p>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 2. 문제 개별 피드백 탭 -->
        <div v-else class="individual-tab">
          <div class="card highlight-card">
            <h3 class="card-title">문장별 교정 내용</h3>
            <p class="guide-text">아래 교정 항목을 클릭하면 지문에서 해당 위치가 하이라이트됩니다.</p>
            <div class="highlighted-script">
              <template v-if="sentenceFeedbacks.length > 0">
                <span 
                  v-for="(fb, idx) in sentenceFeedbacks" 
                  :key="idx"
                  class="sentence-span"
                  :class="{ 'is-highlighted': selectedSentenceIndex === idx }"
                >
                  {{ fb.correctedSegment }}
                </span>
              </template>
              <p v-else class="no-data">교정된 문장 정보가 없습니다.</p>
            </div>
          </div>

          <div class="feedback-list-section">
            <div 
              v-for="(fb, idx) in sentenceFeedbacks"
              :key="idx"
              class="card detail-item-card clickable-item"
              :class="{ 'selected': selectedSentenceIndex === idx }"
              @click="selectSentence(idx)"
            >
              <div class="item-header">
                <span class="number-badge">{{ idx + 1 }}</span>
                <span class="item-title">교정 상세 #{{ idx + 1 }}</span>
              </div>
              
              <div class="comparison-box">
                <div class="comp-row">
                  <span class="comp-label original-label">수정 전</span>
                  <p class="original-text">{{ fb.targetSegment }}</p>
                </div>
                <div class="comp-arrow">
                  <span class="material-icons">arrow_downward</span>
                </div>
                <div class="comp-row">
                  <span class="comp-label corrected-label">수정 후</span>
                  <p class="corrected-text">{{ fb.correctedSegment }}</p>
                </div>
              </div>

              <div class="feedback-reason-box">
                <span class="material-icons info-icon">info</span>
                <p class="feedback-reason">{{ fb.comment || '상세 피드백이 없습니다.' }}</p>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>

    <!-- 에러/데이터 없음 상태 -->
    <div v-else class="empty-screen">
      <span class="material-icons">sentiment_dissatisfied</span>
      <p>해당 문항에 대한 피드백 데이터를 불러올 수 없습니다.</p>
      <p class="sub-text">AI 피드백 생성 중이거나 일시적인 오류일 수 있습니다.</p>
      <button @click="goBack" class="back-link-btn">
        목록으로 돌아가기
      </button>
    </div>
  </div>
</template>

<style scoped>
.feedback-detail-page {
  min-height: 100vh;
  background: var(--bg-primary);
  padding: 40px 24px;
}

.content-container {
  max-width: 1100px;
  margin: 0 auto;
}

/* 헤더 */
.header {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 40px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: var(--bg-secondary);
  border: 1px solid var(--border-primary);
  border-radius: 12px;
  font-weight: 700;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.2s;
}

.back-btn:hover {
  background: var(--bg-tertiary);
  color: var(--text-primary);
}

.title {
  font-size: 2rem;
  font-weight: 800;
  color: var(--text-primary);
}

/* 탭 메뉴 */
.tab-menu {
  display: flex;
  gap: 4px;
  margin-bottom: 24px;
  background: #e2e8f0;
  padding: 4px;
  border-radius: 12px;
  width: fit-content;
}

.tab-btn {
  padding: 12px 24px;
  border: none;
  background: transparent;
  border-radius: 8px;
  font-weight: 700;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.2s;
}

.tab-btn.active {
  background: #FFFFFF;
  color: var(--primary-color);
  box-shadow: var(--shadow-sm);
}

.tab-btn.active[data-tab="comprehensive"] {
  background: #00ff00; /* User design has green for this tab */
  color: #000;
}

/* 위 색상은 사용자 디자인 스크린샷 참고하여 조정 가능 */
.tab-btn:hover:not(.active) {
  background: rgba(255, 255, 255, 0.5);
}

/* 카드 공통 */
.card {
  background: #FFFFFF;
  border-radius: 20px;
  padding: 32px;
  border: var(--border-primary);
  box-shadow: var(--shadow-md);
  margin-bottom: 24px;
}

.card-title {
  font-size: 1.25rem;
  font-weight: 800;
  margin-bottom: 20px;
  color: var(--text-primary);
}

/* 종합 피드백 탭 */
.answer-comparison {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 24px;
}

@media (max-width: 768px) {
  .answer-comparison {
    grid-template-columns: 1fr;
  }
}

.script-box {
  min-height: 200px;
  line-height: 1.8;
  font-size: 1.1rem;
  color: var(--text-primary);
}

.ai-card {
  border-top: 4px solid var(--primary-color);
}

.okkul-feedback {
  display: flex;
  gap: 32px;
  align-items: flex-start;
}

.okkul-img {
  width: 120px;
  height: 120px;
  flex-shrink: 0;
}

.feedback-texts {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feedback-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.feedback-item .label {
  font-weight: 800;
  color: var(--primary-color);
  font-size: 0.9rem;
}

.feedback-item p {
  line-height: 1.6;
  color: var(--text-secondary);
}

/* 개별 피드백 탭 */
.guide-text {
  color: var(--text-tertiary);
  font-size: 0.9rem;
  margin-bottom: 20px;
}

.highlighted-script {
  line-height: 2.2;
  font-size: 1.2rem;
  background: var(--bg-tertiary);
  padding: 24px;
  border-radius: 12px;
}

.sentence-span {
  display: inline;
  padding: 2px 4px;
  margin: 0 2px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.is-highlighted {
  background: rgba(255, 215, 0, 0.2);
  border-bottom: 2px solid #ffd700;
}

.is-highlighted:hover {
  background: rgba(255, 215, 0, 0.4);
}

.is-selected {
  background: #ffd700 !important;
  color: #000;
}

/* Feedback List Section */
.feedback-list-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 24px;
}

.clickable-item {
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid transparent;
  background: white;
}

.clickable-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
  border-color: var(--primary-color);
}

.clickable-item.selected {
  border: 2px solid var(--primary-color);
  background: var(--bg-secondary);
}

.item-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f1f5f9;
}

.number-badge {
  background: var(--primary-color);
  color: #212529;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 0.9rem;
}

.item-title {
  font-weight: 700;
  color: var(--text-primary);
  font-size: 1rem;
}

.comparison-box {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
  padding: 16px;
  background: var(--bg-tertiary);
  border-radius: 12px;
}

.comp-row {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.comp-label {
  font-size: 0.75rem;
  font-weight: 700;
  color: var(--text-tertiary);
}

.original-label { color: #ef4444; }
.corrected-label { color: #16a34a; }

.original-text {
  font-size: 1rem;
  color: var(--text-primary);
  text-decoration: line-through;
  opacity: 0.7;
  margin: 0;
}

.corrected-text {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.comp-arrow {
  display: flex;
  justify-content: center;
  color: var(--text-tertiary);
}

.feedback-reason-box {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  padding: 12px;
  background: rgba(255, 215, 0, 0.1);
  border-radius: 8px;
}

.info-icon {
  color: var(--primary-color);
  font-size: 20px;
  margin-top: 2px;
}

.feedback-reason {
  font-size: 0.95rem;
  line-height: 1.5;
  color: var(--text-secondary);
  margin: 0;
}

/* Updated Sentence Span Styles */
.sentence-span {
  display: inline;
  padding: 2px 4px;
  margin: 0 2px;
  border-radius: 4px;
  transition: all 0.3s;
}

.is-highlighted {
  background: var(--primary-color); /* Gold background */
  color: #000;
  font-weight: 700;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  transform: scale(1.05); /* Slightly enlarge to pop */
  display: inline-block; /* Required for transform */
}

/* 로딩 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 60vh;
  gap: 20px;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid var(--primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 추가 스타일 */
.empty-screen {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 70vh;
  gap: 16px;
  color: var(--text-secondary);
}

.empty-screen .material-icons {
  font-size: 64px;
  color: var(--text-tertiary);
}

.sub-text {
  font-size: 0.9rem;
  color: var(--text-tertiary);
}

.back-link-btn {
  margin-top: 12px;
  padding: 12px 24px;
  background: var(--primary-color);
  border: none;
  border-radius: 12px;
  font-weight: 700;
  cursor: pointer;
}
</style>