/**
 * Ngambis.ai — Vue Router
 *
 * Route definitions with navigation guards.
 * Welcome page is the public landing page.
 * Dashboard and other routes require authentication.
 */

import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  // ===== Public Routes =====
  {
    path: '/',
    name: 'Welcome',
    component: () => import('@/views/WelcomeView.vue'),
    meta: { requiresAuth: false, title: 'Welcome' }
  },
  {
    path: '/oauth2/callback',
    name: 'OAuth2Callback',
    component: () => import('@/views/auth/OAuth2CallbackView.vue'),
    meta: { requiresAuth: false, title: 'Authenticating...' }
  },

  // ===== Protected Routes =====
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/DashboardView.vue'),
    meta: { requiresAuth: true, title: 'Dashboard' }
  },
  {
    path: '/chat',
    name: 'AIChat',
    component: () => import('@/views/AIChatView.vue'),
    meta: { requiresAuth: true, title: 'AI Chat' }
  },
  {
    path: '/flashcards',
    name: 'Flashcards',
    component: () => import('@/views/FlashcardsView.vue'),
    meta: { requiresAuth: true, title: 'Flashcards' }
  },
  {
    path: '/quiz-mode',
    name: 'QuizMode',
    component: () => import('@/views/QuizModeView.vue'),
    meta: { requiresAuth: true, title: 'Quiz Mode' }
  },
  {
    path: '/voice-to-speech',
    name: 'VoiceToSpeech',
    component: () => import('@/views/VoiceToSpeechView.vue'),
    meta: { requiresAuth: true, title: 'Voice To Speech' }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('@/views/SettingsView.vue'),
    meta: { requiresAuth: true, title: 'Settings' }
  },

  // ===== Catch-all 404 =====
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFoundView.vue'),
    meta: { requiresAuth: false, title: '404 Not Found' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    return savedPosition || { top: 0 }
  }
})

// =============================================
// Navigation Guard
// =============================================
router.beforeEach((to, from, next) => {
  // Update page title
  document.title = to.meta.title
    ? `${to.meta.title} — Ngambis.ai`
    : 'Ngambis.ai'

  const authStore = useAuthStore()
  const isAuthenticated = authStore.isAuthenticated

  if (to.meta.requiresAuth && !isAuthenticated) {
    // Protected route but not logged in → redirect to Welcome & show auth modal
    next({ name: 'Welcome' })

    // Use nextTick to ensure the store is ready after navigation
    setTimeout(() => {
      authStore.requestAuth(
        'Kamu perlu login terlebih dahulu untuk mengakses fitur ini.',
        to.fullPath
      )
    }, 100)
  } else if (
    isAuthenticated &&
    (to.name === 'Welcome' || to.name === 'Login' || to.name === 'Register')
  ) {
    // Already logged in trying to visit welcome/login/register → redirect to dashboard
    next({ name: 'Dashboard' })
  } else {
    next()
  }
})

export default router
