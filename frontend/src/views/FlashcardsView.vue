<script setup>
import { ref, computed, inject, onMounted, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useQuizStore } from '@/stores/quiz'
import { useDocumentStore } from '@/stores/document'
import DocumentManager from '@/components/shared/DocumentManager.vue'

const authStore = useAuthStore()
const quizStore = useQuizStore()
const docStore = useDocumentStore()
const toggleSidebar = inject('toggleSidebar', () => {})

const displayName = computed(() => authStore.userName || 'User')
const avatarUrl = computed(() => authStore.userAvatar)
const initials = computed(() => displayName.value.split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2))

const showUserMenu = ref(false)
function toggleUserMenu() { showUserMenu.value = !showUserMenu.value }
function closeUserMenu() { showUserMenu.value = false }
function handleLogout() { authStore.logout(); closeUserMenu() }

const isFlipped = ref(false)
const showSetup = ref(true)
const questionCount = ref(5)
const setupError = ref(null)

const currentCard = computed(() => quizStore.currentQuestion)
const totalCards = computed(() => quizStore.totalQuestions)
const currentIndex = computed(() => quizStore.currentQuestionIndex)
const progressPercent = computed(() => quizStore.progressPercent)
const isGenerating = computed(() => quizStore.generating)

function flipCard() { isFlipped.value = !isFlipped.value }

function prevCard() {
  if (currentIndex.value > 0) {
    isFlipped.value = false
    quizStore.prevQuestion()
  }
}

function nextCard() {
  if (!quizStore.isLastQuestion) {
    isFlipped.value = false
    quizStore.nextQuestion()
  }
}



async function startFlashcards() {
  if (docStore.selectedDocumentIds.length === 0) {
    setupError.value = 'Pilih minimal satu dokumen.'
    return
  }
  setupError.value = null
  try {
    await quizStore.createSession({
      documentIds: docStore.selectedDocumentIds,
      personaType: 'FRIENDLY_SENIOR',
      questionCount: questionCount.value,
      itemType: 'ESSAY'
    })
    showSetup.value = false
    isFlipped.value = false
  } catch (err) {
    setupError.value = quizStore.error || 'Gagal membuat flashcard.'
  }
}

function handleNewFlashcard() {
  quizStore.resetQuiz()
  docStore.clearSelection()
  showSetup.value = true
  isFlipped.value = false
}

onMounted(() => {
  docStore.loadDocuments()
  if (quizStore.quizItems.length > 0 && quizStore.currentQuestion?.itemType === 'ESSAY') {
    showSetup.value = false
  }
})

watch(() => quizStore.currentSession, (newSession) => {
  if (newSession && quizStore.quizItems.length > 0) {
    showSetup.value = false
    isFlipped.value = false
  }
})
</script>

<template>
  <div class="fc-page" @click="closeUserMenu">
    <header class="fc-header">
      <div class="fc-header-left">
        <button class="burger-btn" @click.stop="toggleSidebar" aria-label="Menu">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="18" x2="21" y2="18"/></svg>
        </button>
        <button class="btn-new" @click="handleNewFlashcard">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/></svg>
          Flashcard Baru
        </button>
      </div>
      <div class="fc-user-area" @click.stop>
        <button class="fc-avatar-btn" @click="toggleUserMenu">
          <div v-if="avatarUrl" class="fc-avatar"><img :src="avatarUrl" :alt="displayName" referrerpolicy="no-referrer"/></div>
          <div v-else class="fc-avatar fc-avatar-initials">{{ initials }}</div>
        </button>
        <div v-if="showUserMenu" class="fc-user-dropdown">
          <div class="dd-info"><span class="dd-name">{{ displayName }}</span><span class="dd-email">{{ authStore.userEmail }}</span></div>
          <div class="dd-div"></div>
          <button class="dd-item" @click="handleLogout">Logout</button>
        </div>
      </div>
    </header>

    <!-- Setup Modal -->
    <div v-if="showSetup" class="fc-setup">
      <div class="setup-card">
        <div class="setup-icon">🃏</div>
        <h2>Buat Smart Flashcards</h2>
        <p>Upload dokumen PDF kuliah, dan AI akan membuat kartu-kartu belajar untuk kamu.</p>

        <DocumentManager mode="chip" />

        <div class="setup-count-section">
          <div class="count-label">
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#3B82F6" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect width="18" height="18" x="3" y="3" rx="2" ry="2"/><line x1="3" y1="9" x2="21" y2="9"/><line x1="9" y1="21" x2="9" y2="9"/></svg>
            <span>Pilih Jumlah Kartu</span>
          </div>
          <div class="count-options">
            <button
              v-for="opt in [3, 5, 8, 10]"
              :key="opt"
              class="count-pill"
              :class="{ active: questionCount === opt }"
              @click="questionCount = opt"
              type="button"
            >
              <span class="pill-cards">
                <span v-for="i in Math.min(opt, 4)" :key="i" class="mini-card" :style="{ transform: `rotate(${(i - 2) * 8}deg) translateY(${-i}px)` }"></span>
              </span>
              <span class="pill-num">{{ opt }}</span>
              <span class="pill-label">kartu</span>
            </button>
          </div>
        </div>

        <div v-if="setupError" class="setup-error">{{ setupError }}</div>

        <div class="setup-actions">
          <button class="btn-generate" @click="startFlashcards" :disabled="isGenerating || docStore.selectedDocumentIds.length === 0">
            {{ isGenerating ? '⏳ AI sedang membuat kartu...' : '✨ Generate Flashcards' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Flashcard Content -->
    <div v-else class="fc-content">
      <div class="fc-progress-section">
        <div class="fc-prog-left">
          <div class="fc-prog-icon"><svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#3B82F6" stroke-width="2"><ellipse cx="12" cy="5" rx="9" ry="3"/><path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3"/><path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"/></svg></div>
          <div class="fc-prog-text">
            <h2>{{ quizStore.currentSession?.documentFilename || 'Flashcards' }}</h2>
            <p>Flashcard Session • AI Generated</p>
          </div>
        </div>
        <div class="fc-prog-right">
          <span class="prog-label">PROGRESS BELAJAR</span>
          <div class="prog-bar-wrap">
            <div class="prog-bar"><div class="prog-fill" :style="{ width: progressPercent + '%' }"></div></div>
            <span class="prog-count"><strong>{{ currentIndex + 1 }}</strong>/{{ totalCards }}</span>
          </div>
        </div>
      </div>

      <div class="fc-card-container" v-if="currentCard">
        <div class="fc-card" :class="{ flipped: isFlipped }">
          <div class="fc-face fc-front" @click="flipCard">
            <div class="badge-pill">PERTANYAAN {{ currentIndex + 1 }}</div>
            <h1 class="front-q">{{ currentCard.questionText }}</h1>
            <div class="flip-action">
              <button class="btn-flip"><svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/><path d="M3 3v5h5"/></svg></button>
              <span class="flip-text">Klik untuk melihat jawaban</span>
            </div>
          </div>
          <div class="fc-face fc-back" @click="flipCard">
            <div class="verified-badge">AI Answer</div>
            <div class="back-q-section">
              <div class="back-label"><svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#64748B" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg> PERTANYAAN</div>
              <h2 class="back-q">{{ currentCard.questionText }}</h2>
            </div>
            <div class="back-a-section">
              <div class="back-label success"><svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="#10B981" stroke="none"><circle cx="12" cy="12" r="10"/><path d="m9 12 2 2 4-4" stroke="white" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"/></svg> JAWABAN AI</div>
              <div class="back-a-text">{{ currentCard.referenceText }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="fc-controls">
        <button class="ctrl-btn btn-prev" @click="prevCard" :disabled="currentIndex === 0">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg>
          Sebelumnya
        </button>
        <span class="ctrl-counter">{{ currentIndex + 1 }} / {{ totalCards }}</span>
        <button v-if="!quizStore.isLastQuestion" class="ctrl-btn btn-next" @click="nextCard">
          Selanjutnya
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>
        </button>
        <button v-else class="ctrl-btn btn-finish" @click="handleNewFlashcard">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
          Selesai
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.fc-page { min-height:100vh; background:#F8FAFC; color:#1E293B; display:flex; flex-direction:column; }
.fc-header { display:flex; align-items:center; justify-content:space-between; padding:1rem 2rem; background:#FFF; border-bottom:1px solid #E2E8F0; }
.fc-header-left { display:flex; align-items:center; gap:.75rem; }
.btn-new { display:inline-flex; align-items:center; gap:.5rem; background:#0052CC; color:#FFF; padding:.625rem 1rem; border-radius:8px; font-size:.875rem; font-weight:600; border:none; cursor:pointer; transition:all .2s; }
.btn-new:hover { background:#0043A6; }
.fc-user-area { position:relative; }
.fc-avatar-btn { display:flex; align-items:center; gap:.5rem; background:none; border:none; cursor:pointer; padding:4px; border-radius:10px; transition:background .2s; }
.fc-avatar-btn:hover { background:#F1F5F9; }
.fc-avatar { width:36px; height:36px; border-radius:10px; overflow:hidden; border:2px solid #E2E8F0; }
.fc-avatar img { width:100%; height:100%; object-fit:cover; }
.fc-avatar-initials { display:flex; align-items:center; justify-content:center; background:linear-gradient(135deg,#3B82F6,#8B5CF6); color:#fff; font-size:.75rem; font-weight:700; }
.fc-user-dropdown { position:absolute; right:0; top:calc(100%+8px); width:220px; background:#fff; border:1px solid #E2E8F0; border-radius:12px; box-shadow:0 10px 40px rgba(0,0,0,.1); padding:.5rem; z-index:60; }
.dd-info { padding:.625rem .75rem; } .dd-name { display:block; font-size:.875rem; font-weight:600; color:#1E293B; } .dd-email { display:block; font-size:.75rem; color:#94A3B8; margin-top:2px; }
.dd-div { height:1px; background:#F1F5F9; margin:.25rem 0; } .dd-item { display:flex; width:100%; padding:.5rem .75rem; background:none; border:none; border-radius:8px; font-size:.8125rem; font-weight:500; color:#EF4444; cursor:pointer; } .dd-item:hover { background:#FEF2F2; }

/* Setup */
.fc-setup { flex:1; display:flex; align-items:center; justify-content:center; padding:2rem; }
.setup-card { background:#fff; border:1px solid #E2E8F0; border-radius:20px; padding:3rem; max-width:520px; width:100%; text-align:center; box-shadow:0 10px 40px rgba(0,0,0,.04); }
.setup-icon { font-size:3rem; margin-bottom:1rem; }
.setup-card h2 { font-size:1.5rem; font-weight:800; color:#0F172A; margin-bottom:.5rem; }
.setup-card > p { color:#64748B; font-size:.9375rem; margin-bottom:1.5rem; line-height:1.6; }
.setup-docs { text-align:left; margin-bottom:1.25rem; }
.setup-label { display:block; font-size:.8125rem; font-weight:700; color:#475569; margin-bottom:.5rem; }
.doc-list-mini { display:flex; flex-wrap:wrap; gap:.5rem; }
.doc-chip { display:inline-flex; align-items:center; gap:.375rem; padding:.5rem .75rem; background:#F8FAFC; border:1px solid #E2E8F0; border-radius:8px; font-size:.8125rem; cursor:pointer; transition:all .2s; }
.doc-chip.selected { border-color:#3B82F6; background:#EFF6FF; color:#2563EB; }
.doc-chip input { display:none; }
/* Card Count Selector */
.setup-count-section { margin-bottom:1.5rem; }
.count-label { display:flex; align-items:center; justify-content:center; gap:.5rem; margin-bottom:1rem; }
.count-label span { font-size:.875rem; font-weight:700; color:#475569; letter-spacing:.01em; }
.count-options { display:flex; gap:.75rem; justify-content:center; flex-wrap:wrap; }
.count-pill { display:flex; flex-direction:column; align-items:center; gap:.5rem; padding:1rem 1.25rem .875rem; background:#fff; border:2px solid #E2E8F0; border-radius:16px; cursor:pointer; transition:all .25s cubic-bezier(.4,0,.2,1); min-width:72px; position:relative; box-shadow:0 2px 8px rgba(0,0,0,.04); }
.count-pill:hover { border-color:#93C5FD; background:#F0F7FF; transform:translateY(-3px); box-shadow:0 6px 20px rgba(59,130,246,.12); }
.count-pill.active { border-color:#3B82F6; background:linear-gradient(to bottom, #EFF6FF, #DBEAFE); box-shadow:0 4px 16px rgba(59,130,246,.2), inset 0 1px 0 rgba(255,255,255,.8); animation:pillBounce .35s cubic-bezier(.34,1.56,.64,1); }
@keyframes pillBounce { 0%{transform:scale(.92)} 50%{transform:scale(1.06)} 100%{transform:scale(1)} }
.pill-cards { display:flex; align-items:center; justify-content:center; position:relative; width:32px; height:24px; }
.mini-card { position:absolute; width:18px; height:22px; background:#CBD5E1; border:1.5px solid #94A3B8; border-radius:4px; transition:all .25s; }
.count-pill:hover .mini-card { background:#93C5FD; border-color:#60A5FA; }
.count-pill.active .mini-card { background:#3B82F6; border-color:#2563EB; box-shadow:0 1px 3px rgba(37,99,235,.3); }
.pill-num { font-size:1.375rem; font-weight:800; color:#1E293B; line-height:1; }
.count-pill.active .pill-num { color:#1D4ED8; }
.pill-label { font-size:.625rem; font-weight:700; color:#94A3B8; text-transform:uppercase; letter-spacing:.08em; }
.count-pill.active .pill-label { color:#3B82F6; }
.setup-error { padding:.625rem; background:#FEF2F2; color:#DC2626; border-radius:8px; font-size:.8125rem; margin-bottom:1rem; }
.setup-actions { display:flex; gap:.75rem; justify-content:center; flex-wrap:wrap; }
.btn-upload-setup { padding:.75rem 1.25rem; background:#F1F5F9; color:#475569; border:1px solid #E2E8F0; border-radius:10px; font-size:.875rem; font-weight:600; cursor:pointer; transition:all .2s; }
.btn-upload-setup:hover { background:#E2E8F0; }
.btn-upload-setup:disabled { opacity:.5; }
.btn-generate { padding:.75rem 1.5rem; background:#3B82F6; color:#fff; border:none; border-radius:10px; font-size:.875rem; font-weight:600; cursor:pointer; transition:all .2s; box-shadow:0 4px 16px rgba(59,130,246,.3); }
.btn-generate:hover { background:#2563EB; transform:translateY(-2px); }
.btn-generate:disabled { opacity:.6; cursor:default; transform:none; }

/* Content */
.fc-content { flex:1; max-width:900px; width:100%; margin:0 auto; padding:2rem 2rem 4rem; display:flex; flex-direction:column; gap:1.5rem; }
.fc-progress-section { display:flex; align-items:center; justify-content:space-between; }
.fc-prog-left { display:flex; align-items:center; gap:1rem; }
.fc-prog-icon { width:44px; height:44px; background:#EFF6FF; border-radius:12px; display:flex; align-items:center; justify-content:center; }
.fc-prog-text h2 { font-size:1.125rem; font-weight:700; color:#0F172A; margin-bottom:.25rem; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; max-width:300px; }
.fc-prog-text p { font-size:.8125rem; color:#64748B; font-weight:500; }
.fc-prog-right { display:flex; flex-direction:column; align-items:flex-end; gap:.5rem; }
.prog-label { font-size:.6875rem; font-weight:800; letter-spacing:.05em; color:#64748B; }
.prog-bar-wrap { display:flex; align-items:center; gap:.75rem; }
.prog-bar { width:140px; height:8px; background:#E2E8F0; border-radius:4px; overflow:hidden; }
.prog-fill { height:100%; background:#10B981; border-radius:4px; transition:width .4s; }
.prog-count { font-size:.875rem; color:#64748B; } .prog-count strong { color:#0F172A; }

/* Flashcard 3D */
.fc-card-container { perspective:1000px; width:100%; height:420px; margin:1rem 0; }
.fc-card { width:100%; height:100%; position:relative; transition:transform .6s cubic-bezier(.4,0,.2,1); transform-style:preserve-3d; cursor:pointer; }
.fc-card.flipped { transform:rotateY(180deg); }
.fc-face { position:absolute; width:100%; height:100%; backface-visibility:hidden; background:#FFF; border-radius:20px; box-shadow:0 10px 40px rgba(0,0,0,.04); border:1px solid #E2E8F0; display:flex; flex-direction:column; padding:3rem; overflow-y:auto; }
.fc-back { transform:rotateY(180deg); padding:2.5rem 3rem; }
.fc-front { align-items:center; justify-content:center; text-align:center; }
.badge-pill { background:#DBEAFE; color:#2563EB; font-size:.75rem; font-weight:700; padding:.375rem .875rem; border-radius:20px; letter-spacing:.05em; margin-bottom:2rem; }
.front-q { font-size:1.75rem; font-weight:800; color:#0F172A; line-height:1.3; margin-bottom:2.5rem; max-width:85%; }
.flip-action { display:flex; flex-direction:column; align-items:center; gap:.75rem; }
.btn-flip { width:48px; height:48px; background:#3B82F6; color:#fff; border:none; border-radius:12px; display:flex; align-items:center; justify-content:center; box-shadow:0 4px 12px rgba(59,130,246,.3); cursor:pointer; transition:all .2s; }
.btn-flip:hover { background:#2563EB; transform:scale(1.05); }
.flip-text { font-size:.8125rem; color:#3B82F6; font-weight:600; }
.verified-badge { position:absolute; top:1.5rem; right:1.5rem; background:#ECFDF5; color:#10B981; font-weight:700; font-size:.75rem; padding:.375rem .875rem; border-radius:6px; border:1px solid #A7F3D0; }
.back-q-section { border-bottom:1px solid #F1F5F9; padding-bottom:1.25rem; margin-bottom:1.25rem; }
.back-label { display:flex; align-items:center; gap:.5rem; font-size:.6875rem; font-weight:800; color:#64748B; letter-spacing:.06em; margin-bottom:.75rem; }
.back-label.success { color:#10B981; }
.back-q { font-size:1.125rem; font-weight:700; color:#0F172A; line-height:1.4; }
.back-a-section { flex:1; }
.back-a-text { font-size:.9375rem; color:#475569; line-height:1.7; }

/* Controls */
.fc-controls { display:flex; align-items:center; justify-content:space-between; margin-top:1rem; }
.ctrl-btn { display:flex; align-items:center; gap:.5rem; padding:.75rem 1.5rem; border-radius:10px; font-size:.875rem; font-weight:600; cursor:pointer; transition:all .2s; }
.ctrl-btn:disabled { opacity:.4; cursor:default; }
.btn-prev { background:#FFF; border:1px solid #CBD5E1; color:#475569; }
.btn-prev:hover:not(:disabled) { background:#F1F5F9; }
.btn-next { background:#0052CC; border:none; color:#fff; box-shadow:0 4px 12px rgba(0,82,204,.25); }
.btn-next:hover:not(:disabled) { background:#0043A6; transform:translateY(-1px); }
.btn-finish { background:#10B981; border:none; color:#fff; box-shadow:0 4px 12px rgba(16,185,129,.3); }
.btn-finish:hover { background:#059669; transform:translateY(-1px); }
.ctrl-counter { font-size:.875rem; font-weight:600; color:#64748B; }

@media (max-width:768px) {
  .fc-header { padding:.75rem 1rem; }
  .fc-content { padding:1.5rem 1rem; }
  .fc-progress-section { flex-direction:column; align-items:flex-start; gap:1rem; }
  .fc-prog-right { align-items:flex-start; }
  .fc-prog-text h2 { max-width:200px; }
  .front-q { font-size:1.25rem; max-width:100%; }
  .fc-face { padding:1.5rem; }
  .fc-card-container { height:380px; }
  .fc-controls { flex-wrap:wrap; gap:.75rem; justify-content:center; }
  .ctrl-btn { flex:1; min-width:120px; justify-content:center; }
  .setup-card { padding:2rem 1.5rem; }
}
</style>
