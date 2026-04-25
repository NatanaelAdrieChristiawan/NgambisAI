<script setup>
/**
 * DashboardView.vue
 *
 * Main landing page — accessible without authentication.
 * Shows a welcoming hero section and feature cards.
 * When authenticated, shows personalized greeting + stats.
 * When not authenticated, clicking feature cards triggers auth modal.
 */
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import { computed, ref, onMounted } from 'vue'

const authStore = useAuthStore()
const router = useRouter()

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 12) return 'Selamat Pagi'
  if (hour < 17) return 'Selamat Siang'
  return 'Selamat Malam'
})

const isLoggedIn = computed(() => authStore.isAuthenticated)

// Features / quick actions
const features = [
  {
    icon: '📄',
    title: 'Upload PDF',
    description: 'Upload materi kuliah untuk generate quiz dan simulator ujian secara otomatis.',
    route: '/upload',
    gradient: 'linear-gradient(135deg, #6c63ff 0%, #574fd9 100%)',
    iconBg: 'rgba(108, 99, 255, 0.15)'
  },
  {
    icon: '❓',
    title: 'Quiz Session',
    description: 'Mulai sesi quiz pilihan ganda dan essay dari materi yang sudah diupload.',
    route: '/quiz',
    gradient: 'linear-gradient(135deg, #00d2ff 0%, #3b82f6 100%)',
    iconBg: 'rgba(0, 210, 255, 0.15)'
  },
  {
    icon: '🎤',
    title: 'Simulator Ujian Lisan',
    description: 'Latihan ujian lisan dengan AI sebagai dosen penguji yang interaktif.',
    route: '/simulator',
    gradient: 'linear-gradient(135deg, #7c3aed 0%, #a855f7 100%)',
    iconBg: 'rgba(124, 58, 237, 0.15)'
  },
  {
    icon: '📋',
    title: 'Riwayat',
    description: 'Lihat riwayat nilai, evaluasi, dan progress belajar kamu.',
    route: '/history',
    gradient: 'linear-gradient(135deg, #10b981 0%, #059669 100%)',
    iconBg: 'rgba(16, 185, 129, 0.15)'
  }
]

function handleFeatureClick(feature) {
  if (isLoggedIn.value) {
    router.push(feature.route)
  } else {
    authStore.requestAuth(
      `Login terlebih dahulu untuk mengakses ${feature.title}.`,
      feature.route
    )
  }
}

// Entrance animation
const isReady = ref(false)
onMounted(() => {
  requestAnimationFrame(() => {
    isReady.value = true
  })
})
</script>

<template>
  <div class="dashboard-page" :class="{ ready: isReady }">
    <!-- Background decoration -->
    <div class="bg-decoration">
      <div class="bg-orb bg-orb-1"></div>
      <div class="bg-orb bg-orb-2"></div>
      <div class="bg-orb bg-orb-3"></div>
    </div>

    <div class="container">
      <!-- Hero / Welcome Section -->
      <section class="hero-section">
        <div class="hero-content">
          <!-- Logged-in greeting -->
          <template v-if="isLoggedIn">
            <div class="hero-badge">
              <span class="badge-dot"></span>
              Online
            </div>
            <h1 class="hero-title">
              {{ greeting }}, <span class="accent">{{ authStore.userName }}</span> 👋
            </h1>
            <p class="hero-subtitle">
              Siap belajar hari ini? Upload materi PDF dan mulai latihan ujian dengan bantuan AI.
            </p>
          </template>

          <!-- Guest welcome -->
          <template v-else>
            <div class="hero-badge">
              <span class="badge-icon">✨</span>
              AI-Powered Learning
            </div>
            <h1 class="hero-title">
              Belajar Lebih Cerdas <br/>dengan <span class="accent">Ngambis.ai</span>
            </h1>
            <p class="hero-subtitle">
              Upload materi kuliah dalam PDF, generate quiz secara otomatis, dan latihan ujian lisan 
              dengan AI sebagai dosen penguji. Mulai sekarang — gratis!
            </p>
            <button class="hero-cta" @click="authStore.requestAuth('Buat akun atau masuk untuk mulai belajar.', '/')">
              <span>Mulai Sekarang</span>
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24"
                   fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M5 12h14" /><path d="m12 5 7 7-7 7" />
              </svg>
            </button>
          </template>
        </div>

        <!-- Hero visual (right side) — decorative illustration-like shapes -->
        <div class="hero-visual">
          <div class="visual-card visual-card-1 glass-card">
            <div class="visual-card-icon">📄</div>
            <div class="visual-card-text">
              <span class="visual-card-title">Materi PDF</span>
              <span class="visual-card-sub">Uploaded successfully</span>
            </div>
          </div>
          <div class="visual-card visual-card-2 glass-card">
            <div class="visual-card-icon">🤖</div>
            <div class="visual-card-text">
              <span class="visual-card-title">AI Quiz Ready</span>
              <span class="visual-card-sub">15 soal generated</span>
            </div>
          </div>
          <div class="visual-card visual-card-3 glass-card">
            <div class="visual-card-icon">🎯</div>
            <div class="visual-card-text">
              <span class="visual-card-title">Score: 92/100</span>
              <span class="visual-card-sub">Great performance!</span>
            </div>
          </div>
        </div>
      </section>

      <!-- Features / Quick Actions -->
      <section class="features-section">
        <div class="section-header">
          <h2 class="section-title">
            {{ isLoggedIn ? 'Mulai Cepat' : 'Fitur Utama' }}
          </h2>
          <p class="section-subtitle">
            {{ isLoggedIn 
              ? 'Pilih fitur untuk mulai belajar.' 
              : 'Semua yang kamu butuhkan untuk persiapan ujian.' 
            }}
          </p>
        </div>

        <div class="features-grid">
          <button
            v-for="(feature, index) in features"
            :key="feature.route"
            class="feature-card glass-card"
            :style="{ '--delay': `${index * 0.08}s` }"
            @click="handleFeatureClick(feature)"
          >
            <div class="feature-icon-wrapper" :style="{ background: feature.iconBg }">
              <span class="feature-icon">{{ feature.icon }}</span>
            </div>
            <div class="feature-body">
              <h3 class="feature-title">{{ feature.title }}</h3>
              <p class="feature-desc">{{ feature.description }}</p>
            </div>
            <div class="feature-arrow">
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24"
                   fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="m9 18 6-6-6-6"/>
              </svg>
            </div>
            <!-- Lock indicator for guests -->
            <div v-if="!isLoggedIn" class="feature-lock">
              <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24"
                   fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
              </svg>
            </div>
          </button>
        </div>
      </section>

      <!-- Stats Preview (only for authenticated users) -->
      <section v-if="isLoggedIn" class="stats-section">
        <h2 class="section-title">Statistik</h2>
        <div class="stats-grid">
          <div class="stat-card glass-card">
            <div class="stat-icon">📚</div>
            <div class="stat-value">—</div>
            <div class="stat-label">Documents</div>
          </div>
          <div class="stat-card glass-card">
            <div class="stat-icon">📝</div>
            <div class="stat-value">—</div>
            <div class="stat-label">Quiz Sessions</div>
          </div>
          <div class="stat-card glass-card">
            <div class="stat-icon">🏆</div>
            <div class="stat-value">—</div>
            <div class="stat-label">Avg. Score</div>
          </div>
          <div class="stat-card glass-card">
            <div class="stat-icon">📊</div>
            <div class="stat-value">—</div>
            <div class="stat-label">Evaluations</div>
          </div>
        </div>
      </section>

      <!-- Info section for guests -->
      <section v-if="!isLoggedIn" class="info-section">
        <div class="info-grid">
          <div class="info-card glass-card">
            <div class="info-number">01</div>
            <h3>Upload Materi</h3>
            <p>Upload file PDF materi kuliah kamu. AI akan menganalisis dan memahami konten secara mendalam.</p>
          </div>
          <div class="info-card glass-card">
            <div class="info-number">02</div>
            <h3>Generate Quiz</h3>
            <p>AI membuat soal pilihan ganda dan essay berdasarkan materi. Quiz disesuaikan dengan tingkat kesulitan.</p>
          </div>
          <div class="info-card glass-card">
            <div class="info-number">03</div>
            <h3>Latihan Ujian</h3>
            <p>Simulasikan ujian lisan dengan AI yang berperan sebagai dosen penguji. Dapatkan feedback langsung.</p>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
/* ===== Page Layout ===== */
.dashboard-page {
  padding: var(--space-xl) 0 var(--space-3xl);
  position: relative;
  overflow: hidden;
  min-height: 100vh;
}

/* ===== Background Decoration ===== */
.bg-decoration {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 0;
}

.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(120px);
  opacity: 0.4;
}

.bg-orb-1 {
  width: 500px;
  height: 500px;
  background: rgba(108, 99, 255, 0.15);
  top: -100px;
  right: -100px;
  animation: orbFloat1 20s ease-in-out infinite;
}

.bg-orb-2 {
  width: 400px;
  height: 400px;
  background: rgba(0, 210, 255, 0.1);
  bottom: -50px;
  left: -100px;
  animation: orbFloat2 25s ease-in-out infinite;
}

.bg-orb-3 {
  width: 300px;
  height: 300px;
  background: rgba(124, 58, 237, 0.1);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation: orbFloat3 18s ease-in-out infinite;
}

@keyframes orbFloat1 {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(-40px, 30px); }
}

@keyframes orbFloat2 {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(30px, -40px); }
}

@keyframes orbFloat3 {
  0%, 100% { transform: translate(-50%, -50%) scale(1); }
  50% { transform: translate(-50%, -50%) scale(1.15); }
}

.container {
  position: relative;
  z-index: 1;
}

/* ===== Hero Section ===== */
.hero-section {
  display: flex;
  align-items: center;
  gap: var(--space-3xl);
  padding: var(--space-xl) 0 var(--space-3xl);
  min-height: 320px;
}

.hero-content {
  flex: 1;
  opacity: 0;
  transform: translateY(20px);
  transition: opacity 0.6s ease, transform 0.6s ease;
}

.dashboard-page.ready .hero-content {
  opacity: 1;
  transform: translateY(0);
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.375rem 0.875rem;
  background: rgba(108, 99, 255, 0.12);
  border: 1px solid rgba(108, 99, 255, 0.2);
  border-radius: var(--radius-full);
  font-size: var(--font-size-xs);
  font-weight: 600;
  color: var(--color-primary-light);
  margin-bottom: var(--space-lg);
  letter-spacing: 0.02em;
}

.badge-dot {
  width: 8px;
  height: 8px;
  background: var(--color-success);
  border-radius: 50%;
  animation: pulse 2s ease-in-out infinite;
}

.badge-icon {
  font-size: 0.875rem;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.hero-title {
  font-size: clamp(1.75rem, 4vw, 2.75rem);
  font-weight: 800;
  line-height: 1.2;
  margin-bottom: var(--space-md);
  letter-spacing: -0.02em;
}

.accent {
  background: linear-gradient(135deg, var(--color-primary-light), var(--color-secondary));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.hero-subtitle {
  color: var(--color-text-secondary);
  font-size: var(--font-size-lg);
  line-height: 1.7;
  max-width: 520px;
  margin-bottom: var(--space-xl);
}

.hero-cta {
  display: inline-flex;
  align-items: center;
  gap: 0.625rem;
  padding: 0.875rem 1.75rem;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  color: white;
  border: none;
  border-radius: var(--radius-md);
  font-size: var(--font-size-base);
  font-weight: 600;
  cursor: pointer;
  transition: all var(--transition-fast);
  box-shadow: 0 4px 20px var(--color-primary-glow);
}

.hero-cta:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 30px var(--color-primary-glow);
}

.hero-cta svg {
  transition: transform var(--transition-fast);
}

.hero-cta:hover svg {
  transform: translateX(4px);
}

/* ===== Hero Visual (Right Side) ===== */
.hero-visual {
  flex: 0 0 340px;
  position: relative;
  height: 300px;
  opacity: 0;
  transform: translateY(20px) translateX(10px);
  transition: opacity 0.6s ease 0.2s, transform 0.6s ease 0.2s;
}

.dashboard-page.ready .hero-visual {
  opacity: 1;
  transform: translateY(0) translateX(0);
}

.visual-card {
  position: absolute;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.875rem 1.125rem;
  border-radius: var(--radius-lg);
  min-width: 220px;
  transition: transform var(--transition-base);
}

.visual-card:hover {
  transform: scale(1.04);
}

.visual-card-1 {
  top: 10px;
  left: 10px;
  animation: floatCard1 6s ease-in-out infinite;
}

.visual-card-2 {
  top: 100px;
  right: 0;
  animation: floatCard2 7s ease-in-out infinite;
  animation-delay: 0.5s;
}

.visual-card-3 {
  bottom: 20px;
  left: 30px;
  animation: floatCard3 8s ease-in-out infinite;
  animation-delay: 1s;
}

@keyframes floatCard1 {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

@keyframes floatCard2 {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

@keyframes floatCard3 {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}

.visual-card-icon {
  font-size: 1.75rem;
  flex-shrink: 0;
}

.visual-card-text {
  display: flex;
  flex-direction: column;
}

.visual-card-title {
  font-size: var(--font-size-sm);
  font-weight: 600;
  color: var(--color-text-primary);
}

.visual-card-sub {
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
}

/* ===== Section Headers ===== */
.section-header {
  margin-bottom: var(--space-xl);
}

.section-title {
  font-size: var(--font-size-xl);
  font-weight: 700;
  margin-bottom: 0.25rem;
  color: var(--color-text-primary);
}

.section-subtitle {
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

/* ===== Features Grid ===== */
.features-section {
  margin-bottom: var(--space-3xl);
  opacity: 0;
  transform: translateY(20px);
  transition: opacity 0.6s ease 0.3s, transform 0.6s ease 0.3s;
}

.dashboard-page.ready .features-section {
  opacity: 1;
  transform: translateY(0);
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--space-lg);
}

.feature-card {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: var(--space-md);
  padding: var(--space-xl);
  text-decoration: none;
  color: var(--color-text-primary);
  transition: all var(--transition-base);
  cursor: pointer;
  text-align: left;
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-xl);
  background: var(--glass-bg);
  animation: cardFadeIn 0.5s ease both;
  animation-delay: var(--delay, 0s);
  overflow: hidden;
}

.feature-card::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: inherit;
  background: linear-gradient(135deg, rgba(108, 99, 255, 0.05), transparent);
  opacity: 0;
  transition: opacity var(--transition-base);
}

.feature-card:hover {
  border-color: rgba(255, 255, 255, 0.15);
  transform: translateY(-4px);
  box-shadow: 0 16px 50px rgba(0, 0, 0, 0.3);
}

.feature-card:hover::before {
  opacity: 1;
}

@keyframes cardFadeIn {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.feature-icon-wrapper {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-md);
  flex-shrink: 0;
}

.feature-icon {
  font-size: 1.5rem;
}

.feature-body {
  flex: 1;
  min-width: 0;
}

.feature-title {
  font-size: var(--font-size-base);
  font-weight: 600;
  margin-bottom: 0.25rem;
}

.feature-desc {
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
  line-height: 1.5;
}

.feature-arrow {
  flex-shrink: 0;
  color: var(--color-text-muted);
  opacity: 0;
  transform: translateX(-4px);
  transition: all var(--transition-fast);
  align-self: center;
}

.feature-card:hover .feature-arrow {
  opacity: 1;
  transform: translateX(0);
}

.feature-lock {
  position: absolute;
  top: 0.75rem;
  right: 0.75rem;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: var(--radius-sm);
  color: var(--color-text-muted);
}

/* ===== Stats Grid ===== */
.stats-section {
  margin-bottom: var(--space-3xl);
  opacity: 0;
  transform: translateY(20px);
  transition: opacity 0.6s ease 0.4s, transform 0.6s ease 0.4s;
}

.dashboard-page.ready .stats-section {
  opacity: 1;
  transform: translateY(0);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: var(--space-md);
  margin-top: var(--space-lg);
}

.stat-card {
  padding: var(--space-xl);
  text-align: center;
  transition: all var(--transition-base);
}

.stat-card:hover {
  transform: translateY(-2px);
  border-color: rgba(255, 255, 255, 0.15);
}

.stat-icon {
  font-size: 1.75rem;
  margin-bottom: var(--space-sm);
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

/* ===== Info Section (Guest only — How it works) ===== */
.info-section {
  margin-bottom: var(--space-3xl);
  opacity: 0;
  transform: translateY(20px);
  transition: opacity 0.6s ease 0.5s, transform 0.6s ease 0.5s;
}

.dashboard-page.ready .info-section {
  opacity: 1;
  transform: translateY(0);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--space-lg);
}

.info-card {
  padding: var(--space-xl);
  position: relative;
  transition: all var(--transition-base);
}

.info-card:hover {
  transform: translateY(-2px);
  border-color: rgba(255, 255, 255, 0.15);
}

.info-number {
  font-size: 2.5rem;
  font-weight: 800;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  opacity: 0.4;
  margin-bottom: var(--space-sm);
  line-height: 1;
}

.info-card h3 {
  font-size: var(--font-size-lg);
  font-weight: 600;
  margin-bottom: var(--space-sm);
}

.info-card p {
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
  line-height: 1.6;
}

/* ===== Responsive ===== */
@media (max-width: 900px) {
  .hero-section {
    flex-direction: column;
    text-align: center;
    gap: var(--space-2xl);
  }

  .hero-subtitle {
    margin-left: auto;
    margin-right: auto;
  }

  .hero-visual {
    flex: none;
    width: 100%;
    max-width: 360px;
    height: 260px;
    margin: 0 auto;
  }

  .hero-cta {
    margin: 0 auto;
  }
}

@media (max-width: 640px) {
  .dashboard-page {
    padding: var(--space-lg) 0 var(--space-2xl);
  }

  .hero-title {
    font-size: 1.5rem;
  }

  .hero-subtitle {
    font-size: var(--font-size-base);
  }

  .features-grid {
    grid-template-columns: 1fr;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .hero-visual {
    display: none;
  }
}
</style>
