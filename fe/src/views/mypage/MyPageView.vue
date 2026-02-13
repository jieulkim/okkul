<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { usersApi, historyApi } from '@/api'
import defaultProfile from '@/assets/images/default-profile.png'

const router = useRouter()
const authStore = useAuthStore()

const userName = computed(() => {
  return authStore.user?.nickname || authStore.user?.name || '사용자'
})

// 프로필 기본 이미지 : 오꿀
const displayAvatar = computed(() => {
  const url = authStore.user?.profileImageUrl
  // URL이 없거나 구글 기본 이미지 경로인 경우 기본 오꿀 이미지 반환
  if (!url || url.includes('googleusercontent.com')) {
    return defaultProfile
  }
  return url
})

// 프로필 편집
const isEditing = ref(false)
const editForm = ref({
  nickname: '',
  targetLevel: ''
})

const fileInput = ref(null)
const isUploading = ref(false)

function triggerFileInput() {
  if (isEditing.value) {
    fileInput.value.click()
  }
}

async function handleImageUpload(event) {
  const file = event.target.files[0]
  if (!file) return

  try {
    isUploading.value = true
    await usersApi.updateProfileImage({ file })
    
    // 유저 정보 갱신
    const response = await usersApi.getMyInfo()
    if (response.data) {
      authStore.updateUser(response.data)
      if (localStorage.getItem('user')) {
        localStorage.setItem('user', JSON.stringify(response.data))
      }
    }
    alert('프로필 이미지가 변경되었습니다.')
  } catch (error) {
    console.error('이미지 업로드 실패:', error)
    alert('이미지 업로드에 실패했습니다.')
  } finally {
    isUploading.value = false
    // 파일 입력 초기화 (같은 파일 다시 선택 가능하도록)
    event.target.value = ''
  }
}

const levelOptions = [
  { value: 'ADVANCED_LOW', label: 'AL (Advanced Low)' },
  { value: 'INTERMEDIATE_HIGH', label: 'IH (Intermediate High)' },
  { value: 'INTERMEDIATE_MID_3', label: 'IM3 (Intermediate Mid 3)' },
  { value: 'INTERMEDIATE_MID_2', label: 'IM2 (Intermediate Mid 2)' },
  { value: 'INTERMEDIATE_MID_1', label: 'IM1 (Intermediate Mid 1)' },
  { value: 'INTERMEDIATE_LOW', label: 'IL (Intermediate Low)' }
]

function startEdit() {
  editForm.value = {
    nickname: '', // placeholder를 보여주기 위해 빈칸으로 초기화
    targetLevel: authStore.user?.targetLevel || 'INTERMEDIATE_HIGH'
  }
  isEditing.value = true
}

function cancelEdit() {
  isEditing.value = false
}

async function saveProfile() {
  try {
    // 닉네임이 빈칸이면 기존 닉네임 유지
    const finalNickname = editForm.value.nickname.trim() || authStore.user?.nickname || authStore.user?.name;
    
    if (finalNickname && finalNickname !== authStore.user?.nickname) {
      await usersApi.updateNickname({ nickname: finalNickname })
    }
    
    if (editForm.value.targetLevel !== authStore.user?.targetLevel) {
      await usersApi.updateTargetLevel({ targetLevel: editForm.value.targetLevel })
    }
    
    // 최신 사용자 정보 조회
    const response = await usersApi.getMyInfo()
    
    // authStore 업데이트
    if (response.data) {
      if (localStorage.getItem('user')) {
        localStorage.setItem('user', JSON.stringify(response.data))
      }
      authStore.updateUser(response.data)
      console.log('[MyPageView] 프로필 업데이트 완료:', authStore.user)
    }
    
    isEditing.value = false
    alert('프로필이 저장되었습니다.')
  } catch (error) {
    console.error('프로필 저장 실패:', error)
    alert('프로필 저장에 실패했습니다.')
    isEditing.value = false
  }
}

// 모의고사 내역
const examHistory = ref([])
const isLoadingExams = ref(false)

async function loadExamHistory() {
  try {
    isLoadingExams.value = true
    const { data } = await historyApi.getExamHistories({ size: 20, sort: ['createdAt,desc'] }) // 통계 계산을 위해 사이즈 늘림
    
    const total = data.page?.totalElements || data.content?.length || 0;
    
    examHistory.value = data.content?.map((exam, index) => {
      const difficulty = exam.adjustedDifficulty || exam.initialDifficulty || 1;
      const date = new Date(exam.createdAt).toLocaleDateString('ko-KR', { 
        year: 'numeric', 
        month: '2-digit', 
        day: '2-digit' 
      }).replace(/\. /g, '.').replace(/\.$/, '');
      
      return {
        examId: exam.examId,
        num: total - index,
        title: `난이도 ${difficulty} 모의고사 (${date})`,
        createdAt: exam.createdAt,
        grade: exam.grade || '채점 중'
      };
    }) || []
  } catch (error) {
    console.error('시험 내역 로드 실패:', error)
    examHistory.value = []
  } finally {
    isLoadingExams.value = false
  }
}

// 유형 연습 내역
const practiceHistory = ref([])
const isLoadingPractice = ref(false)

async function loadPracticeHistory() {
  try {
    isLoadingPractice.value = true
    const { data } = await historyApi.getPracticeHistories({ size: 20, sort: ['startedAt,desc'] }) // 통계 계산을 위해 사이즈 늘림
    
    practiceHistory.value = data.content?.map(practice => ({
      practiceId: practice.practiceId,
      questionId: null, 
      typeName: practice.typeName,
      topicName: practice.topic || '토픽 없음',
      title: `${practice.topic || '토픽 없음'} 연습`,
      createdAt: practice.startedAt,
      status: 'COMPLETED'
    })) || []
  } catch (error) {
    console.error('연습 내역 로드 실패:', error)
    practiceHistory.value = []
  } finally {
    isLoadingPractice.value = false
  }
}

// 학습 통계 (실제 데이터 기반 계산)
const learningStats = computed(() => {
  // 중복 없는 학습 일수 계산
  const allDates = [
    ...examHistory.value.map(e => e.createdAt),
    ...practiceHistory.value.map(p => p.createdAt)
  ].filter(Boolean);

  const uniqueDays = new Set(allDates.map(d => new Date(d).toDateString()));

  return {
    totalExams: examHistory.value.length,
    totalPractice: practiceHistory.value.length,
    studyDays: uniqueDays.size,
    totalMinutes: 0 // API에서 Duration 정보를 제공하지 않으므로 0 (혹은 추후 구현)
  }
})

// 결과 보기
function viewExamResult(examId, num) {
  router.push({ path: '/exam/feedback', query: { examId, num } })
}

function viewPracticeFeedback(practiceId) {
  router.push({ path: '/practice/feedback', query: { practiceId } })
}

onMounted(() => {
  loadExamHistory()
  loadPracticeHistory()
  if (authStore.isAuthenticated) {
      authStore.fetchUser();
  }
})
</script>

<template>
  <div class="mypage-container">
    <main class="page-content fade-in">

      <div class="mypage-grid">
        <!-- 프로필 카드 -->
        <section class="card profile-section">
          <div class="section-header">
            <h2>프로필 정보</h2>
            <button v-if="!isEditing" @click="startEdit" class="btn btn-ghost">
              <span class="material-icons-outlined">edit</span>
              편집
            </button>
          </div>
          <div class="profile-content">
            <!-- 프로필 이미지 -->
            <div class="profile-avatar-display" :class="{ 'editable': isEditing }" @click="triggerFileInput">
              <div class="avatar-circle">
                <img :src="displayAvatar" alt="프로필" class="profile-image" />
                <div v-if="isEditing" class="avatar-overlay">
                  <span class="material-icons-outlined">photo_camera</span>
                </div>
                <div v-if="isUploading" class="upload-spinner">
                  <div class="mini-spinner"></div>
                </div>
              </div>
              <input type="file" ref="fileInput" @change="handleImageUpload" style="display: none" accept="image/*" />
            </div>

            <!-- 프로필 정보 -->
            <div v-if="!isEditing" class="profile-info-display">
              <div class="info-row">
                <span class="info-label">닉네임</span>
                <span class="info-value">{{ userName }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">이메일</span>
                <span class="info-value">{{ authStore.user?.email }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">목표 등급</span>
                <span class="info-value grade-badge">{{ authStore.user?.targetLevel || 'IH' }}</span>
              </div>
            </div>

            <!-- 프로필 편집 폼 -->
            <div v-else class="profile-edit">
              <div class="form-group">
                <label class="label">닉네임</label>
                <input 
                  v-model="editForm.nickname" 
                  type="text" 
                  class="input" 
                  :placeholder="userName"
                  @focus="$event.target.placeholder = ''"
                  @blur="$event.target.placeholder = userName"
                />
              </div>
              <div class="form-group">
                <label class="label">목표 등급</label>
                <select v-model="editForm.targetLevel" class="input">
                  <option v-for="option in levelOptions" :key="option.value" :value="option.value">
                    {{ option.label }}
                  </option>
                </select>
              </div>
              <div class="form-actions">
                <button @click="cancelEdit" class="btn btn-secondary">취소</button>
                <button @click="saveProfile" class="btn btn-primary">저장</button>
              </div>
            </div>
          </div>
        </section>

        <!-- 학습 통계 카드 -->
        <section class="card stats-section">
          <div class="section-header">
            <h2>학습 요약</h2>
          </div>
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-icon full-icon">
                <span class="material-icons-outlined">assignment</span>
              </div>
              <div class="stat-content">
                <p class="stat-label">실전 모의고사</p>
                <p class="stat-value"><span class="num-text">{{ learningStats.totalExams }}</span>회</p>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon full-icon">
                <span class="material-icons-outlined">category</span>
              </div>
              <div class="stat-content">
                <p class="stat-label">유형별 연습</p>
                <p class="stat-value"><span class="num-text">{{ learningStats.totalPractice }}</span>회</p>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon full-icon">
                <span class="material-icons-outlined">calendar_today</span>
              </div>
              <div class="stat-content">
                <p class="stat-label">학습 일수</p>
                <p class="stat-value"><span class="num-text">{{ learningStats.studyDays }}</span>일</p>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon full-icon">
                <span class="material-icons-outlined">schedule</span>
              </div>
              <div class="stat-content">
                <p class="stat-label">학습 시간</p>
                <p class="stat-value"><span class="num-text">{{ Math.floor(learningStats.totalMinutes / 60) }}</span>시간</p>
              </div>
            </div>
          </div>
        </section>
      </div>

      <!-- 학습 기록 -->
      <div class="history-section">
        <!-- 모의고사 내역 -->
        <section class="card">
          <div class="section-header">
            <h2>실전 모의고사 기록</h2>
            <span class="count-badge">{{ examHistory.length }}회</span>
          </div>

          <div v-if="isLoadingExams" class="loading">로딩 중...</div>
          <div v-else-if="examHistory.length === 0" class="empty-state">
            <p>아직 응시한 모의고사가 없습니다.</p>
          </div>
          <div v-else class="history-list">
            <div 
              v-for="exam in examHistory" 
              :key="exam.examId"
              class="history-item"
              @click="viewExamResult(exam.examId, exam.num)"
            >
              <div class="item-icon">
                <span class="material-icons-outlined">assignment</span>
              </div>
              <div class="item-content">
                <h4>{{ exam.title }}</h4>
                <p class="item-date">{{ new Date(exam.createdAt).toLocaleString('ko-KR') }}</p>
              </div>
              <div class="item-meta">
                <span class="grade-badge">{{ exam.grade }}</span>
                <span class="score" v-if="exam.totalScore">{{ exam.totalScore }}점</span>
              </div>
              <span class="material-icons-outlined arrow">chevron_right</span>
            </div>
          </div>
        </section>

        <!-- 유형별 연습 내역 -->
        <section class="card">
          <div class="section-header">
            <h2>유형별 연습 기록</h2>
            <span class="count-badge">{{ practiceHistory.length }}회</span>
          </div>

          <div v-if="isLoadingPractice" class="loading">로딩 중...</div>
          <div v-else-if="practiceHistory.length === 0" class="empty-state">
            <p>아직 연습한 문제가 없습니다.</p>
          </div>
          <div v-else class="history-list">
            <div 
              v-for="practice in practiceHistory" 
              :key="practice.practiceId"
              class="history-item"
              @click="viewPracticeFeedback(practice.practiceId)"
            >
              <div class="item-icon">
                <span class="material-icons-outlined">category</span>
              </div>
              <div class="item-content">
                <h4>{{ practice.title || practice.topicName }}</h4>
                <p class="item-date">{{ practice.typeName }} · {{ new Date(practice.createdAt).toLocaleString('ko-KR') }}</p>
              </div>
              <span class="material-icons-outlined arrow">chevron_right</span>
            </div>
          </div>
        </section>
      </div>
    </main>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Material+Icons+Outlined&display=swap');

/* 공통 카드 스타일 */
.card:hover {
  transform: none !important;
}

.page-container {
  min-height: 100vh;
  background: var(--bg-color);
}

.mypage-container {
  min-height: calc(100vh - var(--header-height));
}

.page-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 64px 64px 32px;
}

.fade-in {
  animation: fadeIn 0.6s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 1024px) {
  .page-content {
    padding: 24px 32px;
  }
}

@media (max-width: 768px) {
  .page-content {
    padding: 16px 24px;
  }
}

.page-title {
  font-size: var(--font-size-xl);
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 32px;
}

.mypage-grid {
  display: grid;
  grid-template-columns: 1fr 1.2fr;
  gap: 24px;
  margin-bottom: 24px;
}

.card {
  background: #FFFFFF;
  border: 1px solid #F1F5F9;
  border-radius: 20px;
  padding: 32px;
  box-shadow: var(--shadow-md);
}

@media (max-width: 1024px) {
  .card {
    padding: 24px;
  }
}

/* 섹션 헤더 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-header h2 {
  font-size: 1.5rem;
  font-weight: 800;
  color: var(--text-primary);
}

/* 프로필 섹션 */
.profile-section {
  grid-column: 1 / 2;
}

.profile-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}

/* 프로필 아바타 */
.profile-avatar-display {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
  position: relative;
}

.profile-avatar-display.editable {
  cursor: pointer;
}

.avatar-circle {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  border: 4px solid #FFFFFF;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  overflow: hidden;
  background: #F1F5F9;
  position: relative;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #FFFFFF;
  opacity: 0;
  transition: opacity 0.2s;
}

.profile-avatar-display.editable:hover .avatar-overlay {
  opacity: 1;
}

.upload-spinner {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2;
}

.mini-spinner {
  width: 24px;
  height: 24px;
  border: 3px solid var(--bg-tertiary);
  border-top-color: var(--primary-color);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.profile-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 프로필 정보 표시 영역 */
.profile-info-display {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: var(--bg-tertiary);
  border-radius: 12px;
  border: 1px solid #F1F5F9;
}

.info-label {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-secondary);
}

.info-value {
  font-size: 1rem;
  font-weight: 700;
  color: var(--text-primary);
}

.grade-badge {
  padding: 4px 12px;
  background: var(--primary-light);
  color: #8B7300;
  border-radius: 20px;
  font-weight: 700;
  font-size: 0.8125rem;
}

.dark-mode .grade-badge {
  background: rgba(255, 215, 0, 0.2);
  color: var(--primary-color);
}

.profile-edit {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding: 24px;
  background: #FFFFFF;
  border-radius: 16px;
  border: 1px solid #F1F5F9;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group .input::placeholder {
  color: #BDBDBD;
  opacity: 0.8;
  font-weight: 400;
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}

.form-actions .btn {
  flex: 1;
}

/* 범용 버튼 및 입력창 스타일 */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
  border: none;
  font-size: 0.95rem;
}

.btn-primary {
  background: var(--primary-color);
  color: #212529;
}

.btn-primary:hover {
  background: var(--primary-hover);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.btn-secondary {
  background: var(--bg-tertiary);
  color: var(--text-primary);
}

.btn-secondary:hover {
  background: var(--bg-secondary);
  border: var(--border-thin);
}

.btn-ghost {
  background: transparent;
  color: var(--text-secondary);
  padding: 8px 12px;
  border: none;
}

.btn-ghost:hover {
  color: var(--primary-color);
  background: var(--bg-tertiary);
  transform: translateY(-2px);
}

.btn-secondary:hover {
  transform: translateY(-2px);
}

.label {
  font-size: 0.9rem;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 6px;
}

.input {
  width: 100%;
  padding: 12px 16px;
  background: #FFFFFF;
  border: 1px solid #F1F5F9;
  border-radius: 12px;
  color: var(--text-primary);
  font-size: 1rem;
  transition: all 0.2s ease;
}

.input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 4px var(--primary-light);
}

/* 학습 통계 섹션 - Minimalist Typographic Style */
.stats-section {
  grid-column: 2 / 3;
}

@media (max-width: 1024px) {
  .profile-section, .stats-section {
    grid-column: 1 / 2;
  }
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1px;
  background: rgba(251, 192, 45, 0.12);
  position: relative;
  margin-top: 56px; /* 헤더와의 간격을 더 넓혀 내용을 밑으로 충분히 내림 */
  margin-bottom: 12px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;
  padding: 24px 32px;
  background: #FFFFFF;
  position: relative;
  min-height: 135px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: default;
}

.stat-item:hover {
  background: #FFFFFF; /* 배경색 변화 제거 */
}

.stat-item::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: 0;
  width: 0;
  height: 2px;
  background: var(--honey-500);
  transition: width 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.stat-item:hover::after {
  width: 100%;
}

.stat-icon {
  position: absolute;
  top: 24px;
  right: 24px;
  opacity: 0.4;
  font-size: 2.5rem;
  pointer-events: none;
}

.stat-icon.full-icon {
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon .material-icons-outlined {
  font-size: 2.5rem;
  color: var(--honey-600);
}

.stat-content {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  z-index: 1;
}

.stat-label {
  font-family: var(--font-heading);
  font-size: 1.2rem;
  color: var(--text-secondary);
  font-weight: 700;
  letter-spacing: -0.01em;
}

.stat-value {
  font-family: var(--font-heading);
  font-size: 2.1rem;
  font-weight: 800;
  line-height: 1;
  letter-spacing: -0.02em;
  color: var(--text-primary);
}

.num-text {
  color: var(--honey-600);
  margin-right: 2px;
}

/* 학습 기록 */
.history-section {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.count-badge {
  padding: 4px 12px;
  background: var(--primary-light);
  border-radius: 16px;
  font-size: 0.8125rem;
  font-weight: 700;
  color: #8B7300;
}

.dark-mode .count-badge {
  background: rgba(255, 215, 0, 0.2);
  color: var(--primary-color);
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.history-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: var(--bg-tertiary);
  border-radius: 16px;
  border: 1px solid #F1F5F9;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.history-item:hover {
  transform: translateY(-4px);
  background: #FFFFFF;
  box-shadow: 0 8px 16px rgba(0,0,0,0.06);
  border-color: var(--primary-color);
}

.item-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: var(--primary-light);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #8B7300;
  flex-shrink: 0;
}

.dark-mode .item-icon {
  background: rgba(255, 215, 0, 0.2);
  color: var(--primary-color);
}

.item-icon .material-icons-outlined {
  font-size: 1.5rem;
}

.item-content {
  flex: 1;
}

.item-content h4 {
  font-size: 1rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.item-date {
  font-size: 0.875rem;
  color: var(--text-tertiary);
}

.item-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.item-meta .grade-badge {
  padding: 4px 12px;
  font-size: 0.8125rem;
}

.score {
  font-size: 1rem;
  font-weight: 700;
  color: var(--text-primary);
}

.arrow {
  color: var(--text-tertiary);
  font-size: 1.5rem;
}

.loading, .empty-state {
  text-align: center;
  padding: 40px;
  color: var(--text-secondary);
}

/* 반응형 */
@media (max-width: 1024px) {
  .mypage-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .page-title {
    font-size: 2rem;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>