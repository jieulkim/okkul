<script setup>
import { ref, onMounted, inject } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const isDarkMode = inject('isDarkMode', ref(false));
const isLoading = ref(true);

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
        <h1 class="page-title">오꿀쌤 피드백 내역</h1>
        <p class="page-subtitle">지금까지의 성장을 확인해보세요!</p>
      </header>

      <div v-if="isLoading" class="loading-state">
        <div class="spinner"></div>
        <p>기록을 불러오는 중...</p>
      </div>

      <div v-else class="feedback-grid">
        <div 
          v-for="item in feedbackHistory" 
          :key="`${item.type}-${item.id}`" 
          class="feedback-card"
          :class="item.type.toLowerCase()"
          @click="goToDetail(item)"
        >
          <div class="card-tag">{{ item.type === 'EXAM' ? '모의고사' : '유형별 연습' }}</div>
          <div class="card-content">
            <h3 class="card-title">{{ item.title }}</h3>
            <p class="card-date">{{ item.date }}</p>
            
            <div v-if="item.type === 'EXAM'" class="exam-info">
              <div class="grade-badge">{{ item.grade }}</div>
              <div class="score-text">점수: {{ item.score }}</div>
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
  margin-bottom: 48px;
  text-align: center;
}

.page-title {
  font-size: 3rem;
  font-weight: 900;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.page-subtitle {
  font-size: 1.25rem;
  color: #64748b;
}

.feedback-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.feedback-card {
  background: var(--bg-secondary);
  border: 4px solid #000;
  border-radius: 24px;
  padding: 24px;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 6px 6px 0 #000;
  position: relative;
  display: flex;
  flex-direction: column;
}

/* 다크모드 테두리 및 그림자 */
.dark-mode .feedback-card {
  border-color: #FFF;
  box-shadow: 6px 6px 0 #FFF;
}

.feedback-card:hover {
  transform: translate(-4px, -4px);
  box-shadow: 8px 8px 0 #000;
}

.dark-mode .feedback-card:hover {
  box-shadow: 8px 8px 0 #FFF;
}

/* 상단 태그 스타일 */
.card-tag {
  display: inline-block;
  padding: 4px 12px;
  background: #000;
  color: #FFF;
  font-weight: 900;
  font-size: 0.75rem;
  border-radius: 99px;
  margin-bottom: 16px;
  width: fit-content;
}

.dark-mode .card-tag {
  background: #FFF;
  color: #000;
}

/* --- EXAM 카드 스타일 --- */
.exam.feedback-card {
  background: var(--primary-color);
}

.dark-mode .exam.feedback-card {
  background: #868e96;
}

/* --- PRACTICE 카드 스타일 --- */
.practice.feedback-card {
  background: #FFFFFF;
}

.dark-mode .practice.feedback-card {
  background: var(--bg-secondary);
}

.card-title {
  font-size: 1.5rem;
  font-weight: 900;
  margin-bottom: 8px;
  color: #000;
}

.dark-mode .card-title {
  color: var(--text-primary);
}

.dark-mode .exam.feedback-card .card-title {
  color: #FFFFFF;
}

.card-date {
  font-size: 0.9rem;
  color: rgba(0, 0, 0, 0.6);
  margin-bottom: 20px;
}

.dark-mode .card-date {
  color: var(--text-secondary);
}

/* 등급 및 점수 정보 */
.exam-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.grade-badge {
  padding: 8px 16px;
  background: #000;
  color: var(--primary-color);
  font-weight: 900;
  font-size: 1.25rem;
  border-radius: 12px;
}

.dark-mode .exam.feedback-card .grade-badge {
  background: var(--primary-color);
  color: #000000;
}

.score-text {
  font-weight: 800;
  color: #000;
}

.dark-mode .score-text {
  color: var(--text-primary);
}

/* 주제 태그 */
.topic-tag {
  padding: 6px 12px;
  background: #f1f5f9;
  color: #475569;
  border-radius: 8px;
  font-weight: 700;
}

.dark-mode .topic-tag {
  background: var(--bg-tertiary);
  color: var(--text-secondary);
}

/* 카드 하단 */
.card-footer {
  margin-top: auto;
  padding-top: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 800;
  font-size: 0.9rem;
  color: #000;
}

.dark-mode .card-footer {
  color: var(--text-primary);
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
  border: 4px solid #e2e8f0;
  border-top-color: var(--primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
