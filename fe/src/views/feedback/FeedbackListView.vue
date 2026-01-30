<script setup>
import { ref, onMounted, inject, computed } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const isDarkMode = inject('isDarkMode', ref(false));
const isLoading = ref(true);
const currentCategory = ref(null); // 'EXAM', 'PRACTICE', or null

// 가상 데이터 (API 연결 전)
const feedbackHistory = ref([
  {
    id: 1,
    type: 'EXAM',
    title: '제 3회 모의고사',
    date: '2026-01-28',
    grade: 'IM2',
    score: 85
  },
  {
    id: 2,
    type: 'PRACTICE',
    title: '유형별 연습 - 영화보기',
    date: '2026-01-27',
    grade: null,
    score: null,
    topic: '영화보기'
  },
  {
    id: 3,
    type: 'EXAM',
    title: '제 2회 모의고사',
    date: '2026-01-25',
    grade: 'IL',
    score: 65
  },
  {
    id: 4,
    type: 'PRACTICE',
    title: '유형별 연습 - 공원 가기',
    date: '2026-01-24',
    grade: null,
    score: null,
    topic: '공원 가기'
  }
]);

onMounted(async () => {
  // 실제 API 연동 시 이곳에서 데이터 로드
  setTimeout(() => {
    isLoading.value = false;
  }, 500);
});

const filteredHistory = computed(() => {
  if (!currentCategory.value) return [];
  return feedbackHistory.value.filter(item => item.type === currentCategory.value);
});

const selectCategory = (category) => {
  currentCategory.value = category;
};

const goBackToCategories = () => {
  currentCategory.value = null;
};

const goToDetail = (item) => {
  if (item.type === 'EXAM') {
    router.push({ path: '/exam/feedback', query: { examId: item.id } });
  } else {
    // 연습 피드백의 경우 practiceId와 questionId가 필요함 (가정)
    router.push({ path: '/practice/feedback', query: { practiceId: item.id, questionId: 101 } });
  }
};
</script>

<template>
  <div class="feedback-list-page" :class="{ 'dark-mode': isDarkMode }">
    <div class="container">
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
        <p class="page-subtitle">지금까지의 성장을 확인해보세요!</p>
      </header>

      <div v-if="isLoading" class="loading-state">
        <div class="spinner"></div>
        <p>기록을 불러오는 중...</p>
      </div>

      <div v-else>
        <!-- 카테고리 선택 화면 -->
        <div v-if="!currentCategory" class="categories-grid">
          <div class="category-card" @click="selectCategory('EXAM')">
            <div class="category-icon">
              <span class="material-icons">school</span>
            </div>
            <h2>실전 모의고사</h2>
            <p>전체 시험에 대한 종합적인 피드백을 확인하세요.</p>
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
            <p>각 주제별 집중 연습에 대한 피드백을 확인하세요.</p>
            <div class="category-footer">
              <span>내역 보기</span>
              <span class="material-icons">chevron_right</span>
            </div>
          </div>
        </div>

        <!-- 필터링된 리스트 화면 -->
        <div v-else class="feedback-grid">
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
                <div class="score-text">점수: {{ item.score }}점</div>
              </div>
              <div v-else class="practice-info">
                <span class="topic-tag">#{{ item.topic }}</span>
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
  min-height: 100vh;
  background: var(--bg-primary);
  padding: 60px 20px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
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
  color: var(--primary-color);
}

.page-title {
  font-size: 2.5rem;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.page-subtitle {
  font-size: 1.1rem;
  color: var(--text-secondary);
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
  background: var(--bg-secondary);
  border: var(--border-primary);
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
  box-shadow: var(--shadow-lg);
  border-color: var(--primary-color);
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
}

.category-footer {
  margin-top: auto;
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--primary-color);
  font-weight: 700;
  font-size: 1rem;
}

/* 피드백 리스트 스타일 */
.feedback-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}

.feedback-card {
  background: var(--bg-secondary);
  border: var(--border-primary);
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
  background: var(--primary-color);
}

.feedback-card:hover {
  transform: translateY(-8px);
  box-shadow: var(--shadow-lg);
  border-color: var(--primary-color);
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
  color: var(--primary-color);
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
