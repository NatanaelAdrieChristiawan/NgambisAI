/**
 * Ngambis.ai — Chat API Service
 *
 * Encapsulates all AI Chat related API calls.
 * Handles conversations and messaging with Gemini AI backend.
 */

import api from './api'

const chatApi = {
  /**
   * Send a message to the AI chat.
   * Creates a new conversation if conversationId is null.
   * @param {{ userId: string, conversationId?: string, documentIds?: string[], message: string }} data
   */
  sendMessage(data) {
    return api.post('/api/chat/message', data)
  },

  /**
   * Get a specific conversation with all messages.
   * @param {string} conversationId
   */
  getConversation(conversationId) {
    return api.get(`/api/chat/conversations/${conversationId}`)
  },

  /**
   * Get all conversations for a user (without messages, for listing).
   * @param {string} userId
   */
  getUserConversations(userId) {
    return api.get(`/api/chat/conversations/user/${userId}`)
  },

  /**
   * Delete a conversation and all its messages.
   * @param {string} conversationId
   */
  deleteConversation(conversationId) {
    return api.delete(`/api/chat/conversations/${conversationId}`)
  },

  renameConversation(conversationId, title) {
    return api.put(`/api/chat/conversations/${conversationId}/rename?title=${encodeURIComponent(title)}`)
  },

  pinConversation(conversationId, isPinned) {
    return api.put(`/api/chat/conversations/${conversationId}/pin?isPinned=${isPinned}`)
  }
}

export default chatApi
