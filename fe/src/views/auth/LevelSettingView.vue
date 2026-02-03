<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { usersApi } from '@/api'

const router = useRouter()
const authStore = useAuthStore()

const targetLevel = ref('INTERMEDIATE_HIGH')
const nickname = ref(authStore.user?.nickname || authStore.user?.name || '')
const isSubmitting = ref(false)

const levelOptions = [
  { value: 'ADVANCED_LOW', label: 'AL', desc: 'Advanced Low' },
  { value: 'INTERMEDIATE_HIGH', label: 'IH', desc: 'Intermediate High' },
  { value: 'INTERMEDIATE_MID_3', label: 'IM3', desc: 'Intermediate Mid 3' },
  { value: 'INTERMEDIATE_MID_2', label: 'IM2', desc: 'Intermediate Mid 2' },
  { value: 'INTERMEDIATE_MID_1', label: 'IM1', desc: 'Intermediate Mid 1' },
  { value: 'INTERMEDIATE_LOW', label: 'IL', desc: 'Intermediate Low' }
]

const saveLevel = async () => {
  if (!nickname.value.trim()) {
    alert('닉네임을 입력해 주세요.')
    return
  }

  try {
    isSubmitting.value = true
    
    // 닉네임과 목표 레벨을 각각 업데이트 (병렬 실행)
    await Promise.all([
      usersApi.updateNickname({ nickname: nickname.value.trim() }),
      usersApi.updateTargetLevel({ targetLevel: targetLevel.value })
    ])
    
    // 유저 정보 갱신
    await authStore.fetchUser()
    
    // 완료 후 홈으로 이동
    router.push('/')
  } catch (error) {
    console.error('설정 저장 실패:', error)
    alert('정보 저장에 실패했습니다. 다시 시도해 주세요.')
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <div class="page-container level-setting-page">
    <div class="level-card fade-in">
      <header class="level-header">
        <h2 class="welcome-text">환영합니다!</h2>
        <p class="subtitle">오꿀쌤과 함께할 <span class="highlight">닉네임</span>과<br><span class="highlight">목표 등급</span>을 설정해 주세요.</p>
      </header>

      <div class="onboarding-form">
        <div class="input-section">
          <label for="nickname" class="input-label">사용하실 닉네임</label>
          <div class="input-wrapper">
            <span class="material-icons input-icon">face</span>
            <input 
              id="nickname"
              v-model="nickname" 
              type="text" 
              placeholder="닉네임을 입력하세요" 
              maxlength="10"
              class="nickname-input"
            />
          </div>
        </div>

        <div class="level-section-wrapper">
          <label class="input-label">목표 등급 선택</label>
          <div class="level-selection">
            <div 
              v-for="option in levelOptions" 
              :key="option.value"
              class="level-option"
              :class="{ active: targetLevel === option.value }"
              @click="targetLevel = option.value"
            >
              <div class="level-label">{{ option.label }}</div>
              <div class="level-desc">{{ option.desc }}</div>
              <div class="check-icon" v-if="targetLevel === option.value">
                <span class="material-icons">check_circle</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <footer class="level-footer">
        <button 
          @click="saveLevel" 
          class="start-btn"
          :disabled="isSubmitting"
        >
          <span v-if="isSubmitting" class="spinner"></span>
          오꿀 시작하기
        </button>
        <p class="notice-text">정보는 나중에 마이페이지에서 다시 변경할 수 있습니다.</p>
      </footer>
    </div>
  </div>
</template>

<style scoped>
.page-container {
  height: calc(100vh - var(--header-height, 70px));
  min-height: 0 !important;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--bg-color);
  overflow: hidden;
  padding: 0;
}

.level-card {
  background: white;
  border: 1px solid var(--border-primary, #f1f5f9);
  border-radius: 32px;
  padding: 32px 48px;
  width: 100%;
  max-width: 860px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.05);
  text-align: center;
  margin-top: -2vh;
}

.level-header {
  margin-bottom: 24px;
}

.welcome-text {
  font-size: 1.8rem;
  font-weight: 800;
  margin-bottom: 8px;
  color: var(--text-primary);
}

.subtitle {
  font-size: 1rem;
  color: var(--text-secondary);
  line-height: 1.5;
}

.highlight {
  color: #F9A825;
  font-weight: 700;
  position: relative;
  display: inline-block;
  z-index: 1;
}

.highlight::after {
  content: "";
  position: absolute;
  bottom: 2px;
  left: 0;
  width: 100%;
  height: 8px;
  background: var(--honey-100);
  z-index: -1;
  opacity: 0.8;
}

/* Onboarding Form */
.onboarding-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 24px;
  text-align: left;
}

.input-label {
  display: block;
  font-size: 1rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 12px;
  padding-left: 4px;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 16px;
  color: var(--text-tertiary);
  font-size: 20px;
}

.nickname-input {
  width: 100%;
  padding: 12px 16px 12px 44px;
  background: #F8FAFC;
  border: 2px solid #F1F5F9;
  border-radius: 16px;
  font-size: 1rem;
  font-weight: 600;
  color: var(--text-primary);
  transition: all 0.3s;
}

.nickname-input:focus {
  outline: none;
  background: #FFF;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 4px rgba(254, 215, 0, 0.1);
}

.level-selection {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.level-option {
  padding: 14px 10px;
  background: #fff;
  border: 1px solid #F1F5F9;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.level-option:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.04);
  border-color: var(--honey-200);
}

.level-option.active {
  border-color: var(--primary-color);
  background: var(--honey-50, #FFFDE7);
  box-shadow: 0 12px 24px rgba(251, 191, 36, 0.15);
}

.level-label {
  font-size: 1.25rem;
  font-weight: 800;
  color: var(--text-primary);
}

.level-desc {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--text-tertiary);
}

.check-icon {
  position: absolute;
  top: 8px;
  right: 8px;
  color: var(--primary-color);
  font-size: 18px;
}

.start-btn {
  width: 100%;
  max-width: 320px;
  margin: 0 auto;
  padding: 14px;
  background: var(--primary-color);
  border: none;
  border-radius: 16px;
  font-size: 1.1rem;
  font-weight: 800;
  color: #212529;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 8px 20px rgba(254, 215, 0, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.start-btn:hover:not(:disabled) {
  background: var(--primary-hover);
  transform: translateY(-2px);
  box-shadow: 0 12px 30px rgba(254, 215, 0, 0.3);
}

.start-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.notice-text {
  margin-top: 16px;
  font-size: 0.8rem;
  color: var(--text-tertiary);
}

.spinner {
  width: 20px;
  height: 20px;
  border: 3px solid rgba(0,0,0,0.1);
  border-top-color: #212529;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

/* Animations */
.fade-in {
  animation: fadeIn 0.6s ease-out forwards;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .level-selection {
    grid-template-columns: repeat(2, 1fr);
  }
  .level-card {
    padding: 40px 30px;
  }
}
</style>
