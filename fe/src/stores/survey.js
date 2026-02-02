import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useSurveyStore = defineStore('survey', () => {
  const surveyData = ref(JSON.parse(localStorage.getItem('temp_survey_data')) || null)
  // 로컬에 저장된 삭제 ID 목록
  const deletedSurveyIds = ref(JSON.parse(localStorage.getItem('deletedSurveyIds') || '[]'))

  const setSurveyData = (data) => {
    surveyData.value = data
    localStorage.setItem('temp_survey_data', JSON.stringify(data))
  }

  const clearSurveyData = () => {
    surveyData.value = null
    localStorage.removeItem('temp_survey_data')
  }

  // 삭제 목록 전체 초기화 (테스트용)
  const resetDeletedList = () => {
    deletedSurveyIds.value = []
    localStorage.removeItem('deletedSurveyIds')
    console.warn('[SurveyStore] 삭제 목록이 초기화되었습니다.')
  }

  const deleteSurvey = (surveyId) => {
    const id = Number(surveyId)
    if (!deletedSurveyIds.value.includes(id)) {
      deletedSurveyIds.value.push(id)
      localStorage.setItem('deletedSurveyIds', JSON.stringify(deletedSurveyIds.value))
    }
  }

  const filterSurveys = (surveys) => {
    if (!surveys || surveys.length === 0) return []
    
    const deletedIds = deletedSurveyIds.value.map(id => Number(id))
    
    // 필터링 결과 반환
    return surveys.filter(s => {
      const targetId = Number(s.surveyId)
      const isDeleted = deletedIds.includes(targetId)
      return !isDeleted
    })
  }

  return { 
    surveyData, 
    deletedSurveyIds, 
    setSurveyData, 
    clearSurveyData, 
    deleteSurvey, 
    filterSurveys,
    resetDeletedList
  }
})