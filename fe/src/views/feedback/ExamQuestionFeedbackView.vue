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
            <p class="guide-text">문장을 클릭하면 상세 피드백을 확인할 수 있습니다.</p>
            <div class="highlighted-script">
              <template v-if="sentenceFeedbacks.length > 0">
                <span 
                  v-for="(fb, idx) in sentenceFeedbacks" 
                  :key="idx"
                  class="sentence-span"
                  :class="{ 'is-highlighted': true, 'is-selected': selectedSentenceIndex === idx }"
                  @click="selectSentence(idx)"
                >
                  {{ fb.correctedSegment }}
                </span>
              </template>
              <p v-else class="no-data">교정된 문장 정보가 없습니다.</p>
            </div>
          </div>

          <div class="detail-feedback-section" v-if="selectedSentenceIndex !== null">
            <div class="card detail-item-card">
              <h4 class="detail-label">하이라이트 된 문장 (수정 전)</h4>
              <p class="original-text">{{ sentenceFeedbacks[selectedSentenceIndex].targetSegment }}</p>
            </div>
            <div class="card detail-item-card">
              <h4 class="detail-label">교정 사유 및 피드백</h4>
              <p class="feedback-reason">{{ sentenceFeedbacks[selectedSentenceIndex].comment || '상세 피드백이 없습니다.' }}</p>
            </div>
          </div>
          <div v-else class="empty-detail">
            <span class="material-icons">touch_app</span>
            <p>문장을 클릭하여 상세 피드백을 확인하세요.</p>
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

.detail-feedback-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 32px;
}

.detail-item-card {
  padding: 24px;
}

.detail-label {
  font-weight: 700;
  color: var(--text-secondary);
  margin-bottom: 12px;
  font-size: 0.95rem;
}

.original-text {
  font-size: 1.1rem;
  color: #ef4444;
  text-decoration: line-through;
}

.feedback-reason {
  font-size: 1.1rem;
  line-height: 1.6;
  color: var(--text-primary);
}

.empty-detail {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
  color: var(--text-tertiary);
  gap: 12px;
}

.empty-detail .material-icons {
  font-size: 48px;
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