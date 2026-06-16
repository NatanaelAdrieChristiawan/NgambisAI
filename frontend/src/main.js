/**
 * Ngambis.ai — Application Entry Point
 *
 * Bootstraps Vue app with Pinia (state), Router, and global styles.
 * Initializes auth state on startup to restore logged-in session.
 */

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import App from './App.vue'
import router from './router'
import { useAuthStore } from './stores/auth'

// Global styles
import './assets/styles/main.css'

// Create app
const app = createApp(App)

// Install plugins
const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)  // Persist auth state across page refreshes
app.use(pinia)
app.use(router)

// Initialize auth state before mounting
// Even with persistence, we still call initAuth() to refresh from server
// This ensures user data is always fresh and token is valid
const authStore = useAuthStore()
authStore.initAuth()
  .catch((err) => {
    console.error('Failed to initialize authentication state:', err)
  })
  .finally(() => {
    app.mount('#app')
  })
