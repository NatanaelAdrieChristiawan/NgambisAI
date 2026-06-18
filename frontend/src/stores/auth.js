/**
 * Ngambis.ai — Auth Store (Pinia)
 *
 * Centralized authentication state management.
 * Handles login, register, OAuth2 callback, token management,
 * and user profile state.
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { jwtDecode } from 'jwt-decode'
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

  // ===== Helpers =====

  /**
   * Checks if a JWT token is present and not expired.
   * Uses jwt-decode to read the `exp` claim without signature verification.
   * A 30-second buffer prevents edge cases where the token expires mid-request.
   */
  function isTokenValid(token) {
    if (!token) return false
    try {
      const decoded = jwtDecode(token)
      return decoded.exp * 1000 > Date.now() + 30000
    } catch {
      return false
    }
  }

  // ===== Getters (Computed) =====
  const isAuthenticated = computed(() => isTokenValid(accessToken.value))

  /**
   * Resolves user display name.
   * Priority: name → username (but NOT if it's an email address) → email local-part.
   * Google users have username = email, so we fall back to the part before '@'.
   */
  const userName = computed(() => {
    if (!user.value) return ''
    if (user.value.name) return user.value.name
    const u = user.value.username || ''
    // If username looks like an email (Google user), extract display-friendly part
    if (u.includes('@')) return u.split('@')[0]
    return u
  })

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

    // Save tokens to Pinia state (also synced to localStorage via persist plugin)
    accessToken.value = token
    refreshToken.value = refresh
    // Keep localStorage in sync for the Axios interceptor which reads directly from it
    localStorage.setItem('accessToken', token)
    localStorage.setItem('refreshToken', refresh)

    // Set preliminary user info from URL params so UI renders immediately
    // fetchProfile() will overwrite this with the full server response
    user.value = {
      id: userId,
      email: email || '',
      name: name || (email ? email.split('@')[0] : ''),  // fallback to email local-part
      username: email || '',   // Google users use email as username in DB
      profilePicture: null,    // will be filled by fetchProfile()
      provider: 'GOOGLE',
      roles: ['USER']
    }

    return user.value
  }

  /**
   * Fetch the current user's profile from backend.
   * Normalizes the UserResponse shape to be consistent with _saveAuth().
   * This is CRITICAL: UserResponse uses `id` (not `userId`) and lacks `roles`.
   */
  async function fetchProfile() {
    if (!accessToken.value) return null

    try {
      const response = await authApi.getProfile()
      const data = response.data.data

      // Normalize field names — UserResponse shape may differ from AuthResponse shape
      user.value = {
        id: data.id,
        username: data.username || '',
        email: data.email || '',
        // Guard: if name is null/empty (edge case for Google user), derive from email
        name: data.name || (data.email ? data.email.split('@')[0] : data.username || ''),
        profilePicture: data.profilePicture || null,
        provider: data.provider || 'LOCAL',
        // roles is now included in UserResponse — use server data, fallback to persisted or default
        roles: data.roles || user.value?.roles || ['USER'],
        createdAt: data.createdAt
      }
      return user.value
    } catch (err) {
      // If 401, token is invalid — logout
      if (err.response?.status === 401) {
        logout()
      }
      // On other errors, keep existing user.value (persisted) so UI doesn't break
      console.warn('[auth] fetchProfile failed:', err.message)
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
    localStorage.removeItem('ngambis-auth')  // Clear persisted Pinia state to prevent stale restore

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
    if (!accessToken.value && !refreshToken.value) {
      // No tokens at all — nothing to restore
      return null
    }

    if (!isTokenValid(accessToken.value)) {
      // Access token expired or missing — try refresh
      if (isTokenValid(refreshToken.value)) {
        try {
          const response = await authApi.refreshToken(refreshToken.value)
          const { accessToken: newAccess, refreshToken: newRefresh } = response.data.data
          accessToken.value = newAccess
          refreshToken.value = newRefresh
          localStorage.setItem('accessToken', newAccess)
          localStorage.setItem('refreshToken', newRefresh)
        } catch {
          // Refresh failed — force full logout
          logout()
          return null
        }
      } else {
        // Both tokens expired — force full logout
        logout()
        return null
      }
    }

    // Token is valid (original or refreshed) — fetch fresh profile
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
}, {
  // Persist critical auth state across page refreshes
  // accessToken and refreshToken are also mirrored in localStorage for Axios interceptor
  persist: {
    key: 'ngambis-auth',
    storage: localStorage,
    pick: ['user', 'accessToken', 'refreshToken']
  }
})
