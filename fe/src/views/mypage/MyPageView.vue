<script setup>
import { ref, inject } from 'vue'

// 사용자 프로필 데이터 (전역 상태 주입)
const userProfile = inject('userProfile')

// 프로필 이미지 업로드
const profileImageInput = ref(null)

const handleProfileImageUpload = (event) => {
  const file = event.target.files[0]
  if (file) {
    const reader = new FileReader()
    reader.onload = (e) => {
      userProfile.value.profileImage = e.target.result
      // 실제로는 서버에 업로드하고 URL을 받아와야 함
    }
    reader.readAsDataURL(file)
  }
}

const triggerImageUpload = () => {
  profileImageInput.value?.click()
}

// 프로필 편집 모드
const isEditing = ref(false)
const editForm = ref({
  nickname: '',
  name: '',
  targetLevel: ''
})

const startEdit = () => {
  editForm.value = {
    nickname: userProfile.value.nickname,
    name: userProfile.value.name,
    targetLevel: userProfile.value.targetLevel
  }
  isEditing.value = true
}

const cancelEdit = () => {
  isEditing.value = false
}

const saveProfile = () => {
  userProfile.value.nickname = editForm.value.nickname
  userProfile.value.name = editForm.value.name
  userProfile.value.targetLevel = editForm.value.targetLevel
  isEditing.value = false
  // 실제로는 서버에 저장
}

// 학습 통계 (더미 데이터)
const learningStats = ref({
  totalExams: 12,
  totalPractice: 48,
  studyDays: 23,
  totalMinutes: 1420
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
            <div class="profile-image-section" @click="triggerImageUpload">
              <div class="profile-preview-large">
                <img 
                  v-if="userProfile.profileImage" 
                  :src="userProfile.profileImage" 
                  alt="프로필"
                />
                <span v-else class="profile-placeholder-large">
                  {{ userProfile.nickname?.[0]?.toUpperCase() || 'U' }}
                </span>
                <div class="upload-overlay">
                  <span class="material-icons-outlined">photo_camera</span>
                  <p>사진 변경</p>
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

            <div v-if="!isEditing" class="profile-info">
              <div class="info-item">
                <label>닉네임</label>
                <span>{{ userProfile.nickname }}</span>
              </div>
              <div class="info-item">
                <label>이름</label>
                <span>{{ userProfile.name }}</span>
              </div>
              <div class="info-item">
                <label>현재 등급</label>
                <span class="level-badge">{{ userProfile.currentLevel }}</span>
              </div>
              <div class="info-item">
                <label>목표 등급</label>
                <span class="level-badge target">{{ userProfile.targetLevel }}</span>
              </div>
            </div>

            <!-- 편집 폼 -->
            <div v-else class="profile-edit-form">
              <div class="form-group">
                <label>닉네임</label>
                <input v-model="editForm.nickname" type="text" placeholder="닉네임" />
              </div>
              <div class="form-group">
                <label>이름</label>
                <input v-model="editForm.name" type="text" placeholder="이름" />
              </div>
              <div class="form-group">
                <label>목표 등급</label>
                <select v-model="editForm.targetLevel">
                  <option value="AL">AL</option>
                  <option value="IH">IH</option>
                  <option value="IM">IM</option>
                  <option value="IL">IL</option>
                </select>
              </div>
              <div class="form-actions">
                <button @click="cancelEdit" class="cancel-btn">취소</button>
                <button @click="saveProfile" class="save-btn">저장</button>
              </div>
            </div>
          </div>
        </section>

        <!-- 학습 통계 -->
        <section class="stats-section">
          <h2>학습 통계</h2>
          
          <div class="stats-grid">
            <div class="stat-card">
              <div class="stat-icon" style="background: rgba(255, 215, 0, 0.2); color: #FFD700;">
                <span class="material-icons-outlined">assignment</span>
              </div>
              <div class="stat-info">
                <p class="stat-label">실전 모의고사</p>
                <p class="stat-value">{{ learningStats.totalExams }}회</p>
              </div>
            </div>

            <div class="stat-card">
              <div class="stat-icon" style="background: rgba(16, 185, 129, 0.2); color: #10b981;">
                <span class="material-icons-outlined">category</span>
              </div>
              <div class="stat-info">
                <p class="stat-label">유형별 연습</p>
                <p class="stat-value">{{ learningStats.totalPractice }}회</p>
              </div>
            </div>

            <div class="stat-card">
              <div class="stat-icon" style="background: rgba(99, 102, 241, 0.2); color: #6366f1;">
                <span class="material-icons-outlined">calendar_today</span>
              </div>
              <div class="stat-info">
                <p class="stat-label">연속 학습일</p>
                <p class="stat-value">{{ learningStats.studyDays }}일</p>
              </div>
            </div>

            <div class="stat-card">
              <div class="stat-icon" style="background: rgba(245, 158, 11, 0.2); color: #f59e0b;">
                <span class="material-icons-outlined">schedule</span>
              </div>
              <div class="stat-info">
                <p class="stat-label">총 학습 시간</p>
                <p class="stat-value">{{ Math.floor(learningStats.totalMinutes / 60) }}시간 {{ learningStats.totalMinutes % 60 }}분</p>
              </div>
            </div>
          </div>
        </section>

        <!-- 설정 -->
        <section class="settings-section">
          <h2>설정</h2>
          
          <div class="settings-list">
            <div class="setting-item">
              <div class="setting-info">
                <span class="material-icons-outlined">notifications</span>
                <div>
                  <p class="setting-title">알림 설정</p>
                  <p class="setting-desc">학습 리마인더 및 성적 업데이트 알림</p>
                </div>
              </div>
              <label class="toggle-switch">
                <input type="checkbox" checked />
                <span class="toggle-slider"></span>
              </label>
            </div>

            <div class="setting-item">
              <div class="setting-info">
                <span class="material-icons-outlined">language</span>
                <div>
                  <p class="setting-title">언어 설정</p>
                  <p class="setting-desc">앱 언어 변경</p>
                </div>
              </div>
              <select class="lang-select">
                <option value="ko">한국어</option>
                <option value="en">English</option>
              </select>
            </div>

            <div class="setting-item">
              <div class="setting-info">
                <span class="material-icons-outlined">logout</span>
                <div>
                  <p class="setting-title">로그아웃</p>
                  <p class="setting-desc">계정에서 로그아웃</p>
                </div>
              </div>
              <button class="logout-btn">로그아웃</button>
            </div>
          </div>
        </section>
      </div>
    </main>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Material+Icons+Outlined&display=swap');

.mypage-container {
  min-height: 100vh;
  background: linear-gradient(to bottom, #fafafa 0%, #f5f5f5 100%);
  padding: 40px 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', sans-serif;
}

.dark-mode .mypage-container {
  background: linear-gradient(to bottom, #0f172a 0%, #1e293b 100%);
}

.mypage-content {
  max-width: 1200px;
  margin: 0 auto;
}

.page-title {
  font-size: 32px;
  font-weight: 900;
  color: #0f172a;
  margin-bottom: 32px;
}

.dark-mode .page-title {
  color: #f1f5f9;
}

.mypage-grid {
  display: grid;
  gap: 24px;
}

section {
  background: white;
  border-radius: 20px;
  border: 1px solid #f1f5f9;
  padding: 28px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.dark-mode section {
  background: #1e293b;
  border-color: #334155;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

section h2 {
  font-size: 20px;
  font-weight: 800;
  color: #1e293b;
  margin-bottom: 24px;
}

.dark-mode section h2 {
  color: #f1f5f9;
}

.edit-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.dark-mode .edit-btn {
  background: #0f172a;
  border-color: #334155;
  color: #f1f5f9;
}

.edit-btn:hover {
  background: #FFD700;
  border-color: #FFD700;
  color: #000;
}

/* 프로필 카드 */
.profile-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 32px;
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
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
  border: 5px solid #fff;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  position: relative;
}

.dark-mode .profile-preview-large {
  border-color: #1e293b;
}

.profile-preview-large img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-placeholder-large {
  font-size: 64px;
  font-weight: 900;
  color: #000;
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
  transition: opacity 0.2s;
  gap: 8px;
}

.profile-image-section:hover .upload-overlay {
  opacity: 1;
}

.upload-overlay .material-icons-outlined {
  color: white;
  font-size: 40px;
  font-family: 'Material Icons Outlined';
}

.upload-overlay p {
  color: white;
  font-weight: 600;
  font-size: 14px;
}

.profile-info {
  width: 100%;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item label {
  font-size: 13px;
  color: #94a3b8;
  font-weight: 600;
}

.info-item span {
  font-size: 16px;
  color: #1e293b;
  font-weight: 700;
}

.dark-mode .info-item span {
  color: #f1f5f9;
}

.level-badge {
  display: inline-block;
  padding: 6px 14px;
  background: #f8fafc;
  border-radius: 8px;
  font-size: 15px !important;
  font-weight: 800 !important;
  color: #1e293b !important;
}

.dark-mode .level-badge {
  background: #0f172a;
  color: #f1f5f9 !important;
}

.level-badge.target {
  background: #fff7ed;
  color: #FFD700 !important;
}

.dark-mode .level-badge.target {
  background: rgba(255, 215, 0, 0.2);
  color: #FFD700 !important;
}

/* 편집 폼 */
.profile-edit-form {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-size: 13px;
  color: #64748b;
  font-weight: 600;
}

.form-group input,
.form-group select {
  padding: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 600;
}

.dark-mode .form-group input,
.dark-mode .form-group select {
  background: #0f172a;
  border-color: #334155;
  color: #f1f5f9;
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 12px;
}

.cancel-btn,
.save-btn {
  flex: 1;
  padding: 12px;
  border-radius: 10px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
}

.cancel-btn {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  color: #64748b;
}

.save-btn {
  background: #FFD700;
  border: 2px solid #000;
  color: #000;
  box-shadow: 0 4px 0 #000;
}

.save-btn:active {
  transform: translateY(2px);
  box-shadow: 0 2px 0 #000;
}

/* 학습 통계 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #f8fafc;
  border-radius: 12px;
}

.dark-mode .stat-card {
  background: #0f172a;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon .material-icons-outlined {
  font-size: 28px;
  font-family: 'Material Icons Outlined';
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 13px;
  color: #64748b;
  margin-bottom: 6px;
}

.stat-value {
  font-size: 24px;
  font-weight: 900;
  color: #1e293b;
}

.dark-mode .stat-value {
  color: #f1f5f9;
}

/* 설정 */
.settings-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: #f8fafc;
  border-radius: 12px;
}

.dark-mode .setting-item {
  background: #0f172a;
}

.setting-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.setting-info .material-icons-outlined {
  font-size: 24px;
  color: #64748b;
  font-family: 'Material Icons Outlined';
}

.setting-title {
  font-size: 15px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 4px;
}

.dark-mode .setting-title {
  color: #f1f5f9;
}

.setting-desc {
  font-size: 13px;
  color: #64748b;
}

/* 토글 스위치 */
.toggle-switch {
  position: relative;
  width: 52px;
  height: 28px;
}

.toggle-switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.toggle-slider {
  position: absolute;
  cursor: pointer;
  inset: 0;
  background-color: #cbd5e1;
  border-radius: 28px;
  transition: 0.3s;
}

.toggle-slider:before {
  position: absolute;
  content: "";
  height: 20px;
  width: 20px;
  left: 4px;
  bottom: 4px;
  background-color: white;
  border-radius: 50%;
  transition: 0.3s;
}

.toggle-switch input:checked + .toggle-slider {
  background-color: #FFD700;
}

.toggle-switch input:checked + .toggle-slider:before {
  transform: translateX(24px);
}

.lang-select {
  padding: 8px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
}

.dark-mode .lang-select {
  background: #0f172a;
  border-color: #334155;
  color: #f1f5f9;
}

.logout-btn {
  padding: 8px 16px;
  background: #fee2e2;
  border: 1px solid #fecaca;
  border-radius: 8px;
  color: #ef4444;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
}

.logout-btn:hover {
  background: #ef4444;
  border-color: #ef4444;
  color: white;
}
</style>