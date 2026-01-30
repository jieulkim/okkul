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
</script>

<template>
  <div class="home-container">
    <!-- 1. 비로그인 상태: 세련된 스플릿 레이아웃 (Pastel Theme) -->
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
        <div class="content-wrapper fade-in">
          <header class="home-header">
            <h1 class="main-title">
              진짜 나를 알아가는 과정,<br/>
              <span class="highlight">오꿀쌤</span>과 준비하기
            </h1>
            <p class="description">
              AI 분석, 실전 모의고사, 유형별 연습을 통해<br>
              가장 자연스러운 내 영어 실력을 키워보세요.
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
              학습 중 문의사항: support@okkul.ai
            </p>
          </footer>
        </div>
      </div>
    </div>

    <!-- 2. 로그인 상태: 기능 중심 대시보드 -->
    <div v-else class="dashboard-container">
      <div class="dashboard-inner fade-in">
        <div class="welcome-section">
          <h2 class="welcome-title">
            반가워요, <span class="user-name">{{ userName }}</span>님!
          </h2>
          <p class="welcome-desc">오늘도 즐겁게 영어 공부 시작해볼까요?</p>
        </div>

        <div class="card-grid">
          <div 
            v-for="(card, index) in dashboardCards" 
            :key="index"
            class="dashboard-card"
            @click="handleCardClick(card.path)"
          >
            <div class="icon-box">
              <span class="material-icons">{{ card.icon }}</span>
            </div>
            <h3 class="card-title">{{ card.title }}</h3>
            <p class="card-desc">{{ card.desc }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home-container {
  min-height: calc(100vh - var(--header-height));
  background-color: var(--bg-color);
}

/* ---------------------------------
   비로그인 (Split Layout)
--------------------------------- */
.split-home-container {
  display: flex;
  min-height: calc(100vh - var(--header-height));
}

.visual-section {
  flex: 1;
  position: relative;
  background: #FFFFFF;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

/* Abstract Orbs - Central Gradient Style */
.circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(50px);
  opacity: 0.3;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation: pulse-center 6s infinite alternate ease-in-out;
}

.circle-1 { 
  width: 600px; 
  height: 600px; 
  background: #FFFDE7; /* 가장 바깥쪽: 아주 연한 아이보리 */
  z-index: 0;
}

.circle-2 { 
  width: 500px; 
  height: 500px; 
  background: var(--honey-100); 
  animation-delay: -1s;
  z-index: 0;
}

.circle-3 { 
  width: 350px; 
  height: 350px; 
  background: var(--honey-200); 
  animation-delay: -2s;
  opacity: 0.5;
  z-index: 0;
}

.circle-4 { 
  width: 200px; 
  height: 200px; 
  background: var(--honey-300); 
  animation-delay: -3s; 
  opacity: 0.4;
  z-index: 0;
}

@keyframes pulse-center {
  0% { transform: translate(-50%, -50%) scale(1); }
  100% { transform: translate(-50%, -50%) scale(1.1); }
}

.floating-okkul {
  width: 220px;
  height: auto;
  filter: drop-shadow(0 20px 40px rgba(0,0,0,0.05));
  animation: float 5s ease-in-out infinite;
  z-index: 2;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-15px); }
}

.content-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: #FFF;
}

.content-wrapper {
  max-width: 500px;
  width: 100%;
}

.main-title {
  font-size: 2.5rem;
  font-weight: 800;
  color: var(--text-main);
  margin-bottom: 20px;
  line-height: 1.25;
}

.highlight {
  position: relative;
  z-index: 1;
  color: inherit;
}

.highlight::after {
  content: "";
  position: absolute;
  bottom: 0.1em;
  left: 0;
  width: 100%;
  height: 0.4em;
  background: var(--honey-200);
  z-index: -1;
  border-radius: 4px;
}

.description {
  font-size: 1.1rem;
  color: var(--text-sub);
  line-height: 1.6;
  margin-bottom: 40px;
}

.action-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 40px;
}

.action-item-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  background: #FAFAFA;
  border: 1px solid #F0F0F0;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.2s;
}

.action-item-card:hover {
  background: #FFF;
  border-color: var(--honey-300);
  box-shadow: 0 8px 20px rgba(0,0,0,0.04);
  transform: translateX(4px);
}

.item-badge {
  display: inline-block;
  font-size: 0.75rem;
  font-weight: 700;
  color: #F9A825;
  background: var(--honey-100);
  padding: 2px 8px;
  border-radius: 4px;
  margin-bottom: 4px;
}

.item-title {
  font-size: 1.1rem;
  font-weight: 700;
  color: var(--text-main);
}

.arrow-icon {
  color: var(--text-tertiary);
}

.footer-contact {
  font-size: 0.85rem;
  color: var(--text-tertiary);
  text-align: center;
}

/* ---------------------------------
   로그인 상태 (Dashboard)
--------------------------------- */
.dashboard-container {
  display: flex;
  justify-content: center;
  padding: 60px 20px;
  background: var(--bg-color);
}

.dashboard-inner {
  max-width: 1000px;
  width: 100%;
}

.welcome-section {
  text-align: center;
  margin-bottom: 48px;
}

.welcome-title {
  font-size: 2rem;
  font-weight: 800;
  color: var(--text-main);
  margin-bottom: 8px;
}

.user-name {
  color: var(--honey-600); /* Slightly darker yellow for text readability */
}

.welcome-desc {
  font-size: 1.1rem;
  color: var(--text-sub);
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 24px;
}

.dashboard-card {
  background: #FFF;
  border: 1px solid #F0F0F0;
  border-radius: var(--radius-lg);
  padding: 32px 24px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 12px rgba(0,0,0,0.02);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.dashboard-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 15px 30px rgba(0,0,0,0.06);
  border-color: var(--honey-200);
}

.icon-box {
  width: 60px;
  height: 60px;
  background: var(--honey-50);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  color: #FBC02D;
  transition: all 0.3s;
}

.dashboard-card:hover .icon-box {
  background: var(--honey-300);
  color: #FFF;
}

.card-title {
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--text-main);
  margin-bottom: 8px;
}

.card-desc {
  font-size: 0.95rem;
  color: var(--text-tertiary);
  line-height: 1.4;
}

/* Animations */
.fade-in {
  animation: fadeIn 0.6s ease-out forwards;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 900px) {
  .split-home-container { flex-direction: column; }
  .visual-section { min-height: 300px; }
  .content-section { padding: 40px 20px; }
}
</style>
