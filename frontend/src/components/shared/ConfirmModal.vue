<script setup>
/**
 * ConfirmModal.vue
 * Elegant confirmation dialog with smooth animations.
 * Replaces browser native confirm() / alert() dialogs.
 */
import { ref, watch, nextTick } from 'vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  title: { type: String, default: 'Konfirmasi' },
  message: { type: String, default: 'Apakah kamu yakin?' },
  confirmText: { type: String, default: 'Hapus' },
  cancelText: { type: String, default: 'Batal' },
  variant: { type: String, default: 'danger' } // 'danger' | 'warning' | 'info'
})

const emit = defineEmits(['update:modelValue', 'confirm', 'cancel'])
const isVisible = ref(false)

watch(() => props.modelValue, (val) => {
  if (val) {
    nextTick(() => { isVisible.value = true })
  } else {
    isVisible.value = false
  }
})

function handleConfirm() {
  close()
  emit('confirm')
}

function handleCancel() {
  close()
  emit('cancel')
}

function close() {
  isVisible.value = false
  setTimeout(() => {
    emit('update:modelValue', false)
  }, 250)
}
</script>

<template>
  <Teleport to="body">
    <Transition name="confirm-overlay">
      <div v-if="modelValue" class="confirm-overlay" :class="{ visible: isVisible }" @click.self="handleCancel">
        <Transition name="confirm-card">
          <div v-if="isVisible" class="confirm-card" role="dialog" aria-modal="true">
            <!-- Icon -->
            <div class="confirm-icon" :class="variant">
              <svg v-if="variant === 'danger'" xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="3 6 5 6 21 6"></polyline>
                <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                <line x1="10" y1="11" x2="10" y2="17"></line>
                <line x1="14" y1="11" x2="14" y2="17"></line>
              </svg>
              <svg v-else-if="variant === 'warning'" xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path>
                <line x1="12" y1="9" x2="12" y2="13"></line>
                <line x1="12" y1="17" x2="12.01" y2="17"></line>
              </svg>
              <svg v-else xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="12" y1="16" x2="12" y2="12"></line>
                <line x1="12" y1="8" x2="12.01" y2="8"></line>
              </svg>
            </div>

            <!-- Content -->
            <h3 class="confirm-title">{{ title }}</h3>
            <p class="confirm-message">{{ message }}</p>

            <!-- Actions -->
            <div class="confirm-actions">
              <button class="btn-cancel" @click="handleCancel">{{ cancelText }}</button>
              <button class="btn-confirm" :class="variant" @click="handleConfirm">{{ confirmText }}</button>
            </div>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.confirm-overlay {
  position: fixed;
  inset: 0;
  z-index: 10000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0);
  transition: background 0.25s ease;
  padding: 1rem;
}

.confirm-overlay.visible {
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
}

.confirm-card {
  background: #ffffff;
  border-radius: 20px;
  padding: 2rem 1.75rem 1.5rem;
  width: 100%;
  max-width: 380px;
  text-align: center;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15), 0 4px 12px rgba(0, 0, 0, 0.05);
  will-change: transform, opacity;
}

.confirm-icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1rem;
}

.confirm-icon.danger {
  background: #FEE2E2;
  color: #DC2626;
}

.confirm-icon.warning {
  background: #FEF3C7;
  color: #D97706;
}

.confirm-icon.info {
  background: #DBEAFE;
  color: #2563EB;
}

.confirm-title {
  font-size: 1.125rem;
  font-weight: 700;
  color: #1E293B;
  margin-bottom: 0.5rem;
}

.confirm-message {
  font-size: 0.875rem;
  color: #64748B;
  line-height: 1.6;
  margin-bottom: 1.5rem;
}

.confirm-actions {
  display: flex;
  gap: 0.75rem;
}

.btn-cancel,
.btn-confirm {
  flex: 1;
  padding: 0.75rem 1rem;
  border-radius: 12px;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  border: none;
}

.btn-cancel {
  background: #F1F5F9;
  color: #475569;
}

.btn-cancel:hover {
  background: #E2E8F0;
}

.btn-confirm.danger {
  background: #DC2626;
  color: white;
}

.btn-confirm.danger:hover {
  background: #B91C1C;
  box-shadow: 0 4px 14px rgba(220, 38, 38, 0.35);
}

.btn-confirm.warning {
  background: #D97706;
  color: white;
}

.btn-confirm.warning:hover {
  background: #B45309;
  box-shadow: 0 4px 14px rgba(217, 119, 6, 0.35);
}

.btn-confirm.info {
  background: #2563EB;
  color: white;
}

.btn-confirm.info:hover {
  background: #1D4ED8;
  box-shadow: 0 4px 14px rgba(37, 99, 235, 0.35);
}

/* Transitions */
.confirm-overlay-enter-active,
.confirm-overlay-leave-active {
  transition: opacity 0.25s ease;
}

.confirm-overlay-enter-from,
.confirm-overlay-leave-to {
  opacity: 0;
}

.confirm-card-enter-active {
  transition: opacity 0.3s cubic-bezier(0.16, 1, 0.3, 1),
              transform 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.confirm-card-leave-active {
  transition: opacity 0.2s ease,
              transform 0.2s ease;
}

.confirm-card-enter-from {
  opacity: 0;
  transform: scale(0.9) translateY(10px);
}

.confirm-card-leave-to {
  opacity: 0;
  transform: scale(0.95) translateY(5px);
}

@media (max-width: 480px) {
  .confirm-card {
    padding: 1.5rem 1.25rem 1.25rem;
    max-width: 100%;
    border-radius: 16px;
  }
}
</style>
