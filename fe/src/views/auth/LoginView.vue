<script setup>
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

const handleGoogleLogin = () => {
  authStore.login()
}
</script>

<template>
  <div class="login-container">
    <div class="login-card">
      <div class="logo-section">
        <span class="logo-icon">🍯</span>
        <h1 class="logo-text">오꿀</h1>
        <p class="logo-sub">오늘의 꿀팁, 오픽 꿀잼</p>
      </div>

      <div class="login-content">
        <h2>환영합니다!</h2>
        <p class="login-desc">가장 쉽고 빠른 오픽 준비,<br>오꿀과 함께 시작해보세요.</p>
        
        <button class="google-login-btn" @click="handleGoogleLogin">
          <img src="https://upload.wikimedia.org/wikipedia/commons/c/c1/Google_%22G%22_logo.svg" alt="Google" class="google-icon" />
          <span>Google로 시작하기</span>
        </button>
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
  background: linear-gradient(135deg, #fffbeb 0%, #fff7ed 100%);
  padding: 20px;
}

.dark-mode .login-container {
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
}

.login-card {
  width: 100%;
  max-width: 400px;
  background: white;
  border-radius: 24px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  text-align: center;
  border: 1px solid #f1f5f9;
}

.dark-mode .login-card {
  background: #1e293b;
  border-color: #334155;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
}

.logo-section {
  padding: 48px 24px 32px;
  background: linear-gradient(135deg, #fffbeb 0%, #fff 100%);
  border-bottom: 1px solid #f1f5f9;
}

.dark-mode .logo-section {
  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  border-color: #334155;
}

.logo-icon {
  font-size: 64px;
  display: block;
  margin-bottom: 16px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.logo-text {
  font-size: 32px;
  font-weight: 900;
  color: #FFD700;
  margin: 0;
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.logo-sub {
  color: #64748b;
  font-size: 14px;
  margin-top: 8px;
  font-weight: 500;
}

.dark-mode .logo-sub {
  color: #94a3b8;
}

.login-content {
  padding: 40px 32px;
}

.login-content h2 {
  font-size: 24px;
  font-weight: 800;
  color: #1e293b;
  margin-bottom: 12px;
}

.dark-mode .login-content h2 {
  color: #f1f5f9;
}

.login-desc {
  color: #64748b;
  line-height: 1.6;
  margin-bottom: 32px;
}

.dark-mode .login-desc {
  color: #94a3b8;
}

.google-login-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 16px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
}

.dark-mode .google-login-btn {
  background: #334155;
  border-color: #475569;
  color: #f1f5f9;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.2);
}

.google-login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.05);
  border-color: #cbd5e1;
}

.dark-mode .google-login-btn:hover {
  background: #475569;
  border-color: #64748b;
}

.google-icon {
  width: 24px;
  height: 24px;
}
</style>
