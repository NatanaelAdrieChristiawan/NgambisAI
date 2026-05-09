<script setup>
import { ref } from 'vue'
import { useDocumentStore } from '@/stores/document'
import ConfirmModal from '@/components/shared/ConfirmModal.vue'

const props = defineProps({
  mode: {
    type: String,
    default: 'list' // 'list' or 'chip'
  }
})

const docStore = useDocumentStore()
const fileInput = ref(null)
const isDragging = ref(false)
const uploadError = ref(null)

// Confirm modal state
const showDeleteConfirm = ref(false)
const deleteTargetId = ref(null)
const deleteTargetName = ref('')
const deleteError = ref(null)

function handleDragOver(e) {
  e.preventDefault()
  isDragging.value = true
}

function handleDragLeave(e) {
  e.preventDefault()
  isDragging.value = false
}

async function handleDrop(e) {
  e.preventDefault()
  isDragging.value = false
  const files = e.dataTransfer.files
  if (files && files.length > 0) {
    await processFile(files[0])
  }
}

async function handleFileSelect(e) {
  const file = e.target.files?.[0]
  if (file) {
    await processFile(file)
  }
  if (fileInput.value) fileInput.value.value = ''
}

async function processFile(file) {
  uploadError.value = null
  if (!file.name.toLowerCase().endsWith('.pdf')) {
    uploadError.value = 'Hanya file PDF yang diizinkan.'
    return
  }
  try {
    const doc = await docStore.uploadDocument(file)
    docStore.selectDocument(doc.id)
  } catch (err) {
    uploadError.value = err.response?.data?.message || err.message || 'Gagal mengupload dokumen.'
  }
}

function requestDelete(docId, docName, event) {
  event.stopPropagation()
  deleteTargetId.value = docId
  deleteTargetName.value = docName
  deleteError.value = null
  showDeleteConfirm.value = true
}

async function confirmDelete() {
  if (!deleteTargetId.value) return
  try {
    await docStore.deleteDocument(deleteTargetId.value)
    deleteTargetId.value = null
    deleteTargetName.value = ''
  } catch (err) {
    deleteError.value = 'Gagal menghapus dokumen.'
  }
}
</script>

<template>
  <div class="doc-manager">
    <!-- Delete Confirmation Modal -->
    <ConfirmModal
      v-model="showDeleteConfirm"
      title="Hapus Dokumen"
      :message="`Apakah kamu yakin ingin menghapus '${deleteTargetName}'? Tindakan ini tidak dapat dibatalkan.`"
      confirmText="Ya, Hapus"
      cancelText="Batal"
      variant="danger"
      @confirm="confirmDelete"
    />

    <!-- Drag & Drop Zone -->
    <div 
      class="drop-zone" 
      :class="{ 'is-dragging': isDragging, 'is-uploading': docStore.uploading }"
      @dragover="handleDragOver"
      @dragleave="handleDragLeave"
      @drop="handleDrop"
      @click="!docStore.uploading && fileInput?.click()"
    >
      <input ref="fileInput" type="file" accept=".pdf" style="display:none" @change="handleFileSelect"/>
      
      <div v-if="docStore.uploading" class="uploading-state">
        <div class="spinner"></div>
        <p>Mengupload dokumen...</p>
      </div>
      <div v-else class="idle-state">
        <div class="upload-icon">
          <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
            <polyline points="17 8 12 3 7 8"/>
            <line x1="12" y1="3" x2="12" y2="15"/>
          </svg>
        </div>
        <p class="primary-text">Klik atau Drag & Drop file PDF ke sini</p>
        <p class="secondary-text">Maksimal 10MB per file</p>
      </div>
    </div>
    
    <div v-if="uploadError" class="upload-error">{{ uploadError }}</div>
    <div v-if="deleteError" class="upload-error">{{ deleteError }}</div>

    <!-- Document List -->
    <div v-if="docStore.documents.length > 0" class="doc-list-container" :class="mode">
      <label class="list-label" v-if="mode === 'chip'">Pilih Dokumen:</label>
      
      <div class="items-wrapper">
        <label 
          v-for="doc in docStore.sortedDocuments" 
          :key="doc.id" 
          class="doc-item-row" 
          :class="{ selected: docStore.selectedDocumentIds.includes(doc.id) }"
        >
          <div class="doc-info-area">
            <input 
              type="checkbox" 
              :checked="docStore.selectedDocumentIds.includes(doc.id)" 
              @change="docStore.toggleDocumentSelection(doc.id)"
            />
            <div class="doc-details">
              <span class="doc-name" :title="doc.filename">📄 {{ doc.filename }}</span>
              <span v-if="mode === 'list'" class="doc-meta">{{ doc.extractedTextLength?.toLocaleString() || '?' }} karakter</span>
            </div>
          </div>
          
          <button class="btn-delete" @click="(e) => requestDelete(doc.id, doc.filename, e)" title="Hapus Dokumen">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="3 6 5 6 21 6"></polyline>
              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
            </svg>
          </button>
        </label>
      </div>
    </div>
  </div>
</template>

<style scoped>
.doc-manager {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
  width: 100%;
}

/* Drop Zone */
.drop-zone {
  border: 2px dashed #CBD5E1;
  border-radius: 12px;
  padding: 2rem 1.5rem;
  text-align: center;
  background: #F8FAFC;
  cursor: pointer;
  transition: all 0.2s ease;
}

.drop-zone:hover {
  border-color: #94A3B8;
  background: #F1F5F9;
}

.drop-zone.is-dragging {
  border-color: #3B82F6;
  background: #EFF6FF;
}

.drop-zone.is-uploading {
  cursor: default;
  opacity: 0.8;
}

.upload-icon {
  color: #64748B;
  margin-bottom: 0.75rem;
}

.drop-zone.is-dragging .upload-icon {
  color: #3B82F6;
  transform: translateY(-4px);
  transition: transform 0.2s;
}

.primary-text {
  font-size: 0.9375rem;
  font-weight: 600;
  color: #1E293B;
  margin-bottom: 0.25rem;
}

.secondary-text {
  font-size: 0.75rem;
  color: #64748B;
}

/* Uploading State */
.uploading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
}

.spinner {
  width: 28px;
  height: 28px;
  border: 3px solid #E2E8F0;
  border-top-color: #3B82F6;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.upload-error {
  padding: 0.75rem;
  background: #FEF2F2;
  color: #DC2626;
  border-radius: 8px;
  font-size: 0.8125rem;
  text-align: center;
}

/* Document List */
.doc-list-container {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.list-label {
  font-size: 0.8125rem;
  font-weight: 700;
  color: #475569;
  text-align: left;
}

.items-wrapper {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

/* "list" mode is full width */
.list .items-wrapper {
  width: 100%;
}

/* "chip" mode tries to wrap, but with delete buttons full-width is better. Let's make it grid or flex-wrap */
.chip .items-wrapper {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.doc-item-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.75rem 1rem;
  background: #FFFFFF;
  border: 1px solid #E2E8F0;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.doc-item-row:hover {
  background: #F8FAFC;
  border-color: #CBD5E1;
}

.doc-item-row.selected {
  border-color: #3B82F6;
  background: #EFF6FF;
}

.doc-info-area {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex: 1;
  min-width: 0;
}

.doc-item-row input[type="checkbox"] {
  accent-color: #3B82F6;
  width: 16px;
  height: 16px;
  cursor: pointer;
}

.doc-details {
  display: flex;
  flex-direction: column;
  min-width: 0;
  text-align: left;
}

.doc-name {
  font-size: 0.875rem;
  font-weight: 600;
  color: #1E293B;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.doc-meta {
  font-size: 0.6875rem;
  color: #94A3B8;
  margin-top: 2px;
}

.btn-delete {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: transparent;
  border: none;
  color: #94A3B8;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
}

.btn-delete:hover {
  background: #FEE2E2;
  color: #DC2626;
}

/* Mode Chip overrides */
.chip .doc-item-row {
  padding: 0.5rem 0.75rem;
  border-radius: 8px;
}
.chip .doc-name {
  font-size: 0.8125rem;
}
</style>
