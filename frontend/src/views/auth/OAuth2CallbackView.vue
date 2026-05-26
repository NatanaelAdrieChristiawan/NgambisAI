<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const error = ref(null)

onMounted(async () => {
  try {
    authStore.handleOAuth2Callback()
    // Fetch full profile after setting tokens
    await authStore.fetchProfile()
    router.replace({ name: 'Dashboard' })
  } catch (err) {
    error.value = err.message || 'Authentication failed. Please try again.'
  }
})

function goToLogin() {
  router.push({ name: 'Welcome' }).then(() => {
    authStore.requestAuth('Silakan masuk kembali.')
  })
}
</script>

<template>
  <div class="callback-page">
    <div class="callback-container">
      <template v-if="!error">
        <div class="spinner-large"></div>
        <p class="loading-text">Authenticating with Google...</p>
      </template>
      <template v-else>
        <div class="error-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path>
            <line x1="12" y1="9" x2="12" y2="13"></line>
            <line x1="12" y1="17" x2="12.01" y2="17"></line>
          </svg>
        </div>
        <p class="error-text">{{ error }}</p>
        <button class="btn-back" @click="goToLogin">Back to Login</button>
      </template>
    </div>
  </div>
</template>

<style scoped>
.callback-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
}

.callback-container {
  text-align: center;
  padding: 2rem;
}

.spinner-large {
  width: 56px;
  height: 56px;
  border: 4px solid rgba(255, 255, 255, 0.15);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto 1.5rem;
}

@keyframes spin { to { transform: rotate(360deg); } }

.loading-text {
  color: var(--color-text-secondary);
  font-size: var(--font-size-lg);
}

.error-icon {
  display: flex;
  justify-content: center;
  align-items: center;
  color: var(--color-error, #EF4444);
  margin-bottom: 1rem;
}

.error-text {
  color: var(--color-error);
  font-size: var(--font-size-base);
  margin-bottom: 1.5rem;
}

.btn-back {
  padding: 0.75rem 2rem;
  background: var(--color-primary);
  color: white;
  border: none;
  border-radius: var(--radius-md);
  font-size: var(--font-size-base);
  font-weight: 600;
  transition: all var(--transition-fast);
}

.btn-back:hover {
  background: var(--color-primary-dark);
  transform: translateY(-2px);
}
</style>
