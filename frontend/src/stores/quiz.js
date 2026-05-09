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
   * @param {{ documentIds: string[], personaType: string, questionCount: number, itemType: string }} options
   */
  async function createSession({ documentIds, personaType = 'FRIENDLY_SENIOR', questionCount = 5, itemType = 'MULTIPLE_CHOICE' }) {
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
      quizItems.value = (data.quizItems || []).map(item => {
        const mapped = {
          id: item.id,
          itemType: item.itemType,
          questionText: item.questionText,
          referenceText: item.referenceText
        }

        // Parse MC options from JSON string
        if (item.itemType === 'MULTIPLE_CHOICE' && item.options) {
          try {
            mapped.options = JSON.parse(item.options)
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
          referenceText: item.referenceText
        }
        if (item.itemType === 'MULTIPLE_CHOICE' && item.options) {
          try { mapped.options = JSON.parse(item.options) } catch { mapped.options = [] }
          mapped.correctAnswer = item.correctAnswer
        }
        return mapped
      })
      currentQuestionIndex.value = 0
      score.value = 0
      answers.value = {}
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
  }

  /**
   * Move to next question.
   */
  function nextQuestion() {
    if (currentQuestionIndex.value < totalQuestions.value - 1) {
      currentQuestionIndex.value++
    }
  }

  /**
   * Move to previous question.
   */
  function prevQuestion() {
    if (currentQuestionIndex.value > 0) {
      currentQuestionIndex.value--
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
    clearError
  }
})
