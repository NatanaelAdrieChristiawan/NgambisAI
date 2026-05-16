<script setup>
import { ref, computed, nextTick, onMounted, onBeforeUnmount, inject, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useChatStore } from '@/stores/chat'
import { useDocumentStore } from '@/stores/document'
import DocumentManager from '@/components/shared/DocumentManager.vue'

const route = useRoute()
const authStore = useAuthStore()
const chatStore = useChatStore()
const docStore = useDocumentStore()
const toggleSidebar = inject('toggleSidebar', () => {})

const displayName = computed(() => authStore.userName || 'User')
const avatarUrl = computed(() => authStore.userAvatar)
const initials = computed(() => displayName.value.split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2))

// Active documents context — from conversation response
const currentConversation = computed(() => chatStore.currentConversation)
const conversationDocs = computed(() => {
  return currentConversation.value?.documents || []
})
const activeDocNames = computed(() => {
  return conversationDocs.value.map(d => d.filename || d.fileName || d.name || 'Dokumen')
})

const showUserMenu = ref(false)
function toggleUserMenu() { showUserMenu.value = !showUserMenu.value }
function closeUserMenu() { showUserMenu.value = false }
function handleLogout() { authStore.logout(); closeUserMenu() }

const messageInput = ref('')
const chatArea = ref(null)
const showDocPicker = ref(false)
const sendError = ref(null)

// Drag & drop state
const isDraggingFile = ref(false)
const dropUploadError = ref(null)
const isDropUploading = ref(false)
let dragLeaveTimer = null

function onPageDragOver(e) {
  e.preventDefault()
  e.stopPropagation()
  if (dragLeaveTimer) { clearTimeout(dragLeaveTimer); dragLeaveTimer = null }
  if (e.dataTransfer?.types?.includes('Files')) {
    isDraggingFile.value = true
  }
}

function onPageDragLeave(e) {
  e.preventDefault()
  e.stopPropagation()
  // Small delay to prevent flicker when moving between child elements
  dragLeaveTimer = setTimeout(() => { isDraggingFile.value = false }, 100)
}

async function onPageDrop(e) {
  e.preventDefault()
  e.stopPropagation()
  isDraggingFile.value = false
  dropUploadError.value = null
  if (dragLeaveTimer) { clearTimeout(dragLeaveTimer); dragLeaveTimer = null }

  const files = e.dataTransfer?.files
  if (!files || files.length === 0) return

  const file = files[0]
  if (!file.name.toLowerCase().endsWith('.pdf')) {
    dropUploadError.value = 'Hanya file PDF yang diizinkan.'
    setTimeout(() => { dropUploadError.value = null }, 4000)
    return
  }

  isDropUploading.value = true
  try {
    const doc = await docStore.uploadDocument(file)
    docStore.selectDocument(doc.id)
    dropUploadError.value = null
  } catch (err) {
    dropUploadError.value = err.response?.data?.message || err.message || 'Gagal mengupload dokumen.'
    setTimeout(() => { dropUploadError.value = null }, 4000)
  } finally {
    isDropUploading.value = false
  }
}

// Typewriter animation state
const animatingMsgId = ref(null)
const animatedText = ref('')
let typewriterTimer = null

function startTypewriter(msgId, fullContent) {
  stopTypewriter()
  animatingMsgId.value = msgId
  animatedText.value = ''
  const words = fullContent.split(/( )/)
  let idx = 0
  const chunkSize = 2 // reveal 2 tokens at a time for smooth pacing
  const speed = 18 // ms per chunk

  function tick() {
    if (idx < words.length) {
      animatedText.value += words.slice(idx, idx + chunkSize).join('')
      idx += chunkSize
      scrollToBottom()
      typewriterTimer = setTimeout(tick, speed)
    } else {
      // Animation complete
      animatingMsgId.value = null
      animatedText.value = ''
    }
  }
  tick()
}

function stopTypewriter() {
  if (typewriterTimer) {
    clearTimeout(typewriterTimer)
    typewriterTimer = null
  }
  animatingMsgId.value = null
  animatedText.value = ''
}

function getDisplayContent(msg) {
  if (msg.id === animatingMsgId.value) {
    return animatedText.value
  }
  return msg.content
}

const messages = computed(() => chatStore.messages)
const isSending = computed(() => chatStore.sending)
const isLoading = computed(() => chatStore.loading)

function handleNewChat() {
  chatStore.newChat()
  docStore.clearSelection()
  showDocPicker.value = false
  sendError.value = null
}

async function sendMessage() {
  const text = messageInput.value.trim()
  if (!text || isSending.value) return
  sendError.value = null

  // If no current conversation, need documents
  if (!chatStore.currentConversation) {
    if (docStore.selectedDocumentIds.length === 0) {
      showDocPicker.value = true
      sendError.value = 'Pilih minimal satu dokumen untuk memulai percakapan.'
      return
    }
  }

  messageInput.value = ''
  try {
    await chatStore.sendMessage(text, docStore.selectedDocumentIds)
    await nextTick()
    // Find the latest AI message and animate it
    const lastAiMsg = [...messages.value].reverse().find(m => m.type === 'ai')
    if (lastAiMsg && lastAiMsg.content) {
      startTypewriter(lastAiMsg.id, lastAiMsg.content)
    }
    scrollToBottom()
  } catch (err) {
    sendError.value = chatStore.error || 'Gagal mengirim pesan'
  }
}



function scrollToBottom() {
  if (chatArea.value) chatArea.value.scrollTop = chatArea.value.scrollHeight
}

function handleKeydown(e) {
  if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); sendMessage() }
}

function renderMarkdown(text) {
  if (!text) return ''
  
  // Protect code blocks
  const codes = []
  let processed = text.replace(/```(\w*)\n([\s\S]*?)```/g, (match, lang, code) => {
    codes.push(`<pre class="ai-code"><code>${code.trim()}</code></pre>`)
    return `\n__CODE_${codes.length - 1}__\n`
  })

  // Basic inline formatting
  processed = processed
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.*?)\*/g, '<em>$1</em>')
    .replace(/`([^`]+)`/g, '<code class="ai-inline-code">$1</code>')

  const lines = processed.split('\n')
  const blocks = []
  let currentList = null // { type: 'ul'|'ol', start: number, items: [] }
  let currentParagraph = []

  function flushParagraph() {
    if (currentParagraph.length > 0) {
      blocks.push(`<p>${currentParagraph.join('<br>')}</p>`)
      currentParagraph = []
    }
  }

  function flushList() {
    if (currentList) {
      const tag = currentList.type
      const startAttr = currentList.start !== null && currentList.start !== 1 ? ` start="${currentList.start}"` : ''
      const itemsHtml = currentList.items.map(item => `<li>${item}</li>`).join('')
      blocks.push(`<${tag} class="md-${tag}"${startAttr}>${itemsHtml}</${tag}>`)
      currentList = null
    }
  }

  for (let i = 0; i < lines.length; i++) {
    const line = lines[i].trim()
    
    // Empty line -> flush
    if (line === '') {
      flushParagraph()
      flushList()
      continue
    }

    // Code blocks
    if (line.startsWith('__CODE_')) {
      flushParagraph()
      flushList()
      blocks.push(line)
      continue
    }

    // Headers
    const hMatch = line.match(/^(#{1,4})\s+(.+)/)
    if (hMatch) {
      flushParagraph()
      flushList()
      const level = hMatch[1].length
      blocks.push(`<h${level} class="md-h${level}">${hMatch[2]}</h${level}>`)
      continue
    }

    // HR
    if (line === '---') {
      flushParagraph()
      flushList()
      blocks.push('<hr class="md-hr">')
      continue
    }

    // Ordered list
    const olMatch = line.match(/^(\d+)\.(?:\s+(.*)|\s*)$/)
    if (olMatch) {
      flushParagraph()
      if (currentList && currentList.type !== 'ol') flushList()
      
      const content = olMatch[2] || ''
      if (!currentList) {
        currentList = { type: 'ol', start: parseInt(olMatch[1], 10), items: [content] }
      } else {
        currentList.items.push(content)
      }
      continue
    }

    // Unordered list
    const ulMatch = line.match(/^[-*](?:\s+(.*)|\s*)$/)
    if (ulMatch) {
      flushParagraph()
      if (currentList && currentList.type !== 'ul') flushList()
      
      const content = ulMatch[1] || ''
      if (!currentList) {
        currentList = { type: 'ul', start: null, items: [content] }
      } else {
        currentList.items.push(content)
      }
      continue
    }

    // Otherwise, it's a paragraph
    flushList()
    currentParagraph.push(line)
  }

  flushParagraph()
  flushList()

  processed = blocks.join('\n')

  // Restore code blocks
  codes.forEach((code, i) => {
    processed = processed.replace(`__CODE_${i}__`, code)
  })

  return processed
}

const isReady = ref(false)
onMounted(async () => {
  requestAnimationFrame(() => { isReady.value = true })
  await docStore.loadDocuments()
  const convId = route.query.conversationId
  if (convId) {
    await chatStore.loadConversation(convId)
    await nextTick()
    scrollToBottom()
  }
})

watch(() => route.query.conversationId, async (newId) => {
  if (newId) {
    await chatStore.loadConversation(newId)
    await nextTick()
    scrollToBottom()
  }
})

watch(messages, async () => {
  await nextTick()
  scrollToBottom()
}, { deep: true })

onBeforeUnmount(() => {
  stopTypewriter()
})
</script>

<template>
  <div class="chat-page" :class="{ ready: isReady }" @click="closeUserMenu"
       @dragover="onPageDragOver" @dragleave="onPageDragLeave" @drop="onPageDrop">

    <!-- Drag & Drop Overlay -->
    <Transition name="fade">
      <div v-if="isDraggingFile" class="drop-overlay">
        <div class="drop-overlay-content">
          <div class="drop-overlay-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/></svg>
          </div>
          <h3>Drop file PDF di sini</h3>
          <p>File akan langsung diupload sebagai dokumen konteks</p>
        </div>
      </div>
    </Transition>

    <!-- Drop upload notification -->
    <Transition name="slide-down">
      <div v-if="isDropUploading" class="drop-notification uploading">
        <div class="drop-notif-spinner"></div>
        <span>Mengupload dokumen...</span>
      </div>
    </Transition>
    <Transition name="slide-down">
      <div v-if="dropUploadError" class="drop-notification error" @click="dropUploadError = null">
        <span>⚠️ {{ dropUploadError }}</span>
        <span class="dismiss">✕</span>
      </div>
    </Transition>
    <header class="chat-header">
      <div class="chat-header-left">
        <button class="burger-btn" @click.stop="toggleSidebar" aria-label="Menu">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="18" x2="21" y2="18"/></svg>
        </button>
        <button class="btn-new-chat" @click="handleNewChat">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/></svg>
          <span>Chat Baru</span>
        </button>
      </div>
      <div class="chat-user-area" @click.stop>
        <button class="chat-avatar-btn" @click="toggleUserMenu">
          <div v-if="avatarUrl" class="chat-avatar"><img :src="avatarUrl" :alt="displayName" referrerpolicy="no-referrer"/></div>
          <div v-else class="chat-avatar chat-avatar-initials">{{ initials }}</div>
        </button>
        <div v-if="showUserMenu" class="chat-user-dropdown">
          <div class="dd-info"><span class="dd-name">{{ displayName }}</span><span class="dd-email">{{ authStore.userEmail }}</span></div>
          <div class="dd-div"></div>
          <button class="dd-item" @click="handleLogout">Logout</button>
        </div>
      </div>
    </header>


    <!-- Document Picker Overlay -->
    <div v-if="showDocPicker" class="doc-picker-overlay" @click.self="showDocPicker = false">
      <div class="doc-picker">
        <button class="btn-close-modal" @click="showDocPicker = false" aria-label="Tutup">✕</button>
        <h3>Pilih Dokumen Konteks</h3>
        <p class="doc-picker-hint">Pilih dokumen PDF yang sudah diupload sebagai konteks untuk AI.</p>
        <DocumentManager mode="list" />
        <div class="doc-picker-actions">
          <button class="btn-confirm-doc" @click="showDocPicker = false" :disabled="docStore.selectedDocumentIds.length === 0">
            Gunakan ({{ docStore.selectedDocumentIds.length }})
          </button>
        </div>
      </div>
    </div>

    <!-- Loading state -->
    <div v-if="isLoading" class="chat-loading">
      <div class="loading-spinner"></div>
      <span>Memuat percakapan...</span>
    </div>

    <div v-else class="chat-messages" ref="chatArea">
      <div class="messages-inner">
        <!-- GPT-style file attachment cards at top -->
        <div v-if="conversationDocs.length > 0" class="file-context-section">
          <div class="file-context-cards">
            <div v-for="doc in conversationDocs" :key="doc.id" class="file-card">
              <div class="file-card-icon">
                <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/></svg>
              </div>
              <div class="file-card-info">
                <span class="file-card-name">{{ doc.filename || doc.fileName || 'Dokumen' }}</span>
                <span class="file-card-type">PDF · Dokumen Konteks</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Empty state -->
        <div v-if="messages.length === 0" class="chat-empty-state">
          <div class="empty-icon">
            <img src="/logo/ngambis.png" alt="Ngambis.AI" width="48" height="48" style="border-radius:14px;"/>
          </div>
          <h2>Halo! Saya Ngambis AI 👋</h2>
          <p>Upload dokumen kuliah lalu tanyakan apapun. Saya akan membantu memahami materi.</p>
          <button class="btn-pick-doc" @click="showDocPicker = true">📄 Pilih Dokumen & Mulai</button>
        </div>

        <div v-for="msg in messages" :key="msg.id" class="msg-row" :class="msg.type">
          <div v-if="msg.type === 'user'" class="bubble-user">
            <span class="bubble-user-text">{{ msg.content }}</span>
          </div>
          <div v-else class="ai-wrap">
            <div class="ai-ava"><img src="/logo/ngambis.png" alt="AI" width="36" height="36" style="border-radius:12px;"/></div>
            <div class="ai-body">
              <div class="ai-header">
                <span class="ai-src-lbl">NGAMBIS AI ✦</span>
              </div>
              <div class="ai-card">
                <div class="ai-content" :class="{ 'is-typing': msg.id === animatingMsgId }" v-html="renderMarkdown(getDisplayContent(msg))"></div>
              </div>
            </div>
          </div>
        </div>

        <div v-if="isSending" class="msg-row ai">
          <div class="ai-wrap">
            <div class="ai-ava"><img src="/logo/ngambis.png" alt="AI" width="32" height="32" style="border-radius:10px;"/></div>
            <div class="ai-body"><div class="typing"><span></span><span></span><span></span></div></div>
          </div>
        </div>
      </div>
    </div>

    <!-- Error banner -->
    <div v-if="sendError" class="chat-error" @click="sendError = null">
      ⚠️ {{ sendError }} <span class="dismiss">✕</span>
    </div>

    <div class="chat-input-area">
      <!-- GPT-style attached file chips -->
      <div v-if="docStore.selectedDocuments.length > 0 && !chatStore.currentConversation" class="attached-files">
        <div class="attached-files-inner">
          <TransitionGroup name="chip-pop">
            <div v-for="doc in docStore.selectedDocuments" :key="doc.id" class="attached-chip">
              <div class="attached-chip-icon">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
              </div>
              <div class="attached-chip-info">
                <span class="attached-chip-name">{{ doc.filename || doc.fileName || 'Dokumen' }}</span>
                <span class="attached-chip-type">PDF</span>
              </div>
              <button class="attached-chip-remove" @click="docStore.toggleDocumentSelection(doc.id)" title="Hapus">
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
              </button>
            </div>
          </TransitionGroup>
        </div>
      </div>
      <!-- Uploading indicator chip -->
      <div v-if="isDropUploading" class="attached-files">
        <div class="attached-files-inner">
          <div class="attached-chip uploading">
            <div class="attached-chip-icon uploading-icon">
              <div class="chip-spinner"></div>
            </div>
            <div class="attached-chip-info">
              <span class="attached-chip-name">Mengupload...</span>
              <span class="attached-chip-type">PDF</span>
            </div>
          </div>
        </div>
      </div>
      <div class="input-wrap">
        <button class="in-btn" title="Upload/Pilih dokumen" @click="showDocPicker = true">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m21.44 11.05-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/></svg>
        </button>
        <textarea v-model="messageInput" class="chat-input" placeholder="Tanyakan apapun tentang materi kuliah..." rows="1" @keydown="handleKeydown" :disabled="isSending"></textarea>
        <button class="send-btn" :class="{ active: messageInput.trim() && !isSending }" @click="sendMessage" :disabled="!messageInput.trim() || isSending">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/></svg>
        </button>
      </div>
      <p class="disclaimer">Ngambis.ai dapat membuat kesalahan. Pertimbangkan untuk memeriksa informasi penting.</p>
    </div>
  </div>
</template>

<style scoped>
.chat-page { display:flex; flex-direction:column; height:100vh; background:#F8FAFC; color:#1E293B; position:relative; }

/* Drag & Drop Overlay */
.drop-overlay { position:fixed; inset:0; z-index:999; background:rgba(59,130,246,.08); backdrop-filter:blur(6px); display:flex; align-items:center; justify-content:center; pointer-events:none; }
.drop-overlay-content { text-align:center; padding:3rem; background:rgba(255,255,255,.92); border:2px dashed #3B82F6; border-radius:24px; box-shadow:0 20px 60px rgba(59,130,246,.15); }
.drop-overlay-icon { color:#3B82F6; margin-bottom:1rem; animation:float-bounce 1.5s ease-in-out infinite; }
@keyframes float-bounce { 0%,100%{transform:translateY(0)} 50%{transform:translateY(-8px)} }
.drop-overlay-content h3 { font-size:1.25rem; font-weight:700; color:#0F172A; margin-bottom:.375rem; }
.drop-overlay-content p { font-size:.875rem; color:#64748B; }

/* Drop Notification */
.drop-notification { position:fixed; top:1rem; left:50%; transform:translateX(-50%); z-index:1000; display:flex; align-items:center; gap:.625rem; padding:.75rem 1.25rem; border-radius:12px; font-size:.8125rem; font-weight:600; box-shadow:0 8px 30px rgba(0,0,0,.12); }
.drop-notification.uploading { background:#EFF6FF; color:#1D4ED8; border:1px solid #BFDBFE; }
.drop-notif-spinner { width:18px; height:18px; border:2.5px solid #BFDBFE; border-top-color:#3B82F6; border-radius:50%; animation:spin .7s linear infinite; }
.drop-notification.error { background:#FEF2F2; color:#DC2626; border:1px solid #FECACA; cursor:pointer; }

/* Transitions */
.fade-enter-active, .fade-leave-active { transition:opacity .25s ease; }
.fade-enter-from, .fade-leave-to { opacity:0; }
.slide-down-enter-active, .slide-down-leave-active { transition:all .3s ease; }
.slide-down-enter-from, .slide-down-leave-to { opacity:0; transform:translateX(-50%) translateY(-1rem); }

.chat-header { display:flex; align-items:center; justify-content:space-between; padding:0.75rem 1.5rem; background:#fff; border-bottom:1px solid #E2E8F0; flex-shrink:0; z-index:10; gap:0.75rem; }
.chat-header-left { display:flex; align-items:center; gap:0.75rem; }
.btn-new-chat { display:inline-flex; align-items:center; gap:0.5rem; padding:0.625rem 1.25rem; background:#3B82F6; color:#fff; border:none; border-radius:10px; font-size:0.875rem; font-weight:600; cursor:pointer; transition:all .25s; box-shadow:0 2px 8px rgba(59,130,246,.25); }
.btn-new-chat:hover { background:#2563EB; }

.chat-user-area { position:relative; }
.chat-avatar-btn { display:flex; align-items:center; gap:.5rem; background:none; border:none; cursor:pointer; padding:4px; border-radius:10px; transition:background .2s; }
.chat-avatar-btn:hover { background:#F1F5F9; }
.chat-avatar { width:36px; height:36px; border-radius:50%; overflow:hidden; border:2px solid #E2E8F0; }
.chat-avatar img { width:100%; height:100%; object-fit:cover; }
.chat-avatar-initials { display:flex; align-items:center; justify-content:center; background:linear-gradient(135deg,#3B82F6,#8B5CF6); color:#fff; font-size:.75rem; font-weight:700; }
.chat-user-dropdown { position:absolute; right:0; top:calc(100%+8px); width:220px; background:#fff; border:1px solid #E2E8F0; border-radius:12px; box-shadow:0 10px 40px rgba(0,0,0,.1); padding:.5rem; z-index:60; animation:dropIn .2s ease; }
@keyframes dropIn { from{opacity:0;transform:translateY(-6px)} to{opacity:1;transform:translateY(0)} }
.dd-info { padding:.625rem .75rem; }
.dd-name { display:block; font-size:.875rem; font-weight:600; color:#1E293B; }
.dd-email { display:block; font-size:.75rem; color:#94A3B8; margin-top:2px; }
.dd-div { height:1px; background:#F1F5F9; margin:.25rem 0; }
.dd-item { display:flex; align-items:center; gap:.5rem; width:100%; padding:.5rem .75rem; background:none; border:none; border-radius:8px; font-size:.8125rem; font-weight:500; color:#EF4444; cursor:pointer; transition:background .15s; }
.dd-item:hover { background:#FEF2F2; }

/* File Context Cards (GPT-style) */
.file-context-section { padding-bottom:.5rem; }
.file-context-cards { display:flex; flex-wrap:wrap; gap:.5rem; }
.file-card { display:flex; align-items:center; gap:.75rem; padding:.75rem 1rem; background:#fff; border:1px solid #E2E8F0; border-radius:12px; min-width:200px; max-width:320px; transition:all .2s; cursor:default; box-shadow:0 1px 3px rgba(0,0,0,.04); }
.file-card:hover { border-color:#93C5FD; box-shadow:0 2px 8px rgba(59,130,246,.1); }
.file-card-icon { width:40px; height:40px; border-radius:10px; background:linear-gradient(135deg,#EF4444,#DC2626); display:flex; align-items:center; justify-content:center; flex-shrink:0; color:#fff; }
.file-card-info { flex:1; min-width:0; }
.file-card-name { display:block; font-size:.8125rem; font-weight:600; color:#0F172A; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
.file-card-type { display:block; font-size:.6875rem; color:#94A3B8; margin-top:1px; }

/* Chat Messages */
.chat-messages { flex:1; overflow-y:auto; padding:1.5rem 0; }
.messages-inner { max-width:780px; margin:0 auto; padding:0 1.5rem; display:flex; flex-direction:column; gap:1.75rem; }
.msg-row { display:flex; animation:msgIn .4s cubic-bezier(.16,1,.3,1); }
@keyframes msgIn { from{opacity:0;transform:translateY(12px)} to{opacity:1;transform:translateY(0)} }
.msg-row.user { justify-content:flex-end; }
.bubble-user { max-width:70%; padding:.875rem 1.25rem; background:linear-gradient(135deg,#3B82F6,#2563EB); color:#fff; border-radius:18px 18px 4px 18px; font-size:.9375rem; line-height:1.6; box-shadow:0 4px 12px rgba(59,130,246,.25); }
.ai-wrap { display:flex; gap:.75rem; max-width:90%; }
.ai-ava { flex-shrink:0; width:40px; height:40px; padding-top:2px; }
.ai-body { flex:1; min-width:0; }
.ai-header { display:flex; align-items:center; gap:.5rem; margin-bottom:.5rem; }
.ai-src-lbl { font-size:.6875rem; font-weight:700; background:linear-gradient(135deg,#3B82F6,#8B5CF6); -webkit-background-clip:text; -webkit-text-fill-color:transparent; letter-spacing:.06em; }
.ai-card { background:#fff; border:1px solid #E2E8F0; border-radius:12px; padding:1.125rem 1.25rem; box-shadow:0 1px 4px rgba(0,0,0,.04); }
.ai-content { font-size:.9375rem; color:#334155; line-height:1.8; }
.ai-content.is-typing::after { content:''; display:inline-block; width:2px; height:1em; background:#3B82F6; margin-left:2px; vertical-align:text-bottom; animation:blink-cursor .6s steps(2) infinite; }
@keyframes blink-cursor { 0%{opacity:1} 100%{opacity:0} }
.ai-content :deep(strong) { color:#0F172A; font-weight:700; }
.ai-content :deep(p),
.ai-content :deep(ul),
.ai-content :deep(ol),
.ai-content :deep(h1),
.ai-content :deep(h2),
.ai-content :deep(h3),
.ai-content :deep(h4),
.ai-content :deep(pre) {
  margin-top: 0;
  margin-bottom: 0.625rem;
}
.ai-content :deep(ul), .ai-content :deep(ol) { padding-left:1.5rem; }
.ai-content :deep(ul) { list-style-type: disc; }
.ai-content :deep(ol) { list-style-type: decimal; }
.ai-content :deep(li) { margin-bottom:0.25rem; padding-left:0.125rem; line-height:1.6; }
.ai-content :deep(li:last-child) { margin-bottom:0; }
.ai-content :deep(li)::marker { color:#3B82F6; font-weight:700; }
.ai-content :deep(p) { line-height:1.7; }
.ai-content :deep(p:last-child),
.ai-content :deep(ul:last-child),
.ai-content :deep(ol:last-child) { margin-bottom:0; }
.ai-content :deep(.md-h3) { font-size:1.05rem; font-weight:700; color:#0F172A; padding-bottom:.375rem; border-bottom:2px solid #DBEAFE; }
.ai-content :deep(.md-h4) { font-size:.95rem; font-weight:700; color:#1E293B; padding-left:.625rem; border-left:3px solid #3B82F6; }
.ai-content :deep(.md-hr) { border:none; height:1px; background:linear-gradient(90deg,#E2E8F0,#DBEAFE,#E2E8F0); margin:1.25rem 0; }
.ai-content :deep(.ai-code) { background:#0F172A; color:#E2E8F0; border:none; border-radius:10px; padding:1rem 1.25rem; overflow-x:auto; font-size:.8125rem; font-family:'Fira Code',monospace; }
.ai-content :deep(.ai-inline-code) { background:#EFF6FF; padding:.15rem .4rem; border-radius:5px; font-size:.85em; color:#1D4ED8; font-weight:600; border:1px solid #BFDBFE; }

/* Typing indicator */
.typing { display:flex; gap:4px; padding:.75rem 0; }
.typing span { width:8px; height:8px; border-radius:50%; background:#94A3B8; animation:bounce 1.4s infinite ease-in-out; }
.typing span:nth-child(1){animation-delay:0s} .typing span:nth-child(2){animation-delay:.2s} .typing span:nth-child(3){animation-delay:.4s}
@keyframes bounce { 0%,80%,100%{transform:scale(.6);opacity:.4} 40%{transform:scale(1);opacity:1} }

/* Loading state */
.chat-loading { flex:1; display:flex; flex-direction:column; align-items:center; justify-content:center; gap:1rem; color:#94A3B8; font-size:.875rem; }
.loading-spinner { width:32px; height:32px; border:3px solid #E2E8F0; border-top-color:#3B82F6; border-radius:50%; animation:spin .8s linear infinite; }
@keyframes spin { to { transform:rotate(360deg); } }

/* Empty state */
.chat-empty-state { text-align:center; padding:4rem 2rem; }
.empty-icon { margin:0 auto 1.5rem; }
.chat-empty-state h2 { font-size:1.5rem; font-weight:700; color:#0F172A; margin-bottom:.75rem; }
.chat-empty-state p { color:#64748B; font-size:.9375rem; max-width:400px; margin:0 auto 1.5rem; line-height:1.6; }
.btn-pick-doc { padding:.75rem 1.5rem; background:#3B82F6; color:#fff; border:none; border-radius:10px; font-size:.875rem; font-weight:600; cursor:pointer; transition:all .2s; }
.btn-pick-doc:hover { background:#2563EB; transform:translateY(-2px); }

/* Error banner */
.chat-error { padding:.625rem 1.5rem; background:#FEF2F2; color:#DC2626; font-size:.8125rem; text-align:center; cursor:pointer; border-top:1px solid #FECACA; display:flex; align-items:center; justify-content:center; gap:.5rem; }
.dismiss { font-weight:700; }

/* Input area */
.chat-input-area { flex-shrink:0; padding:.75rem 1.5rem 1rem; background:#fff; border-top:1px solid #E2E8F0; }
.input-wrap { max-width:780px; margin:0 auto; display:flex; align-items:center; gap:.5rem; background:#F8FAFC; border:1px solid #E2E8F0; border-radius:14px; padding:.5rem .75rem; transition:border-color .2s,box-shadow .2s; }
.input-wrap:focus-within { border-color:#3B82F6; box-shadow:0 0 0 3px rgba(59,130,246,.1); }
.in-btn { width:36px; height:36px; display:flex; align-items:center; justify-content:center; background:none; border:none; color:#94A3B8; border-radius:8px; cursor:pointer; flex-shrink:0; transition:all .2s; position:relative; }
.in-btn:hover { background:#E2E8F0; color:#475569; }
.in-btn:disabled { opacity:.5; cursor:default; }
.doc-badge { position:absolute; top:-2px; right:-2px; background:#3B82F6; color:#fff; font-size:.625rem; font-weight:700; min-width:16px; height:16px; border-radius:8px; display:flex; align-items:center; justify-content:center; padding:0 4px; border:2px solid #F8FAFC; box-sizing:content-box; line-height:1; }
.chat-input { flex:1; border:none; background:transparent; font-size:.9375rem; color:#1E293B; resize:none; outline:none; line-height:1.5; max-height:120px; padding:.375rem 0; font-family:inherit; }
.chat-input::placeholder { color:#94A3B8; }
.send-btn { width:38px; height:38px; display:flex; align-items:center; justify-content:center; background:#CBD5E1; border:none; border-radius:10px; color:#fff; cursor:not-allowed; flex-shrink:0; transition:all .25s; }
.send-btn.active { background:#3B82F6; cursor:pointer; box-shadow:0 2px 8px rgba(59,130,246,.3); }
.send-btn.active:hover { background:#2563EB; transform:scale(1.05); }
.disclaimer { text-align:center; font-size:.6875rem; color:#94A3B8; margin-top:.5rem; }

/* Attached File Chips (GPT-style) */
.attached-files { max-width:780px; margin:0 auto .5rem; }
.attached-files-inner { display:flex; flex-wrap:wrap; gap:.5rem; }
.attached-chip { display:flex; align-items:center; gap:.625rem; padding:.5rem .75rem; background:#fff; border:1px solid #E2E8F0; border-radius:12px; max-width:280px; animation:chipIn .25s ease; box-shadow:0 1px 3px rgba(0,0,0,.05); }
@keyframes chipIn { from{opacity:0;transform:scale(.9)} to{opacity:1;transform:scale(1)} }
.attached-chip.uploading { opacity:.7; }
.attached-chip-icon { width:32px; height:32px; border-radius:8px; background:linear-gradient(135deg,#EF4444,#DC2626); display:flex; align-items:center; justify-content:center; flex-shrink:0; color:#fff; }
.attached-chip-icon.uploading-icon { background:#CBD5E1; }
.chip-spinner { width:16px; height:16px; border:2px solid #E2E8F0; border-top-color:#3B82F6; border-radius:50%; animation:spin .7s linear infinite; }
.attached-chip-info { flex:1; min-width:0; }
.attached-chip-name { display:block; font-size:.75rem; font-weight:600; color:#1E293B; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
.attached-chip-type { display:block; font-size:.625rem; color:#94A3B8; margin-top:1px; }
.attached-chip-remove { display:flex; align-items:center; justify-content:center; width:22px; height:22px; border-radius:50%; background:#F1F5F9; border:none; color:#94A3B8; cursor:pointer; flex-shrink:0; transition:all .15s; }
.attached-chip-remove:hover { background:#FEE2E2; color:#EF4444; }

/* Chip pop transition */
.chip-pop-enter-active { transition:all .25s ease; }
.chip-pop-leave-active { transition:all .2s ease; }
.chip-pop-enter-from { opacity:0; transform:scale(.8); }
.chip-pop-leave-to { opacity:0; transform:scale(.8) translateX(-8px); }

/* Document Picker */
.doc-picker-overlay { position:fixed; inset:0; background:rgba(0,0,0,.4); backdrop-filter:blur(4px); z-index:200; display:flex; align-items:center; justify-content:center; padding:1rem; }
.doc-picker { position:relative; background:#fff; border-radius:16px; padding:2rem; max-width:500px; width:100%; max-height:80vh; overflow-y:auto; box-shadow:0 20px 60px rgba(0,0,0,.15); }
.btn-close-modal { position:absolute; top:1.25rem; right:1.5rem; background:none; border:none; font-size:1.25rem; color:#94A3B8; cursor:pointer; padding:0.25rem; transition:color 0.2s; line-height:1; }
.btn-close-modal:hover { color:#0F172A; }
.doc-picker h3 { font-size:1.125rem; font-weight:700; color:#0F172A; margin-bottom:.375rem; padding-right:1.5rem; }
.doc-picker-hint { font-size:.8125rem; color:#64748B; margin-bottom:1.25rem; }
.doc-list { display:flex; flex-direction:column; gap:.5rem; margin-bottom:1.25rem; }
.doc-item { display:flex; align-items:center; gap:.75rem; padding:.75rem 1rem; border:1px solid #E2E8F0; border-radius:10px; cursor:pointer; transition:all .2s; }
.doc-item:hover { background:#F8FAFC; }
.doc-item.selected { border-color:#3B82F6; background:#EFF6FF; }
.doc-item input[type="checkbox"] { accent-color:#3B82F6; width:16px; height:16px; }
.doc-item-info { flex:1; min-width:0; }
.doc-name { display:block; font-size:.875rem; font-weight:600; color:#1E293B; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
.doc-meta { display:block; font-size:.6875rem; color:#94A3B8; margin-top:2px; }
.doc-empty { padding:2rem; text-align:center; color:#94A3B8; font-size:.875rem; }
.doc-picker-actions { display:flex; gap:.75rem; justify-content:flex-end; margin-top:1.5rem; }
.btn-upload-doc { padding:.625rem 1rem; background:#F1F5F9; color:#475569; border:1px solid #E2E8F0; border-radius:8px; font-size:.8125rem; font-weight:600; cursor:pointer; transition:all .2s; }
.btn-upload-doc:hover { background:#E2E8F0; }
.btn-upload-doc:disabled { opacity:.5; cursor:default; }
.btn-confirm-doc { padding:.625rem 1.25rem; background:#3B82F6; color:#fff; border:none; border-radius:8px; font-size:.8125rem; font-weight:600; cursor:pointer; transition:all .2s; }
.btn-confirm-doc:hover { background:#2563EB; }
.btn-confirm-doc:disabled { opacity:.5; cursor:default; }

@media(max-width:768px) { .messages-inner{padding:0 1rem} .chat-header{padding:.75rem 1rem} .chat-input-area{padding:.75rem 1rem 1rem} .bubble-user{max-width:85%} .ai-wrap{max-width:95%} .doc-picker{margin:0 .5rem;padding:1.5rem} .ai-card{padding:.875rem 1rem} .file-card{min-width:160px;max-width:100%} }
</style>
