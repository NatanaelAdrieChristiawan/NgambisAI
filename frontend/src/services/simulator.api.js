/**
 * Ngambis.ai — Simulator API Service
 *
 * Encapsulates oral exam simulation endpoints.
 * Sends student answers (text from Web Speech API) for AI evaluation.
 */

import api from './api'

const simulatorApi = {
  /**
   * Submit a text-based answer for AI evaluation.
   * Uses browser Web Speech API transcript — 100% free, no billing needed.
   * @param {{ sessionId: string, quizItemId: string, transcript: string }} data
   */
  evaluateTextAnswer({ sessionId, quizItemId, transcript }) {
    const params = new URLSearchParams()
    params.append('sessionId', sessionId)
    params.append('quizItemId', quizItemId)
    params.append('transcript', transcript)

    return api.post(`/api/simulator/evaluate-text?${params.toString()}`)
  }
}

export default simulatorApi
