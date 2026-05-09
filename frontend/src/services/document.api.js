/**
 * Ngambis.ai — Document API Service
 *
 * Encapsulates PDF upload, text extraction, and document retrieval.
 */

import api from './api'

const documentApi = {
  /**
   * Upload a PDF document for text extraction.
   * @param {string} userId
   * @param {File} file - PDF file object
   */
  uploadDocument(userId, file) {
    const formData = new FormData()
    formData.append('userId', userId)
    formData.append('file', file)

    return api.post('/api/documents', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 60000 // PDF upload may take longer
    })
  },

  /**
   * Get all documents for a user.
   * @param {string} userId
   */
  getUserDocuments(userId) {
    return api.get(`/api/documents/user/${userId}`)
  },

  /**
   * Get a single document by ID.
   * @param {string} documentId
   */
  getDocument(documentId) {
    return api.get(`/api/documents/${documentId}`)
  },

  /**
   * Delete a document by ID.
   * @param {string} documentId
   */
  deleteDocument(documentId) {
    return api.delete(`/api/documents/${documentId}`)
  }
}

export default documentApi
