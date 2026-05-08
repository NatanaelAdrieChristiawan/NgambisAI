<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
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

// Quiz data
const quizQuestions = ref([
  {
    id: 1,
    category: 'INFORMATIKA',
    difficulty: 'MENENGAH',
    question: 'Manakah di bawah ini yang merupakan struktur data "First-In, First-Out" (FIFO)?',
    options: [
      { text: 'Stack (Tumpukan)', shape: 'triangle', color: '#E53935' },
      { text: 'Queue (Antrean)', shape: 'square', color: '#1E88E5' },
      { text: 'Array (Larik)', shape: 'circle', color: '#F9A825' },
      { text: 'Linked List', shape: 'star', color: '#43A047' }
    ],
    correctIndex: 1
  },
  {
    id: 2,
    category: 'INFORMATIKA',
    difficulty: 'MUDAH',
    question: 'Apa kepanjangan dari HTML?',
    options: [
      { text: 'Hyper Text Markup Language', shape: 'triangle', color: '#E53935' },
      { text: 'High Tech Modern Language', shape: 'square', color: '#1E88E5' },
      { text: 'Hyper Transfer Markup Language', shape: 'circle', color: '#F9A825' },
      { text: 'Home Tool Markup Language', shape: 'star', color: '#43A047' }
    ],
    correctIndex: 0
  },
  {
    id: 3,
    category: 'INFORMATIKA',
    difficulty: 'MENENGAH',
    question: 'Algoritma pencarian mana yang memiliki kompleksitas waktu O(log n)?',
    options: [
      { text: 'Linear Search', shape: 'triangle', color: '#E53935' },
      { text: 'Binary Search', shape: 'square', color: '#1E88E5' },
      { text: 'Bubble Sort', shape: 'circle', color: '#F9A825' },
      { text: 'Insertion Sort', shape: 'star', color: '#43A047' }
    ],
    correctIndex: 1
  },
  {
    id: 4,
    category: 'INFORMATIKA',
    difficulty: 'SULIT',
    question: 'Apa perbedaan utama antara proses dan thread?',
    options: [
      { text: 'Thread berbagi memori, proses tidak', shape: 'triangle', color: '#E53935' },
      { text: 'Proses lebih cepat dari thread', shape: 'square', color: '#1E88E5' },
      { text: 'Thread tidak bisa berjalan paralel', shape: 'circle', color: '#F9A825' },
      { text: 'Proses berbagi memori, thread tidak', shape: 'star', color: '#43A047' }
    ],
    correctIndex: 0
  }
])

const sessionTitle = ref('Struktur Data & Algoritma')
const currentQuestionIndex = ref(0)
const totalQuestions = computed(() => quizQuestions.value.length)
const currentQuestion = computed(() => quizQuestions.value[currentQuestionIndex.value])
const questionNumber = computed(() => currentQuestionIndex.value + 1)

const score = ref(0)
const timer = ref(15)
let timerInterval = null
const timerProgress = computed(() => (timer.value / 15) * 100)

const selectedAnswer = ref(null)
const showResult = ref(false)
const isCorrect = ref(false)

function startTimer() {
  clearInterval(timerInterval)
  timer.value = 15
  timerInterval = setInterval(() => {
    if (timer.value > 0) {
      timer.value--
    } else {
      clearInterval(timerInterval)
      if (selectedAnswer.value === null) handleTimeUp()
    }
  }, 1000)
}

function handleTimeUp() {
  selectedAnswer.value = -1
  showResult.value = true
  isCorrect.value = false
}

function selectAnswer(index) {
  if (selectedAnswer.value !== null) return
  clearInterval(timerInterval)
  selectedAnswer.value = index
  showResult.value = true
  isCorrect.value = index === currentQuestion.value.correctIndex
  if (isCorrect.value) {
    score.value += Math.round(450 + (timer.value / 15) * 550)
  }
}

function nextQuestion() {
  if (currentQuestionIndex.value < totalQuestions.value - 1) {
    currentQuestionIndex.value++
    selectedAnswer.value = null
    showResult.value = false
    isCorrect.value = false
    startTimer()
  }
}

function skipQuestion() {
  if (selectedAnswer.value !== null) return
  clearInterval(timerInterval)
  selectedAnswer.value = -1
  showResult.value = true
  isCorrect.value = false
  setTimeout(nextQuestion, 1200)
}

function getOptionClass(index) {
  if (!showResult.value) return ''
  if (index === currentQuestion.value.correctIndex) return 'correct'
  if (index === selectedAnswer.value && !isCorrect.value) return 'wrong'
  return 'dimmed'
}

const formattedScore = computed(() => score.value.toLocaleString())
const progressPercent = computed(() => ((currentQuestionIndex.value + 1) / totalQuestions.value) * 100)

onMounted(() => { startTimer() })
onUnmounted(() => { clearInterval(timerInterval) })
</script>

<template>
  <div class="qz-page" @click="closeUserMenu">
    <!-- Header Bar -->
    <header class="qz-header">
      <button class="btn-new-quiz">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/></svg>
        Quiz Baru
      </button>
      <div class="qz-user-area" @click.stop>
        <button class="qz-avatar-btn" @click="toggleUserMenu">
          <div v-if="avatarUrl" class="qz-avatar"><img :src="avatarUrl" :alt="displayName" referrerpolicy="no-referrer"/></div>
          <div v-else class="qz-avatar qz-avatar-initials">{{ initials }}</div>
          <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#64748B" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="m6 9 6 6 6-6"/></svg>
        </button>
        <div v-if="showUserMenu" class="qz-user-dropdown">
          <div class="dd-info"><span class="dd-name">{{ displayName }}</span><span class="dd-email">{{ authStore.userEmail }}</span></div>
          <div class="dd-div"></div>
          <button class="dd-item" @click="handleLogout">Logout</button>
        </div>
      </div>
    </header>

    <div class="qz-content">
      <!-- Session Info Bar -->
      <div class="qz-session-bar">
        <div class="session-left">
          <span class="session-label">SESI AKTIF</span>
          <h2 class="session-title">{{ sessionTitle }}</h2>
        </div>
        <div class="session-center">
          <span class="session-progress-pill">
            <strong>{{ questionNumber }} / {{ totalQuestions }}</strong> Pertanyaan
          </span>
        </div>
        <div class="session-right">
          <span class="score-label">SKOR ANDA</span>
          <span class="score-value">{{ formattedScore }} pts</span>
        </div>
      </div>

      <!-- Progress Bar -->
      <div class="qz-progress-bar">
        <div class="qz-progress-fill" :style="{ width: progressPercent + '%' }"></div>
      </div>

      <!-- Question Card -->
      <div class="qz-question-card">
        <!-- Timer Circle -->
        <div class="timer-circle">
          <svg viewBox="0 0 48 48" class="timer-svg">
            <circle cx="24" cy="24" r="20" fill="none" stroke="#E2E8F0" stroke-width="3"/>
            <circle cx="24" cy="24" r="20" fill="none" stroke="#EF4444" stroke-width="3" stroke-linecap="round"
              :stroke-dasharray="125.6" :stroke-dashoffset="125.6 - (timerProgress / 100) * 125.6"
              transform="rotate(-90 24 24)" class="timer-arc"/>
          </svg>
          <span class="timer-number">{{ timer }}</span>
        </div>

        <!-- Category Badge -->
        <div class="qz-category-badge">
          {{ currentQuestion.category }} • {{ currentQuestion.difficulty }}
        </div>

        <!-- Question Text -->
        <h1 class="qz-question-text">{{ currentQuestion.question }}</h1>
      </div>

      <!-- Answer Options Grid -->
      <div class="qz-options-grid">
        <button
          v-for="(opt, i) in currentQuestion.options"
          :key="i"
          class="qz-option"
          :class="getOptionClass(i)"
          :style="{ '--opt-color': opt.color }"
          @click="selectAnswer(i)"
          :disabled="showResult"
        >
          <span class="opt-shape">
            <!-- Triangle -->
            <svg v-if="opt.shape === 'triangle'" width="28" height="28" viewBox="0 0 28 28"><polygon points="14,4 26,24 2,24" fill="white" opacity="0.9"/></svg>
            <!-- Square -->
            <svg v-if="opt.shape === 'square'" width="28" height="28" viewBox="0 0 28 28"><rect x="4" y="4" width="20" height="20" rx="3" fill="white" opacity="0.9"/></svg>
            <!-- Circle -->
            <svg v-if="opt.shape === 'circle'" width="28" height="28" viewBox="0 0 28 28"><circle cx="14" cy="14" r="10" fill="white" opacity="0.9"/></svg>
            <!-- Star -->
            <svg v-if="opt.shape === 'star'" width="28" height="28" viewBox="0 0 28 28"><polygon points="14,3 17.5,10.5 25.5,11.5 19.5,17 21,25 14,21 7,25 8.5,17 2.5,11.5 10.5,10.5" fill="white" opacity="0.9"/></svg>
          </span>
          <span class="opt-text">{{ opt.text }}</span>
          <!-- Result Icon -->
          <span v-if="showResult && i === currentQuestion.correctIndex" class="opt-result-icon correct-icon">✓</span>
          <span v-if="showResult && i === selectedAnswer && !isCorrect && i !== currentQuestion.correctIndex" class="opt-result-icon wrong-icon">✗</span>
        </button>
      </div>

      <!-- Bottom Controls -->
      <div class="qz-bottom-controls">
        <button class="btn-skip" @click="skipQuestion" :disabled="showResult">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M6 4l12 8-12 8V4z"/><rect x="18" y="4" width="2" height="16"/></svg>
          Lewati
        </button>
        <button v-if="showResult && currentQuestionIndex < totalQuestions - 1" class="btn-next-q" @click="nextQuestion">
          Selanjutnya
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.qz-page {
  min-height: 100vh;
  background: #F8FAFC;
  color: #1E293B;
  display: flex;
  flex-direction: column;
}

/* === HEADER === */
.qz-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.75rem 2rem;
  background: #FFFFFF;
  border-bottom: 1px solid #E2E8F0;
}
.btn-new-quiz {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  background: #0052CC;
  color: #FFFFFF;
  padding: 0.625rem 1.25rem;
  border-radius: 24px;
  font-size: 0.875rem;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-new-quiz:hover { background: #0043A6; transform: translateY(-1px); }

.qz-user-area { position: relative; }
.qz-avatar-btn { display: flex; align-items: center; gap: 0.5rem; background: none; border: none; cursor: pointer; padding: 4px; border-radius: 10px; transition: background 0.2s; }
.qz-avatar-btn:hover { background: #F1F5F9; }
.qz-avatar { width: 36px; height: 36px; border-radius: 10px; overflow: hidden; border: 2px solid #E2E8F0; }
.qz-avatar img { width: 100%; height: 100%; object-fit: cover; }
.qz-avatar-initials { display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #3B82F6, #8B5CF6); color: white; font-size: 0.75rem; font-weight: 700; }
.qz-user-dropdown { position: absolute; right: 0; top: calc(100% + 8px); width: 220px; background: white; border: 1px solid #E2E8F0; border-radius: 12px; box-shadow: 0 10px 40px rgba(0,0,0,0.1); padding: 0.5rem; z-index: 60; animation: dropIn 0.2s ease; }
@keyframes dropIn { from { opacity: 0; transform: translateY(-6px); } to { opacity: 1; transform: translateY(0); } }
.dd-info { padding: 0.625rem 0.75rem; }
.dd-name { display: block; font-size: 0.875rem; font-weight: 600; color: #1E293B; }
.dd-email { display: block; font-size: 0.75rem; color: #94A3B8; margin-top: 2px; }
.dd-div { height: 1px; background: #F1F5F9; margin: 0.25rem 0; }
.dd-item { display: flex; align-items: center; gap: 0.5rem; width: 100%; padding: 0.5rem 0.75rem; background: none; border: none; border-radius: 8px; font-size: 0.8125rem; font-weight: 500; color: #EF4444; cursor: pointer; transition: background 0.15s; }
.dd-item:hover { background: #FEF2F2; }

/* === CONTENT WRAPPER === */
.qz-content {
  flex: 1;
  max-width: 900px;
  width: 100%;
  margin: 0 auto;
  padding: 1.5rem 2rem 3rem;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

/* === SESSION BAR === */
.qz-session-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.session-left { display: flex; flex-direction: column; }
.session-label { font-size: 0.6875rem; font-weight: 800; color: #3B82F6; letter-spacing: 0.06em; }
.session-title { font-size: 1.125rem; font-weight: 700; color: #0F172A; margin-top: 2px; }
.session-center { display: flex; align-items: center; }
.session-progress-pill {
  background: #ECFDF5;
  color: #10B981;
  font-size: 0.8125rem;
  font-weight: 600;
  padding: 0.375rem 1rem;
  border-radius: 20px;
  border: 1px solid #A7F3D0;
}
.session-progress-pill strong { color: #059669; }
.session-right { display: flex; flex-direction: column; align-items: flex-end; }
.score-label { font-size: 0.6875rem; font-weight: 800; color: #94A3B8; letter-spacing: 0.05em; }
.score-value { font-size: 1.25rem; font-weight: 800; color: #3B82F6; }

/* === PROGRESS BAR === */
.qz-progress-bar {
  width: 100%;
  height: 8px;
  background: #E2E8F0;
  border-radius: 4px;
  overflow: hidden;
}
.qz-progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #3B82F6, #2563EB);
  border-radius: 4px;
  transition: width 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

/* === QUESTION CARD === */
.qz-question-card {
  background: #FFFFFF;
  border: 1px solid #E2E8F0;
  border-radius: 20px;
  padding: 2.5rem 3rem;
  text-align: center;
  position: relative;
  box-shadow: 0 4px 24px rgba(0,0,0,0.03);
  display: flex;
  flex-direction: column;
  align-items: center;
  min-height: 220px;
  justify-content: center;
}

/* Timer */
.timer-circle {
  position: absolute;
  top: 1.25rem;
  left: 1.5rem;
  width: 52px;
  height: 52px;
}
.timer-svg { width: 100%; height: 100%; }
.timer-arc { transition: stroke-dashoffset 0.3s linear; }
.timer-number {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.125rem;
  font-weight: 800;
  color: #0F172A;
}

/* Category */
.qz-category-badge {
  background: #F1F5F9;
  color: #475569;
  font-size: 0.75rem;
  font-weight: 700;
  padding: 0.375rem 1rem;
  border-radius: 20px;
  letter-spacing: 0.04em;
  margin-bottom: 1.5rem;
}

/* Question */
.qz-question-text {
  font-size: 1.625rem;
  font-weight: 800;
  color: #0F172A;
  line-height: 1.4;
  max-width: 90%;
}

/* === OPTIONS GRID === */
.qz-options-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}
.qz-option {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 1.5rem 1.75rem;
  border-radius: 16px;
  border: none;
  background: var(--opt-color);
  color: white;
  font-size: 1.125rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  min-height: 80px;
  text-align: left;
}
.qz-option:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.15);
  filter: brightness(1.08);
}
.qz-option:active:not(:disabled) { transform: scale(0.97); }
.qz-option:disabled { cursor: default; }
.qz-option.dimmed { opacity: 0.4; transform: scale(0.97); }
.qz-option.correct {
  box-shadow: 0 0 0 4px #fff, 0 0 0 7px #10B981;
  transform: scale(1.02);
}
.qz-option.wrong {
  opacity: 0.5;
  transform: scale(0.95);
}

.opt-shape {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  flex-shrink: 0;
}
.opt-text { flex: 1; }

.opt-result-icon {
  position: absolute;
  right: 1rem;
  top: 50%;
  transform: translateY(-50%);
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.125rem;
  font-weight: 900;
}
.correct-icon { background: rgba(255,255,255,0.3); }
.wrong-icon { background: rgba(0,0,0,0.2); }

/* === BOTTOM CONTROLS === */
.qz-bottom-controls {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-top: 0.5rem;
}
.btn-skip {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.625rem 1rem;
  background: none;
  border: none;
  color: #64748B;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.2s;
}
.btn-skip:hover:not(:disabled) { color: #1E293B; background: #F1F5F9; }
.btn-skip:disabled { opacity: 0.4; cursor: default; }
.btn-next-q {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.625rem 1.25rem;
  background: #0052CC;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  margin-left: auto;
  box-shadow: 0 4px 12px rgba(0,82,204,0.25);
}
.btn-next-q:hover { background: #0043A6; transform: translateY(-1px); }

/* === RESPONSIVE === */
@media (max-width: 768px) {
  .qz-content { padding: 1rem; }
  .qz-session-bar { flex-direction: column; align-items: flex-start; gap: 0.75rem; }
  .session-right { align-items: flex-start; }
  .qz-question-card { padding: 2rem 1.5rem; }
  .qz-question-text { font-size: 1.125rem; max-width: 100%; }
  .qz-options-grid { grid-template-columns: 1fr; }
  .qz-option { padding: 1.25rem; font-size: 1rem; }
  .timer-circle { width: 44px; height: 44px; top: 1rem; left: 1rem; }
}
</style>
