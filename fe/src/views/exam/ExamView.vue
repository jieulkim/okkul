<script setup>
import { ref, onMounted, inject } from 'vue';
import { useRouter } from 'vue-router';
import SurveySelectModal from '@/components/common/SurveySelectModal.vue';
import api from '@/utils/api';

const router = useRouter();
const isDarkMode = inject('isDarkMode', ref(false));

const showSurveySelectModal = ref(true);
const existingSurveys = ref([]);

// ERD/API 참고용 데이터 로드 로직
const fetchExistingSurveys = async () => {
  try {
    const response = await api.get('/surveys');
    if (response.ok) {
        const data = await response.json();
        existingSurveys.value = data.surveys.map(s => ({
            surveyId: s.surveyId,
            createdAt: s.createdAt,
            level: s.level,
            occupation: s.occupationAnswerId,
            topics: s.topicList
        }));
    }
  } catch (error) {
    console.error("설문 목록 로드 실패", error);
  }
};

const startNewSurvey = () => {
  router.push({ path: '/survey', query: { from: 'exam' } });
};

const useSelectedSurvey = (surveyId) => {
  // TODO: 선택된 설문 ID를 저장하거나 처리하는 로직 필요
  console.log('Selected Survey ID:', surveyId);
  router.push('/exam/setup');
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