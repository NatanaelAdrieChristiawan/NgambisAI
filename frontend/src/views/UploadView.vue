<template>
  <div class="placeholder-page">
    <div class="container">
      <div class="placeholder-content glass-card">
        <div class="placeholder-icon">📄</div>
        <h1>Upload PDF</h1>
        <p>Halaman upload materi PDF untuk generate quiz dan simulator ujian lisan.</p>
        <p class="hint">Fitur ini sedang dalam pengembangan oleh tim frontend.</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.placeholder-page { padding: var(--space-2xl) 0; display: flex; align-items: center; min-height: 80vh; }
.placeholder-content { padding: var(--space-3xl); text-align: center; max-width: 500px; margin: 0 auto; }
.placeholder-icon { font-size: 4rem; margin-bottom: var(--space-lg); }
.placeholder-content h1 { font-size: var(--font-size-2xl); margin-bottom: var(--space-md); }
.placeholder-content p { color: var(--color-text-secondary); margin-bottom: var(--space-sm); }
.hint { color: var(--color-text-muted); font-size: var(--font-size-sm); font-style: italic; }
</style>
