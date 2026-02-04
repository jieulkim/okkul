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

const emit = defineEmits(["close", "start-new", "use-selected", "use-recommended"]);

const selectedSurveyId = ref(null);
const isPreviewing = ref(false);
const previewData = ref(null);
const isRecommendedPreview = ref(false);

const recommendedSurvey = {
  occupationAnswerId: 4, // 일 경험 없음
  hasJob: null,
  workPeriodAnswerId: null,
  teachAnswerId: null,
  manager: null,
  student: false, // 학생 아니오
  classTypeAnswerId: 4, // 수강 후 5년 이상 지남
  residenceAnswerId: 1, // 개인주택이나 아파트에 홀로 거주
  leisure: [101, 106, 103, 104], // 영화 보기, 공원 가기, 공연 보기, 콘서트 보기
  hobby: [202], // 음악 감상하기
  exercise: [316, 317, 322], // 조깅, 걷기, 운동을 전혀 하지 않음
  holiday: [403, 404, 405], // 집에서 보내는 휴가, 국내 여행, 해외 여행
};

// 디버깅: 모달에 전달된 설문 데이터 확인
console.log('[SurveySelectModal] Received existingSurveys:', props.existingSurveys);

const formatDate = (dateString) => {
  if (!dateString) return "";
  return new Date(dateString).toLocaleDateString();
};

const handleStartNew = () => {
  emit("start-new");
};

const handleUseSelected = () => {
  if (selectedSurveyId.value) {
    emit("use-selected", selectedSurveyId.value);
  } else if (previewData.value && !isRecommendedPreview.value) {
    emit("use-selected", previewData.value.surveyId);
  }
};

const handleUseRecommended = () => {
  emit("use-recommended", recommendedSurvey);
};

const handlePreviewRecommended = () => {
  previewData.value = recommendedSurvey;
  isRecommendedPreview.value = true;
  isPreviewing.value = true;
};

const handlePreviewSurvey = (event, survey) => {
  event.stopPropagation();
  previewData.value = survey;
  isRecommendedPreview.value = false;
  isPreviewing.value = true;
};

const closePreview = () => {
  isPreviewing.value = false;
  previewData.value = null;
};

// Deletion logic removed as per requirements

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
  if (!val) return "정보 없음";
  const occMap = {
    1: "직장인", COMPANY: "직장인",
    2: "재택근무", HOME: "재택근무",
    3: "교육계", EDUCATION: "교육계",
    4: "무직/경험없음", NONE: "무직/경험없음",
    MILITARY: "군인"
  };
  return occMap[val] || val;
};

const getResidenceLabel = (val) => {
  if (!val) return "정보 없음";
  const resMap = {
    1: "1인 가구", ALONE: "1인 가구",
    2: "공동 거주", FRIENDS: "공동 거주",
    3: "가족 거주", FAMILY: "가족 거주",
    4: "기숙사", DORMITORY: "기숙사",
    5: "군대 막사", MILITARY: "군대 막사"
  };
  return resMap[val] || val;
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
  
  const filteredTopics = topics.filter(t => {
    const id = typeof t === "number" ? t : (t.topicId || t.id);
    return !(id >= 500 && id < 800);
  });

  if (filteredTopics.length === 0) return "선택된 주제 없음";

  const names = filteredTopics.map((t) => {
    if (typeof t === "string") return t;
    if (typeof t === "number") return topicMapping[t] || null;
    return t.topicName || t.name || topicMapping[t.topicId] || topicMapping[t] || t;
  });
  
  const validNames = names.filter(n => n && !n.includes('난이도'));
  
  if (validNames.length === 0) return "선택된 주제 없음";
  if (validNames.length <= 3) return validNames.join(", ");
  return `${validNames.slice(0, 3).join(", ")} 외 ${validNames.length - 3}개`;
};

const getCategorizedTopics = (topics) => {
  if (!topics) return { leisure: [], exercise: [], holiday: [] };
  
  const categorized = { leisure: [], exercise: [], holiday: [] };
  
  topics.forEach(t => {
    const id = typeof t === "number" ? t : (t.topicId || t.id);
    const name = typeof t === "string" ? t : (t.topicName || t.name || topicMapping[id]);
    
    if (!id || (id >= 500 && id < 800)) return;

    if ((id >= 100 && id < 300)) categorized.leisure.push(name);
    else if (id >= 300 && id < 400) categorized.exercise.push(name);
    else if (id >= 400 && id < 500) categorized.holiday.push(name);
  });
  
  return categorized;
};

const getCategorizedRecommendedTopics = (data) => {
  if (!data) return { leisure: [], exercise: [], holiday: [] };
  
  const leisureIds = [...(data.leisure || []), ...(data.hobby || [])];
  const exerciseIds = data.exercise || [];
  const holidayIds = data.holiday || [];

  return {
    leisure: leisureIds.map(id => topicMapping[id]).filter(Boolean),
    exercise: exerciseIds.map(id => topicMapping[id]).filter(Boolean),
    holiday: holidayIds.map(id => topicMapping[id]).filter(Boolean)
  };
};
</script>

<template>
  <div v-if="isVisible" class="modal-overlay">
    <div class="modal-card">
      <div v-if="!isPreviewing" class="list-view">
        <div class="modal-header">
          <button class="modal-close-btn" @click="$emit('close')" title="닫기">
            <span class="material-icons">close</span>
          </button>
          <h3>최근 설문 선택</h3>
          <p class="limit-warning">
            설문 3개 중 선택하세요
          </p>
        </div>

        <div class="survey-list-container">
          <div
            v-for="survey in existingSurveys"
            :key="survey.surveyId"
            class="survey-card-item"
            :class="{
              active: selectedSurveyId === survey.surveyId,
            }"
            @click="selectedSurveyId = survey.surveyId"
          >
            <div class="survey-info">
              <div class="card-title-row">
                <span class="date">{{ formatDate(survey.createdAt) }}</span>
                <button class="detail-link-btn" @click="handlePreviewSurvey($event, survey)">
                  상세보기 <span class="material-icons">chevron_right</span>
                </button>
              </div>
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
          >
            새 설문 작성
          </button>
          <button
            @click="handlePreviewRecommended"
            class="recommended-btn"
          >
            추천 설문 보기
          </button>
          <button
            v-if="selectedSurveyId"
            @click="handleUseSelected"
            class="primary-btn"
          >
            선택한 설문으로 시작
          </button>
          <button
            @click="$emit('close')"
            class="cancel-btn"
          >
            취소
          </button>
        </div>
      </div>

      <!-- 설문 상세 미리보기 화면 -->
      <div v-else class="preview-view animate-fade-in">
        <div class="modal-header">
          <button class="modal-close-btn" @click="closePreview" title="돌아가기">
            <span class="material-icons">arrow_back</span>
          </button>
          <h3>{{ isRecommendedPreview ? '추천 설문 상세 내용' : '설문 상세 내용' }}</h3>
          <p class="subtitle">
            {{ isRecommendedPreview ? '오꿀쌤이 제안하는 기본 설문 구성입니다.' : formatDate(previewData.createdAt) + '에 진행한 설문입니다.' }}
          </p>
        </div>

        <div class="preview-content">
          <section class="preview-section">
            <h4 class="section-title"><span class="material-icons">person</span> 배경 정보</h4>
            <div class="preview-tags">
              <span class="preview-tag">💼 {{ getOccupationLabel(previewData.occupation || previewData.occupationAnswerId) }}</span>
              <span class="preview-tag">🎓 {{ (previewData.student !== undefined && previewData.student !== null) ? (previewData.student ? "학생" : "비학생") : "정보 없음" }}</span>
              <span class="preview-tag">🏠 {{ getResidenceLabel(previewData.residence || previewData.residenceAnswerId) }}</span>
            </div>
          </section>

          <section class="preview-section">
            <h4 class="section-title">
              <span class="material-icons">auto_awesome</span> 
              선택 주제 
              <template v-if="isRecommendedPreview">
                (12개)
              </template>
              <template v-else-if="previewData.topics">
                ({{ previewData.topics.filter(t => {
                    const id = typeof t === 'number' ? t : (t.topicId || t.id);
                    return !(id >= 500 && id < 800);
                  }).length }}개)
              </template>
            </h4>
            <div class="preview-topics">
              <div class="topic-group" v-if="(isRecommendedPreview ? getCategorizedRecommendedTopics(previewData) : getCategorizedTopics(previewData.topics)).leisure.length > 0">
                <label>여가/취미</label>
                <p>{{ (isRecommendedPreview ? getCategorizedRecommendedTopics(previewData) : getCategorizedTopics(previewData.topics)).leisure.join(', ') }}</p>
              </div>
              <div class="topic-group" v-if="(isRecommendedPreview ? getCategorizedRecommendedTopics(previewData) : getCategorizedTopics(previewData.topics)).exercise.length > 0">
                <label>운동</label>
                <p>{{ (isRecommendedPreview ? getCategorizedRecommendedTopics(previewData) : getCategorizedTopics(previewData.topics)).exercise.join(', ') }}</p>
              </div>
              <div class="topic-group" v-if="(isRecommendedPreview ? getCategorizedRecommendedTopics(previewData) : getCategorizedTopics(previewData.topics)).holiday.length > 0">
                <label>휴가/여행</label>
                <p>{{ (isRecommendedPreview ? getCategorizedRecommendedTopics(previewData) : getCategorizedTopics(previewData.topics)).holiday.join(', ') }}</p>
              </div>
            </div>
          </section>
        </div>

        <div class="modal-footer">
          <button v-if="isRecommendedPreview" @click="handleUseRecommended" class="primary-btn action-btn">
            이 설문으로 시작하기
          </button>
          <button v-else @click="handleUseSelected" class="primary-btn action-btn">
            이 설문으로 시작하기
          </button>
          <button @click="closePreview" class="cancel-btn">
            돌아가기
          </button>
        </div>
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
  background: #FFFFFF;
  border-radius: var(--radius-lg);
  max-width: 700px;
  width: 90%;
  border: 1px solid rgba(0,0,0,0.1);
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.2);
  padding-bottom: 24px;
  position: relative;
  z-index: 1001;
  display: block;
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
  color: #0f172a;
}

.modal-header h3 {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 8px;
  color: var(--text-primary);
}

.subtitle {
  font-size: 15px;
  color: #64748b;
  margin: 0;
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

.survey-list-container {
  padding: 0 32px 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 600px;
  overflow-y: visible;
}

.survey-card-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border-radius: 12px;
  border: 1px solid rgba(0,0,0,0.05);
  background: #fffcf0; /* Subtle warm honey background for the "card" itself */
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 4px 6px rgba(0,0,0,0.02);
}

.survey-card-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(0,0,0,0.05);
  background: #fff9e6;
}

.survey-card-item.active {
  border-color: #ffd600 !important;
  background: #fffde7 !important;
  color: #000000;
  box-shadow: 0 0 0 1px #ffd600;
}

.survey-info {
  flex: 1;
  min-width: 0;
}

.card-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.detail-link-btn {
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  color: #2563eb;
  font-size: 11px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  gap: 1px;
  cursor: pointer;
  padding: 2px 6px;
  border-radius: 6px;
  transition: all 0.2s;
  width: fit-content;
  flex: 0 0 auto;
}

.detail-link-btn:hover {
  background: #dbeafe;
  border-color: #93c5fd;
}

.detail-link-btn .material-icons {
  font-size: 16px;
}

.date {
  font-size: 14px;
  font-weight: 600;
  display: block;
}

.tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: 6px;
}

.tag {
  font-size: 11px;
  background: #FFFFFF;
  padding: 3px 8px;
  border-radius: 6px;
  color: #64748b;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 3px;
  border: 1px solid #eef2f7;
}

.level-tag {
  background: #fffbeb !important;
  color: #b45309 !important;
  border-color: #fde68a !important;
}

.radio-circle {
  width: 20px;
  height: 20px;
  border: 2px solid #cbd5e1;
  border-radius: 50%;
  position: relative;
  flex-shrink: 0;
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
  flex-shrink: 0;
}

.modal-footer {
  padding: 0 32px 10px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: center;
}

button {
  flex-grow: 1;
  padding: 14px;
  border-radius: 12px;
  border: none;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}

.secondary-btn {
  background: #f1f5f9;
  color: #64748b;
}

.secondary-btn:hover {
  background: #fff9c4;
  color: #bf360c;
}

.recommended-btn {
  background: #dcfce7;
  color: #166534;
  border: 1px solid #86efac;
}

.recommended-btn:hover {
  background: #bbf7d0;
  border-color: #4ade80;
}

.primary-btn {
  background: #fff9c4;
  color: #bf360c;
  border: 2px solid #ffd54f;
}

.cancel-btn {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  color: #475569;
}

.cancel-btn:hover {
  background: #f1f5f9;
  border-color: #cbd5e1;
}

.topic-summary-text {
  font-size: 12px;
  color: #94a3b8;
  font-weight: 600;
}

.preview-content {
  padding: 0 32px 24px;
}

.preview-section {
  margin-bottom: 24px;
  background: #f8fafc;
  padding: 20px;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 12px;
}

.section-title .material-icons {
  color: #64748b;
  font-size: 20px;
}

.preview-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.preview-tag {
  background: #FFFFFF;
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #475569;
  border: 1px solid #e2e8f0;
}

.topic-group {
  margin-bottom: 12px;
}

.topic-group label {
  display: block;
  font-size: 12px;
  font-weight: 700;
  color: #94a3b8;
  margin-bottom: 4px;
}

.topic-group p {
  font-size: 14px;
  color: #475569;
  line-height: 1.5;
  margin: 0;
}

.animate-fade-in {
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.action-btn {
  padding: 14px 40px;
}
</style>