<script setup>
import { ref, computed, onMounted, onUnmounted, inject, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useQuizStore } from '@/stores/quiz'
import { useDocumentStore } from '@/stores/document'
import simulatorApi from '@/services/simulator.api'
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

// Setup
const showSetup = ref(true)
const questionCount = ref(3)
const personaType = ref('FRIENDLY_SENIOR')
const setupError = ref(null)

// Speech recognition
const isRecording = ref(false)
const transcript = ref('')
const interimTranscript = ref('')
let recognition = null
const speechSupported = ref(false)

// Evaluation
const evaluating = ref(false)
const evaluation = ref(null)
const evalError = ref(null)
const currentQIndex = ref(0)
const finished = ref(false)
const evaluations = ref([])

const currentQuestion = computed(() => quizStore.quizItems[currentQIndex.value])
const totalQuestions = computed(() => quizStore.quizItems.length)

function saveProgress() {
  if (!quizStore.currentSession?.id) return
  const progressData = {
    currentQIndex: currentQIndex.value,
    evaluations: evaluations.value,
    transcript: transcript.value,
    evaluation: evaluation.value,
    finished: finished.value
  }
  localStorage.setItem(`vts_progress_${quizStore.currentSession.id}`, JSON.stringify(progressData))
}

function loadProgress() {
  if (!quizStore.currentSession?.id) return
  const saved = localStorage.getItem(`vts_progress_${quizStore.currentSession.id}`)
  if (saved) {
    try {
      const parsed = JSON.parse(saved)
      currentQIndex.value = parsed.currentQIndex || 0
      evaluations.value = parsed.evaluations || []
      transcript.value = parsed.transcript || ''
      evaluation.value = parsed.evaluation || null
      finished.value = parsed.finished || false
      evalError.value = null
    } catch (e) {
      resetLocalState()
    }
  } else {
    resetLocalState()
  }
}

function resetLocalState() {
  currentQIndex.value = 0
  evaluations.value = []
  transcript.value = ''
  evaluation.value = null
  finished.value = false
  evalError.value = null
}

onMounted(() => {
  docStore.loadDocuments()
  speechSupported.value = 'webkitSpeechRecognition' in window || 'SpeechRecognition' in window
  if (quizStore.quizItems.length > 0 && quizStore.currentQuestion?.itemType === 'ESSAY') {
    showSetup.value = false
    loadProgress()
  }
})

watch(() => quizStore.currentSession, (newSession) => {
  if (newSession && quizStore.quizItems.length > 0 && quizStore.currentQuestion?.itemType === 'ESSAY') {
    showSetup.value = false
    loadProgress()
  } else {
    showSetup.value = true
  }
})

function initSpeechRecognition() {
  const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition
  if (!SpeechRecognition) return
  recognition = new SpeechRecognition()
  recognition.lang = 'id-ID'
  recognition.interimResults = true
  recognition.continuous = true
  recognition.maxAlternatives = 1

  recognition.onresult = (event) => {
    let final = '', interim = ''
    for (let i = 0; i < event.results.length; i++) {
      if (event.results[i].isFinal) final += event.results[i][0].transcript + ' '
      else interim = event.results[i][0].transcript
    }
    transcript.value = final.trim()
    interimTranscript.value = interim
  }
  recognition.onerror = (e) => { if (e.error !== 'no-speech') evalError.value = 'Speech error: ' + e.error; stopRecording() }
  recognition.onend = () => { if (isRecording.value) { try { recognition.start() } catch {} } }
}

function startRecording() {
  if (!speechSupported.value) { evalError.value = 'Browser tidak mendukung Speech Recognition'; return }
  transcript.value = ''
  interimTranscript.value = ''
  evaluation.value = null
  evalError.value = null
  initSpeechRecognition()
  try { recognition.start(); isRecording.value = true } catch (e) { evalError.value = 'Gagal memulai recording' }
}

function stopRecording() {
  isRecording.value = false
  if (recognition) { try { recognition.stop() } catch {} }
}

async function submitAnswer() {
  if (!transcript.value.trim()) { evalError.value = 'Rekam jawaban kamu terlebih dahulu.'; return }
  stopRecording()
  evaluating.value = true
  evalError.value = null
  try {
    const resp = await simulatorApi.evaluateTextAnswer({
      sessionId: quizStore.currentSession.id,
      quizItemId: currentQuestion.value.id,
      transcript: transcript.value
    })
    evaluation.value = resp.data.data
    evaluations.value.push({ question: currentQuestion.value.questionText, ...resp.data.data })
    saveProgress()
  } catch (err) {
    evalError.value = err.response?.data?.message || 'Gagal mengevaluasi jawaban.'
  } finally {
    evaluating.value = false
  }
}

function nextQuestion() {
  if (currentQIndex.value >= totalQuestions.value - 1) { finished.value = true; saveProgress(); return }
  currentQIndex.value++
  transcript.value = ''; interimTranscript.value = ''; evaluation.value = null
  saveProgress()
}



async function startSession() {
  if (docStore.selectedDocumentIds.length === 0) { setupError.value = 'Pilih minimal satu dokumen.'; return }
  setupError.value = null
  try {
    await quizStore.createSession({ documentIds: docStore.selectedDocumentIds, personaType: personaType.value, questionCount: questionCount.value, itemType: 'ESSAY' })
    showSetup.value = false; resetLocalState(); saveProgress()
  } catch (err) { setupError.value = quizStore.error || 'Gagal membuat sesi.' }
}

function handleNewSession() { quizStore.resetQuiz(); docStore.clearSelection(); showSetup.value = true; resetLocalState() }
function getScoreColor(s) { if (s >= 80) return '#10B981'; if (s >= 60) return '#F59E0B'; return '#EF4444' }

onUnmounted(() => { stopRecording() })
</script>

<template>
  <div class="vs-page" @click="closeUserMenu">
    <header class="vs-header">
      <div class="vs-header-left">
        <button class="burger-btn" @click.stop="toggleSidebar" aria-label="Menu">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="18" x2="21" y2="18"/></svg>
        </button>
        <button class="btn-new-session" @click="handleNewSession">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/></svg>
          Sesi Baru
        </button>
      </div>
      <div class="vs-user-area" @click.stop>
        <button class="vs-avatar-btn" @click="toggleUserMenu">
          <div v-if="avatarUrl" class="vs-avatar"><img :src="avatarUrl" :alt="displayName" referrerpolicy="no-referrer"/></div>
          <div v-else class="vs-avatar vs-avatar-initials">{{ initials }}</div>
        </button>
        <div v-if="showUserMenu" class="vs-user-dropdown">
          <div class="dd-info"><span class="dd-name">{{ displayName }}</span><span class="dd-email">{{ authStore.userEmail }}</span></div>
          <div class="dd-div"></div>
          <button class="dd-item" @click="handleLogout">Logout</button>
        </div>
      </div>
    </header>

    <!-- Setup -->
    <div v-if="showSetup" class="vs-setup">
      <div class="setup-card">
        <div class="setup-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 2a3 3 0 0 0-3 3v7a3 3 0 0 0 6 0V5a3 3 0 0 0-3-3Z"/>
            <path d="M19 10v2a7 7 0 0 1-14 0v-2"/>
            <line x1="12" x2="12" y1="19" y2="22"/>
          </svg>
        </div>
        <h2>Simulasi Ujian Lisan</h2>
        <p>Jawab pertanyaan secara lisan menggunakan mikrofon. AI akan mengevaluasi jawabanmu secara real-time.</p>
        <div v-if="!speechSupported" class="setup-warning">
          <svg class="warning-icon-svg" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"/>
            <line x1="12" y1="9" x2="12" y2="13"/>
            <line x1="12" y1="17" x2="12.01" y2="17"/>
          </svg>
          <span>Browser ini tidak mendukung Speech Recognition. Gunakan Chrome/Edge.</span>
        </div>
        <DocumentManager mode="chip" />
        <div class="setup-options">
          <div class="setup-opt">
            <label>Jumlah Soal:</label>
            <select v-model="questionCount">
              <option :value="2">2</option>
              <option :value="3">3</option>
              <option :value="5">5</option>
            </select>
          </div>
          <div class="setup-opt">
            <label>Persona AI:</label>
            <select v-model="personaType">
              <option value="FRIENDLY_SENIOR">Kakak Senior</option>
              <option value="STRICT_LECTURER">Dosen Tegas</option>
            </select>
          </div>
        </div>
        <div v-if="setupError" class="setup-error">{{ setupError }}</div>
        <div class="setup-actions">
          <button class="btn-generate" @click="startSession" :disabled="quizStore.generating || docStore.selectedDocumentIds.length === 0">
            <span class="btn-icon-content" v-if="quizStore.generating">
              <svg class="animate-spin" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              <span>Menyiapkan soal...</span>
            </span>
            <span class="btn-icon-content" v-else>
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M12 2a3 3 0 0 0-3 3v7a3 3 0 0 0 6 0V5a3 3 0 0 0-3-3Z"/>
                <path d="M19 10v2a7 7 0 0 1-14 0v-2"/>
                <line x1="12" x2="12" y1="19" y2="22"/>
              </svg>
              <span>Mulai Simulasi</span>
            </span>
          </button>
        </div>
      </div>
    </div>

    <!-- Finished -->
    <div v-else-if="finished" class="vs-finished">
      <div class="finished-card">
        <div class="finished-emoji">
          <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M6 9H4.5a2.5 2.5 0 0 1 0-5H6"/>
            <path d="M18 9h1.5a2.5 2.5 0 0 0 0-5H18"/>
            <path d="M4 22h16"/>
            <path d="M10 14.66V17c0 .55-.45 1-1 1H4v2h16v-2h-5c-.55 0-1-.45-1-1v-2.34"/>
            <path d="M12 2a5 5 0 0 1 5 5v5a5 5 0 0 1-10 0V7a5 5 0 0 1 5-5z"/>
          </svg>
        </div>
        <h2>Simulasi Selesai!</h2>
        <div class="eval-summary">
          <div v-for="(ev, i) in evaluations" :key="i" class="eval-item">
            <div class="eval-q">{{ i + 1 }}. {{ ev.question }}</div>
            <div class="eval-score" :style="{ color: getScoreColor(ev.score) }">{{ ev.score }}/100</div>
          </div>
        </div>
        <div class="avg-score">Rata-rata: <strong :style="{ color: getScoreColor(evaluations.reduce((s,e)=>s+e.score,0)/evaluations.length) }">{{ Math.round(evaluations.reduce((s,e)=>s+e.score,0)/evaluations.length) }}/100</strong></div>
        <button class="btn-generate" @click="handleNewSession">
          <span class="btn-icon-content">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21.5 2v6h-6M21.34 15.57a10 10 0 1 1-.57-8.38l5.67-5.67"/>
            </svg>
            <span>Sesi Baru</span>
          </span>
        </button>
      </div>
    </div>

    <!-- Active Session -->
    <div v-else class="vs-content">
      <div class="vs-progress"><span>Soal {{ currentQIndex + 1 }} / {{ totalQuestions }}</span><div class="vs-prog-bar"><div class="vs-prog-fill" :style="{ width: ((currentQIndex+1)/totalQuestions*100)+'%' }"></div></div></div>

      <div class="vs-question-card" v-if="currentQuestion">
        <div class="q-badge">PERTANYAAN {{ currentQIndex + 1 }}</div>
        <h2>{{ currentQuestion.questionText }}</h2>
      </div>

      <div class="vs-record-section">
        <button class="record-btn" :class="{ recording: isRecording }" @click="isRecording ? stopRecording() : startRecording()" :disabled="evaluating">
          <svg v-if="!isRecording" xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3z"/><path d="M19 10v2a7 7 0 0 1-14 0v-2"/><line x1="12" y1="19" x2="12" y2="23"/><line x1="8" y1="23" x2="16" y2="23"/></svg>
          <svg v-else xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="currentColor"><rect x="6" y="6" width="12" height="12" rx="2"/></svg>
        </button>
        <span class="record-label">
          <span v-if="isRecording" class="recording-status">
            <span class="pulse-dot"></span>
            <span>Merekam... klik untuk berhenti</span>
          </span>
          <span v-else-if="evaluating" class="evaluating-status">
            <svg class="animate-spin" xmlns="http://www.w3.org/2000/svg" width="14" height="14" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            <span>Mengevaluasi...</span>
          </span>
          <span v-else>Klik untuk mulai bicara</span>
        </span>
        <div v-if="isRecording" class="wave-bars"><span></span><span></span><span></span><span></span><span></span></div>
      </div>

      <div class="vs-transcript">
        <label>Jawaban Kamu: <span v-if="!isRecording && !evaluation" class="edit-hint">(Bisa diketik/diedit secara manual)</span></label>
        <p v-if="isRecording || evaluation">{{ transcript || '(Belum ada rekaman suara...)' }}<span class="interim" v-if="isRecording">{{ interimTranscript }}</span></p>
        <textarea v-else v-model="transcript" class="transcript-editor" placeholder="Ketik jawaban kamu di sini atau klik tombol mikrofon di atas untuk berbicara..." rows="4"></textarea>
      </div>

      <div v-if="evalError" class="vs-error">{{ evalError }}</div>

      <div v-if="evaluation" class="vs-evaluation">
        <div class="eval-header">
          <div class="eval-score-big" :style="{ color: getScoreColor(evaluation.score) }">{{ evaluation.score }}<span>/100</span></div>
          <span class="eval-badge" :style="{ background: getScoreColor(evaluation.score)+'20', color: getScoreColor(evaluation.score) }">{{ evaluation.score >= 80 ? 'Sangat Baik' : evaluation.score >= 60 ? 'Cukup' : 'Perlu Perbaikan' }}</span>
        </div>
        <div class="eval-feedback"><label>Feedback AI:</label><p>{{ evaluation.feedback }}</p></div>
      </div>

      <div class="vs-actions">
        <button v-if="!evaluation && !isRecording" class="btn-submit" @click="submitAnswer" :disabled="!transcript.trim() || evaluating">
          <span class="btn-icon-content" v-if="evaluating">
            <svg class="animate-spin" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            <span>Mengevaluasi...</span>
          </span>
          <span class="btn-icon-content" v-else>
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/>
            </svg>
            <span>Kirim Jawaban</span>
          </span>
        </button>
        <button v-if="evaluation" class="btn-generate" @click="nextQuestion">
          <span class="btn-icon-content" v-if="currentQIndex >= totalQuestions-1">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z"/><line x1="4" y1="22" x2="4" y2="15"/>
            </svg>
            <span>Selesai</span>
          </span>
          <span class="btn-icon-content" v-else>
            <span>Soal Berikutnya</span>
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M5 12h14"/><path d="m12 5 7 7-7 7"/>
            </svg>
          </span>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.vs-page { min-height:100vh; background:#F8FAFC; color:#1E293B; display:flex; flex-direction:column; }
.vs-header { display:flex; align-items:center; justify-content:space-between; padding:.75rem 2rem; background:#FFF; border-bottom:1px solid #E2E8F0; }
.vs-header-left { display:flex; align-items:center; gap:.75rem; }
.btn-new-session { display:inline-flex; align-items:center; gap:.5rem; background:#10B981; color:#FFF; padding:.625rem 1.25rem; border-radius:10px; font-size:.875rem; font-weight:600; border:none; cursor:pointer; transition:all .2s; } .btn-new-session:hover { background:#059669; }
.vs-user-area { position:relative; } .vs-avatar-btn { display:flex; align-items:center; gap:.5rem; background:none; border:none; cursor:pointer; padding:4px; border-radius:10px; } .vs-avatar-btn:hover { background:#F1F5F9; }
.vs-avatar { width:36px; height:36px; border-radius:10px; overflow:hidden; border:2px solid #E2E8F0; } .vs-avatar img { width:100%; height:100%; object-fit:cover; }
.vs-avatar-initials { display:flex; align-items:center; justify-content:center; background:linear-gradient(135deg,#3B82F6,#8B5CF6); color:#fff; font-size:.75rem; font-weight:700; }
.vs-user-dropdown { position:absolute; right:0; top:calc(100%+8px); width:220px; background:#fff; border:1px solid #E2E8F0; border-radius:12px; box-shadow:0 10px 40px rgba(0,0,0,.1); padding:.5rem; z-index:60; }
.dd-info { padding:.625rem .75rem; } .dd-name { display:block; font-size:.875rem; font-weight:600; } .dd-email { display:block; font-size:.75rem; color:#94A3B8; margin-top:2px; } .dd-div { height:1px; background:#F1F5F9; margin:.25rem 0; }
.dd-item { display:flex; width:100%; padding:.5rem .75rem; background:none; border:none; border-radius:8px; font-size:.8125rem; color:#EF4444; cursor:pointer; } .dd-item:hover { background:#FEF2F2; }

/* Shared setup styles */
.vs-setup,.vs-finished { flex:1; display:flex; align-items:center; justify-content:center; padding:2rem; }
.setup-card,.finished-card { background:#fff; border:1px solid #E2E8F0; border-radius:20px; padding:3rem; max-width:520px; width:100%; text-align:center; box-shadow:0 10px 40px rgba(0,0,0,.04); }
.setup-icon,.finished-emoji { font-size:3rem; margin-bottom:1rem; display: flex; justify-content: center; align-items: center; color: #3B82F6; } .setup-card h2,.finished-card h2 { font-size:1.5rem; font-weight:800; margin-bottom:.5rem; }

.warning-icon-svg { flex-shrink: 0; color: #D97706; }
.setup-warning { display: flex; align-items: center; gap: 0.5rem; text-align: left; }
.btn-icon-content { display: inline-flex; align-items: center; justify-content: center; gap: 0.5rem; }
.recording-status, .evaluating-status { display: inline-flex; align-items: center; gap: 0.5rem; }
.pulse-dot { width: 8px; height: 8px; background-color: #EF4444; border-radius: 50%; display: inline-block; animation: pulse-dot-anim 1.5s infinite; }
@keyframes pulse-dot-anim { 0% { transform: scale(0.9); opacity: 0.6; } 50% { transform: scale(1.2); opacity: 1; } 100% { transform: scale(0.9); opacity: 0.6; } }
.animate-spin { animation: spin 1s linear infinite; }
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }

.setup-card > p { color:#64748B; font-size:.9375rem; margin-bottom:1.5rem; line-height:1.6; }
.setup-warning { padding:.75rem; background:#FEF3C7; color:#92400E; border-radius:8px; font-size:.8125rem; margin-bottom:1rem; }
.setup-docs { text-align:left; margin-bottom:1.25rem; } .setup-label { display:block; font-size:.8125rem; font-weight:700; color:#475569; margin-bottom:.5rem; }
.doc-list-mini { display:flex; flex-wrap:wrap; gap:.5rem; }
.doc-chip { display:inline-flex; align-items:center; gap:.375rem; padding:.5rem .75rem; background:#F8FAFC; border:1px solid #E2E8F0; border-radius:8px; font-size:.8125rem; cursor:pointer; } .doc-chip.selected { border-color:#3B82F6; background:#EFF6FF; } .doc-chip input { display:none; }
.setup-options { display:grid; grid-template-columns:1fr 1fr; gap:1.25rem; margin-top:1.75rem; margin-bottom:2rem; text-align:left; width:100%; }
.setup-opt { display:flex; flex-direction:column; align-items:stretch; gap:0.5rem; }
.setup-opt label { font-size:0.8125rem; font-weight:700; color:#475569; text-transform:uppercase; letter-spacing:0.05em; line-height: 1.2; }
.setup-opt select { padding:0.75rem 1rem; border:1.5px solid #E2E8F0; border-radius:12px; font-size:0.9375rem; font-weight:500; color:#1E293B; background-color:#FFF; transition:border-color 0.2s, box-shadow 0.2s; }
.setup-opt select { padding-right:2.25rem; background-image:url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 20 20'%3e%3cpath stroke='%236b7280' stroke-linecap='round' stroke-linejoin='round' stroke-width='1.5' d='M6 8l4 4 4-4'/%3e%3c/svg%3e"); background-position:right 0.75rem center; background-size:1.25rem; background-repeat:no-repeat; appearance:none; cursor:pointer; }
.setup-opt select:hover { border-color:#CBD5E1; }
.setup-opt select:focus { outline:none; border-color:#3B82F6; box-shadow:0 0 0 3px rgba(59, 130, 246, 0.15); }
.setup-error { padding:.625rem; background:#FEF2F2; color:#DC2626; border-radius:8px; font-size:.8125rem; margin-bottom:1rem; }
.setup-actions { display:flex; gap:.75rem; justify-content:center; flex-wrap:wrap; }
.btn-upload-setup { padding:.75rem 1.25rem; background:#F1F5F9; color:#475569; border:1px solid #E2E8F0; border-radius:10px; font-size:.875rem; font-weight:600; cursor:pointer; } .btn-upload-setup:disabled { opacity:.5; }
.btn-generate { padding:.75rem 1.5rem; background:#3B82F6; color:#fff; border:none; border-radius:10px; font-size:.875rem; font-weight:600; cursor:pointer; box-shadow:0 4px 16px rgba(59,130,246,.3); } .btn-generate:disabled { opacity:.6; cursor:default; }

/* Content */
.vs-content { flex:1; max-width:700px; width:100%; margin:0 auto; padding:2rem 2rem 4rem; display:flex; flex-direction:column; gap:1.5rem; }
.vs-progress { display:flex; align-items:center; gap:1rem; } .vs-progress span { font-size:.875rem; font-weight:600; color:#475569; white-space:nowrap; }
.vs-prog-bar { flex:1; height:8px; background:#E2E8F0; border-radius:4px; overflow:hidden; } .vs-prog-fill { height:100%; background:#10B981; border-radius:4px; transition:width .4s; }
.vs-question-card { background:#fff; border:1px solid #E2E8F0; border-radius:16px; padding:2rem; text-align:center; box-shadow:0 4px 16px rgba(0,0,0,.03); }
.q-badge { display:inline-block; background:#DBEAFE; color:#2563EB; font-size:.6875rem; font-weight:700; padding:.25rem .75rem; border-radius:20px; letter-spacing:.05em; margin-bottom:1rem; }
.vs-question-card h2 { font-size:1.25rem; font-weight:700; color:#0F172A; line-height:1.5; }

/* Record */
.vs-record-section { display:flex; flex-direction:column; align-items:center; gap:1rem; padding:1.5rem; }
.record-btn { width:80px; height:80px; border-radius:50%; border:none; background:#3B82F6; color:#fff; display:flex; align-items:center; justify-content:center; cursor:pointer; transition:all .3s; box-shadow:0 8px 24px rgba(59,130,246,.3); }
.record-btn:hover { transform:scale(1.05); } .record-btn:disabled { opacity:.5; cursor:default; }
.record-btn.recording { background:#EF4444; animation:pulse-rec 1.5s infinite; box-shadow:0 8px 24px rgba(239,68,68,.3); }
@keyframes pulse-rec { 0%,100%{box-shadow:0 0 0 0 rgba(239,68,68,.4)} 50%{box-shadow:0 0 0 16px rgba(239,68,68,0)} }
.record-label { font-size:.875rem; color:#64748B; font-weight:500; }
.wave-bars { display:flex; gap:4px; align-items:center; height:24px; }
.wave-bars span { width:4px; background:#3B82F6; border-radius:2px; animation:wave .8s ease-in-out infinite; }
.wave-bars span:nth-child(1){height:8px;animation-delay:0s} .wave-bars span:nth-child(2){height:16px;animation-delay:.1s} .wave-bars span:nth-child(3){height:24px;animation-delay:.2s} .wave-bars span:nth-child(4){height:16px;animation-delay:.3s} .wave-bars span:nth-child(5){height:8px;animation-delay:.4s}
@keyframes wave { 0%,100%{transform:scaleY(.5)} 50%{transform:scaleY(1)} }

/* Transcript */
.vs-transcript { background:#fff; border:1px solid #E2E8F0; border-radius:12px; padding:1.25rem; }
.vs-transcript label { display:flex; align-items:center; font-size:.6875rem; font-weight:700; color:#94A3B8; letter-spacing:.06em; margin-bottom:.5rem; }
.vs-transcript p { font-size:.9375rem; color:#1E293B; line-height:1.6; margin:0; } .interim { color:#94A3B8; font-style:italic; margin-left:4px; }
.transcript-editor { width:100%; border:1px solid #E2E8F0; border-radius:8px; padding:.75rem; font-size:.9375rem; color:#1E293B; line-height:1.6; font-family:inherit; resize:vertical; background:#F8FAFC; outline:none; transition:border-color .2s; }
.transcript-editor:focus { border-color:#3B82F6; background:#FFF; }
.edit-hint { font-size:.6875rem; color:#3B82F6; font-weight:500; font-style:italic; margin-left:.5rem; letter-spacing:normal; text-transform:none; }
.vs-error { padding:.625rem; background:#FEF2F2; color:#DC2626; border-radius:8px; font-size:.8125rem; text-align:center; }

/* Evaluation */
.vs-evaluation { background:#fff; border:1px solid #E2E8F0; border-radius:16px; padding:1.5rem; }
.eval-header { display:flex; align-items:center; gap:1rem; margin-bottom:1rem; }
.eval-score-big { font-size:2.5rem; font-weight:900; } .eval-score-big span { font-size:1rem; font-weight:600; color:#94A3B8; }
.eval-badge { font-size:.75rem; font-weight:700; padding:.375rem .75rem; border-radius:20px; }
.eval-feedback label { display:block; font-size:.6875rem; font-weight:700; color:#94A3B8; letter-spacing:.06em; margin-bottom:.5rem; }
.eval-feedback p { font-size:.9375rem; color:#475569; line-height:1.7; }

/* Actions */
.vs-actions { display:flex; justify-content:center; gap:1rem; }
.btn-submit { padding:.75rem 1.5rem; background:#10B981; color:#fff; border:none; border-radius:10px; font-size:.875rem; font-weight:600; cursor:pointer; box-shadow:0 4px 16px rgba(16,185,129,.3); } .btn-submit:disabled { opacity:.5; cursor:default; }

/* Finished */
.eval-summary { text-align:left; margin:1.5rem 0; }
.eval-item { display:flex; align-items:center; justify-content:space-between; padding:.75rem; border-bottom:1px solid #F1F5F9; gap:.75rem; }
.eval-q { flex:1; font-size:.8125rem; color:#475569; line-height:1.4; } .eval-score { font-size:1rem; font-weight:800; flex-shrink:0; }
.avg-score { font-size:1.125rem; color:#475569; margin-bottom:1.5rem; } .avg-score strong { font-size:1.5rem; }

@media (max-width:768px) {
  .vs-header { padding:.75rem 1rem; }
  .vs-content { padding:1.5rem 1rem; }
  .record-btn { width:72px; height:72px; }
  .setup-card,.finished-card { padding:2rem 1.5rem; }
  .eval-header { flex-wrap:wrap; }
  .setup-opt label { min-height: 2.25rem; display: flex; align-items: flex-end; }
}
</style>
