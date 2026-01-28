<script setup>
import { ref, defineProps, defineEmits, inject } from "vue";

const props = defineProps({
  isVisible: {
    type: Boolean,
    required: true,
  },
  existingSurveys: {
    type: Array,
    default: () => [],
  },
});

const emit = defineEmits(["close", "start-new", "use-selected", "delete-survey"]);

const isDarkMode = inject("isDarkMode", ref(false));
const selectedSurveyId = ref(null);

// 디버깅: 모달에 전달된 설문 데이터 확인
console.log('[SurveySelectModal] Received existingSurveys:', props.existingSurveys);

const formatDate = (dateString) => {
  return new Date(dateString).toLocaleDateString();
};

const handleStartNew = () => {
  emit("start-new");
};

const handleUseSelected = () => {
  if (selectedSurveyId.value) {
    emit("use-selected", selectedSurveyId.value);
  }
};

const handleDeleteSurvey = (event, surveyId) => {
  event.stopPropagation(); // 카드 선택 방지
  if (confirm("이 설문 데이터를 삭제하시겠습니까?")) {
    emit("delete-survey", surveyId);
    if (selectedSurveyId.value === surveyId) {
      selectedSurveyId.value = null;
    }
  }
};

const labels = {
  occupation: {
    COMPANY: "직장인",
    HOME: "재택근무",
    EDUCATION: "교육계",
    NONE: "무직/경험없음",
    MILITARY: "군인",
  },
  residence: {
    ALONE: "1인 가구",
    FRIENDS: "공동 거주",
    FAMILY: "가족 거주",
    DORMITORY: "기숙사",
    MILITARY: "군대 막사",
  },
};

const getOccupationLabel = (val) => {
  if (!val) return null;
  const occMap = {
    1: "직장인",
    2: "재택근무",
    3: "교육계",
    4: "무직/경험없음"
  };
  return occMap[val] || labels.occupation[val] || val;
};

const getResidenceLabel = (val) => {
  if (!val) return null;
  // Handle numeric IDs if they come as numbers
  const resMap = {
    1: "1인 가구",
    2: "공동 거주",
    3: "가족 거주",
    4: "기숙사",
    5: "군대 막사",
  };
  return resMap[val] || labels.residence[val] || val;
};

const topicMapping = {
  101: "영화보기", 102: "클럽/나이트클럽 가기", 103: "공연보기", 104: "콘서트보기", 
  105: "박물관가기", 106: "공원가기", 107: "캠핑하기", 108: "해변가기", 
  109: "스포츠 관람", 110: "주거 개선",
  201: "아이에게 책 읽어주기", 202: "음악 감상하기", 203: "악기 연주하기", 
  204: "혼자 노래부르거나 합창하기", 205: "춤추기", 206: "글쓰기", 207: "그림 그리기", 
  208: "요리하기", 209: "애완동물 기르기",
  301: "농구", 302: "야구/소프트볼", 303: "축구", 304: "미식축구", 305: "하키", 
  306: "크리켓", 307: "골프", 308: "배구", 309: "테니스", 310: "배드민턴", 
  311: "탁구", 312: "수영", 313: "자전거", 314: "스키/스노우보드", 
  315: "아이스 스케이트", 316: "조깅", 317: "걷기", 318: "요가", 
  319: "하이킹/트레킹", 320: "낚시", 321: "헬스", 322: "운동 안 함",
  401: "국내출장", 402: "해외출장", 403: "집 휴가", 404: "국내 여행", 405: "해외 여행"
};

const getTopicsSummary = (topics) => {
  if (!topics || topics.length === 0) return "선택된 주제 없음";
  
  const names = topics.map((t) => {
    if (typeof t === "string") return t;
    if (typeof t === "number") return topicMapping[t] || `토픽 ${t}`;
    return t.topicName || t.name || topicMapping[t.topicId] || topicMapping[t] || "알 수 없는 주제";
  });
  
  const validNames = names.filter(n => n && !n.includes('난이도'));
  
  if (validNames.length <= 3) return validNames.join(", ");
  return `${validNames.slice(0, 3).join(", ")} 외 ${validNames.length - 3}개`;
};
</script>

<template>
  <div v-if="isVisible" class="modal-overlay">
    <div class="modal-card" :class="{ 'dark-mode-card': isDarkMode }">
      <div class="modal-header">
        <button class="modal-close-btn" @click="$emit('close')" title="닫기">
          <span class="material-icons">close</span>
        </button>
        <h3>기존 설문 데이터 선택</h3>
        <p class="subtitle">
          이전에 완료한 설문을 사용하여 바로 시작할 수 있습니다.
        </p>
        <p v-if="existingSurveys.length >= 3" class="limit-warning">
          ⚠️ 설문은 최대 3개까지만 저장 가능합니다. (새 설문을 위해 기존 데이터를 삭제해주세요)
        </p>
      </div>

      <div class="survey-list-container">
        <div
          v-for="survey in existingSurveys"
          :key="survey.surveyId"
          class="survey-card-item"
          :class="{
            active: selectedSurveyId === survey.surveyId,
            'dark-mode-item': isDarkMode,
          }"
          @click="selectedSurveyId = survey.surveyId"
        >
          <div class="survey-info">
            <span class="date">{{ formatDate(survey.createdAt) }}</span>
            <div class="tags">
              <span class="tag level-tag">난이도 {{ survey.level }}</span>
              <span class="tag" v-if="survey.occupation && survey.occupation !== 'N/A'">
                💼 {{ getOccupationLabel(survey.occupation) }}
              </span>
              <span class="tag" v-if="survey.student !== null && survey.student !== undefined">
                🎓 {{ survey.student ? "학생" : "비학생" }}
              </span>
              <span class="tag" v-if="survey.residence">
                🏠 {{ getResidenceLabel(survey.residence) }}
              </span>
            </div>
            <!-- 주제 미리보기 (요약형) -->
            <div
              class="topics-preview"
              v-if="survey.topics && survey.topics.length > 0"
            >
              <span class="topic-summary-text">
                {{ getTopicsSummary(survey.topics) }}
              </span>
            </div>
          </div>
          <div class="card-actions">
            <button class="delete-icon-btn" @click="handleDeleteSurvey($event, survey.surveyId)" title="삭제">
              <span class="material-icons">delete_outline</span>
            </button>
            <div
              class="radio-circle"
              :class="{ selected: selectedSurveyId === survey.surveyId }"
            ></div>
          </div>
        </div>
      </div>

      <div class="modal-footer">
        <button
          @click="handleStartNew"
          class="secondary-btn"
          :class="{ 'dark-mode-btn': isDarkMode }"
          :disabled="existingSurveys.length >= 3"
        >
          {{ existingSurveys.length >= 3 ? '저장 용량 초과' : '새 설문 작성' }}
        </button>
        <button
          @click="handleUseSelected"
          class="primary-btn"
          :disabled="!selectedSurveyId"
        >
          선택한 설문으로 시작
        </button>
        <button
          @click="$emit('close')"
          class="cancel-btn"
          :class="{ 'dark-mode-btn': isDarkMode }"
        >
          취소
        </button>
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
  position: relative;
}

.modal-close-btn {
  position: absolute;
  top: 20px;
  right: 20px;
  background: none;
  border: none;
  color: #94a3b8;
  cursor: pointer;
  padding: 8px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.modal-close-btn:hover {
  background: #f1f5f9;
  color: #1e293b;
}

.dark-mode-card .modal-close-btn:hover {
  background: #334155;
  color: #f1f5f9;
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

.limit-warning {
  margin-top: 12px;
  font-size: 13px;
  color: #ef4444;
  font-weight: 700;
  background: #fef2f2;
  padding: 8px 12px;
  border-radius: 8px;
  display: inline-block;
}

.dark-mode-card .limit-warning {
  background: rgba(239, 68, 68, 0.1);
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
  border-color: #ffd700 !important;
  background: #fffef0;
}

.dark-mode-item.active {
  background: #422006 !important;
  border-color: #ffd700 !important;
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
  font-size: 11px;
  background: rgba(0, 0, 0, 0.04);
  padding: 4px 10px;
  border-radius: 6px;
  color: #475569;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.dark-mode-item .tag {
  background: rgba(255, 255, 255, 0.08);
  color: #cbd5e1;
  border-color: rgba(255, 255, 255, 0.1);
}

.level-tag {
  background: #fffbeb !important;
  color: #b45309 !important;
  border-color: #fde68a !important;
}

.dark-mode-item .level-tag {
  background: rgba(251, 191, 36, 0.1) !important;
  color: #fbbf24 !important;
  border-color: rgba(251, 191, 36, 0.2) !important;
}

.radio-circle {
  width: 20px;
  height: 20px;
  border: 2px solid #cbd5e1;
  border-radius: 50%;
  position: relative;
}

.radio-circle.selected {
  border-color: #ffd700;
  background: #ffd700;
}

.radio-circle.selected::after {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 8px;
  height: 8px;
  background: white;
  border-radius: 50%;
}

.card-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.delete-icon-btn {
  background: none;
  border: none;
  padding: 4px;
  color: #94a3b8;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  border-radius: 6px;
}

.delete-icon-btn:hover {
  background: #fee2e2;
  color: #ef4444;
}

.dark-mode-item .delete-icon-btn:hover {
  background: rgba(239, 68, 68, 0.2);
}

.delete-icon-btn .material-icons {
  font-size: 20px;
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
  background: #ffd700;
  color: #1e293b;
}

.primary-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.cancel-btn {
  background: #f8fafc;
  border: 1.5px solid #e2e8f0;
  color: #64748b;
  transition: all 0.2s;
}

.cancel-btn:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
  color: #1e293b;
}

.dark-mode-btn.cancel-btn {
  background: #0f172a;
  border-color: #334155;
  color: #94a3b8;
}

.dark-mode-btn.cancel-btn:hover {
  background: #1e293b;
  color: #f1f5f9;
}

/* 주제 및 상세 태그 스타일 */
.level-tag {
  background: #fef3c7 !important;
  color: #92400e !important;
}

.topics-preview {
  margin-top: 8px;
}

.topic-summary-text {
  font-size: 13px;
  color: #64748b;
  font-weight: 600;
  letter-spacing: -0.2px;
}

.dark-mode-item .topic-summary-text {
  color: #94a3b8;
}
</style>
