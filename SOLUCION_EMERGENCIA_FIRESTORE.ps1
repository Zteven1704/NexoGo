# ========================================
# SOLUCIÓN DE EMERGENCIA PARA FIRESTORE
# NexoGo - Error PERMISSION_DENIED
# ========================================

Write-Host "🚨 SOLUCIÓN DE EMERGENCIA PARA PERMISSION_DENIED" -ForegroundColor Red
Write-Host "=================================================" -ForegroundColor Red

Write-Host "`n⚠️  ATENCIÓN: Esta solución aplica reglas MUY PERMISIVAS" -ForegroundColor Yellow
Write-Host "   Solo para desarrollo - NO usar en producción" -ForegroundColor Yellow

Write-Host "`n🔧 APLICANDO REGLAS DE EMERGENCIA..." -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan

Write-Host "`n1️⃣ Abre Firebase Console:" -ForegroundColor Green
Write-Host "   https://console.firebase.google.com/" -ForegroundColor White

Write-Host "`n2️⃣ Selecciona tu proyecto NexoGo" -ForegroundColor Green

Write-Host "`n3️⃣ Ve a Firestore Database > Reglas" -ForegroundColor Green

Write-Host "`n4️⃣ REEMPLAZA TODO el contenido con estas reglas de emergencia:" -ForegroundColor Green
Write-Host "===============================================================" -ForegroundColor Yellow

# Mostrar las reglas de emergencia
$emergencyRules = Get-Content "firestore_rules_emergency.rules" -Raw
Write-Host $emergencyRules -ForegroundColor White

Write-Host "`n5️⃣ Haz clic en 'Publicar'" -ForegroundColor Green

Write-Host "`n6️⃣ Reinicia la app completamente" -ForegroundColor Green

Write-Host "`n🧪 VERIFICACIÓN:" -ForegroundColor Cyan
Write-Host "===============" -ForegroundColor Cyan
Write-Host "• La app NO debería cerrarse más" -ForegroundColor Green
Write-Host "• Los errores PERMISSION_DENIED deberían desaparecer" -ForegroundColor Green
Write-Host "• Revisa Logcat para confirmar que funciona" -ForegroundColor Green

Write-Host "`n📱 PRUEBA LA APP:" -ForegroundColor Yellow
Write-Host "=================" -ForegroundColor Yellow
Write-Host "1. Abre la app" -ForegroundColor White
Write-Host "2. Intenta agendar una cita" -ForegroundColor White
Write-Host "3. Verifica que NO se cierre" -ForegroundColor White
Write-Host "4. Revisa Logcat para confirmar éxito" -ForegroundColor White

Write-Host "`n🔒 IMPORTANTE - SEGURIDAD:" -ForegroundColor Red
Write-Host "===========================" -ForegroundColor Red
Write-Host "• Estas reglas permiten TODO sin autenticación" -ForegroundColor Yellow
Write-Host "• SOLO para desarrollo y testing" -ForegroundColor Yellow
Write-Host "• Cambiar a reglas más restrictivas en producción" -ForegroundColor Yellow

Write-Host "`n✅ PROCESO COMPLETADO" -ForegroundColor Green
Write-Host "=====================" -ForegroundColor Green

Read-Host "`nPresiona Enter para continuar..."

