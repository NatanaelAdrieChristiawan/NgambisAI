/**
 * Ngambis.ai — Auth API Service
 *
 * Encapsulates all authentication-related API calls.
 * Used by the auth store (Pinia) to interact with the backend.
 */

import api from './api'

const authApi = {
  /**
   * Register a new user (LOCAL provider).
   * @param {{ username: string, email: string, password: string }} data
   * @returns {Promise<import('axios').AxiosResponse>}
   */
  register(data) {
    return api.post('/api/auth/register', data)
  },

  /**
   * Login with username/password (LOCAL provider).
   * @param {{ username: string, password: string }} data
   * @returns {Promise<import('axios').AxiosResponse>}
   */
  login(data) {
    return api.post('/api/auth/login', data)
  },

  /**
   * Refresh JWT access token.
   * @param {string} refreshToken
   * @returns {Promise<import('axios').AxiosResponse>}
   */
  refreshToken(refreshToken) {
    return api.post(`/api/auth/refresh?refreshToken=${refreshToken}`)
  },

  /**
   * Get current authenticated user's profile.
   * @returns {Promise<import('axios').AxiosResponse>}
   */
  getProfile() {
    return api.get('/api/auth/me')
  },

  /**
   * Get the Google OAuth2 login URL.
   * Always points directly to backend — OAuth2 redirect flow
   * requires the browser to interact with the backend directly
   * so that session cookies and the callback URI match.
   */
  getGoogleLoginUrl() {
    const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
    return `${baseUrl}/oauth2/authorization/google`
  }
}

export default authApi
