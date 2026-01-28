<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { usersApi } from '@/api'

const router = useRouter()
const authStore = useAuthStore()

// 프로필 이미지 URL (없거나 빈 문자열이면 기본 오꿀이 이미지)
// 프로필 이미지 URL
const profileImageUrl = computed(() => {
  const userImage = authStore.user?.profileImageUrl
  if (!userImage || typeof userImage !== 'string' || userImage.trim() === '') {
    return '/default-profile.png'
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
    
    // authStore 업데이트
    if (response.data?.profileImageUrl) {
      authStore.user.profileImageUrl = response.data.profileImageUrl
      alert('프로필 이미지가 변경되었습니다.')
    }
  } catch (error) {
    console.error('이미지 업로드 실패:', error)
    // 임시로 로컬 프리뷰만 표시
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
    // 닉네임 변경
    if (editForm.value.nickname !== authStore.user?.nickname) {
      await usersApi.updateNickname({ nickname: editForm.value.nickname })
    }
    
    // 목표 등급 변경
    if (editForm.value.targetLevel !== authStore.user?.targetLevel) {
      await usersApi.updateTargetLevel({ targetLevel: editForm.value.targetLevel })
    }
    
    // authStore 업데이트
    const response = await usersApi.getMyInfo()
    authStore.user = response.data
    
    isEditing.value = false
    alert('프로필이 저장되었습니다.')
  } catch (error) {
    console.error('프로필 저장 실패:', error)
    // 임시로 로컬에만 저장
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
    // TODO: 실제 API 엔드포인트 구현 필요
    
    // 임시 더미 데이터
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
    // TODO: 실제 API 엔드포인트 구현 필요
    
    // 임시 더미 데이터
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
  <div class="mypage-container">
    <main class="mypage-content">
      <h1 class="page-title">마이페이지</h1>

      <div class="mypage-grid">
        <!-- 프로필 카드 -->
        <section class="profile-section">
          <div class="section-header">
            <h2>프로필 정보</h2>
            <button v-if="!isEditing" @click="startEdit" class="edit-btn">
              <span class="material-icons-outlined">edit</span>
              편집
            </button>
          </div>

          <div class="profile-card">
            <!-- 프로필 이미지 -->
            <div class="profile-image-section" @click="triggerImageUpload">
              <div class="profile-preview-large">
                <img 
                  :src="profileImageUrl" 
                  alt="프로필"
                  class="profile-img"
                  @error="(e) => e.target.src = '/default-profile.png'"
                />
                <div class="upload-overlay">
                  <span class="material-icons-outlined">photo_camera</span>
                  <p>{{ isUploadingImage ? '업로드 중...' : '사진 변경' }}</p>
                </div>
              </div>
              <input 
                ref="profileImageInput"
                type="file" 
                accept="image/*"
                @change="handleProfileImageUpload"
                style="display: none"
              />
            </div>

            <!-- 프로필 정보 (읽기 모드) -->
            <div v-if="!isEditing" class="profile-info">
              <div class="info-row">
                <span class="info-label">닉네임</span>
                <span class="info-value">{{ authStore.user?.nickname }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">이메일</span>
                <span class="info-value">{{ authStore.user?.email }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">목표 등급</span>
                <span class="info-value badge">{{ authStore.user?.targetLevel || 'IH' }}</span>
              </div>
            </div>

            <!-- 프로필 편집 폼 -->
            <div v-else class="profile-edit">
              <div class="form-group">
                <label>닉네임</label>
                <input v-model="editForm.nickname" type="text" class="form-input" />
              </div>
              <div class="form-group">
                <label>목표 등급</label>
                <select v-model="editForm.targetLevel" class="form-select">
                  <option v-for="option in levelOptions" :key="option.value" :value="option.value">
                    {{ option.label }}
                  </option>
                </select>
              </div>
              <div class="form-actions">
                <button @click="cancelEdit" class="btn-cancel">취소</button>
                <button @click="saveProfile" class="btn-save">저장</button>
              </div>
            </div>
          </div>
        </section>

        <!-- 학습 통계 -->
        <section class="stats-section">
          <h2 class="section-title">학습 통계</h2>
          <div class="stats-grid">
            <div class="stat-card">
              <span class="stat-icon">📝</span>
              <div class="stat-content">
                <span class="stat-value">{{ learningStats.totalExams }}</span>
                <span class="stat-label">모의고사</span>
              </div>
            </div>
            <div class="stat-card">
              <span class="stat-icon">✏️</span>
              <div class="stat-content">
                <span class="stat-value">{{ learningStats.totalPractice }}</span>
                <span class="stat-label">유형 연습</span>
              </div>
            </div>
            <div class="stat-card">
              <span class="stat-icon">🔥</span>
              <div class="stat-content">
                <span class="stat-value">{{ learningStats.studyDays }}</span>
                <span class="stat-label">학습일</span>
              </div>
            </div>
            <div class="stat-card">
              <span class="stat-icon">⏱️</span>
              <div class="stat-content">
                <span class="stat-value">{{ Math.floor(learningStats.totalMinutes / 60) }}</span>
                <span class="stat-label">학습 시간</span>
              </div>
            </div>
          </div>
        </section>

        <!-- 모의고사 내역 -->
        <section class="history-section">
          <h2 class="section-title">
            <span class="material-icons-outlined">assignment</span>
            모의고사 내역
          </h2>
          
          <div v-if="isLoadingExams" class="loading">
            <div class="spinner"></div>
            <p>로딩 중...</p>
          </div>
          
          <div v-else-if="examHistory.length === 0" class="empty-state">
            <span class="material-icons-outlined empty-icon">assignment</span>
            <p>아직 모의고사 기록이 없습니다</p>
            <button @click="router.push('/exam')" class="start-btn">
              모의고사 시작하기
            </button>
          </div>
          
          <div v-else class="history-list">
            <div 
              v-for="exam in examHistory" 
              :key="exam.examId" 
              class="history-card"
              @click="viewExamResult(exam.examId)"
            >
              <div class="history-content">
                <div class="history-header">
                  <h3>{{ exam.title }}</h3>
                  <span class="grade-badge">{{ exam.grade }}</span>
                </div>
                <div class="history-meta">
                  <span class="meta-item">
                    <span class="material-icons-outlined">calendar_today</span>
                    {{ new Date(exam.createdAt).toLocaleDateString('ko-KR') }}
                  </span>
                  <span class="meta-item">
                    <span class="material-icons-outlined">score</span>
                    {{ Math.round(exam.totalScore) }}점
                  </span>
                </div>
              </div>
              <div class="history-action">
                <span class="material-icons-outlined">chevron_right</span>
              </div>
            </div>
          </div>
        </section>

        <!-- 유형 연습 내역 -->
        <section class="history-section">
          <h2 class="section-title">
            <span class="material-icons-outlined">category</span>
            유형 연습 내역
          </h2>
          
          <div v-if="isLoadingPractice" class="loading">
            <div class="spinner"></div>
            <p>로딩 중...</p>
          </div>
          
          <div v-else-if="practiceHistory.length === 0" class="empty-state">
            <span class="material-icons-outlined empty-icon">category</span>
            <p>아직 유형 연습 기록이 없습니다</p>
            <button @click="router.push('/practice')" class="start-btn">
              유형 연습 시작하기
            </button>
          </div>
          
          <div v-else class="history-list">
            <div 
              v-for="practice in practiceHistory" 
              :key="practice.practiceId" 
              class="history-card"
              @click="viewPracticeFeedback(practice.practiceId, practice.questionId)"
            >
              <div class="history-content">
                <div class="history-header">
                  <h3>{{ practice.typeName }}: {{ practice.topicName }}</h3>
                </div>
                <div class="history-meta">
                  <span class="meta-item">
                    <span class="material-icons-outlined">calendar_today</span>
                    {{ new Date(practice.createdAt).toLocaleDateString('ko-KR') }}
                  </span>
                  <span class="meta-item status-reviewed">
                    <span class="material-icons-outlined">check_circle</span>
                    피드백 확인 완료
                  </span>
                </div>
              </div>
              <div class="history-action">
                <span class="material-icons-outlined">chevron_right</span>
              </div>
            </div>
          </div>
        </section>
      </div>
    </main>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@400;500;700;900&display=swap');
@import url('https://fonts.googleapis.com/icon?family=Material+Icons|Material+Icons+Outlined');

.mypage-container {
  min-height: 100vh;
  background: #f8fafc;
  padding: 40px 20px;
}

.mypage-content {
  max-width: 1200px;
  margin: 0 auto;
}

.page-title {
  font-size: 32px;
  font-weight: 900;
  margin-bottom: 32px;
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.mypage-grid {
  display: grid;
  gap: 24px;
}

/* 섹션 공통 */
.profile-section,
.stats-section,
.history-section {
  background: white;
  border-radius: 20px;
  padding: 32px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  border: 2px solid #e2e8f0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-header h2,
.section-title {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.edit-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #f1f5f9;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.edit-btn:hover {
  background: #e2e8f0;
}

/* 프로필 카드 */
.profile-card {
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
  width: 150px;
  height: 150px;
  border-radius: 50%;
  overflow: hidden;
  position: relative;
  border: 4px solid #FFD700;
  box-shadow: 0 4px 12px rgba(255, 215, 0, 0.3);
  background: white;
}

.profile-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
  color: white;
}

.profile-image-section:hover .upload-overlay {
  opacity: 1;
}

.upload-overlay .material-icons-outlined {
  font-size: 32px;
  margin-bottom: 8px;
}

.upload-overlay p {
  font-size: 14px;
  font-weight: 600;
}

/* 프로필 정보 */
.profile-info {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f8fafc;
  border-radius: 8px;
}

.info-label {
  font-weight: 600;
  color: #64748b;
}

.info-value {
  font-weight: 600;
  color: #1e293b;
}

.badge {
  padding: 4px 12px;
  background: linear-gradient(135deg, #FFD700, #FFA500);
  color: #92400e;
  border-radius: 6px;
  font-size: 14px;
}

/* 프로필 편집 */
.profile-edit {
  width: 100%;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: #374151;
}

.form-input,
.form-select {
  width: 100%;
  padding: 12px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 15px;
  transition: border-color 0.2s;
}

.form-input:focus,
.form-select:focus {
  outline: none;
  border-color: #FFD700;
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.btn-cancel,
.btn-save {
  padding: 10px 24px;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel {
  background: #f1f5f9;
  border: none;
  color: #64748b;
}

.btn-save {
  background: linear-gradient(135deg, #FFD700, #FFA500);
  border: none;
  color: #92400e;
}

/* 통계 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #f8fafc;
  border-radius: 12px;
  border: 2px solid #e2e8f0;
  transition: all 0.2s;
}

.stat-card:hover {
  border-color: #FFD700;
  transform: translateY(-2px);
}

.stat-icon {
  font-size: 32px;
}

.stat-content {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 24px;
  font-weight: 900;
  color: #1e293b;
}

.stat-label {
  font-size: 13px;
  color: #64748b;
  font-weight: 600;
}

/* 로딩 */
.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  gap: 16px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #e2e8f0;
  border-top-color: #FFD700;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* 빈 상태 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #94a3b8;
}

.empty-icon {
  font-size: 64px !important;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-state p {
  font-size: 16px;
  margin-bottom: 20px;
}

.start-btn {
  padding: 12px 24px;
  background: linear-gradient(135deg, #FFD700, #FFA500);
  color: #92400e;
  border: none;
  border-radius: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
}

.start-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 215, 0, 0.3);
}

/* 내역 리스트 */
.history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.history-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
  background: #f8fafc;
  border-radius: 12px;
  border: 2px solid #e2e8f0;
  cursor: pointer;
  transition: all 0.2s;
}

.history-card:hover {
  border-color: #FFD700;
  transform: translateX(4px);
}

.history-content {
  flex: 1;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  gap: 16px;
}

.history-header h3 {
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
  flex: 1;
}

.grade-badge {
  padding: 4px 12px;
  background: #FFD700;
  color: #92400e;
  border-radius: 6px;
  font-weight: 700;
  font-size: 14px;
  flex-shrink: 0;
}

.history-meta {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #64748b;
}

.meta-item .material-icons-outlined {
  font-size: 18px;
}

.status-reviewed {
  color: #10b981;
  font-weight: 600;
}

.history-action {
  color: #94a3b8;
}

/* 반응형 */
@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .history-card {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .history-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>