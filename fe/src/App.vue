<script setup>
import { computed, onMounted } from 'vue'
import { RouterView, useRouter } from 'vue-router'
import Navbar from '@/components/common/Navbar.vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

// 전역 사용자 상태 (Store와 연동)
const userProfile = computed(() => authStore.user || {
  nickname: '게스트',
  profileImage: null
})

onMounted(async () => {
  // 1. URL에서 토큰 추출 (백엔드에서 직접 특정 페이지로 리다이렉트하는 경우 대응)
  const urlParams = new URLSearchParams(window.location.search);
  const accessToken = urlParams.get('accessToken');
  const refreshToken = urlParams.get('refreshToken');

  if (accessToken) {
    console.log('[App] Token found in URL. Saving to localStorage...');
    localStorage.setItem('accessToken', accessToken);
    if (refreshToken) localStorage.setItem('refreshToken', refreshToken);
    
    // URL에서 토큰 파라미터 제거 (보안 및 미관상)
    const newUrl = window.location.pathname + window.location.hash;
    window.history.replaceState({}, document.title, newUrl);
    
    // 토큰 저장 후 유저 정보 갱신
    await authStore.fetchUser();

    // ✅ 로그인 성공 시 항상 홈으로 (설문 강제 진입 방지)
    console.log('[App] Successful login from URL. Redirecting to Home...');
    router.push('/');
  } else if (authStore.token) {
    // 2. 이미 토큰이 있는 경우 유저 정보 가져오기
    await authStore.fetchUser();
  }
})

// 다크모드 제거로 인해 관련 provide도 제거됨

// 전역 상태 제공
// provide('isDarkMode', isDarkMode) // 삭제
// provide('toggleDarkMode', toggleDarkMode) // 삭제

// userProfile과 authStore는 다른 컴포넌트에서 사용할 수 있으므로 유지 또는 필요 시 제거 고려 
// (Navbar로 prop 전달하고 있지만, 깊은 곳에서 inject로 쓸 수도 있음)
// 여기서는 Navbar에 prop으로 넘기므로, provide는 유지하되 다크모드만 제거
// provide('userProfile', userProfile)
// provide('authStore', authStore)
</script>

<template>
  <Navbar :userProfile="userProfile" />
  <RouterView />
</template>

<style>
</style>