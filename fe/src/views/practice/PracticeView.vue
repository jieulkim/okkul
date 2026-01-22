<script setup>
import { ref, computed, inject } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const isDarkMode = inject('isDarkMode', ref(false))

// 상태 관리
const currentStep = ref('type') 
const selectedType = ref(null)
const hoveredType = ref(null)
const showTopicConfirmPopup = ref(false)

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

// 가상 설문 데이터 (실제 데이터는 API/Store 연동)
const surveyData = ref({
  job: '직업 있음',
  student: '학생 아님',
  dwelling: '가족과 함께 거주',
  hobbies: ['영화보기', '공원가기', '카페가기', '음악감상', '조깅', '걷기', '국내여행', '술집가기', 'TV시청', '독서', '요리하기', '쇼핑하기']
})

const selectType = (type) => {
  selectedType.value = type
  showTopicConfirmPopup.value = true
}

const goToQuestionPage = () => {
  router.push({
    name: 'practice-question',
    state: {
      typeId: selectedType.value.id,
      typeName: selectedType.value.name,
      surveyHobbies: surveyData.value.hobbies
    }
  })
}
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
      <h1 class="page-title">나의 연습 조건 확인</h1>
      <div class="condition-card">
        <div class="section-label">선택한 주제 (취미/여가)</div>
        <div class="tag-group">
          <span v-for="h in surveyData.hobbies" :key="h" class="hobby-tag"># {{ h }}</span>
        </div>
        <div class="status-row">
          <div class="status-box"><span>직업</span><br><b>{{ surveyData.job }}</b></div>
          <div class="status-box"><span>학생</span><br><b>{{ surveyData.student }}</b></div>
          <div class="status-box"><span>거주</span><br><b>{{ surveyData.dwelling }}</b></div>
        </div>
      </div>
      <button class="start-btn" @click="goToQuestionPage">이 조건으로 연습 시작 🚀</button>
    </div>

    <div v-if="showTopicConfirmPopup" class="popup-overlay">
      <div class="popup-content">
        <div class="okkul">🐷</div>
        <h3>기존 설문 데이터를 사용하시겠습니까?</h3>
        <div class="btns">
          <button @click="router.push('/survey')">새로 작성</button>
          <button class="primary" @click="showTopicConfirmPopup = false; currentStep = 'topic-check'">네, 그대로 사용</button>
        </div>
      </div>
    </div>

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

.hobby-tag { display: inline-block; padding: 6px 12px; background: #f1f5f9; border-radius: 50px; margin: 4px; font-size: 13px; font-weight: 600; }
.status-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; margin-top: 20px; }
.status-box { background: #fffef0; border: 2px solid #FFD700; padding: 15px; border-radius: 15px; font-size: 14px; }

.start-btn { 
  display: block; width: 300px; margin: 40px auto; padding: 20px; background: #FFD700; 
  border: 2px solid #000; border-radius: 50px; font-size: 18px; font-weight: 900; cursor: pointer; box-shadow: 0 4px 0 #000;
}
.popup-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.6); display: flex; align-items: center; justify-content: center; z-index: 100; }
.popup-content { background: white; padding: 40px; border-radius: 30px; text-align: center; width: 400px; }
.btns { display: flex; gap: 10px; margin-top: 25px; }
.btns button { flex: 1; padding: 15px; border-radius: 12px; border: none; cursor: pointer; font-weight: bold; }
.btns button.primary { background: #FFD700; }

/* 다크모드 간단 대응 */
.dark-mode .type-card { background: #1e293b; border-color: #334155; color: white; }
.dark-mode .hover-details { background: rgba(30, 41, 59, 0.98); }
</style>