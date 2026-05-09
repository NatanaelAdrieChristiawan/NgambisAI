/**
 * Ngambis.ai — Document Store (Pinia)
 *
 * Centralized state management for document uploads and retrieval.
 * Documents are PDF files whose text is extracted by the backend for AI context.
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import documentApi from '@/services/document.api'
import { useAuthStore } from './auth'

export const useDocumentStore = defineStore('document', () => {
  // ===== State =====
  const documents = ref([])
  const loading = ref(false)
  const uploading = ref(false)
  const error = ref(null)
  const selectedDocumentIds = ref([])

  // ===== Getters =====
  const sortedDocuments = computed(() => {
    return [...documents.value].sort(
      (a, b) => new Date(b.uploadedAt) - new Date(a.uploadedAt)
    )
  })

  const hasDocuments = computed(() => documents.value.length > 0)

  const selectedDocuments = computed(() => {
    return documents.value.filter(d => selectedDocumentIds.value.includes(d.id))
  })

  // ===== Actions =====

  /**
   * Load all documents for the current user.
   */
  async function loadDocuments() {
    const authStore = useAuthStore()
    if (!authStore.user?.id) return

    loading.value = true
    error.value = null
    try {
      const response = await documentApi.getUserDocuments(authStore.user.id)
      documents.value = response.data.data || []
    } catch (err) {
      error.value = err.response?.data?.message || 'Gagal memuat dokumen'
      console.error('Failed to load documents:', err)
    } finally {
      loading.value = false
    }
  }

  /**
   * Upload a PDF document.
   * @param {File} file - PDF file
   * @returns {Object} uploaded document response
   */
  async function uploadDocument(file) {
    const authStore = useAuthStore()
    if (!authStore.user?.id) throw new Error('User not authenticated')

    uploading.value = true
    error.value = null
    try {
      const response = await documentApi.uploadDocument(authStore.user.id, file)
      const doc = response.data.data

      // Add to local list
      documents.value.unshift(doc)

      return doc
    } catch (err) {
      error.value = err.response?.data?.message || 'Gagal mengupload dokumen'
      throw err
    } finally {
      uploading.value = false
    }
  }

  /**
   * Toggle document selection (for quiz/chat context).
   */
  function toggleDocumentSelection(docId) {
    const idx = selectedDocumentIds.value.indexOf(docId)
    if (idx >= 0) {
      selectedDocumentIds.value.splice(idx, 1)
    } else {
      selectedDocumentIds.value.push(docId)
    }
  }

  /**
   * Clear document selection.
   */
  function clearSelection() {
    selectedDocumentIds.value = []
  }

  /**
   * Select a single document.
   */
  function selectDocument(docId) {
    selectedDocumentIds.value = [docId]
  }

  function clearError() {
    error.value = null
  }

  /**
   * Delete a document by ID.
   */
  async function deleteDocument(docId) {
    try {
      await documentApi.deleteDocument(docId)
      const idx = documents.value.findIndex(d => d.id === docId)
      if (idx !== -1) {
        documents.value.splice(idx, 1)
      }
      // Also remove from selection if it was selected
      const selIdx = selectedDocumentIds.value.indexOf(docId)
      if (selIdx !== -1) {
        selectedDocumentIds.value.splice(selIdx, 1)
      }
    } catch (err) {
      console.error('Failed to delete document:', err)
      throw err
    }
  }

  return {
    // State
    documents,
    loading,
    uploading,
    error,
    selectedDocumentIds,
    // Getters
    sortedDocuments,
    hasDocuments,
    selectedDocuments,
    // Actions
    loadDocuments,
    uploadDocument,
    deleteDocument,
    toggleDocumentSelection,
    clearSelection,
    selectDocument,
    clearError
  }
})
