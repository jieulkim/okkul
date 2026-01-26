<script setup>
import { ref, computed, inject, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import SurveySelectModal from '@/components/common/SurveySelectModal.vue'
import { Surveys } from '@/api/Surveys'

const router = useRouter()
const route = useRoute()
const isDarkMode = inject('isDarkMode', ref(false))

// 상태 관리
const currentStep = ref('type') 
const selectedType = ref(null)
const hoveredType = ref(null)
const showSurveySelectModal = ref(false)
const existingSurveys = ref([])
const selectedTopic = ref(null)

// [사용자 요청] 유형별 상세 정보 데이터
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

// ERD/API 참고용 데이터 로드 로직 (ExamView와 동일하게 유지)
const fetchExistingSurveys = async () => {
  try {
    // 실제 구현 시: const { data } = await axios.get('/api/surveys/me')
    // 현재는 더미 데이터를 ERD 구조에 맞춰 유지
    existingSurveys.value = [
      { surveyId: 101, createdAt: '2026-01-21T14:00:00', level: 5, occupation: '직장인', topics: [1, 5, 12] },
      { surveyId: 102, createdAt: '2026-01-25T09:30:00', level: 4, occupation: '학생', topics: [2, 8, 15] }
    ];
  } catch (error) {
    console.error("설문 목록 로드 실패", error);
  }
};

// 특정 설문 상세 조회
const fetchSurveyDetails = async (surveyId) => {
  try {
    const surveysApi = new Surveys();
    const response = await surveysApi.getSurveyById(surveyId);
    const data = response.data;
    
    // 1. 기본 토픽 (selectedTopics)
    let combinedTopics = (data.selectedTopics || []).map(t => ({
      topicId: t.topicId,
      name: t.topicName
    }));

    // 2. Background Survey 항목을 토픽으로 추가
    // Occupation (직업)
    if (data.occupation) {
      // API에서 occupation이 어떤 형태로 오는지 확인 필요 (여기서는 string 가정)
      // 실제 API 응답값이 "COMPANY" 등 코드라면 한글 변환 필요할 수도 있음.
      // 현재는 받은 값 그대로 Topic으로 추가
      combinedTopics.unshift({
        topicId: -1, // 임시 ID (백엔드가 어떻게 처리하냐에 따라 다름, 여기서는 UI 표시용)
        name: data.occupation,
        type: 'background' // 구분용
      });
    }

    // Residence (거주지)
    if (data.residence) {
      combinedTopics.unshift({
        topicId: -2,
        name: data.residence,
        type: 'background'
      });
    }

    // Student (학생)
    if (data.student) {
       combinedTopics.unshift({
        topicId: -3,
        name: "학생",
        type: 'background'
      });
    }

    surveyData.value = {
      topics: combinedTopics,
      occupation: data.occupation,
      hasJob: data.hasJob,
      isStudent: data.student,
      residence: data.residence
    };
    
    selectedTopic.value = null; // 초기화
  } catch (error) {
    console.error("설문 상세 조회 실패", error);
    alert("설문 정보를 불러오는데 실패했습니다.");
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
  await fetchSurveyDetails(surveyId);
  showSurveySelectModal.value = false;
  currentStep.value = 'topic-check';
};

const selectTopic = (topic) => {
  selectedTopic.value = topic;
}

const goToQuestionPage = () => {
  if (!selectedTopic.value) return;
  
  router.push({
    name: 'practice-question',
    query: { 
      type: selectedType.value?.id,
      topic: selectedTopic.value.topicId,
      topicName: selectedTopic.value.name
    }
  });
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
    await useSelectedSurvey(Number(surveyId));
  }
});
</script>

<template>
  <div class="practice-page" :class="{ 'dark-mode': isDarkMode }">
    
    <div v-if="currentStep === 'type'" class="container">
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
    </div>

    <div v-else-if="currentStep === 'topic-check'" class="container">
      <h1 class="page-title">연습 주제 선택</h1>
      
      <div class="condition-card">
        <div class="section-top">
          <div class="section-label">주제 선택 (취미/여가)</div>
          <p class="section-desc">연습하고 싶은 주제를 하나 선택해주세요.</p>
          <div class="tag-group">
            <button 
              v-for="t in surveyData.topics" 
              :key="t.topicId" 
              class="topic-btn"
              :class="{ active: selectedTopic?.topicId === t.topicId }"
              @click="selectTopic(t)"
            >
              # {{ t.name }}
            </button>
          </div>
        </div>

        <div class="section-divider"></div>

        <div class="section-bottom">
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">직업</span>
              <span class="info-value">{{ surveyData.hasJob ? '있음' : '없음' }} ({{ surveyData.occupation }})</span>
            </div>
            <div class="info-item">
              <span class="info-label">학생</span>
              <span class="info-value">{{ surveyData.isStudent ? '학생임' : '아님' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">거주</span>
              <span class="info-value">{{ surveyData.residence }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <button 
        class="start-btn" 
        @click="goToQuestionPage"
        :disabled="!selectedTopic"
      >
        선택한 주제로 연습 시작 🚀
      </button>
    </div>

    <SurveySelectModal
      :isVisible="showSurveySelectModal"
      :existingSurveys="existingSurveys"
      @start-new="startNewSurvey"
      @use-selected="useSelectedSurvey"
      @close="showSurveySelectModal = false"
    />

  </div>
</template>

<style scoped>
.practice-page { min-height: 100vh; background: #f8fafc; padding: 60px 20px; }
.container { max-width: 1100px; margin: 0 auto; }
.page-title { text-align: center; font-size: 32px; font-weight: 900; margin-bottom: 40px; }

.types-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); gap: 20px; }
.type-card { 
  background: white; border-radius: 24px; padding: 40px 20px; text-align: center; border: 2px solid #e2e8f0; 
  cursor: pointer; position: relative; transition: all 0.3s; height: 320px; display: flex; flex-direction: column; align-items: center;
}
.type-card:hover { border-color: #FFD700; transform: translateY(-5px); }
.type-icon { font-size: 50px; margin-bottom: 15px; }
.type-name { font-size: 22px; font-weight: 800; margin-bottom: 10px; }
.type-desc { font-size: 14px; color: #64748b; }

.hover-details { 
  position: absolute; inset: 0; background: rgba(255, 255, 255, 0.96); border-radius: 24px; 
  padding: 20px; display: flex; flex-direction: column; justify-content: center; z-index: 10;
}
.diff-box { font-size: 12px; text-align: left; color: #1e293b; margin-bottom: 10px; border-bottom: 1px solid #eee; padding-bottom: 10px; }
.info-text { font-size: 12px; color: #64748b; line-height: 1.5; text-align: left; }

/* Topic Selection Styles */
.condition-card {
  background: white;
  border-radius: 24px;
  padding: 40px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}

.section-label {
  font-size: 18px;
  font-weight: 800;
  color: #1e293b;
  margin-bottom: 8px;
}

.section-desc {
  color: #64748b;
  font-size: 14px;
  margin-bottom: 24px;
}

.tag-group {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 30px;
}

.topic-btn {
  padding: 10px 20px;
  border-radius: 50px;
  border: 2px solid #e2e8f0;
  background: #fff;
  color: #64748b;
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.topic-btn:hover {
  border-color: #FFD700;
  color: #d97706;
}

.topic-btn.active {
  background: #fffef0;
  border-color: #FFD700;
  color: #d97706;
  box-shadow: 0 0 0 2px rgba(255, 215, 0, 0.2);
}

.section-divider {
  height: 1px;
  background: #e2e8f0;
  margin: 30px 0;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.info-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px;
  background: #f8fafc;
  border-radius: 16px;
}

.info-label {
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
}

.info-value {
  font-size: 16px;
  font-weight: 700;
  color: #1e293b;
}

.start-btn { 
  display: block; width: 100%; max-width: 400px; margin: 40px auto 0; padding: 20px; 
  background: #FFD700; border: none; border-radius: 16px; 
  font-size: 18px; font-weight: 800; color: #1e293b;
  cursor: pointer; transition: all 0.2s;
  box-shadow: 0 4px 12px rgba(255, 215, 0, 0.3);
}

.start-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  background: #ffc800;
}

.start-btn:disabled {
  background: #cbd5e1;
  color: #94a3b8;
  cursor: not-allowed;
  box-shadow: none;
}

/* 다크모드 간단 대응 */
.dark-mode .type-card { background: #1e293b; border-color: #334155; color: white; }
.dark-mode .hover-details { background: rgba(30, 41, 59, 0.98); }
.dark-mode .condition-card { background: #1e293b; }
.dark-mode .section-label, .dark-mode .info-value { color: #f1f5f9; }
.dark-mode .topic-btn { background: #0f172a; border-color: #334155; color: #94a3b8; }
.dark-mode .topic-btn.active { background: #422006; border-color: #FFD700; color: #fbbf24; }
.dark-mode .info-item { background: #0f172a; }
</style>