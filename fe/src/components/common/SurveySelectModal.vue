<script setup>
import { ref, defineProps, defineEmits, inject } from 'vue';

const props = defineProps({
  isVisible: {
    type: Boolean,
    required: true
  },
  existingSurveys: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['close', 'start-new', 'use-selected']);

const isDarkMode = inject('isDarkMode', ref(false));
const selectedSurveyId = ref(null);

const formatDate = (dateString) => {
  return new Date(dateString).toLocaleDateString();
};

const handleStartNew = () => {
  emit('start-new');
};

const handleUseSelected = () => {
  if (selectedSurveyId.value) {
    emit('use-selected', selectedSurveyId.value);
  }
};
</script>

<template>
  <div v-if="isVisible" class="modal-overlay">
    <div class="modal-card" :class="{ 'dark-mode-card': isDarkMode }">
      <div class="modal-header">
        <h3>기존 설문 데이터 선택</h3>
        <p class="subtitle">이전에 완료한 설문을 사용하여 바로 시작할 수 있습니다.</p>
      </div>
      
      <div class="survey-list-container">
        <div 
          v-for="survey in existingSurveys" 
          :key="survey.surveyId"
          class="survey-card-item"
          :class="{ active: selectedSurveyId === survey.surveyId, 'dark-mode-item': isDarkMode }"
          @click="selectedSurveyId = survey.surveyId"
        >
          <div class="survey-info">
            <span class="date">{{ formatDate(survey.createdAt) }}</span>
            <div class="tags">
              <span class="tag">난이도 {{ survey.level }}</span>
              <span class="tag">{{ survey.occupation }}</span>
            </div>
          </div>
          <div class="radio-circle" :class="{ selected: selectedSurveyId === survey.surveyId }"></div>
        </div>
      </div>

      <div class="modal-footer">
        <button @click="handleStartNew" class="secondary-btn" :class="{ 'dark-mode-btn': isDarkMode }">새 설문 작성</button>
        <button 
          @click="handleUseSelected" 
          class="primary-btn" 
          :disabled="!selectedSurveyId"
        >선택한 설문으로 시작</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.modal-card {
  background: white;
  border-radius: 24px;
  max-width: 600px;
  width: 90%;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  padding-bottom: 20px;
}

.dark-mode-card {
  background: #1e293b;
  color: #f1f5f9;
}

.modal-header {
  padding: 32px 32px 16px;
  text-align: center;
}

.modal-header h3 {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 8px;
  color: #1e293b;
}

.dark-mode-card .modal-header h3 {
  color: #f1f5f9;
}

.subtitle {
  font-size: 15px;
  color: #64748b;
  margin: 0;
}

.dark-mode-card .subtitle {
  color: #94a3b8;
}

.survey-list-container {
  padding: 0 32px 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 400px;
  overflow-y: auto;
}

.survey-card-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border-radius: 12px;
  border: 2px solid #e2e8f0;
  background: #f8f9fa;
  cursor: pointer;
  transition: all 0.2s;
}

.dark-mode-item {
  background: #0f172a !important;
  border-color: #334155 !important;
  color: #f1f5f9;
}

.survey-card-item:hover {
  border-color: rgba(255, 215, 0, 0.5);
}

.survey-card-item.active {
  border-color: #FFD700 !important;
  background: #fffef0;
}

.dark-mode-item.active {
  background: #422006 !important;
  border-color: #FFD700 !important;
}

.survey-info {
  flex: 1;
}

.date {
  font-size: 14px;
  font-weight: 600;
  display: block;
  margin-bottom: 8px;
}

.tags {
  display: flex;
  gap: 8px;
}

.tag {
  font-size: 12px;
  background: rgba(0,0,0,0.05);
  padding: 4px 8px;
  border-radius: 4px;
  color: #64748b;
}

.dark-mode-item .tag {
  background: rgba(255,255,255,0.1);
  color: #94a3b8;
}

.radio-circle {
  width: 20px;
  height: 20px;
  border: 2px solid #cbd5e1;
  border-radius: 50%;
  position: relative;
}

.radio-circle.selected {
  border-color: #FFD700;
  background: #FFD700;
}

.radio-circle.selected::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 8px;
  height: 8px;
  background: white;
  border-radius: 50%;
}

.modal-footer {
  padding: 0 32px 10px;
  display: flex;
  gap: 12px;
}

button {
  flex: 1;
  padding: 14px;
  border-radius: 12px;
  border: none;
  font-weight: 700;
  cursor: pointer;
}

.secondary-btn {
  background: #f1f5f9;
  color: #64748b;
}

.dark-mode-btn {
  background: #334155;
  color: #f1f5f9;
}

.primary-btn {
  background: #FFD700;
  color: #1e293b;
}

.primary-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
