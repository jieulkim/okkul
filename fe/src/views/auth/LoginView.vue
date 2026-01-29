<script setup>
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

const handleLogin = () => {
  authStore.login()
}

const isDevMode = import.meta.env.VITE_USE_MOCK_DATA === 'true'

const handleDevLogin = () => {
  if (confirm('개발자 모드로 로그인하시겠습니까?\n실제 인증 절차를 건너뛰고 가짜 데이터로 로그인합니다.')) {
    authStore.loginAsDev()
  }
}

// 개발자 테스트용 함수
import { useRouter } from 'vue-router';
const router = useRouter();

const startDevTest = () => {
  const dummyExamId = 999;
  const dummyData = {
    examId: dummyExamId,
    currentIndex: 6, // 7번 문제 (index 6)부터 시작
    questions: Array.from({ length: 7 }, (_, i) => ({
      id: i + 1,
      answerId: 500 + i,
      order: i + 1,
      questionText: `테스트 문제 ${i + 1}`,
      audioUrl: null,
      type: 'SPEAKING',
      preparationTime: 10,
      speakingTime: 30
    })),
    totalQuestions: 15
  };
  
  localStorage.setItem(`exam_${dummyExamId}`, JSON.stringify(dummyData));
  router.push(`/exam/question?examId=${dummyExamId}`);
};
</script>

<template>
  <div class="login-container">
    <div class="login-card">
      <div class="logo-section">
        <div class="okkul-wrapper">
          <img src="/okkul.svg" alt="Okkul" class="okkul-img" />
        </div>
        <h1>오꿀</h1>
        <p>오늘의 꿀같은 오픽 점수, 오꿀과 함께 만드세요!</p>
      </div>

      <div class="login-actions">
        <button @click="handleLogin" class="google-login-btn">
          <img src="https://upload.wikimedia.org/wikipedia/commons/c/c1/Google_%22G%22_logo.svg" alt="Google" />
          구글로 시작하기
        </button>

        <!-- 개발자 모드 전용 버튼 -->
        <button 
          v-if="isDevMode"
          @click="handleDevLogin" 
          class="dev-login-btn"
        >
          <span class="material-icons">code</span>
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
  background: var(--bg-primary);
  padding: 24px;
}

.login-card {
  background: var(--bg-secondary);
  border: 2px solid var(--border-primary);
  padding: 48px;
  border-radius: 32px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.05);
  max-width: 440px;
  width: 100%;
  text-align: center;
}

.okkul-wrapper {
  margin-bottom: 24px;
  display: flex;
  justify-content: center;
}

.logo-section .logo-icon {
  font-size: 64px;
  display: block;
  margin-bottom: 16px;
}

.logo-section h1 {
  font-size: 36px;
  font-weight: 900;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.logo-section p {
  color: var(--text-secondary);
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

.dev-login-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 16px;
  margin-top: 12px;
  background: #334155;
  border: 2px solid #334155;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 700;
  color: white;
  cursor: pointer;
  transition: all 0.2s;
}

.dev-login-btn:hover {
  background: #475569;
  border-color: #475569;
  transform: translateY(-2px);
}
</style>
