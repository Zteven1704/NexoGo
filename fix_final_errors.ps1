# Script para corregir errores finales del sistema de autenticación

Write-Host "🔧 Corrigiendo errores finales..." -ForegroundColor Green

# Correcciones específicas
$corrections = @{
    "VET_VET_ASSISTANT" = "VET_ASSISTANT"
    "getUSERsByOwner" = "getUsersByOwner"
    "getChatsByUSER" = "getChatsByUser"
    "relatedUSERId" = "relatedUserId"
    "loadUSERs" = "loadUsers"
    "saveUSERs" = "saveUsers"
}

# Archivos a corregir
$files = @(
    "app/src/main/java/com/example/nexogo/modules/auth/AuthViewModel.kt",
    "app/src/main/java/com/example/nexogo/modules/auth/RegisterScreen.kt",
    "app/src/main/java/com/example/nexogo/modules/dashboard/DashboardScreen.kt",
    "app/src/main/java/com/example/nexogo/modules/dashboard/DashboardViewModel.kt",
    "app/src/main/java/com/example/nexogo/ui/screens/auth/ModernLoginScreen.kt",
    "app/src/main/java/com/example/nexogo/ui/screens/chat/NewChatScreen.kt",
    "app/src/main/java/com/example/nexogo/ui/screens/settings/SettingsScreen.kt",
    "app/src/main/java/com/example/nexogo/viewmodel/AuthViewModel.kt",
    "app/src/main/java/com/example/nexogo/viewmodel/ChatViewModel.kt",
    "app/src/main/java/com/example/nexogo/viewmodel/PatientViewModel.kt"
)

foreach ($file in $files) {
    if (Test-Path $file) {
        Write-Host "📝 Procesando: $file" -ForegroundColor Yellow
        
        $content = Get-Content $file -Raw
        
        foreach ($search in $corrections.Keys) {
            $replace = $corrections[$search]
            $content = $content -replace $search, $replace
        }
        
        Set-Content $file -Value $content -NoNewline
        Write-Host "✅ Corregido: $file" -ForegroundColor Green
    }
}

Write-Host "🎉 Correcciones finales completadas!" -ForegroundColor Green
