<script setup>
/**
 * AppSidebar.vue — Light-themed sidebar with mobile overlay support.
 * Features: Logo + subtitle, navigation menu, dynamic chat history, settings link.
 * On mobile: opens as overlay with swipe-to-close gesture support.
 */
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useQuizStore } from '@/stores/quiz'
import { useChatStore } from '@/stores/chat'
import ConfirmModal from '@/components/shared/ConfirmModal.vue'

const props = defineProps({
  open: { type: Boolean, default: false }
})

const emit = defineEmits(['close'])

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const quizStore = useQuizStore()
const chatStore = useChatStore()

const menuItems = [
  { name: 'Dashboard', icon: 'dashboard', route: 'Dashboard', theme: '#3B82F6' },
  { name: 'AI Chat', icon: 'chat', route: 'AIChat', theme: '#3B82F6' },
  { name: 'Flashcards', icon: 'flashcards', route: 'Flashcards', theme: '#3B82F6' },
  { name: 'Quiz Mode', icon: 'quiz', route: 'QuizMode', theme: '#F59E0B' },
  { name: 'Voice To Speech', icon: 'voice', route: 'VoiceToSpeech', theme: '#10B981' }
]

// Dynamic flashcard history from store
const flashcardHistory = computed(() => {
  return [...quizStore.sessions]
    .filter(s => s.itemType === 'ESSAY' || (s.quizItems && s.quizItems.length > 0 && s.quizItems[0].itemType === 'ESSAY'))
    .reverse().slice(0, 8).map(session => ({
    id: session.id,
    title: session.documentFilename || session.document?.filename || 'Flashcard Session',
    time: formatRelativeTime(session.createdAt),
    active: quizStore.currentSession?.id === session.id && route.name === 'Flashcards'
  }))
})

// Dynamic voice history from store
const voiceHistory = computed(() => {
  return [...quizStore.sessions]
    .filter(s => s.itemType === 'ESSAY' || (s.quizItems && s.quizItems.length > 0 && s.quizItems[0].itemType === 'ESSAY'))
    .reverse().slice(0, 8).map(session => ({
    id: session.id,
    title: session.documentFilename || session.document?.filename || 'Voice Session',
    time: formatRelativeTime(session.createdAt),
    active: quizStore.currentSession?.id === session.id && route.name === 'VoiceToSpeech'
  }))
})

// Dynamic quiz history from store
const quizHistory = computed(() => {
  return [...quizStore.sessions]
    .filter(s => s.itemType === 'MULTIPLE_CHOICE' || (s.quizItems && s.quizItems.length > 0 && s.quizItems[0].itemType === 'MULTIPLE_CHOICE'))
    .reverse().slice(0, 8).map(session => ({
    id: session.id,
    title: session.documentFilename || session.document?.filename || 'Quiz Session',
    time: formatRelativeTime(session.createdAt),
    active: quizStore.currentSession?.id === session.id && route.name === 'QuizMode'
  }))
})

// Dynamic chat history from store
const chatHistory = computed(() => {
  return chatStore.sortedConversations.slice(0, 8).map(conv => ({
    id: conv.id,
    title: conv.title || 'Percakapan Baru',
    time: formatRelativeTime(conv.updatedAt || conv.createdAt),
    active: chatStore.currentConversation?.id === conv.id && route.name === 'AIChat'
  }))
})

function formatRelativeTime(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diffMs = now - date
  const diffMins = Math.floor(diffMs / 60000)
  const diffHours = Math.floor(diffMs / 3600000)
  const diffDays = Math.floor(diffMs / 86400000)

  if (diffMins < 1) return 'Baru saja'
  if (diffMins < 60) return `${diffMins} menit yang lalu`
  if (diffHours < 24) return `${diffHours} jam yang lalu`
  if (diffDays < 7) return `${diffDays} hari yang lalu`
  return date.toLocaleDateString('id-ID', { day: 'numeric', month: 'short' })
}

function handleSessionClick(session) {
  quizStore.loadSession(session.id).then(() => {
    if (route.name !== 'Flashcards') {
      router.push({ name: 'Flashcards' })
    }
  })
  emit('close')
}

function handleVoiceClick(session) {
  quizStore.loadSession(session.id).then(() => {
    if (route.name !== 'VoiceToSpeech') {
      router.push({ name: 'VoiceToSpeech' })
    }
  })
  emit('close')
}

function handleQuizClick(session) {
  quizStore.loadSession(session.id).then(() => {
    if (route.name !== 'QuizMode') {
      router.push({ name: 'QuizMode' })
    }
  })
  emit('close')
}

function handleChatClick(chat) {
  router.push({ name: 'AIChat', query: { conversationId: chat.id } })
  emit('close')
}

function handleNavClick() {
  emit('close')
}

// Chat delete
const showDeleteChat = ref(false)
const deleteChatId = ref(null)
const deleteChatTitle = ref('')

function requestDeleteChat(chat, event) {
  event.stopPropagation()
  deleteChatId.value = chat.id
  deleteChatTitle.value = chat.title
  showDeleteChat.value = true
}

async function confirmDeleteChat() {
  if (!deleteChatId.value) return
  try {
    await chatStore.deleteConversation(deleteChatId.value)
    deleteChatId.value = null
    deleteChatTitle.value = ''
  } catch (err) {
    console.error('Failed to delete conversation:', err)
  }
}const isActive = (routeName) => route.name === routeName

// Load sessions on mount
onMounted(async () => {
  if (authStore.isAuthenticated) {
    await Promise.all([
      quizStore.loadSessions(),
      chatStore.loadConversations()
    ])
  }
})

// Refresh when auth changes
watch(() => authStore.isAuthenticated, async (isAuth) => {
  if (isAuth) {
    await Promise.all([
      quizStore.loadSessions(),
      chatStore.loadConversations()
    ])
  }
})

// ===== Swipe-to-close gesture =====
const touchStartX = ref(0)
const touchCurrentX = ref(0)
const isSwiping = ref(false)

function onTouchStart(e) {
  touchStartX.value = e.touches[0].clientX
  isSwiping.value = true
}

function onTouchMove(e) {
  if (!isSwiping.value) return
  touchCurrentX.value = e.touches[0].clientX
}

function onTouchEnd() {
  if (!isSwiping.value) return
  const diff = touchStartX.value - touchCurrentX.value
  if (diff > 80) { // Swiped left — close sidebar
    emit('close')
  }
  isSwiping.value = false
}
</script>

<template>
  <aside
    class="sidebar"
    :class="{ open: open }"
    @touchstart="onTouchStart"
    @touchmove="onTouchMove"
    @touchend="onTouchEnd"
  >
    <!-- Close button (mobile only) -->
    <button class="sidebar-close-btn" @click="$emit('close')" aria-label="Tutup sidebar">
      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
    </button>

    <!-- Logo -->
    <div class="sidebar-logo">
      <div class="logo-icon-wrap">
        <img src="/logo/ngambis.png" alt="Ngambis.AI" width="28" height="28"/>
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
        :style="{ '--theme-color': item.theme, '--theme-bg': item.theme + '15' }"
        @click="handleNavClick"
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



    <!-- Delete Chat Confirmation -->
    <ConfirmModal
      v-model="showDeleteChat"
      title="Hapus Percakapan"
      :message="`Apakah kamu yakin ingin menghapus percakapan '${deleteChatTitle}'? Semua pesan akan terhapus permanen.`"
      confirmText="Ya, Hapus"
      cancelText="Batal"
      variant="danger"
      @confirm="confirmDeleteChat"
    />

    <!-- Dynamic History -->
    <div class="chat-history" v-if="['AIChat', 'Flashcards', 'VoiceToSpeech', 'QuizMode'].includes(route.name)">
      <template v-if="route.name === 'Flashcards'">
        <div class="history-label">RIWAYAT FLASHCARD</div>
        <div class="history-list" v-if="flashcardHistory.length > 0">
          <div
            v-for="session in flashcardHistory"
            :key="session.id"
            class="history-item"
            :class="{ active: session.active }"
            @click="handleSessionClick(session)"
          >
            <span class="history-title">{{ session.title }}</span>
            <div class="history-right">
              <span class="history-time">{{ session.time }}</span>
            </div>
          </div>
        </div>
        <div v-else class="history-empty">
          <span>Belum ada riwayat flashcard</span>
        </div>
      </template>

      <template v-else-if="route.name === 'AIChat'">
        <div class="history-label">RIWAYAT CHAT</div>
        <div class="history-list" v-if="chatHistory.length > 0">
          <div
            v-for="chat in chatHistory"
            :key="chat.id"
            class="history-item"
            :class="{ active: chat.active }"
            @click="handleChatClick(chat)"
          >
            <span class="history-title">{{ chat.title }}</span>
            <div class="history-right">
              <span class="history-time">{{ chat.time }}</span>
              <button class="history-delete-btn" @click.stop="(e) => requestDeleteChat(chat, e)" title="Hapus chat">
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
              </button>
            </div>
          </div>
        </div>
        <div v-else class="history-empty">
          <span>Belum ada riwayat chat</span>
        </div>
      </template>
      <template v-else-if="route.name === 'VoiceToSpeech'">
        <div class="history-label">RIWAYAT VOICE TO SPEECH</div>
        <div class="history-list" v-if="voiceHistory.length > 0">
          <div
            v-for="session in voiceHistory"
            :key="session.id"
            class="history-item"
            :class="{ active: session.active }"
            @click="handleVoiceClick(session)"
          >
            <span class="history-title">{{ session.title }}</span>
            <div class="history-right">
              <span class="history-time">{{ session.time }}</span>
            </div>
          </div>
        </div>
        <div v-else class="history-empty">
          <span>Belum ada riwayat voice to speech</span>
        </div>
      </template>

      <template v-else-if="route.name === 'QuizMode'">
        <div class="history-label">RIWAYAT QUIZ MODE</div>
        <div class="history-list" v-if="quizHistory.length > 0">
          <div
            v-for="session in quizHistory"
            :key="session.id"
            class="history-item"
            :class="{ active: session.active }"
            @click="handleQuizClick(session)"
          >
            <span class="history-title">{{ session.title }}</span>
            <div class="history-right">
              <span class="history-time">{{ session.time }}</span>
            </div>
          </div>
        </div>
        <div v-else class="history-empty">
          <span>Belum ada riwayat quiz</span>
        </div>
      </template>
    </div>

    <!-- Settings -->
    <div class="sidebar-footer">
      <router-link :to="{ name: 'Settings' }" class="nav-item settings-link" :class="{ active: isActive('Settings') }" @click="handleNavClick">
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
  z-index: 200;
  overflow-y: auto;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

/* Close button — mobile only */
.sidebar-close-btn {
  display: none;
  position: absolute;
  top: 0.75rem;
  right: 0.75rem;
  width: 32px;
  height: 32px;
  align-items: center;
  justify-content: center;
  background: none;
  border: none;
  color: #94A3B8;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s;
  z-index: 10;
}
.sidebar-close-btn:hover { background: #F1F5F9; color: #475569; }

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
  color: var(--theme-color, #3B82F6);
  background: var(--theme-bg, #EFF6FF);
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
  background: var(--theme-color, #3B82F6);
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
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.5rem;
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
  flex: 1;
  min-width: 0;
}

.history-item.active .history-title {
  color: white;
  font-weight: 600;
}

.history-right {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  flex-shrink: 0;
}

.history-item .history-time {
  font-size: 0.6875rem;
  color: #94A3B8;
  white-space: nowrap;
}

.history-item.active .history-time {
  color: rgba(255, 255, 255, 0.7);
}

.history-delete-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border: none;
  background: transparent;
  border-radius: 6px;
  color: #94A3B8;
  cursor: pointer;
  opacity: 0;
  transition: all 0.15s ease;
  flex-shrink: 0;
}

.history-item:hover .history-delete-btn {
  opacity: 1;
}

.history-delete-btn:hover {
  background: #FEE2E2;
  color: #DC2626;
}

.history-item.active .history-delete-btn {
  color: rgba(255, 255, 255, 0.6);
}

.history-item.active .history-delete-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}.history-empty {
  padding: 1rem 0.875rem;
  text-align: center;
}

.history-empty span {
  font-size: 0.75rem;
  color: #94A3B8;
  font-style: italic;
}

/* Footer / Settings */
.sidebar-footer {
  padding: 0.5rem 0.75rem 1rem;
  border-top: 1px solid #F1F5F9;
}

.settings-link {
  margin-top: 0.25rem;
}

/* ===== MOBILE ===== */
@media (max-width: 768px) {
  .sidebar {
    transform: translateX(-100%);
    box-shadow: 10px 0 40px rgba(0, 0, 0, 0.15);
  }

  .sidebar.open {
    transform: translateX(0);
  }

  .sidebar-close-btn {
    display: flex;
  }
}
</style>
