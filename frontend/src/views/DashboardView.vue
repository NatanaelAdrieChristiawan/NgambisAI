<script setup>
/**
 * DashboardView.vue — Authenticated dashboard with light theme.
 * Has its own top navbar (no sidebar). Clean layout matching reference.
 */
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import { computed, ref, onMounted } from 'vue'

const authStore = useAuthStore()
const router = useRouter()
const displayName = computed(() => authStore.userName || 'User')
const avatarUrl = computed(() => authStore.userAvatar)
const initials = computed(() => {
  const n = displayName.value
  return n.split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2)
})

const showUserMenu = ref(false)
function toggleUserMenu() { showUserMenu.value = !showUserMenu.value }
function closeUserMenu() { showUserMenu.value = false }
function handleLogout() { authStore.logout(); closeUserMenu() }

const historyItems = ref([
  { id: 1, category: 'JARINGAN KOMPUTER', title: 'Protokol TCP/IP & Keamanan', lastEdited: 'Terakhir diedit 2 jam yang lalu' },
  { id: 2, category: 'BASIS DATA', title: 'Normalisasi & Optimasi Query', lastEdited: 'Terakhir diedit 1 hari yang lalu' }
])

const learningTools = [
  { icon: 'flashcard', iconBg: '#3B82F6', title: 'Smart Flashcards', description: 'Ubah catatan rumit menjadi kartu memori interaktif menggunakan AI. Cocok untuk menghafal terminologi OS.', tag: '24 Decks Tersedia', tagColor: '#3B82F6', action: 'Mulai Belajar', actionIcon: '↗', route: '/upload' },
  { icon: 'voice', iconBg: '#10B981', title: 'Voice to Speech', description: 'Debat dengan AI dengan Real Time', tag: 'AI Transkripsi Aktif', tagColor: '#10B981', action: 'Berdebat', actionIcon: '🔊', route: '/simulator' },
  { icon: 'quiz', iconBg: '#F59E0B', title: 'Adaptive Quiz', description: 'Tes pemahamanmu dengan kuis yang dipersonalisasi. Tingkat kesulitan menyesuaikan progres belajarmu.', tag: 'Level: Intermediate', tagColor: '#F59E0B', action: 'Ambil Quiz', actionIcon: '⚡', route: '/quiz' }
]

function handleNewChat() { router.push('/upload') }
function handleToolClick(tool) { router.push(tool.route) }
function handleStartSession() { router.push('/upload') }

const isReady = ref(false)
onMounted(() => { requestAnimationFrame(() => { isReady.value = true }) })
</script>

<template>
  <div class="dash-page" :class="{ ready: isReady }" @click="closeUserMenu">
    <!-- Top Navbar -->
    <header class="dash-navbar">
      <div class="dash-navbar-inner">
        <div class="dash-brand">
          <svg class="dash-brand-icon" width="28" height="28" viewBox="0 0 32 32" fill="none">
            <rect width="32" height="32" rx="8" fill="#3B82F6"/>
            <path d="M10 22V12l6 4-6 4zM16 22V12l6 4-6 4z" fill="white" opacity="0.9"/>
          </svg>
          <span class="dash-brand-text">NGAMBIS<span class="dash-brand-accent">.AI</span></span>
        </div>
        <div class="dash-user-area" @click.stop>
          <button class="dash-avatar-btn" @click="toggleUserMenu">
            <div v-if="avatarUrl" class="dash-avatar"><img :src="avatarUrl" :alt="displayName" referrerpolicy="no-referrer"/></div>
            <div v-else class="dash-avatar dash-avatar-initials">{{ initials }}</div>
            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#64748B" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="m6 9 6 6 6-6"/></svg>
          </button>
          <div v-if="showUserMenu" class="dash-user-dropdown">
            <div class="dropdown-user-info">
              <span class="dropdown-name">{{ displayName }}</span>
              <span class="dropdown-email">{{ authStore.userEmail }}</span>
            </div>
            <div class="dropdown-divider"></div>
            <button class="dropdown-item" @click="handleLogout">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
              Logout
            </button>
          </div>
        </div>
      </div>
    </header>

    <div class="dash-content">
      <!-- Welcome -->
      <section class="welcome-section">
        <div class="welcome-left">
          <div class="welcome-badge">WELCOME BACK, {{ displayName.toUpperCase() }}!</div>
          <h1 class="welcome-title">Siap Untuk Ngambis<br/>Hari Ini?</h1>
        </div>
        <div class="welcome-right">
          <button class="btn-start" @click="handleStartSession">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/></svg>
            <span>Mulai Sesi Ngambis</span>
          </button>
        </div>
      </section>

      <!-- History -->
      <section class="history-section">
        <div class="sec-label">HISTORY</div>
        <div class="history-grid">
          <button class="h-card h-card-new" @click="handleNewChat">
            <div class="new-icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
            </div>
            <span class="new-label">OBROLAN BARU</span>
          </button>
          <div v-for="item in historyItems" :key="item.id" class="h-card h-card-item">
            <div class="h-card-head"><span class="h-cat">{{ item.category }}</span>
              <button class="h-menu" aria-label="More"><svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><circle cx="12" cy="5" r="2"/><circle cx="12" cy="12" r="2"/><circle cx="12" cy="19" r="2"/></svg></button>
            </div>
            <h3 class="h-title">{{ item.title }}</h3>
            <span class="h-time">{{ item.lastEdited }}</span>
          </div>
        </div>
      </section>

      <!-- Learning Tools -->
      <section class="tools-section">
        <div class="tools-head">
          <h2 class="sec-label">Powerful Learning Tools</h2>
          <div class="tools-nav">
            <button class="tn-btn"><svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="m15 18-6-6 6-6"/></svg></button>
            <button class="tn-btn"><svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="m9 18 6-6-6-6"/></svg></button>
          </div>
        </div>
        <div class="tools-grid">
          <button v-for="(tool, i) in learningTools" :key="tool.title" class="t-card" :style="{'--td': i * 0.1 + 's'}" @click="handleToolClick(tool)">
            <div class="t-icon" :style="{ background: tool.iconBg }">
              <svg v-if="tool.icon==='flashcard'" xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2"><rect x="2" y="3" width="20" height="14" rx="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/></svg>
              <svg v-if="tool.icon==='voice'" xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2"><path d="M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3z"/><path d="M19 10v2a7 7 0 0 1-14 0v-2"/><line x1="12" y1="19" x2="12" y2="23"/><line x1="8" y1="23" x2="16" y2="23"/></svg>
              <svg v-if="tool.icon==='quiz'" xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg>
            </div>
            <h3 class="t-name">{{ tool.title }}</h3>
            <p class="t-desc">{{ tool.description }}</p>
            <div class="t-foot">
              <span class="t-tag" :style="{ color: tool.tagColor, background: tool.tagColor + '15' }">{{ tool.tag }}</span>
              <span class="t-action">{{ tool.action }} {{ tool.actionIcon }}</span>
            </div>
          </button>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
/* ===== Light Theme Dashboard ===== */
.dash-page { background: #F8FAFC; color: #1E293B; min-height: 100vh; }

/* Navbar */
.dash-navbar { position: sticky; top: 0; z-index: 50; background: rgba(255,255,255,0.92); backdrop-filter: blur(12px); border-bottom: 1px solid #E2E8F0; }
.dash-navbar-inner { max-width: 1100px; margin: 0 auto; padding: 0 2rem; height: 60px; display: flex; align-items: center; justify-content: space-between; }
.dash-brand { display: flex; align-items: center; gap: 0.5rem; }
.dash-brand-text { font-size: 1rem; font-weight: 800; color: #1E293B; letter-spacing: 0.02em; }
.dash-brand-accent { color: #3B82F6; }

/* User Area */
.dash-user-area { position: relative; }
.dash-avatar-btn { display: flex; align-items: center; gap: 0.5rem; background: none; border: none; cursor: pointer; padding: 4px; border-radius: 10px; transition: background 0.2s; }
.dash-avatar-btn:hover { background: #F1F5F9; }
.dash-avatar { width: 36px; height: 36px; border-radius: 10px; overflow: hidden; border: 2px solid #E2E8F0; }
.dash-avatar img { width: 100%; height: 100%; object-fit: cover; }
.dash-avatar-initials { display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #3B82F6, #8B5CF6); color: white; font-size: 0.75rem; font-weight: 700; }
.dash-user-dropdown { position: absolute; right: 0; top: calc(100% + 8px); width: 220px; background: white; border: 1px solid #E2E8F0; border-radius: 12px; box-shadow: 0 10px 40px rgba(0,0,0,0.1); padding: 0.5rem; z-index: 60; animation: dropIn 0.2s ease; }
@keyframes dropIn { from { opacity: 0; transform: translateY(-6px); } to { opacity: 1; transform: translateY(0); } }
.dropdown-user-info { padding: 0.625rem 0.75rem; }
.dropdown-name { display: block; font-size: 0.875rem; font-weight: 600; color: #1E293B; }
.dropdown-email { display: block; font-size: 0.75rem; color: #94A3B8; margin-top: 2px; }
.dropdown-divider { height: 1px; background: #F1F5F9; margin: 0.25rem 0; }
.dropdown-item { display: flex; align-items: center; gap: 0.5rem; width: 100%; padding: 0.5rem 0.75rem; background: none; border: none; border-radius: 8px; font-size: 0.8125rem; font-weight: 500; color: #EF4444; cursor: pointer; transition: background 0.15s; }
.dropdown-item:hover { background: #FEF2F2; }

/* Content */
.dash-content { max-width: 1100px; margin: 0 auto; padding: 2rem 2rem 3rem; }

/* Welcome */
.welcome-section { display: flex; align-items: flex-start; justify-content: space-between; gap: 2rem; margin-bottom: 2.5rem; opacity: 0; transform: translateY(20px); transition: opacity 0.6s cubic-bezier(0.16,1,0.3,1), transform 0.6s cubic-bezier(0.16,1,0.3,1); }
.dash-page.ready .welcome-section { opacity: 1; transform: translateY(0); }
.welcome-badge { display: inline-block; padding: 0.25rem 0.75rem; background: linear-gradient(135deg, #3B82F6, #60A5FA); color: white; font-size: 0.6875rem; font-weight: 700; letter-spacing: 0.06em; border-radius: 6px; margin-bottom: 0.75rem; }
.welcome-title { font-size: clamp(1.5rem, 3.5vw, 2rem); font-weight: 800; line-height: 1.25; color: #0F172A; }
.welcome-right { flex-shrink: 0; padding-top: 0.5rem; }
.btn-start { display: inline-flex; align-items: center; gap: 0.5rem; padding: 0.75rem 1.5rem; background: #3B82F6; color: white; border: none; border-radius: 10px; font-size: 0.875rem; font-weight: 600; cursor: pointer; transition: all 0.25s; box-shadow: 0 4px 16px rgba(59,130,246,0.3); white-space: nowrap; }
.btn-start:hover { background: #2563EB; transform: translateY(-2px); box-shadow: 0 8px 24px rgba(59,130,246,0.35); }

/* Section Label */
.sec-label { font-size: 0.8125rem; font-weight: 700; color: #0F172A; letter-spacing: 0.04em; margin-bottom: 1rem; }

/* History */
.history-section { margin-bottom: 2.5rem; opacity: 0; transform: translateY(20px); transition: opacity 0.6s cubic-bezier(0.16,1,0.3,1) 0.1s, transform 0.6s cubic-bezier(0.16,1,0.3,1) 0.1s; }
.dash-page.ready .history-section { opacity: 1; transform: translateY(0); }
.history-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 1rem; }
.h-card { background: white; border: 1px solid #E2E8F0; border-radius: 14px; padding: 1.5rem; text-align: left; min-height: 160px; transition: all 0.3s; }
.h-card:hover { border-color: #CBD5E1; box-shadow: 0 8px 24px rgba(0,0,0,0.06); transform: translateY(-3px); }

.h-card-new { display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 0.75rem; cursor: pointer; color: #1E293B; }
.new-icon { width: 52px; height: 52px; border-radius: 14px; background: #EFF6FF; border: 1.5px dashed #93C5FD; display: flex; align-items: center; justify-content: center; color: #3B82F6; transition: all 0.25s; }
.h-card-new:hover .new-icon { background: #DBEAFE; border-color: #60A5FA; transform: scale(1.05); }
.new-label { font-size: 0.75rem; font-weight: 700; color: #3B82F6; letter-spacing: 0.04em; }

.h-card-item { display: flex; flex-direction: column; }
.h-card-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 0.75rem; }
.h-cat { font-size: 0.6875rem; font-weight: 700; color: #94A3B8; letter-spacing: 0.06em; }
.h-menu { width: 28px; height: 28px; display: flex; align-items: center; justify-content: center; background: none; border: none; color: #CBD5E1; border-radius: 6px; cursor: pointer; transition: all 0.2s; }
.h-menu:hover { background: #F1F5F9; color: #64748B; }
.h-title { font-size: 1rem; font-weight: 700; color: #0F172A; line-height: 1.35; margin-bottom: auto; }
.h-time { font-size: 0.75rem; color: #94A3B8; margin-top: 1rem; }

/* Tools */
.tools-section { opacity: 0; transform: translateY(20px); transition: opacity 0.6s cubic-bezier(0.16,1,0.3,1) 0.2s, transform 0.6s cubic-bezier(0.16,1,0.3,1) 0.2s; }
.dash-page.ready .tools-section { opacity: 1; transform: translateY(0); }
.tools-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 1rem; }
.tools-nav { display: flex; gap: 0.375rem; }
.tn-btn { width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; background: white; border: 1px solid #E2E8F0; border-radius: 8px; color: #94A3B8; cursor: pointer; transition: all 0.2s; }
.tn-btn:hover { background: #F1F5F9; color: #475569; border-color: #CBD5E1; }
.tools-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 1rem; }
.t-card { background: white; border: 1px solid #E2E8F0; border-radius: 14px; padding: 1.5rem; text-align: left; cursor: pointer; transition: all 0.3s; color: #1E293B; display: flex; flex-direction: column; opacity: 0; transform: translateY(16px); animation: tcIn 0.5s cubic-bezier(0.16,1,0.3,1) forwards; animation-delay: var(--td, 0s); animation-play-state: paused; }
.dash-page.ready .t-card { animation-play-state: running; }
@keyframes tcIn { from { opacity: 0; transform: translateY(16px); } to { opacity: 1; transform: translateY(0); } }
.t-card:hover { border-color: #CBD5E1; transform: translateY(-4px); box-shadow: 0 8px 24px rgba(0,0,0,0.06); }
.t-icon { width: 44px; height: 44px; border-radius: 12px; display: flex; align-items: center; justify-content: center; margin-bottom: 1rem; }
.t-name { font-size: 1rem; font-weight: 700; margin-bottom: 0.5rem; color: #0F172A; }
.t-desc { font-size: 0.8125rem; line-height: 1.6; color: #64748B; margin-bottom: 1.25rem; flex: 1; }
.t-foot { display: flex; align-items: center; justify-content: space-between; gap: 0.5rem; flex-wrap: wrap; }
.t-tag { font-size: 0.6875rem; font-weight: 600; padding: 0.25rem 0.625rem; border-radius: 6px; white-space: nowrap; }
.t-action { font-size: 0.8125rem; font-weight: 600; color: #475569; transition: color 0.2s; }
.t-card:hover .t-action { color: #3B82F6; }

/* Responsive */
@media (max-width: 768px) {
  .welcome-section { flex-direction: column; gap: 1rem; }
  .history-grid, .tools-grid { grid-template-columns: 1fr; }
  .btn-start { width: 100%; justify-content: center; }
  .dash-content { padding: 1.5rem 1rem 2rem; }
}
@media (max-width: 960px) and (min-width: 769px) {
  .history-grid { grid-template-columns: repeat(2, 1fr); }
  .tools-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
