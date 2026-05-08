<template>
  <div class="page-wrapper">
    <div class="page-content">
      <div class="page-card">
        <div class="page-icon">📝</div>
        <h1>Quiz Mode</h1>
        <p>Tes pemahamanmu dengan kuis yang dipersonalisasi. Tingkat kesulitan menyesuaikan progres belajarmu.</p>
        <p class="hint">Fitur ini sedang dalam pengembangan oleh tim frontend.</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-wrapper { padding: 3rem 1.5rem; display: flex; align-items: center; justify-content: center; min-height: 80vh; background: #F8FAFC; }
.page-content { width: 100%; max-width: 500px; }
.page-card { background: white; border: 1px solid #E2E8F0; border-radius: 16px; padding: 3rem; text-align: center; box-shadow: 0 4px 24px rgba(0,0,0,0.04); }
.page-icon { font-size: 3.5rem; margin-bottom: 1rem; }
.page-card h1 { font-size: 1.5rem; font-weight: 700; color: #0F172A; margin-bottom: 0.75rem; }
.page-card p { color: #64748B; font-size: 0.9375rem; line-height: 1.6; margin-bottom: 0.5rem; }
.hint { color: #94A3B8 !important; font-size: 0.8125rem !important; font-style: italic; }
</style>
