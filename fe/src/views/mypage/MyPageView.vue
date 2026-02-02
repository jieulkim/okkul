<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { usersApi, historyApi } from '@/api'

const router = useRouter()
const authStore = useAuthStore()

// 사용자 닉네임
const userName = computed(() => {
  return authStore.user?.nickname || authStore.user?.name || '사용자'
})

// 프로필 편집
const isEditing = ref(false)
const editForm = ref({
  nickname: '',
  targetLevel: ''
})

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
    nickname: authStore.user?.nickname || '',
    targetLevel: authStore.user?.targetLevel || 'INTERMEDIATE_HIGH'
  }
  isEditing.value = true
}

function cancelEdit() {
  isEditing.value = false
}

async function saveProfile() {
  try {
    // API 호출
    if (editForm.value.nickname !== authStore.user?.nickname) {
      await usersApi.updateNickname({ nickname: editForm.value.nickname })
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
    
    examHistory.value = data.content?.map((exam, index) => ({
      examId: exam.examId,
      num: total - index, // 최신순 정렬이므로 total - index가 회차임
      title: `제 ${total - index}회 실전 모의고사`,
      createdAt: exam.createdAt,
      grade: exam.grade || '채점 중'
    })) || []
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
  <div class="page-container">
    <main class="page-content">

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
            <div class="profile-avatar-display">
              <div class="avatar-circle">
                <img src="/default-profile.png" alt="프로필" class="profile-image" />
              </div>
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
                <input v-model="editForm.nickname" type="text" class="input" />
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
            <h2>학습 통계</h2>
          </div>
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-icon full-icon">
                <span class="material-icons-outlined">assignment</span>
              </div>
              <div class="stat-content">
                <p class="stat-label">실전 모의고사</p>
                <p class="stat-value">{{ learningStats.totalExams }}회</p>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon full-icon">
                <span class="material-icons-outlined">category</span>
              </div>
              <div class="stat-content">
                <p class="stat-label">유형별 연습</p>
                <p class="stat-value">{{ learningStats.totalPractice }}회</p>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon full-icon">
                <span class="material-icons-outlined">calendar_today</span>
              </div>
              <div class="stat-content">
                <p class="stat-label">학습 일수</p>
                <p class="stat-value">{{ learningStats.studyDays }}일</p>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon full-icon">
                <span class="material-icons-outlined">schedule</span>
              </div>
              <div class="stat-content">
                <p class="stat-label">학습 시간</p>
                <p class="stat-value">{{ Math.floor(learningStats.totalMinutes / 60) }}시간</p>
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
                <h4>{{ practice.topicName }}</h4>
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

.page-container {
  min-height: 100vh;
  background: var(--bg-color);
}

.page-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 32px 64px;
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
  width: 100%;
  display: flex;
  justify-content: center;
  margin-bottom: 8px;
}

.avatar-circle {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  background: var(--bg-tertiary);
  display: flex;
  align-items: center;
  justify-content: center;
  border: 4px solid var(--bg-secondary);
  box-shadow: 0 8px 24px rgba(255, 215, 0, 0.2);
  overflow: hidden;
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

/* 학습 통계 섹션 */
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
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.stat-item {
  display: flex;
  flex-direction: column; /* 아이콘 위, 텍스트 아래 */
  align-items: center;    /* 가로 중앙 정렬 */
  justify-content: center; /* 세로 중앙 정렬 */
  text-align: center;     /* 텍스트 중앙 정렬 */
  gap: 10px;
  padding: 32px 24px;
  background: var(--bg-tertiary);
  border-radius: 16px;
  border: 1px solid #F1F5F9;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.stat-item:hover {
  transform: translateY(-4px);
  background: #FFFFFF;
  box-shadow: 0 8px 16px rgba(0,0,0,0.06);
  border-color: var(--primary-color);
}

.stat-icon {
  font-size: 2.5rem;
}

.stat-icon.full-icon {
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon .material-icons-outlined {
  font-size: 3rem;
  color: #F9A825; /* 아이콘 색상 직접 지정 */
}

.stat-content {
  display: flex;
  flex-direction: column;
  align-items: center; /* 텍스트 가로 중앙 정렬 */
  gap: 4px;
}

.stat-label {
  font-size: 0.875rem;
  color: var(--text-secondary);
  font-weight: 600;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 800;
  color: var(--text-primary);
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