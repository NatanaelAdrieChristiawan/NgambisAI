# 🎓 Penerapan Konsep PBO (OOP) pada Proyek Ngambis.ai

Dokumen ini menjelaskan penerapan 4 pilar utama Pemrograman Berorientasi Objek (PBO) serta design pattern yang digunakan di backend Java Spring Boot **Ngambis.ai** sebagai referensi pengerjaan Tugas Besar (Tubes).

---

## 1. Inheritance (Pewarisan)
Pewarisan digunakan untuk mendefinisikan kelas anak (`subclass`) yang mewarisi sifat, atribut, dan perilaku dari kelas induk (`superclass`).

*   **Pewarisan Entity Database (JPA Single Table Inheritance):**
    *   **Kelas Induk (Superclass):** `public abstract class QuizItem` di file [`QuizItem.java`](./src/main/java/com/ngambis/ai/models/QuizItem.java). Menampung properti umum seperti `id`, `questionText`, `referenceText`, dll.
    *   **Kelas Anak (Subclass):** 
        *   `public class MultipleChoiceItem extends QuizItem` di file [`MultipleChoiceItem.java`](./src/main/java/com/ngambis/ai/models/MultipleChoiceItem.java) (menambahkan kolom spesifik `options` dan `correctAnswer`).
        *   `public class EssayItem extends QuizItem` di file [`EssayItem.java`](./src/main/java/com/ngambis/ai/models/EssayItem.java) (untuk pertanyaan lisan/esai).
    *   **Penerapan di Database:** Menggunakan anotasi JPA `@Inheritance(strategy = InheritanceType.SINGLE_TABLE)` dengan kolom diskriminator (`item_type`) untuk memetakan objek secara polimorfis ke satu tabel database (`quiz_items`).

*   **Pewarisan pada Custom Exception:**
    *   Kelas [`ResourceNotFoundException`](./src/main/java/com/ngambis/ai/exceptions/ResourceNotFoundException.java) (dan kelas exception kustom lainnya) mewarisi kelas bawaan Java `RuntimeException` (`extends RuntimeException`).

*   **Pewarisan Interface (Inheritance Interface):**
    *   Interface [`UserRepository`](./src/main/java/com/ngambis/ai/repositories/UserRepository.java) mewarisi interface `JpaRepository<User, UUID>` (`extends JpaRepository`) untuk mendapatkan fungsionalitas query database JPA secara otomatis.

---

## 2. Polymorphism (Polimorfisme)
Polimorfisme memungkinkan suatu objek atau metode memiliki banyak bentuk tindakan yang berbeda, baik pada saat kompilasi (*compile-time*) maupun saat program berjalan (*runtime*).

*   **Runtime Polymorphism (Method Overriding & Strategy Pattern):**
    *   **Strategy Pattern** digunakan untuk mengubah gaya evaluasi AI berdasarkan persona (dosen killer/kakak tingkat) secara dinamis di runtime.
    *   Interface [`PersonaStrategy`](./src/main/java/com/ngambis/ai/strategies/PersonaStrategy.java) mendefinisikan metode kontrak `generateEvaluationPrompt(...)`.
    *   Kelas konkret [`FriendlySeniorStrategy`](./src/main/java/com/ngambis/ai/strategies/FriendlySeniorStrategy.java) dan [`StrictLecturerStrategy`](./src/main/java/com/ngambis/ai/strategies/StrictLecturerStrategy.java) mengimplementasikan interface tersebut dan meng-*override* metode tersebut dengan cara pembuatan prompt AI masing-masing.
    *   Pemanggilan metode dilakukan secara polimorfis pada [`SimulatorService`](./src/main/java/com/ngambis/ai/services/SimulatorService.java):
        ```java
        PersonaStrategy strategy = resolveStrategy(session.getPersonaType());
        // Dynamic binding: Java menentukan di runtime implementasi mana yang akan dieksekusi
        String prompt = strategy.generateEvaluationPrompt(...);
        ```
    *   **Implementasi Interface Security:** Kelas [`User`](./src/main/java/com/ngambis/ai/models/User.java) mengimplementasikan interface `UserDetails` dan meng-override metode runtime seperti `getAuthorities()`, `getPassword()`, dll.

*   **Compile-time Polymorphism (Method Overloading):**
    *   **Konstruktor Overloading:** Dapat dilihat pada konstruktor [`ResourceNotFoundException`](./src/main/java/com/ngambis/ai/exceptions/ResourceNotFoundException.java) yang memiliki dua versi konstruktor dengan jumlah/tipe parameter berbeda:
        ```java
        // Versi 1
        public ResourceNotFoundException(String message) { ... }
        
        // Versi 2 (Overloaded)
        public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) { ... }
        ```
    *   **Method Overloading pada Service:** Dapat dilihat pada [`PdfExtractionService`](./src/main/java/com/ngambis/ai/services/PdfExtractionService.java) di mana method `extractAndChunk` memiliki dua tanda tangan method (*signature*) yang berbeda:
        ```java
        // Versi 1: Menerima file dan custom chunk size
        public List<String> extractAndChunk(MultipartFile file, int chunkSize) { ... }
        
        // Versi 2 (Overloaded): Menerima file saja (menggunakan default chunk size)
        public List<String> extractAndChunk(MultipartFile file) { ... }
        ```

---

## 3. Abstraction (Abstraksi)
Abstraksi digunakan untuk menyembunyikan detail implementasi yang rumit dan hanya memperlihatkan fungsionalitas esensial kepada komponen luar.

*   **Abstract Class:** Kelas [`QuizItem`](./src/main/java/com/ngambis/ai/models/QuizItem.java) dideklarasikan sebagai `public abstract class`. Kelas ini tidak dapat diinstansiasi menjadi objek secara langsung, melainkan harus diturunkan terlebih dahulu.
*   **Interface:** Interface [`PersonaStrategy`](./src/main/java/com/ngambis/ai/strategies/PersonaStrategy.java) menyembunyikan detail teknis kalimat instruksi prompt dari kelas service yang menggunakannya.
*   **Dependency Injection & Loose Coupling:** Hubungan antar objek dirancang menggunakan prinsip **Dependency Inversion** (bergantung pada abstraksi, bukan konkrit). 
    *   Di dalam [`SimulatorService`](./src/main/java/com/ngambis/ai/services/SimulatorService.java), service bergantung pada interface `PersonaStrategy`, bukan pada kelas implementasi konkretnya.
    *   Dependency diinjeksikan secara otomatis oleh Spring Boot melalui konstruktor (*Constructor Injection* dibantu anotasi Lombok `@RequiredArgsConstructor`) pada objek yang dideklarasikan sebagai `private final`, sehingga menghindari instansiasi manual menggunakan kata kunci `new` yang kaku.

---

## 4. Encapsulation (Enkapsulasi)
Enkapsulasi dicapai dengan menyembunyikan variabel kelas dari akses luar langsung (menggunakan access modifier `private`) dan menyediakan akses terkontrol lewat getter dan setter.

*   **Penyembunyian Status & Akses Terkontrol:** Semua variabel anggota pada entitas model seperti [`User`](./src/main/java/com/ngambis/ai/models/User.java), `QuizSession`, dan `Evaluation` bersifat `private`. Akses baca dan tulis dibatasi melalui metode getter/setter publik yang dikelola secara bersih oleh **Lombok** lewat anotasi tingkat kelas seperti `@Data`, `@Getter`, `@Setter`, atau `@Builder`.
*   **Enkapsulasi Relasi Asosiasi (Composition & Aggregation):** Koleksi objek relasional (JPA `@OneToMany` dan `@ManyToOne`) dibungkus rapat di dalam kelas induk.
    *   Misalnya, kelas [`User`](./src/main/java/com/ngambis/ai/models/User.java) menyembunyikan koleksi dokumen dan sesi belajar miliknya dengan modifier `private` dan menginstansiasinya sebagai list kosong bawaan untuk menjaga keamanan data dari manipulasi langsung luar kelas:
        ```java
        private List<Document> documents = new ArrayList<>();
        private List<QuizSession> quizSessions = new ArrayList<>();
        ```
