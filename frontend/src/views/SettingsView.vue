<script setup>
import { ref, computed, onMounted, inject } from 'vue'
import { useAuthStore } from '@/stores/auth'
import userApi from '@/services/user.api'

const authStore = useAuthStore()
const toggleSidebar = inject('toggleSidebar', () => {})

const displayName = computed(() => authStore.userName || 'User')
const avatarUrl = computed(() => authStore.userAvatar)
const initials = computed(() => displayName.value.split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2))

const editName = ref('')
const editUsername = ref('')
const saving = ref(false)
const saveSuccess = ref(false)
const saveError = ref(null)
const loading = ref(true)

const provider = computed(() => authStore.user?.provider || 'LOCAL')

async function loadProfile() {
  loading.value = true
  try {
    const resp = await userApi.getUser(authStore.user.id)
    const data = resp.data.data || resp.data
    editName.value = data.name || ''
    editUsername.value = data.username || ''
  } catch { /* fallback to store data */
    editName.value = authStore.userName || ''
    editUsername.value = authStore.user?.username || ''
  } finally {
    loading.value = false
  }
}

async function saveProfile() {
  saving.value = true
  saveError.value = null
  saveSuccess.value = false
  try {
    await userApi.updateProfile(authStore.user.id, {
      name: editName.value.trim(),
      username: editUsername.value.trim()
    })
    saveSuccess.value = true
    // Update local auth store
    if (authStore.user) {
      authStore.user.name = editName.value.trim()
      authStore.user.username = editUsername.value.trim()
    }
    setTimeout(() => { saveSuccess.value = false }, 3000)
  } catch (err) {
    saveError.value = err.response?.data?.message || 'Gagal menyimpan profil.'
  } finally {
    saving.value = false
  }
}

onMounted(() => { loadProfile() })
</script>

<template>
  <div class="st-page">
    <header class="st-header">
      <div class="st-header-left">
        <button class="burger-btn" @click.stop="toggleSidebar" aria-label="Menu">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="12" x2="21" y2="12"/><line x1="3" y1="18" x2="21" y2="18"/></svg>
        </button>
        <h1 class="st-title">⚙️ Settings</h1>
      </div>
    </header>

    <div class="st-content">
      <!-- Profile Section -->
      <div class="st-card">
        <div class="profile-header">
          <div v-if="avatarUrl" class="profile-avatar"><img :src="avatarUrl" :alt="displayName" referrerpolicy="no-referrer"/></div>
          <div v-else class="profile-avatar profile-avatar-initials">{{ initials }}</div>
          <div class="profile-info">
            <h2>{{ displayName }}</h2>
            <p>{{ authStore.userEmail }}</p>
            <span class="provider-badge" :class="provider.toLowerCase()">
              <svg v-if="provider==='GOOGLE'" xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 0 1-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z"/><path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/><path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/><path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/></svg>
              {{ provider === 'GOOGLE' ? 'Google Account' : 'Local Account' }}
            </span>
          </div>
        </div>
      </div>

      <!-- Edit Form -->
      <div class="st-card" v-if="!loading">
        <h3 class="card-title">Edit Profil</h3>
        <div class="form-group">
          <label>Nama Lengkap</label>
          <input v-model="editName" type="text" placeholder="Masukkan nama lengkap" class="form-input"/>
        </div>
        <div class="form-group">
          <label>Username</label>
          <input v-model="editUsername" type="text" placeholder="Masukkan username" class="form-input"/>
        </div>
        <div class="form-group">
          <label>Email</label>
          <input :value="authStore.userEmail" type="email" disabled class="form-input disabled"/>
          <span class="form-hint">Email tidak dapat diubah.</span>
        </div>

        <div v-if="saveSuccess" class="form-success">✅ Profil berhasil disimpan!</div>
        <div v-if="saveError" class="form-error">⚠️ {{ saveError }}</div>

        <div class="form-actions">
          <button class="btn-save" @click="saveProfile" :disabled="saving">{{ saving ? 'Menyimpan...' : '💾 Simpan Perubahan' }}</button>
        </div>
      </div>

      <div class="st-card" v-else>
        <div class="loading-state"><div class="loading-spinner"></div><span>Memuat profil...</span></div>
      </div>

      <!-- Danger Zone -->
      <div class="st-card danger-zone">
        <div class="danger-action-list">
          <div class="danger-action-item">
            <div class="danger-action-info">
              <h4>Keluar Aplikasi</h4>
              <p>Mengakhiri sesi Anda saat ini. Anda perlu login kembali untuk mengakses data Anda.</p>
            </div>
            <button class="btn-danger" @click="authStore.logout()">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
              Logout
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.st-page { min-height:100vh; background:#F8FAFC; }
.st-header { display:flex; align-items:center; padding:1rem 2rem; background:#FFF; border-bottom:1px solid #E2E8F0; }
.st-header-left { display:flex; align-items:center; gap:.75rem; }
.st-title { font-size:1.125rem; font-weight:700; color:#0F172A; }

.st-content { max-width:640px; margin:0 auto; padding:2rem; display:flex; flex-direction:column; gap:1.5rem; }
.st-card { background:#fff; border:1px solid #E2E8F0; border-radius:16px; padding:2rem; box-shadow:0 2px 12px rgba(0,0,0,.03); }
.card-title { font-size:1rem; font-weight:700; color:#0F172A; margin-bottom:1.25rem; }

/* Profile header */
.profile-header { display:flex; align-items:center; gap:1.25rem; }
.profile-avatar { width:72px; height:72px; border-radius:16px; overflow:hidden; border:3px solid #E2E8F0; flex-shrink:0; }
.profile-avatar img { width:100%; height:100%; object-fit:cover; }
.profile-avatar-initials { display:flex; align-items:center; justify-content:center; background:linear-gradient(135deg,#3B82F6,#8B5CF6); color:#fff; font-size:1.25rem; font-weight:800; }
.profile-info h2 { font-size:1.25rem; font-weight:800; color:#0F172A; margin-bottom:.25rem; }
.profile-info p { font-size:.875rem; color:#64748B; margin-bottom:.5rem; }
.provider-badge { display:inline-flex; align-items:center; gap:.375rem; font-size:.6875rem; font-weight:700; padding:.25rem .625rem; border-radius:6px; }
.provider-badge.google { background:#FEF3C7; color:#B45309; }
.provider-badge.local { background:#DBEAFE; color:#1D4ED8; }

/* Form */
.form-group { margin-bottom:1.25rem; }
.form-group label { display:block; font-size:.8125rem; font-weight:700; color:#475569; margin-bottom:.5rem; }
.form-input { width:100%; padding:.75rem 1rem; border:1px solid #E2E8F0; border-radius:10px; font-size:.9375rem; color:#1E293B; background:#F8FAFC; transition:all .2s; outline:none; }
.form-input:focus { border-color:#3B82F6; box-shadow:0 0 0 3px rgba(59,130,246,.1); background:#fff; }
.form-input.disabled { background:#F1F5F9; color:#94A3B8; cursor:not-allowed; }
.form-hint { font-size:.6875rem; color:#94A3B8; margin-top:.375rem; display:block; }
.form-success { padding:.75rem; background:#ECFDF5; color:#065F46; border-radius:8px; font-size:.8125rem; font-weight:600; margin-bottom:1rem; }
.form-error { padding:.75rem; background:#FEF2F2; color:#DC2626; border-radius:8px; font-size:.8125rem; margin-bottom:1rem; }
.form-actions { display:flex; justify-content:flex-end; }
.btn-save { padding:.75rem 1.5rem; background:#3B82F6; color:#fff; border:none; border-radius:10px; font-size:.875rem; font-weight:600; cursor:pointer; transition:all .2s; box-shadow:0 4px 12px rgba(59,130,246,.25); }
.btn-save:hover { background:#2563EB; } .btn-save:disabled { opacity:.5; cursor:default; }

/* Loading */
.loading-state { display:flex; align-items:center; justify-content:center; gap:1rem; padding:2rem; color:#94A3B8; font-size:.875rem; }
.loading-spinner { width:24px; height:24px; border:3px solid #E2E8F0; border-top-color:#3B82F6; border-radius:50%; animation:spin .8s linear infinite; }
@keyframes spin { to { transform:rotate(360deg); } }

/* Danger zone */
.danger-zone { border: 1px solid #FECACA; background: #FEF2F2; padding: 0; overflow: hidden; margin-top: 0.5rem; }
.danger-header { display: flex; align-items: center; gap: 0.5rem; padding: 1.5rem 1.5rem 1rem 1.5rem; }
.danger-icon { color: #DC2626; }
.danger-title { font-size: 1rem; font-weight: 700; color: #991B1B; margin: 0; }
.danger-action-list { }
.danger-action-item { display: flex; align-items: center; justify-content: space-between; padding: 1.25rem 1.5rem; gap: 1rem; background: #FFF5F5; }
.danger-action-info h4 { font-size: .875rem; font-weight: 700; color: #7F1D1D; margin: 0 0 .25rem 0; }
.danger-action-info p { font-size: .8125rem; color: #991B1B; margin: 0; line-height: 1.5; }
.btn-danger { display:inline-flex; align-items:center; gap:.5rem; padding:.625rem 1rem; background:#fff; color:#DC2626; border:1px solid #FECACA; border-radius:8px; font-size:.875rem; font-weight:600; cursor:pointer; transition:all .2s; box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05); white-space: nowrap; flex-shrink: 0; }
.btn-danger:hover { background:#DC2626; color:#fff; border-color:#DC2626; box-shadow: 0 4px 6px -1px rgba(220, 38, 38, 0.2); }

@media (max-width:768px) {
  .st-header { padding:.75rem 1rem; }
  .st-content { padding:1.5rem 1rem; }
  .profile-header { flex-direction:column; align-items:flex-start; }
  .st-card { padding:1.5rem; }
  .danger-header { padding: 1.25rem 1.25rem 0.75rem 1.25rem; }
  .danger-action-item { flex-direction: column; align-items: flex-start; padding: 1.25rem; }
  .btn-danger { width: 100%; justify-content: center; margin-top: .5rem; }
}
</style>
