import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { usersApi } from '@/api'
import { useSurveyStore } from '@/stores/survey'

export const useAuthStore = defineStore('auth', () => {
    const user = ref(null)
    const token = ref(localStorage.getItem('accessToken'))
    const loading = ref(false)

    const isAuthenticated = computed(() => !!user.value)

    const login = () => {
        // 현재 내가 접속한 주소 (localhost:5173 또는 dev.okkul.site)
        const currentOrigin = window.location.origin
        const redirectUri = `${currentOrigin}/oauth2/redirect`
        window.location.href = `${import.meta.env.VITE_API_BASE_URL}/oauth2/authorization/google?redirect_uri=${redirectUri}`
    }

    // 개발 모드용 모의 로그인
    const loginAsDev = () => {
        const mockUser = {
            id: 999,
            email: 'dev@example.com',
            name: 'Dev User',
            role: 'USER',
            profileImage: 'https://via.placeholder.com/150'
        }
        const mockToken = 'mock_dev_token_' + Date.now()

        user.value = mockUser
        token.value = mockToken

        localStorage.setItem('accessToken', mockToken)
        localStorage.setItem('refreshToken', mockToken)

        // Mock Mode: 설문 데이터 자동 주입
        if (import.meta.env.VITE_USE_MOCK_DATA === 'true') {
            const surveyStore = useSurveyStore()

            const dummySurvey = {
                occupationAnswerId: 1, // 사업/회사
                hasJob: true,
                workPeriodAnswerId: 2, // 2개월 이상
                teachAnswerId: null,
                manager: false,
                student: false,
                classTypeAnswerId: null,
                residenceAnswerId: 1, // 개인주택/아파트 홀로 거주
                leisure: [101, 106], // 영화보기, 공원가기
                hobby: [202], // 음악 감상
                exercise: [317], // 걷기
                holiday: [404], // 국내 여행
                level: null // 레벨은 SurveyLevelView에서 선택
            }

            surveyStore.setSurveyData(dummySurvey)
            localStorage.setItem('survey_completed', 'true')
            console.log('[DevLogin] 더미 설문 데이터가 Store에 주입되었습니다.')
        }

        // 홈으로 이동
        window.location.href = '/'
    }

    let fetchPromise = null

    const fetchUser = async () => {
        if (!localStorage.getItem('accessToken')) return null
        if (fetchPromise) return fetchPromise

        fetchPromise = (async () => {
            // Mock Mode: 실제 API 호출 건너뛰기
            if (import.meta.env.VITE_USE_MOCK_DATA === 'true') {
                const mockToken = localStorage.getItem('accessToken')
                if (mockToken && mockToken.startsWith('mock_dev_token')) {
                    user.value = {
                        id: 999,
                        email: 'dev@example.com',
                        name: 'Dev User',
                        role: 'USER',
                        profileImage: 'https://via.placeholder.com/150'
                    }
                    token.value = mockToken
                    return user.value
                }
            }

            loading.value = true
            try {
                const response = await usersApi.getMyInfo()
                if (response.data) {
                    user.value = response.data
                    token.value = localStorage.getItem('accessToken') // 토큰 상태도 동기화
                    return user.value
                } else {
                    user.value = null
                    return null
                }
            } catch (err) {
                user.value = null
                throw err
            } finally {
                loading.value = false
                fetchPromise = null
            }
        })()

        return fetchPromise
    }

    const updateUser = (userData) => {
        if (userData) {
            user.value = { ...userData }
            console.log('[AuthStore] 사용자 정보 업데이트됨:', user.value)
        }
    }

    const logout = () => {
        user.value = null
        token.value = null
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('survey_completed')
        localStorage.removeItem('temp_survey_data')
        window.location.href = '/login'
    }

    return { user, token, loading, isAuthenticated, login, loginAsDev, fetchUser, logout, updateUser }
})