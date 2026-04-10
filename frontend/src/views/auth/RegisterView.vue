<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const form = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const showPassword = ref(false)
const localError = ref(null)

async function handleRegister() {
  authStore.clearError()
  localError.value = null

  if (form.password !== form.confirmPassword) {
    localError.value = 'Passwords do not match.'
    return
  }

  if (form.password.length < 6) {
    localError.value = 'Password must be at least 6 characters.'
    return
  }

  try {
    await authStore.register(form.username, form.email, form.password)
    router.push('/')
  } catch {
    // Error set in store
  }
}

function handleGoogleLogin() {
  authStore.loginWithGoogle()
}

const displayError = ref(null)
</script>

<template>
  <div class="auth-page">
    <div class="auth-container glass-card">
      <!-- Header -->
      <div class="auth-header">
        <div class="logo">
          <span class="logo-icon">🎓</span>
          <h1>Ngambis<span class="accent">.ai</span></h1>
        </div>
        <p class="subtitle">Create your account</p>
      </div>

      <!-- Form -->
      <form @submit.prevent="handleRegister" class="auth-form">
        <div class="form-group">
          <label for="reg-username">Username</label>
          <div class="input-wrapper">
            <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
            <input id="reg-username" v-model="form.username" type="text" placeholder="Choose a username" required autocomplete="username" minlength="3" />
          </div>
        </div>

        <div class="form-group">
          <label for="reg-email">Email</label>
          <div class="input-wrapper">
            <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="4" width="20" height="16" rx="2"/><path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7"/></svg>
            <input id="reg-email" v-model="form.email" type="email" placeholder="Enter your email" required autocomplete="email" />
          </div>
        </div>

        <div class="form-group">
          <label for="reg-password">Password</label>
          <div class="input-wrapper">
            <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
            <input id="reg-password" v-model="form.password" :type="showPassword ? 'text' : 'password'" placeholder="Min. 6 characters" required autocomplete="new-password" minlength="6" />
            <button type="button" class="toggle-password" @click="showPassword = !showPassword" tabindex="-1">
              {{ showPassword ? '🙈' : '👁️' }}
            </button>
          </div>
        </div>

        <div class="form-group">
          <label for="reg-confirm">Confirm Password</label>
          <div class="input-wrapper">
            <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
            <input id="reg-confirm" v-model="form.confirmPassword" :type="showPassword ? 'text' : 'password'" placeholder="Confirm your password" required autocomplete="new-password" />
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

      <!-- Google -->
      <button @click="handleGoogleLogin" class="btn-google">
        <svg class="google-icon" viewBox="0 0 24 24" width="20" height="20">
          <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 01-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z" fill="#4285F4"/>
          <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
          <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
          <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
        </svg>
        <span>Sign up with Google</span>
      </button>

      <p class="auth-link">
        Already have an account?
        <router-link to="/login">Sign In</router-link>
      </p>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 1rem;
}

.auth-container { width: 100%; max-width: 420px; padding: 2.5rem; }
.auth-header { text-align: center; margin-bottom: 2rem; }
.logo { display: flex; align-items: center; justify-content: center; gap: 0.5rem; margin-bottom: 0.5rem; }
.logo-icon { font-size: 2rem; }
.logo h1 { font-size: 1.75rem; font-weight: 700; color: var(--color-text-primary); }
.accent { color: var(--color-primary); }
.subtitle { color: var(--color-text-muted); font-size: var(--font-size-sm); }
.auth-form { display: flex; flex-direction: column; gap: 1.125rem; }
.form-group { display: flex; flex-direction: column; gap: 0.5rem; }
.form-group label { color: var(--color-text-secondary); font-size: var(--font-size-sm); font-weight: 500; }
.input-wrapper { position: relative; display: flex; align-items: center; }
.input-icon { position: absolute; left: 0.875rem; color: var(--color-text-muted); pointer-events: none; }
.input-wrapper input { width: 100%; padding: 0.875rem 1rem 0.875rem 2.75rem; background: rgba(255,255,255,0.06); border: 1px solid var(--color-border); border-radius: var(--radius-md); color: var(--color-text-primary); font-size: var(--font-size-base); transition: all var(--transition-fast); outline: none; }
.input-wrapper input::placeholder { color: var(--color-text-disabled); }
.input-wrapper input:focus { border-color: var(--color-primary); background: rgba(108,99,255,0.06); box-shadow: 0 0 0 3px var(--color-primary-glow); }
.toggle-password { position: absolute; right: 0.75rem; background: none; border: none; font-size: 1rem; cursor: pointer; padding: 0.25rem; line-height: 1; }
.btn-primary { width: 100%; padding: 0.875rem; background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark)); color: white; border: none; border-radius: var(--radius-md); font-size: var(--font-size-base); font-weight: 600; transition: all var(--transition-fast); display: flex; align-items: center; justify-content: center; gap: 0.5rem; }
.btn-primary:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 8px 25px var(--color-primary-glow); }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }
.spinner { width: 20px; height: 20px; border: 2px solid rgba(255,255,255,0.3); border-top-color: white; border-radius: 50%; animation: spin 0.6s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.error-message { color: var(--color-error); font-size: var(--font-size-sm); text-align: center; padding: 0.5rem; background: rgba(239,68,68,0.1); border-radius: var(--radius-sm); border: 1px solid rgba(239,68,68,0.2); }
.divider { display: flex; align-items: center; gap: 1rem; margin: 1.5rem 0; }
.divider::before, .divider::after { content: ''; flex: 1; height: 1px; background: var(--color-border); }
.divider span { color: var(--color-text-muted); font-size: var(--font-size-xs); font-weight: 600; letter-spacing: 0.1em; }
.btn-google { width: 100%; padding: 0.875rem; background: rgba(255,255,255,0.06); border: 1px solid var(--color-border); border-radius: var(--radius-md); color: var(--color-text-primary); font-size: var(--font-size-base); font-weight: 500; display: flex; align-items: center; justify-content: center; gap: 0.75rem; transition: all var(--transition-fast); }
.btn-google:hover { background: rgba(255,255,255,0.1); border-color: var(--color-border-hover); transform: translateY(-1px); }
.auth-link { text-align: center; color: var(--color-text-muted); font-size: var(--font-size-sm); margin-top: 1.5rem; }
.auth-link a { color: var(--color-primary-light); font-weight: 600; }
.auth-link a:hover { color: var(--color-primary); }
@media (max-width: 480px) { .auth-container { padding: 1.75rem; } .logo h1 { font-size: 1.5rem; } }
</style>
