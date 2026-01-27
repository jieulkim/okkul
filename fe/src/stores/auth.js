import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import router from '@/router'
import api from '@/utils/api'

export const useAuthStore = defineStore('auth', () => {
    const user = ref(null)
    const token = ref(localStorage.getItem('accessToken'))
    const loading = ref(false)
    const error = ref(null)

    const isAuthenticated = computed(() => !!user.value)

    const login = () => {
        window.location.href = 'https://api.dev.okkul.site/oauth2/authorization/google'
    }

    const fetchUser = async () => {
        loading.value = true
        try {
            const response = await api.get('/users/me')
            if (response.ok) {
                user.value = await response.json()
            } else {
                user.value = null
            }
        } catch (err) {
            console.error('Error fetching user:', err)
            user.value = null
        } finally {
            loading.value = false
        }
    }

    const updateNickname = async (nickname) => {
        loading.value = true
        try {
            const response = await api.post('/users/nickname', { nickname })
            if (response.ok) {
                user.value = await response.json()
                return true
            }
            return false
        } catch (err) {
            console.error('Error updating nickname:', err)
            return false
        } finally {
            loading.value = false
        }
    }

    const updateTargetLevel = async (targetLevel) => {
        loading.value = true
        try {
            const response = await api.post('/users/target-level', { targetLevel })
            if (response.ok) {
                user.value = await response.json()
                return true
            }
            return false
        } catch (err) {
            console.error('Error updating target level:', err)
            return false
        } finally {
            loading.value = false
        }
    }

    const updateProfileImage = async (file) => {
        const formData = new FormData()
        formData.append('file', file)

        loading.value = true
        try {
            const response = await api.post('/users/profile-image', formData)
            if (response.ok) {
                user.value = await response.json()
                return true
            }
            return false
        } catch (err) {
            console.error('Error updating profile image:', err)
            return false
        } finally {
            loading.value = false
        }
    }

    const logout = () => {
        user.value = null
        token.value = null
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
        router.push('/login')
    }

    return {
        user,
        token,
        loading,
        error,
        isAuthenticated,
        login,
        fetchUser,
        logout,
        updateNickname,
        updateTargetLevel,
        updateProfileImage
    }
})