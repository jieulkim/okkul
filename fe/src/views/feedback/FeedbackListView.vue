<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { historyApi } from '@/api';

// 라우터
const router = useRouter();
const route = useRoute();

// 상태 관리
const isLoading = ref(false);
const currentCategory = ref(null); // 'EXAM', 'PRACTICE', null(전체/초기화면)
const feedbackHistory = ref([]);
const isFiltering = ref(false);

const selectCategory = async (category) => {
  currentCategory.value = category;
  await loadFilteredHistory();
};

const loadFilteredHistory = async () => {
    if (!currentCategory.value) return;
    
    isFiltering.value = true;
    try {
        if (currentCategory.value === 'EXAM') {
            const { data } = await historyApi.getExamHistories({ size: 20, sort: ['createdAt,desc'] });
            feedbackHistory.value = data.content?.map((item, index) => {
                const difficulty = item.initialDifficulty || item.adjustedDifficulty || 1;
                const date = new Date(item.createdAt).toLocaleDateString('ko-KR', { 
                    year: 'numeric', 
                    month: '2-digit', 
                    day: '2-digit' 
                }).replace(/\. /g, '.').replace(/\.$/, '');
                
                return {
                    id: item.examId,
                    type: 'EXAM',
                    title: `난이도 ${difficulty} 모의고사`, 
                    date: formatDate(item.createdAt),
                    grade: item.grade || '등급 없음',
                    score: null // API 미제공
                };
            }) || [];
        } else {
            const { data } = await historyApi.getPracticeHistories({ size: 20, sort: ['startedAt,desc'] });
            feedbackHistory.value = data.content?.map(item => ({
                id: item.practiceId,
                type: 'PRACTICE',
                title: (item.typeName === 'INTRO' || item.typeName === 'INTRODUCTION') 
                    ? '자기소개 연습' 
                    : `${item.typeName || ''} ${item.topic || '토픽 정보 없음'}`.trim(),
                date: formatDate(item.startedAt),
                score: null,
                grade: null
            })) || [];
        }
    } catch (error) {
        console.error('Failed to fetch history:', error);
        feedbackHistory.value = [];
    } finally {
        isFiltering.value = false;
    }
};

const goBackToCategories = () => {
  currentCategory.value = null;
  feedbackHistory.value = [];
};

const goToDetail = (item) => {
  if (item.type === 'EXAM') {
    router.push({ path: '/exam/feedback', query: { examId: item.id } });
  } else {
    router.push({ path: '/practice/feedback', query: { practiceId: item.id } });
  }
};

const formatDate = (dateString) => {
    if (!dateString) return '';
    return new Date(dateString).toLocaleDateString('ko-KR');
};

// 브라우저 뒤로가기 금지
const handlePopState = () => {
  window.history.pushState(null, '', window.location.href);
};

onMounted(() => {
  const category = route.query.category;
  if (category === 'EXAM' || category === 'PRACTICE') {
    selectCategory(category);
  }
  window.history.pushState(null, '', window.location.href);
  window.addEventListener('popstate', handlePopState);
});

onBeforeUnmount(() => {
  window.removeEventListener('popstate', handlePopState);
});

const filteredHistory = computed(() => feedbackHistory.value);
</script>

<template>
  <div class="feedback-list-page">
    <div class="container fade-in">
      <header class="page-header">
        <div v-if="currentCategory" class="header-nav">
          <button @click="goBackToCategories" class="back-link">
            <span class="material-icons">arrow_back</span>
            카테고리로 돌아가기
          </button>
        </div>
        <h1 class="page-title">
          {{ !currentCategory ? '오꿀쌤 피드백 내역' : (currentCategory === 'EXAM' ? '모의고사 피드백' : '유형별 연습 피드백') }}
        </h1>
        <p class="page-subtitle">지금까지의 <span class="highlight">성장을 확인</span>해보세요!</p>
      </header>

      <!-- 카테고리 선택 화면 -->
      <div v-if="!currentCategory" class="categories-grid visible-animation">
        <div class="category-card" @click="selectCategory('EXAM')">
          <div class="category-icon">
            <span class="material-icons">school</span>
          </div>
          <h2>실전 모의고사</h2>
          <p><span class="highlight">회차별 시험</span>에 대한 <span class="highlight">종합적인 피드백</span>을 확인하세요.</p>
          <div class="category-footer">
            <span>내역 보기</span>
            <span class="material-icons">chevron_right</span>
          </div>
        </div>
        <div class="category-card" @click="selectCategory('PRACTICE')">
          <div class="category-icon">
            <span class="material-icons">track_changes</span>
          </div>
          <h2>유형별 연습</h2>
          <p><span class="highlight">각 주제별 집중 연습</span>에 대한 <span class="highlight">피드백</span>을 확인하세요.</p>
          <div class="category-footer">
            <span>내역 보기</span>
            <span class="material-icons">chevron_right</span>
          </div>
        </div>
      </div>

      <!-- 필터링된 리스트 화면 (카테고리 선택 시) -->
      <div v-else>
        <!-- 로딩 중일 때 표시 -->
        <div v-if="isFiltering" class="loading-state">
          <div class="spinner"></div>
          <p>기록을 불러오는 중...</p>
        </div>

        <!-- 로딩 완료 후 리스트 표시 -->
        <div v-else class="feedback-grid visible-animation">
          <div 
            v-for="item in filteredHistory" 
            :key="`${item.type}-${item.id}`" 
            class="feedback-card"
            :class="item.type.toLowerCase()"
            @click="goToDetail(item)"
          >
            <div class="card-content">
              <h3 class="card-title">{{ item.title }}</h3>
              <p class="card-date">{{ item.date }}</p>
              
              <div v-if="item.type === 'EXAM'" class="exam-info">
                <div class="grade-badge">{{ item.grade }}</div>
                <div v-if="item.score" class="score-text">점수: {{ item.score }}점</div>
              </div>
              <div v-else class="practice-info">
              </div>
            </div>
            <div class="card-footer">
              <span>자세히 보기</span>
              <span class="material-icons">chevron_right</span>
            </div>
          </div>

          <!-- 내역이 없을 경우 -->
          <div v-if="filteredHistory.length === 0" class="no-data">
            <span class="material-icons">inbox</span>
            <p>아직 피드백 내역이 없습니다.</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.feedback-list-page {
  min-height: calc(100vh - var(--header-height));
  background: var(--bg-color);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 64px 0;
}

.fade-in {
  animation: fadeIn 0.6s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.container {
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 0 24px;
}

.page-header {
  margin-bottom: 64px;
  text-align: center;
}

.header-nav {
  display: flex;
  justify-content: flex-start;
  margin-bottom: 24px;
}

.back-link {
  display: flex;
  align-items: center;
  gap: 8px;
  background: none;
  border: none;
  color: var(--text-secondary);
  font-weight: 600;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 12px;
  transition: all 0.2s;
}

.back-link:hover {
  background: var(--bg-tertiary);
  color: #FBC02D;
}

.page-title {
  font-size: 2.6rem;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 24px;
}

.page-subtitle {
  font-size: 1rem;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 50px;
}

.highlight {
  color: #F9A825;
  font-weight: 700;
  position: relative;
  display: inline-block;
  z-index: 1;
}

.highlight::after {
  content: "";
  position: absolute;
  bottom: 2px;
  left: 0;
  width: 100%;
  height: 8px;
  background: var(--honey-200);
  z-index: -1;
  opacity: 0.8;
}

.visible-animation {
  animation: fadeIn 0.6s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

/* 카테고리 선택 스타일 */
.categories-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(340px, 1fr));
  gap: 32px;
  max-width: 900px;
  margin: 0 auto;
}

.category-card {
  background: #FFFFFF;
  border: 1px solid #F1F5F9;
  border-radius: 32px;
  padding: 48px 32px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: var(--shadow-md);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.category-card:hover {
  transform: translateY(-8px);
  /* 글라스모피즘 효과 적용 */
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.1);
  border-color: #FBC02D;
}

.category-icon {
  margin-bottom: 24px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.category-icon .material-icons {
  font-size: 64px;
  color: var(--primary-color);
}

.category-card h2 {
  font-size: 1.75rem;
  font-weight: 800;
  margin-bottom: 16px;
  color: var(--text-primary);
}

.category-card p {
  font-size: 1rem;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 32px;
  word-break: keep-all;
}


.category-footer {
  margin-top: auto;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #FBC02D;
  font-weight: 700;
  font-size: 1rem;
}

/* ... */

.card-footer {
  margin-top: auto;
  padding-top: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 700;
  font-size: 0.9rem;
  color: #FBC02D;
}

/* 피드백 리스트 스타일 */
.feedback-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}

.feedback-card {
  background: #FFFFFF;
  border: 1px solid #F1F5F9;
  border-radius: 20px;
  padding: 24px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: var(--shadow-md);
  position: relative;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.feedback-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
}

.feedback-card.exam::before {
  background: #FBC02D;
}

.feedback-card.practice::before {
  background: #fcfc05;
}

.feedback-card:hover {
  transform: translateY(-8px);
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.1);
  border-color: #FBC02D;
}

.card-title {
  font-size: 1.25rem;
  font-weight: 800;
  margin-bottom: 8px;
  color: var(--text-primary);
}

.card-date {
  font-size: 0.9rem;
  color: var(--text-tertiary);
  margin-bottom: 20px;
}

.exam-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.grade-badge {
  padding: 6px 14px;
  background: var(--primary-light);
  color: #8B7300;
  font-weight: 800;
  font-size: 1rem;
  border-radius: 10px;
}

.score-text {
  font-weight: 600;
  color: var(--text-secondary);
  font-size: 0.95rem;
}

.topic-tag {
  display: inline-block;
  padding: 6px 12px;
  background: var(--bg-tertiary);
  color: var(--text-secondary);
  border-radius: 8px;
  font-weight: 600;
}

.card-footer {
  margin-top: auto;
  padding-top: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 700;
  font-size: 0.9rem;
  color: #FBC02D;
}

.no-data {
  grid-column: 1 / -1;
  text-align: center;
  padding: 80px 0;
  color: var(--text-tertiary);
}

.no-data .material-icons {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.no-data p {
  font-size: 1.1rem;
}

/* 로딩 스피너 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100px 0;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid var(--bg-tertiary);
  border-top-color: var(--primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .page-title {
    font-size: 2rem;
  }
}
</style>