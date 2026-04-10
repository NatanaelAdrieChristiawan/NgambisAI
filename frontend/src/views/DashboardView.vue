<script setup>
import { useAuthStore } from '@/stores/auth'
import { computed } from 'vue'

const authStore = useAuthStore()
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 12) return 'Selamat Pagi'
  if (hour < 17) return 'Selamat Siang'
  return 'Selamat Malam'
})
</script>

<template>
  <div class="dashboard-page">
    <div class="container">
      <!-- Welcome Section -->
      <section class="welcome-section">
        <div class="welcome-content">
          <h1>{{ greeting }}, <span class="accent">{{ authStore.userName }}</span> 👋</h1>
          <p>Siap belajar hari ini? Upload materi PDF dan mulai latihan ujian.</p>
        </div>
      </section>

      <!-- Quick Actions -->
      <section class="quick-actions">
        <h2 class="section-title">Mulai Cepat</h2>
        <div class="actions-grid">
          <router-link to="/upload" class="action-card glass-card">
            <div class="action-icon">📄</div>
            <h3>Upload PDF</h3>
            <p>Upload materi kuliah untuk generate quiz</p>
          </router-link>

          <router-link to="/quiz" class="action-card glass-card">
            <div class="action-icon">❓</div>
            <h3>Quiz Session</h3>
            <p>Mulai sesi quiz dari materi yang sudah diupload</p>
          </router-link>

          <router-link to="/simulator" class="action-card glass-card">
            <div class="action-icon">🎤</div>
            <h3>Simulator Ujian Lisan</h3>
            <p>Latihan ujian lisan dengan AI penguji</p>
          </router-link>

          <router-link to="/history" class="action-card glass-card">
            <div class="action-icon">📋</div>
            <h3>Riwayat</h3>
            <p>Lihat riwayat nilai dan evaluasi</p>
          </router-link>
        </div>
      </section>

      <!-- Stats Preview -->
      <section class="stats-section">
        <h2 class="section-title">Statistik</h2>
        <div class="stats-grid">
          <div class="stat-card glass-card">
            <div class="stat-value">—</div>
            <div class="stat-label">Documents</div>
          </div>
          <div class="stat-card glass-card">
            <div class="stat-value">—</div>
            <div class="stat-label">Quiz Sessions</div>
          </div>
          <div class="stat-card glass-card">
            <div class="stat-value">—</div>
            <div class="stat-label">Avg. Score</div>
          </div>
          <div class="stat-card glass-card">
            <div class="stat-value">—</div>
            <div class="stat-label">Evaluations</div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.dashboard-page {
  padding: var(--space-2xl) 0;
}

.welcome-section {
  margin-bottom: var(--space-2xl);
}

.welcome-content h1 {
  font-size: var(--font-size-3xl);
  font-weight: 700;
  margin-bottom: 0.5rem;
}

.welcome-content p {
  color: var(--color-text-secondary);
  font-size: var(--font-size-lg);
}

.accent { color: var(--color-primary-light); }

.section-title {
  font-size: var(--font-size-xl);
  font-weight: 600;
  margin-bottom: var(--space-lg);
  color: var(--color-text-primary);
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: var(--space-lg);
  margin-bottom: var(--space-2xl);
}

.action-card {
  padding: var(--space-xl);
  text-decoration: none;
  color: var(--color-text-primary);
  transition: all var(--transition-base);
  cursor: pointer;
}

.action-card:hover {
  background: var(--color-bg-card-hover);
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.3);
}

.action-icon {
  font-size: 2.5rem;
  margin-bottom: var(--space-md);
}

.action-card h3 {
  font-size: var(--font-size-lg);
  font-weight: 600;
  margin-bottom: var(--space-xs);
}

.action-card p {
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
  line-height: 1.5;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: var(--space-md);
}

.stat-card {
  padding: var(--space-lg);
  text-align: center;
}

.stat-value {
  font-size: var(--font-size-3xl);
  font-weight: 700;
  color: var(--color-primary-light);
  margin-bottom: var(--space-xs);
}

.stat-label {
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
  font-weight: 500;
}

@media (max-width: 640px) {
  .welcome-content h1 { font-size: var(--font-size-2xl); }
  .actions-grid { grid-template-columns: 1fr; }
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
