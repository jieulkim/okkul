import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import axios from 'axios'
import router from '@/router'

export const useAuthStore = defineStore('auth', () => {
    const user = ref(null)
    const token = ref(localStorage.getItem('accessToken'))
    const loading = ref(false)
    const error = ref(null)

    const isAuthenticated = computed(() => !!user.value)

    const login = () => {
        window.location.href = '/api/oauth2/authorization/google'
    }

    const fetchUser = async () => {
        loading.value = true
        try {
            const response = await axios.get('/api/users/me')
            user.value = response.data
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
            const response = await axios.post('/api/users/nickname', { nickname })
            if (user.value) user.value = response.data
            return true
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
            const response = await axios.post('/api/users/target-level', { targetLevel })
            if (user.value) user.value = response.data
            return true
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
            const response = await axios.post('/api/users/profile-image', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            })
            if (user.value) user.value = response.data
            return true
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
