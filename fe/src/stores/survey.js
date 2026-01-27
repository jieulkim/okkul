import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useSurveyStore = defineStore('survey', () => {
  const surveyData = ref(null)

  const setSurveyData = (data) => {
    surveyData.value = data
  }

  const clearSurveyData = () => {
    surveyData.value = null
  }

  return { surveyData, setSurveyData, clearSurveyData }
})
