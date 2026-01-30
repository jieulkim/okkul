<script setup>
import { inject, onMounted, watch, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

// 로그인 여부 확인
const isLoggedIn = computed(() => !!authStore.user)

// 사용자 이름
const userName = computed(() => authStore.user?.nickname || authStore.user?.name || '사용자')

// 다크모드 상태
const isDarkMode = inject('isDarkMode', ref(false))

// 다크모드 변경 감지
watch(isDarkMode, (newVal) => {
  if (newVal) {
    document.documentElement.classList.add('dark-mode')
  } else {
    document.documentElement.classList.remove('dark-mode')
  }
}, { immediate: true })

onMounted(() => {
  if (isDarkMode.value) {
    document.documentElement.classList.add('dark-mode')
  }
})

// [Logged-Out] 메뉴 아이템 데이터
const guestMenuItems = [
  {
    title: '유형별 집중 연습하기',
    desc: '약점 보완 및 테마별 튜토리얼 학습',
    path: '/practice',
    badge: '추천'
  },
  {
    title: '실전 모의고사 응시하기',
    desc: '실제 시험 환경과 동일한 모의 테스트',
    path: '/exam',
    badge: null
  },
  {
    title: '나의 결과표 보러가기',
    desc: 'AI 분석 결과 및 상세 피드백 확인',
    path: '/feedback',
    badge: null
  }
]

// [Logged-In] 대시보드 실제 기능 카드 데이터
const dashboardCards = [
  { 
    icon: 'play_lesson', 
    title: '실전 모의고사',
    desc: '실전과 동일한 환경 테스트',
    path: '/exam' 
  },
  { 
    icon: 'category', 
    title: '유형별 연습',
    desc: '취약 유형 집중 공략',
    path: '/practice' 
  },
  { 
    icon: 'history_edu', 
    title: '피드백 내역',
    desc: '나의 성장 기록 리포트',
    path: '/feedback' 
  },
  { 
    icon: 'account_circle', 
    title: '마이페이지',
    desc: '정보 수정 및 목표 관리',
    path: '/mypage' 
  }
]

// 카드 클릭 핸들러 (비로그인 제한 로직 포함)
const handleCardClick = (path) => {
  if (!isLoggedIn.value) {
    router.push('/login')
  } else {
    router.push(path)
  }
}

// 로그인 대시보드 하단 메인 액션
const handleMainAction = () => {
  router.push('/exam')
}
</script>

<template>
  <div class="home-main-container">
    <!-- 1. 비로그인 상태: 세련된 스플릿 레이아웃 -->
        <div v-if="!isLoggedIn" class="split-home-container">
      <div class="visual-section">
        <div class="glass-circles">
          <div class="circle circle-1"></div>
          <div class="circle circle-2"></div>
          <div class="circle circle-3"></div>
          <div class="circle circle-4"></div>
        </div>
        <div class="visual-logo">
          <img src="/okkul.svg" alt="Okkul" class="floating-okkul" />
        </div>
      </div>

      <div class="content-section">
        <div class="content-wrapper">
          <header class="home-header">
            <h1 class="main-title">
              진짜 나를 알아가는 과정,<br/>
              <span class="highlight">오꿀쌤</span>과 완벽하게 준비하기
            </h1>
            <p class="description">
              오꿀쌤은 <strong>최신 AI 분석, 실전 모의고사, 유형별 연습</strong>으로 이루어져 있습니다.
            </p>
            <p class="sub-description">
              각 학습은 약 10~40분 정도 소요되며, 언제 어디서든 자유롭게 응시 가능합니다.
            </p>
          </header>

          <nav class="action-list">
            <div 
              v-for="item in guestMenuItems" 
              :key="item.title"
              class="action-item-card"
              @click="handleCardClick(item.path)"
            >
              <div class="card-left">
                <span v-if="item.badge" class="item-badge">{{ item.badge }}</span>
                <h3 class="item-title">{{ item.title }}</h3>
              </div>
              <span class="material-icons arrow-icon">chevron_right</span>
            </div>
          </nav>

          <footer class="home-footer">
            <p class="footer-contact">
              <span class="material-icons info-icon">info</span>
              학습 중 문제가 생길 경우 <a href="mailto:support@okkul.ai">support@okkul.ai</a>로 문의해주세요.
            </p>
          </footer>
        </div>
      </div>
    </div>

    <!-- 2. 로그인 상태: 이미지 구성 기반 '기능 중심' 대시보드 -->
    <div v-else class="login-dashboard-container">
      <div class="dashboard-inner">
        <div class="top-visual">
          <div class="icon-circle">
            <span class="material-icons logo-icon">auto_awesome</span>
          </div>
        </div>
        
        <div class="guide-card">
          <header class="guide-header">
            <h2 class="guide-title"><span class="user-highlight">{{ userName }}</span>님, 오늘 어떤 학습을 시작할까요?</h2>
            <p class="guide-subtitle">원하는 메뉴를 선택하여 실전에 대비하세요.</p>
          </header>

          <div class="info-grid">
            <div 
              v-for="(card, index) in dashboardCards" 
              :key="index"
              class="info-card interactive"
              @click="handleCardClick(card.path)"
            >
              <div class="info-icon-box">
                <span class="material-icons">{{ card.icon }}</span>
              </div>
              <div class="info-content">
                <h3 class="info-title">{{ card.title }}</h3>
                <p class="info-text">{{ card.desc }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home-main-container {
  min-height: calc(100vh - 80px);
  background-color: var(--bg-primary);
}

/* ---------------------------------
   비로그인 (Split Layout) 스타일 
--------------------------------- */
.split-home-container {
  display: flex;
  min-height: calc(100vh - 80px);
}

.visual-section {
  flex: 1;
  position: relative;
  background: linear-gradient(135deg, #fffcf0 0%, #fff9e6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.dark-mode .visual-section {
  background: linear-gradient(135deg, #1a1c1e 0%, #121416 100%);
}

.glass-circles {
  position: absolute;
  width: 100%;
  height: 100%;
}

.circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.6;
  animation: pulse 10s infinite alternate ease-in-out;
}

.circle-1 { width: 400px; height: 400px; background: rgba(255, 215, 0, 0.4); top: -100px; left: -50px; }
.circle-2 { width: 500px; height: 500px; background: rgba(255, 165, 0, 0.3); bottom: -150px; right: -100px; animation-delay: -2s; }
.circle-3 { width: 350px; height: 350px; background: rgba(255, 230, 100, 0.3); top: 20%; right: 10%; animation-delay: -5s; }
.circle-4 { width: 300px; height: 300px; background: rgba(255, 200, 0, 0.2); bottom: 20%; left: 10%; animation-delay: -7s; }

@keyframes pulse {
  0% { transform: scale(1) translate(0, 0); }
  100% { transform: scale(1.2) translate(30px, 20px); }
}

.floating-okkul {
  width: 240px;
  height: auto;
  filter: drop-shadow(0 30px 50px rgba(0, 0, 0, 0.1));
  animation: float 4s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-20px); }
}

.content-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  background-color: var(--bg-secondary);
}

.content-wrapper {
  max-width: 600px;
  width: 100%;
}

.main-title {
  font-size: 2.25rem;
  font-weight: 800;
  color: var(--text-primary);
  line-height: 1.35;
  margin-bottom: 24px;
}

.highlight {
  color: var(--primary-color);
}

.description {
  font-size: 1.25rem;
  color: var(--text-secondary);
  margin-bottom: 12px;
}

.sub-description {
  font-size: 0.95rem;
  color: var(--text-tertiary);
}

.action-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 48px;
}

.action-item-card {
  background: var(--bg-primary);
  border: 1px solid var(--border-primary);
  border-radius: 20px;
  padding: 24px 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-item-card:hover {
  transform: translateX(10px);
  border-color: var(--primary-color);
  box-shadow: 0 10px 30px rgba(255, 215, 0, 0.1);
}

.item-badge {
  background: #212529;
  color: var(--primary-color);
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 0.75rem;
  font-weight: 800;
}

.dark-mode .item-badge {
  background: var(--primary-color);
  color: #212529;
}

.item-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--text-primary);
}

.arrow-icon {
  color: var(--text-tertiary);
}

.footer-contact {
  font-size: 0.875rem;
  color: var(--text-tertiary);
}

/* ---------------------------------
   로그인 (Dashboard Layout) 스타일 
--------------------------------- */
.login-dashboard-container {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  min-height: calc(100vh - 80px);
  background: radial-gradient(circle at top left, rgba(255, 215, 0, 0.05), transparent 40%),
              radial-gradient(circle at bottom right, rgba(255, 215, 0, 0.03), transparent 40%);
  background-color: #f8f9fa;
  transition: background 0.3s;
}

.dark-mode .login-dashboard-container {
  background-color: #121416;
}

.dashboard-inner {
  width: 100%;
  max-width: 1100px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 30px;
}

.top-visual .icon-circle {
  width: 72px;
  height: 72px;
  background: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
  border: 4px solid rgba(255, 215, 0, 0.1);
}

.dark-mode .icon-circle {
  background: #1e2124;
  border-color: rgba(255, 215, 0, 0.05);
}

.logo-icon {
  font-size: 36px;
  color: #FFD700;
  animation: rotate-slow 10s linear infinite;
}

@keyframes rotate-slow {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.guide-card {
  background: white;
  border-radius: 40px;
  padding: 50px 60px;
  width: 100%;
  box-shadow: 0 40px 100px rgba(0, 0, 0, 0.08);
  text-align: center;
  position: relative;
  overflow: hidden;
}

.dark-mode .guide-card {
  background: #1e2124;
  box-shadow: 0 40px 100px rgba(0, 0, 0, 0.4);
}

.guide-header {
  margin-bottom: 40px;
}

.user-highlight {
  color: #FFD700;
  font-weight: 900;
}

.guide-title {
  font-size: 2rem;
  font-weight: 800;
  color: #212529;
  margin-bottom: 12px;
  word-break: keep-all;
}

.dark-mode .guide-title {
  color: #f1f5f9;
}

.guide-subtitle {
  font-size: 1.1rem;
  color: #64748b;
  font-weight: 500;
}

.dark-mode .guide-subtitle {
  color: #94a3b8;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 50px;
}

.info-card.interactive {
  background: #ffffff;
  border: 1px solid #f1f5f9;
  border-radius: 24px;
  padding: 30px 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.dark-mode .info-card.interactive {
  background: #25282c;
  border-color: rgba(255, 255, 255, 0.03);
}

.info-card.interactive:hover {
  transform: translateY(-12px);
  border-color: #FFD700;
  box-shadow: 0 20px 40px rgba(255, 215, 0, 0.1);
}

.dark-mode .info-card.interactive:hover {
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
}

.info-icon-box {
  width: 64px;
  height: 64px;
  background: #fffcf0;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  transition: all 0.3s;
}

.dark-mode .info-icon-box {
  background: #2d3136;
}

.info-card.interactive:hover .info-icon-box {
  background: #FFD700;
  transform: scale(1.1);
}

.info-icon-box .material-icons {
  font-size: 32px;
  color: #FFD700;
  transition: color 0.3s;
}

.info-card.interactive:hover .material-icons {
  color: #212529;
}

.info-content {
  text-align: center;
}

.info-title {
  font-size: 1.15rem;
  font-weight: 800;
  color: #1e293b;
  margin-bottom: 8px;
}

.dark-mode .info-title {
  color: #f8fafc;
}

.info-text {
  font-size: 0.9rem;
  font-weight: 500;
  color: #64748b;
  line-height: 1.4;
}

.dark-mode .info-text {
  color: #94a3b8;
}

.primary-gold-btn {
  background: #FFD700;
  color: #212529;
  border: none;
  padding: 18px 60px;
  border-radius: 50px;
  font-size: 1.2rem;
  font-weight: 800;
  cursor: pointer;
  box-shadow: 0 10px 30px rgba(255, 215, 0, 0.25);
  transition: all 0.3s;
}

.primary-gold-btn:hover {
  transform: translateY(-3px) scale(1.02);
  background: #ffdb1a;
  box-shadow: 0 15px 40px rgba(255, 215, 0, 0.35);
}

.primary-gold-btn:active {
  transform: scale(0.98);
}

@media (max-width: 1024px) {
  .info-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
