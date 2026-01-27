/**
 * API 유틸리티
 * - JWT 토큰 자동 포함
 * - 401 에러 시 자동 로그인 페이지 이동
 */

// API Base URL - '/api'를 사용하여 Vite Proxy를 이용합니다.
const API_BASE_URL = '/api'

/**
 * Authorization 헤더가 포함된 fetch 래퍼
 */
export const apiFetch = async (url, options = {}) => {
    const accessToken = localStorage.getItem('accessToken')

    const headers = {
        'Content-Type': 'application/json',
        ...options.headers,
    }

    // JWT 토큰 자동 포함
    if (accessToken) {
        headers['Authorization'] = `Bearer ${accessToken}`
    }

    // FormData는 Content-Type 자동 설정
    if (options.body instanceof FormData) {
        delete headers['Content-Type']
    }

    try {
        const response = await fetch(`${API_BASE_URL}${url}`, {
            ...options,
            headers,
        })

        // 401 Unauthorized
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

/**
 * 편의 메서드
 */
export const api = {
    get: (url, options = {}) =>
        apiFetch(url, { ...options, method: 'GET' }),

    post: (url, data, options = {}) =>
        apiFetch(url, {
            ...options,
            method: 'POST',
            body: data instanceof FormData ? data : JSON.stringify(data)
        }),

    put: (url, data, options = {}) =>
        apiFetch(url, {
            ...options,
            method: 'PUT',
            body: JSON.stringify(data)
        }),

    patch: (url, data, options = {}) =>
        apiFetch(url, {
            ...options,
            method: 'PATCH',
            body: JSON.stringify(data)
        }),

    delete: (url, options = {}) =>
        apiFetch(url, { ...options, method: 'DELETE' }),
}

export default api