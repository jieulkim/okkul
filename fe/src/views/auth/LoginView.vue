<script setup>
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter() // router 사용

const handleLogin = () => {
  authStore.login()
}

const isDevMode = import.meta.env.VITE_USE_MOCK_DATA === 'true'

const handleDevLogin = () => {
  if (confirm('개발자 모드로 로그인하시겠습니까?\n실제 인증 절차를 건너뛰고 가짜 데이터로 로그인합니다.')) {
    authStore.loginAsDev()
  }
}
</script>

<template>
  <div class="login-container">
    <div class="login-card fade-in">
      <div class="logo-section">
        <div class="okkul-bubble">
          <img src="/okkul.svg" alt="Okkul" class="okkul-img" />
        </div>
        <h1>오꿀</h1>
        <p>꿀처럼 달콤한 오픽 점수,<br>AI와 함께 만들어보세요!</p>
      </div>

      <div class="login-actions">
        <button @click="handleLogin" class="google-login-btn">
          <img src="https://upload.wikimedia.org/wikipedia/commons/c/c1/Google_%22G%22_logo.svg" alt="Google" width="20" />
          구글 계정으로 시작하기
        </button>

        <!-- 개발자 모드 전용 버튼 -->
        <button 
          v-if="isDevMode"
          @click="handleDevLogin" 
          class="dev-login-btn"
        >
          <span class="material-icons-outlined text-sm">code</span>
          개발자 로그인 (Mock Mode)
        </button>
      </div>

      <div class="footer-note">
        <p>로그인 시 서비스 이용약관 및 개인정보 처리방침에 동의하게 됩니다.</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: var(--bg-color);
  /* Optional: Centered Pattern */
  background-image: radial-gradient(circle at 50% 50%, var(--honey-50) 0%, transparent 70%);
}

.login-card {
  width: 100%;
  max-width: 420px;
  background: var(--card-bg);
  backdrop-filter: blur(20px);
  border: var(--card-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--card-shadow);
  padding: 48px 40px;
  text-align: center;
  transition: transform 0.3s;
}

.logo-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 32px;
}

.okkul-bubble {
  width: 90px;
  height: 90px;
  background: var(--honey-100);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  box-shadow: 0 4px 12px rgba(255, 235, 59, 0.3);
}

.okkul-img {
  width: 50px;
  height: auto;
}

.logo-section h1 {
  font-size: 2rem;
  font-weight: 800;
  color: var(--text-main);
  margin-bottom: 8px;
}

.logo-section p {
  font-size: 1rem;
  color: var(--text-sub);
  line-height: 1.5;
}

.login-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}

.google-login-btn {
  width: 100%;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  background: #FFFFFF;
  border: 1px solid #E0E0E0;
  border-radius: var(--radius-full);
  font-size: 1rem;
  font-weight: 600;
  color: #1F1F1F;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.google-login-btn:hover {
  background: #F8F9FA;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  transform: translateY(-1px);
}

.dev-login-btn {
  width: 100%;
  padding: 12px;
  background: transparent;
  border: 1px dashed #BDBDBD;
  border-radius: var(--radius-full);
  font-size: 0.85rem;
  color: var(--text-tertiary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: all 0.2s;
}

.dev-login-btn:hover {
  border-color: var(--text-sub);
  color: var(--text-sub);
  background: rgba(0,0,0,0.02);
}

.footer-note {
  margin-top: 32px;
}

.footer-note p {
  font-size: 0.75rem;
  color: var(--text-tertiary);
}

.text-sm {
  font-size: 18px;
}

/* Fade In Animation */
.fade-in {
  animation: fadeIn 0.5s ease-out forwards;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
