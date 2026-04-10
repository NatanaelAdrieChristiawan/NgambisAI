<template>
  <div class="not-found-page">
    <div class="not-found-content">
      <div class="error-code">404</div>
      <h1>Halaman Tidak Ditemukan</h1>
      <p>Maaf, halaman yang kamu cari tidak ada atau telah dipindahkan.</p>
      <router-link to="/" class="btn-home">
        ← Kembali ke Dashboard
      </router-link>
    </div>
  </div>
</template>

<style scoped>
.not-found-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  text-align: center;
  padding: 2rem;
}

.error-code {
  font-size: 8rem;
  font-weight: 800;
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1;
  margin-bottom: var(--space-md);
}

.not-found-content h1 {
  font-size: var(--font-size-2xl);
  margin-bottom: var(--space-sm);
}

.not-found-content p {
  color: var(--color-text-secondary);
  margin-bottom: var(--space-xl);
}

.btn-home {
  display: inline-block;
  padding: 0.75rem 2rem;
  background: var(--color-primary);
  color: white;
  border-radius: var(--radius-md);
  font-weight: 600;
  transition: all var(--transition-fast);
}

.btn-home:hover {
  background: var(--color-primary-dark);
  transform: translateY(-2px);
  color: white;
}
</style>
