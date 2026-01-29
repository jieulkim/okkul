<script setup>
import { computed, inject } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const authStore = useAuthStore()

// 로그인 여부
const isLoggedIn = computed(() => !!authStore.user)

// 프로필 이미지가 있는지 확인
const hasProfileImage = computed(() => {
  const userImage = authStore.user?.profileImageUrl
  return userImage && typeof userImage === 'string' && userImage.trim() !== ''
})

// 프로필 이미지 URL
const profileImageUrl = computed(() => {
  const userImage = authStore.user?.profileImageUrl
  if (!userImage || typeof userImage !== 'string' || userImage.trim() === '') {
    return ''
  }
  
  if (userImage.startsWith('http') || userImage.startsWith('data:')) {
    return userImage
  }
  
  const baseUrl = import.meta.env.VITE_API_BASE_URL || ''
  const cleanBase = baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl
  const cleanPath = userImage.startsWith('/') ? userImage : `/${userImage}`
  return `${cleanBase}${cleanPath}`
})

// 네비게이션 메뉴
const navItems = [
  { path: '/exam', label: '실전 모의고사', icon: 'assignment' },
  { path: '/practice', label: '유형별 연습', icon: 'category' }
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
        <span class="logo-text-wrapper">
          <span class="logo-text">오꿀</span>
        </span>
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
              <img 
                v-if="hasProfileImage"
                :src="profileImageUrl" 
                alt="프로필"
                class="profile-image"
              />
              <img 
                v-else 
                src="/default-profile.png" 
                alt="기본 프로필"
                class="profile-image fallback"
              />
            </div>
            <span class="profile-name">{{ authStore.user?.nickname }}님</span>
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
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  border-bottom: 2px solid #e5e7eb;
  transition: all 0.3s ease;
}

.dark-mode .main-navbar {
  background: rgba(15, 23, 42, 0.95);
  border-bottom-color: #334155;
}

.navbar-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 64px;
  display: flex;
  align-items: center;
  height: 64px;
  gap: 1.2rem;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
  transition: transform 0.2s;
  position: relative;
  z-index: 10;
}

.logo:hover {
  transform: scale(1.02);
}

.logo-text-wrapper {
  background: #000000;
  padding: 8px 16px;
  border-radius: 12px;
  transition: all 0.2s;
}

.logo:hover .logo-text-wrapper {
  background: #1a1a1a;
}

.logo-text {
  font-size: 1.5rem;
  font-weight: 900;
  color: #FFD700;
  flex-shrink: 0;
}

.dark-mode .logo-text-wrapper {
  background: #FFD700;
}

.dark-mode .logo:hover .logo-text-wrapper {
  background: #FFC700;
}

.dark-mode .logo-text {
  color: #000000;
}

.nav-menu {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 16px;
  margin-right: 16px;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border-radius: 12px;
  text-decoration: none;
  color: #6b7280;
  font-weight: 600;
  font-size: 1.05rem;
  transition: all 0.2s;
  position: relative;
}

.nav-link:hover {
  background: #f9fafb;
  color: #1f2937;
}

.dark-mode .nav-link:hover {
  background: #1e293b;
  color: #f1f5f9;
}

.nav-link.active {
  background: linear-gradient(135deg, #FFF9E6 0%, #FFE4B3 100%);
  color: #92400e;
  border: 2px solid #FFD700;
}

.dark-mode .nav-link.active {
  background: rgba(255, 215, 0, 0.2);
  color: #ffd700;
  border-color: rgba(255, 215, 0, 0.3);
}

.nav-icon {
  font-size: 1.25rem;
}

.guest-msg {
  color: #6b7280;
  font-size: 1.05rem;
  font-weight: 600;
}

.dark-mode .guest-msg {
  color: #94a3b8;
}

.nav-controls {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 12px;
  border-radius: 12px;
  text-decoration: none;
  color: #374151;
  font-weight: 600;
  transition: all 0.2s;
  border: 2px solid transparent;
}

.user-profile:hover {
  background: #f9fafb;
  border-color: #FFD700;
}

.dark-mode .user-profile {
  color: #f1f5f9;
}

.dark-mode .user-profile:hover {
  background: #1e293b;
  border-color: #FFD700;
}

.user-profile.active {
  background: linear-gradient(135deg, #FFF9E6 0%, #FFE4B3 100%);
  border-color: #FFD700;
}

.dark-mode .user-profile.active {
  background: rgba(255, 215, 0, 0.2);
  border-color: rgba(255, 215, 0, 0.3);
}

.profile-avatar {
  width: 36px; /* 크기 축소 */
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid #FFD700;
  box-shadow: 0 2px 6px rgba(255, 215, 0, 0.2);
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0; /* 찌그러짐 방지 */
}

.dark-mode .profile-avatar {
  background: #1e293b;
}

.profile-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-image.fallback {
  width: 80%;
  height: 80%;
  object-fit: contain;
  opacity: 0.8;
}

.profile-name {
  font-size: 0.95rem;
}

.logout-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  border: none;
  background: #f9fafb;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s;
}

.logout-btn:hover {
  background: #fee2e2;
  color: #dc2626;
}

.dark-mode .logout-btn {
  background: #1e293b;
  color: #94a3b8;
}

.dark-mode .logout-btn:hover {
  background: rgba(220, 38, 38, 0.2);
  color: #ef4444;
}

.theme-toggle {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  border: none;
  background: #f9fafb;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s;
}

.theme-toggle:hover {
  background: #f3f4f6;
  color: #FFD700;
}

.dark-mode .theme-toggle {
  background: #334155;
  color: #94a3b8;
}

.dark-mode .theme-toggle:hover {
  background: #475569;
  color: #FFD700;
}

.login-btn {
  padding: 8px 16px;
  background: #FFD700;
  color: #000;
  text-decoration: none;
  border-radius: 12px;
  font-weight: 700;
  font-size: 0.9rem;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(255, 215, 0, 0.3);
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 215, 0, 0.4);
  background: #FFA500;
}

.dark-mode .login-btn {
  background: #FFD700;
  color: #000;
}

@media (max-width: 1024px) {
  .navbar-content {
    padding: 0 32px;
  }
}

@media (max-width: 768px) {
  .navbar-content {
    padding: 0 24px;
    gap: 1rem;
  }

  .nav-label {
    display: none;
  }

  .profile-name {
    display: none;
  }

  .guest-msg {
    font-size: 0.85rem;
  }

  .logo-text {
    font-size: 1.5rem;
  }
  
  .logo-character {
    width: 44px;
    height: 44px;
  }
}
</style>