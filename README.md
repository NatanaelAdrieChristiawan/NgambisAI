# 🎓 Ngambis.ai

> Platform belajar cerdas berbasis AI — Upload PDF, Generate Quiz, dan Simulasi Ujian Lisan.

## Tech Stack

| Layer    | Technology                          |
|----------|-------------------------------------|
| Backend  | Spring Boot 3.4, Spring Security, JWT |
| Frontend | Vue.js 3, Vite, Pinia              |
| Database | MySQL 8.0                           |
| AI       | Google Gemini API                   |
| Auth     | Local (email/password) + Google OAuth2 |

## Struktur Proyek (Monorepo)

```
Tubes/
├── src/main/java/         # Backend (Spring Boot)
├── src/main/resources/    # Config (application.yml)
├── frontend/              # Frontend (Vue.js + Vite)
│   ├── src/views/         # Halaman UI
│   ├── src/stores/        # Pinia state management
│   └── src/services/      # API service layer
├── docker-compose.yml     # MySQL via Docker
├── pom.xml                # Maven dependencies
└── start-backend.sh       # Script jalankan backend
```

## Cara Menjalankan

### 1. Prerequisites

- Java 17+
- Node.js 18+
- Docker Desktop (untuk MySQL)

### 2. Jalankan MySQL

```bash
docker-compose up -d
```

### 3. Setup Environment

Buat file `.env` di root project:

```env
DB_HOST=localhost
DB_PORT=3306
DB_NAME=ngambis_ai
DB_USERNAME=root
DB_PASSWORD=

GEMINI_API_KEY=your_gemini_api_key
JWT_SECRET=your_jwt_secret_min_64_chars

GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
OAUTH2_FRONTEND_REDIRECT=http://localhost:5173/oauth2/callback
```

### 4. Jalankan Backend

```bash
chmod +x start-backend.sh
./start-backend.sh
```

> ⚠️ **Jangan** gunakan `./mvnw spring-boot:run` langsung — gunakan `start-backend.sh` agar `.env` terbaca.

Backend berjalan di: `http://localhost:8080`

### 5. Jalankan Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend berjalan di: `http://localhost:5173`

## Google OAuth2 Setup

1. Buka [Google Cloud Console](https://console.cloud.google.com/)
2. Buat OAuth 2.0 Client ID (Web application)
3. Set Authorized JavaScript origins:
   - `http://localhost:5173`
   - `http://localhost:8080`
4. Set Authorized redirect URIs:
   - `http://localhost:8080/login/oauth2/code/google`
5. Copy Client ID & Secret ke file `.env`

## API Documentation

Setelah backend berjalan, buka Swagger UI:

```
http://localhost:8080/swagger-ui.html
```

## Tim Pengembang

Ngambis.ai — Tugas Besar PBO Semester 4
