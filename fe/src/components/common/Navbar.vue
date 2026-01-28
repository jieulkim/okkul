<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

// 1. 로그인 여부 판단 (authStore의 user 상태를 실시간 감시)
const isLoggedIn = computed(() => !!authStore.user)

// 2. 프로필 표시 데이터 (유저 닉네임의 첫 글자)
const profileInitial = computed(() => {
  return authStore.user?.nickname?.[0]?.toUpperCase() || 'U'
})

// 네비게이션 메뉴
const navItems = [
  { path: '/exam', label: '실전 모의고사', icon: 'assignment' },
  { path: '/practice', label: '유형별 연습', icon: 'category' }
  // 피드백 리포트는 추후 경로 확정 시 추가
]

// 3. 로그아웃 처리
const handleLogout = () => {
  if (confirm('로그아웃 하시겠습니까?')) {
    console.log('[Navbar] Initiating logout...')
    authStore.logout()
  }
}

// 4. 현재 활성 메뉴 표시를 위한 함수
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
        <!-- 로그인 상태일 때 -->
        <template v-if="isLoggedIn">
          <!-- 프로필 - 마이페이지로 이동 -->
          <router-link to="/mypage" class="user-profile" :class="{ active: isActive('/mypage') }">
            <div class="profile-avatar">
              <span class="profile-initial">{{ profileInitial }}</span>
            </div>
            <span class="profile-name">{{ authStore.user?.nickname }}님</span>
          </router-link>

          <!-- 로그아웃 버튼 -->
          <button class="logout-btn" @click="handleLogout" title="로그아웃">
            <span class="material-icons-outlined">logout</span>
          </button>
        </template>

        <!-- 로그인 안 했을 때 -->
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
</style>