<script setup>
import { ref, computed, nextTick, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
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

const messageInput = ref('')
const chatArea = ref(null)
const isTyping = ref(false)

const messages = ref([
  {
    id: 1, type: 'user',
    content: 'Halo Ngambis! Bisa tolong jelaskan apa itu Algoritma Dijkstra dan bagaimana cara kerjanya secara sederhana?'
  },
  {
    id: 2, type: 'ai', content: '',
    richContent: {
      title: 'Memahami Algoritma Dijkstra',
      intro: 'Bayangkan kamu sedang berada di sebuah kota besar dan ingin mencari jalur terpendek dari rumahmu ke kampus. Algoritma Dijkstra bekerja persis seperti Google Maps.',
      definition: { label: 'Definisi Inti:', text: 'Algoritma Dijkstra digunakan untuk menemukan jalur terpendek antara simpul-simpul dalam graf, yang dapat mewakili, misalnya, jaringan jalan.' },
      stepsIntro: 'Cara kerjanya secara bertahap:',
      steps: [
        'Tentukan titik awal dan berikan nilai "jarak" 0. Titik lainnya dianggap berjarak tak terhingga (∞).',
        'Kunjungi tetangga dari titik aktif saat ini dan hitung total jaraknya.',
        'Pilih tetangga dengan jarak terkecil sebagai titik aktif berikutnya.'
      ]
    }
  }
])

function handleNewChat() { messages.value = [] }

async function sendMessage() {
  const text = messageInput.value.trim()
  if (!text) return
  messages.value.push({ id: Date.now(), type: 'user', content: text })
  messageInput.value = ''
  await nextTick()
  scrollToBottom()
  isTyping.value = true
  setTimeout(() => {
    messages.value.push({ id: Date.now() + 1, type: 'ai', content: 'Terima kasih atas pertanyaannya! Saya sedang memproses jawaban untuk kamu. Fitur AI Chat masih dalam tahap pengembangan — nantikan update selanjutnya! 🚀' })
    isTyping.value = false
    nextTick(() => scrollToBottom())
  }, 1500)
}

function scrollToBottom() {
  if (chatArea.value) chatArea.value.scrollTop = chatArea.value.scrollHeight
}

function handleKeydown(e) {
  if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); sendMessage() }
}

const isReady = ref(false)
onMounted(() => {
  requestAnimationFrame(() => { isReady.value = true })
  nextTick(() => scrollToBottom())
})
</script>

<template>
  <div class="chat-page" :class="{ ready: isReady }" @click="closeUserMenu">
    <header class="chat-header">
      <button class="btn-new-chat" @click="handleNewChat">
        <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/></svg>
        <span>Chat Baru</span>
      </button>
      <div class="chat-user-area" @click.stop>
        <button class="chat-avatar-btn" @click="toggleUserMenu">
          <div v-if="avatarUrl" class="chat-avatar"><img :src="avatarUrl" :alt="displayName" referrerpolicy="no-referrer"/></div>
          <div v-else class="chat-avatar chat-avatar-initials">{{ initials }}</div>
          <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#64748B" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="m6 9 6 6 6-6"/></svg>
        </button>
        <div v-if="showUserMenu" class="chat-user-dropdown">
          <div class="dd-info"><span class="dd-name">{{ displayName }}</span><span class="dd-email">{{ authStore.userEmail }}</span></div>
          <div class="dd-div"></div>
          <button class="dd-item" @click="handleLogout">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
            Logout
          </button>
        </div>
      </div>
    </header>

    <div class="chat-messages" ref="chatArea">
      <div class="messages-inner">
        <div v-for="msg in messages" :key="msg.id" class="msg-row" :class="msg.type">
          <div v-if="msg.type === 'user'" class="bubble-user">{{ msg.content }}</div>
          <div v-else class="ai-wrap">
            <div class="ai-ava"><svg width="32" height="32" viewBox="0 0 40 40" fill="none"><rect width="40" height="40" rx="12" fill="#EEF2FF"/><path d="M12 28V16l8 5-8 5z" fill="#3B82F6"/><path d="M20 28V16l8 5-8 5z" fill="#60A5FA"/></svg></div>
            <div class="ai-body">
              <div class="ai-src"><span class="ai-src-lbl">NGAMBIS AI INSIGHT ✦</span>
                <button class="ai-copy" title="Salin"><svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="9" y="9" width="13" height="13" rx="2" ry="2"/><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/></svg></button>
              </div>
              <template v-if="msg.richContent">
                <h2 class="ai-h2">{{ msg.richContent.title }}</h2>
                <p class="ai-p">{{ msg.richContent.intro }}</p>
                <div class="ai-def" v-if="msg.richContent.definition">
                  <span class="ai-def-lbl">{{ msg.richContent.definition.label }}</span>
                  <p>{{ msg.richContent.definition.text }}</p>
                </div>
                <p class="ai-p" v-if="msg.richContent.stepsIntro">{{ msg.richContent.stepsIntro }}</p>
                <ol class="ai-steps" v-if="msg.richContent.steps">
                  <li v-for="(step, i) in msg.richContent.steps" :key="i"><span class="sn">{{ i + 1 }}</span><span>{{ step }}</span></li>
                </ol>
              </template>
              <p v-else class="ai-p">{{ msg.content }}</p>
            </div>
          </div>
        </div>
        <div v-if="isTyping" class="msg-row ai">
          <div class="ai-wrap">
            <div class="ai-ava"><svg width="32" height="32" viewBox="0 0 40 40" fill="none"><rect width="40" height="40" rx="12" fill="#EEF2FF"/><path d="M12 28V16l8 5-8 5z" fill="#3B82F6"/><path d="M20 28V16l8 5-8 5z" fill="#60A5FA"/></svg></div>
            <div class="ai-body"><div class="typing"><span></span><span></span><span></span></div></div>
          </div>
        </div>
      </div>
    </div>

    <div class="chat-input-area">
      <div class="input-wrap">
        <button class="in-btn" title="Lampirkan file"><svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m21.44 11.05-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/></svg></button>
        <textarea v-model="messageInput" class="chat-input" placeholder="Tanyakan apapun tentang algoritma..." rows="1" @keydown="handleKeydown"></textarea>
        <button class="in-btn" title="Voice input"><svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3z"/><path d="M19 10v2a7 7 0 0 1-14 0v-2"/><line x1="12" y1="19" x2="12" y2="23"/><line x1="8" y1="23" x2="16" y2="23"/></svg></button>
        <button class="send-btn" :class="{ active: messageInput.trim() }" @click="sendMessage" :disabled="!messageInput.trim()"><svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/></svg></button>
      </div>
      <p class="disclaimer">Ngambis.ai dapat membuat kesalahan. Pertimbangkan untuk memeriksa informasi penting.</p>
    </div>
  </div>
</template>

<style scoped>
.chat-page { display:flex; flex-direction:column; height:100vh; background:#F8FAFC; color:#1E293B; }

.chat-header { display:flex; align-items:center; justify-content:space-between; padding:0.75rem 1.5rem; background:#fff; border-bottom:1px solid #E2E8F0; flex-shrink:0; z-index:10; }
.btn-new-chat { display:inline-flex; align-items:center; gap:0.5rem; padding:0.625rem 1.25rem; background:#3B82F6; color:#fff; border:none; border-radius:10px; font-size:0.875rem; font-weight:600; cursor:pointer; transition:all .25s; box-shadow:0 2px 8px rgba(59,130,246,.25); }
.btn-new-chat:hover { background:#2563EB; transform:translateY(-1px); box-shadow:0 4px 16px rgba(59,130,246,.35); }

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

.chat-messages { flex:1; overflow-y:auto; padding:1.5rem 0; }
.messages-inner { max-width:780px; margin:0 auto; padding:0 1.5rem; display:flex; flex-direction:column; gap:1.5rem; }
.msg-row { display:flex; animation:msgIn .4s cubic-bezier(.16,1,.3,1); }
@keyframes msgIn { from{opacity:0;transform:translateY(12px)} to{opacity:1;transform:translateY(0)} }
.msg-row.user { justify-content:flex-end; }

.bubble-user { max-width:70%; padding:.875rem 1.25rem; background:#3B82F6; color:#fff; border-radius:18px 18px 4px 18px; font-size:.9375rem; line-height:1.6; box-shadow:0 2px 8px rgba(59,130,246,.2); }

.ai-wrap { display:flex; gap:.75rem; max-width:85%; }
.ai-ava { flex-shrink:0; width:36px; height:36px; }
.ai-ava svg { width:36px; height:36px; }
.ai-body { flex:1; min-width:0; }
.ai-src { display:flex; align-items:center; gap:.5rem; margin-bottom:.75rem; }
.ai-src-lbl { font-size:.6875rem; font-weight:700; color:#3B82F6; letter-spacing:.06em; }
.ai-copy { width:28px; height:28px; display:flex; align-items:center; justify-content:center; background:none; border:none; color:#94A3B8; border-radius:6px; cursor:pointer; transition:all .2s; }
.ai-copy:hover { background:#F1F5F9; color:#3B82F6; }
.ai-h2 { font-size:1.25rem; font-weight:700; color:#0F172A; margin-bottom:.75rem; line-height:1.3; }
.ai-p { font-size:.9375rem; color:#475569; line-height:1.7; margin-bottom:1rem; }
.ai-def { background:#F8FAFC; border-left:3px solid #3B82F6; border-radius:0 10px 10px 0; padding:1rem 1.25rem; margin:1rem 0; }
.ai-def-lbl { display:block; font-size:.8125rem; font-weight:700; color:#3B82F6; margin-bottom:.375rem; }
.ai-def p { font-size:.875rem; color:#475569; line-height:1.65; margin:0; }
.ai-steps { list-style:none; padding:0; display:flex; flex-direction:column; gap:.75rem; }
.ai-steps li { display:flex; gap:.75rem; align-items:flex-start; }
.sn { flex-shrink:0; width:26px; height:26px; border-radius:50%; background:#EF4444; color:#fff; display:flex; align-items:center; justify-content:center; font-size:.75rem; font-weight:700; margin-top:2px; }

.typing { display:flex; gap:4px; padding:.75rem 0; }
.typing span { width:8px; height:8px; border-radius:50%; background:#94A3B8; animation:bounce 1.4s infinite ease-in-out; }
.typing span:nth-child(1){animation-delay:0s} .typing span:nth-child(2){animation-delay:.2s} .typing span:nth-child(3){animation-delay:.4s}
@keyframes bounce { 0%,80%,100%{transform:scale(.6);opacity:.4} 40%{transform:scale(1);opacity:1} }

.chat-input-area { flex-shrink:0; padding:.75rem 1.5rem 1rem; background:#fff; border-top:1px solid #E2E8F0; }
.input-wrap { max-width:780px; margin:0 auto; display:flex; align-items:center; gap:.5rem; background:#F8FAFC; border:1px solid #E2E8F0; border-radius:14px; padding:.5rem .75rem; transition:border-color .2s,box-shadow .2s; }
.input-wrap:focus-within { border-color:#3B82F6; box-shadow:0 0 0 3px rgba(59,130,246,.1); }
.in-btn { width:36px; height:36px; display:flex; align-items:center; justify-content:center; background:none; border:none; color:#94A3B8; border-radius:8px; cursor:pointer; flex-shrink:0; transition:all .2s; }
.in-btn:hover { background:#E2E8F0; color:#475569; }
.chat-input { flex:1; border:none; background:transparent; font-size:.9375rem; color:#1E293B; resize:none; outline:none; line-height:1.5; max-height:120px; padding:.375rem 0; font-family:inherit; }
.chat-input::placeholder { color:#94A3B8; }
.send-btn { width:38px; height:38px; display:flex; align-items:center; justify-content:center; background:#CBD5E1; border:none; border-radius:10px; color:#fff; cursor:not-allowed; flex-shrink:0; transition:all .25s; }
.send-btn.active { background:#3B82F6; cursor:pointer; box-shadow:0 2px 8px rgba(59,130,246,.3); }
.send-btn.active:hover { background:#2563EB; transform:scale(1.05); }
.disclaimer { text-align:center; font-size:.6875rem; color:#94A3B8; margin-top:.5rem; }

@media(max-width:768px) { .messages-inner{padding:0 1rem} .chat-header{padding:.75rem 1rem} .chat-input-area{padding:.75rem 1rem 1rem} .bubble-user{max-width:85%} .ai-wrap{max-width:95%} }
</style>
