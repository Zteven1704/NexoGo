# ========================================
# SCRIPT PARA APLICAR REGLAS DE FIRESTORE
# NexoGo - Solución PERMISSION_DENIED
# ========================================

Write-Host "🔥 APLICANDO REGLAS DE FIRESTORE PARA NEXOGO" -ForegroundColor Yellow
Write-Host "===============================================" -ForegroundColor Yellow

# Verificar si Firebase CLI está instalado
Write-Host "`n🔍 Verificando Firebase CLI..." -ForegroundColor Cyan
try {
    $firebaseVersion = firebase --version 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Firebase CLI encontrado: $firebaseVersion" -ForegroundColor Green
        $hasFirebaseCLI = $true
    } else {
        $hasFirebaseCLI = $false
    }
} catch {
    $hasFirebaseCLI = $false
}

if ($hasFirebaseCLI) {
    Write-Host "`n🚀 Aplicando reglas con Firebase CLI..." -ForegroundColor Green
    try {
        firebase deploy --only firestore:rules
        Write-Host "✅ Reglas aplicadas correctamente con Firebase CLI" -ForegroundColor Green
    } catch {
        Write-Host "❌ Error al aplicar reglas con Firebase CLI" -ForegroundColor Red
        Write-Host "💡 Continuando con método manual..." -ForegroundColor Yellow
        $hasFirebaseCLI = $false
    }
}

if (-not $hasFirebaseCLI) {
    Write-Host "`n📋 MÉTODO MANUAL - Sigue estos pasos:" -ForegroundColor Yellow
    Write-Host "=====================================" -ForegroundColor Yellow
    
    Write-Host "`n1️⃣ Abre Firebase Console:" -ForegroundColor Cyan
    Write-Host "   https://console.firebase.google.com/" -ForegroundColor White
    
    Write-Host "`n2️⃣ Selecciona tu proyecto NexoGo" -ForegroundColor Cyan
    
    Write-Host "`n3️⃣ Ve a Firestore Database > Reglas" -ForegroundColor Cyan
    
    Write-Host "`n4️⃣ Copia y pega estas reglas:" -ForegroundColor Cyan
    Write-Host "=================================" -ForegroundColor Yellow
    
    # Mostrar las reglas
    $rulesContent = Get-Content "firestore_rules_final.rules" -Raw
    Write-Host $rulesContent -ForegroundColor White
    
    Write-Host "`n5️⃣ Haz clic en 'Publicar'" -ForegroundColor Cyan
    
    Write-Host "`n6️⃣ Verifica que las reglas se aplicaron correctamente" -ForegroundColor Cyan
}

Write-Host "`n🧪 PROBANDO CONEXIÓN..." -ForegroundColor Yellow
Write-Host "=======================" -ForegroundColor Yellow

Write-Host "`n📱 Ejecuta la app y revisa Logcat para ver si el error PERMISSION_DENIED desaparece" -ForegroundColor Green
Write-Host "🔍 Busca en Logcat con el filtro: 'FirebaseDiagnostics' o 'FirestoreRulesUpdater'" -ForegroundColor Green

Write-Host "`n✅ PROCESO COMPLETADO" -ForegroundColor Green
Write-Host "=====================" -ForegroundColor Green

Read-Host "`nPresiona Enter para continuar..."

