#!/bin/bash
# ========================================
# Ngambis.ai — Backend Startup Script
# ========================================
# Loads environment variables from .env file
# then starts the Spring Boot application.
#
# Usage: ./start-backend.sh
# ========================================

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ENV_FILE="$SCRIPT_DIR/.env"

# Load .env file if it exists
if [ -f "$ENV_FILE" ]; then
  echo "📂 Loading environment from .env..."
  set -a
  source "$ENV_FILE"
  set +a
  echo "✅ Environment loaded successfully"
else
  echo "⚠️  No .env file found at $ENV_FILE"
  echo "   Create one based on .env.example"
  exit 1
fi

echo ""
echo "🚀 Starting Ngambis.ai Backend..."
echo "   Database: ${DB_HOST}:${DB_PORT}/${DB_NAME}"
echo "   Server:   http://localhost:${SERVER_PORT:-8080}"
echo ""

# Run Spring Boot
cd "$SCRIPT_DIR"
./mvnw spring-boot:run
