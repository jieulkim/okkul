<script setup>
import { ref, watch, provide, computed, onMounted } from 'vue'
import { RouterView, useRouter } from 'vue-router'
import Navbar from '@/components/common/Navbar.vue'
import { useAuthStore } from '@/stores/auth'

// 1. 다크모드 상태 관리
const isDarkMode = ref(localStorage.getItem('theme') === 'dark')

// 2. 다크모드 토글 함수
const toggleDarkMode = () => {
  isDarkMode.value = !isDarkMode.value
}

// 3. 상태 변화에 따른 실제 클래스 적용 및 저장
watch(isDarkMode, (val) => {
  if (val) {
    document.documentElement.classList.add('dark-mode')
    localStorage.setItem('theme', 'dark')
  } else {
    document.documentElement.classList.remove('dark-mode')
    localStorage.setItem('theme', 'light')
  }
}, { immediate: true })

// 전역 상태 제공
provide('isDarkMode', isDarkMode)
provide('toggleDarkMode', toggleDarkMode)

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

provide('userProfile', userProfile)
provide('authStore', authStore)
</script>

<template>
  <Navbar :userProfile="userProfile" />
  <RouterView />
</template>

<style>
</style>