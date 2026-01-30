<script setup>
import { ref, computed, inject, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
const router = useRouter()

import { useAuthStore } from '@/stores/auth'

// 사용자 프로필 데이터
const userProfile = inject('userProfile')

// 다크모드 상태
const isDarkMode = inject('isDarkMode', ref(false))

const authStore = useAuthStore()
import { useSurveyStore } from '@/stores/survey'
const surveyStore = useSurveyStore()
const isLoggedIn = computed(() => !!authStore.user)

// 다크모드 변경 감지
watch(isDarkMode, (newVal) => {
  if (newVal) {
    document.documentElement.classList.add('dark-mode')
  } else {
    document.documentElement.classList.remove('dark-mode')
  }
}, { immediate: true })

// 컴포넌트 마운트 시 다크모드 클래스 적용
onMounted(() => {
  if (isDarkMode.value) {
    document.documentElement.classList.add('dark-mode')
  }
})

// 마이페이지 이동
const goToMyPage = () => {
  router.push('/mypage')
}

// 최근 성적 데이터
const recentScores = ref([
  { day: '월', score: 65, height: 40 },
  { day: '화', score: 72, height: 55 },
  { day: '수', score: 68, height: 45 },
  { day: '목', score: 80, height: 70 },
  { day: '금', score: 88, height: 85 },
  { day: '오늘', score: 85, height: 82, active: true }
])

// 최근 학습 기록
const recentActivities = ref([
  {
    id: 1,
    type: 'EXAM',
    title: '제 12회 실전 모의고사',
    date: '2026.01.20',
    status: 'COMPLETED',
    score: 'IH',
    detail: '15문항 · 38분'
  },
  {
    id: 2,
    type: 'PRACTICE',
    title: '롤플 문제: 여행 중 겪은 경험',
    date: '2026.01.23',
    status: 'REVIEWING',
    detail: '유형별 연습'
  },
  {
    id: 3,
    type: 'DRAFT',
    title: '주제: 음악 감상 및 기기',
    date: '2026.01.22',
    status: 'DRAFT',
    detail: '어휘 집중 학습',
    wordCount: '+12 words'
  }
])

const getStatusIcon = (status) => {
  const icons = {
    COMPLETED: 'check_circle',
    REVIEWING: 'rate_review',
    DRAFT: 'edit_note'
  }
  return icons[status] || 'description'
}

const getStatusColor = (status) => {
  const colors = {
    COMPLETED: '#10b981',
    REVIEWING: '#f59e0b',
    DRAFT: '#6366f1'
  }
  return colors[status] || '#64748b'
}
</script>

<template>
  <div class="page-container">
    <main class="page-content">
      <!-- 로그인 상태일 때 (대시보드) -->
      <div v-if="isLoggedIn" class="dashboard-grid">
        <!-- 왼쪽 컬럼 -->
        <div class="left-column">
          <!-- 웰컴 배너 / 학습 시작 섹션 -->
          <section class="welcome-banner" :class="{ 'no-history': recentActivities.length === 0 }">
            <div class="welcome-header">
              <div class="welcome-text">
                <h1 >{{ authStore.user?.nickname || authStore.user?.name || '사용자' }}님, 오늘도 달콤한 성과를 만들어요!</h1>
                <p class="subtitle">오꿀쌤과 함께 목표 등급 달성까지 달려봐요!</p>
              </div>
              <img src="/okkul.svg" alt="Okkul" class="okkul-img" />
            </div>
            
            <div class="action-buttons-container">
              <div class="action-buttons">
                <!-- 실전 모의고사 -->
                <router-link 
                  to="/exam" 
                  class="action-card primary"
                >
                  <div class="card-icon">
                    <span class="material-icons">play_circle_filled</span>
                  </div>
                  <div class="card-info">
                    <span class="card-title">실전 모의고사 시작</span>
                    <span class="card-sub">15문항, 약 40분 소요</span>
                  </div>
                </router-link>
                
                <!-- 유형별 연습 -->
                <router-link to="/practice" class="action-card secondary">
                  <div class="card-icon">
                    <span class="material-icons">category</span>
                  </div>
                  <div class="card-info">
                    <span class="card-title">유형별 집중 연습</span>
                    <span class="card-sub">약점 보완 테마별 학습</span>
                  </div>
                </router-link>
              </div>
            </div>
          </section>

          <!-- 최근 성적 추이 -->
          <section class="stats-card">
            <div class="card-header">
              <h3>최근 성적 추이</h3>
              <span class="header-meta">최근 7일 기준</span>
            </div>
            
            <div class="chart-area">
              <div 
                v-for="(item, idx) in recentScores" 
                :key="idx"
                class="bar" 
                :style="{ height: item.height + '%' }"
              >
                <div :class="['fill', { active: item.active }]"></div>
                <span class="score-label">{{ item.score }}</span>
              </div>
            </div>
            
            <div class="chart-labels">
              <span v-for="item in recentScores" :key="item.day">{{ item.day }}</span>
            </div>
          </section>

          <!-- 최근 학습 기록 -->
          <section class="recent-activities">
            <div class="card-header">
              <h3>최근 학습 기록</h3>
              <a href="#" class="header-link">전체보기</a>
            </div>
            
            <div class="activity-list">
              <div 
                v-for="activity in recentActivities" 
                :key="activity.id"
                class="activity-item"
              >
                <div class="activity-icon" :style="{ backgroundColor: getStatusColor(activity.status) + '20', color: getStatusColor(activity.status) }">
                  <span class="material-icons-outlined">{{ getStatusIcon(activity.status) }}</span>
                </div>
                
                <div class="activity-content">
                  <div class="activity-header">
                    <h4>{{ activity.title }}</h4>
                    <span class="activity-date">{{ activity.date }}</span>
                  </div>
                  <p class="activity-detail">{{ activity.detail }}</p>
                  <div class="activity-meta">
                    <span v-if="activity.score" class="score-badge">예상 등급: {{ activity.score }}</span>
                    <span v-if="activity.wordCount" class="word-badge">{{ activity.wordCount }}</span>
                  </div>
                </div>
              </div>
            </div>
          </section>
        </div>
        
        <!-- 오른쪽 컬럼 -->
        <div class="right-column">
          <!-- 프로필 관리 카드 -->
          <section class="profile-card">
            <h3>프로필 관리</h3>
            
            <div class="profile-edit">
              <div class="profile-image-section" @click="goToMyPage">
                <div class="profile-avatar">
                  <img src="/default-profile.png" alt="프로필" class="profile-image" />
                  <div class="upload-overlay">
                    <span class="material-icons-outlined">settings</span>
                  </div>
                </div>
              </div>
    
              <div class="profile-info" @click="goToMyPage" style="cursor: pointer;">
                <div class="info-row">
                  <label>닉네임</label>
                  <span>{{ authStore.user?.nickname || authStore.user?.name || '사용자' }}</span>
                </div>
              </div>
            </div>
          </section>
          
          <!-- AI 등급 분석 -->
          <section class="ai-analysis-card">
            <h3>AI 등급 분석</h3>
            
            <div class="grade-circle">
              <svg viewBox="0 0 100 100">
                <circle class="bg" cx="50" cy="50" r="45"></circle>
                <circle class="fg" cx="50" cy="50" r="45" style="stroke-dashoffset: 70"></circle>
              </svg>
              <div class="grade-text">
                <span class="label">AI Predicted</span>
                <span class="grade">{{ userProfile.currentLevel || 'IH' }}</span>
                <span class="percent">상위 15%</span>
              </div>
            </div>

            <div class="grade-stats">
              <div class="stat">
                <p>현재 예상</p>
                <strong>{{ userProfile.currentLevel || 'IH' }}</strong>
              </div>
              <div class="divider"></div>
              <div class="stat">
                <p>목표 등급</p>
                <strong class="target">{{ userProfile.targetLevel || 'AL' }}</strong>
              </div>
            </div>

            <button class="detail-btn">상세 리포트 보기</button>
          </section>
        </div>
      </div>

      <!-- 비로그인 상태 -->
      <div v-else class="landing-hero">
        <div class="hero-content">
          <span class="badge">AI 기반 OPIc 트레이닝</span>
          
          <div class="hero-title-wrapper">
            <h1 class="hero-title">
              당신의 OPIc 목표,<br/>
              <span class="highlight-text">오꿀쌤</span>과 함께 달성하세요!
            </h1>
            <div class="okkul-wrapper">
              <img src="/okkul.svg" alt="Okkul" class="okkul-img" style="width: 120px; height: 120px;" />
            </div>
          </div>
          
          <p class="hero-desc">
            최신 AI 기술로 분석하는 내 영어 실력.<br/>
            실전 모의고사부터 취약 유형 집중 연습까지 한 번에.
          </p>
          
          <div class="hero-actions">
            <router-link to="/login" class="hero-btn-primary">시작하기</router-link>
          </div>
        </div>
        
        <div class="hero-features">
          <div class="feature-card">
            <div class="f-icon">🎯</div>
            <h3>실전 모의고사</h3>
            <p>실제 시험과 동일한 환경에서 연습하세요.</p>
          </div>
          <div class="feature-card">
            <div class="f-icon">📊</div>
            <h3>AI 정밀 분석</h3>
            <p>발음, 억양, 문법까지 AI가 분석해드립니다.</p>
          </div>
          <div class="feature-card">
            <div class="f-icon">📝</div>
            <h3>성적 리포트</h3>
            <p>등급 변화를 한눈에 파악하세요.</p>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Material+Icons+Outlined&display=swap');

/* 컨테이너 및 레이아웃 */
.page-container { 
  min-height: 100vh; 
  background: var(--bg-primary);
  transition: background 0.3s ease;
}

.page-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 32px 64px;
}

@media (max-width: 1024px) {
  .page-content {
    padding: 24px 32px;
  }
}

@media (max-width: 768px) {
  .page-content {
    padding: 16px 24px;
  }
}

/* 랜딩 페이지 */
.landing-hero { 
  padding: 80px 0; 
  text-align: center;
}

.badge {
  display: inline-block;
  padding: 6px 16px;
  background: var(--primary-light);
  color: #8B7300;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 700;
  margin-bottom: 32px;
}

.dark-mode .badge {
  background: rgba(255, 215, 0, 0.2);
  color: var(--primary-color);
}

.hero-title-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 32px;
  margin-bottom: 24px;
}

.hero-title {
  font-size: 3.5rem;
  font-weight: 900;
  color: var(--text-primary);
  line-height: 1.2;
  margin: 0;
}

.dark-mode .hero-title {
  color: #f1f5f9;
}

.highlight-text {
  color: var(--primary-color);
  position: relative;
  display: inline-block;
}

.okkul-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero-desc {
  font-size: 1.25rem;
  color: #64748b;
  line-height: 1.8;
  margin-bottom: 48px;
}

.dark-mode .hero-desc {
  color: #94a3b8;
}

.hero-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-bottom: 80px;
}

.hero-btn-primary {
  padding: 16px 40px;
  background: var(--primary-color);
  color: #212529;
  text-decoration: none;
  border: none;
  border-radius: var(--border-radius);
  font-weight: 700;
  font-size: 1.125rem;
  transition: all 0.2s;
  box-shadow: var(--shadow-md);
}

.hero-btn-primary:hover {
  transform: translateY(-2px);
  background: var(--primary-hover);
  box-shadow: var(--shadow-lg);
}

.dark-mode .hero-btn-primary {
  background: var(--primary-color);
  color: #212529;
}

.hero-features {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 32px;
  max-width: 1200px;
  margin: 0 auto;
}

.feature-card {
  padding: 40px 32px;
  background: var(--bg-secondary);
  border-radius: 24px;
  text-align: center;
  border: var(--border-primary);
  box-shadow: var(--shadow-sm);
}

.dark-mode .feature-card {
  background: var(--bg-secondary);
}

.f-icon {
  font-size: 3rem;
  margin-bottom: 20px;
}

.feature-card h3 {
  font-size: 1.5rem;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.dark-mode .feature-card h3 {
  color: #f1f5f9;
}

.feature-card p {
  font-size: 1rem;
  color: #64748b;
  line-height: 1.6;
}

.dark-mode .feature-card p {
  color: #94a3b8;
}

/* 대시보드 */
.dashboard-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 32px;
}

.left-column, .right-column {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 웰컴 배너 */
.welcome-banner {
  background: var(--bg-secondary);
  padding: 40px;
  border-radius: var(--border-radius);
  border: var(--border-primary);
  box-shadow: var(--shadow-md);
}

.dark-mode .welcome-banner {
  background: var(--bg-secondary);
  border-color: #FFFFFF;
}

.welcome-text {
  flex: 1;
}

.welcome-banner h1 {
  font-size: 2.5rem;
  font-weight: 900;
  color: var(--text-primary);
  line-height: 1.2;
  margin-bottom: 8px;
}

.okkul-img {
  width: 180px;
  height: auto;
  transition: transform 0.3s ease;
}

.okkul-img:hover {
  transform: scale(1.05) rotate(5deg);
}

.subtitle {
  font-size: 1.25rem;
  color: #64748b;
  margin: 0;
}

.action-buttons-container {
  margin-top: 40px;
  width: 100%;
}

.action-buttons {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.action-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 30px;
  border-radius: 20px;
  text-decoration: none;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: var(--border-primary);
  box-shadow: var(--shadow-sm);
}

.action-card.primary {
  background: var(--primary-color);
  color: #212529;
}

.action-card.secondary {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.action-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
  border-color: var(--primary-color);
}

.action-card:active {
  transform: translateY(-2px);
}

.card-icon {
  width: 60px;
  height: 60px;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-card.primary .card-icon {
  background: rgba(0, 0, 0, 0.1);
}

.card-icon .material-icons {
  font-size: 32px;
}

.card-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.card-title {
  font-size: 1.5rem;
  font-weight: 900;
}

.card-sub {
  font-size: 0.95rem;
  opacity: 0.8;
  font-weight: 600;
}

/* Dark Mode Overrides */
.dark-mode .action-card {
  border-color: rgba(255, 255, 255, 0.1);
}

.dark-mode .action-card.secondary {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.dark-mode .action-card.secondary .card-icon {
  background: rgba(255, 255, 255, 0.05);
}

.dark-mode .action-card:hover {
  box-shadow: var(--shadow-lg);
  border-color: var(--primary-color);
}

/* 카드 공통 스타일 */
.stats-card, .recent-activities, .profile-card, .ai-analysis-card {
  background: var(--bg-secondary);
  padding: 32px;
  border-radius: var(--border-radius);
  border: var(--border-primary);
  box-shadow: var(--shadow-md);
  transition: all 0.2s ease;
}

.dark-mode .stats-card,
.dark-mode .recent-activities,
.dark-mode .profile-card,
.dark-mode .ai-analysis-card {
  background: var(--bg-secondary);
  border-color: #FFFFFF;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.card-header h3 {
  font-size: 1.25rem;
  font-weight: 800;
  color: var(--text-primary);
}

.dark-mode .card-header h3 {
  color: #f1f5f9;
}

.header-meta {
  font-size: 0.875rem;
  color: #94a3b8;
}

.header-link {
  font-size: 0.875rem;
  color: #FFD700;
  text-decoration: none;
  font-weight: 700;
}

.header-link:hover {
  text-decoration: underline;
}

/* 차트 영역 */
.chart-area {
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  height: 200px;
  padding: 16px 0;
  margin-bottom: 16px;
}

.bar {
  flex: 1;
  width: 40px; /* 명시적 너비 추가 */
  max-width: 60px;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  background: rgba(0, 0, 0, 0.05); /* 바 배경 추가 */
  border-radius: 8px 8px 0 0;
}

.dark-mode .bar {
  background: rgba(255, 255, 255, 0.05);
}

.fill {
  width: 100%;
  height: 100%;
  background: var(--bg-tertiary);
  border: 1px solid rgba(0, 0, 0, 0.05);
  border-radius: 8px 8px 0 0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.dark-mode .fill {
  border-color: rgba(255, 255, 255, 0.05);
}

.fill.active {
  background: linear-gradient(to top, #FFD700, #FFA500);
  box-shadow: 0 0 20px rgba(255, 215, 0, 0.4);
}

.dark-mode .fill {
  background: linear-gradient(to top, #334155, #475569);
}

.score-label {
  position: absolute;
  top: -24px;
  font-size: 0.875rem;
  font-weight: 700;
  color: #64748b;
}

.dark-mode .score-label {
  color: #94a3b8;
}

.fill.active + .score-label {
  color: #FFD700;
}

.chart-labels {
  display: flex;
  justify-content: space-around;
  font-size: 0.875rem;
  color: #94a3b8;
  font-weight: 600;
}

/* 활동 목록 */
.activity-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.activity-item {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: #f8fafc;
  border-radius: 16px;
  transition: all 0.3s ease;
  cursor: pointer;
}

.activity-item:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.dark-mode .activity-item {
  background: var(--bg-primary);
}

.dark-mode .activity-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.activity-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.activity-icon .material-icons-outlined {
  font-family: 'Material Icons Outlined';
}

.activity-content {
  flex: 1;
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.activity-header h4 {
  font-size: 1rem;
  font-weight: 700;
  color: var(--text-primary);
}

.dark-mode .activity-header h4 {
  color: #f1f5f9;
}

.activity-date {
  font-size: 0.875rem;
  color: #94a3b8;
}

.activity-detail {
  font-size: 0.875rem;
  color: #64748b;
  margin-bottom: 8px;
}

.dark-mode .activity-detail {
  color: #94a3b8;
}

.activity-meta {
  display: flex;
  gap: 8px;
}

.score-badge, .word-badge {
  font-size: 0.75rem;
  padding: 4px 10px;
  border-radius: 6px;
  font-weight: 600;
}

.score-badge {
  background: #fff7ed;
  color: #ea580c;
}

.dark-mode .score-badge {
  background: rgba(234, 88, 12, 0.2);
  color: #fb923c;
}

.word-badge {
  background: #f0fdf4;
  color: #16a34a;
}

.dark-mode .word-badge {
  background: rgba(22, 163, 74, 0.2);
  color: #4ade80;
}

/* 프로필 카드 */
.profile-edit {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}

.profile-image-section {
  cursor: pointer;
  position: relative;
}

.profile-avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-tertiary);
  border: 4px solid var(--bg-secondary);
  box-shadow: 0 8px 24px rgba(255, 215, 0, 0.3);
  position: relative;
}

.dark-mode .profile-avatar {
  background: var(--bg-tertiary);
  border-color: var(--bg-primary);
}

.profile-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  z-index: 1;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
  border-radius: 50%;
}

.profile-image-section:hover .upload-overlay {
  opacity: 1;
}

.upload-overlay .material-icons-outlined {
  color: white;
  font-size: 2rem;
  font-family: 'Material Icons Outlined';
}

.profile-info {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 12px;
  background: #f8fafc;
  border-radius: 8px;
}

.dark-mode .info-row {
  background: var(--bg-primary);
}

.info-row label {
  font-size: 0.875rem;
  color: #94a3b8;
  font-weight: 600;
}

.info-row span {
  font-size: 0.875rem;
  color: var(--text-primary);
  font-weight: 700;
}

.dark-mode .info-row span {
  color: #f1f5f9;
}

/* AI 등급 분석 */
.grade-circle { 
  position: relative; 
  width: 200px; 
  height: 200px; 
  margin: 32px auto; 
}

.grade-circle svg { 
  transform: rotate(-90deg); 
}

.grade-circle circle { 
  fill: none; 
  stroke-width: 10; 
  stroke-linecap: round; 
}

.grade-circle .bg { 
  stroke: #f1f5f9; 
}

.dark-mode .grade-circle .bg {
  stroke: #334155;
}

.grade-circle .fg { 
  stroke: #FFD700;
  stroke-dasharray: 283; 
  animation: fill-circle 2s ease-out forwards;
}

@keyframes fill-circle {
  from { stroke-dashoffset: 283; }
  to { stroke-dashoffset: 70; }
}

.grade-text { 
  position: absolute; 
  top: 50%; 
  left: 50%; 
  transform: translate(-50%, -50%);
  display: flex; 
  flex-direction: column; 
  align-items: center;
  gap: 4px;
}

.label {
  font-size: 0.75rem;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.grade { 
  font-size: 3rem; 
  font-weight: 900;
  color: #FFD700;
}

.percent {
  font-size: 0.875rem;
  color: #64748b;
  font-weight: 600;
}

.dark-mode .percent {
  color: #94a3b8;
}

.grade-stats {
  display: flex;
  justify-content: space-around;
  padding: 20px;
  background: #f8fafc;
  border-radius: 12px;
  margin-bottom: 20px;
}

.dark-mode .grade-stats {
  background: var(--bg-primary);
}

.stat {
  text-align: center;
}

.stat p {
  font-size: 0.875rem;
  color: #94a3b8;
  margin-bottom: 8px;
}

.stat strong {
  font-size: 1.5rem;
  font-weight: 900;
  color: var(--text-primary);
}

.dark-mode .stat strong {
  color: #f1f5f9;
}

.stat .target {
  color: #FFD700;
}

.divider {
  width: 1px;
  background: #e2e8f0;
}

.dark-mode .divider {
  background: #334155;
}

.detail-btn {
  width: 100%;
  padding: 12px;
  background: #f8fafc;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  font-weight: 700;
  color: var(--text-primary);
  cursor: pointer;
  transition: all 0.2s;
}

.dark-mode .detail-btn {
  background: var(--bg-primary);
  border-color: #334155;
  color: #f1f5f9;
}

.detail-btn:hover {
  background: #FFD700;
  border-color: #FFD700;
  color: #000;
}

/* 반응형 */
@media (max-width: 1024px) {
  .main-content {
    padding: 24px 32px;
  }

  .dashboard-grid {
    grid-template-columns: 1fr;
  }

  .hero-features {
    grid-template-columns: 1fr;
  }

  .hero-title {
    font-size: 2.5rem;
  }

  .hero-title-wrapper {
    flex-direction: column;
    gap: 24px;
  }
}

@media (max-width: 768px) {
  .main-content {
    padding: 16px 24px;
  }

  .action-buttons {
    grid-template-columns: 1fr;
  }

  .hero-title {
    font-size: 2rem;
  }
}
</style>