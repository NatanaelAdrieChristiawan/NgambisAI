/**
 * Ngambis.ai — Quiz Store (Pinia)
 *
 * Centralized state management for Quiz and Flashcard sessions.
 * Handles session creation with AI-generated questions and answer tracking.
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import quizApi from '@/services/quiz.api'
import { useAuthStore } from './auth'

function deterministicShuffle(array, seedString) {
  if (!array || !Array.isArray(array)) return []
  if (!seedString) return [...array]
  
  let seed = 0
  for (let i = 0; i < seedString.length; i++) {
    seed = (seed << 5) - seed + seedString.charCodeAt(i)
    seed |= 0 // Convert to 32bit integer
  }
  
  function random() {
    let x = Math.sin(seed++) * 10000
    return x - Math.floor(x)
  }

  const shuffled = [...array]
  for (let i = shuffled.length - 1; i > 0; i--) {
    const j = Math.floor(random() * (i + 1));
    [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]]
  }
  return shuffled
}

export const useQuizStore = defineStore('quiz', () => {
  // ===== State =====
  const sessions = ref([])
  const currentSession = ref(null)
  const quizItems = ref([])
  const currentQuestionIndex = ref(0)
  const score = ref(0)
  const answers = ref({}) // { quizItemId: selectedIndex }
  const loading = ref(false)
  const generating = ref(false)
  const error = ref(null)

  /**
   * Tracks which view created each session (e.g. 'Flashcards' | 'VoiceToSpeech' | 'QuizMode').
   * Stored as { sessionId: viewName } — used to disambiguate ESSAY sessions in the sidebar.
   */
  const sessionSources = ref({})

  // ===== Getters =====
  const currentQuestion = computed(() => {
    return quizItems.value[currentQuestionIndex.value] || null
  })

  const totalQuestions = computed(() => quizItems.value.length)

  const progressPercent = computed(() => {
    if (totalQuestions.value === 0) return 0
    return ((currentQuestionIndex.value + 1) / totalQuestions.value) * 100
  })

  const isLastQuestion = computed(() => {
    return currentQuestionIndex.value >= totalQuestions.value - 1
  })

  const formattedScore = computed(() => score.value.toLocaleString())

  // ===== Actions =====

  /**
   * Create a new session — AI generates questions from documents.
   * @param {{ documentIds: string[], personaType: string, questionCount: number, itemType: string, sourceView?: string }} options
   *   sourceView — the route name of the calling view ('Flashcards' | 'VoiceToSpeech' | 'QuizMode')
   */
  async function createSession({ documentIds, personaType = 'FRIENDLY_SENIOR', questionCount = 5, itemType = 'MULTIPLE_CHOICE', sourceView = null }) {
    const authStore = useAuthStore()
    if (!authStore.user?.id) throw new Error('User not authenticated')

    generating.value = true
    error.value = null
    try {
      const response = await quizApi.createSession({
        userId: authStore.user.id,
        documentIds,
        personaType,
        questionCount,
        itemType
      })

      const data = response.data.data
      currentSession.value = data

      // ✅ Realtime history: prepend new session to sidebar immediately
      const existingIndex = sessions.value.findIndex(s => s.id === data.id)
      if (existingIndex === -1) {
        sessions.value = [data, ...sessions.value]
      } else {
        sessions.value[existingIndex] = data
      }

      // Track which view created this session (to differentiate ESSAY flashcard vs voice)
      if (sourceView && data.id) {
        sessionSources.value = { ...sessionSources.value, [data.id]: sourceView }
      }

      quizItems.value = (data.quizItems || []).map(item => {
        const mapped = {
          id: item.id,
          itemType: item.itemType,
          questionText: item.questionText,
          referenceText: item.referenceText,
          evaluations: item.evaluations || []
        }

        // Parse MC options from JSON string
        if (item.itemType === 'MULTIPLE_CHOICE' && item.options) {
          try {
            const parsed = JSON.parse(item.options)
            mapped.options = deterministicShuffle(parsed, item.id)
          } catch {
            mapped.options = [item.options]
          }
          mapped.correctAnswer = item.correctAnswer
        }

        return mapped
      })

      // Reset quiz state
      currentQuestionIndex.value = 0
      score.value = 0
      answers.value = {}

      return data
    } catch (err) {
      error.value = err.response?.data?.message || 'Gagal membuat sesi quiz'
      throw err
    } finally {
      generating.value = false
    }
  }

  /**
   * Load all sessions for the current user.
   */
  async function loadSessions() {
    const authStore = useAuthStore()
    if (!authStore.user?.id) return

    loading.value = true
    error.value = null
    try {
      const response = await quizApi.getUserSessions(authStore.user.id)
      sessions.value = response.data.data || []
    } catch (err) {
      error.value = err.response?.data?.message || 'Gagal memuat sesi'
      console.error('Failed to load sessions:', err)
    } finally {
      loading.value = false
    }
  }

  /**
   * Load a specific session by ID.
   */
  async function loadSession(sessionId) {
    loading.value = true
    error.value = null
    try {
      const response = await quizApi.getSession(sessionId)
      const data = response.data.data
      currentSession.value = data
      quizItems.value = (data.quizItems || []).map(item => {
        const mapped = {
          id: item.id,
          itemType: item.itemType,
          questionText: item.questionText,
          referenceText: item.referenceText,
          evaluations: item.evaluations || []
        }
        if (item.itemType === 'MULTIPLE_CHOICE' && item.options) {
          try {
            const parsed = JSON.parse(item.options)
            mapped.options = deterministicShuffle(parsed, item.id)
          } catch { mapped.options = [] }
          mapped.correctAnswer = item.correctAnswer
        }
        return mapped
      })
      loadProgress(sessionId)
    } catch (err) {
      error.value = err.response?.data?.message || 'Gagal memuat sesi'
    } finally {
      loading.value = false
    }
  }

  /**
   * Record an answer for the current question.
   */
  function selectAnswer(optionIndex) {
    const q = currentQuestion.value
    if (!q || answers.value[q.id] !== undefined) return

    answers.value[q.id] = optionIndex

    // Check correctness for MC
    if (q.itemType === 'MULTIPLE_CHOICE' && q.options && q.correctAnswer) {
      const selectedText = q.options[optionIndex]
      if (selectedText === q.correctAnswer) {
        score.value += 100
      }
    }
    
    saveProgress()
  }

  /**
   * Move to next question.
   */
  function nextQuestion() {
    if (currentQuestionIndex.value < totalQuestions.value - 1) {
      currentQuestionIndex.value++
      saveProgress()
    }
  }

  /**
   * Move to previous question.
   */
  function prevQuestion() {
    if (currentQuestionIndex.value > 0) {
      currentQuestionIndex.value--
      saveProgress()
    }
  }

  /**
   * Reset quiz state for a new session.
   */
  function resetQuiz() {
    currentSession.value = null
    quizItems.value = []
    currentQuestionIndex.value = 0
    score.value = 0
    answers.value = {}
    error.value = null
  }

  function clearError() {
    error.value = null
  }

  async function deleteSession(sessionId) {
    loading.value = true
    error.value = null
    try {
      await quizApi.deleteSession(sessionId)
      sessions.value = sessions.value.filter(s => s.id !== sessionId)
      if (currentSession.value?.id === sessionId) {
        resetQuiz()
      }
      // Clean up source tracking
      const { [sessionId]: _, ...rest } = sessionSources.value
      sessionSources.value = rest
    } catch (err) {
      error.value = err.response?.data?.message || 'Gagal menghapus sesi'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function renameSession(sessionId, title) {
    loading.value = true
    error.value = null
    try {
      const response = await quizApi.renameSession(sessionId, title)
      const updated = response.data.data
      const index = sessions.value.findIndex(s => s.id === sessionId)
      if (index !== -1) {
        sessions.value[index] = updated
      }
      if (currentSession.value?.id === sessionId) {
        currentSession.value.title = title
      }
      return updated
    } catch (err) {
      error.value = err.response?.data?.message || 'Gagal mengganti nama sesi'
      throw err
    } finally {
      loading.value = false
    }
  }

  async function pinSession(sessionId, pinned) {
    loading.value = true
    error.value = null
    try {
      const response = await quizApi.pinSession(sessionId, pinned)
      const updated = response.data.data
      const index = sessions.value.findIndex(s => s.id === sessionId)
      if (index !== -1) {
        sessions.value[index] = updated
      }
      sessions.value.sort((a, b) => {
        if (a.pinned && !b.pinned) return -1
        if (!a.pinned && b.pinned) return 1
        return new Date(b.createdAt) - new Date(a.createdAt)
      })
      if (currentSession.value?.id === sessionId) {
        currentSession.value.pinned = pinned
      }
      return updated
    } catch (err) {
      error.value = err.response?.data?.message || 'Gagal menyematkan sesi'
      throw err
    } finally {
      loading.value = false
    }
  }

  // ===== Storage Persistence =====
  function saveProgress() {
    if (!currentSession.value?.id) return
    const progressData = {
      score: score.value,
      answers: answers.value,
      currentQuestionIndex: currentQuestionIndex.value
    }
    localStorage.setItem(`quiz_progress_${currentSession.value.id}`, JSON.stringify(progressData))
  }

  function loadProgress(sessionId) {
    const saved = localStorage.getItem(`quiz_progress_${sessionId}`)
    if (saved) {
      try {
        const parsed = JSON.parse(saved)
        score.value = parsed.score || 0
        answers.value = parsed.answers || {}
        currentQuestionIndex.value = parsed.currentQuestionIndex || 0
      } catch (e) {
        score.value = 0
        answers.value = {}
        currentQuestionIndex.value = 0
      }
    } else {
      score.value = 0
      answers.value = {}
      currentQuestionIndex.value = 0
    }
  }

  return {
    // State
    sessions,
    currentSession,
    quizItems,
    currentQuestionIndex,
    score,
    answers,
    loading,
    generating,
    error,
    sessionSources,
    // Getters
    currentQuestion,
    totalQuestions,
    progressPercent,
    isLastQuestion,
    formattedScore,
    // Actions
    createSession,
    loadSessions,
    loadSession,
    selectAnswer,
    nextQuestion,
    prevQuestion,
    resetQuiz,
    clearError,
    deleteSession,
    renameSession,
    pinSession
  }
})
