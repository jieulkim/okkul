/**
 * API 유틸리티
 */

// 백엔드 도메인 주소
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL

export const apiFetch = async (url, options = {}) => {
    const accessToken = localStorage.getItem('accessToken')

    const headers = {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        ...options.headers,
    }

    if (accessToken) {
        headers['Authorization'] = `Bearer ${accessToken}`
        const refreshToken = localStorage.getItem('refreshToken')
        if (refreshToken) {
            headers['Authorization-Refresh'] = `Bearer ${refreshToken}`
            headers['Authorization-refresh'] = `Bearer ${refreshToken}`
            headers['Refresh-Token'] = `Bearer ${refreshToken}`
        }
    }

    if (options.body instanceof FormData) {
        delete headers['Content-Type']
    }

    const cleanUrl = url.startsWith('/') ? url.slice(1) : url
    const finalUrl = `${API_BASE_URL}/${cleanUrl}`

    try {
        const response = await fetch(finalUrl, {
            ...options,
            headers,
        })

        if (response.status === 401) {
            localStorage.removeItem('accessToken')
            localStorage.removeItem('refreshToken')
            window.location.href = '/login'
            throw new Error('Unauthorized')
        }

        return response
    } catch (error) {
        console.error('API Error:', error)
        throw error
    }
}

export const api = {
    get: (url, options = {}) => apiFetch(url, { ...options, method: 'GET' }),
    post: (url, data, options = {}) => apiFetch(url, {
        ...options,
        method: 'POST',
        body: data instanceof FormData ? data : JSON.stringify(data)
    }),
    put: (url, data, options = {}) => apiFetch(url, { ...options, method: 'PUT', body: JSON.stringify(data) }),
    patch: (url, data, options = {}) => apiFetch(url, { ...options, method: 'PATCH', body: JSON.stringify(data) }),
    delete: (url, options = {}) => apiFetch(url, { ...options, method: 'DELETE' }),
}

export default api