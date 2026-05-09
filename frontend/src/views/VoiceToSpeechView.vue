<script setup>
import { ref, computed, onMounted, onUnmounted, inject } from 'vue'
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

onMounted(() => {
  docStore.loadDocuments()
  speechSupported.value = 'webkitSpeechRecognition' in window || 'SpeechRecognition' in window
  if (quizStore.quizItems.length > 0 && quizStore.currentQuestion?.itemType === 'ESSAY') {
    showSetup.value = false
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
  } catch (err) {
    evalError.value = err.response?.data?.message || 'Gagal mengevaluasi jawaban.'
  } finally {
    evaluating.value = false
  }
}

function nextQuestion() {
  if (currentQIndex.value >= totalQuestions.value - 1) { finished.value = true; return }
  currentQIndex.value++
  transcript.value = ''; interimTranscript.value = ''; evaluation.value = null
}



async function startSession() {
  if (docStore.selectedDocumentIds.length === 0) { setupError.value = 'Pilih minimal satu dokumen.'; return }
  setupError.value = null
  try {
    await quizStore.createSession({ documentIds: docStore.selectedDocumentIds, personaType: personaType.value, questionCount: questionCount.value, itemType: 'ESSAY' })
    showSetup.value = false; finished.value = false; currentQIndex.value = 0; evaluations.value = []; transcript.value = ''; evaluation.value = null
  } catch (err) { setupError.value = quizStore.error || 'Gagal membuat sesi.' }
}

function handleNewSession() { quizStore.resetQuiz(); docStore.clearSelection(); showSetup.value = true; finished.value = false; currentQIndex.value = 0; evaluations.value = [] }
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
        <div class="setup-icon">🎙️</div>
        <h2>Simulasi Ujian Lisan</h2>
        <p>Jawab pertanyaan secara lisan menggunakan mikrofon. AI akan mengevaluasi jawabanmu secara real-time.</p>
        <div v-if="!speechSupported" class="setup-warning">⚠️ Browser ini tidak mendukung Speech Recognition. Gunakan Chrome/Edge.</div>
        <DocumentManager mode="chip" />
        <div class="setup-options">
          <div class="setup-opt"><label>Soal:</label><select v-model="questionCount"><option :value="2">2</option><option :value="3">3</option><option :value="5">5</option></select></div>
          <div class="setup-opt"><label>Persona:</label><select v-model="personaType"><option value="FRIENDLY_SENIOR">Kakak Senior</option><option value="STRICT_LECTURER">Dosen Tegas</option></select></div>
        </div>
        <div v-if="setupError" class="setup-error">{{ setupError }}</div>
        <div class="setup-actions">
          <button class="btn-generate" @click="startSession" :disabled="quizStore.generating || docStore.selectedDocumentIds.length === 0">{{ quizStore.generating ? '⏳ Menyiapkan soal...' : '🎤 Mulai Simulasi' }}</button>
        </div>
      </div>
    </div>

    <!-- Finished -->
    <div v-else-if="finished" class="vs-finished">
      <div class="finished-card">
        <div class="finished-emoji">🏆</div>
        <h2>Simulasi Selesai!</h2>
        <div class="eval-summary">
          <div v-for="(ev, i) in evaluations" :key="i" class="eval-item">
            <div class="eval-q">{{ i + 1 }}. {{ ev.question }}</div>
            <div class="eval-score" :style="{ color: getScoreColor(ev.score) }">{{ ev.score }}/100</div>
          </div>
        </div>
        <div class="avg-score">Rata-rata: <strong :style="{ color: getScoreColor(evaluations.reduce((s,e)=>s+e.score,0)/evaluations.length) }">{{ Math.round(evaluations.reduce((s,e)=>s+e.score,0)/evaluations.length) }}/100</strong></div>
        <button class="btn-generate" @click="handleNewSession">🔄 Sesi Baru</button>
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
        <span class="record-label">{{ isRecording ? '🔴 Merekam... klik untuk berhenti' : evaluating ? '⏳ Mengevaluasi...' : 'Klik untuk mulai bicara' }}</span>
        <div v-if="isRecording" class="wave-bars"><span></span><span></span><span></span><span></span><span></span></div>
      </div>

      <div class="vs-transcript" v-if="transcript || interimTranscript">
        <label>Transkripsi:</label>
        <p>{{ transcript }}<span class="interim">{{ interimTranscript }}</span></p>
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
        <button v-if="!evaluation && !isRecording" class="btn-submit" @click="submitAnswer" :disabled="!transcript.trim() || evaluating">{{ evaluating ? 'Mengevaluasi...' : '📤 Kirim Jawaban' }}</button>
        <button v-if="evaluation" class="btn-generate" @click="nextQuestion">{{ currentQIndex >= totalQuestions-1 ? '🏁 Selesai' : '➡️ Soal Berikutnya' }}</button>
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
.setup-icon,.finished-emoji { font-size:3rem; margin-bottom:1rem; } .setup-card h2,.finished-card h2 { font-size:1.5rem; font-weight:800; margin-bottom:.5rem; }
.setup-card > p { color:#64748B; font-size:.9375rem; margin-bottom:1.5rem; line-height:1.6; }
.setup-warning { padding:.75rem; background:#FEF3C7; color:#92400E; border-radius:8px; font-size:.8125rem; margin-bottom:1rem; }
.setup-docs { text-align:left; margin-bottom:1.25rem; } .setup-label { display:block; font-size:.8125rem; font-weight:700; color:#475569; margin-bottom:.5rem; }
.doc-list-mini { display:flex; flex-wrap:wrap; gap:.5rem; }
.doc-chip { display:inline-flex; align-items:center; gap:.375rem; padding:.5rem .75rem; background:#F8FAFC; border:1px solid #E2E8F0; border-radius:8px; font-size:.8125rem; cursor:pointer; } .doc-chip.selected { border-color:#3B82F6; background:#EFF6FF; } .doc-chip input { display:none; }
.setup-options { display:flex; gap:1rem; justify-content:center; margin-bottom:1.25rem; flex-wrap:wrap; }
.setup-opt { display:flex; align-items:center; gap:.5rem; } .setup-opt label { font-size:.875rem; font-weight:600; color:#475569; }
.setup-opt select { padding:.5rem .75rem; border:1px solid #E2E8F0; border-radius:8px; font-size:.875rem; background:#fff; }
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
.vs-transcript label { display:block; font-size:.6875rem; font-weight:700; color:#94A3B8; letter-spacing:.06em; margin-bottom:.5rem; }
.vs-transcript p { font-size:.9375rem; color:#1E293B; line-height:1.6; } .interim { color:#94A3B8; font-style:italic; }
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
}
</style>
