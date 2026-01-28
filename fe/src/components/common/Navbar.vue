<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// 로그인 여부
const isLoggedIn = computed(() => !!authStore.user)

// 프로필 이미지 URL (없거나 빈 문자열이면 기본 오꿀이 이미지)
const profileImageUrl = computed(() => {
  const userImage = authStore.user?.profileImageUrl
  if (!userImage || typeof userImage !== 'string' || userImage.trim() === '') {
    return '/default-profile.png'
  }
  
  // 절대 경로거나 base64 데이터인 경우 그대로 반환
  if (userImage.startsWith('http') || userImage.startsWith('data:')) {
    return userImage
  }
  
  // 상대 경로인 경우 API 베이스 URL 결합
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
        <span class="logo-icon">🍯</span>
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
        <span class="guest-msg">로그인 후 AI 분석과 모의고사를 이용해보세요! 🍯</span>
      </div>

      <!-- 우측 컨트롤 -->
      <div class="nav-controls">
        <template v-if="isLoggedIn">
          <!-- 프로필 -->
          <router-link to="/mypage" class="user-profile" :class="{ active: isActive('/mypage') }">
            <div class="profile-avatar">
              <img 
                :src="profileImageUrl" 
                alt="프로필"
                class="profile-image"
                @error="(e) => e.target.src = '/default-profile.png'"
              />
            </div>
            <span class="profile-name">{{ authStore.user?.nickname }}님</span>
          </router-link>

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
  border-bottom: 1px solid #e5e7eb;
  transition: all 0.3s ease;
}

.navbar-content {
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 2rem;
  display: flex;
  align-items: center;
  height: 70px;
  gap: 2rem;
}

.logo {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  text-decoration: none;
  font-weight: 800;
  font-size: 1.5rem;
  color: #1f2937;
  transition: transform 0.2s;
}

.logo:hover {
  transform: scale(1.05);
}

.logo-icon {
  font-size: 2rem;
}

.logo-text {
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.nav-menu {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.25rem;
  border-radius: 12px;
  text-decoration: none;
  color: #6b7280;
  font-weight: 600;
  font-size: 0.95rem;
  transition: all 0.2s;
  position: relative;
}

.nav-link:hover {
  background: #f9fafb;
  color: #1f2937;
}

.nav-link.active {
  background: linear-gradient(135deg, #FFF9E6 0%, #FFE4B3 100%);
  color: #92400e;
}

.nav-link.active::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 1.25rem;
  right: 1.25rem;
  height: 3px;
  background: linear-gradient(90deg, #FFD700, #FFA500);
  border-radius: 2px 2px 0 0;
}

.nav-icon {
  font-size: 1.25rem;
}

.guest-msg {
  color: #6b7280;
  font-size: 0.95rem;
  font-weight: 500;
}

.nav-controls {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem 1rem;
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

.user-profile.active {
  background: linear-gradient(135deg, #FFF9E6 0%, #FFE4B3 100%);
  border-color: #FFD700;
}

.profile-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid #FFD700;
  box-shadow: 0 2px 8px rgba(255, 215, 0, 0.3);
  background: white;
}

.profile-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-name {
  font-size: 0.95rem;
}

.logout-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
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

.login-btn {
  padding: 0.75rem 1.5rem;
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
  color: #92400e;
  text-decoration: none;
  border-radius: 12px;
  font-weight: 700;
  font-size: 0.95rem;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(255, 215, 0, 0.3);
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 215, 0, 0.4);
}

@media (max-width: 768px) {
  .navbar-content {
    padding: 0 1rem;
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
}
</style>