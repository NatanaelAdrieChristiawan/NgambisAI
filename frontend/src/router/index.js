/**
 * Ngambis.ai — Vue Router
 *
 * Route definitions with navigation guards.
 * The Dashboard is public and acts as the landing page.
 * Protected routes trigger auth modal for unauthenticated users.
 */

import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  // ===== Public Routes =====
  {
    path: '/',
    name: 'Dashboard',
    component: () => import('@/views/DashboardView.vue'),
    meta: { requiresAuth: false, title: 'Dashboard' }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { requiresAuth: false, title: 'Login' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/RegisterView.vue'),
    meta: { requiresAuth: false, title: 'Register' }
  },
  {
    path: '/oauth2/callback',
    name: 'OAuth2Callback',
    component: () => import('@/views/auth/OAuth2CallbackView.vue'),
    meta: { requiresAuth: false, title: 'Authenticating...' }
  },

  // ===== Protected Routes =====
  {
    path: '/upload',
    name: 'Upload',
    component: () => import('@/views/UploadView.vue'),
    meta: { requiresAuth: true, title: 'Upload PDF' }
  },
  {
    path: '/quiz/:sessionId?',
    name: 'Quiz',
    component: () => import('@/views/QuizView.vue'),
    meta: { requiresAuth: true, title: 'Quiz Session' }
  },
  {
    path: '/simulator',
    name: 'Simulator',
    component: () => import('@/views/SimulatorView.vue'),
    meta: { requiresAuth: true, title: 'Simulator Ujian Lisan' }
  },
  {
    path: '/history',
    name: 'History',
    component: () => import('@/views/HistoryView.vue'),
    meta: { requiresAuth: true, title: 'Riwayat' }
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
    // Protected route but not logged in → redirect to dashboard & show auth modal
    next({ name: 'Dashboard' })

    // Use nextTick to ensure the store is ready after navigation
    setTimeout(() => {
      authStore.requestAuth(
        'Kamu perlu login terlebih dahulu untuk mengakses fitur ini.',
        to.fullPath
      )
    }, 100)
  } else if (
    to.meta.requiresAuth === false &&
    isAuthenticated &&
    (to.name === 'Login' || to.name === 'Register')
  ) {
    // Already logged in trying to visit login/register → redirect to dashboard
    next({ name: 'Dashboard' })
  } else {
    next()
  }
})

export default router
