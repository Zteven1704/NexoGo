# Script para aplicar índices de Firestore
# Ejecutar desde la raíz del proyecto

Write-Host "🔧 Aplicando índices de Firestore..." -ForegroundColor Cyan

# Verificar que firebase-tools esté instalado
if (-not (Get-Command firebase -ErrorAction SilentlyContinue)) {
    Write-Host "❌ Firebase CLI no está instalado" -ForegroundColor Red
    Write-Host "💡 Instalar con: npm install -g firebase-tools" -ForegroundColor Yellow
    exit 1
}

# Verificar que estemos en un proyecto Firebase
if (-not (Test-Path "firebase.json")) {
    Write-Host "⚠️ No se encontró firebase.json" -ForegroundColor Yellow
    Write-Host "💡 Inicializar Firebase con: firebase init" -ForegroundColor Yellow
}

# Desplegar índices
Write-Host "📤 Desplegando índices de Firestore..." -ForegroundColor Cyan
firebase deploy --only firestore:indexes

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Índices desplegados exitosamente" -ForegroundColor Green
    Write-Host "⏳ Los índices pueden tardar varios minutos en crearse" -ForegroundColor Yellow
    Write-Host "💡 Verificar estado en: https://console.firebase.google.com/project/nexogo-82003/firestore/indexes" -ForegroundColor Cyan
} else {
    Write-Host "❌ Error al desplegar índices" -ForegroundColor Red
    Write-Host "💡 Alternativa: Crear índices manualmente usando el enlace del error" -ForegroundColor Yellow
}




