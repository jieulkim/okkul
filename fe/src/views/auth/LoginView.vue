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
  border: var(--border-primary);
  padding: 56px 48px;
  border-radius: 32px;
  box-shadow: var(--shadow-xl);
  max-width: 440px;
  width: 100%;
  text-align: center;
}

.okkul-wrapper {
  margin-bottom: 32px;
  display: flex;
  justify-content: center;
}

.okkul-img {
  width: 80px;
  height: 80px;
}

.logo-section h1 {
  font-size: 2.5rem;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.logo-section p {
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 48px;
  font-size: 1rem;
}

.login-actions {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.google-login-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 18px;
  background: var(--primary-color);
  border: none;
  border-radius: 16px;
  font-size: 1rem;
  font-weight: 700;
  color: #212529;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: var(--shadow-md);
}

.google-login-btn:hover {
  background: var(--primary-hover);
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.google-login-btn img {
  width: 24px;
  height: 24px;
}

.dev-login-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 16px;
  background: var(--bg-tertiary);
  border: 1px solid var(--border-primary);
  border-radius: 14px;
  font-size: 0.95rem;
  font-weight: 600;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.2s;
}

.dev-login-btn:hover {
  background: #e2e8f0;
  color: var(--text-primary);
  transform: translateY(-2px);
}

.footer-note {
  margin-top: 40px;
}

.footer-note p {
  font-size: 0.8125rem;
  color: var(--text-tertiary);
  line-height: 1.5;
}
</style>
