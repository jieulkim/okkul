import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useSurveyStore = defineStore('survey', () => {
  const surveyData = ref(JSON.parse(localStorage.getItem('temp_survey_data')) || null)
  const deletedSurveyIds = ref(JSON.parse(localStorage.getItem('deletedSurveyIds') || '[]'))

  const setSurveyData = (data) => {
    console.log('[SurveyStore] setSurveyData:', data)
    surveyData.value = data
    localStorage.setItem('temp_survey_data', JSON.stringify(data))
  }

  const clearSurveyData = () => {
    console.log('[SurveyStore] clearSurveyData')
    surveyData.value = null
    localStorage.removeItem('temp_survey_data')
  }

  const deleteSurvey = (surveyId) => {
    const id = Number(surveyId)
    if (!deletedSurveyIds.value.includes(id)) {
      deletedSurveyIds.value.push(id)
      localStorage.setItem('deletedSurveyIds', JSON.stringify(deletedSurveyIds.value))
    }
  }

  const filterSurveys = (surveys) => {
    if (!surveys) return []
    return surveys.filter(s => !deletedSurveyIds.value.includes(Number(s.surveyId)))
  }

  return { surveyData, deletedSurveyIds, setSurveyData, clearSurveyData, deleteSurvey, filterSurveys }
})

export default useSurveyStore