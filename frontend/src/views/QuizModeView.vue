<script setup>
import { ref, computed, onMounted, onUnmounted, inject, watch } from 'vue'
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

// Setup
const showSetup = ref(true)
const questionCount = ref(5)
const personaType = ref('FRIENDLY_SENIOR')
const timerLimit = ref(15)
const setupError = ref(null)

watch(questionCount, (newVal) => {
  if (newVal !== null && newVal !== '') {
    const num = Number(newVal)
    if (num > 10) {
      questionCount.value = 10
    }
  }
})

// Quiz state
const timer = ref(15)
let timerInterval = null
const timerProgress = computed(() => {
  if (timerLimit.value === 0) return 0
  return (timer.value / timerLimit.value) * 100
})
const selectedAnswer = ref(null)
const showResult = ref(false)
const isCorrect = ref(false)
const quizFinished = ref(false)

const currentQuestion = computed(() => quizStore.currentQuestion)
const totalQuestions = computed(() => quizStore.totalQuestions)
const questionNumber = computed(() => quizStore.currentQuestionIndex + 1)
const score = computed(() => quizStore.score)
const formattedScore = computed(() => quizStore.formattedScore)
const progressPercent = computed(() => quizStore.progressPercent)

const quizReviewList = computed(() => {
  return quizStore.quizItems.map((q, index) => {
    const selectedIndex = quizStore.answers[q.id]
    const hasAnswered = selectedIndex !== undefined && selectedIndex !== -1
    const selectedText = hasAnswered ? q.options[selectedIndex] : null
    const isCorrect = hasAnswered && selectedText === q.correctAnswer
    const isSkipped = selectedIndex === -1 || selectedIndex === undefined
    
    return {
      number: index + 1,
      questionText: q.questionText,
      options: q.options,
      correctAnswer: q.correctAnswer,
      selectedText,
      isCorrect,
      isSkipped
    }
  })
})

const correctCount = computed(() => {
  return quizReviewList.value.filter(item => item.isCorrect).length
})
const skippedCount = computed(() => {
  return quizReviewList.value.filter(item => item.isSkipped).length
})
const wrongCount = computed(() => {
  return quizReviewList.value.filter(item => !item.isCorrect && !item.isSkipped).length
})
const accuracyPercent = computed(() => {
  return totalQuestions.value > 0 ? Math.round((correctCount.value / totalQuestions.value) * 100) : 0
})
const normalizedScore = computed(() => {
  return totalQuestions.value > 0 ? Math.round((correctCount.value / totalQuestions.value) * 100) : 0
})

// Quiz options with shapes/colors
const optionMeta = [
  { shape: 'triangle', color: '#E53935' },
  { shape: 'square', color: '#1E88E5' },
  { shape: 'circle', color: '#F9A825' },
  { shape: 'star', color: '#43A047' }
]

function startTimer() {
  clearInterval(timerInterval)
  if (timerLimit.value === 0) return
  timer.value = timerLimit.value
  timerInterval = setInterval(() => {
    if (timer.value > 0) timer.value--
    else { clearInterval(timerInterval); if (selectedAnswer.value === null) handleTimeUp() }
  }, 1000)
}

function handleTimeUp() {
  selectedAnswer.value = -1
  showResult.value = true
  isCorrect.value = false
  if (currentQuestion.value) {
    quizStore.selectAnswer(-1)
  }
}

function loadCurrentQuestionState() {
  if (!quizStore.currentQuestion) return

  const answeredIndex = quizStore.answers[quizStore.currentQuestion.id]
  if (answeredIndex !== undefined) {
    selectedAnswer.value = answeredIndex
    showResult.value = true
    
    // Evaluate if correct
    const q = quizStore.currentQuestion
    if (q && q.options && q.correctAnswer) {
      if (answeredIndex === -1) {
        isCorrect.value = false
      } else {
        isCorrect.value = q.options[answeredIndex] === q.correctAnswer
      }
    } else {
      isCorrect.value = false
    }
    clearInterval(timerInterval)
  } else {
    selectedAnswer.value = null
    showResult.value = false
    isCorrect.value = false
    startTimer()
  }
}

function selectAnswer(index) {
  if (selectedAnswer.value !== null) return
  clearInterval(timerInterval)
  selectedAnswer.value = index
  showResult.value = true
  const q = currentQuestion.value
  if (q && q.options && q.correctAnswer) {
    isCorrect.value = q.options[index] === q.correctAnswer
    quizStore.selectAnswer(index)
  }
}

function nextQuestion() {
  if (quizStore.isLastQuestion) {
    quizFinished.value = true
    return
  }
  quizStore.nextQuestion()
  loadCurrentQuestionState()
}

function skipQuestion() {
  if (selectedAnswer.value !== null) return
  clearInterval(timerInterval)
  selectedAnswer.value = -1
  showResult.value = true
  isCorrect.value = false
  if (currentQuestion.value) {
    quizStore.selectAnswer(-1)
  }
}

function getOptionClass(index) {
  if (!showResult.value) return ''
  const q = currentQuestion.value
  if (q && q.options[index] === q.correctAnswer) return 'correct'
  if (index === selectedAnswer.value && !isCorrect.value) return 'wrong'
  return 'dimmed'
}

function getCorrectIndex() {
  const q = currentQuestion.value
  if (!q) return -1
  return q.options?.findIndex(o => o === q.correctAnswer) ?? -1
}



async function startQuiz() {
  if (docStore.selectedDocumentIds.length === 0) { setupError.value = 'Pilih minimal satu dokumen.'; return }
  const count = Number(questionCount.value)
  if (!count || count < 1 || count > 10) {
    setupError.value = 'Jumlah soal harus di antara 1 dan 10.'
    return
  }
  setupError.value = null
  try {
    await quizStore.createSession({ documentIds: docStore.selectedDocumentIds, personaType: personaType.value, questionCount: questionCount.value, itemType: 'MULTIPLE_CHOICE' })
    showSetup.value = false
    quizFinished.value = false
    loadCurrentQuestionState()
  } catch (err) { setupError.value = quizStore.error || 'Gagal membuat quiz.' }
}

function handleNewQuiz() {
  clearInterval(timerInterval)
  quizStore.resetQuiz()
  docStore.clearSelection()
  showSetup.value = true
  quizFinished.value = false
  selectedAnswer.value = null
  showResult.value = false
}

onMounted(() => {
  docStore.loadDocuments()
  if (quizStore.quizItems.length > 0 && quizStore.currentQuestion?.itemType === 'MULTIPLE_CHOICE') {
    showSetup.value = false; loadCurrentQuestionState()
  }
})

watch(() => quizStore.currentSession, (newSession) => {
  if (newSession && quizStore.quizItems.length > 0 && quizStore.currentQuestion?.itemType === 'MULTIPLE_CHOICE') {
    showSetup.value = false
    quizFinished.value = false
    loadCurrentQuestionState()
  } else {
    showSetup.value = true
    clearInterval(timerInterval)
  }
})
onUnmounted(() => clearInterval(timerInterval))
</script>

<template>
  <div class="qz-page" @click="closeUserMenu">
    <header class="qz-header">
      <div class="qz-header-left">
        <button class="burger-btn" @click.stop="toggleSidebar" aria-label="Menu">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="18" x2="21" y2="18"/></svg>
        </button>
        <button class="btn-new-quiz" @click="handleNewQuiz">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/></svg>
          Quiz Baru
        </button>
      </div>
      <div class="qz-user-area" @click.stop>
        <button class="qz-avatar-btn" @click="toggleUserMenu">
          <div v-if="avatarUrl" class="qz-avatar"><img :src="avatarUrl" :alt="displayName" referrerpolicy="no-referrer"/></div>
          <div v-else class="qz-avatar qz-avatar-initials">{{ initials }}</div>
        </button>
        <div v-if="showUserMenu" class="qz-user-dropdown">
          <div class="dd-info"><span class="dd-name">{{ displayName }}</span><span class="dd-email">{{ authStore.userEmail }}</span></div>
          <div class="dd-div"></div>
          <button class="dd-item" @click="handleLogout">Logout</button>
        </div>
      </div>
    </header>

    <!-- Setup -->
    <div v-if="showSetup" class="qz-setup">
      <div class="setup-card">
        <div class="setup-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/>
          </svg>
        </div>
        <h2>Buat Adaptive Quiz</h2>
        <p>Upload dokumen PDF dan AI akan membuat soal pilihan ganda.</p>
        <DocumentManager mode="chip" />
        <div class="setup-options">
          <div class="setup-opt"><label>Jumlah Soal:</label><input type="number" v-model.number="questionCount" min="1" max="10" placeholder="Maks 10" /></div>
          <div class="setup-opt">
            <label>Tingkat Kesulitan:</label>
            <select v-model="personaType">
              <option value="FRIENDLY_SENIOR">Mudah (Easy)</option>
              <option value="STRICT_LECTURER">Sulit (Hard)</option>
            </select>
          </div>
          <div class="setup-opt" style="grid-column: span 2;">
            <label>Durasi per Soal:</label>
            <select v-model="timerLimit">
              <option :value="10">10 Detik</option>
              <option :value="15">15 Detik</option>
              <option :value="30">30 Detik</option>
              <option :value="60">60 Detik</option>
              <option :value="0">Latihan (Tanpa Timer)</option>
            </select>
          </div>
        </div>
        <div v-if="setupError" class="setup-error">{{ setupError }}</div>
        <div class="setup-actions">
          <button class="btn-generate" @click="startQuiz" :disabled="quizStore.generating || docStore.selectedDocumentIds.length === 0">
            <span class="btn-icon-content" v-if="quizStore.generating">
              <svg class="animate-spin" xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              <span>AI sedang membuat soal...</span>
            </span>
            <span class="btn-icon-content" v-else>
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="m12 3-1.912 5.813a2 2 0 0 1-1.275 1.275L3 12l5.813 1.912a2 2 0 0 1 1.275 1.275L12 21l1.912-5.813a2 2 0 0 1 1.275-1.275L21 12l-5.813-1.912a2 2 0 0 1-1.275-1.275L12 3Z"/>
              </svg>
              <span>Generate Quiz</span>
            </span>
          </button>
        </div>
      </div>
    </div>

    <!-- Quiz Finished -->
    <div v-else-if="quizFinished" class="qz-finished">
      <div class="finished-card">
        <div class="finished-emoji">
          <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M6 9H4.5a2.5 2.5 0 0 1 0-5H6"/>
            <path d="M18 9h1.5a2.5 2.5 0 0 0 0-5H18"/>
            <path d="M4 22h16"/>
            <path d="M10 14.66V17c0 .55-.45 1-1 1H4v2h16v-2h-5c-.55 0-1-.45-1-1v-2.34"/>
            <path d="M12 2a5 5 0 0 1 5 5v5a5 5 0 0 1-10 0V7a5 5 0 0 1 5-5z"/>
          </svg>
        </div>
        <h2>Quiz Selesai!</h2>
        <div class="finished-score">
          {{ normalizedScore }} <span class="pts-text">/ 100 pts</span>
        </div>
        <p>Kamu menjawab benar {{ correctCount }} dari {{ totalQuestions }} soal</p>

        <!-- Accuracy & Stats Grid -->
        <div class="stats-grid">
          <div class="stat-card accuracy">
            <span class="stat-val">{{ accuracyPercent }}%</span>
            <span class="stat-lbl">Akurasi</span>
          </div>
          <div class="stat-card correct">
            <span class="stat-val">{{ correctCount }}</span>
            <span class="stat-lbl">Benar</span>
          </div>
          <div class="stat-card wrong">
            <span class="stat-val">{{ wrongCount }}</span>
            <span class="stat-lbl">Salah</span>
          </div>
          <div class="stat-card skipped">
            <span class="stat-val">{{ skippedCount }}</span>
            <span class="stat-lbl">Dilewati</span>
          </div>
        </div>
        
        <!-- Review Section -->
        <div class="review-section">
          <h3>Review Pertanyaan</h3>
          <div class="review-list">
            <div v-for="item in quizReviewList" :key="item.number" class="review-card" :class="{ 'rc-correct': item.isCorrect, 'rc-wrong': !item.isCorrect && !item.isSkipped, 'rc-skipped': item.isSkipped }">
              <div class="review-card-header">
                <span class="review-q-num">Pertanyaan {{ item.number }}</span>
                <span class="review-badge" :class="item.isCorrect ? 'badge-correct' : (item.isSkipped ? 'badge-skipped' : 'badge-wrong')">
                  {{ item.isCorrect ? 'Benar' : (item.isSkipped ? 'Dilewati' : 'Salah') }}
                </span>
              </div>
              <p class="review-q-text">{{ item.questionText }}</p>
              <div class="review-answers">
                <div class="answer-row">
                  <span class="answer-label">Jawaban Anda:</span>
                  <span class="answer-val" :class="{ 'val-correct': item.isCorrect, 'val-wrong': !item.isCorrect && !item.isSkipped, 'val-skipped': item.isSkipped }">
                    {{ item.isSkipped ? 'Tidak dijawab / dilewati' : item.selectedText }}
                  </span>
                </div>
                <div v-if="!item.isCorrect && !item.isSkipped" class="answer-row">
                  <span class="answer-label">Jawaban Benar:</span>
                  <span class="answer-val val-correct">{{ item.correctAnswer }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <button class="btn-generate" @click="handleNewQuiz" style="margin-top: 2rem;">
          <span class="btn-icon-content">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21.5 2v6h-6M21.34 15.57a10 10 0 1 1-.57-8.38l5.67-5.67"/>
            </svg>
            <span>Quiz Baru</span>
          </span>
        </button>
      </div>
    </div>

    <!-- Active Quiz -->
    <div v-else class="qz-content">
      <div class="qz-session-bar">
        <div class="session-left"><span class="session-label">SESI AKTIF</span><h2 class="session-title">{{ quizStore.currentSession?.documentFilename || 'Quiz' }}</h2></div>
        <div class="session-center"><span class="session-progress-pill"><strong>{{ questionNumber }} / {{ totalQuestions }}</strong> Pertanyaan</span></div>
        <div class="session-right"><span class="score-label">SKOR ANDA</span><span class="score-value">{{ formattedScore }} pts</span></div>
      </div>
      <div class="qz-progress-bar"><div class="qz-progress-fill" :style="{ width: progressPercent + '%' }"></div></div>

      <transition name="slide-fade" mode="out-in">
        <div :key="questionNumber" class="qz-question-container">
          <div class="qz-question-card" v-if="currentQuestion">
            <div v-if="timerLimit > 0" class="timer-circle">
              <svg viewBox="0 0 48 48" class="timer-svg"><circle cx="24" cy="24" r="20" fill="none" stroke="#E2E8F0" stroke-width="3"/><circle cx="24" cy="24" r="20" fill="none" stroke="#EF4444" stroke-width="3" stroke-linecap="round" :stroke-dasharray="125.6" :stroke-dashoffset="125.6 - (timerProgress / 100) * 125.6" transform="rotate(-90 24 24)" class="timer-arc"/></svg>
              <span class="timer-number">{{ timer }}</span>
            </div>
            <div v-else class="practice-badge">
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M4 19.5v-15A2.5 2.5 0 0 1 6.5 2H20v20H6.5a2.5 2.5 0 0 1-2.5-2.5Z"/>
                <path d="M6 6h10M6 10h10"/>
              </svg>
              <span>Mode Latihan</span>
            </div>
            <div class="qz-category-badge">
              QUIZ AI • {{ personaType === 'STRICT_LECTURER' ? 'SULIT' : 'MUDAH' }}
              <span v-if="timerLimit > 0"> • {{ timerLimit }}S</span>
              <span v-else> • LATIHAN</span>
            </div>
            <h1 class="qz-question-text">{{ currentQuestion.questionText }}</h1>
          </div>

          <div class="qz-options-grid" v-if="currentQuestion && currentQuestion.options">
            <button v-for="(opt, i) in currentQuestion.options" :key="i" class="qz-option" :class="getOptionClass(i)" :style="{ '--opt-color': optionMeta[i % 4].color }" @click="selectAnswer(i)" :disabled="showResult">
              <span class="opt-shape">
                <svg v-if="optionMeta[i%4].shape==='triangle'" width="28" height="28" viewBox="0 0 28 28"><polygon points="14,4 26,24 2,24" fill="white" opacity="0.9"/></svg>
                <svg v-if="optionMeta[i%4].shape==='square'" width="28" height="28" viewBox="0 0 28 28"><rect x="4" y="4" width="20" height="20" rx="3" fill="white" opacity="0.9"/></svg>
                <svg v-if="optionMeta[i%4].shape==='circle'" width="28" height="28" viewBox="0 0 28 28"><circle cx="14" cy="14" r="10" fill="white" opacity="0.9"/></svg>
                <svg v-if="optionMeta[i%4].shape==='star'" width="28" height="28" viewBox="0 0 28 28"><polygon points="14,3 17.5,10.5 25.5,11.5 19.5,17 21,25 14,21 7,25 8.5,17 2.5,11.5 10.5,10.5" fill="white" opacity="0.9"/></svg>
              </span>
              <span class="opt-text">{{ opt }}</span>
              <span v-if="showResult && i === getCorrectIndex()" class="opt-result-icon correct-icon">✓</span>
              <span v-if="showResult && i === selectedAnswer && !isCorrect && i !== getCorrectIndex()" class="opt-result-icon wrong-icon">✗</span>
            </button>
          </div>
        </div>
      </transition>

      <div class="qz-bottom-controls">
        <button class="btn-skip" @click="skipQuestion" :disabled="showResult"><svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M6 4l12 8-12 8V4z"/><rect x="18" y="4" width="2" height="16"/></svg> Lewati</button>
        <button v-if="showResult" class="btn-next-q" @click="nextQuestion">{{ quizStore.isLastQuestion ? 'Selesai' : 'Selanjutnya' }} <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg></button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.qz-page { min-height:100vh; background:#F8FAFC; color:#1E293B; display:flex; flex-direction:column; }
.qz-header { display:flex; align-items:center; justify-content:space-between; padding:.75rem 2rem; background:#FFF; border-bottom:1px solid #E2E8F0; }
.qz-header-left { display:flex; align-items:center; gap:.75rem; }
.btn-new-quiz { display:inline-flex; align-items:center; gap:.5rem; background:#0052CC; color:#FFF; padding:.625rem 1.25rem; border-radius:24px; font-size:.875rem; font-weight:600; border:none; cursor:pointer; transition:all .2s; }
.btn-new-quiz:hover { background:#0043A6; }
.qz-user-area { position:relative; } .qz-avatar-btn { display:flex; align-items:center; gap:.5rem; background:none; border:none; cursor:pointer; padding:4px; border-radius:10px; } .qz-avatar-btn:hover { background:#F1F5F9; }
.qz-avatar { width:36px; height:36px; border-radius:10px; overflow:hidden; border:2px solid #E2E8F0; } .qz-avatar img { width:100%; height:100%; object-fit:cover; }
.qz-avatar-initials { display:flex; align-items:center; justify-content:center; background:linear-gradient(135deg,#3B82F6,#8B5CF6); color:#fff; font-size:.75rem; font-weight:700; }
.qz-user-dropdown { position:absolute; right:0; top:calc(100%+8px); width:220px; background:#fff; border:1px solid #E2E8F0; border-radius:12px; box-shadow:0 10px 40px rgba(0,0,0,.1); padding:.5rem; z-index:60; }
.dd-info { padding:.625rem .75rem; } .dd-name { display:block; font-size:.875rem; font-weight:600; color:#1E293B; } .dd-email { display:block; font-size:.75rem; color:#94A3B8; margin-top:2px; } .dd-div { height:1px; background:#F1F5F9; margin:.25rem 0; }
.dd-item { display:flex; width:100%; padding:.5rem .75rem; background:none; border:none; border-radius:8px; font-size:.8125rem; color:#EF4444; cursor:pointer; } .dd-item:hover { background:#FEF2F2; }

/* Setup */
.qz-setup { flex:1; display:flex; align-items:center; justify-content:center; padding:2rem; }
.setup-card { background:#fff; border:1px solid #E2E8F0; border-radius:20px; padding:3rem; max-width:520px; width:100%; text-align:center; box-shadow:0 10px 40px rgba(0,0,0,.04); }
.setup-icon,.finished-emoji { font-size:3rem; margin-bottom:1rem; display: flex; justify-content: center; align-items: center; color: #3B82F6; } .setup-card h2 { font-size:1.5rem; font-weight:800; color:#0F172A; margin-bottom:.5rem; }

.practice-badge { position:absolute; top:1.25rem; left:1.5rem; background:#EFF6FF; border:1.5px solid #3B82F6; color:#3B82F6; font-size:0.8125rem; font-weight:700; padding:0.375rem 0.75rem; border-radius:20px; display: inline-flex; align-items: center; gap: 0.375rem; }

.btn-icon-content { display: inline-flex; align-items: center; justify-content: center; gap: 0.5rem; }
.animate-spin { animation: spin 1s linear infinite; }
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }

.setup-card > p { color:#64748B; font-size:.9375rem; margin-bottom:1.5rem; }
.setup-docs { text-align:left; margin-bottom:1.25rem; } .setup-label { display:block; font-size:.8125rem; font-weight:700; color:#475569; margin-bottom:.5rem; }
.doc-list-mini { display:flex; flex-wrap:wrap; gap:.5rem; }
.doc-chip { display:inline-flex; align-items:center; gap:.375rem; padding:.5rem .75rem; background:#F8FAFC; border:1px solid #E2E8F0; border-radius:8px; font-size:.8125rem; cursor:pointer; transition:all .2s; }
.doc-chip.selected { border-color:#3B82F6; background:#EFF6FF; } .doc-chip input { display:none; }
.setup-options { display:grid; grid-template-columns:1fr 1fr; gap:1.25rem; margin-top:1.75rem; margin-bottom:2rem; text-align:left; width:100%; }
.setup-opt { display:flex; flex-direction:column; align-items:stretch; gap:0.5rem; }
.setup-opt label { font-size:0.8125rem; font-weight:700; color:#475569; text-transform:uppercase; letter-spacing:0.05em; }
.setup-opt select, .setup-opt input[type="number"] { padding:0.75rem 1rem; border:1.5px solid #E2E8F0; border-radius:12px; font-size:0.9375rem; font-weight:500; color:#1E293B; background-color:#FFF; transition:border-color 0.2s, box-shadow 0.2s; }
.setup-opt select { padding-right:2.25rem; background-image:url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 20 20'%3e%3cpath stroke='%236b7280' stroke-linecap='round' stroke-linejoin='round' stroke-width='1.5' d='M6 8l4 4 4-4'/%3e%3c/svg%3e"); background-position:right 0.75rem center; background-size:1.25rem; background-repeat:no-repeat; appearance:none; cursor:pointer; }
.setup-opt select:hover, .setup-opt input[type="number"]:hover { border-color:#CBD5E1; }
.setup-opt select:focus, .setup-opt input[type="number"]:focus { outline:none; border-color:#3B82F6; box-shadow:0 0 0 3px rgba(59, 130, 246, 0.15); }
.setup-error { padding:.625rem; background:#FEF2F2; color:#DC2626; border-radius:8px; font-size:.8125rem; margin-bottom:1rem; }
.setup-actions { display:flex; gap:.75rem; justify-content:center; flex-wrap:wrap; }
.btn-upload-setup { padding:.75rem 1.25rem; background:#F1F5F9; color:#475569; border:1px solid #E2E8F0; border-radius:10px; font-size:.875rem; font-weight:600; cursor:pointer; } .btn-upload-setup:hover { background:#E2E8F0; } .btn-upload-setup:disabled { opacity:.5; }
.btn-generate { padding:.75rem 1.5rem; background:#3B82F6; color:#fff; border:none; border-radius:10px; font-size:.875rem; font-weight:600; cursor:pointer; box-shadow:0 4px 16px rgba(59,130,246,.3); transition:all .2s; } .btn-generate:hover { background:#2563EB; } .btn-generate:disabled { opacity:.6; cursor:default; }

/* Finished */
.qz-finished { flex:1; display:flex; align-items:center; justify-content:center; padding:2rem; }
.finished-card { background:#fff; border:1px solid #E2E8F0; border-radius:20px; padding:3rem; max-width:680px; width:100%; text-align:center; box-shadow:0 10px 40px rgba(0,0,0,.04); }
.finished-emoji { font-size:4rem; margin-bottom:1rem; } .finished-card h2 { font-size:1.75rem; font-weight:800; margin-bottom:1rem; }
.finished-score { font-size:3.5rem; font-weight:900; color:#3B82F6; margin-bottom:.5rem; display:flex; align-items:baseline; justify-content:center; gap:0.25rem; }
.finished-score .pts-text { font-size:1.5rem; font-weight:600; color:#94A3B8; }
.finished-score .score-divider { font-size:2rem; font-weight:400; color:#94A3B8; margin:0 0.5rem; }

.practice-badge { position:absolute; top:1.25rem; left:1.5rem; background:#EFF6FF; border:1.5px solid #3B82F6; color:#3B82F6; font-size:0.8125rem; font-weight:700; padding:0.375rem 0.75rem; border-radius:20px; }

.stats-grid { display:grid; grid-template-columns:repeat(4, 1fr); gap:1rem; margin:2rem 0; }
.stat-card { background:#F8FAFC; border:1px solid #E2E8F0; border-radius:12px; padding:1rem; display:flex; flex-direction:column; align-items:center; justify-content:center; }
.stat-card .stat-val { font-size:1.5rem; font-weight:800; }
.stat-card .stat-lbl { font-size:0.75rem; font-weight:600; color:#64748B; text-transform:uppercase; margin-top:0.25rem; }
.stat-card.accuracy .stat-val { color:#3B82F6; }
.stat-card.correct .stat-val { color:#10B981; }
.stat-card.correct { border-bottom:4px solid #10B981; }
.stat-card.wrong .stat-val { color:#EF4444; }
.stat-card.wrong { border-bottom:4px solid #EF4444; }
.stat-card.skipped .stat-val { color:#64748B; }
.stat-card.skipped { border-bottom:4px solid #64748B; }

.qz-question-container { display:flex; flex-direction:column; gap:1.25rem; width:100%; }

.slide-fade-enter-active { transition:all 0.3s ease-out; }
.slide-fade-leave-active { transition:all 0.2s ease-in; }
.slide-fade-enter-from { transform:translateX(15px); opacity:0; }
.slide-fade-leave-to { transform:translateX(-15px); opacity:0; }
.finished-card p { color:#64748B; margin-bottom:1.5rem; }

.review-section { margin-top:2.5rem; text-align:left; border-top:1px solid #F1F5F9; padding-top:2rem; width:100%; }
.review-section h3 { font-size:1.125rem; font-weight:700; color:#0F172A; margin-bottom:1.25rem; }
.review-list { display:flex; flex-direction:column; gap:1rem; max-height:480px; overflow-y:auto; padding-right:0.5rem; }
.review-list::-webkit-scrollbar { width:6px; }
.review-list::-webkit-scrollbar-track { background:#F1F5F9; border-radius:3px; }
.review-list::-webkit-scrollbar-thumb { background:#CBD5E1; border-radius:3px; }
.review-card { background:#F8FAFC; border:1px solid #E2E8F0; border-radius:14px; padding:1.25rem; transition:all 0.2s; }
.review-card.rc-correct { border-left:4px solid #10B981; }
.review-card.rc-wrong { border-left:4px solid #EF4444; }
.review-card.rc-skipped { border-left:4px solid #64748B; }
.review-card-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:0.75rem; }
.review-q-num { font-size:0.8125rem; font-weight:700; color:#64748B; text-transform:uppercase; letter-spacing:0.05em; }
.review-badge { font-size:0.75rem; font-weight:700; padding:0.25rem 0.625rem; border-radius:12px; }
.badge-correct { background:#ECFDF5; color:#10B981; }
.badge-wrong { background:#FEF2F2; color:#EF4444; }
.badge-skipped { background:#F1F5F9; color:#64748B; }
.review-q-text { font-size:0.9375rem; font-weight:600; color:#1E293B; line-height:1.5; margin:0 0 1rem 0; }
.review-answers { background:#FFF; border:1px solid #E2E8F0; border-radius:10px; padding:0.875rem 1.125rem; display:flex; flex-direction:column; gap:0.625rem; }
.answer-row { display:flex; flex-direction:column; }
@media (min-width: 640px) { .answer-row { flex-direction:row; align-items:center; } }
.answer-label { font-size:0.8125rem; font-weight:600; color:#64748B; min-width:110px; }
.answer-val { font-size:0.875rem; font-weight:700; }
.val-correct { color:#10B981; }
.val-wrong { color:#EF4444; }
.val-skipped { color:#64748B; font-style:italic; font-weight:500; }

/* Quiz content */
.qz-content { flex:1; max-width:900px; width:100%; margin:0 auto; padding:1.5rem 2rem 3rem; display:flex; flex-direction:column; gap:1.25rem; }
.qz-session-bar { display:flex; align-items:center; justify-content:space-between; }
.session-left { display:flex; flex-direction:column; } .session-label { font-size:.6875rem; font-weight:800; color:#3B82F6; letter-spacing:.06em; }
.session-title { font-size:1.125rem; font-weight:700; color:#0F172A; margin-top:2px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap; max-width:250px; }
.session-center { display:flex; align-items:center; }
.session-progress-pill { background:#ECFDF5; color:#10B981; font-size:.8125rem; font-weight:600; padding:.375rem 1rem; border-radius:20px; border:1px solid #A7F3D0; } .session-progress-pill strong { color:#059669; }
.session-right { display:flex; flex-direction:column; align-items:flex-end; } .score-label { font-size:.6875rem; font-weight:800; color:#94A3B8; } .score-value { font-size:1.25rem; font-weight:800; color:#3B82F6; }
.qz-progress-bar { width:100%; height:8px; background:#E2E8F0; border-radius:4px; overflow:hidden; }
.qz-progress-fill { height:100%; background:linear-gradient(90deg,#3B82F6,#2563EB); border-radius:4px; transition:width .5s; }
.qz-question-card { background:#FFF; border:1px solid #E2E8F0; border-radius:20px; padding:2.5rem 3rem; text-align:center; position:relative; box-shadow:0 4px 24px rgba(0,0,0,.03); display:flex; flex-direction:column; align-items:center; min-height:200px; justify-content:center; }
.timer-circle { position:absolute; top:1.25rem; left:1.5rem; width:52px; height:52px; } .timer-svg { width:100%; height:100%; } .timer-arc { transition:stroke-dashoffset .3s linear; }
.timer-number { position:absolute; inset:0; display:flex; align-items:center; justify-content:center; font-size:1.125rem; font-weight:800; color:#0F172A; }
.qz-category-badge { background:#F1F5F9; color:#475569; font-size:.75rem; font-weight:700; padding:.375rem 1rem; border-radius:20px; margin-bottom:1.5rem; }
.qz-question-text { font-size:1.5rem; font-weight:800; color:#0F172A; line-height:1.4; max-width:90%; }
.qz-options-grid { display:grid; grid-template-columns:1fr 1fr; gap:1rem; }
.qz-option { display:flex; align-items:center; gap:1rem; padding:1.5rem 1.75rem; border-radius:16px; border:none; background:var(--opt-color); color:#fff; font-size:1.0625rem; font-weight:700; cursor:pointer; transition:all .25s; position:relative; min-height:72px; text-align:left; }
.qz-option:hover:not(:disabled) { transform:translateY(-3px); box-shadow:0 8px 24px rgba(0,0,0,.15); filter:brightness(1.08); }
.qz-option:disabled { cursor:default; } .qz-option.dimmed { opacity:.4; transform:scale(.97); }
.qz-option.correct { box-shadow:0 0 0 4px #fff, 0 0 0 7px #10B981; transform:scale(1.02); }
.qz-option.wrong { opacity:.5; transform:scale(.95); }
.opt-shape { display:flex; align-items:center; justify-content:center; width:36px; height:36px; flex-shrink:0; } .opt-text { flex:1; }
.opt-result-icon { position:absolute; right:1rem; top:50%; transform:translateY(-50%); width:32px; height:32px; border-radius:50%; display:flex; align-items:center; justify-content:center; font-size:1.125rem; font-weight:900; }
.correct-icon { background:rgba(255,255,255,.3); } .wrong-icon { background:rgba(0,0,0,.2); }
.qz-bottom-controls { display:flex; align-items:center; gap:1rem; margin-top:.5rem; }
.btn-skip { display:inline-flex; align-items:center; gap:.5rem; padding:.625rem 1rem; background:none; border:none; color:#64748B; font-size:.875rem; font-weight:600; cursor:pointer; border-radius:8px; } .btn-skip:hover:not(:disabled) { color:#1E293B; background:#F1F5F9; } .btn-skip:disabled { opacity:.4; cursor:default; }
.btn-next-q { display:inline-flex; align-items:center; gap:.5rem; padding:.625rem 1.25rem; background:#0052CC; color:#fff; border:none; border-radius:10px; font-size:.875rem; font-weight:600; cursor:pointer; margin-left:auto; box-shadow:0 4px 12px rgba(0,82,204,.25); } .btn-next-q:hover { background:#0043A6; }

@media (max-width:768px) {
  .qz-header { padding:.75rem 1rem; }
  .qz-content { padding:1rem; }
  .qz-session-bar { flex-direction:column; align-items:flex-start; gap:.75rem; }
  .session-right { align-items:flex-start; }
  .qz-question-card { padding:2rem 1.5rem; }
  .qz-question-text { font-size:1.125rem; max-width:100%; }
  .qz-options-grid { grid-template-columns:1fr; }
  .qz-option { padding:1.25rem; font-size:1rem; }
  .timer-circle { width:44px; height:44px; top:1rem; left:1rem; }
  .setup-card { padding:2rem 1.5rem; }
}
</style>
