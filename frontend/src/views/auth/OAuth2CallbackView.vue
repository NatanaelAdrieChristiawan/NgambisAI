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
  router.push({ name: 'Login' })
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
        <div class="error-icon">⚠️</div>
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
  font-size: 3rem;
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
