<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { usersApi } from '@/api'

const router = useRouter()
const authStore = useAuthStore()

// 프로필 이미지가 있는지 확인
const hasProfileImage = computed(() => {
  const userImage = authStore.user?.profileImageUrl
  return userImage && typeof userImage === 'string' && userImage.trim() !== ''
})

// 프로필 이미지 URL
const profileImageUrl = computed(() => {
  const userImage = authStore.user?.profileImageUrl
  if (!userImage || typeof userImage !== 'string' || userImage.trim() === '') {
    return ''
  }
  
  if (userImage.startsWith('http') || userImage.startsWith('data:')) {
    return userImage
  }
  
  const baseUrl = import.meta.env.VITE_API_BASE_URL || ''
  const cleanBase = baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl
  const cleanPath = userImage.startsWith('/') ? userImage : `/${userImage}`
  return `${cleanBase}${cleanPath}`
})

// 프로필 이미지 업로드
const profileImageInput = ref(null)
const isUploadingImage = ref(false)

const handleProfileImageUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  try {
    isUploadingImage.value = true
    
    const response = await usersApi.updateProfileImage({ file })
    
    if (response.data?.profileImageUrl) {
      authStore.user.profileImageUrl = response.data.profileImageUrl
      alert('프로필 이미지가 변경되었습니다.')
    }
  } catch (error) {
    console.error('이미지 업로드 실패:', error)
    const reader = new FileReader()
    reader.onload = (e) => {
      authStore.user.profileImageUrl = e.target.result
    }
    reader.readAsDataURL(file)
  } finally {
    isUploadingImage.value = false
  }
}

const triggerImageUpload = () => {
  profileImageInput.value?.click()
}

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

const startEdit = () => {
  editForm.value = {
    nickname: authStore.user?.nickname || '',
    targetLevel: authStore.user?.targetLevel || 'INTERMEDIATE_HIGH'
  }
  isEditing.value = true
}

const cancelEdit = () => {
  isEditing.value = false
}

const saveProfile = async () => {
  try {
    if (editForm.value.nickname !== authStore.user?.nickname) {
      await usersApi.updateNickname({ nickname: editForm.value.nickname })
    }
    
    if (editForm.value.targetLevel !== authStore.user?.targetLevel) {
      await usersApi.updateTargetLevel({ targetLevel: editForm.value.targetLevel })
    }
    
    const response = await usersApi.getMyInfo()
    authStore.user = response.data
    
    // authStore 갱신 후 즉시 반영을 위해 관련 필드 수동 업데이트
    if (localStorage.getItem('user')) {
      localStorage.setItem('user', JSON.stringify(response.data))
    }
    
    isEditing.value = false
    alert('프로필이 저장되었습니다.')
  } catch (error) {
    console.error('프로필 저장 실패:', error)
    authStore.user.nickname = editForm.value.nickname
    authStore.user.targetLevel = editForm.value.targetLevel
    isEditing.value = false
  }
}

// 모의고사 내역
const examHistory = ref([])
const isLoadingExams = ref(false)

const loadExamHistory = async () => {
  try {
    isLoadingExams.value = true
    
    examHistory.value = [
      {
        examId: 1,
        title: '제 12회 실전 모의고사',
        createdAt: '2026-01-20T10:30:00',
        grade: 'IH',
        totalScore: 85.5,
        status: 'COMPLETED'
      },
      {
        examId: 2,
        title: '제 11회 실전 모의고사',
        createdAt: '2026-01-15T14:20:00',
        grade: 'IM3',
        totalScore: 78.2,
        status: 'COMPLETED'
      },
      {
        examId: 3,
        title: '제 10회 실전 모의고사',
        createdAt: '2026-01-10T09:15:00',
        grade: 'IM2',
        totalScore: 72.8,
        status: 'COMPLETED'
      }
    ]
  } catch (error) {
    console.error('시험 내역 로드 실패:', error)
  } finally {
    isLoadingExams.value = false
  }
}

// 유형 연습 내역
const practiceHistory = ref([])
const isLoadingPractice = ref(false)

const loadPracticeHistory = async () => {
  try {
    isLoadingPractice.value = true
    
    practiceHistory.value = [
      {
        practiceId: 1,
        questionId: 101,
        typeName: '롤플레이',
        topicName: '여행 중 겪은 경험',
        createdAt: '2026-01-23T16:40:00',
        status: 'REVIEWED'
      },
      {
        practiceId: 2,
        questionId: 202,
        typeName: '콤보',
        topicName: '음악 감상 및 기기',
        createdAt: '2026-01-22T11:20:00',
        status: 'REVIEWED'
      },
      {
        practiceId: 3,
        questionId: 303,
        typeName: '자유주제',
        topicName: '좋아하는 음식',
        createdAt: '2026-01-18T15:30:00',
        status: 'REVIEWED'
      }
    ]
  } catch (error) {
    console.error('연습 내역 로드 실패:', error)
  } finally {
    isLoadingPractice.value = false
  }
}

// 학습 통계
const learningStats = computed(() => ({
  totalExams: examHistory.value.length,
  totalPractice: practiceHistory.value.length,
  studyDays: 23,
  totalMinutes: 1420
}))

// 결과 보기
const viewExamResult = (examId) => {
  router.push({ path: '/exam/result', query: { examId } })
}

const viewPracticeFeedback = (practiceId, questionId) => {
  router.push({ path: '/practice/feedback', query: { practiceId, questionId } })
}

onMounted(() => {
  loadExamHistory()
  loadPracticeHistory()
})
</script>

<template>
  <div class="page-container">
    <main class="page-content">
      <h1 class="page-title">마이페이지</h1>

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
            <div class="profile-image-section">
              <div class="profile-preview-large" @click="!isEditing && triggerImageUpload()">
                <img 
                  v-if="authStore.user?.profileImageUrl" 
                  :src="profileImageUrl" 
                  alt="프로필" 
                  class="profile-img" 
                />
                <img 
                  v-else 
                  src="/default-profile.png" 
                  alt="기본 프로필" 
                  class="profile-img fallback" 
                />
                <div v-if="!isEditing" class="upload-overlay">
                  <span class="material-icons-outlined">photo_camera</span>
                  <p>{{ isUploadingImage ? '업로드 중...' : '사진 변경' }}</p>
                </div>
              </div>
              <input 
                ref="profileImageInput"
                type="file" 
                accept="image/*" 
                style="display: none"
                @change="handleProfileImageUpload"
              />
            </div>

            <!-- 프로필 정보 -->
            <div v-if="!isEditing" class="profile-info-display">
              <div class="info-row">
                <span class="info-label">닉네임</span>
                <span class="info-value">{{ authStore.user?.nickname || '사용자' }}</span>
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
          <h2>학습 통계</h2>
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-icon">📝</div>
              <div class="stat-content">
                <p class="stat-label">실전 모의고사</p>
                <p class="stat-value">{{ learningStats.totalExams }}회</p>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon">🎯</div>
              <div class="stat-content">
                <p class="stat-label">유형별 연습</p>
                <p class="stat-value">{{ learningStats.totalPractice }}회</p>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon">📅</div>
              <div class="stat-content">
                <p class="stat-label">학습 일수</p>
                <p class="stat-value">{{ learningStats.studyDays }}일</p>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-icon">⏱️</div>
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
              @click="viewExamResult(exam.examId)"
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
                <span class="score">{{ exam.totalScore }}점</span>
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
              @click="viewPracticeFeedback(practice.practiceId, practice.questionId)"
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
  background: var(--bg-primary);
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
  font-size: 2.5rem;
  font-weight: 900;
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
  background: var(--bg-secondary);
  border: var(--border-primary);
  border-radius: var(--border-radius);
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
  font-weight: 900;
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

.profile-image-section {
  cursor: pointer;
  position: relative;
}

.profile-preview-large {
  width: 160px;
  height: 160px;
  border-radius: 50%;
  overflow: hidden;
  border: var(--border-primary);
  box-shadow: var(--shadow-sm);
  position: relative;
  background: var(--bg-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
}

.profile-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.okkul-preview {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  transform: scale(1.3);
}

.upload-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
  gap: 8px;
}

.profile-image-section:hover .upload-overlay {
  opacity: 1;
}

.upload-overlay .material-icons-outlined {
  color: white;
  font-size: 2rem;
}

.upload-overlay p {
  color: white;
  font-size: 0.875rem;
  font-weight: 600;
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
  border: 1px solid var(--border-primary);
}

.info-label {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-secondary);
}

.info-value {
  font-size: 1rem;
  font-weight: 900;
  color: var(--text-primary);
}

.grade-badge {
  padding: 6px 16px;
  background: var(--primary-color);
  color: #000000;
  border-radius: var(--border-radius);
  font-weight: 900;
  border: var(--border-secondary);
  box-shadow: var(--shadow-sm);
}

.dark-mode .grade-badge {
  background: rgba(255, 215, 0, 0.2);
  color: var(--primary-color);
  border-color: rgba(255, 215, 0, 0.3);
}

.profile-edit {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding: 24px;
  background: var(--bg-secondary);
  border-radius: var(--border-radius);
  border: var(--border-primary);
  box-shadow: var(--shadow-sm);
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
  border-radius: var(--border-radius);
  font-weight: 900;
  cursor: pointer;
  transition: all 0.2s ease;
  border: var(--border-secondary);
  font-size: 0.95rem;
  box-shadow: var(--shadow-sm);
}

.btn-primary {
  background: var(--primary-color);
  color: #000000;
}

.btn-primary:hover {
  transform: translate(-0.02em, -0.02em);
  box-shadow: var(--shadow-md);
}

.btn-secondary {
  background: var(--bg-tertiary);
  color: var(--text-primary);
}

.btn-secondary:hover {
  background: var(--bg-secondary);
  border-color: var(--primary-color);
}

.btn-ghost {
  background: transparent;
  color: var(--text-secondary);
  padding: 8px 12px;
  border: none;
  box-shadow: none;
}

.btn-ghost:hover {
  color: var(--primary-color);
  background: var(--bg-tertiary);
}

.label {
  font-size: 0.9rem;
  font-weight: 700;
  color: var(--text-secondary);
  margin-bottom: 6px;
}

.input {
  width: 100%;
  padding: 12px 16px;
  background: var(--bg-secondary);
  border: var(--border-secondary);
  border-radius: var(--border-radius);
  color: var(--text-primary);
  font-size: 1rem;
  transition: all 0.2s ease;
}

.input:focus {
  outline: none;
  border-color: var(--primary-color);
  background: var(--bg-secondary);
  box-shadow: 0 0 0 4px rgba(255, 215, 0, 0.1);
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
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: var(--bg-tertiary);
  border-radius: var(--border-radius);
  border: var(--border-thin);
  transition: all 0.2s ease;
}

.stat-item:hover {
  transform: translate(-0.02em, -0.02em);
  box-shadow: var(--shadow-sm);
}

.stat-icon {
  font-size: 2.5rem;
}

.stat-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 0.875rem;
  color: var(--text-secondary);
  font-weight: 600;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 900;
  color: var(--text-primary);
}

/* 학습 기록 */
.history-section {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.count-badge {
  padding: 6px 12px;
  background: var(--primary-color);
  border-radius: var(--border-radius);
  font-size: 0.875rem;
  font-weight: 900;
  color: #000000;
  border: var(--border-thin);
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.history-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: var(--bg-tertiary);
  border-radius: var(--border-radius);
  border: var(--border-thin);
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: var(--shadow-sm);
}

.history-item:hover {
  transform: translate(-0.02em, -0.02em);
  box-shadow: var(--shadow-md);
  background: var(--bg-secondary);
}

.item-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: var(--primary-color);
  border: var(--border-thin);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #92400e;
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
  color: var(--text-secondary);
}

.item-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.item-meta .grade-badge {
  padding: 4px 12px;
  font-size: 0.875rem;
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