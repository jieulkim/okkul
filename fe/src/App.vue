<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterView, useRouter } from 'vue-router'
import Navbar from '@/components/common/Navbar.vue'
import FeedbackFAB from '@/components/common/FeedbackFAB.vue'
import FeedbackForm from '@/components/common/FeedbackForm.vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

// 피드백 폼 표시 여부 상태
const isFeedbackFormVisible = ref(false)

// 피드백 폼 토글 함수
const toggleFeedbackForm = () => {
  isFeedbackFormVisible.value = !isFeedbackFormVisible.value
}

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

    // ✅ 신규 유저 체크 (Target Level이 없으면 설정 페이지로)
    if (authStore.user && !authStore.user.targetLevel) {
      console.log('[App] New user detected. Redirecting to Level Setting...');
      router.push('/auth/level');
    } else {
      console.log('[App] Successful login from URL. Redirecting to Home...');
      router.push('/');
    }
  }
})
</script>

<template>
  <Navbar :userProfile="userProfile" />
  <RouterView />
  
  <FeedbackFAB @toggle="toggleFeedbackForm" />

  <Transition name="fade">
    <div
      v-if="isFeedbackFormVisible"
      class="feedback-overlay"
      @click.self="toggleFeedbackForm"
    >
      <Transition name="slide-up">
        <div v-if="isFeedbackFormVisible" class="feedback-form-wrapper">
          <FeedbackForm @close="toggleFeedbackForm" />
        </div>
      </Transition>
    </div>
  </Transition>
</template>

<style>
.feedback-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.feedback-form-wrapper {
  z-index: 1001;
}

/* Transition 'fade' for overlay */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* Transition 'slide-up' for form */
.slide-up-enter-active,
.slide-up-leave-active {
  transition: transform 0.4s var(--ease-spring), opacity 0.3s ease;
}
.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(50px);
}
</style>
