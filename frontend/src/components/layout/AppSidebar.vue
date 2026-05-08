<script setup>
/**
 * AppSidebar.vue — Light-themed sidebar matching reference design.
 * Features: Logo + subtitle, navigation menu, chat history section, settings link.
 */
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const authStore = useAuthStore()

const menuItems = [
  { name: 'Dashboard', icon: 'dashboard', route: 'Dashboard' },
  { name: 'AI Chat', icon: 'chat', route: 'AIChat' },
  { name: 'Flashcards', icon: 'flashcards', route: 'Flashcards' },
  { name: 'Quiz Mode', icon: 'quiz', route: 'QuizMode' },
  { name: 'Voice To Speech', icon: 'voice', route: 'VoiceToSpeech' }
]

const chatHistory = [
  { id: 1, title: 'Algoritma Dijkstra', time: '2 menit yang lalu', active: true },
  { id: 2, title: 'Struktur Data Binary Tree', time: 'Kemarin', active: false },
  { id: 3, title: 'Pengenalan Machine Learning', time: '3 hari yang lalu', active: false },
  { id: 4, title: 'Analisis Kompleksitas Waktu', time: 'Minggu lalu', active: false }
]

const isActive = (routeName) => route.name === routeName
</script>

<template>
  <aside class="sidebar">
    <!-- Logo -->
    <div class="sidebar-logo">
      <div class="logo-icon-wrap">
        <svg width="28" height="28" viewBox="0 0 32 32" fill="none">
          <rect width="32" height="32" rx="8" fill="#3B82F6"/>
          <path d="M10 22V12l6 4-6 4z" fill="white" opacity="0.9"/>
          <path d="M16 22V12l6 4-6 4z" fill="white" opacity="0.6"/>
        </svg>
      </div>
      <div>
        <h2>NGAMBIS<span class="accent">.AI</span></h2>
        <span class="subtitle">AI Learning Partner</span>
      </div>
    </div>

    <!-- Navigation -->
    <nav class="sidebar-nav">
      <router-link
        v-for="item in menuItems"
        :key="item.route"
        :to="{ name: item.route }"
        class="nav-item"
        :class="{ active: isActive(item.route) }"
      >
        <span class="nav-icon">
          <!-- Dashboard -->
          <svg v-if="item.icon==='dashboard'" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>
          <!-- AI Chat -->
          <svg v-if="item.icon==='chat'" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
          <!-- Flashcards -->
          <svg v-if="item.icon==='flashcards'" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="3" width="20" height="14" rx="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/></svg>
          <!-- Quiz -->
          <svg v-if="item.icon==='quiz'" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg>
          <!-- Voice -->
          <svg v-if="item.icon==='voice'" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3z"/><path d="M19 10v2a7 7 0 0 1-14 0v-2"/><line x1="12" y1="19" x2="12" y2="23"/><line x1="8" y1="23" x2="16" y2="23"/></svg>
        </span>
        <span class="nav-label">{{ item.name }}</span>
      </router-link>
    </nav>

    <!-- Chat History -->
    <div class="chat-history">
      <div class="history-label">RIWAYAT CHAT</div>
      <div class="history-list">
        <div
          v-for="chat in chatHistory"
          :key="chat.id"
          class="history-item"
          :class="{ active: chat.active }"
        >
          <span class="history-title">{{ chat.title }}</span>
          <span class="history-time">{{ chat.time }}</span>
        </div>
      </div>
    </div>

    <!-- Settings -->
    <div class="sidebar-footer">
      <router-link :to="{ name: 'Settings' }" class="nav-item settings-link" :class="{ active: isActive('Settings') }">
        <span class="nav-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg>
        </span>
        <span class="nav-label">Settings</span>
      </router-link>
    </div>
  </aside>
</template>

<style scoped>
.sidebar {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  width: var(--sidebar-width);
  background: #FFFFFF;
  border-right: 1px solid #E2E8F0;
  display: flex;
  flex-direction: column;
  z-index: 100;
  overflow-y: auto;
}

/* Logo */
.sidebar-logo {
  display: flex;
  align-items: center;
  gap: 0.625rem;
  padding: 1.25rem 1.25rem 0.75rem;
}

.logo-icon-wrap {
  flex-shrink: 0;
}

.sidebar-logo h2 {
  font-size: 1rem;
  font-weight: 800;
  color: #0F172A;
  margin: 0;
  letter-spacing: 0.02em;
}

.accent {
  color: #3B82F6;
}

.subtitle {
  display: block;
  font-size: 0.6875rem;
  color: #94A3B8;
  font-weight: 500;
  margin-top: 1px;
}

/* Navigation */
.sidebar-nav {
  padding: 0.5rem 0.75rem;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 0.625rem;
  padding: 0.625rem 0.875rem;
  border-radius: 8px;
  color: #475569;
  text-decoration: none;
  font-size: 0.875rem;
  font-weight: 500;
  transition: all 0.15s ease;
  position: relative;
}

.nav-item:hover {
  background: #F1F5F9;
  color: #1E293B;
}

.nav-item.active {
  color: #3B82F6;
  background: #EFF6FF;
  font-weight: 600;
}

.nav-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 60%;
  background: #3B82F6;
  border-radius: 0 3px 3px 0;
}

.nav-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  flex-shrink: 0;
}

/* Chat History */
.chat-history {
  flex: 1;
  padding: 0.5rem 0.75rem;
  min-height: 0;
  overflow-y: auto;
}

.history-label {
  font-size: 0.6875rem;
  font-weight: 700;
  color: #94A3B8;
  letter-spacing: 0.08em;
  padding: 0.5rem 0.875rem 0.5rem;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.history-item {
  padding: 0.625rem 0.875rem;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.history-item:hover {
  background: #F1F5F9;
}

.history-item.active {
  background: #3B82F6;
}

.history-item .history-title {
  display: block;
  font-size: 0.8125rem;
  font-weight: 500;
  color: #1E293B;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.history-item.active .history-title {
  color: white;
  font-weight: 600;
}

.history-item .history-time {
  display: block;
  font-size: 0.6875rem;
  color: #94A3B8;
  margin-top: 2px;
}

.history-item.active .history-time {
  color: rgba(255, 255, 255, 0.7);
}

/* Footer / Settings */
.sidebar-footer {
  padding: 0.5rem 0.75rem 1rem;
  border-top: 1px solid #F1F5F9;
}

.settings-link {
  margin-top: 0.25rem;
}

@media (max-width: 768px) {
  .sidebar {
    transform: translateX(-100%);
    transition: transform 0.25s ease;
  }

  .sidebar.open {
    transform: translateX(0);
  }
}
</style>
