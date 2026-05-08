<script setup>
import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const displayName = computed(() => authStore.userName || 'User')
const avatarUrl = computed(() => authStore.userAvatar)
const initials = computed(() => {
  const n = displayName.value
  return n.split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2)
})

const showUserMenu = ref(false)
function toggleUserMenu() { showUserMenu.value = !showUserMenu.value }
function closeUserMenu() { showUserMenu.value = false }
function handleLogout() { authStore.logout(); closeUserMenu() }

const isFlipped = ref(false)
function flipCard() {
  isFlipped.value = !isFlipped.value
}
</script>

<template>
  <div class="fc-page" @click="closeUserMenu">
    <!-- Header -->
    <header class="fc-header">
      <button class="btn-new">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="16"/><line x1="8" y1="12" x2="16" y2="12"/></svg>
        Flashcard Baru
      </button>
      <div class="fc-user-area" @click.stop>
        <button class="fc-avatar-btn" @click="toggleUserMenu">
          <div v-if="avatarUrl" class="fc-avatar"><img :src="avatarUrl" :alt="displayName" referrerpolicy="no-referrer"/></div>
          <div v-else class="fc-avatar fc-avatar-initials">{{ initials }}</div>
          <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#64748B" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="m6 9 6 6 6-6"/></svg>
        </button>
        <div v-if="showUserMenu" class="fc-user-dropdown">
          <div class="dd-info"><span class="dd-name">{{ displayName }}</span><span class="dd-email">{{ authStore.userEmail }}</span></div>
          <div class="dd-div"></div>
          <button class="dd-item" @click="handleLogout">Logout</button>
        </div>
      </div>
    </header>

    <div class="fc-content">
      <!-- Progress Bar Section -->
      <div class="fc-progress-section">
        <div class="fc-prog-left">
          <div class="fc-prog-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#3B82F6" stroke-width="2"><ellipse cx="12" cy="5" rx="9" ry="3"/><path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3"/><path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"/></svg>
          </div>
          <div class="fc-prog-text">
            <h2>Database SQL Fundamentals</h2>
            <p>Flashcard Session • Master SQL Commands</p>
          </div>
        </div>
        <div class="fc-prog-right">
          <span class="prog-label">PROGRESS BELAJAR</span>
          <div class="prog-bar-wrap">
            <div class="prog-bar">
              <div class="prog-fill" style="width: 60%"></div>
            </div>
            <span class="prog-count"><strong>6</strong>/10</span>
          </div>
        </div>
      </div>

      <!-- Flashcard 3D Container -->
      <div class="fc-card-container">
        <div class="fc-card" :class="{ flipped: isFlipped }">
          
          <!-- FRONT -->
          <div class="fc-face fc-front" @click="flipCard">
            <div class="badge-pill">PERTANYAAN</div>
            <h1 class="front-q">Apa fungsi dari perintah JOIN dalam SQL?</h1>
            
            <div class="flip-action">
              <button class="btn-flip">
                <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/><path d="M3 3v5h5"/></svg>
              </button>
              <span class="flip-text">Klik untuk melihat jawaban</span>
            </div>
          </div>

          <!-- BACK -->
          <div class="fc-face fc-back" @click="flipCard">
            <div class="verified-badge">Verified</div>
            <div class="back-q-section">
              <div class="back-label">
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#64748B" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"/><line x1="12" y1="17" x2="12.01" y2="17"/></svg>
                PERTANYAAN
              </div>
              <h2 class="back-q">Apa perbedaan mendasar antara INNER JOIN dan LEFT JOIN?</h2>
            </div>

            <div class="back-a-section">
              <div class="back-label success">
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="#10B981" stroke="none"><circle cx="12" cy="12" r="10"/><path d="m9 12 2 2 4-4" stroke="white" stroke-width="2" fill="none" stroke-linecap="round" stroke-linejoin="round"/></svg>
                JAWABAN AI
              </div>
              <div class="back-a-text">
                <p><strong>INNER JOIN</strong> hanya mengembalikan baris yang memiliki kecocokan di kedua tabel. Jika tidak ada kecocokan, data tidak akan ditampilkan.</p>
                <p><strong>LEFT JOIN</strong> mengembalikan <span class="highlight">semua baris</span> dari tabel kiri, dan baris yang cocok dari tabel kanan. Jika tidak ada kecocokan di tabel kanan, hasilnya adalah <strong>NULL</strong>.</p>
              </div>

              <div class="back-boxes">
                <div class="box box-insight">
                  <div class="box-head">
                    <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#3B82F6" stroke-width="2"><path d="M9 18h6"/><path d="M10 22h4"/><path d="M12 2v1"/><path d="M12 7a5 5 0 0 0-4.54 7.07 5.2 5.2 0 0 1-.46 5.93H17a5.2 5.2 0 0 1-.46-5.93A5 5 0 0 0 12 7z"/></svg>
                    Key Insight
                  </div>
                  <p>Gunakan LEFT JOIN saat Anda ingin mempertahankan data utama meskipun data referensinya kosong.</p>
                </div>
                <div class="box box-code">
                  <div class="box-head">
                    <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#3B82F6" stroke-width="2"><polyline points="16 18 22 12 16 6"/><polyline points="8 6 2 12 8 18"/></svg>
                    SQL Snippet
                  </div>
                  <code>SELECT * FROM users LEFT JOIN orders ON users.id = orders.user_id;</code>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- AI Tips -->
      <div class="fc-tips">
        <div class="tips-head">
          <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#3B82F6" stroke-width="2.5"><path d="m12 3-1.912 5.813a2 2 0 0 1-1.275 1.275L3 12l5.813 1.912a2 2 0 0 1 1.275 1.275L12 21l1.912-5.813a2 2 0 0 1 1.275-1.275L21 12l-5.813-1.912a2 2 0 0 1-1.275-1.275L12 3Z"/></svg>
          TIPS AI
        </div>
        <p>"Bayangkan JOIN seperti menggabungkan dua potongan puzzle dari tabel berbeda menggunakan satu warna yang sama."</p>
      </div>

      <!-- Controls -->
      <div class="fc-controls">
        <button class="ctrl-btn btn-prev">
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/></svg>
          Sebelumnya
        </button>
        
        <div class="ctrl-reactions">
          <button class="react-btn react-sad">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#EF4444" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M16 16s-1.5-2-4-2-4 2-4 2"/><line x1="9" y1="9" x2="9.01" y2="9"/><line x1="15" y1="9" x2="15.01" y2="9"/></svg>
          </button>
          <button class="react-btn react-neutral">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#64748B" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="8" y1="15" x2="16" y2="15"/><line x1="9" y1="9" x2="9.01" y2="9"/><line x1="15" y1="9" x2="15.01" y2="9"/></svg>
          </button>
          <button class="react-btn react-happy">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#10B981" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M8 14s1.5 2 4 2 4-2 4-2"/><line x1="9" y1="9" x2="9.01" y2="9"/><line x1="15" y1="9" x2="15.01" y2="9"/></svg>
          </button>
        </div>

        <button class="ctrl-btn btn-next">
          Selanjutnya
          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></svg>
        </button>
      </div>

    </div>
  </div>
</template>

<style scoped>
.fc-page {
  min-height: 100vh;
  background: #F8FAFC;
  color: #1E293B;
  display: flex;
  flex-direction: column;
}

/* Header */
.fc-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 2rem;
  background: #FFFFFF;
  border-bottom: 1px solid #E2E8F0;
}
.btn-new {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  background: #0052CC;
  color: #FFFFFF;
  padding: 0.625rem 1rem;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-new:hover { background: #0043A6; }

.fc-user-area { position: relative; }
.fc-avatar-btn { display: flex; align-items: center; gap: 0.5rem; background: none; border: none; cursor: pointer; padding: 4px; border-radius: 10px; transition: background 0.2s; }
.fc-avatar-btn:hover { background: #F1F5F9; }
.fc-avatar { width: 36px; height: 36px; border-radius: 10px; overflow: hidden; border: 2px solid #E2E8F0; }
.fc-avatar img { width: 100%; height: 100%; object-fit: cover; }
.fc-avatar-initials { display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #3B82F6, #8B5CF6); color: white; font-size: 0.75rem; font-weight: 700; }
.fc-user-dropdown { position: absolute; right: 0; top: calc(100% + 8px); width: 220px; background: white; border: 1px solid #E2E8F0; border-radius: 12px; box-shadow: 0 10px 40px rgba(0,0,0,0.1); padding: 0.5rem; z-index: 60; animation: dropIn 0.2s ease; }
@keyframes dropIn { from { opacity: 0; transform: translateY(-6px); } to { opacity: 1; transform: translateY(0); } }
.dd-info { padding: 0.625rem 0.75rem; }
.dd-name { display: block; font-size: 0.875rem; font-weight: 600; color: #1E293B; }
.dd-email { display: block; font-size: 0.75rem; color: #94A3B8; margin-top: 2px; }
.dd-div { height: 1px; background: #F1F5F9; margin: 0.25rem 0; }
.dd-item { display: flex; align-items: center; gap: 0.5rem; width: 100%; padding: 0.5rem 0.75rem; background: none; border: none; border-radius: 8px; font-size: 0.8125rem; font-weight: 500; color: #EF4444; cursor: pointer; transition: background 0.15s; }
.dd-item:hover { background: #FEF2F2; }

/* Content Wrapper */
.fc-content {
  flex: 1;
  max-width: 900px;
  width: 100%;
  margin: 0 auto;
  padding: 2rem 2rem 4rem;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* Progress Section */
.fc-progress-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: transparent;
}
.fc-prog-left { display: flex; align-items: center; gap: 1rem; }
.fc-prog-icon { width: 44px; height: 44px; background: #EFF6FF; border-radius: 12px; display: flex; align-items: center; justify-content: center; }
.fc-prog-text h2 { font-size: 1.25rem; font-weight: 700; color: #0F172A; margin-bottom: 0.25rem; }
.fc-prog-text p { font-size: 0.8125rem; color: #64748B; font-weight: 500; }
.fc-prog-right { display: flex; flex-direction: column; align-items: flex-end; gap: 0.5rem; }
.prog-label { font-size: 0.6875rem; font-weight: 800; letter-spacing: 0.05em; color: #64748B; text-transform: uppercase; }
.prog-bar-wrap { display: flex; align-items: center; gap: 0.75rem; }
.prog-bar { width: 140px; height: 8px; background: #E2E8F0; border-radius: 4px; overflow: hidden; }
.prog-fill { height: 100%; background: #10B981; border-radius: 4px; }
.prog-count { font-size: 0.875rem; color: #64748B; }
.prog-count strong { color: #0F172A; }

/* Flashcard 3D */
.fc-card-container {
  perspective: 1000px;
  width: 100%;
  height: 420px;
  margin: 1rem 0;
}
.fc-card {
  width: 100%;
  height: 100%;
  position: relative;
  transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  transform-style: preserve-3d;
  cursor: pointer;
}
.fc-card.flipped { transform: rotateY(180deg); }

.fc-face {
  position: absolute;
  width: 100%;
  height: 100%;
  backface-visibility: hidden;
  background: #FFFFFF;
  border-radius: 20px;
  box-shadow: 0 10px 40px rgba(0,0,0,0.04);
  border: 1px solid #E2E8F0;
  display: flex;
  flex-direction: column;
  padding: 3rem;
}
.fc-back {
  transform: rotateY(180deg);
  padding: 2.5rem 3rem;
}

/* Front Face */
.fc-front {
  align-items: center;
  justify-content: center;
  text-align: center;
}
.badge-pill {
  background: #DBEAFE;
  color: #2563EB;
  font-size: 0.75rem;
  font-weight: 700;
  padding: 0.375rem 0.875rem;
  border-radius: 20px;
  letter-spacing: 0.05em;
  margin-bottom: 2.5rem;
}
.front-q {
  font-size: 2.25rem;
  font-weight: 800;
  color: #0F172A;
  line-height: 1.3;
  margin-bottom: 3rem;
  max-width: 80%;
}
.flip-action {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
}
.btn-flip {
  width: 48px;
  height: 48px;
  background: #3B82F6;
  color: white;
  border: none;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(59,130,246,0.3);
  transition: all 0.2s;
  cursor: pointer;
}
.btn-flip:hover { background: #2563EB; transform: scale(1.05); }
.flip-text { font-size: 0.8125rem; color: #3B82F6; font-weight: 600; }

/* Back Face */
.verified-badge {
  position: absolute;
  top: 1.5rem;
  right: 1.5rem;
  background: #ECFDF5;
  color: #10B981;
  font-weight: 700;
  font-size: 0.75rem;
  padding: 0.375rem 0.875rem;
  border-radius: 6px;
  border: 1px solid #A7F3D0;
  transform: rotate(5deg);
}

.back-q-section { border-bottom: 1px solid #F1F5F9; padding-bottom: 1.5rem; margin-bottom: 1.5rem; }
.back-label { display: flex; align-items: center; gap: 0.5rem; font-size: 0.6875rem; font-weight: 800; color: #64748B; letter-spacing: 0.06em; margin-bottom: 0.75rem; }
.back-label.success { color: #10B981; }
.back-q { font-size: 1.25rem; font-weight: 700; color: #0F172A; line-height: 1.4; }

.back-a-section { flex: 1; display: flex; flex-direction: column; }
.back-a-text { font-size: 0.9375rem; color: #475569; line-height: 1.6; margin-bottom: 1.5rem; }
.back-a-text p { margin-bottom: 0.75rem; }
.back-a-text .highlight { background: #E0E7FF; color: #4338CA; padding: 0.125rem 0.375rem; border-radius: 4px; font-weight: 600; }

.back-boxes { display: flex; gap: 1rem; margin-top: auto; }
.box { flex: 1; background: #F8FAFC; border: 1px solid #E2E8F0; border-radius: 12px; padding: 1rem 1.25rem; }
.box-head { display: flex; align-items: center; gap: 0.5rem; font-size: 0.75rem; font-weight: 700; color: #3B82F6; margin-bottom: 0.5rem; }
.box-insight p { font-size: 0.8125rem; color: #475569; line-height: 1.5; margin: 0; }
.box-code code { font-family: monospace; font-size: 0.8125rem; color: #2563EB; background: transparent; padding: 0; }

/* AI Tips */
.fc-tips {
  background: #FFFFFF;
  border: 1px solid #E2E8F0;
  border-radius: 16px;
  padding: 1.25rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  box-shadow: 0 4px 16px rgba(0,0,0,0.02);
}
.tips-head { display: flex; align-items: center; gap: 0.375rem; font-size: 0.75rem; font-weight: 800; color: #3B82F6; letter-spacing: 0.05em; margin-bottom: 0.5rem; }
.fc-tips p { font-size: 0.875rem; color: #64748B; font-style: italic; margin: 0; }

/* Controls */
.fc-controls {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 1rem;
}
.ctrl-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  border-radius: 10px;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-prev { background: #FFFFFF; border: 1px solid #CBD5E1; color: #475569; }
.btn-prev:hover { background: #F1F5F9; }
.btn-next { background: #0052CC; border: none; color: white; box-shadow: 0 4px 12px rgba(0,82,204,0.25); }
.btn-next:hover { background: #0043A6; transform: translateY(-1px); }

.ctrl-reactions { display: flex; gap: 0.75rem; }
.react-btn {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}
.react-sad { background: #FEF2F2; border: 1px solid #FECACA; color: #EF4444; }
.react-sad:hover { background: #FEE2E2; transform: scale(1.05); }
.react-neutral { background: #F1F5F9; border: 1px solid #E2E8F0; color: #64748B; }
.react-neutral:hover { background: #E2E8F0; transform: scale(1.05); }
.react-happy { background: #ECFDF5; border: 1px solid #A7F3D0; color: #10B981; }
.react-happy:hover { background: #D1FAE5; transform: scale(1.05); }

@media (max-width: 768px) {
  .fc-content { padding: 1.5rem 1rem; }
  .fc-progress-section { flex-direction: column; align-items: flex-start; gap: 1rem; }
  .fc-prog-right { align-items: flex-start; }
  .front-q { font-size: 1.5rem; max-width: 100%; }
  .fc-face { padding: 1.5rem; }
  .back-boxes { flex-direction: column; gap: 0.5rem; }
  .fc-controls { flex-direction: column; gap: 1rem; }
  .ctrl-btn { width: 100%; justify-content: center; }
}
</style>
