<script setup>
import { ref, onMounted, inject } from 'vue';
import { useRouter } from 'vue-router';
import SurveySelectModal from '@/components/common/SurveySelectModal.vue';
import { Surveys } from '@/api/Surveys';
import { useSurveyStore } from '@/stores/survey';

const router = useRouter();
const surveyStore = useSurveyStore();
const isDarkMode = inject('isDarkMode', ref(false));

const showSurveySelectModal = ref(true);
const existingSurveys = ref([]);

// 실제 API를 사용하여 설문 목록 로드
const fetchExistingSurveys = async () => {
  try {
    const surveysApi = new Surveys();
    const response = await surveysApi.getSurveyList();
    console.log('[ExamView] Raw Survey List Response:', response.data);
    
    // API 응답 데이터 매핑 (객체 내부 배열 또는 직접 배열 대응)
    const surveyList = response.data?.surveySummaryResponses || (Array.isArray(response.data) ? response.data : []);
    
    // 전역 필터 적용
    const filteredList = surveyStore.filterSurveys(surveyList);

    existingSurveys.value = filteredList.map(s => ({
      surveyId: s.surveyId,
      createdAt: s.createdAt,
      level: s.level,
      occupation: s.occupation || 'N/A',
      topics: s.topics || []
    }));
    console.log('[ExamView] Mapped & Filtered Survey List:', existingSurveys.value);
  } catch (error) {
    console.error("설문 목록 로드 실패", error);
    // 에러 발생 시 빈 목록으로 유지
    existingSurveys.value = [];
  }
};

const startNewSurvey = () => {
  router.push({ path: '/survey', query: { from: 'exam' } });
};

const useSelectedSurvey = (surveyId) => {
  console.log('Selected Survey ID:', surveyId);
  // 선택된 설문 ID를 가지고 다음 단계로 이동
  router.push({
    path: '/exam/setup',
    query: { surveyId: surveyId }
  });
};

const handleDeleteSurvey = (surveyId) => {
  surveyStore.deleteSurvey(surveyId);
  existingSurveys.value = surveyStore.filterSurveys(existingSurveys.value);
  console.log(`[ExamView] Survey ${surveyId} deleted (Global Sync)`);
};

onMounted(() => {
  fetchExistingSurveys();
});
</script>

<template>
  <div class="exam-container" :class="{ 'dark-mode': isDarkMode }">
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
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;400;500;700&display=swap');

.exam-page {
  min-height: 100vh;
  background: #f8fafc;
  font-family: 'Noto Sans KR', sans-serif;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dark-mode {
  background: #0f172a;
  color: #f1f5f9;
}
</style>