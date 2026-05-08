<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import AppSidebar from '@/components/layout/AppSidebar.vue'
import AuthModal from '@/components/AuthModal.vue'

const route = useRoute()
const authStore = useAuthStore()

// Show sidebar only on authenticated pages (not on login/register/callback)
const showSidebar = computed(() => {
  const noSidebarRoutes = ['Welcome', 'Dashboard', 'Login', 'Register', 'OAuth2Callback', 'NotFound']
  return authStore.isAuthenticated && !noSidebarRoutes.includes(route.name)
})

function handleAuthenticated() {
  authStore.closeAuthModal()
}
</script>

<template>
  <div class="app-layout" :class="{ 'with-sidebar': showSidebar }">
    <AppSidebar v-if="showSidebar" />
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
}

.main-content {
  flex: 1;
  min-height: 100vh;
  overflow-x: hidden;
}

.app-layout.with-sidebar .main-content {
  margin-left: var(--sidebar-width);
  transition: margin-left var(--transition-base);
}

@media (max-width: 768px) {
  .app-layout.with-sidebar .main-content {
    margin-left: 0;
  }
}
</style>
