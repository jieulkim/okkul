<script setup>
import { computed, inject } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const authStore = useAuthStore()

// 로그인 여부
const isLoggedIn = computed(() => !!authStore.user)

// 사용자 닉네임 (우선순위: nickname > name > '사용자')
const userName = computed(() => {
  return authStore.user?.nickname || authStore.user?.name || '사용자'
})

// 네비게이션 메뉴
const navItems = [
  { path: '/exam', label: '실전 모의고사', icon: 'assignment' },
  { path: '/practice', label: '유형별 연습', icon: 'category' },
  { path: '/feedback', label: '오꿀쌤 피드백', icon: 'feedback' }
]

// 다크모드
const isDarkMode = inject('isDarkMode')
const toggleDarkMode = inject('toggleDarkMode')

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
        <span class="logo-text">오꿀</span>
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
      <div v-else class="nav-menu">
        <span class="guest-msg">로그인 후 AI 분석과 모의고사를 이용해보세요!</span>
      </div>

      <!-- 우측 컨트롤 -->
      <div class="nav-controls">
        <template v-if="isLoggedIn">
          <!-- 프로필 -->
          <router-link to="/mypage" class="user-profile" :class="{ active: isActive('/mypage') }">
            <div class="profile-avatar">
              <img src="/default-profile.png" alt="프로필" class="profile-image" />
            </div>
            <span class="profile-name">{{ userName }}님</span>
          </router-link>

          <!-- 다크모드 토글 -->
          <button class="theme-toggle" @click="toggleDarkMode" title="테마 변경">
            <span class="material-icons-outlined">{{ isDarkMode ? 'light_mode' : 'dark_mode' }}</span>
          </button>

          <!-- 로그아웃 -->
          <button class="logout-btn" @click="handleLogout" title="로그아웃">
            <span class="material-icons-outlined">logout</span>
          </button>
        </template>

        <!-- 로그인 버튼 -->
        <router-link v-else to="/login" class="login-btn">
          로그인
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
  background: var(--bg-secondary);
  border-bottom: var(--border-primary);
  transition: all 0.3s ease;
}

.dark-mode .main-navbar {
  background: var(--bg-secondary);
}

.navbar-content {
  max-width: var(--max-width);
  margin: 0 auto;
  padding: 0 var(--container-padding);
  display: flex;
  align-items: center;
  height: 64px;
  gap: 1.5rem;
}

@media (max-width: 768px) {
  .navbar-content {
    height: 56px;
    gap: 0.75rem;
  }
}

/* 로고 - 원래 버튼 스타일 유지 */
.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0.4em 1em;
  background: var(--primary-color);
  border: var(--border-secondary);
  border-radius: var(--border-radius);
  box-shadow: var(--shadow-sm);
  text-decoration: none;
  transition: all 0.2s ease;
  z-index: 10;
  cursor: pointer;
}

.logo:hover {
  transform: translate(-0.02em, -0.02em);
  box-shadow: var(--shadow-md);
}

.logo:active {
  transform: translate(0.02em, 0.02em);
  box-shadow: none;
}

.logo-text {
  font-size: 1.1rem;
  font-weight: 900;
  color: #000000;
  flex-shrink: 0;
  font-family: inherit;
}

/* 네비게이션 메뉴 - 원래 중앙 정렬 유지 */
.nav-menu {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

@media (max-width: 768px) {
  .nav-menu {
    display: none;
  }
}

.guest-msg {
  font-size: 0.95rem;
  color: var(--text-secondary);
  font-weight: 600;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: var(--border-radius);
  text-decoration: none;
  color: var(--text-secondary);
  font-weight: 900;
  font-size: var(--font-size-base);
  transition: all 0.2s;
  border: 2px solid transparent;
}

.nav-link:hover {
  background: var(--bg-tertiary);
  color: var(--text-primary);
}

.nav-link.active {
  background: var(--bg-tertiary);
  color: var(--primary-color);
  border-color: var(--primary-color);
}

.nav-icon {
  font-size: 1.25rem;
}

/* 프로필 */
.user-profile {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem 1rem;
  border-radius: var(--border-radius);
  text-decoration: none;
  transition: all 0.2s ease;
  border: 2px solid transparent;
}

.user-profile:hover {
  background: var(--bg-tertiary);
}

.user-profile.active {
  background: var(--bg-tertiary);
  border-color: var(--primary-color);
}

.profile-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: var(--bg-tertiary);
  border: 2px solid var(--primary-color);
}

.profile-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-name {
  font-size: 0.95rem;
  font-weight: 700;
  color: var(--text-primary);
  white-space: nowrap;
}

@media (max-width: 768px) {
  .profile-name {
    display: none;
  }
}

/* 컨트롤 버튼들 */
.nav-controls {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.theme-toggle,
.logout-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: var(--border-thin);
  background: var(--bg-tertiary);
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.theme-toggle:hover,
.logout-btn:hover {
  background: var(--bg-primary);
  color: var(--primary-color);
  border-color: var(--primary-color);
}

.login-btn {
  padding: 0.5rem 1.5rem;
  background: var(--primary-color);
  color: #000000;
  border-radius: var(--border-radius);
  font-weight: 900;
  text-decoration: none;
  border: var(--border-secondary);
  transition: all 0.2s ease;
  box-shadow: var(--shadow-sm);
}

.login-btn:hover {
  transform: translate(-0.02em, -0.02em);
  box-shadow: var(--shadow-md);
}
</style>