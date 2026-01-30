<script setup>
import { ref, computed, inject, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import SurveySelectModal from '@/components/common/SurveySelectModal.vue'
import { surveysApi } from '@/api'
import { useSurveyStore } from '@/stores/survey'

const router = useRouter()
const route = useRoute()
const surveyStore = useSurveyStore()
const isDarkMode = inject('isDarkMode', ref(false))

// 상태 관리
const currentStep = ref('type') 
const selectedType = ref(null)
const hoveredType = ref(null)
const showSurveySelectModal = ref(false)
const existingSurveys = ref([])
const selectedTopic = ref(null)
const activeSurveyId = ref(null) // 현재 선택된 설문 ID 추적

// 유형별 상세 정보 데이터
const practiceTypes = [
  {
    id: 'INTRO',
    name: '자기소개',
    icon: '👤',
    description: '자신을 소개하는 문제입니다.',
    details: {
      difficulty: {
        '1-2': '난이도 1-2: 1번 문제로 출제됩니다.',
        '3-4': '난이도 3-4: 1번 문제로 출제됩니다.',
        '5-6': '난이도 5-6: 1번 문제로 출제됩니다.'
      },
      info: '기본적인 자기소개로 시작하며, 모든 난이도에서 첫 번째 문제로 출제됩니다.'
    }
  },
  {
    id: 'COMBO',
    name: '콤보',
    icon: '🎯',
    description: '묘사, 루틴, 과거경험 등이 결합된 연속 문제입니다.',
    details: {
      difficulty: {
        '1-2': '난이도 1-2: 총 10문제 (콤보1~5, 각 2문제씩)',
        '3-4': '난이도 3-4: 총 9문제 (콤보1~3, 각 3문제씩)',
        '5-6': '난이도 5-6: 총 9문제 (콤보1~3, 각 3문제씩)'
      },
      info: '선택한 주제에 대해 묘사(현재), 루틴/비교, 과거경험 등을 연속으로 답변합니다. 묘사 → 루틴/비교 → 과거경험 순서로 난이도가 상승합니다.'
    }
  },
  {
    id: 'ROLEPLAY',
    name: '롤플레잉',
    icon: '🎭',
    description: '실제 상황을 가정한 문제 해결 유형입니다.',
    details: {
      difficulty: {
        '1-2': '난이도 1-2: 2문제 (정보요청 + Eva에게 질문)',
        '3-4': '난이도 3-4: 5문제 (정보요청 + 대안제시 + 과거경험 + 묘사 + 질문)',
        '5-6': '난이도 5-6: 3문제 (정보요청 + 대안제시 + 관련 과거경험)'
      },
      info: '정보요청(난이도 낮음) → 대안제시(난이도 높음) → 관련 과거경험(난이도 높음) 순으로 출제됩니다. 실제 상황에서의 문제해결 능력을 평가합니다.'
    }
  },
  {
    id: 'ADVANCED',
    name: '어드밴스',
    icon: '🚀',
    description: 'AL 등급을 위한 고난이도 문제입니다.',
    details: {
      difficulty: {
        '1-2': '난이도 1-2: 출제되지 않음',
        '3-4': '난이도 3-4: 출제되지 않음',
        '5-6': '난이도 5-6: 2문제 (비교/묘사/루틴 + 관련 이슈/의견)'
      },
      info: '난이도 5-6에서만 출제됩니다. 주제 관련 이슈, 뉴스, 의견 등을 구체적인 예시와 함께 설명해야 하는 고난이도 문제입니다.'
    }
  }
]

// 설문 데이터 (API 응답 구조 반영)
const surveyData = ref({
  topics: [],     // selected_topic -> topic (12개 이상)
  occupation: '', // 직업 (Part 1)
  hasJob: false,  // 직업 유무
  isStudent: false, // 학생 여부 (Part 2)
  residence: ''   // 거주지 (Part 3)
})

// ERD/API 참고용 데이터 로드 로직
const fetchExistingSurveys = async () => {
  try {
    console.log('[PracticeView] Fetching Existing Surveys...');
    const response = await surveysApi.getSurveyList();
    console.log('[PracticeView] Raw Survey List Response:', response.data);
    
    // 백엔드 응답 구조: { surveySummaryResponses: [...] } 또는 직접 배열
    let surveyList = response.data?.surveySummaryResponses || (Array.isArray(response.data) ? response.data : []);
    
    // 로컬 저장소 및 스토어에서 삭제된 ID 필터링
    existingSurveys.value = surveyStore.filterSurveys(surveyList).map(s => ({
      ...s,
      topics: s.topicList || []
    }));
    console.log('[PracticeView] Parsed Survey List (Filtered):', existingSurveys.value);
  } catch (error) {
    console.error("설문 목록 로드 실패", error);
  }
};

// 특정 설문 상세 조회
const fetchSurveyDetails = async (surveyId) => {
  console.log("11 설문 ID:", surveyId);
  try {
    console.log("22 설문 ID:", surveyId);
    const response = await surveysApi.getSurveyById(surveyId);
    const data = response.data;
    
    // 카테고리 매핑 (주관적 정의 혹은 DB 코드 연동)
    const categoryNames = {
      0: '배경 정보',
      1: '여가 활동',
      2: '취미/관심사',
      3: '운동/스포츠',
      4: '휴가/출장'
    };

    let groups = {
      0: { name: categoryNames[0], topics: [] },
      1: { name: categoryNames[1], topics: [] },
      2: { name: categoryNames[2], topics: [] },
      3: { name: categoryNames[3], topics: [] },
      4: { name: categoryNames[4], topics: [] }
    };

    // 1. 기본 토픽 (selectedTopics) 분류
    (data.selectedTopics || []).forEach(t => {
      let catId = t.categoryId || 1; // 기본값 여가
      
      // 배경 정보와 겹칠 수 있는 항목 강제 이동 (단순 문자열 매칭 등)
      const lowerName = t.topicName.toLowerCase();
      if (
        lowerName.includes('직장인') || 
        lowerName.includes('학생') || 
        lowerName.includes('거주') ||
        lowerName.includes('무직') ||
        lowerName.includes('구직')
      ) {
        catId = 0; // 배경 정보로 강제 할당
      }

      if (groups[catId]) {
        groups[catId].topics.push({
          topicId: t.topicId,
          name: t.topicName
        });
      }
    });

    // 2. Background 정보 가공
    // 사용자 피드백에 따라 설문 토픽 위주로만 표시

    // 빈 그룹 제거
    const finalGroups = Object.values(groups).filter(g => g.topics.length > 0);

    surveyData.value = {
      topicGroups: finalGroups,
      occupation: data.occupation,
      hasJob: data.hasJob,
      isStudent: data.student,
      residence: data.residence
    };
    
    selectedTopic.value = null; 
    currentStep.value = 'topic-check';
  } catch (error) {
    console.error("설문 상세 조회 실패:", error);
    alert("설문 정보를 불러오는데 실패했습니다.");
    currentStep.value = 'type';
  }
}


const selectType = (type) => {
  selectedType.value = type
  showSurveySelectModal.value = true
}

const startNewSurvey = () => {
  router.push({ path: '/survey', query: { from: 'practice', type: selectedType.value?.id } });
};

const useSelectedSurvey = async (surveyId) => {
  console.log('Use existing survey:', surveyId);
  activeSurveyId.value = surveyId; // 활성 설문 ID 저장
  await fetchSurveyDetails(surveyId);
  showSurveySelectModal.value = false;
};

const selectTopic = (topic) => {
  selectedTopic.value = topic;
}

const goToQuestionPage = () => {
  if (!selectedTopic.value) return;
  
  console.log('[PracticeView] Navigating to PracticeQuestionView', {
    type: selectedType.value?.id,
    topic: selectedTopic.value.topicId,
    surveyId: activeSurveyId.value
  });
  
  router.push({
    name: 'practice-question',
    query: { 
      type: selectedType.value?.id,
      topic: selectedTopic.value.topicId,
      topicName: selectedTopic.value.name,
      surveyId: activeSurveyId.value || route.query.surveyId // 활성 ID 또는 쿼리 ID 사용
    }
  });
};

const handleDeleteSurvey = (surveyId) => {
  // 1. 스토어 및 로컬 저장소에 삭제 반영
  surveyStore.deleteSurvey(surveyId);
  
  // 2. 현재 목록 UI 즉시 업데이트
  existingSurveys.value = surveyStore.filterSurveys(existingSurveys.value);
  
  console.log(`[PracticeView] Survey ${surveyId} deleted (Global FE Sync)`);
};

onMounted(async () => {
  await fetchExistingSurveys();

  // URL 쿼리 파라미터 확인 (설문 완료 후 돌아온 경우)
  const { type, surveyId } = route.query;
  
  if (type && surveyId) {
    // 1. 해당 Type 선택 상태 복구
    const targetType = practiceTypes.find(t => t.id === type);
    if (targetType) {
      selectedType.value = targetType;
    }

    // 2. 설문 상세 데이터 로드 및 주제 선택 화면으로 전환
    activeSurveyId.value = Number(surveyId); // 라우트에서 온 ID도 저장
    await useSelectedSurvey(Number(surveyId));
  }
});
</script>

<template>
  <div class="page-container">
    <main v-if="currentStep === 'type'" class="page-content">
      <h1 class="page-title">유형별 연습</h1>
      <div class="types-grid">
        <div 
          v-for="type in practiceTypes" 
          :key="type.id" 
          class="type-card"
          @mouseenter="hoveredType = type.id"
          @mouseleave="hoveredType = null"
          @click="selectType(type)"
        >
          <div class="type-icon">{{ type.icon }}</div>
          <h2 class="type-name">{{ type.name }}</h2>
          <p class="type-desc">{{ type.description }}</p>
          
          <div v-if="hoveredType === type.id" class="hover-details">
             <div class="diff-box">
               <p v-for="(txt, lv) in type.details.difficulty" :key="lv">{{ txt }}</p>
             </div>
             <p class="info-text">{{ type.details.info }}</p>
          </div>
        </div>
      </div>
    </main>

    <main v-else-if="currentStep === 'topic-check'" class="page-content">
      <h1 class="page-title">연습 주제 선택</h1>
      
      <div class="condition-card">
        <div v-for="group in surveyData.topicGroups" :key="group.name" class="topic-group-section">
          <div class="section-label">{{ group.name }}</div>
          <div class="tag-group">
            <button 
              v-for="t in group.topics" 
              :key="t.topicId" 
              class="topic-btn"
              :class="{ active: selectedTopic?.topicId === t.topicId }"
              @click="selectTopic(t)"
            >
              # {{ t.name }}
            </button>
          </div>
          <div class="section-divider"></div>
        </div>
      </div>
      
      <button 
        class="start-btn" 
        @click="goToQuestionPage"
        :disabled="!selectedTopic"
      >
        선택한 주제로 연습 시작
      </button>
    </main>

    <SurveySelectModal
      :isVisible="showSurveySelectModal"
      :existingSurveys="existingSurveys"
      @start-new="startNewSurvey"
      @use-selected="useSelectedSurvey"
      @delete-survey="handleDeleteSurvey"
      @close="showSurveySelectModal = false"
    />

  </div>
</template>

<style scoped>
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
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 40px;
  text-align: center;
}

.types-grid { 
  display: grid; 
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); 
  gap: 24px; 
}

.type-card { 
  background: var(--bg-secondary);
  border: var(--border-primary);
  border-radius: 24px;
  padding: 48px 32px;
  text-align: center;
  cursor: pointer;
  position: relative;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  height: 340px;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: var(--shadow-sm);
}

.type-card:hover { 
  transform: translateY(-8px);
  box-shadow: var(--shadow-lg);
  border-color: var(--primary-color);
}

.type-icon { font-size: 48px; margin-bottom: 20px; }

.type-name { 
  font-size: 1.5rem; 
  font-weight: 800; 
  margin-bottom: 12px; 
  color: var(--text-primary); 
}

.type-desc { 
  font-size: 0.95rem; 
  color: var(--text-secondary); 
  line-height: 1.5;
}

.hover-details { 
  position: absolute; 
  inset: 0; 
  background: var(--bg-secondary); 
  border-radius: 24px; 
  padding: 32px; 
  display: flex; 
  flex-direction: column; 
  justify-content: center; 
  z-index: 10;
  border: 2px solid var(--primary-color);
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: scale(0.95); }
  to { opacity: 1; transform: scale(1); }
}

.diff-box { 
  font-size: 0.8125rem; 
  text-align: left; 
  color: var(--text-primary); 
  margin-bottom: 16px; 
  border-bottom: 1px solid var(--border-primary); 
  padding-bottom: 16px; 
}

.diff-box p {
  margin-bottom: 4px;
}

.info-text { 
  font-size: 0.8125rem; 
  color: var(--text-secondary); 
  line-height: 1.6; 
  text-align: left; 
}

/* Topic Selection Styles */
.condition-card {
  background: var(--bg-secondary);
  border: var(--border-primary);
  border-radius: 24px;
  padding: 48px;
  box-shadow: var(--shadow-md);
}

.topic-group-section {
  margin-bottom: 40px;
}

.topic-group-section:last-child {
  margin-bottom: 0;
}

.section-label {
  font-size: 1.25rem;
  font-weight: 800;
  color: var(--text-primary);
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-label::before {
  content: "";
  display: block;
  width: 4px;
  height: 24px;
  background: var(--primary-color);
  border-radius: 2px;
}

.tag-group {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.topic-btn {
  padding: 10px 24px;
  border-radius: 12px;
  border: 1px solid var(--border-primary);
  background: var(--bg-tertiary);
  color: var(--text-secondary);
  font-weight: 600;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: var(--shadow-sm);
}

.topic-btn:hover:not(.active) {
  border-color: var(--primary-color);
  background: var(--bg-secondary);
  color: var(--primary-color);
  transform: translateY(-2px);
}

.topic-btn.active {
  background: var(--primary-color);
  border-color: var(--primary-color);
  color: #212529;
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.section-divider {
  height: 1px;
  background: var(--border-primary);
  margin-top: 40px;
}

.topic-group-section:last-child .section-divider {
  display: none;
}

.start-btn { 
  display: block; 
  width: 100%; 
  max-width: 480px; 
  margin: 60px auto 0; 
  padding: 20px 40px; 
  background: var(--primary-color); 
  border: none; 
  border-radius: 16px; 
  font-size: 1.125rem; 
  font-weight: 700; 
  color: #212529;
  cursor: pointer; 
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: var(--shadow-md);
}

.start-btn:hover:not(:disabled) {
  background: var(--primary-hover);
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.start-btn:disabled {
  background: var(--bg-tertiary);
  color: var(--text-tertiary);
  cursor: not-allowed;
  box-shadow: none;
  transform: none;
  opacity: 0.6;
}
</style>
