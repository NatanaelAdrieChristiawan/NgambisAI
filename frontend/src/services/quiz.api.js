/**
 * Ngambis.ai — Quiz Session API Service
 *
 * Encapsulates quiz/flashcard session creation and retrieval.
 * Sessions generate AI-powered questions from uploaded documents.
 */

import api from './api'

const quizApi = {
  /**
   * Create a new quiz session — AI generates questions from documents.
   * @param {{ userId: string, documentIds: string[], personaType: string, questionCount: number, itemType: string }} data
   * @param {string} data.personaType - "STRICT_LECTURER" or "FRIENDLY_SENIOR"
   * @param {string} data.itemType - "MULTIPLE_CHOICE" or "ESSAY"
   */
  createSession(data) {
    return api.post('/api/sessions', data)
  },

  /**
   * Get a quiz session by ID (includes quiz items).
   * @param {string} sessionId
   */
  getSession(sessionId) {
    return api.get(`/api/sessions/${sessionId}`)
  },

  /**
   * Get all sessions for a user.
   * @param {string} userId
   */
  getUserSessions(userId) {
    return api.get(`/api/sessions/user/${userId}`)
  }
}

export default quizApi
