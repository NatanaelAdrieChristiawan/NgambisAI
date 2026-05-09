/**
 * Ngambis.ai — User API Service
 *
 * Encapsulates user profile management endpoints.
 */

import api from './api'

const userApi = {
  /**
   * Get user profile by ID.
   * @param {string} userId
   */
  getUser(userId) {
    return api.get(`/api/users/${userId}`)
  },

  /**
   * Update user profile (name, username, profilePicture).
   * @param {string} userId
   * @param {{ name?: string, username?: string, profilePicture?: string }} data
   */
  updateProfile(userId, data) {
    return api.put(`/api/users/${userId}`, data)
  }
}

export default userApi
