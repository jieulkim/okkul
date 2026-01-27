<script setup>
import { inject, computed } from 'vue'
import { useRoute } from 'vue-router'

// Props
const props = defineProps({
  userProfile: {
    type: Object,
    default: () => ({
      name: '사용자',
      nickname: '오꿀이',
      profileImage: null
    })
  }
})

const route = useRoute()

// 전역 다크모드 상태 및 토글 함수 주입 (안전하게 처리)
const isDarkMode = inject('isDarkMode', null)
const toggleDarkMode = inject('toggleDarkMode', null)

// 디버깅용 로그
console.log('Navbar - isDarkMode:', isDarkMode)
console.log('Navbar - toggleDarkMode:', toggleDarkMode)

// 프로필 이미지 또는 첫 글자 표시
const profileDisplay = computed(() => {
  if (props.userProfile.profileImage) {
    return { type: 'image', value: props.userProfile.profileImage }
  }
  return { 
    type: 'initial', 
    value: props.userProfile.nickname?.[0]?.toUpperCase() || 'U'
  }
})

// 현재 활성 경로 확인
const isActive = (path) => {
  return route.path === path
}

// 다크모드 토글 핸들러
const handleDarkModeToggle = () => {
  console.log('Dark mode toggle clicked')
  if (toggleDarkMode) {
    toggleDarkMode()
    console.log('Dark mode toggled, new value:', isDarkMode?.value)
  } else {
    console.error('toggleDarkMode function not found')
  }
}

// 네비게이션 메뉴
const navItems = [
  { path: '/exam', label: '실전 모의고사', icon: 'assignment' },
  { path: '/practice', label: '유형별 연습', icon: 'category' },
  { path: '/reports', label: '피드백 리포트', icon: 'assessment' }
]
</script>

<template>
  <header class="main-navbar">
    <div class="navbar-content">
      <!-- 로고 - 홈으로 이동 -->
      <router-link to="/" class="logo">
        <span class="logo-icon">🍯</span>
        <span class="logo-text">오꿀</span>
      </router-link>

      <!-- 네비게이션 메뉴 -->
      <nav class="nav-menu">
        <router-link
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          :class="['nav-link', { active: isActive(item.path) }]"
        >
          <span class="material-icons-outlined nav-icon">{{ item.icon }}</span>
          <span class="nav-label">{{ item.label }}</span>
        </router-link>
      </nav>

      <!-- 우측 컨트롤 -->
      <div class="nav-controls">
        <!-- 다크모드 토글 -->
        <button class="dark-mode-toggle" @click="handleDarkModeToggle">
          <span class="material-icons-outlined">
            {{ isDarkMode ? 'light_mode' : 'dark_mode' }}
          </span>
        </button>

        <!-- 프로필 - 마이페이지로 이동 -->
        <router-link to="/mypage" class="user-profile">
          <div class="profile-avatar">
            <img 
              v-if="profileDisplay.type === 'image'" 
              :src="profileDisplay.value" 
              :alt="userProfile.nickname"
            />
            <span v-else class="profile-initial">{{ profileDisplay.value }}</span>
          </div>
          <span class="profile-name">{{ userProfile.nickname || userProfile.name }}</span>
        </router-link>
      </div>
    </div>
  </header>
</template>

<style scoped>
.main-navbar {
  position: sticky;
  top: 0;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid #e5e7eb;
  transition: all 0.3s ease;
}

:global(.dark-mode) .main-navbar {
  background: rgba(26, 32, 44, 0.95);
  border-bottom-color: #2d3748;
}

.navbar-content {
  max-width: 1400px;
  margin: 0 auto;
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
}

/* 로고 */
.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 24px;
  font-weight: 900;
  color: #FFD700;
  text-decoration: none;
  cursor: pointer;
  transition: transform 0.2s;
}

.logo:hover {
  transform: scale(1.05);
}

.logo-icon {
  font-size: 28px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

.logo-text {
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

/* 네비게이션 메뉴 */
.nav-menu {
  display: flex;
  gap: 8px;
  flex: 1;
  justify-content: center;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 12px;
  text-decoration: none;
  color: #64748b;
  font-weight: 600;
  font-size: 15px;
  transition: all 0.2s;
  position: relative;
}

:global(.dark-mode) .nav-link {
  color: #94a3b8;
}

.nav-link:hover {
  background: #f8fafc;
  color: #1e293b;
}

:global(.dark-mode) .nav-link:hover {
  background: #2d3748;
  color: #e2e8f0;
}

.nav-link.active {
  background: #FFD700;
  color: #000;
  font-weight: 800;
  box-shadow: 0 4px 12px rgba(255, 215, 0, 0.3);
}

:global(.dark-mode) .nav-link.active {
  background: #FFD700;
  color: #000;
  box-shadow: 0 4px 12px rgba(255, 215, 0, 0.5);
}

.nav-icon {
  font-size: 20px;
}

.nav-label {
  white-space: nowrap;
}

/* 우측 컨트롤 */
.nav-controls {
  display: flex;
  align-items: center;
  gap: 16px;
}

/* 다크모드 토글 */
.dark-mode-toggle {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: 2px solid #e5e7eb;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

:global(.dark-mode) .dark-mode-toggle {
  background: #2d3748;
  border-color: #4a5568;
  color: #fbbf24;
}

.dark-mode-toggle:hover {
  transform: rotate(20deg) scale(1.1);
  border-color: #FFD700;
}

.dark-mode-toggle .material-icons-outlined {
  font-size: 22px;
  color: #64748b;
  transition: color 0.2s;
}

:global(.dark-mode) .dark-mode-toggle .material-icons-outlined {
  color: #fbbf24;
}

/* 프로필 */
.user-profile {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 16px;
  border-radius: 24px;
  background: #f8fafc;
  cursor: pointer;
  transition: all 0.2s;
  text-decoration: none;
}

:global(.dark-mode) .user-profile {
  background: #2d3748;
}

.user-profile:hover {
  background: #e2e8f0;
  transform: translateY(-2px);
}

:global(.dark-mode) .user-profile:hover {
  background: #374151;
}

.profile-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

:global(.dark-mode) .profile-avatar {
  border-color: #1a202c;
}

.profile-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-initial {
  font-size: 16px;
  font-weight: 900;
  color: #000;
}

.profile-name {
  font-size: 15px;
  font-weight: 700;
  color: #1e293b;
}

:global(.dark-mode) .profile-name {
  color: #e2e8f0;
}

/* 반응형 */
@media (max-width: 768px) {
  .navbar-content {
    padding: 0 16px;
    height: 60px;
  }

  .nav-label {
    display: none;
  }

  .nav-link {
    padding: 10px 12px;
  }

  .profile-name {
    display: none;
  }

  .logo-text {
    display: none;
  }
}
</style>