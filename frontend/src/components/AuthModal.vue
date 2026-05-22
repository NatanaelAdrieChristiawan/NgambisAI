<script setup>
/**
 * AuthModal.vue
 * Clean white card modal for login/register matching reference design.
 */
import { reactive, ref, watch, nextTick } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  message: { type: String, default: '' },
  redirectTo: { type: String, default: null }
})

const emit = defineEmits(['update:modelValue', 'authenticated'])
const authStore = useAuthStore()
const router = useRouter()

const activeTab = ref('login')
const loginForm = reactive({ username: '', password: '' })
const registerForm = reactive({ username: '', email: '', password: '', confirmPassword: '' })
const showPassword = ref(false)
const localError = ref(null)
const isVisible = ref(false)
const isClosing = ref(false)

watch(() => props.modelValue, (val) => {
  if (val) {
    isClosing.value = false
    nextTick(() => { isVisible.value = true })
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
  loginForm.username = ''; loginForm.password = ''
  registerForm.username = ''; registerForm.email = ''; registerForm.password = ''; registerForm.confirmPassword = ''
  showPassword.value = false; localError.value = null; authStore.clearError()
}

function switchTab(tab) { activeTab.value = tab; localError.value = null; authStore.clearError() }

async function handleLogin() {
  authStore.clearError(); localError.value = null
  try {
    await authStore.login(loginForm.username, loginForm.password)
    emit('authenticated'); closeModal()
    if (props.redirectTo) router.push(props.redirectTo)
  } catch { /* Error in store */ }
}

async function handleRegister() {
  authStore.clearError(); localError.value = null
  if (registerForm.password !== registerForm.confirmPassword) { localError.value = 'Kata sandi tidak cocok.'; return }
  if (registerForm.password.length < 6) { localError.value = 'Kata sandi minimal 6 karakter.'; return }
  try {
    await authStore.register(registerForm.username, registerForm.email, registerForm.password)
    emit('authenticated'); closeModal()
    if (props.redirectTo) router.push(props.redirectTo)
  } catch { /* Error in store */ }
}

function handleGoogleLogin() { authStore.loginWithGoogle() }
function handleOverlayClick(e) { if (e.target === e.currentTarget) closeModal() }
</script>

<template>
  <Teleport to="body">
    <Transition name="modal-overlay">
      <div v-if="modelValue" class="auth-modal-overlay" :class="{ visible: isVisible, closing: isClosing }" @click="handleOverlayClick" @keydown.esc="closeModal">
        <Transition name="modal-content">
          <div v-if="isVisible" class="auth-modal" role="dialog" aria-modal="true" aria-label="Authentication">
            <!-- Close -->
            <button class="modal-close" @click="closeModal" aria-label="Tutup">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
            </button>

            <!-- Header -->
            <div class="modal-header">
              <div class="modal-logo">
              <img v-if="activeTab === 'login'" class="logo-icon" src="/logo/ngambis.png" alt="Ngambis.AI" width="30" height="30"/>
                <h2><span class="brand-dark">NGAMBIS</span><span class="brand-accent">.AI</span></h2>
              </div>
              <p class="modal-subtitle">{{ activeTab === 'login' ? 'SIMULATOR KUIS INTERAKTIF & UJIAN LISAN' : 'PROTOKOL NGAMBIS.AI' }}</p>
            </div>

            <!-- Tabs -->
            <div class="auth-tabs">
              <button class="tab-btn" :class="{ active: activeTab === 'login' }" @click="switchTab('login')">Masuk</button>
              <button class="tab-btn" :class="{ active: activeTab === 'register' }" @click="switchTab('register')">Daftar</button>
              <div class="tab-indicator" :class="{ right: activeTab === 'register' }"></div>
            </div>

            <!-- Login Form -->
            <form v-if="activeTab === 'login'" @submit.prevent="handleLogin" class="modal-form">
              <div class="form-group">
                <label for="modal-login-username">Nama Pengguna</label>
                <div class="input-wrapper">
                  <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                  <input id="modal-login-username" v-model="loginForm.username" type="text" placeholder="Masukkan nama pengguna" required autocomplete="username"/>
                </div>
              </div>
              <div class="form-group">
                <label for="modal-login-password">Kata Sandi</label>
                <div class="input-wrapper">
                  <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
                  <input id="modal-login-password" v-model="loginForm.password" :type="showPassword ? 'text' : 'password'" placeholder="••••••••" required autocomplete="current-password"/>
                  <button type="button" class="toggle-password" @click="showPassword = !showPassword" tabindex="-1" aria-label="Toggle password">
                    <svg v-if="!showPassword" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                    <svg v-else xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
                  </button>
                </div>
                <div class="forgot-link"><a href="#">Lupa kata sandi?</a></div>
              </div>
              <button type="submit" class="btn-primary" :disabled="authStore.loading">
                <span v-if="authStore.loading" class="spinner"></span>
                <span v-else>Masuk</span>
              </button>
              <p v-if="authStore.error" class="error-message">{{ authStore.error }}</p>
            </form>

            <!-- Register Form -->
            <form v-if="activeTab === 'register'" @submit.prevent="handleRegister" class="modal-form">
              <div class="form-group">
                <label for="modal-reg-username">Nama Pengguna</label>
                <div class="input-wrapper">
                  <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                  <input id="modal-reg-username" v-model="registerForm.username" type="text" placeholder="Masukkan handle Anda" required autocomplete="username" minlength="3"/>
                </div>
              </div>
              <div class="form-group">
                <label for="modal-reg-email">Alamat Email</label>
                <div class="input-wrapper">
                  <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="4" width="20" height="16" rx="2"/><path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7"/></svg>
                  <input id="modal-reg-email" v-model="registerForm.email" type="email" placeholder="name@domain.com" required autocomplete="email"/>
                </div>
              </div>
              <div class="form-group">
                <label for="modal-reg-password">Kata Sandi</label>
                <div class="input-wrapper">
                  <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
                  <input id="modal-reg-password" v-model="registerForm.password" :type="showPassword ? 'text' : 'password'" placeholder="Min. 8 karakter" required autocomplete="new-password" minlength="6"/>
                  <button type="button" class="toggle-password" @click="showPassword = !showPassword" tabindex="-1" aria-label="Toggle password">
                    <svg v-if="!showPassword" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
                    <svg v-else xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94"/><line x1="1" y1="1" x2="23" y2="23"/></svg>
                  </button>
                </div>
              </div>
              <div class="form-group">
                <label for="modal-reg-confirm">Konfirmasi Kata Sandi</label>
                <div class="input-wrapper">
                  <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>
                  <input id="modal-reg-confirm" v-model="registerForm.confirmPassword" :type="showPassword ? 'text' : 'password'" placeholder="Ulangi kata sandi" required autocomplete="new-password"/>
                </div>
              </div>
              <button type="submit" class="btn-primary" :disabled="authStore.loading">
                <span v-if="authStore.loading" class="spinner"></span>
                <span v-else>Daftar Akun</span>
              </button>
              <p v-if="localError || authStore.error" class="error-message">{{ localError || authStore.error }}</p>
            </form>

            <!-- Divider -->
            <div class="divider"><span>ATAU</span></div>

            <!-- Google -->
            <button @click="handleGoogleLogin" class="btn-google">
              <svg class="google-icon" viewBox="0 0 24 24" width="20" height="20">
                <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 01-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z" fill="#4285F4"/>
                <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
                <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
                <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
              </svg>
              <span>{{ activeTab === 'login' ? 'Masuk dengan Google' : 'Daftar dengan Google' }}</span>
            </button>

            <!-- Switch link -->
            <p class="auth-switch">
              <template v-if="activeTab === 'login'">Belum punya akun? <a href="#" @click.prevent="switchTab('register')">Buat Akun</a></template>
              <template v-else>Sudah punya akun? <a href="#" @click.prevent="switchTab('login')">Masuk</a></template>
            </p>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.auth-modal-overlay {
  position: fixed; inset: 0; z-index: 9999;
  display: flex; align-items: center; justify-content: center;
  background: rgba(0,0,0,0);
  transition: background 0.3s ease-out;
  padding: 1rem;
}
.auth-modal-overlay.visible { background: rgba(0,0,0,0.4); }
.auth-modal-overlay.closing { background: rgba(0,0,0,0); }

.auth-modal {
  position: relative; width: 100%; max-width: 440px; max-height: 90vh; overflow-y: auto;
  background: #ffffff; border-radius: 24px;
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05), 0 20px 50px -12px rgba(79,70,229,0.15);
  padding: 2.25rem 2rem;
  will-change: transform, opacity;
}

.modal-close {
  position: absolute; top: 1rem; right: 1rem;
  width: 36px; height: 36px; display: flex; align-items: center; justify-content: center;
  background: transparent; border: none; border-radius: 8px;
  color: #94a3b8; cursor: pointer; transition: all 200ms ease; z-index: 2;
}
.modal-close:hover { background: #f1f5f9; color: #475569; }

.modal-header { text-align: center; margin-bottom: 1.5rem; }
.modal-logo { display: flex; align-items: center; justify-content: center; gap: 0.5rem; margin-bottom: 0.375rem; }
.logo-icon { flex-shrink: 0; }
.modal-logo h2 { font-size: 1.5rem; font-weight: 800; letter-spacing: -0.01em; margin: 0; }
.brand-dark { color: #1e1b4b; }
.brand-accent { color: #4F46E5; }
.modal-subtitle { color: #94a3b8; font-size: 0.65rem; font-weight: 500; letter-spacing: 0.15em; }

.auth-tabs {
  position: relative; display: flex; background: #f1f5f9; border-radius: 12px;
  padding: 4px; margin-bottom: 1.5rem;
}
.tab-btn {
  flex: 1; padding: 0.625rem; background: transparent; border: none;
  color: #64748b; font-size: 0.875rem; font-weight: 600;
  cursor: pointer; transition: color 200ms ease; z-index: 1; position: relative; border-radius: 8px;
}
.tab-btn.active { color: #1e293b; }
.tab-btn:hover:not(.active) { color: #475569; }
.tab-indicator {
  position: absolute; top: 4px; left: 4px; width: calc(50% - 4px); height: calc(100% - 8px);
  background: #ffffff; border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  transition: transform 0.3s cubic-bezier(0.16,1,0.3,1);
}
.tab-indicator.right { transform: translateX(100%); }

.modal-form { display: flex; flex-direction: column; gap: 1rem; animation: fadeInForm 0.25s ease; }
@keyframes fadeInForm { from { opacity: 0; transform: translateY(8px); } to { opacity: 1; transform: translateY(0); } }

.form-group { display: flex; flex-direction: column; gap: 0.375rem; }
.form-group label { color: #1e293b; font-size: 0.875rem; font-weight: 600; }
.input-wrapper { position: relative; display: flex; align-items: center; }
.input-icon { position: absolute; left: 1rem; color: #94a3b8; pointer-events: none; transition: color 200ms ease; }
.input-wrapper input {
  width: 100%; padding: 0.8rem 1rem 0.8rem 2.75rem;
  background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 12px;
  color: #1e293b; font-size: 0.875rem; transition: all 200ms ease; outline: none;
}
.input-wrapper input::placeholder { color: #94a3b8; }
.input-wrapper input:focus { border-color: #818cf8; background: #fff; box-shadow: 0 0 0 3px rgba(99,102,241,0.1); }
.input-wrapper:focus-within .input-icon { color: #6366f1; }

.toggle-password {
  position: absolute; right: 0.875rem; background: none; border: none;
  color: #94a3b8; cursor: pointer; padding: 0.25rem; line-height: 1;
  display: flex; align-items: center; transition: color 200ms ease;
}
.toggle-password:hover { color: #64748b; }

.forgot-link { text-align: right; }
.forgot-link a { color: #6366f1; font-size: 0.8125rem; font-weight: 500; transition: color 200ms ease; }
.forgot-link a:hover { color: #4338ca; }

.btn-primary {
  width: 100%; padding: 0.875rem;
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  color: #fff; border: none; border-radius: 14px;
  font-size: 0.9375rem; font-weight: 700;
  transition: all 250ms ease; display: flex; align-items: center; justify-content: center;
  gap: 0.5rem; margin-top: 0.25rem; cursor: pointer;
}
.btn-primary:hover:not(:disabled) { background: linear-gradient(135deg,#4f46e5,#4338ca); transform: translateY(-1px); box-shadow: 0 8px 24px rgba(79,70,229,0.35); }
.btn-primary:active:not(:disabled) { transform: translateY(0); }
.btn-primary:disabled { opacity: 0.6; cursor: not-allowed; }

.spinner { width: 18px; height: 18px; border: 2px solid rgba(255,255,255,0.3); border-top-color: #fff; border-radius: 50%; animation: spin 0.6s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.error-message { color: #dc2626; font-size: 0.8125rem; text-align: center; padding: 0.5rem 0.75rem; background: #fef2f2; border-radius: 10px; border: 1px solid #fecaca; }

.divider { display: flex; align-items: center; gap: 1rem; margin: 1.25rem 0; }
.divider::before, .divider::after { content: ''; flex: 1; height: 1px; background: #e2e8f0; }
.divider span { color: #94a3b8; font-size: 0.6875rem; font-weight: 600; letter-spacing: 0.15em; }

.btn-google {
  width: 100%; padding: 0.8rem; background: #fff; border: 1px solid #e2e8f0;
  border-radius: 14px; color: #334155; font-size: 0.875rem; font-weight: 500;
  display: flex; align-items: center; justify-content: center; gap: 0.75rem;
  transition: all 200ms ease; cursor: pointer;
}
.btn-google:hover { background: #f8fafc; border-color: #cbd5e1; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.google-icon { flex-shrink: 0; }

.auth-switch { text-align: center; color: #64748b; font-size: 0.875rem; margin-top: 1.5rem; }
.auth-switch a { color: #4f46e5; font-weight: 600; transition: color 200ms ease; }
.auth-switch a:hover { color: #4338ca; }

.modal-content-enter-active,
.modal-content-leave-active {
  transition: opacity 0.3s ease-out, transform 0.3s ease-out;
}
.modal-content-enter-from,
.modal-content-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.98);
}

.auth-modal::-webkit-scrollbar { width: 4px; }
.auth-modal::-webkit-scrollbar-thumb { background: #cbd5e1; border-radius: 4px; }

@media (max-width: 480px) {
  .auth-modal { padding: 1.5rem; max-width: 100%; border-radius: 20px; max-height: 95vh; }
  .modal-logo h2 { font-size: 1.25rem; }
  .modal-form { gap: 0.875rem; }
}
@media (max-height: 700px) {
  .auth-modal { max-height: 95vh; }
  .modal-header { margin-bottom: 1rem; }
  .auth-tabs { margin-bottom: 1rem; }
  .divider { margin: 0.875rem 0; }
}
@media (prefers-reduced-motion: reduce) {
  .auth-modal { animation: none; }
  .btn-primary:hover:not(:disabled) { transform: none; }
}
</style>
