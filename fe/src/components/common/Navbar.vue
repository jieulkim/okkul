<script setup>
import { inject, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()

// 전역 다크모드 상태 및 토글 함수 주입
const isDarkMode = inject('isDarkMode', null)
const toggleDarkMode = inject('toggleDarkMode', null)

// 로그인 여부 (Auth Store)
const isAuthenticated = computed(() => authStore.isAuthenticated)
const userProfile = computed(() => authStore.user || {})

// 프로필 이미지 표시 계산
const profileDisplay = computed(() => {
  if (userProfile.value.profileImage) {
    return { type: 'image', value: userProfile.value.profileImage }
  }
  return { 
    type: 'initial', 
    value: userProfile.value.nickname?.[0]?.toUpperCase() || 'U'
  }
})

// 다크모드 토글 핸들러
const handleDarkModeToggle = () => {
  if (toggleDarkMode) {
    toggleDarkMode()
  }
}

// 네비게이션 메뉴
const navItems = [
  { path: '/exam', label: '실전 모의고사', icon: 'assignment' },
  { path: '/practice', label: '유형별 연습', icon: 'category' },
]

// 로그아웃 처리
const handleLogout = () => {
  if (confirm('로그아웃 하시겠습니까?')) {
    console.log('[Navbar] Initiating logout...')
    authStore.logout()
  }
}

// 현재 활성 메뉴 표시를 위한 함수
const isActive = (path) => {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}
</script>

<template>
  <header class="main-navbar">
    <div class="navbar-content">
      <!-- 로고 - 홈으로 이동 -->
      <router-link to="/" class="logo">
        <span class="logo-icon">🍯</span>
        <span class="logo-text">오꿀</span>
      </router-link>

      <!-- 네비게이션 메뉴 (로그인 시에만 노출) -->
      <nav v-if="isAuthenticated" class="nav-menu">
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
        <!-- 로그인 상태에 따른 UI -->
        <template v-if="isAuthenticated">
          <!-- 프로필 - 마이페이지로 이동 -->
          <router-link to="/mypage" class="user-profile" :class="{ active: isActive('/mypage') }">
            <div class="profile-avatar">
              <img 
                v-if="profileDisplay.type === 'image'" 
                :src="profileDisplay.value" 
                :alt="userProfile.nickname"
              />
              <span v-else class="profile-initial">{{ profileDisplay.value }}</span>
            </div>
            <span class="profile-name">{{ userProfile.nickname || userProfile.name }}님</span>
          </router-link>
          
          <!-- 로그아웃 버튼 -->
          <button @click="handleLogout" class="logout-btn" title="로그아웃">
            <span class="material-icons-outlined">logout</span>
          </button>
        </template>
        
        <template v-else>
           <router-link to="/login" class="login-btn">
             로그인
           </router-link>
        </template>
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
  max-width: 1400px;
  margin: 0 auto;
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 24px;
  font-weight: 900;
  color: #FFD700;
  text-decoration: none;
  cursor: pointer;
}

.logo-icon { font-size: 28px; }
.logo-text {
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

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
}

.nav-link:hover {
  background: #f8fafc;
  color: #1e293b;
}

.nav-link.active {
  background: #FFD700;
  color: #000;
  font-weight: 800;
}

.nav-controls {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 16px;
  border-radius: 24px;
  background: #f8fafc;
  text-decoration: none;
  transition: all 0.2s;
}

.user-profile:hover, .user-profile.active {
  background: #f1f5f9;
}

.profile-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  color: #000;
}

.profile-name {
  font-size: 14px;
  font-weight: 600;
  color: #475569;
}

.logout-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 1px solid #e2e8f0;
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #94a3b8;
  transition: all 0.2s;
}

.logout-btn:hover {
  background: #fff1f2;
  color: #e11d48;
  border-color: #fecaca;
}

.login-btn {
  padding: 10px 24px;
  background: #FFD700;
  color: #000;
  border-radius: 12px;
  text-decoration: none;
  font-weight: 700;
  transition: all 0.2s;
}

.guest-msg {
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
}

.login-btn {
  padding: 8px 20px;
  background: #FFD700;
  color: #000;
  border-radius: 20px;
  font-weight: 700;
  text-decoration: none;
  font-size: 14px;
  transition: all 0.2s;
  box-shadow: 0 2px 4px rgba(255, 215, 0, 0.2);
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 215, 0, 0.4);
  background: #ffdb1a;
}
</style>