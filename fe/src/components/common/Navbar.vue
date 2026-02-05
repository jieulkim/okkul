<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import defaultProfile from '@/assets/images/default-profile.png'
import okkulLogo from '@/assets/images/okkul_logo.png'

const route = useRoute()
const authStore = useAuthStore()

// 로그인 여부
const isLoggedIn = computed(() => !!authStore.user)

const userName = computed(() => {
  return authStore.user?.nickname || authStore.user?.name || '사용자'
})

// 기본 이미지 : 오꿀
const displayAvatar = computed(() => {
  const url = authStore.user?.profileImageUrl
  // URL이 없거나 구글 기본 이미지 경로인 경우 기본 오꿀 이미지 반환
  if (!url || url.includes('googleusercontent.com')) {
    return defaultProfile
  }
  return url
})

// 네비게이션 메뉴
const navItems = [
  { path: '/exam', label: '실전 모의고사', icon: 'assignment' },
  { path: '/practice', label: '유형별 연습', icon: 'category' },
  { path: '/feedback', label: '오꿀쌤 피드백', icon: 'feedback' }
]

// 로그아웃
const handleLogout = () => {
  if (confirm('로그아웃 하시겠습니까?')) {
    console.log('[Navbar] Initiating logout...')
    authStore.logout()
  }
}

// 활성 메뉴
const isActive = (path) => {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}
</script>

<template>
  <header class="main-navbar">
    <div class="navbar-content">
      <!-- 로고 -->
      <router-link to="/" class="logo">
        <img :src="okkulLogo" alt="오꿀 로고" class="navbar-logo-img" />
      </router-link>

      <!-- 네비게이션 메뉴 -->
      <nav v-if="isLoggedIn" class="nav-menu">
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
      <div v-else class="nav-menu guest-menu">
        <span class="guest-msg">✨ 로그인하고 AI 꿀팁 받으세요!</span>
      </div>

      <!-- 우측 컨트롤 -->
      <div class="nav-controls">
        <template v-if="isLoggedIn">
          <!-- 프로필 -->
          <router-link to="/mypage" class="user-profile" :class="{ active: isActive('/mypage') }">
            <div class="profile-avatar">
              <img :src="displayAvatar" alt="프로필" class="profile-image" />
            </div>
            <span class="profile-name">{{ userName }}님</span>
          </router-link>

          <!-- 로그아웃 -->
          <button class="icon-btn logout-btn" @click="handleLogout" title="로그아웃">
            <span class="material-icons-outlined">logout</span>
          </button>
        </template>

        <!-- 로그인 버튼 -->
        <router-link v-else to="/login" class="login-btn">
          시작하기
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
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  height: var(--header-height);
  display: flex;
  align-items: center;
}

.navbar-content {
  width: 100%;
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: center;
  /* 네비게이션 메뉴를 절대 위치로 중앙 정렬하기 위해 relative 설정 */
  position: relative;
  justify-content: space-between;
}

/* 로고 */
.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  font-weight: 800;
  font-size: 1.5rem;
  color: var(--text-main);
  transition: opacity 0.2s;
  /* 로고 영역 확보 */
  z-index: 10;
}

.logo:hover {
  opacity: 0.8;
}

.navbar-logo-img {
  height: 32px;
  width: auto;
}

.logo-text {
  font-family: var(--font-heading);
  letter-spacing: -0.02em;
  color: #FBC02D; /* Deep Yellow */
}

/* 네비게이션 메뉴 */
.nav-menu {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: var(--radius-full);
  text-decoration: none;
  color: var(--text-sub);
  font-weight: 600;
  font-size: 0.95rem;
  transition: all 0.2s var(--ease-smooth);
}

.nav-link:hover {
  background: rgba(0, 0, 0, 0.03);
  color: var(--text-main);
}

.nav-link.active {
  background: var(--honey-100);
  color: #F9A825;
}

.nav-icon {
  font-size: 1.2rem;
}

.guest-msg {
  font-size: 0.9rem;
  color: var(--text-tertiary);
  font-weight: 500;
}

/* 우측 컨트롤 */
.nav-controls {
  display: flex;
  align-items: center;
  gap: 12px;
}

.icon-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: transparent;
  color: var(--text-sub);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.icon-btn:hover {
  background: rgba(0, 0, 0, 0.05);
  color: var(--text-main);
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px 4px 4px;
  border-radius: var(--radius-full);
  text-decoration: none;
  background: rgba(0,0,0,0.02);
  transition: all 0.2s;
  border: 1px solid transparent;
}

.user-profile:hover {
  background: #fff;
  border-color: #eee;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.user-profile.active {
  background: #fff;
  border-color: var(--honey-200);
  color: var(--text-main);
}

.profile-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid #fff;
}

.profile-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-name {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--text-main);
  padding-right: 4px;
}

.login-btn {
  padding: 8px 20px;
  background: var(--honey-300);
  color: #3E2723;
  border-radius: var(--radius-full);
  font-weight: 700;
  font-size: 0.9rem;
  text-decoration: none;
  transition: transform 0.2s;
}

.login-btn:hover {
  transform: translateY(-1px);
  background: var(--honey-400);
}

@media (max-width: 768px) {
  .nav-menu { display: none; }
  .profile-name { display: none; }
  .navbar-content { padding: 0 16px; }
}
</style>