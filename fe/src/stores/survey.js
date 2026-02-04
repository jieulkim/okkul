import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useSurveyStore = defineStore('survey', () => {
  const surveyData = ref(JSON.parse(localStorage.getItem('temp_survey_data')) || null)

  const setSurveyData = (data) => {
    surveyData.value = data
    localStorage.setItem('temp_survey_data', JSON.stringify(data))
  }

  const clearSurveyData = () => {
    surveyData.value = null
    localStorage.removeItem('temp_survey_data')
  }

  return { 
    surveyData, 
    setSurveyData, 
    clearSurveyData
  }
})