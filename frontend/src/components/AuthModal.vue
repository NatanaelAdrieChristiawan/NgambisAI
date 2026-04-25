<script setup>
/**
 * AuthModal.vue
 *
 * A glassmorphism-styled modal for login/register.
 * Shown when unauthenticated users try to access protected features.
 * Supports local auth (username/password) and Google OAuth2.
 */
import { reactive, ref, watch, nextTick } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  /** Optional message explaining why login is needed */
  message: { type: String, default: '' },
  /** Where to redirect after successful login */
  redirectTo: { type: String, default: null }
})

const emit = defineEmits(['update:modelValue', 'authenticated'])

const authStore = useAuthStore()
const router = useRouter()

// Tab state
const activeTab = ref('login')

// Login form
const loginForm = reactive({
  username: '',
  password: ''
})

// Register form
const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const showPassword = ref(false)
const localError = ref(null)
const isVisible = ref(false)
const isClosing = ref(false)

// Watch modelValue to control animation
watch(() => props.modelValue, (val) => {
  if (val) {
    isClosing.value = false
    nextTick(() => {
      isVisible.value = true
    })
  } else {
    isVisible.value = false
  }
})

function closeModal() {
  isClosing.value = true
  isVisible.value = false
  setTimeout(() => {
    emit('update:modelValue', false)
    isClosing.value = false
    resetForms()
  }, 300)
}

function resetForms() {
  loginForm.username = ''
  loginForm.password = ''
  registerForm.username = ''
  registerForm.email = ''
  registerForm.password = ''
  registerForm.confirmPassword = ''
  showPassword.value = false
  localError.value = null
  authStore.clearError()
}

function switchTab(tab) {
  activeTab.value = tab
  localError.value = null
  authStore.clearError()
}

async function handleLogin() {
  authStore.clearError()
  localError.value = null

  try {
    await authStore.login(loginForm.username, loginForm.password)
    emit('authenticated')
    closeModal()
    if (props.redirectTo) {
      router.push(props.redirectTo)
    }
  } catch {
    // Error is set in the store
  }
}

async function handleRegister() {
  authStore.clearError()
  localError.value = null

  if (registerForm.password !== registerForm.confirmPassword) {
    localError.value = 'Passwords do not match.'
    return
  }

  if (registerForm.password.length < 6) {
    localError.value = 'Password must be at least 6 characters.'
    return
  }

  try {
    await authStore.register(registerForm.username, registerForm.email, registerForm.password)
    emit('authenticated')
    closeModal()
    if (props.redirectTo) {
      router.push(props.redirectTo)
    }
  } catch {
    // Error is set in the store
  }
}

function handleGoogleLogin() {
  authStore.loginWithGoogle()
}

function handleOverlayClick(e) {
  if (e.target === e.currentTarget) {
    closeModal()
  }
}
</script>

<template>
  <Teleport to="body">
    <Transition name="modal-overlay">
      <div
        v-if="modelValue"
        class="auth-modal-overlay"
        :class="{ visible: isVisible, closing: isClosing }"
        @click="handleOverlayClick"
        @keydown.esc="closeModal"
      >
        <Transition name="modal-content">
          <div v-if="isVisible" class="auth-modal" role="dialog" aria-modal="true" aria-label="Authentication">
            <!-- Close button -->
            <button class="modal-close" @click="closeModal" aria-label="Close">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24"
                   fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="18" y1="6" x2="6" y2="18" />
                <line x1="6" y1="6" x2="18" y2="18" />
              </svg>
            </button>

            <!-- Decorative glow -->
            <div class="modal-glow modal-glow-1"></div>
            <div class="modal-glow modal-glow-2"></div>

            <!-- Header -->
            <div class="modal-header">
              <div class="modal-logo">
                <span class="modal-logo-icon">🎓</span>
                <h2>Ngambis<span class="accent">.ai</span></h2>
              </div>
              <p v-if="message" class="modal-message">{{ message }}</p>
              <p v-else class="modal-message">Masuk untuk mulai belajar dengan AI</p>
            </div>

            <!-- Tabs -->
            <div class="auth-tabs">
              <button
                class="tab-btn"
                :class="{ active: activeTab === 'login' }"
                @click="switchTab('login')"
              >
                Sign In
              </button>
              <button
                class="tab-btn"
                :class="{ active: activeTab === 'register' }"
                @click="switchTab('register')"
              >
                Sign Up
              </button>
              <div class="tab-indicator" :class="{ right: activeTab === 'register' }"></div>
            </div>

            <!-- Login Form -->
            <form v-if="activeTab === 'login'" @submit.prevent="handleLogin" class="modal-form">
              <div class="form-group">
                <label for="modal-login-username">Username</label>
                <div class="input-wrapper">
                  <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                  <input
                    id="modal-login-username"
                    v-model="loginForm.username"
                    type="text"
                    placeholder="Enter your username"
                    required
                    autocomplete="username"
                  />
                </div>
              </div>

              <div class="form-group">
                <label for="modal-login-password">Password</label>
                <div class="input-wrapper">
                  <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
                  <input
                    id="modal-login-password"
                    v-model="loginForm.password"
                    :type="showPassword ? 'text' : 'password'"
                    placeholder="Enter your password"
                    required
                    autocomplete="current-password"
                  />
                  <button type="button" class="toggle-password" @click="showPassword = !showPassword" tabindex="-1">
                    {{ showPassword ? '🙈' : '👁️' }}
                  </button>
                </div>
              </div>

              <button type="submit" class="btn-primary" :disabled="authStore.loading">
                <span v-if="authStore.loading" class="spinner"></span>
                <span v-else>Sign In</span>
              </button>

              <p v-if="authStore.error" class="error-message">{{ authStore.error }}</p>
            </form>

            <!-- Register Form -->
            <form v-if="activeTab === 'register'" @submit.prevent="handleRegister" class="modal-form">
              <div class="form-group">
                <label for="modal-reg-username">Username</label>
                <div class="input-wrapper">
                  <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                  <input
                    id="modal-reg-username"
                    v-model="registerForm.username"
                    type="text"
                    placeholder="Choose a username"
                    required
                    autocomplete="username"
                    minlength="3"
                  />
                </div>
              </div>

              <div class="form-group">
                <label for="modal-reg-email">Email</label>
                <div class="input-wrapper">
                  <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="4" width="20" height="16" rx="2"/><path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7"/></svg>
                  <input
                    id="modal-reg-email"
                    v-model="registerForm.email"
                    type="email"
                    placeholder="Enter your email"
                    required
                    autocomplete="email"
                  />
                </div>
              </div>

              <div class="form-group">
                <label for="modal-reg-password">Password</label>
                <div class="input-wrapper">
                  <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
                  <input
                    id="modal-reg-password"
                    v-model="registerForm.password"
                    :type="showPassword ? 'text' : 'password'"
                    placeholder="Min. 6 characters"
                    required
                    autocomplete="new-password"
                    minlength="6"
                  />
                  <button type="button" class="toggle-password" @click="showPassword = !showPassword" tabindex="-1">
                    {{ showPassword ? '🙈' : '👁️' }}
                  </button>
                </div>
              </div>

              <div class="form-group">
                <label for="modal-reg-confirm">Confirm Password</label>
                <div class="input-wrapper">
                  <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
                  <input
                    id="modal-reg-confirm"
                    v-model="registerForm.confirmPassword"
                    :type="showPassword ? 'text' : 'password'"
                    placeholder="Confirm your password"
                    required
                    autocomplete="new-password"
                  />
                </div>
              </div>

              <button type="submit" class="btn-primary" :disabled="authStore.loading">
                <span v-if="authStore.loading" class="spinner"></span>
                <span v-else>Create Account</span>
              </button>

              <p v-if="localError || authStore.error" class="error-message">
                {{ localError || authStore.error }}
              </p>
            </form>

            <!-- Divider -->
            <div class="divider"><span>OR</span></div>

            <!-- Google OAuth -->
            <button @click="handleGoogleLogin" class="btn-google">
              <svg class="google-icon" viewBox="0 0 24 24" width="20" height="20">
                <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 01-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z" fill="#4285F4"/>
                <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
                <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
                <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
              </svg>
              <span>{{ activeTab === 'login' ? 'Sign in with Google' : 'Sign up with Google' }}</span>
            </button>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
/* ===== Overlay ===== */
.auth-modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0);
  backdrop-filter: blur(0);
  transition: background 0.3s ease, backdrop-filter 0.3s ease;
  padding: 1rem;
}

.auth-modal-overlay.visible {
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(8px);
}

.auth-modal-overlay.closing {
  background: rgba(0, 0, 0, 0);
  backdrop-filter: blur(0);
}

/* ===== Modal Container ===== */
.auth-modal {
  position: relative;
  width: 100%;
  max-width: 440px;
  max-height: 90vh;
  overflow-y: auto;
  background: rgba(15, 12, 41, 0.92);
  backdrop-filter: blur(40px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-xl);
  box-shadow:
    0 32px 100px rgba(0, 0, 0, 0.5),
    0 0 80px rgba(108, 99, 255, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.06);
  padding: 2.25rem;
  animation: modalSlideUp 0.35s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes modalSlideUp {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.96);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* Decorative glows */
.modal-glow {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  pointer-events: none;
  z-index: -1;
}

.modal-glow-1 {
  top: -40px;
  right: -20px;
  width: 160px;
  height: 160px;
  background: rgba(108, 99, 255, 0.15);
}

.modal-glow-2 {
  bottom: -30px;
  left: -20px;
  width: 120px;
  height: 120px;
  background: rgba(0, 210, 255, 0.1);
}

/* ===== Close Button ===== */
.modal-close {
  position: absolute;
  top: 1rem;
  right: 1rem;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: var(--radius-sm);
  color: var(--color-text-muted);
  cursor: pointer;
  transition: all var(--transition-fast);
  z-index: 2;
}

.modal-close:hover {
  background: rgba(255, 255, 255, 0.1);
  color: var(--color-text-primary);
  transform: rotate(90deg);
}

/* ===== Header ===== */
.modal-header {
  text-align: center;
  margin-bottom: 1.5rem;
}

.modal-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  margin-bottom: 0.625rem;
}

.modal-logo-icon {
  font-size: 1.75rem;
}

.modal-logo h2 {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0;
}

.accent {
  color: var(--color-primary);
}

.modal-message {
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
  line-height: 1.5;
}

/* ===== Tabs ===== */
.auth-tabs {
  position: relative;
  display: flex;
  background: rgba(255, 255, 255, 0.04);
  border-radius: var(--radius-md);
  padding: 4px;
  margin-bottom: 1.5rem;
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.tab-btn {
  flex: 1;
  padding: 0.625rem;
  background: transparent;
  border: none;
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
  font-weight: 600;
  cursor: pointer;
  transition: color var(--transition-fast);
  z-index: 1;
  position: relative;
  border-radius: calc(var(--radius-md) - 4px);
}

.tab-btn.active {
  color: var(--color-text-primary);
}

.tab-btn:hover:not(.active) {
  color: var(--color-text-secondary);
}

.tab-indicator {
  position: absolute;
  top: 4px;
  left: 4px;
  width: calc(50% - 4px);
  height: calc(100% - 8px);
  background: rgba(108, 99, 255, 0.2);
  border: 1px solid rgba(108, 99, 255, 0.25);
  border-radius: calc(var(--radius-md) - 4px);
  transition: transform 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.tab-indicator.right {
  transform: translateX(100%);
}

/* ===== Form ===== */
.modal-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  animation: fadeInForm 0.25s ease;
}

@keyframes fadeInForm {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
}

.form-group label {
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  font-weight: 500;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 0.875rem;
  color: var(--color-text-muted);
  pointer-events: none;
  transition: color var(--transition-fast);
}

.input-wrapper input {
  width: 100%;
  padding: 0.75rem 1rem 0.75rem 2.75rem;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  color: var(--color-text-primary);
  font-size: var(--font-size-sm);
  transition: all var(--transition-fast);
  outline: none;
}

.input-wrapper input::placeholder {
  color: var(--color-text-disabled);
}

.input-wrapper input:focus {
  border-color: var(--color-primary);
  background: rgba(108, 99, 255, 0.06);
  box-shadow: 0 0 0 3px var(--color-primary-glow);
}

.input-wrapper input:focus + .input-icon,
.input-wrapper:focus-within .input-icon {
  color: var(--color-primary-light);
}

.toggle-password {
  position: absolute;
  right: 0.75rem;
  background: none;
  border: none;
  font-size: 0.9rem;
  cursor: pointer;
  padding: 0.25rem;
  line-height: 1;
}

/* ===== Buttons ===== */
.btn-primary {
  width: 100%;
  padding: 0.75rem;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  color: white;
  border: none;
  border-radius: var(--radius-md);
  font-size: var(--font-size-sm);
  font-weight: 600;
  transition: all var(--transition-fast);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  margin-top: 0.25rem;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px var(--color-primary-glow);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-message {
  color: var(--color-error);
  font-size: var(--font-size-xs);
  text-align: center;
  padding: 0.5rem;
  background: rgba(239, 68, 68, 0.1);
  border-radius: var(--radius-sm);
  border: 1px solid rgba(239, 68, 68, 0.2);
}

/* ===== Divider ===== */
.divider {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin: 1.25rem 0;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: var(--color-border);
}

.divider span {
  color: var(--color-text-muted);
  font-size: var(--font-size-xs);
  font-weight: 600;
  letter-spacing: 0.1em;
}

/* ===== Google Button ===== */
.btn-google {
  width: 100%;
  padding: 0.75rem;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  color: var(--color-text-primary);
  font-size: var(--font-size-sm);
  font-weight: 500;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  transition: all var(--transition-fast);
}

.btn-google:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: var(--color-border-hover);
  transform: translateY(-1px);
}

.google-icon {
  flex-shrink: 0;
}

/* ===== Transition classes ===== */
.modal-overlay-enter-active {
  transition: all 0.3s ease;
}
.modal-overlay-leave-active {
  transition: all 0.3s ease;
}
.modal-overlay-enter-from,
.modal-overlay-leave-to {
  opacity: 0;
}

.modal-content-enter-active {
  transition: all 0.35s cubic-bezier(0.16, 1, 0.3, 1);
}
.modal-content-leave-active {
  transition: all 0.25s ease;
}
.modal-content-enter-from {
  opacity: 0;
  transform: translateY(30px) scale(0.96);
}
.modal-content-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.98);
}

/* ===== Scrollbar for modal ===== */
.auth-modal::-webkit-scrollbar {
  width: 4px;
}

.auth-modal::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
}

/* ===== Responsive ===== */
@media (max-width: 480px) {
  .auth-modal {
    padding: 1.5rem;
    max-width: 100%;
    border-radius: var(--radius-lg);
    max-height: 95vh;
  }

  .modal-logo h2 {
    font-size: 1.25rem;
  }

  .modal-form {
    gap: 0.875rem;
  }
}

@media (max-height: 700px) {
  .auth-modal {
    max-height: 95vh;
  }

  .modal-header {
    margin-bottom: 1rem;
  }

  .auth-tabs {
    margin-bottom: 1rem;
  }

  .divider {
    margin: 0.875rem 0;
  }
}
</style>
