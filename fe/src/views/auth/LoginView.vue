<script setup>
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

const handleLogin = () => {
  authStore.login()
}
</script>

<template>
  <div class="login-container">
    <div class="login-card">
      <div class="logo-section">
        <span class="logo-icon">🍯</span>
        <h1>오꿀</h1>
        <p>오늘의 꿀같은 오픽 점수, 오꿀과 함께 만드세요!</p>
      </div>

      <div class="login-actions">
        <button @click="handleLogin" class="google-login-btn">
          <img src="https://upload.wikimedia.org/wikipedia/commons/c/c1/Google_%22G%22_logo.svg" alt="Google" />
          구글로 시작하기
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
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
  padding: 20px;
}

.login-card {
  background: white;
  padding: 48px;
  border-radius: 32px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  max-width: 440px;
  width: 100%;
  text-align: center;
}

.logo-section .logo-icon {
  font-size: 64px;
  display: block;
  margin-bottom: 16px;
}

.logo-section h1 {
  font-size: 36px;
  font-weight: 900;
  color: #1e293b;
  margin-bottom: 12px;
}

.logo-section p {
  color: #64748b;
  line-height: 1.6;
  margin-bottom: 40px;
}

.google-login-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 16px;
  background: white;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
  cursor: pointer;
  transition: all 0.2s;
}

.google-login-btn:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
  transform: translateY(-2px);
}

.google-login-btn img {
  width: 24px;
  height: 24px;
}

.footer-note {
  margin-top: 32px;
}

.footer-note p {
  font-size: 13px;
  color: #94a3b8;
}
</style>
