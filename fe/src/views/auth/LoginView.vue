<script setup>
import { inject, ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const isDarkMode = inject('isDarkMode', ref(false))

const handleGoogleLogin = () => {
  authStore.login()
}

// Background animation elements
const beePositions = ref([
  { top: '15%', left: '10%', delay: '0s' },
  { top: '65%', left: '85%', delay: '1s' },
  { top: '10%', left: '80%', delay: '2s' },
  { top: '80%', left: '15%', delay: '1.5s' }
])
</script>

<template>
  <div class="login-page" :class="{ 'dark-mode': isDarkMode }">
    <!-- Animated Background Elements -->
    <div class="bg-elements">
      <div v-for="(bee, idx) in beePositions" :key="idx" 
           class="floating-bee" 
           :style="{ top: bee.top, left: bee.left, animationDelay: bee.delay }">
        🐝
      </div>
      <div class="honeycomb-pattern"></div>
    </div>

    <div class="login-container">
      <div class="login-card">
        <div class="glass-effect"></div>
        
        <header class="login-header">
          <div class="logo-wrapper">
            <span class="logo-emoji">🍯</span>
            <div class="logo-glow"></div>
          </div>
          <h1 class="brand-name">오꿀</h1>
          <p class="brand-tagline">오늘의 꿀팁, 오픽 꿀잼</p>
        </header>

        <main class="login-body">
          <div class="welcome-text">
            <h2>다시 오신 것을 환영해요!</h2>
            <p>당신의 오픽 목표 달성,<br>오꿀이 가장 빠른 길을 안내해 드릴게요.</p>
          </div>

          <div class="auth-actions">
            <button class="google-login-button" @click="handleGoogleLogin">
              <div class="button-content">
                <img src="https://upload.wikimedia.org/wikipedia/commons/c/c1/Google_%22G%22_logo.svg" alt="Google" class="google-logo" />
                <span>Google 계정으로 계속하기</span>
              </div>
              <div class="button-hover-effect"></div>
            </button>
            
            <p class="terms-text">
              로그인 시 오꿀의 <a href="#">이용약관</a> 및 <a href="#">개인정보 처리방침</a>에 동의하게 됩니다.
            </p>
          </div>
        </main>

        <footer class="login-footer">
          <div class="step-indicators">
            <span class="step-dot active"></span>
            <span class="step-dot"></span>
            <span class="step-dot"></span>
          </div>
          <p class="footer-note">간편하게 로그인하고 당신만의 학습 리포트를 확인하세요.</p>
        </footer>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;600;700;800&display=swap');

.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fffbeb;
  font-family: 'Plus Jakarta Sans', -apple-system, sans-serif;
  overflow: hidden;
  position: relative;
}

.login-page.dark-mode {
  background: #0f172a;
}

/* Background Elements */
.bg-elements {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 1;
}

.floating-bee {
  position: absolute;
  font-size: 32px;
  filter: drop-shadow(0 10px 15px rgba(255, 215, 0, 0.3));
  animation: bee-float 6s ease-in-out infinite;
  opacity: 0.6;
}

@keyframes bee-float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  33% { transform: translateY(-30px) rotate(15deg); }
  66% { transform: translateY(10px) rotate(-10deg); }
}

.honeycomb-pattern {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(#FFD700 1px, transparent 1px);
  background-size: 30px 30px;
  opacity: 0.1;
}

.dark-mode .honeycomb-pattern {
  background-image: radial-gradient(#FFD700 0.5px, transparent 0.5px);
  opacity: 0.05;
}

/* Login Card Container */
.login-container {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 440px;
  padding: 20px;
}

.login-card {
  position: relative;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  border-radius: 32px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 40px 100px rgba(0, 0, 0, 0.08);
  padding: 48px 32px;
  text-align: center;
  overflow: hidden;
}

.dark-mode .login-card {
  background: rgba(30, 41, 59, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.05);
  box-shadow: 0 40px 100px rgba(0, 0, 0, 0.4);
}

.glass-effect {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at top left, rgba(255, 215, 0, 0.1), transparent 70%);
  pointer-events: none;
}

/* Header Section */
.login-header {
  margin-bottom: 40px;
}

.logo-wrapper {
  position: relative;
  width: 80px;
  height: 80px;
  margin: 0 auto 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-emoji {
  font-size: 56px;
  z-index: 2;
  animation: logo-pop 4s cubic-bezier(0.68, -0.6, 0.32, 1.6) infinite;
}

@keyframes logo-pop {
  0%, 100% { transform: scale(1) translateY(0); }
  50% { transform: scale(1.1) translateY(-10px); }
}

.logo-glow {
  position: absolute;
  width: 100%;
  height: 100%;
  background: #FFD700;
  border-radius: 50%;
  filter: blur(40px);
  opacity: 0.3;
  z-index: 1;
}

.brand-name {
  font-size: 32px;
  font-weight: 800;
  letter-spacing: -1px;
  margin-bottom: 8px;
  color: #1e293b;
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}

.brand-tagline {
  font-size: 15px;
  color: #64748b;
  font-weight: 500;
}

.dark-mode .brand-tagline {
  color: #94a3b8;
}

/* Body Section */
.welcome-text {
  margin-bottom: 40px;
}

.welcome-text h2 {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 12px;
}

.dark-mode .welcome-text h2 {
  color: #f1f5f9;
}

.welcome-text p {
  font-size: 14px;
  color: #64748b;
  line-height: 1.6;
}

.dark-mode .welcome-text p {
  color: #94a3b8;
}

/* Auth Actions */
.google-login-button {
  width: 100%;
  position: relative;
  padding: 16px 24px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.02);
}

.dark-mode .google-login-button {
  background: #334155;
  border-color: #475569;
}

.button-content {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  gap: 12px;
  color: #1e293b;
  font-weight: 600;
  font-size: 15px;
}

.dark-mode .button-content {
  color: #f1f5f9;
}

.google-logo {
  width: 20px;
  height: 20px;
}

.button-hover-effect {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(255, 215, 0, 0.1) 0%, transparent 100%);
  opacity: 0;
  transition: opacity 0.3s;
}

.google-login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.05);
  border-color: #FFD700;
}

.google-login-button:hover .button-hover-effect {
  opacity: 1;
}

.terms-text {
  margin-top: 24px;
  font-size: 12px;
  color: #94a3b8;
  line-height: 1.6;
}

.terms-text a {
  color: #64748b;
  text-decoration: underline;
  text-underline-offset: 2px;
}

.dark-mode .terms-text a {
  color: #cbd5e1;
}

/* Footer Section */
.login-footer {
  margin-top: 48px;
}

.step-indicators {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-bottom: 16px;
}

.step-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #e2e8f0;
}

.step-dot.active {
  width: 20px;
  border-radius: 3px;
  background: #FFD700;
}

.dark-mode .step-dot {
  background: #475569;
}

.dark-mode .step-dot.active {
  background: #FFD700;
}

.footer-note {
  font-size: 12px;
  color: #64748b;
}

.dark-mode .footer-note {
  color: #94a3b8;
}

@media (max-width: 480px) {
  .login-card {
    padding: 32px 24px;
    border-radius: 24px;
  }
}
</style>
