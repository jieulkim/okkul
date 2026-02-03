<script setup>
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useSurveyStore } from '@/stores/survey'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const surveyStore = useSurveyStore()

onMounted(async () => {
  console.log('[OAuth2Redirect] Full Query Params:', route.query)
  const accessToken = route.query.accessToken
  const refreshToken = route.query.refreshToken

  if (accessToken) {
    console.log('[OAuth2Redirect] Token received. Saving to localStorage...')
    // 1. 토큰 저장
    localStorage.setItem('accessToken', accessToken)
    if (refreshToken) localStorage.setItem('refreshToken', refreshToken)
    
    // 2. 로그인 성공 시 이전 삭제 기록(deletedSurveyIds) 초기화
    if (typeof surveyStore.resetDeletedList === 'function') {
      surveyStore.resetDeletedList()
    } else {
      localStorage.removeItem('deletedSurveyIds')
    }

    // 3. 유저 정보 가져오기
    console.log('[OAuth2Redirect] Fetching user info...')
    await authStore.fetchUser()

    // 4. 신규 유저 여부 판단 (Target Level이 없으면 설정 페이지로)
    if (authStore.user && !authStore.user.targetLevel) {
      console.log('[OAuth2Redirect] New user detected. Redirecting to Goal Setting...')
      router.push('/auth/goal')
    } else {
      // 5. 기존 유저인 경우 홈으로
      console.log('[OAuth2Redirect] Exiting user. Redirecting to Home...')
      router.push('/')
    }
  } else {
    // 토큰이 없는 경우 로그인 페이지로
    console.error('[OAuth2Redirect] Authentication failed: No access token received')
    router.push('/login')
  }
})
</script>

<template>
  <div class="redirect-container">
    <div class="loading-content">
      <div class="honey-loader">
        <div class="honey-drop"></div>
        <div class="honey-drop"></div>
        <div class="honey-drop"></div>
      </div>
      <h2>로그인 중입니다...</h2>
      <p>잠시만 기다려주세요. 오꿀이 준비되고 있어요!</p>
    </div>
  </div>
</template>

<style scoped>
.redirect-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-primary);
  color: var(--text-primary);
}

.loading-content {
  text-align: center;
}

.loading-content h2 {
  font-size: 24px;
  font-weight: 800;
  margin-top: 24px;
  color: var(--text-primary);
}

.loading-content p {
  color: var(--text-secondary);
  margin-top: 8px;
}

/* Honey Loader Animation */
.honey-loader {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.honey-drop {
  width: 16px;
  height: 16px;
  background: var(--primary-color);
  border-radius: 50% 50% 50% 0;
  transform: rotate(45deg);
  animation: drip 1.2s ease-in-out infinite;
}

.honey-drop:nth-child(2) { animation-delay: 0.2s; }
.honey-drop:nth-child(3) { animation-delay: 0.4s; }

@keyframes drip {
  0%, 100% { transform: translateY(0) rotate(45deg) scale(1); }
  50% { transform: translateY(15px) rotate(45deg) scale(1.1); }
}

.dark-mode .honey-drop {
  border: 1px solid #FFFFFF;
}
</style>