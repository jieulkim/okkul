<script setup>
import { ref, computed, inject, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router' // Add router for redirection
import { useAuthStore } from '@/stores/auth' // Import auth store

const authStore = useAuthStore()
const router = useRouter()

// 사용자 프로필 데이터 (Auth Store 사용)
// authStore.user가 null이면 로딩중이거나 비로그인 상태
const userProfile = computed(() => authStore.user || {
  nickname: '게스트',
  targetLevel: '-',
  currentLevel: '-',
  name: '게스트'
})

// 다크모드 상태 주입
const isDarkMode = inject('isDarkMode', ref(false))

// 다크모드 변경 감지
watch(isDarkMode, (newVal) => {
  if (newVal) {
    document.documentElement.classList.add('dark-mode')
  } else {
    document.documentElement.classList.remove('dark-mode')
  }
}, { immediate: true })

// 컴포넌트 마운트 시 다크모드 클래스 적용 및 로그인 체크
onMounted(() => {
  if (isDarkMode.value) {
    document.documentElement.classList.add('dark-mode')
  }
})

// 프로필 이미지 업로드 (Store Action 사용)
const profileImageInput = ref(null)

const handleProfileImageUpload = async (event) => {
  const file = event.target.files[0]
  if (file) {
    await authStore.updateProfileImage(file)
  }
}

const triggerImageUpload = () => {
  profileImageInput.value?.click()
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
  <div class="home-container">
    <main class="main-content">
      <div class="dashboard-grid">
        <!-- 왼쪽 컬럼 -->
        <div class="left-column">
          <!-- 웰컴 배너 -->
          <section class="welcome-banner">
            <h1>{{ userProfile.nickname }}님, 오늘도 <span class="highlight">꿀</span>처럼 달콤한 성과를 만들어요! 🍯</h1>
            <p class="subtitle">목표 등급 {{ userProfile.targetLevel }} 달성까지 단 2단계 남았습니다.</p>
            
            <div class="action-buttons">
              <router-link to="/exam" class="btn-primary">
                <span class="material-icons-outlined btn-icon">play_circle_filled</span>
                <div class="btn-text">
                  <span class="title">실전 모의고사 시작</span>
                  <span class="sub">15문항, 약 40분 소요</span>
                </div>
              </router-link>
              
              <router-link to="/practice" class="btn-secondary">
                <span class="material-icons-outlined btn-icon">category</span>
                <div class="btn-text">
                  <span class="title">유형별 집중 연습</span>
                  <span class="sub">약점 보완 테마별 학습</span>
                </div>
              </router-link>
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
              <div class="profile-image-section" @click="triggerImageUpload">
                <div class="profile-preview">
                  <img 
                    v-if="userProfile.profileImage" 
                    :src="userProfile.profileImage" 
                    alt="프로필"
                  />
                  <span v-else class="profile-placeholder">
                    {{ userProfile.nickname?.[0]?.toUpperCase() || 'U' }}
                  </span>
                  <div class="upload-overlay">
                    <span class="material-icons-outlined">photo_camera</span>
                  </div>
                </div>
                <input 
                  ref="profileImageInput"
                  type="file" 
                  accept="image/*"
                  @change="handleProfileImageUpload"
                  style="display: none"
                />
              </div>
    
              <div class="profile-info">
                <div class="info-row">
                  <label>닉네임</label>
                  <span>{{ userProfile.nickname }}</span>
                </div>
                <div class="info-row">
                  <label>이름</label>
                  <span>{{ userProfile.name }}</span>
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
                <span class="grade">{{ userProfile.currentLevel }}</span>
                <span class="percent">상위 15%</span>
              </div>
            </div>

            <div class="grade-stats">
              <div class="stat">
                <p>현재 예상</p>
                <strong>{{ userProfile.currentLevel }}</strong>
              </div>
              <div class="divider"></div>
              <div class="stat">
                <p>목표 등급</p>
                <strong class="target">{{ userProfile.targetLevel }}</strong>
              </div>
            </div>

            <button class="detail-btn">상세 리포트 보기</button>
          </section>
        </div>
      </div>
    </main>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Material+Icons+Outlined&display=swap');

.home-container { 
  min-height: 100vh; 
  background: linear-gradient(to bottom, #fafafa 0%, #f5f5f5 100%);
  transition: background 0.3s ease;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Helvetica Neue', Arial, sans-serif;
}

.dark-mode .home-container {
  background: linear-gradient(to bottom, #0f172a 0%, #1e293b 100%);
}

.main-content { 
  max-width: 1400px; 
  margin: 0 auto; 
  padding: 40px 32px; 
}

.dashboard-grid { 
  display: grid; 
  grid-template-columns: 1fr 420px; 
  gap: 28px; 
}

@media (max-width: 1200px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}

section { 
  background: white; 
  border-radius: 20px; 
  border: 1px solid #f1f5f9; 
  padding: 28px; 
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  margin-bottom: 24px;
  transition: all 0.3s ease;
}

.dark-mode section {
  background: #1e293b;
  border-color: #334155;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.card-header h3 {
  font-size: 20px;
  font-weight: 800;
  color: #1e293b;
}

.dark-mode .card-header h3 {
  color: #f1f5f9;
}

.header-meta {
  font-size: 13px;
  color: #94a3b8;
}

.header-link {
  font-size: 14px;
  color: #FFD700;
  font-weight: 600;
  text-decoration: none;
}

.welcome-banner h1 { 
  font-size: 32px; 
  font-weight: 900; 
  margin-bottom: 12px;
  color: #0f172a;
  line-height: 1.3;
}

.dark-mode .welcome-banner h1 {
  color: #f1f5f9;
}

.highlight { 
  color: #FFD700;
  text-shadow: 0 0 20px rgba(255, 215, 0, 0.3);
}

.subtitle {
  color: #64748b;
  font-size: 15px;
  margin-bottom: 32px;
}

.dark-mode .subtitle {
  color: #94a3b8;
}

.action-buttons { 
  display: grid; 
  grid-template-columns: 1fr 1fr; 
  gap: 16px; 
}

@media (max-width: 768px) {
  .action-buttons {
    grid-template-columns: 1fr;
  }
}

.btn-primary, .btn-secondary {
  display: flex; 
  align-items: center; 
  gap: 16px;
  padding: 24px; 
  border-radius: 16px; 
  cursor: pointer; 
  border: none; 
  transition: all 0.2s;
  text-decoration: none;
  color: inherit;
}

.btn-primary { 
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
  box-shadow: 0 8px 24px rgba(255, 215, 0, 0.25);
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 32px rgba(255, 215, 0, 0.35);
}

.btn-secondary { 
  background: white; 
  border: 2px solid #FFD700;
}

.dark-mode .btn-secondary {
  background: #2d3748;
  border-color: #FFD700;
}

.btn-secondary:hover {
  background: #fffef0;
  transform: translateY(-2px);
}

.dark-mode .btn-secondary:hover {
  background: #374151;
}

.btn-icon {
  font-size: 32px;
  color: #1e293b;
  font-family: 'Material Icons Outlined';
}

.dark-mode .btn-secondary .btn-icon {
  color: #FFD700;
}

.btn-text { 
  text-align: left; 
  display: flex; 
  flex-direction: column;
  gap: 4px;
}

.btn-text .title { 
  font-weight: 800; 
  font-size: 16px;
  color: #0f172a;
}

.dark-mode .btn-secondary .btn-text .title {
  color: #f1f5f9;
}

.btn-text .sub { 
  font-size: 13px; 
  color: #64748b;
}

.dark-mode .btn-secondary .btn-text .sub {
  color: #94a3b8;
}

.chart-area { 
  height: 200px; 
  display: flex; 
  align-items: flex-end; 
  justify-content: space-between; 
  padding: 0 20px;
  gap: 12px;
}

.bar { 
  flex: 1;
  background: #f1f5f9; 
  border-radius: 8px 8px 0 0; 
  position: relative;
  transition: all 0.3s ease;
}

.dark-mode .bar {
  background: #334155;
}

.bar:hover {
  transform: translateY(-4px);
}

.fill { 
  position: absolute; 
  bottom: 0; 
  width: 100%; 
  height: 100%; 
  background: linear-gradient(to top, #FFD700, #FFA500);
  opacity: 0.4; 
  border-radius: inherit;
  transition: opacity 0.3s;
}

.fill.active { 
  opacity: 1;
  box-shadow: 0 -4px 12px rgba(255, 215, 0, 0.4);
}

.score-label {
  position: absolute;
  top: -24px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 12px;
  font-weight: 700;
  color: #64748b;
}

.dark-mode .score-label {
  color: #94a3b8;
}

.chart-labels { 
  display: flex; 
  justify-content: space-between; 
  margin-top: 12px; 
  color: #94a3b8; 
  font-size: 13px;
  font-weight: 600;
}

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
  border-radius: 12px;
  transition: all 0.2s;
  cursor: pointer;
}

.dark-mode .activity-item {
  background: #0f172a;
}

.activity-item:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
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
  font-size: 15px;
  font-weight: 700;
  color: #1e293b;
}

.dark-mode .activity-header h4 {
  color: #f1f5f9;
}

.activity-date {
  font-size: 12px;
  color: #94a3b8;
}

.activity-detail {
  font-size: 13px;
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
  font-size: 11px;
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
  font-size: 11px;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.grade { 
  font-size: 48px; 
  font-weight: 900;
  background: linear-gradient(135deg, #FFD700, #FFA500);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.percent {
  font-size: 13px;
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
  background: #0f172a;
}

.stat {
  text-align: center;
}

.stat p {
  font-size: 13px;
  color: #94a3b8;
  margin-bottom: 8px;
}

.stat strong {
  font-size: 24px;
  font-weight: 900;
  color: #1e293b;
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
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  font-weight: 700;
  color: #1e293b;
  cursor: pointer;
  transition: all 0.2s;
}

.dark-mode .detail-btn {
  background: #0f172a;
  border-color: #334155;
  color: #f1f5f9;
}

.detail-btn:hover {
  background: #FFD700;
  border-color: #FFD700;
  color: #000;
}

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

.profile-preview {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
  border: 4px solid #fff;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  position: relative;
}

.dark-mode .profile-preview {
  border-color: #1e293b;
}

.profile-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-placeholder {
  font-size: 48px;
  font-weight: 900;
  color: #000;
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
}

.profile-image-section:hover .upload-overlay {
  opacity: 1;
}

.upload-overlay .material-icons-outlined {
  color: white;
  font-size: 32px;
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
  background: #0f172a;
}

.info-row label {
  font-size: 13px;
  color: #94a3b8;
  font-weight: 600;
}

.info-row span {
  font-size: 14px;
  color: #1e293b;
  font-weight: 700;
}

.dark-mode .info-row span {
  color: #f1f5f9;
}
</style>