<script setup>
/**
 * AppSidebar.vue — Light-themed sidebar with mobile overlay support.
 * Features: Logo + subtitle, navigation menu, dynamic chat history, settings link.
 * On mobile: opens as overlay with swipe-to-close gesture support.
 */
import { computed, onMounted, onBeforeUnmount, ref, watch, nextTick } from 'vue'
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
  { name: 'Chat AI', icon: 'chat', route: 'AIChat', theme: '#3B82F6' },
  { name: 'Flashcard Pintar', icon: 'flashcards', route: 'Flashcards', theme: '#6A42C2' },
  { name: 'Kuis Adaptif', icon: 'quiz', route: 'QuizMode', theme: '#F59E0B' },
  { name: 'Ujian Lisan', icon: 'voice', route: 'VoiceToSpeech', theme: '#10B981' }
]

/**
 * Helper: get the effective itemType of a session.
 * Reads from quizItems[0] if session-level itemType is missing.
 */
function getSessionItemType(session) {
  if (session.itemType) return session.itemType
  if (session.quizItems?.length > 0) return session.quizItems[0].itemType
  return null
}

/**
 * Helper: determine the source view of an ESSAY session.
 * For sessions created in this browser tab, we have sessionSources.
 * For sessions loaded from server (older history), we default to both
 * flashcard and voice (they share ESSAY type); the heuristic is:
 * if source is tracked → use it; otherwise show in the current view.
 */
function isFlashcardSession(session) {
  const type = getSessionItemType(session)
  if (type !== 'ESSAY') return false
  const src = quizStore.sessionSources[session.id]
  // If we know the source, trust it
  if (src) return src === 'Flashcards'
  // Fallback for server-loaded sessions: show under both flashcard & voice
  return true
}

function isVoiceSession(session) {
  const type = getSessionItemType(session)
  if (type !== 'ESSAY') return false
  const src = quizStore.sessionSources[session.id]
  if (src) return src === 'VoiceToSpeech'
  // Fallback: also show under voice for server-loaded ESSAY sessions
  return true
}

// Dynamic flashcard history from store
const flashcardHistory = computed(() => {
  const list = [...quizStore.sessions]
    .filter(isFlashcardSession)
    .reverse()
    .map(session => ({
      id: session.id,
      title: session.title || session.documentFilename || session.document?.filename || 'Sesi Flashcard Pintar',
      time: formatRelativeTime(session.createdAt),
      pinned: session.pinned || false,
      active: quizStore.currentSession?.id === session.id && route.name === 'Flashcards'
    }))
  return list.sort((a, b) => {
    if (a.pinned && !b.pinned) return -1
    if (!a.pinned && b.pinned) return 1
    return 0
  }).slice(0, 8)
})

// Dynamic voice history from store
const voiceHistory = computed(() => {
  const list = [...quizStore.sessions]
    .filter(isVoiceSession)
    .reverse()
    .map(session => ({
      id: session.id,
      title: session.title || session.documentFilename || session.document?.filename || 'Sesi Ujian Lisan',
      time: formatRelativeTime(session.createdAt),
      pinned: session.pinned || false,
      active: quizStore.currentSession?.id === session.id && route.name === 'VoiceToSpeech'
    }))
  return list.sort((a, b) => {
    if (a.pinned && !b.pinned) return -1
    if (!a.pinned && b.pinned) return 1
    return 0
  }).slice(0, 8)
})

// Dynamic quiz history from store
const quizHistory = computed(() => {
  const list = [...quizStore.sessions]
    .filter(s => getSessionItemType(s) === 'MULTIPLE_CHOICE')
    .reverse()
    .map(session => ({
      id: session.id,
      title: session.title || session.documentFilename || session.document?.filename || 'Sesi Kuis Adaptif',
      time: formatRelativeTime(session.createdAt),
      pinned: session.pinned || false,
      active: quizStore.currentSession?.id === session.id && route.name === 'QuizMode'
    }))
  return list.sort((a, b) => {
    if (a.pinned && !b.pinned) return -1
    if (!a.pinned && b.pinned) return 1
    return 0
  }).slice(0, 8)
})

// Dynamic chat history from store
const chatHistory = computed(() => {
  return chatStore.sortedConversations.slice(0, 8).map(conv => ({
    id: conv.id,
    title: conv.title || 'Percakapan Baru',
    time: formatRelativeTime(conv.updatedAt || conv.createdAt),
    pinned: conv.pinned,
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

// Menu Dropdown State
const activeMenuId = ref(null)

function toggleMenu(chatId, event) {
  event.stopPropagation()
  if (activeMenuId.value === chatId) {
    activeMenuId.value = null
  } else {
    activeMenuId.value = chatId
  }
}

function closeAllMenus() {
  activeMenuId.value = null
}

// Chat delete
const showDeleteChat = ref(false)
const deleteChatId = ref(null)
const deleteChatTitle = ref('')

function requestDeleteChat(chat, event) {
  event.stopPropagation()
  closeAllMenus()
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
}

// Chat rename
const showRenameModal = ref(false)
const renameChatId = ref(null)
const renameInput = ref('')
const renameInputRef = ref(null)

function requestRenameChat(chat, event) {
  event.stopPropagation()
  closeAllMenus()
  renameChatId.value = chat.id
  renameInput.value = chat.title
  showRenameModal.value = true
  nextTick(() => {
    if (renameInputRef.value) renameInputRef.value.focus()
  })
}

async function confirmRenameChat() {
  if (!renameChatId.value || !renameInput.value.trim()) return
  try {
    await chatStore.renameConversation(renameChatId.value, renameInput.value.trim())
    showRenameModal.value = false
    renameChatId.value = null
    renameInput.value = ''
  } catch (err) {
    console.error('Failed to rename conversation:', err)
  }
}

// Chat pin
async function togglePinChat(chat, event) {
  event.stopPropagation()
  closeAllMenus()
  try {
    await chatStore.pinConversation(chat.id, !chat.pinned)
  } catch (err) {
    console.error('Failed to toggle pin state:', err)
  }
}

// Session delete
const showDeleteSession = ref(false)
const deleteSessionId = ref(null)
const deleteSessionTitle = ref('')

function requestDeleteSession(session, event) {
  event.stopPropagation()
  closeAllMenus()
  deleteSessionId.value = session.id
  deleteSessionTitle.value = session.title
  showDeleteSession.value = true
}

async function confirmDeleteSession() {
  if (!deleteSessionId.value) return
  try {
    await quizStore.deleteSession(deleteSessionId.value)
    deleteSessionId.value = null
    deleteSessionTitle.value = ''
  } catch (err) {
    console.error('Failed to delete session:', err)
  }
}

// Session rename
const showRenameSessionModal = ref(false)
const renameSessionId = ref(null)
const renameSessionInput = ref('')
const renameSessionInputRef = ref(null)

function requestRenameSession(session, event) {
  event.stopPropagation()
  closeAllMenus()
  renameSessionId.value = session.id
  renameSessionInput.value = session.title
  showRenameSessionModal.value = true
  nextTick(() => {
    if (renameSessionInputRef.value) renameSessionInputRef.value.focus()
  })
}

async function confirmRenameSession() {
  if (!renameSessionId.value || !renameSessionInput.value.trim()) return
  try {
    await quizStore.renameSession(renameSessionId.value, renameSessionInput.value.trim())
    showRenameSessionModal.value = false
    renameSessionId.value = null
    renameSessionInput.value = ''
  } catch (err) {
    console.error('Failed to rename session:', err)
  }
}

// Session pin
async function togglePinSession(session, event) {
  event.stopPropagation()
  closeAllMenus()
  try {
    await quizStore.pinSession(session.id, !session.pinned)
  } catch (err) {
    console.error('Failed to toggle session pin state:', err)
  }
}

const isActive = (routeName) => route.name === routeName

// Load sessions on mount
onMounted(async () => {
  window.addEventListener('click', closeAllMenus)
  if (authStore.isAuthenticated) {
    await Promise.all([
      quizStore.loadSessions(),
      chatStore.loadConversations()
    ])
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('click', closeAllMenus)
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
        :to="{ name: item.route, query: ['Flashcards', 'QuizMode', 'VoiceToSpeech'].includes(item.route) ? { new: true } : {} }"
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

    <!-- Delete Session Confirmation -->
    <ConfirmModal
      v-model="showDeleteSession"
      title="Hapus Sesi Belajar"
      :message="`Apakah kamu yakin ingin menghapus sesi '${deleteSessionTitle}'? Semua data sesi ini akan terhapus permanen.`"
      confirmText="Ya, Hapus"
      cancelText="Batal"
      variant="danger"
      @confirm="confirmDeleteSession"
    />

    <!-- Dynamic History -->
    <div class="chat-history" v-if="['AIChat', 'Flashcards', 'VoiceToSpeech', 'QuizMode'].includes(route.name)">
      <template v-if="route.name === 'Flashcards'">
        <div class="history-label">RIWAYAT FLASHCARD PINTAR</div>
        <div class="history-list" v-if="flashcardHistory.length > 0">
          <div
            v-for="session in flashcardHistory"
            :key="session.id"
            class="history-item"
            :class="{ active: session.active, pinned: session.pinned }"
            @click="handleSessionClick(session)"
          >
            <span v-if="session.pinned" class="pinned-indicator-icon" title="Dipin">
              <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="17" x2="12" y2="22"/><path d="M5 17h14v-1.76a2 2 0 0 0-.44-1.24l-2.33-2.91A2 2 0 0 1 16 9.85V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v4.85a2 2 0 0 1-.23 1.05l-2.33 2.9A2 2 0 0 0 5 15.24z"/></svg>
            </span>
            <span class="history-title">{{ session.title }}</span>
            <div class="history-right">
              <span class="history-time">{{ session.time }}</span>
              <div class="menu-container" @click.stop>
                <button class="options-btn" @click="toggleMenu(session.id, $event)" title="Opsi sesi">
                  <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="5" r="1"/><circle cx="12" cy="12" r="1"/><circle cx="12" cy="19" r="1"/></svg>
                </button>
                <Transition name="fade-pop">
                  <div v-if="activeMenuId === session.id" class="options-dropdown">
                    <button class="opt-item" @click="togglePinSession(session, $event)">
                      <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="17" x2="12" y2="22"/><path d="M5 17h14v-1.76a2 2 0 0 0-.44-1.24l-2.33-2.91A2 2 0 0 1 16 9.85V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v4.85a2 2 0 0 1-.23 1.05l-2.33 2.9A2 2 0 0 0 5 15.24z"/></svg>
                      <span>{{ session.pinned ? 'Lepaskan' : 'Sematkan (Pin)' }}</span>
                    </button>
                    <button class="opt-item" @click="requestRenameSession(session, $event)">
                      <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 1 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                      <span>Ganti nama</span>
                    </button>
                    <div class="opt-divider"></div>
                    <button class="opt-item opt-danger" @click="requestDeleteSession(session, $event)">
                      <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
                      <span>Hapus</span>
                    </button>
                  </div>
                </Transition>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="history-empty">
          <span>Belum ada riwayat flashcard pintar</span>
        </div>
      </template>

      <template v-else-if="route.name === 'AIChat'">
        <div class="history-label">RIWAYAT CHAT</div>
        <div class="history-list" v-if="chatHistory.length > 0">
          <div
            v-for="chat in chatHistory"
            :key="chat.id"
            class="history-item"
            :class="{ active: chat.active, pinned: chat.pinned }"
            @click="handleChatClick(chat)"
          >
            <span v-if="chat.pinned" class="pinned-indicator-icon" title="Dipin">
              <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="17" x2="12" y2="22"/><path d="M5 17h14v-1.76a2 2 0 0 0-.44-1.24l-2.33-2.91A2 2 0 0 1 16 9.85V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v4.85a2 2 0 0 1-.23 1.05l-2.33 2.9A2 2 0 0 0 5 15.24z"/></svg>
            </span>
            <span class="history-title">{{ chat.title }}</span>
            <div class="history-right">
              <span class="history-time">{{ chat.time }}</span>
              <div class="menu-container" @click.stop>
                <button class="options-btn" @click="toggleMenu(chat.id, $event)" title="Opsi chat">
                  <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="5" r="1"/><circle cx="12" cy="12" r="1"/><circle cx="12" cy="19" r="1"/></svg>
                </button>
                <Transition name="fade-pop">
                  <div v-if="activeMenuId === chat.id" class="options-dropdown">
                    <button class="opt-item" @click="togglePinChat(chat, $event)">
                      <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="17" x2="12" y2="22"/><path d="M5 17h14v-1.76a2 2 0 0 0-.44-1.24l-2.33-2.91A2 2 0 0 1 16 9.85V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v4.85a2 2 0 0 1-.23 1.05l-2.33 2.9A2 2 0 0 0 5 15.24z"/></svg>
                      <span>{{ chat.pinned ? 'Lepaskan' : 'Sematkan (Pin)' }}</span>
                    </button>
                    <button class="opt-item" @click="requestRenameChat(chat, $event)">
                      <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 1 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                      <span>Ganti nama</span>
                    </button>
                    <div class="opt-divider"></div>
                    <button class="opt-item opt-danger" @click="requestDeleteChat(chat, $event)">
                      <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
                      <span>Hapus</span>
                    </button>
                  </div>
                </Transition>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="history-empty">
          <span>Belum ada riwayat chat</span>
        </div>
      </template>

      <template v-else-if="route.name === 'VoiceToSpeech'">
        <div class="history-label">RIWAYAT UJIAN LISAN</div>
        <div class="history-list" v-if="voiceHistory.length > 0">
          <div
            v-for="session in voiceHistory"
            :key="session.id"
            class="history-item"
            :class="{ active: session.active, pinned: session.pinned }"
            @click="handleVoiceClick(session)"
          >
            <span v-if="session.pinned" class="pinned-indicator-icon" title="Dipin">
              <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="17" x2="12" y2="22"/><path d="M5 17h14v-1.76a2 2 0 0 0-.44-1.24l-2.33-2.91A2 2 0 0 1 16 9.85V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v4.85a2 2 0 0 1-.23 1.05l-2.33 2.9A2 2 0 0 0 5 15.24z"/></svg>
            </span>
            <span class="history-title">{{ session.title }}</span>
            <div class="history-right">
              <span class="history-time">{{ session.time }}</span>
              <div class="menu-container" @click.stop>
                <button class="options-btn" @click="toggleMenu(session.id, $event)" title="Opsi sesi">
                  <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="5" r="1"/><circle cx="12" cy="12" r="1"/><circle cx="12" cy="19" r="1"/></svg>
                </button>
                <Transition name="fade-pop">
                  <div v-if="activeMenuId === session.id" class="options-dropdown">
                    <button class="opt-item" @click="togglePinSession(session, $event)">
                      <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="17" x2="12" y2="22"/><path d="M5 17h14v-1.76a2 2 0 0 0-.44-1.24l-2.33-2.91A2 2 0 0 1 16 9.85V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v4.85a2 2 0 0 1-.23 1.05l-2.33 2.9A2 2 0 0 0 5 15.24z"/></svg>
                      <span>{{ session.pinned ? 'Lepaskan' : 'Sematkan (Pin)' }}</span>
                    </button>
                    <button class="opt-item" @click="requestRenameSession(session, $event)">
                      <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 1 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                      <span>Ganti nama</span>
                    </button>
                    <div class="opt-divider"></div>
                    <button class="opt-item opt-danger" @click="requestDeleteSession(session, $event)">
                      <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
                      <span>Hapus</span>
                    </button>
                  </div>
                </Transition>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="history-empty">
          <span>Belum ada riwayat ujian lisan</span>
        </div>
      </template>

      <template v-else-if="route.name === 'QuizMode'">
        <div class="history-label">RIWAYAT KUIS ADAPTIF</div>
        <div class="history-list" v-if="quizHistory.length > 0">
          <div
            v-for="session in quizHistory"
            :key="session.id"
            class="history-item"
            :class="{ active: session.active, pinned: session.pinned }"
            @click="handleQuizClick(session)"
          >
            <span v-if="session.pinned" class="pinned-indicator-icon" title="Dipin">
              <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="17" x2="12" y2="22"/><path d="M5 17h14v-1.76a2 2 0 0 0-.44-1.24l-2.33-2.91A2 2 0 0 1 16 9.85V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v4.85a2 2 0 0 1-.23 1.05l-2.33 2.9A2 2 0 0 0 5 15.24z"/></svg>
            </span>
            <span class="history-title">{{ session.title }}</span>
            <div class="history-right">
              <span class="history-time">{{ session.time }}</span>
              <div class="menu-container" @click.stop>
                <button class="options-btn" @click="toggleMenu(session.id, $event)" title="Opsi sesi">
                  <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="5" r="1"/><circle cx="12" cy="12" r="1"/><circle cx="12" cy="19" r="1"/></svg>
                </button>
                <Transition name="fade-pop">
                  <div v-if="activeMenuId === session.id" class="options-dropdown">
                    <button class="opt-item" @click="togglePinSession(session, $event)">
                      <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="17" x2="12" y2="22"/><path d="M5 17h14v-1.76a2 2 0 0 0-.44-1.24l-2.33-2.91A2 2 0 0 1 16 9.85V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v4.85a2 2 0 0 1-.23 1.05l-2.33 2.9A2 2 0 0 0 5 15.24z"/></svg>
                      <span>{{ session.pinned ? 'Lepaskan' : 'Sematkan (Pin)' }}</span>
                    </button>
                    <button class="opt-item" @click="requestRenameSession(session, $event)">
                      <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 1 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
                      <span>Ganti nama</span>
                    </button>
                    <div class="opt-divider"></div>
                    <button class="opt-item opt-danger" @click="requestDeleteSession(session, $event)">
                      <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
                      <span>Hapus</span>
                    </button>
                  </div>
                </Transition>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="history-empty">
          <span>Belum ada riwayat kuis adaptif</span>
        </div>
      </template>
    </div>

    <!-- Rename Chat Modal -->
    <div v-if="showRenameModal" class="custom-modal-overlay" @click.self="showRenameModal = false">
      <div class="custom-modal">
        <h3>Ganti Nama Percakapan</h3>
        <p>Masukkan nama baru untuk percakapan ini:</p>
        <input v-model="renameInput" class="modal-input" @keyup.enter="confirmRenameChat" placeholder="Nama percakapan..." ref="renameInputRef" />
        <div class="modal-actions">
          <button class="btn-modal-cancel" @click="showRenameModal = false">Batal</button>
          <button class="btn-modal-confirm" @click="confirmRenameChat" :disabled="!renameInput.trim()">Simpan</button>
        </div>
      </div>
    </div>

    <!-- Rename Session Modal -->
    <div v-if="showRenameSessionModal" class="custom-modal-overlay" @click.self="showRenameSessionModal = false">
      <div class="custom-modal">
        <h3>Ganti Nama Sesi</h3>
        <p>Masukkan nama baru untuk sesi belajar ini:</p>
        <input v-model="renameSessionInput" class="modal-input" @keyup.enter="confirmRenameSession" placeholder="Nama sesi..." ref="renameSessionInputRef" />
        <div class="modal-actions">
          <button class="btn-modal-cancel" @click="showRenameSessionModal = false">Batal</button>
          <button class="btn-modal-confirm" @click="confirmRenameSession" :disabled="!renameSessionInput.trim()">Simpan</button>
        </div>
      </div>
    </div>

    <!-- Settings -->
    <div class="sidebar-footer">
      <router-link :to="{ name: 'Settings' }" class="nav-item settings-link" :class="{ active: isActive('Settings') }" @click="handleNavClick">
        <span class="nav-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"/></svg>
        </span>
        <span class="nav-label">Pengaturan</span>
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

/* Options dropdown and custom modal style */
.menu-container {
  position: relative;
  display: inline-flex;
}

.options-btn {
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

.history-item:hover .options-btn,
.menu-container:focus-within .options-btn {
  opacity: 1;
}

/* On touch/mobile: always show the three-dot button since there's no hover */
@media (max-width: 768px) {
  .options-btn {
    opacity: 1;
  }
}

.options-btn:hover {
  background: #E2E8F0;
  color: #475569;
}

.history-item.active .options-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

.options-dropdown {
  position: absolute;
  right: 0;
  top: calc(100% + 4px);
  width: 160px;
  background: white;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  box-shadow: 0 10px 25px rgba(0,0,0,0.08);
  padding: 4px;
  z-index: 100;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.opt-item {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 6px 10px;
  background: none;
  border: none;
  border-radius: 6px;
  font-size: 0.8125rem;
  font-weight: 500;
  color: #475569;
  cursor: pointer;
  text-align: left;
  transition: background 0.15s;
}

.opt-item:hover {
  background: #F1F5F9;
  color: #1E293B;
}

.opt-divider {
  height: 1px;
  background: #F1F5F9;
  margin: 2px 4px;
}

.opt-danger {
  color: #EF4444;
}

.opt-danger:hover {
  background: #FEF2F2;
  color: #EF4444;
}

.pinned-indicator-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #3B82F6;
  margin-right: 4px;
  flex-shrink: 0;
}

.history-item.active .pinned-indicator-icon {
  color: white;
}

.history-item.active .options-btn {
  color: rgba(255,255,255,0.7);
}

/* Custom modal */
.custom-modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.4);
  backdrop-filter: blur(4px);
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
}

.custom-modal {
  background: white;
  border-radius: 16px;
  padding: 1.75rem;
  max-width: 400px;
  width: 100%;
  box-shadow: 0 20px 50px rgba(0,0,0,0.15);
  animation: modalPop 0.25s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes modalPop {
  from { opacity: 0; transform: scale(0.95); }
  to { opacity: 1; transform: scale(1); }
}

.custom-modal h3 {
  font-size: 1.125rem;
  font-weight: 700;
  color: #0F172A;
  margin: 0 0 0.5rem;
}

.custom-modal p {
  font-size: 0.875rem;
  color: #64748B;
  margin: 0 0 1rem;
}

.modal-input {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  font-size: 0.875rem;
  color: #1E293B;
  outline: none;
  transition: all 0.2s;
  box-sizing: border-box;
}

.modal-input:focus {
  border-color: #3B82F6;
  box-shadow: 0 0 0 3px rgba(59,130,246,0.1);
}

.modal-actions {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
  margin-top: 1.5rem;
}

.btn-modal-cancel {
  padding: 0.625rem 1.25rem;
  background: #F1F5F9;
  color: #475569;
  border: 1px solid #E2E8F0;
  border-radius: 8px;
  font-size: 0.8125rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-modal-cancel:hover {
  background: #E2E8F0;
}

.btn-modal-confirm {
  padding: 0.625rem 1.25rem;
  background: #3B82F6;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 0.8125rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-modal-confirm:hover:not(:disabled) {
  background: #2563EB;
}

.btn-modal-confirm:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Transitions */
.fade-pop-enter-active, .fade-pop-leave-active {
  transition: all 0.15s ease;
}
.fade-pop-enter-from, .fade-pop-leave-to {
  opacity: 0;
  transform: scale(0.95) translateY(-4px);
}
</style>
