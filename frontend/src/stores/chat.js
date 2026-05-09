/**
 * Ngambis.ai — Chat Store (Pinia)
 *
 * Centralized state management for AI Chat conversations.
 * Handles loading conversations, sending messages, and managing chat state.
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import chatApi from '@/services/chat.api'
import { useAuthStore } from './auth'

export const useChatStore = defineStore('chat', () => {
  // ===== State =====
  const conversations = ref([])
  const currentConversation = ref(null)
  const messages = ref([])
  const loading = ref(false)
  const sending = ref(false)
  const error = ref(null)

  // ===== Getters =====
  const sortedConversations = computed(() => {
    return [...conversations.value].sort(
      (a, b) => new Date(b.updatedAt || b.createdAt) - new Date(a.updatedAt || a.createdAt)
    )
  })

  const hasConversations = computed(() => conversations.value.length > 0)

  // ===== Actions =====

  /**
   * Load all conversations for the current user (list view, no messages).
   */
  async function loadConversations() {
    const authStore = useAuthStore()
    if (!authStore.user?.id) return

    loading.value = true
    error.value = null
    try {
      const response = await chatApi.getUserConversations(authStore.user.id)
      conversations.value = response.data.data || []
    } catch (err) {
      error.value = err.response?.data?.message || 'Gagal memuat riwayat chat'
      console.error('Failed to load conversations:', err)
    } finally {
      loading.value = false
    }
  }

  /**
   * Load a specific conversation with all messages.
   */
  async function loadConversation(conversationId) {
    if (!conversationId) return

    loading.value = true
    error.value = null
    try {
      const response = await chatApi.getConversation(conversationId)
      const data = response.data.data
      currentConversation.value = data
      messages.value = (data.messages || []).map(msg => ({
        id: msg.id,
        type: msg.role === 'model' ? 'ai' : 'user',
        content: msg.content,
        createdAt: msg.createdAt
      }))
    } catch (err) {
      error.value = err.response?.data?.message || 'Gagal memuat percakapan'
      console.error('Failed to load conversation:', err)
    } finally {
      loading.value = false
    }
  }

  /**
   * Send a message. Creates new conversation if no current one exists.
   * @param {string} messageText
   * @param {string[]} documentIds - required for new conversations
   */
  async function sendMessage(messageText, documentIds = []) {
    const authStore = useAuthStore()
    if (!authStore.user?.id || !messageText.trim()) return

    sending.value = true
    error.value = null

    // Optimistic: add user message immediately
    const tempId = 'temp-' + Date.now()
    messages.value.push({
      id: tempId,
      type: 'user',
      content: messageText.trim(),
      createdAt: new Date().toISOString()
    })

    try {
      const payload = {
        userId: authStore.user.id,
        message: messageText.trim()
      }

      if (currentConversation.value?.id) {
        payload.conversationId = currentConversation.value.id
      } else {
        // New conversation requires document IDs
        if (documentIds.length === 0) {
          throw new Error('Pilih minimal satu dokumen untuk memulai percakapan baru')
        }
        payload.documentIds = documentIds
      }

      const response = await chatApi.sendMessage(payload)
      const data = response.data.data

      // Update conversation state
      currentConversation.value = data

      // Replace messages with full conversation from backend
      messages.value = (data.messages || []).map(msg => ({
        id: msg.id,
        type: msg.role === 'model' ? 'ai' : 'user',
        content: msg.content,
        createdAt: msg.createdAt
      }))

      // Refresh conversations list
      await loadConversations()

      return data
    } catch (err) {
      error.value = err.response?.data?.message || err.message || 'Gagal mengirim pesan'
      // Remove optimistic message on error
      messages.value = messages.value.filter(m => m.id !== tempId)
      throw err
    } finally {
      sending.value = false
    }
  }

  /**
   * Start a new chat (clear current conversation).
   */
  function newChat() {
    currentConversation.value = null
    messages.value = []
    error.value = null
  }

  /**
   * Clear error state.
   */
  function clearError() {
    error.value = null
  }

  /**
   * Delete a conversation by ID.
   */
  async function deleteConversation(conversationId) {
    try {
      await chatApi.deleteConversation(conversationId)
      // Remove from local list
      conversations.value = conversations.value.filter(c => c.id !== conversationId)
      // If it's the current conversation, clear it
      if (currentConversation.value?.id === conversationId) {
        currentConversation.value = null
        messages.value = []
      }
    } catch (err) {
      console.error('Failed to delete conversation:', err)
      throw err
    }
  }

  return {
    // State
    conversations,
    currentConversation,
    messages,
    loading,
    sending,
    error,
    // Getters
    sortedConversations,
    hasConversations,
    // Actions
    loadConversations,
    loadConversation,
    sendMessage,
    deleteConversation,
    newChat,
    clearError
  }
})
