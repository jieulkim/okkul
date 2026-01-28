import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import api from '@/utils/api'

export const useAuthStore = defineStore('auth', () => {
    const user = ref(null)
    const token = ref(localStorage.getItem('accessToken'))
    const loading = ref(false)

    const isAuthenticated = computed(() => !!user.value)

    const login = () => {
        // 현재 내가 접속한 주소 (localhost:5173 또는 dev.okkul.site)
        const currentOrigin = window.location.origin
        const redirectUri = `${currentOrigin}/oauth2/redirect`

        window.location.href = `https://api.cicd.okkul.site/oauth2/authorization/google?redirect_uri=${redirectUri}`
    }

    const fetchUser = async () => {
        if (!localStorage.getItem('accessToken')) return

        loading.value = true
        try {
            const response = await api.get('/users/me')
            if (response.ok) {
                user.value = await response.json()
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