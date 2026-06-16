<script setup>
/**
 * WelcomeView.vue
 *
 * Public landing page — shown to unauthenticated users.
 * Clean, light design with blue accents matching the reference mockup.
 * After login, users are redirected to the Dashboard.
 */
import { useAuthStore } from '@/stores/auth'
import { ref, onMounted } from 'vue'

const authStore = useAuthStore()

// Entrance animation
const isReady = ref(false)
onMounted(() => {
  requestAnimationFrame(() => {
    isReady.value = true
  })
})

function handleGetStarted() {
  authStore.requestAuth('Buat akun atau masuk untuk mulai belajar.', '/dashboard')
}



function scrollToFeatures() {
  const el = document.getElementById('features-section')
  if (el) el.scrollIntoView({ behavior: 'smooth' })
}
</script>

<template>
  <div class="welcome-page" :class="{ ready: isReady }">
    <!-- Navbar -->
    <header class="welcome-navbar">
      <div class="navbar-inner">
        <div class="navbar-brand">
          <img class="brand-icon" src="/logo/ngambis.png" alt="Ngambis.AI" width="28" height="28"/>
          <span class="brand-text">NGAMBIS<span class="brand-accent">.AI</span></span>
        </div>
        <nav class="navbar-actions">
          <button class="btn-nav-cta" @click="handleGetStarted">Mulai Sekarang</button>
        </nav>
      </div>
    </header>

    <!-- Hero Section -->
    <section class="hero-section">
      <div class="hero-bg-gradient"></div>
      <div class="hero-container">
        <div class="hero-badge">
          <svg class="badge-icon-svg" xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="m12 3-1.912 5.813a2 2 0 0 1-1.275 1.275L3 12l5.813 1.912a2 2 0 0 1 1.275 1.275L12 21l1.912-5.813a2 2 0 0 1 1.275-1.275L21 12l-5.813-1.912a2 2 0 0 1-1.275-1.275L12 3Z"/>
          </svg>
          <span>PEMBELAJARAN BERBASIS AI</span>
        </div>
        <h1 class="hero-title">
          Belajar Lebih Cerdas dengan<br/>
          <span class="hero-accent">NGAMBIS.AI</span>
        </h1>
        <p class="hero-subtitle">
          Tingkatkan efisiensi belajarmu. Upload materi PDF, dan biarkan AI kami
          meng-generate kuis interaktif dan simulasi ujian lisan secara otomatis.
        </p>
        <div class="hero-buttons">
          <button class="btn-primary-hero" @click="handleGetStarted">
            <span>Mulai Sekarang</span>
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24"
                 fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M5 12h14"/><path d="m12 5 7 7-7 7"/>
            </svg>
          </button>
          <button class="btn-secondary-hero" @click="scrollToFeatures">
            <span>Pelajari Fitur</span>
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24"
                 fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"/><path d="m12 8 4 4-4 4"/><path d="M8 12h8"/>
            </svg>
          </button>
        </div>
      </div>
    </section>

    <!-- Features Section -->
    <section id="features-section" class="features-section">
      <div class="features-container">
        <div class="features-header">
          <h2 class="features-title">Fitur Utama</h2>
          <p class="features-subtitle">
            Alat cerdas yang dirancang untuk membantu mahasiswa informatika memahami
            materi lebih dalam dan bersiap menghadapi ujian.
          </p>
        </div>

        <div class="features-grid">
          <!-- Upload PDF -->
          <div class="feature-card" style="--card-delay: 0s">
            <div class="feature-icon-box" style="--icon-color: #3B82F6; --icon-bg: #EFF6FF">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
                <line x1="12" y1="18" x2="12" y2="12"/>
                <line x1="9" y1="15" x2="15" y2="15"/>
              </svg>
            </div>
            <h3 class="feature-name">Upload PDF</h3>
            <p class="feature-desc">
              Upload materi kuliah untuk generate kuis dan simulator ujian secara
              otomatis. Mendukung format jurnal, slide, dan diktat.
            </p>
          </div>

          <!-- Quiz Session -->
          <div class="feature-card" style="--card-delay: 0.08s">
            <div class="feature-icon-box" style="--icon-color: #10B981; --icon-bg: #ECFDF5">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M9 11l3 3L22 4"/>
                <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>
              </svg>
            </div>
            <h3 class="feature-name">Sesi Kuis</h3>
            <p class="feature-desc">
              Mulai sesi kuis pilihan ganda dan essay dari materi yang sudah
              diupload. Dapatkan evaluasi langsung untuk mengukur pemahaman.
            </p>
          </div>

          <!-- Simulator Ujian Lisan -->
          <div class="feature-card" style="--card-delay: 0.16s">
            <div class="feature-icon-box" style="--icon-color: #8B5CF6; --icon-bg: #F5F3FF">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3z"/>
                <path d="M19 10v2a7 7 0 0 1-14 0v-2"/>
                <line x1="12" y1="19" x2="12" y2="23"/>
                <line x1="8" y1="23" x2="16" y2="23"/>
              </svg>
            </div>
            <h3 class="feature-name">Simulator Ujian Lisan</h3>
            <p class="feature-desc">
              Latihan ujian lisan dengan AI yang berperan sebagai dosen penguji yang
              interaktif. Latih kemampuan komunikasi dan argumentasi teknismu.
            </p>
          </div>

          <!-- Flashcard -->
          <div class="feature-card" style="--card-delay: 0.24s">
            <div class="feature-icon-box" style="--icon-color: #F59E0B; --icon-bg: #FFFBEB">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="2" y="3" width="20" height="14" rx="2" ry="2"/>
                <line x1="8" y1="21" x2="16" y2="21"/>
                <line x1="12" y1="17" x2="12" y2="21"/>
              </svg>
            </div>
            <h3 class="feature-name">Flashcard</h3>
            <p class="feature-desc">
              Belajar konsep-konsep informatika dengan mudah menggunakan flashcard
              interaktif yang di-generate otomatis dari materi.
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- Footer -->
    <footer class="welcome-footer">
      <div class="footer-inner">
        <span class="footer-brand">NGAMBIS.AI</span>
        <span class="footer-copy">© 2026 NGAMBIS.AI. Built for Informatics Students.</span>
      </div>
    </footer>
  </div>
</template>

<style scoped>
/* ===== Page Reset for Light Theme ===== */
.welcome-page {
  background: #FFFFFF;
  color: #1E293B;
  min-height: 100vh;
  overflow-x: hidden;
}

/* ===== Navbar ===== */
.welcome-navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(16px);
  border-bottom: 1px solid #F1F5F9;
}

.navbar-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1.5rem;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.navbar-brand {
  display: flex;
  align-items: center;
  gap: 0.625rem;
}

.brand-icon {
  flex-shrink: 0;
}

.brand-text {
  font-size: 1.125rem;
  font-weight: 800;
  letter-spacing: 0.02em;
  color: #1E293B;
}

.brand-accent {
  color: #3B82F6;
}

.navbar-actions {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.btn-nav-cta {
  padding: 0.5rem 1.25rem;
  background: #1E293B;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-nav-cta:hover {
  background: #334155;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(30, 41, 59, 0.2);
}

/* ===== Hero Section ===== */
.hero-section {
  position: relative;
  text-align: center;
  padding: 5rem 1.5rem 4rem;
  overflow: hidden;
}

.hero-bg-gradient {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, #F0F7FF 0%, #FFFFFF 100%);
  z-index: 0;
}

.hero-container {
  position: relative;
  z-index: 1;
  max-width: 720px;
  margin: 0 auto;
  opacity: 0;
  transform: translateY(30px);
  transition: opacity 0.7s cubic-bezier(0.16, 1, 0.3, 1),
              transform 0.7s cubic-bezier(0.16, 1, 0.3, 1);
}

.welcome-page.ready .hero-container {
  opacity: 1;
  transform: translateY(0);
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.375rem 1rem;
  background: #EFF6FF;
  border: 1px solid #DBEAFE;
  border-radius: 9999px;
  font-size: 0.6875rem;
  font-weight: 700;
  color: #3B82F6;
  letter-spacing: 0.08em;
  margin-bottom: 1.5rem;
}

.badge-icon-svg {
  flex-shrink: 0;
}

.hero-title {
  font-size: clamp(2rem, 5vw, 3rem);
  font-weight: 800;
  line-height: 1.2;
  color: #0F172A;
  margin-bottom: 1.25rem;
  letter-spacing: -0.02em;
}

.hero-accent {
  color: #3B82F6;
}

.hero-subtitle {
  font-size: 1.0625rem;
  line-height: 1.7;
  color: #64748B;
  max-width: 560px;
  margin: 0 auto 2rem;
}

.hero-buttons {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.875rem;
  flex-wrap: wrap;
}

.btn-primary-hero {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.8125rem 1.75rem;
  background: #3B82F6;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 0.9375rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s ease;
  box-shadow: 0 4px 16px rgba(59, 130, 246, 0.3);
}

.btn-primary-hero:hover {
  background: #2563EB;
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(59, 130, 246, 0.35);
}

.btn-primary-hero svg {
  transition: transform 0.2s ease;
}

.btn-primary-hero:hover svg {
  transform: translateX(3px);
}

.btn-secondary-hero {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.8125rem 1.5rem;
  background: white;
  color: #475569;
  border: 1.5px solid #E2E8F0;
  border-radius: 10px;
  font-size: 0.9375rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s ease;
}

.btn-secondary-hero:hover {
  border-color: #CBD5E1;
  color: #1E293B;
  background: #F8FAFC;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
}

/* ===== Features Section ===== */
.features-section {
  padding: 4rem 1.5rem 5rem;
  background: #FAFBFD;
}

.features-container {
  max-width: 1100px;
  margin: 0 auto;
}

.features-header {
  text-align: center;
  margin-bottom: 3rem;
  opacity: 0;
  transform: translateY(24px);
  transition: opacity 0.6s cubic-bezier(0.16, 1, 0.3, 1) 0.15s,
              transform 0.6s cubic-bezier(0.16, 1, 0.3, 1) 0.15s;
}

.welcome-page.ready .features-header {
  opacity: 1;
  transform: translateY(0);
}

.features-title {
  font-size: 1.75rem;
  font-weight: 800;
  color: #0F172A;
  margin-bottom: 0.625rem;
}

.features-subtitle {
  font-size: 0.9375rem;
  line-height: 1.7;
  color: #64748B;
  max-width: 520px;
  margin: 0 auto;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1.25rem;
}

.feature-card {
  background: white;
  border: 1px solid #F1F5F9;
  border-radius: 16px;
  padding: 1.75rem 1.5rem;
  transition: all 0.3s ease;
  opacity: 0;
  transform: translateY(20px);
  animation: featureCardIn 0.6s cubic-bezier(0.16, 1, 0.3, 1) forwards;
  animation-delay: var(--card-delay, 0s);
  /* Delay animation until page ready via class */
  animation-play-state: paused;
}

.welcome-page.ready .feature-card {
  animation-play-state: running;
}

@keyframes featureCardIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.feature-card:hover {
  border-color: #E2E8F0;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.06);
  transform: translateY(-4px);
}

.feature-icon-box {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 1.125rem;
  background: var(--icon-bg);
  color: var(--icon-color);
}

.feature-name {
  font-size: 1.0625rem;
  font-weight: 700;
  color: #0F172A;
  margin-bottom: 0.5rem;
}

.feature-desc {
  font-size: 0.8125rem;
  line-height: 1.65;
  color: #64748B;
}

/* ===== Footer ===== */
.welcome-footer {
  padding: 2rem 1.5rem;
  border-top: 1px solid #F1F5F9;
  background: white;
}

.footer-inner {
  max-width: 1100px;
  margin: 0 auto;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.375rem;
}

.footer-brand {
  font-size: 0.9375rem;
  font-weight: 800;
  color: #1E293B;
  letter-spacing: 0.02em;
}

.footer-copy {
  font-size: 0.75rem;
  color: #94A3B8;
}

/* ===== Responsive ===== */
@media (max-width: 900px) {
  .features-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {

  .hero-section {
    padding: 3rem 1rem 2.5rem;
  }

  .hero-title {
    font-size: 1.75rem;
  }

  .hero-subtitle {
    font-size: 0.9375rem;
  }

  .features-grid {
    grid-template-columns: 1fr;
  }

  .features-section {
    padding: 2.5rem 1rem 3rem;
  }

  .hero-buttons {
    flex-direction: column;
  }

  .btn-primary-hero,
  .btn-secondary-hero {
    width: 100%;
    justify-content: center;
  }
}
</style>
