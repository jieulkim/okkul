import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { usersApi } from '@/api'

export const useAuthStore = defineStore('auth', () => {
    const user = ref(null)
    const token = ref(localStorage.getItem('accessToken'))
    const loading = ref(false)

    const isAuthenticated = computed(() => !!user.value)

    const login = () => {
        // 현재 내가 접속한 주소 (localhost:5173 또는 dev.okkul.site)
        const currentOrigin = window.location.origin
        const redirectUri = `${currentOrigin}/oauth2/redirect`
        console.log(import.meta.env.VITE_API_BASE_URL)
        window.location.href = `${import.meta.env.VITE_API_BASE_URL}/oauth2/authorization/google?redirect_uri=${redirectUri}`
    }

    const fetchUser = async () => {
        if (!localStorage.getItem('accessToken')) return

        loading.value = true
        try {
            const response = await usersApi.getMyInfo()
            if (response.data) {
                user.value = response.data
                token.value = localStorage.getItem('accessToken') // 토큰 상태도 동기화
            } else {
                user.value = null
            }
        } catch (err) {
            user.value = null
        } finally {
            loading.value = false
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

    return { user, token, loading, isAuthenticated, login, fetchUser, logout }
})