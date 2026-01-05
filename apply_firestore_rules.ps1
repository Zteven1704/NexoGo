# Script para aplicar reglas de Firestore
# Asegúrate de tener Firebase CLI instalado y autenticado

Write-Host "Aplicando reglas de Firestore..." -ForegroundColor Green

# Aplicar reglas de Firestore
firebase deploy --only firestore:rules

Write-Host "Reglas de Firestore aplicadas exitosamente!" -ForegroundColor Green
Write-Host "Ahora puedes probar la funcionalidad de citas." -ForegroundColor Yellow

