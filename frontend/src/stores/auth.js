/**
 * Ngambis.ai — Auth Store (Pinia)
 *
 * Centralized authentication state management.
 * Handles login, register, OAuth2 callback, token management,
 * and user profile state.
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import authApi from '@/services/auth.api'
import router from '@/router'

export const useAuthStore = defineStore('auth', () => {
  // ===== State =====
  const user = ref(null)
  const accessToken = ref(localStorage.getItem('accessToken') || null)
  const refreshToken = ref(localStorage.getItem('refreshToken') || null)
  const loading = ref(false)
  const error = ref(null)

  // Auth modal state
  const showAuthModal = ref(false)
  const authModalMessage = ref('')
  const authModalRedirect = ref(null)

  // ===== Getters (Computed) =====
  const isAuthenticated = computed(() => !!accessToken.value)
  const userName = computed(() => user.value?.name || user.value?.username || '')
  const userEmail = computed(() => user.value?.email || '')
  const userAvatar = computed(() => user.value?.profilePicture || null)
  const userProvider = computed(() => user.value?.provider || 'LOCAL')

  // ===== Actions =====

  /**
   * Register a new user (LOCAL provider).
   */
  async function register(username, email, password) {
    loading.value = true
    error.value = null

    try {
      const response = await authApi.register({ username, email, password })
      const data = response.data.data

      _saveAuth(data)
      return data
    } catch (err) {
      error.value = err.response?.data?.message || 'Registration failed'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Login with username/password (LOCAL provider).
   */
  async function login(username, password) {
    loading.value = true
    error.value = null

    try {
      const response = await authApi.login({ username, password })
      const data = response.data.data

      _saveAuth(data)
      return data
    } catch (err) {
      error.value = err.response?.data?.message || 'Login failed. Please check your credentials.'
      throw err
    } finally {
      loading.value = false
    }
  }

  /**
   * Initiate Google OAuth2 login.
   * Redirects the browser to backend OAuth2 endpoint.
   */
  function loginWithGoogle() {
    window.location.href = authApi.getGoogleLoginUrl()
  }

  /**
   * Handle OAuth2 callback — extract tokens from URL parameters.
   * Called from the /oauth2/callback route component.
   */
  function handleOAuth2Callback() {
    const params = new URLSearchParams(window.location.search)

    const token = params.get('accessToken')
    const refresh = params.get('refreshToken')
    const userId = params.get('userId')
    const email = params.get('email')
    const name = params.get('name')

    if (!token || !refresh) {
      error.value = 'OAuth2 callback missing required tokens'
      throw new Error(error.value)
    }

    // Save tokens
    accessToken.value = token
    refreshToken.value = refresh
    localStorage.setItem('accessToken', token)
    localStorage.setItem('refreshToken', refresh)

    // Set basic user info from URL params
    user.value = {
      id: userId,
      email: email,
      name: name,
      provider: 'GOOGLE'
    }

    return user.value
  }

  /**
   * Fetch the current user's profile from backend.
   * Call after login/OAuth2 to get full user data.
   */
  async function fetchProfile() {
    if (!accessToken.value) return null

    try {
      const response = await authApi.getProfile()
      user.value = response.data.data
      return user.value
    } catch (err) {
      // If 401, token is invalid — logout
      if (err.response?.status === 401) {
        logout()
      }
      return null
    }
  }

  /**
   * Logout — clear all auth state and redirect to dashboard.
   */
  function logout() {
    user.value = null
    accessToken.value = null
    refreshToken.value = null
    error.value = null

    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')

    router.push({ name: 'Welcome' })
  }

  /**
   * Request authentication — opens the auth modal with an optional message.
   * Call this from any component when a guest tries a protected action.
   * @param {string} message - Message to show in the modal
   * @param {string|null} redirectTo - Where to redirect after login
   */
  function requestAuth(message = '', redirectTo = null) {
    authModalMessage.value = message
    authModalRedirect.value = redirectTo
    showAuthModal.value = true
  }

  /**
   * Close the auth modal.
   */
  function closeAuthModal() {
    showAuthModal.value = false
    authModalMessage.value = ''
    authModalRedirect.value = null
  }

  /**
   * Initialize auth state on app startup.
   * Checks for existing tokens and fetches profile.
   */
  async function initAuth() {
    if (accessToken.value) {
      await fetchProfile()
    }
  }

  /**
   * Clear error state.
   */
  function clearError() {
    error.value = null
  }

  // ===== Private Helpers =====
  function _saveAuth(data) {
    accessToken.value = data.accessToken
    refreshToken.value = data.refreshToken
    localStorage.setItem('accessToken', data.accessToken)
    localStorage.setItem('refreshToken', data.refreshToken)

    user.value = {
      id: data.userId,
      username: data.username,
      email: data.email,
      name: data.name,
      profilePicture: data.profilePicture,
      provider: data.provider,
      roles: data.roles
    }
  }

  return {
    // State
    user,
    accessToken,
    refreshToken,
    loading,
    error,
    showAuthModal,
    authModalMessage,
    authModalRedirect,
    // Getters
    isAuthenticated,
    userName,
    userEmail,
    userAvatar,
    userProvider,
    // Actions
    register,
    login,
    loginWithGoogle,
    handleOAuth2Callback,
    fetchProfile,
    logout,
    initAuth,
    clearError,
    requestAuth,
    closeAuthModal
  }
})
