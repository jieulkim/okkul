<script setup>
import { ref, inject } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const isDarkMode = inject('isDarkMode', ref(false))

// 최대 3개 설문 데이터 (예시)
const savedSurveys = ref([
  { id: 1, title: '최근 설문 (Level 4)', date: '2026-01-20' },
  { id: 2, title: '비즈니스 연습 (Level 5)', date: '2026-01-18' }
])

const showModal = ref(false)

// 기존 설문 선택 시 -> 바로 셋업으로
const useSurvey = (id) => {
  router.push('/exam/setup')
}

// 새 설문 시작 시 -> 출처(exam)와 타겟 슬롯 전달
const startNewSurvey = (slotId = null) => {
  const targetSlot = slotId || (savedSurveys.value.length + 1)
  router.push({ 
    path: '/survey', 
    query: { from: 'exam', slot: targetSlot } 
  })
}
</script>

<template>
  <div class="exam-container" :class="{ 'dark-mode': isDarkMode }">
    <div class="header">
      <h1>실전 모의고사</h1>
      <p>실제 시험과 동일한 환경에서 연습합니다.</p>
    </div>

    <div class="action-card">
      <button @click="showModal = true" class="main-btn">시험 시작하기</button>
    </div>

    <div v-if="showModal" class="modal-overlay" @click="showModal = false">
      <div class="modal-card" @click.stop :class="{ 'dark-card': isDarkMode }">
        <div class="modal-header">
          <h3>설문 정보 선택</h3>
          <p>사용할 설문 데이터를 선택하거나 새로 작성하세요.</p>
        </div>
        <div class="survey-list">
          <div v-for="s in savedSurveys" :key="s.id" class="survey-item">
            <div class="info">
              <span class="title">{{ s.title }}</span>
              <span class="date">{{ s.date }}</span>
            </div>
            <div class="btns">
              <button @click="useSurvey(s.id)" class="select-btn">선택</button>
              <button @click="startNewSurvey(s.id)" class="replace-btn">교체</button>
            </div>
          </div>
          <button v-if="savedSurveys.length < 3" @click="startNewSurvey()" class="add-btn">+ 새 설문 추가</button>
        </div>
        <button @click="showModal = false" class="close-btn">닫기</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 원본 디자인 유지 + 모달 스타일 추가 */
.exam-container { padding: 40px; text-align: center; min-height: 80vh; }
.main-btn { background: #FFD700; border: 2px solid #000; padding: 15px 50px; font-weight: 900; border-radius: 50px; cursor: pointer; box-shadow: 0 4px 0 #000; font-size: 1.2rem; }
.main-btn:active { transform: translateY(2px); box-shadow: 0 2px 0 #000; }

.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.7); display: flex; align-items: center; justify-content: center; z-index: 2000; backdrop-filter: blur(4px); }
.modal-card { background: white; padding: 30px; border-radius: 24px; width: 450px; text-align: left; }
.dark-card { background: #1e293b; color: white; }
.survey-item { display: flex; justify-content: space-between; align-items: center; padding: 15px; background: #f8f9fa; border-radius: 12px; margin-bottom: 10px; border: 1px solid #eee; }
.dark-card .survey-item { background: #334155; border-color: #475569; }
.select-btn { background: #FFD700; border: none; padding: 8px 15px; border-radius: 8px; font-weight: bold; cursor: pointer; margin-right: 5px; }
.replace-btn { background: #e2e8f0; border: none; padding: 8px 15px; border-radius: 8px; cursor: pointer; }
.add-btn { width: 100%; padding: 15px; border: 2px dashed #ccc; background: none; border-radius: 12px; cursor: pointer; color: #64748b; font-weight: bold; }
.close-btn { margin-top: 20px; width: 100%; border: none; background: none; color: #94a3b8; cursor: pointer; }
</style>