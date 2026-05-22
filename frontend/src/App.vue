<script setup>
import { computed, ref, provide, watch, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import AppSidebar from '@/components/layout/AppSidebar.vue'
import AuthModal from '@/components/AuthModal.vue'

const route = useRoute()
const authStore = useAuthStore()

// ===== Sidebar State (Global) =====
const sidebarOpen = ref(false)
const isMobile = ref(window.innerWidth <= 768)

function toggleSidebar() {
  sidebarOpen.value = !sidebarOpen.value
  if (sidebarOpen.value && isMobile.value) {
    document.body.classList.add('sidebar-open')
  } else {
    document.body.classList.remove('sidebar-open')
  }
}

function closeSidebar() {
  sidebarOpen.value = false
  document.body.classList.remove('sidebar-open')
}

function handleResize() {
  isMobile.value = window.innerWidth <= 768
  if (!isMobile.value) {
    closeSidebar() // Reset on desktop
  }
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  document.body.classList.remove('sidebar-open')
})

// Close sidebar when navigating on mobile
watch(() => route.path, () => {
  if (isMobile.value) closeSidebar()
})

// Provide sidebar controls to all child components
provide('sidebarOpen', sidebarOpen)
provide('toggleSidebar', toggleSidebar)
provide('closeSidebar', closeSidebar)
provide('isMobile', isMobile)

// Show sidebar only on authenticated pages (not on login/register/callback)
const showSidebar = computed(() => {
  const noSidebarRoutes = ['Welcome', 'Login', 'Register', 'OAuth2Callback', 'NotFound']
  return authStore.isAuthenticated && !noSidebarRoutes.includes(route.name)
})

// Dashboard shows sidebar only on mobile (burger menu), hidden on desktop
const sidebarMobileOnly = computed(() => route.name === 'Dashboard')

function handleAuthenticated() {
  authStore.closeAuthModal()
}
</script>

<template>
  <div class="app-layout" :class="{ 'with-sidebar': showSidebar, 'sidebar-mobile-only': sidebarMobileOnly && showSidebar }">
    <!-- Sidebar Backdrop (mobile overlay) -->
    <div
      v-if="showSidebar && isMobile"
      class="sidebar-backdrop"
      :class="{ active: sidebarOpen }"
      @click="closeSidebar"
    ></div>

    <AppSidebar
      v-if="showSidebar"
      :open="sidebarOpen"
      @close="closeSidebar"
    />

    <main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- Global Auth Modal -->
    <AuthModal
      v-model="authStore.showAuthModal"
      :message="authStore.authModalMessage"
      :redirect-to="authStore.authModalRedirect"
      @authenticated="handleAuthenticated"
    />
  </div>
</template>

<style scoped>
.app-layout {
  display: flex;
  min-height: 100vh;
  max-width: 100%;
}

.main-content {
  flex: 1;
  min-height: 100vh;
  overflow-x: hidden;
  min-width: 0;
}

.app-layout.with-sidebar .main-content {
  margin-left: var(--sidebar-width);
  transition: margin-left var(--transition-base);
  background: #FFFFFF;
}

@media (max-width: 768px) {
  .app-layout.with-sidebar .main-content {
    margin-left: 0;
  }
}

/* Dashboard: hide sidebar on desktop, keep it available for mobile burger */
@media (min-width: 769px) {
  .app-layout.sidebar-mobile-only :deep(.sidebar) {
    transform: translateX(-100%);
    pointer-events: none;
  }

  .app-layout.sidebar-mobile-only .main-content {
    margin-left: 0;
  }
}
</style>
