<script setup>
import { ref, provide, watch, onMounted } from 'vue'
import { RouterView } from 'vue-router'
import Navbar from '@/components/common/Navbar.vue'

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

// 전역 사용자 상태
const userProfile = ref({
  name: '홍길동',
  nickname: '오꿀이',
  profileImage: null,
  currentLevel: 'IH',
  targetLevel: 'AL',
  examRemaining: 20
})
provide('userProfile', userProfile)
</script>

<template>
  <Navbar :userProfile="userProfile" />
  <RouterView />
</template>

<style>
:root {
  --bg-primary: #ffffff;
  --text-primary: #1e293b;
}

/* documentElement(html) 또는 body에 적용될 클래스 */
.dark-mode {
  --bg-primary: #1e293b;
  --text-primary: #f1f5f9;
  background-color: #0f172a !important;
}

body {
  margin: 0;
  padding: 0;
  font-family: 'Noto Sans KR', sans-serif;
  background-color: var(--bg-primary);
  color: var(--text-primary);
  transition: background 0.3s ease, color 0.3s ease;
}
</style>