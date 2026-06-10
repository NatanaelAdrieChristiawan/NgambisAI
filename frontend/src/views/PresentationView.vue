<script setup>
/**
 * PresentationView.vue
 * Interactive slide deck presentation integrated directly into the web application.
 * Controls: Right/Left arrow keys, Space, or on-screen controls.
 */
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const currentSlide = ref(0)
const maxSlides = 11

const slides = [
  { title: 'Welcome', subtitle: 'Ngambis.ai' },
  { title: 'Pembagian Tugas', subtitle: 'Tim Pengembang & Topik' },
  { title: 'Latar Belakang', subtitle: 'Solusi Belajar Mahasiswa' },
  { title: 'Tech Stack', subtitle: 'Arsitektur Sistem' },
  { title: 'Pilar 1: Inheritance', subtitle: 'Pewarisan Kelas & Database' },
  { title: 'Pilar 2: Polymorphism', subtitle: 'Overloading & Overriding' },
  { title: 'Pilar 3: Abstraction', subtitle: 'Interface & Dependency Inversion' },
  { title: 'Pilar 4: Encapsulation', subtitle: 'Enkapsulasi & Asosiasi Objek' },
  { title: 'Fitur Utama', subtitle: 'PDFBox, MCQ, & Ujian Lisan' },
  { title: 'Optimasi & Keamanan', subtitle: 'Skalabilitas & Token' },
  { title: 'Demo & Kesimpulan', subtitle: 'Live Action' }
]

function nextSlide() {
  if (currentSlide.value < maxSlides - 1) {
    currentSlide.value++
  }
}

function prevSlide() {
  if (currentSlide.value > 0) {
    currentSlide.value--
  }
}

function goToSlide(index) {
  if (index >= 0 && index < maxSlides) {
    currentSlide.value = index
  }
}

function handleKeyDown(e) {
  if (e.key === 'ArrowRight' || e.key === ' ' || e.key === 'PageDown') {
    e.preventDefault()
    nextSlide()
  } else if (e.key === 'ArrowLeft' || e.key === 'PageUp') {
    e.preventDefault()
    prevSlide()
  } else if (e.key === 'Escape') {
    router.push('/dashboard')
  }
}

onMounted(() => {
  window.addEventListener('keydown', handleKeyDown)
  document.body.style.overflow = 'hidden' // Lock scroll for full screen presentation
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleKeyDown)
  document.body.style.overflow = ''
})

// Active layer state for Tech Stack slide
const activeStackLayer = ref('frontend')

// Code Explorer states for PBO Pillars
const activePilar1Tab = ref('db')
const activePilar2Tab = ref('strategy')
const activePilar3Tab = ref('abstract')
const activePilar4Tab = ref('private')
</script>

<template>
  <div class="presentation-container">
    <!-- Top Header -->
    <header class="pres-header">
      <div class="brand">
        <img src="/logo/ngambis.png" alt="Logo" width="24" height="24" />
        <span>NGAMBIS<span class="accent">.AI</span></span>
      </div>
      <div class="slide-nav-pills">
        <button 
          v-for="(s, idx) in slides" 
          :key="idx" 
          class="nav-pill"
          :class="{ active: currentSlide === idx }"
          @click="goToSlide(idx)"
          :title="s.title"
        >
          {{ idx + 1 }}
        </button>
      </div>
      <button class="btn-exit" @click="router.push('/dashboard')">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
        <span>Keluar</span>
      </button>
    </header>

    <!-- Slides Area -->
    <main class="slide-viewport">
      <Transition name="slide-fade" mode="out-in">
        
        <!-- Slide 1: Welcome -->
        <div v-if="currentSlide === 0" class="slide slide-welcome" key="0">
          <div class="hero-gradient"></div>
          <div class="slide-content text-center">
            <div class="badge">TUGAS BESAR PEMROGRAMAN BERORIENTASI OBJEK (PBO)</div>
            <h1 class="welcome-title">NGAMBIS<span class="hero-accent">.AI</span></h1>
            <p class="welcome-desc">Platform Belajar Cerdas Berbasis AI — Upload PDF, Smart Flashcards, Kuis Adaptif, dan Simulasi Ujian Lisan.</p>
            <div class="divider"></div>
            <div class="presenters-grid">
              <div class="presenter-card">
                <span class="p-name">Arya Fajar Pratama</span>
                <span class="p-nim">103012400125</span>
              </div>
              <div class="presenter-card">
                <span class="p-name">Diki Sugiantoro</span>
                <span class="p-nim">103012400401</span>
              </div>
              <div class="presenter-card">
                <span class="p-name">Muhammad Revikhasha Farabi Putera</span>
                <span class="p-nim">103012400287</span>
              </div>
              <div class="presenter-card">
                <span class="p-name">Natanael Adrie Christiawan</span>
                <span class="p-nim">103012400334</span>
              </div>
              <div class="presenter-card">
                <span class="p-name">Rizky Dzulfikar Ahmad</span>
                <span class="p-nim">103012430033</span>
              </div>
            </div>
            <div class="hint-text">Tekan <kbd>Space</kbd> atau <kbd>→</kbd> untuk slide berikutnya</div>
          </div>
        </div>

        <!-- Slide 2: Pembagian Tugas -->
        <div v-else-if="currentSlide === 1" class="slide slide-normal" key="1">
          <div class="slide-content text-center">
            <div class="slide-category">TIM PENGEMBANG & PRESENTASI</div>
            <h2 class="slide-title">Pembagian Tugas & Topik Presentasi</h2>
            
            <div class="task-grid">
              <div class="task-card border-blue">
                <div class="task-member">Arya Fajar Pratama</div>
                <div class="task-nim">NIM: 103012400125</div>
                <div class="task-divider"></div>
                <div class="task-topic"><strong>Materi Presentasi:</strong></div>
                <ul class="task-list">
                  <li>Latar Belakang Masalah</li>
                  <li>Solusi Aplikasi</li>
                  <li>Arsitektur & Tech Stack</li>
                </ul>
              </div>

              <div class="task-card border-green">
                <div class="task-member">Diki Sugiantoro</div>
                <div class="task-nim">NIM: 103012400401</div>
                <div class="task-divider"></div>
                <div class="task-topic"><strong>Pilar 1: Inheritance</strong></div>
                <ul class="task-list">
                  <li>JPA Single Table</li>
                  <li>Custom Exception Inherit</li>
                  <li>Interface Repository</li>
                </ul>
              </div>

              <div class="task-card border-red">
                <div class="task-member">M. Revikhasha F. P.</div>
                <div class="task-nim">NIM: 103012400287</div>
                <div class="task-divider"></div>
                <div class="task-topic"><strong>Pilar 2: Polymorphism</strong></div>
                <ul class="task-list">
                  <li>Method Overloading</li>
                  <li>UserDetails Overriding</li>
                  <li>Constructor Overloading</li>
                </ul>
              </div>

              <div class="task-card border-yellow">
                <div class="task-member">Natanael Adrie C.</div>
                <div class="task-nim">NIM: 103012400334</div>
                <div class="task-divider"></div>
                <div class="task-topic"><strong>Pilar 3: Abstraction</strong></div>
                <ul class="task-list">
                  <li>Abstract Class QuizItem</li>
                  <li>Interface PersonaStrategy</li>
                  <li>Dependency Injection</li>
                </ul>
              </div>

              <div class="task-card border-purple">
                <div class="task-member">Rizky Dzulfikar A.</div>
                <div class="task-nim">NIM: 103012430033</div>
                <div class="task-divider"></div>
                <div class="task-topic"><strong>Pilar 4: Encapsulation</strong></div>
                <ul class="task-list">
                  <li>Private Class Variables</li>
                  <li>Boilerplate Lombok</li>
                  <li>Relasi Asosiasi JPA</li>
                </ul>
              </div>
            </div>
          </div>
        </div>

        <!-- Slide 3: Latar Belakang -->
        <div v-else-if="currentSlide === 2" class="slide slide-normal" key="2">
          <div class="slide-content">
            <div class="slide-category">LATAR BELAKANG & MASALAH</div>
            <h2 class="slide-title">Mengapa Ngambis.ai?</h2>
            
            <div class="grid-2col">
              <div class="card card-dark flex-column justify-center">
                <h3 class="accent-red flex-align-center gap-2">
                  <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                  Hambatan Belajar Mahasiswa
                </h3>
                <ul class="bullet-list text-left">
                  <li><strong>Informasi Berlebih:</strong> Buku diktat dan slide kuliah tebal membuat mahasiswa sulit memilah konsep inti.</li>
                  <li><strong>Pasif vs Aktif:</strong> Membaca ulang materi (*passive recall*) tidak seefektif menjawab pertanyaan (*active recall*).</li>
                  <li><strong>Kurang Latihan Ujian Lisan:</strong> Ujian lisan sering memicu kecemasan karena tidak adanya simulasi mandiri.</li>
                </ul>
              </div>
              
              <div class="card card-primary flex-column justify-center">
                <h3 class="accent-green flex-align-center gap-2">
                  <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><circle cx="12" cy="12" r="10"/><polyline points="22 4 12 14.01 9 11.01"/></svg>
                  Solusi Pintar Ngambis.ai
                </h3>
                <ul class="bullet-list text-left">
                  <li><strong>Ekstraksi Konteks Otomatis:</strong> Mengubah dokumen PDF kuliah menjadi pangkalan pengetahuan AI.</li>
                  <li><strong>Kuis Adaptif & Flashcards:</strong> Latihan instan untuk menguji pemahaman secara efisien (*active recall*).</li>
                  <li><strong>Simulator Penguji Verbal:</strong> Uji pemahaman lisan mahasiswa menggunakan simulasi AI berbasis Web Speech.</li>
                </ul>
              </div>
            </div>
          </div>
        </div>

        <!-- Slide 4: Tech Stack -->
        <div v-else-if="currentSlide === 3" class="slide slide-normal" key="3">
          <div class="slide-content">
            <div class="slide-category">ARSITEKTUR & TEKNOLOGI</div>
            <h2 class="slide-title">Spesifikasi Teknologi (Tech Stack)</h2>

            <div class="grid-layout-stack">
              <div class="stack-layers">
                <button 
                  class="stack-layer-btn" 
                  :class="{ active: activeStackLayer === 'frontend' }" 
                  @click="activeStackLayer = 'frontend'"
                  style="border-left: 4px solid #3B82F6;"
                >
                  Layer 1: Frontend (Client)
                </button>
                <button 
                  class="stack-layer-btn" 
                  :class="{ active: activeStackLayer === 'backend' }" 
                  @click="activeStackLayer = 'backend'"
                  style="border-left: 4px solid #10B981;"
                >
                  Layer 2: Backend (Server)
                </button>
                <button 
                  class="stack-layer-btn" 
                  :class="{ active: activeStackLayer === 'database' }" 
                  @click="activeStackLayer = 'database'"
                  style="border-left: 4px solid #F59E0B;"
                >
                  Layer 3: Relational Database
                </button>
                <button 
                  class="stack-layer-btn" 
                  :class="{ active: activeStackLayer === 'ai' }" 
                  @click="activeStackLayer = 'ai'"
                  style="border-left: 4px solid #8B5CF6;"
                >
                  Layer 4: AI & Speech Integration
                </button>
              </div>

              <div class="stack-detail card card-dark flex-column justify-center">
                <div v-if="activeStackLayer === 'frontend'">
                  <h3 class="accent-blue">Vue.js 3 + Vite + Pinia</h3>
                  <p>Antarmuka pengguna interaktif bergaya modern dengan sistem state global terpusat:</p>
                  <ul class="bullet-list inline">
                    <li><strong>Vue.js 3 Composition API</strong> untuk modularitas komponen UI.</li>
                    <li><strong>Pinia</strong> untuk menyimpan data autentikasi, kuis, dan chat.</li>
                    <li><strong>CSS Vanilla</strong> murni untuk desain visual premium dan micro-animation.</li>
                  </ul>
                </div>
                <div v-else-if="activeStackLayer === 'backend'">
                  <h3 class="accent-green">Spring Boot 3.4 & Spring Security</h3>
                  <p>RESTful API backend yang kuat, aman, dan modular:</p>
                  <ul class="bullet-list inline">
                    <li><strong>JWT (JSON Web Tokens)</strong> untuk stateless authorization.</li>
                    <li><strong>JPA/Hibernate</strong> untuk pemetaan objek database terenkapsulasi.</li>
                    <li><strong>Apache PDFBox</strong> untuk ekstraksi dokumen PDF kuliah secara server-side.</li>
                  </ul>
                </div>
                <div v-else-if="activeStackLayer === 'database'">
                  <h3 class="accent-yellow">MySQL 8.0</h3>
                  <p>Database relasional untuk menyimpan identitas pengguna, file dokumen, riwayat sesi, dan hasil evaluasi:</p>
                  <ul class="bullet-list inline">
                    <li>Integritas data dengan relasi Foreign Key terindeks (indeks pada `username` & `email`).</li>
                    <li>Operasi penghapusan kaskade otomatis (*cascade delete*) menjaga kebersihan database.</li>
                  </ul>
                </div>
                <div v-else-if="activeStackLayer === 'ai'">
                  <h3 class="accent-purple">Google Gemini API & Web Speech API</h3>
                  <p>Mesin kecerdasan utama aplikasi:</p>
                  <ul class="bullet-list inline">
                    <li><strong>Google Gemini API:</strong> Digunakan untuk pembuatan soal kuis otomatis, asisten chat dokumen, dan pemberi evaluasi kritis.</li>
                    <li><strong>Web Speech API:</strong> Transkripsi suara lisan mahasiswa langsung di client-side secara real-time dan gratis.</li>
                  </ul>
                </div>
              </div>
            </div>
          </div>
        </div>
        <!-- Slide 5: Pilar 1 Inheritance -->
        <div v-else-if="currentSlide === 4" class="slide slide-normal" key="4">
          <div class="slide-content">
            <div class="slide-category">PILAR PBO 1: INHERITANCE (PEWARISAN)</div>
            <h2 class="slide-title">Pewarisan Kelas, Interface, & Database</h2>
            <p class="slide-intro">Mewarisi sifat, atribut, dan perilaku dari superclass untuk meminimalkan redundansi kode dan mewujudkan polimorfisme.</p>

            <div class="code-explorer-grid">
              <div class="tabs">
                <button :class="{ active: activePilar1Tab === 'db' }" @click="activePilar1Tab = 'db'">QuizItem.java (JPA Single Table)</button>
                <button :class="{ active: activePilar1Tab === 'exception' }" @click="activePilar1Tab = 'exception'">ResourceNotFoundException.java</button>
                <button :class="{ active: activePilar1Tab === 'repo' }" @click="activePilar1Tab = 'repo'">UserRepository.java</button>
              </div>

              <div class="code-viewport">
                <pre v-if="activePilar1Tab === 'db'"><code><span class="comment">// 1. Kelas Induk (Superclass): QuizItem.java</span>
<span class="annotation">@Entity</span>
<span class="annotation">@Inheritance(strategy = InheritanceType.SINGLE_TABLE)</span> <span class="comment">// Strategi Single Table</span>
<span class="annotation">@DiscriminatorColumn(name = "item_type")</span> <span class="comment">// Kolom diskriminator penanda tipe</span>
<span class="keyword">public abstract class</span> <span class="type">QuizItem</span> {
    <span class="annotation">@Id</span> <span class="annotation">@GeneratedValue</span> <span class="keyword">private</span> <span class="type">UUID</span> id;
    <span class="keyword">private</span> <span class="type">String</span> questionText;
    <span class="keyword">private</span> <span class="type">String</span> referenceText;
}

<span class="comment">// 2. Kelas Anak (Subclass 1): MultipleChoiceItem.java</span>
<span class="annotation">@Entity</span> <span class="annotation">@DiscriminatorValue("MULTIPLE_CHOICE")</span>
<span class="keyword">public class</span> <span class="type">MultipleChoiceItem</span> <span class="keyword">extends</span> <span class="type">QuizItem</span> {
    <span class="keyword">private</span> <span class="type">String</span> options;
    <span class="keyword">private</span> <span class="type">String</span> correctAnswer;
}

<span class="comment">// 3. Kelas Anak (Subclass 2): EssayItem.java</span>
<span class="annotation">@Entity</span> <span class="annotation">@DiscriminatorValue("ESSAY")</span>
<span class="keyword">public class</span> <span class="type">EssayItem</span> <span class="keyword">extends</span> <span class="type">QuizItem</span> {
    <span class="comment">// Mewarisi properti superclass tanpa kolom database tambahan</span>
}</code></pre>
                <pre v-else-if="activePilar1Tab === 'exception'"><code><span class="comment">// Pewarisan pada Custom Exception: ResourceNotFoundException.java</span>
<span class="annotation">@ResponseStatus(value = HttpStatus.NOT_FOUND)</span>
<span class="keyword">public class</span> <span class="type">ResourceNotFoundException</span> <span class="keyword">extends</span> <span class="type">RuntimeException</span> { <span class="comment">// Mewarisi exception runtime bawaan Java</span>
    <span class="keyword">private</span> <span class="type">String</span> resourceName;
    
    <span class="keyword">public</span> ResourceNotFoundException(<span class="type">String</span> message) {
        <span class="keyword">super</span>(message); <span class="comment">// Memanggil konstruktor superclass (RuntimeException)</span>
    }
}</code></pre>
                <pre v-else-if="activePilar1Tab === 'repo'"><code><span class="comment">// Pewarisan Interface: UserRepository.java</span>
<span class="annotation">@Repository</span>
<span class="comment">// Interface UserRepository mewarisi seluruh kemampuan CRUD dari JpaRepository bawaan Spring</span>
<span class="keyword">public interface</span> <span class="type">UserRepository</span> <span class="keyword">extends</span> <span class="type">JpaRepository</span>&lt;<span class="type">User</span>, <span class="type">UUID</span>&gt; {
    <span class="type">Optional</span>&lt;<span class="type">User</span>&gt; findByEmail(<span class="type">String</span> email);
}</code></pre>
              </div>
            </div>
          </div>
        </div>

        <!-- Slide 6: Pilar 2 Polymorphism -->
        <div v-else-if="currentSlide === 5" class="slide slide-normal" key="5">
          <div class="slide-content">
            <div class="slide-category">PILAR PBO 2: POLYMORPHISM (POLIMORFISME)</div>
            <h2 class="slide-title">Compile-Time & Run-Time Polymorphism</h2>
            <p class="slide-intro">Memungkinkan objek atau method memiliki berbagai bentuk tindakan berbeda saat kompilasi maupun saat dijalankan.</p>

            <div class="code-explorer-grid">
              <div class="tabs">
                <button :class="{ active: activePilar2Tab === 'strategy' }" @click="activePilar2Tab = 'strategy'">Dynamic Binding (Runtime)</button>
                <button :class="{ active: activePilar2Tab === 'overload_method' }" @click="activePilar2Tab = 'overload_method'">Method Overloading (Compile-time)</button>
                <button :class="{ active: activePilar2Tab === 'overload_const' }" @click="activePilar2Tab = 'overload_const'">Constructor Overloading (Compile-time)</button>
                <button :class="{ active: activePilar2Tab === 'security' }" @click="activePilar2Tab = 'security'">UserDetails Overriding (Runtime)</button>
              </div>

              <div class="code-viewport">
                <pre v-if="activePilar2Tab === 'strategy'"><code><span class="comment">// 1. Abstraksi Interface: PersonaStrategy.java</span>
<span class="keyword">public interface</span> <span class="type">PersonaStrategy</span> {
    <span class="type">String</span> generateEvaluationPrompt(<span class="type">String</span> q, <span class="type">String</span> ref, <span class="type">String</span> ans);
}

<span class="comment">// 2. Concrete Strategy 1 (Overriding): FriendlySeniorStrategy.java</span>
<span class="annotation">@Component</span>
<span class="keyword">public class</span> <span class="type">FriendlySeniorStrategy</span> <span class="keyword">implements</span> <span class="type">PersonaStrategy</span> {
    <span class="annotation">@Override</span>
    <span class="keyword">public</span> <span class="type">String</span> generateEvaluationPrompt(<span class="type">String</span> q, <span class="type">String</span> ref, <span class="type">String</span> ans) {
        <span class="keyword">return</span> <span class="string">"Role: Friendly Senior. Use simple analogies..."</span>; <span class="comment">// Implementasi 1</span>
    }
}

<span class="comment">// 3. Concrete Strategy 2 (Overriding): StrictLecturerStrategy.java</span>
<span class="annotation">@Component</span>
<span class="keyword">public class</span> <span class="type">StrictLecturerStrategy</span> <span class="keyword">implements</span> <span class="type">PersonaStrategy</span> {
    <span class="annotation">@Override</span>
    <span class="keyword">public</span> <span class="type">String</span> generateEvaluationPrompt(<span class="type">String</span> q, <span class="type">String</span> ref, <span class="type">String</span> ans) {
        <span class="keyword">return</span> <span class="string">"Role: Strict Lecturer. Be critical and point out flaws..."</span>; <span class="comment">// Implementasi 2</span>
    }
}

<span class="comment">// 4. Dynamic Binding (Runtime Polymorphism) di SimulatorService.java</span>
<span class="type">PersonaStrategy</span> strategy = resolveStrategy(session.getPersonaType());
<span class="comment">// Java menentukan di runtime implementasi konkret mana yang akan dijalankan:</span>
<span class="type">String</span> prompt = strategy.generateEvaluationPrompt(question, ctx, ans);</code></pre>
                <pre v-else-if="activePilar2Tab === 'overload_method'"><code><span class="comment">// Method Overloading di PdfExtractionService.java (Compile-Time Polymorphism)</span>
 
<span class="comment">// Versi 1: Menerima file dan custom chunk size</span>
<span class="keyword">public</span> <span class="type">List</span>&lt;<span class="type">String</span>&gt; extractAndChunk(<span class="type">MultipartFile</span> file, <span class="keyword">int</span> chunkSize) {
    <span class="type">String</span> fullText = extractText(file);
    <span class="keyword">return</span> chunkText(fullText, chunkSize);
}

<span class="comment">// Versi 2 (Overloaded): Menerima file saja (menggunakan default chunk size)</span>
<span class="comment">// Memiliki nama yang sama tetapi signature parameter yang berbeda</span>
<span class="keyword">public</span> <span class="type">List</span>&lt;<span class="type">String</span>&gt; extractAndChunk(<span class="type">MultipartFile</span> file) {
    <span class="keyword">return</span> extractAndChunk(file, DEFAULT_CHUNK_SIZE);
}</code></pre>
                <pre v-else-if="activePilar2Tab === 'overload_const'"><code><span class="comment">// Constructor Overloading di ResourceNotFoundException.java (Compile-Time Polymorphism)</span>
<span class="keyword">public class</span> <span class="type">ResourceNotFoundException</span> <span class="keyword">extends</span> <span class="type">RuntimeException</span> {
    
    <span class="comment">// Versi 1: Menerima pesan string langsung</span>
    <span class="keyword">public</span> ResourceNotFoundException(<span class="type">String</span> message) {
        <span class="keyword">super</span>(message);
    }
    
    <span class="comment">// Versi 2 (Overloaded): Konstruktor dinamis berparameter spesifik</span>
    <span class="comment">// Memiliki parameter berbeda untuk menyusun pesan terformat</span>
    <span class="keyword">public</span> ResourceNotFoundException(<span class="type">String</span> resourceName, <span class="type">String</span> fieldName, <span class="type">Object</span> fieldValue) {
        <span class="keyword">super</span>(<span class="type">String</span>.format(<span class="string">"%s not found with %s: '%s'"</span>, resourceName, fieldName, fieldValue));
    }
}</code></pre>
                <pre v-else-if="activePilar2Tab === 'security'"><code><span class="comment">// Method Overriding dari interface Spring Security UserDetails di model User.java</span>
<span class="annotation">@Override</span>
<span class="keyword">public</span> <span class="type">Collection</span>&lt;? <span class="keyword">extends</span> <span class="type">GrantedAuthority</span>&gt; getAuthorities() {
    <span class="comment">// Mengoverride kelakuan default untuk memetakan peran user</span>
    <span class="keyword">return</span> roles.stream()
            .map(role -> <span class="keyword">new</span> <span class="type">SimpleGrantedAuthority</span>(<span class="string">"ROLE_"</span> + role))
            .collect(<span class="type">Collectors</span>.toList());
}</code></pre>
              </div>
            </div>
          </div>
        </div>

        <!-- Slide 7: Pilar 3 Abstraction -->
        <div v-else-if="currentSlide === 6" class="slide slide-normal" key="6">
          <div class="slide-content">
            <div class="slide-category">PILAR PBO 3: ABSTRACTION (ABSTRAKSI)</div>
            <h2 class="slide-title">Abstraksi Kode & Dependency Injection</h2>
            <p class="slide-intro">Menyembunyikan kerumitan detail teknis di balik struktur abstrak (Interface/Abstract Class) untuk mencapai kelonggaran ikatan (loose coupling).</p>

            <div class="code-explorer-grid">
              <div class="tabs">
                <button :class="{ active: activePilar3Tab === 'abstract' }" @click="activePilar3Tab = 'abstract'">QuizItem (Abstract Class)</button>
                <button :class="{ active: activePilar3Tab === 'interface' }" @click="activePilar3Tab = 'interface'">PersonaStrategy (Interface)</button>
                <button :class="{ active: activePilar3Tab === 'di' }" @click="activePilar3Tab = 'di'">Dependency Injection</button>
              </div>

              <div class="code-viewport">
                <pre v-if="activePilar3Tab === 'abstract'"><code><span class="comment">// Abstraksi Objek Soal Kuis (QuizItem.java)</span>
<span class="comment">// Kelas induk abstrak yang tidak dapat diinstansiasi langsung menggunakan kata kunci 'new'</span>
<span class="keyword">public abstract class</span> <span class="type">QuizItem</span> {
    <span class="annotation">@Id</span> <span class="keyword">private</span> <span class="type">UUID</span> id;
    <span class="keyword">private</span> <span class="type">String</span> questionText;
    <span class="keyword">private</span> <span class="type">String</span> referenceText;
    
    <span class="comment">// Subclass konkret wajib memperluas kelas ini sebelum dapat diinstansiasi</span>
}</code></pre>
                <pre v-else-if="activePilar3Tab === 'interface'"><code><span class="comment">// Abstraksi Gaya Prompt AI (PersonaStrategy.java)</span>
<span class="comment">// Interface mendefinisikan kontrak tanpa detail implementasi prompt</span>
<span class="keyword">public interface</span> <span class="type">PersonaStrategy</span> {
    <span class="comment">// Menyembunyikan kompleksitas pembentukan prompt evaluasi AI</span>
    <span class="type">String</span> generateEvaluationPrompt(<span class="type">String</span> question, 
                                   <span class="type">String</span> referenceText, 
                                   <span class="type">String</span> studentAnswer);
}</code></pre>
                <pre v-else-if="activePilar3Tab === 'di'"><code><span class="comment">// Dependency Injection & Loose Coupling di SimulatorService.java</span>
<span class="annotation">@Service</span>
<span class="annotation">@RequiredArgsConstructor</span> <span class="comment">// Menghasilkan konstruktor untuk field final secara otomatis</span>
<span class="keyword">public class</span> <span class="type">SimulatorService</span> {
    
    <span class="comment">// Dependensi diinjeksikan secara otomatis oleh Spring Boot IoC Container</span>
    <span class="comment">// Bergantung pada abstraksi interface (PersonaStrategy), bukan kelas konkret!</span>
    <span class="keyword">private final</span> <span class="type">StrictLecturerStrategy</span> strictLecturerStrategy;
    <span class="keyword">private final</span> <span class="type">FriendlySeniorStrategy</span> friendlySeniorStrategy;
    
    <span class="keyword">private</span> <span class="type">PersonaStrategy</span> resolveStrategy(<span class="type">PersonaType</span> type) {
        <span class="keyword">return switch</span> (type) {
            <span class="keyword">case</span> STRICT_LECTURER -> strictLecturerStrategy;
            <span class="keyword">case</span> FRIENDLY_SENIOR -> friendlySeniorStrategy;
        };
    }
}</code></pre>
              </div>
            </div>
          </div>
        </div>

        <!-- Slide 8: Pilar 4 Encapsulation -->
        <div v-else-if="currentSlide === 7" class="slide slide-normal" key="7">
          <div class="slide-content">
            <div class="slide-category">PILAR PBO 4: ENCAPSULATION (ENKAPSULASI)</div>
            <h2 class="slide-title">Enkapsulasi Status, Lombok, & Asosiasi</h2>
            <p class="slide-intro">Menyembunyikan data internal suatu kelas dengan akses terbatas, serta membungkus relasi asosiasi objek di dalam entitas.</p>

            <div class="code-explorer-grid">
              <div class="tabs">
                <button :class="{ active: activePilar4Tab === 'private' }" @click="activePilar4Tab = 'private'">Private Fields & Lombok</button>
                <button :class="{ active: activePilar4Tab === 'association' }" @click="activePilar4Tab = 'association'">Encapsulated Association</button>
              </div>

              <div class="code-viewport">
                <pre v-if="activePilar4Tab === 'private'"><code><span class="comment">// Penyembunyian Data & Akses Terkontrol di Evaluation.java</span>
<span class="annotation">@Data</span> <span class="comment">// Otomatis menghasilkan getter/setter untuk semua field private</span>
<span class="annotation">@Builder</span>
<span class="annotation">@NoArgsConstructor</span>
<span class="annotation">@AllArgsConstructor</span>
<span class="keyword">public class</span> <span class="type">Evaluation</span> {
    <span class="annotation">@Id</span>
    <span class="keyword">private</span> <span class="type">UUID</span> id; <span class="comment">// Hak akses disembunyikan menggunakan modifier private</span>
    
    <span class="keyword">private</span> <span class="type">Integer</span> score;
    <span class="keyword">private</span> <span class="type">String</span> feedback;
    
    <span class="comment">// Kelas luar tidak dapat memodifikasi variabel di atas secara acak tanpa melalui setter publik</span>
}</code></pre>
                <pre v-else-if="activePilar4Tab === 'association'"><code><span class="comment">// Enkapsulasi Relasi Asosiasi (Composition & Aggregation) di User.java</span>
<span class="keyword">public class</span> <span class="type">User</span> <span class="keyword">implements</span> <span class="type">UserDetails</span> {
    <span class="keyword">private</span> <span class="type">UUID</span> id;
    
    <span class="comment">// Koleksi data relasional dibungkus rapi dengan access modifier private</span>
    <span class="comment">// Diinisialisasi langsung sebagai list kosong untuk menjaga keamanan internal state</span>
    
    <span class="annotation">@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)</span>
    <span class="keyword">private</span> <span class="type">List</span>&lt;<span class="type">Document</span>&gt; documents = <span class="keyword">new</span> <span class="type">ArrayList</span>&lt;&gt;();
    
    <span class="annotation">@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)</span>
    <span class="keyword">private</span> <span class="type">List</span>&lt;<span class="type">QuizSession</span>&gt; quizSessions = <span class="keyword">new</span> <span class="type">ArrayList</span>&lt;&gt;();
}</code></pre>
              </div>
            </div>
          </div>
        </div>

        <!-- Slide 9: Fitur Utama -->
        <div v-else-if="currentSlide === 8" class="slide slide-normal" key="8">
          <div class="slide-content">
            <div class="slide-category">FITUR UTAMA APLIKASI</div>
            <h2 class="slide-title">PDF Extraction, MCQ, & Ujian Lisan</h2>
            
            <div class="grid-3col text-center">
              <div class="card card-dark">
                <div class="card-icon-round bg-blue">📂</div>
                <h4>Ekstraksi PDFBox</h4>
                <p>Membaca berkas PDF, memotong teks per 2000 karakter (overlap 200 karakter) demi menghemat limitasi token Gemini API.</p>
              </div>

              <div class="card card-dark">
                <div class="card-icon-round bg-yellow">⏱️</div>
                <h4>Kuis Pilihan Ganda</h4>
                <p>Generasi soal adaptif dengan timer interaktif, pencatatan skor real-time, statistik akurasi, dan review pembahasan jawaban.</p>
              </div>

              <div class="card card-dark">
                <div class="card-icon-round bg-green">🎙️</div>
                <h4>Simulasi Ujian Lisan</h4>
                <p>Tanya-jawab verbal menggunakan Web Speech API (Client) secara real-time & gratis, dievaluasi polimorfik oleh persona AI.</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Slide 10: Optimasi -->
        <div v-else-if="currentSlide === 9" class="slide slide-normal" key="9">
          <div class="slide-content">
            <div class="slide-category">ARSITEKTUR & OPTIMASI</div>
            <h2 class="slide-title">Efisiensi, Caching, & Keamanan API</h2>
            
            <div class="grid-3col text-center">
              <div class="card card-dark">
                <div class="card-icon-round bg-red">🛡️</div>
                <h4>API Rate Limiting</h4>
                <p>Mencegah eksploitasi API Gemini dengan membatasi pemanggilan hingga maksimal <strong>10 request/menit</strong> per user.</p>
              </div>

              <div class="card card-dark">
                <div class="card-icon-round bg-purple">💾</div>
                <h4>Caching Terdistribusi</h4>
                <p>Menggunakan caching bawaan Spring Boot untuk mempercepat pemuatan riwayat evaluasi tanpa membebani database MySQL.</p>
              </div>

              <div class="card card-dark">
                <div class="card-icon-round bg-green">✂️</div>
                <h4>Context Truncation</h4>
                <p>Hanya bagian teks PDF yang relevan dengan pertanyaan yang dikirim ke AI, mereduksi prompt hingga <strong>80% token savings</strong>.</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Slide 11: Demo & Kesimpulan -->
        <div v-else-if="currentSlide === 10" class="slide slide-normal" key="10">
          <div class="slide-content text-center">
            <div class="slide-category">DEMO APLIKASI & KESIMPULAN</div>
            <h2 class="slide-title">Sesi Live Demo</h2>
            <p class="slide-intro">Silakan klik tombol di bawah untuk langsung beralih ke fitur aplikasi yang ingin Anda demokan ke dosen:</p>

            <div class="demo-buttons-grid">
              <button class="btn-demo" @click="router.push('/dashboard')">
                <span>📂 Buka Dashboard</span>
              </button>
              <button class="btn-demo" @click="router.push('/flashcards')">
                <span>🎴 Flashcard Pintar</span>
              </button>
              <button class="btn-demo" @click="router.push('/quiz-mode')">
                <span>⏱️ Kuis Adaptif</span>
              </button>
              <button class="btn-demo" @click="router.push('/voice-to-speech')">
                <span>🎙️ Ujian Lisan Verbal</span>
              </button>
            </div>

            <div class="conclusion-box card card-dark text-left">
              <strong>Poin Kesimpulan Tugas Besar PBO:</strong>
              <ul class="bullet-list-short">
                <li>Mengintegrasikan konsep-konsep inti PBO (Pewarisan, Polimorfisme, Enkapsulasi, Desain Pola) dengan teknologi web modern Vue.js & Spring Boot.</li>
                <li>Menghasilkan solusi praktis bernilai guna tinggi untuk membantu produktivitas belajar mahasiswa informatika.</li>
              </ul>
            </div>
          </div>
        </div>

      </Transition>
    </main>

    <!-- Bottom Footer Control Bar -->
    <footer class="pres-footer">
      <button class="btn-ctrl" @click="prevSlide" :disabled="currentSlide === 0">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="15 18 9 12 15 6"/></svg>
        <span>Sebelumnya</span>
      </button>
      <span class="slide-indicator">Slide <strong>{{ currentSlide + 1 }}</strong> dari {{ maxSlides }}</span>
      <button class="btn-ctrl" @click="nextSlide" :disabled="currentSlide === maxSlides - 1">
        <span>Selanjutnya</span>
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline points="9 18 15 12 9 6"/></svg>
      </button>
    </footer>
  </div>
</template>

<style scoped>
/* Reset & Fullscreen Container */
.presentation-container {
  position: fixed;
  inset: 0;
  background: #0B0F19; /* Dark Mode Presentation Theme */
  color: #E2E8F0;
  display: flex;
  flex-direction: column;
  z-index: 9999; /* Full overlay */
  font-family: 'Inter', system-ui, -apple-system, sans-serif;
}

/* Header styling */
.pres-header {
  height: 60px;
  background: rgba(15, 23, 42, 0.8);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid #1E293B;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 2rem;
}

.brand {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 800;
  font-size: 0.95rem;
  color: #FFFFFF;
}
.brand .accent {
  color: #3B82F6;
}

.slide-nav-pills {
  display: flex;
  gap: 6px;
}

.nav-pill {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 1px solid #334155;
  background: transparent;
  color: #94A3B8;
  font-size: 0.75rem;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}
.nav-pill:hover {
  background: #1E293B;
  color: #FFFFFF;
  border-color: #475569;
}
.nav-pill.active {
  background: #3B82F6;
  color: #FFFFFF;
  border-color: #3B82F6;
  box-shadow: 0 0 10px rgba(59, 130, 246, 0.4);
}

.btn-exit {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  background: rgba(239, 68, 68, 0.1);
  color: #EF4444;
  border: 1px solid rgba(239, 68, 68, 0.2);
  padding: 0.375rem 0.875rem;
  border-radius: 8px;
  font-size: 0.8125rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-exit:hover {
  background: #EF4444;
  color: white;
}

/* Slide Viewport */
.slide-viewport {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2.5rem;
  position: relative;
  overflow: hidden;
}

.slide {
  width: 100%;
  max-width: 1100px;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.slide-content {
  width: 100%;
  display: flex;
  flex-direction: column;
}

/* Visual Components */
.badge {
  display: inline-block;
  align-self: center;
  padding: 0.375rem 1rem;
  background: rgba(59, 130, 246, 0.15);
  border: 1px solid rgba(59, 130, 246, 0.3);
  color: #60A5FA;
  font-size: 0.75rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  border-radius: 9999px;
  margin-bottom: 1.5rem;
}

.slide-category {
  font-size: 0.8125rem;
  font-weight: 800;
  color: #3B82F6;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  margin-bottom: 0.5rem;
}

.slide-title {
  font-size: 2.25rem;
  font-weight: 800;
  color: #FFFFFF;
  margin-bottom: 1.75rem;
  letter-spacing: -0.01em;
}

.slide-intro {
  color: #94A3B8;
  font-size: 1rem;
  line-height: 1.6;
  margin-bottom: 2rem;
  max-width: 800px;
}

/* Grids & Columns */
.grid-2col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
}

.grid-3col {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.5rem;
}

.card {
  border-radius: 16px;
  padding: 2rem;
  border: 1px solid #1E293B;
  transition: all 0.3s;
}
.card-dark {
  background: #111827;
}
.card-primary {
  background: rgba(30, 41, 59, 0.4);
  border-color: rgba(59, 130, 246, 0.2);
}

.bullet-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  padding-left: 1.25rem;
  color: #94A3B8;
  font-size: 0.9375rem;
  line-height: 1.6;
}
.bullet-list strong {
  color: #E2E8F0;
}
.bullet-list-short {
  display: flex;
  flex-direction: column;
  gap: 0.625rem;
  padding-left: 1.25rem;
  color: #94A3B8;
  font-size: 0.75rem;
  line-height: 1.5;
}

/* Slide 1 custom styles */
.slide-welcome {
  position: relative;
}
.welcome-title {
  font-size: 5rem;
  font-weight: 900;
  letter-spacing: -0.03em;
  margin: 0 0 1rem;
  line-height: 1;
}
.welcome-desc {
  font-size: 1.25rem;
  color: #94A3B8;
  max-width: 700px;
  margin: 0 auto;
  line-height: 1.6;
}
.divider {
  height: 2px;
  width: 80px;
  background: linear-gradient(90deg, #3B82F6, #10B981);
  margin: 2.5rem auto;
  border-radius: 2px;
}

/* Presenter List on Slide 1 */
.presenters-grid {
  display: flex;
  justify-content: center;
  gap: 1rem;
  flex-wrap: wrap;
  max-width: 960px;
  margin: 0 auto;
}
.presenter-card {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  background: #111827;
  border: 1px solid #1E293B;
  padding: 1rem 1.25rem;
  border-radius: 12px;
  flex: 1 1 180px;
  max-width: 240px;
  text-align: center;
  box-sizing: border-box;
}
.p-role {
  font-size: 0.5625rem;
  font-weight: 800;
  color: #64748B;
  letter-spacing: 0.08em;
}
.p-name {
  font-size: 0.9375rem;
  font-weight: 700;
  color: #FFFFFF;
  line-height: 1.2;
}
.p-nim {
  font-size: 0.75rem;
  color: #3B82F6;
  font-weight: 600;
}

.hint-text {
  position: absolute;
  bottom: 1rem;
  color: #475569;
  font-size: 0.8125rem;
}
kbd {
  background: #1E293B;
  border: 1px solid #334155;
  border-radius: 4px;
  padding: 2px 6px;
  color: #FFFFFF;
  font-size: 0.75rem;
  font-family: monospace;
}

/* Task Grid Slide 2 */
.task-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 0.75rem;
  margin-top: 1rem;
}
.task-card {
  background: #111827;
  border: 1px solid #1E293B;
  border-radius: 12px;
  padding: 1.25rem 1rem;
  display: flex;
  flex-direction: column;
  text-align: left;
  transition: all 0.3s;
}
.task-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.2);
}
.task-card.border-blue { border-top: 4px solid #3B82F6; }
.task-card.border-green { border-top: 4px solid #10B981; }
.task-card.border-red { border-top: 4px solid #EF4444; }
.task-card.border-yellow { border-top: 4px solid #F59E0B; }
.task-card.border-purple { border-top: 4px solid #8B5CF6; }

.task-member {
  font-size: 0.875rem;
  font-weight: 800;
  color: #FFFFFF;
  line-height: 1.25;
}
.task-nim {
  font-size: 0.75rem;
  color: #64748B;
  margin-top: 2px;
  font-weight: 500;
}
.task-divider {
  height: 1px;
  background: #1E293B;
  margin: 0.75rem 0;
}
.task-topic {
  font-size: 0.75rem;
  color: #E2E8F0;
  margin-bottom: 0.5rem;
}
.task-list {
  padding-left: 1rem;
  margin: 0;
  font-size: 0.75rem;
  color: #94A3B8;
  display: flex;
  flex-direction: column;
  gap: 0.375rem;
  line-height: 1.3;
}

/* Slide 3: Tech Stack custom styling */
.grid-layout-stack {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 2rem;
}
.stack-layers {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}
.stack-layer-btn {
  background: #111827;
  border: 1px solid #1E293B;
  color: #94A3B8;
  padding: 1rem 1.25rem;
  border-radius: 10px;
  font-size: 0.875rem;
  font-weight: 700;
  cursor: pointer;
  text-align: left;
  transition: all 0.2s;
}
.stack-layer-btn:hover {
  background: #1E293B;
  color: #FFFFFF;
}
.stack-layer-btn.active {
  background: rgba(30, 41, 59, 0.8);
  color: #FFFFFF;
  border-color: #3B82F6;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
}

/* Slide 4, 5, 6, 7 Code Explorer custom styling */
.code-explorer-grid {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 1.25rem;
  height: 380px;
}
.code-explorer-grid .tabs {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.code-explorer-grid .tabs button {
  background: #111827;
  border: 1px solid #1E293B;
  color: #94A3B8;
  padding: 0.75rem 1rem;
  border-radius: 8px;
  font-size: 0.8125rem;
  font-weight: 600;
  text-align: left;
  cursor: pointer;
  font-family: monospace;
  transition: all 0.2s;
}
.code-explorer-grid .tabs button:hover {
  background: #1E293B;
  color: white;
}
.code-explorer-grid .tabs button.active {
  background: #1E293B;
  color: #3B82F6;
  border-color: #3B82F6;
}
.code-viewport {
  background: #090D16;
  border: 1px solid #1E293B;
  border-radius: 12px;
  padding: 1.25rem;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 0.8125rem;
  overflow: auto;
  text-align: left;
  height: 100%;
}
.code-viewport pre { margin: 0; }

/* Code highlight classes */
.keyword { color: #F43F5E; font-weight: bold; }
.type { color: #60A5FA; }
.comment { color: #64748B; font-style: italic; }
.string { color: #10B981; }
.annotation { color: #EC4899; }

/* Slide 5: Inheritance visual tree styling */
.inheritance-tree-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
  align-items: center;
}
.visual-tree {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.tree-node {
  background: #111827;
  border: 1.5px solid #1E293B;
  border-radius: 12px;
  padding: 1rem 1.25rem;
  text-align: center;
  width: 240px;
}
.tree-node.parent {
  border-color: #64748B;
  background: rgba(30, 41, 59, 0.6);
}
.tree-node.child {
  width: 200px;
}
.tree-node strong {
  display: block;
  font-size: 0.875rem;
  margin-bottom: 4px;
}
.tree-node .fields {
  display: block;
  font-size: 0.6875rem;
  color: #64748B;
}
.tree-node .tag {
  display: inline-block;
  font-size: 0.625rem;
  font-weight: 700;
  padding: 2px 6px;
  border-radius: 4px;
  margin-top: 6px;
}
.tree-connectors {
  position: relative;
  width: 240px;
  height: 40px;
}
.tree-connectors .line {
  position: absolute;
  top: 0;
  height: 40px;
  border-top: 1.5px solid #475569;
}
.tree-connectors .line-left {
  left: 50%;
  width: 25%;
  border-left: 1.5px solid #475569;
}
.tree-connectors .line-right {
  right: 50%;
  width: 25%;
  border-right: 1.5px solid #475569;
}
.sub-nodes {
  display: flex;
  gap: 1.5rem;
}
.db-table-card h4 {
  margin: 0 0 1rem;
  font-size: 0.9375rem;
}
.table-scroll {
  overflow-x: auto;
}
.simple-db-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.75rem;
}
.simple-db-table th, .simple-db-table td {
  border: 1px solid #1E293B;
  padding: 8px 10px;
  text-align: left;
}
.simple-db-table th {
  background: #1E293B;
  color: #94A3B8;
}
.simple-db-table td.highlight {
  background: rgba(30, 41, 59, 0.4);
}

/* MCQ icons */
.card-icon-round {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: #1E293B;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  margin: 0 auto 1rem;
}

/* Demo buttons */
.demo-buttons-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1rem;
  margin: 1.5rem 0 2rem;
}
.btn-demo {
  background: #1E293B;
  border: 1.5px solid #334155;
  color: white;
  padding: 1rem;
  border-radius: 12px;
  font-size: 0.875rem;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.25s;
}
.btn-demo:hover {
  background: #3B82F6;
  border-color: #3B82F6;
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.3);
}
.conclusion-box {
  font-size: 0.875rem;
}
.conclusion-box strong {
  display: block;
  margin-bottom: 0.5rem;
  color: #3B82F6;
}

/* Global Helpers */
.text-center { text-align: center; }
.text-left { text-align: left; }
.text-muted { color: #475569; }
.flex-column { display: flex; flex-direction: column; }
.justify-center { justify-content: center; }
.justify-between { justify-content: space-between; }
.flex-align-center { display: flex; align-items: center; }
.self-start { align-self: flex-start; }
.gap-2 { gap: 0.5rem; }
.font-bold { font-weight: 700; }

.accent-blue { color: #60A5FA; }
.accent-green { color: #34D399; }
.accent-red { color: #F87171; }
.accent-yellow { color: #FBBF24; }
.accent-purple { color: #C084FC; }

.text-blue { color: #3B82F6; }
.text-green { color: #10B981; }

.bg-blue { background: rgba(59, 130, 246, 0.2); color: #60A5FA; }
.bg-green { background: rgba(16, 185, 129, 0.2); color: #34D399; }
.bg-yellow { background: rgba(245, 158, 11, 0.2); color: #FBBF24; }
.bg-red { background: rgba(239, 68, 68, 0.2); color: #F87171; }
.bg-purple { background: rgba(139, 92, 246, 0.2); color: #C084FC; }

/* Slide Footer controls */
.pres-footer {
  height: 60px;
  background: #0B0F19;
  border-top: 1px solid #1E293B;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 2rem;
}

.btn-ctrl {
  background: #111827;
  border: 1px solid #1E293B;
  color: #94A3B8;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  font-size: 0.8125rem;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.375rem;
  transition: all 0.2s;
}
.btn-ctrl:hover:not(:disabled) {
  background: #1E293B;
  color: #FFFFFF;
}
.btn-ctrl:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.slide-indicator {
  font-size: 0.875rem;
  color: #64748B;
}
.slide-indicator strong {
  color: #FFFFFF;
}

/* Animations */
.slide-fade-enter-active {
  transition: all 0.35s ease-out;
}
.slide-fade-leave-active {
  transition: all 0.25s ease-in;
}
.slide-fade-enter-from {
  transform: translateX(30px);
  opacity: 0;
}
.slide-fade-leave-to {
  transform: translateX(-30px);
  opacity: 0;
}

.hero-gradient {
  position: absolute;
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(59, 130, 246, 0.1) 0%, rgba(0,0,0,0) 70%);
  z-index: 0;
}

/* Responsive Task Grid */
@media (max-width: 960px) {
  .task-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 1rem;
  }
  .task-card {
    max-width: 100%;
  }
}
@media (max-width: 600px) {
  .task-grid {
    grid-template-columns: 1fr;
  }
}
</style>
