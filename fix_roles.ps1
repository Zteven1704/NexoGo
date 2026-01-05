# Script para corregir todos los roles en el proyecto NexoGo

Write-Host "🔧 Iniciando corrección masiva de roles..." -ForegroundColor Green

# Lista de archivos a corregir
$files = @(
    "app/src/main/java/com/example/nexogo/data/AppDataStore.kt",
    "app/src/main/java/com/example/nexogo/firebase/FirebaseFirestoreManager.kt",
    "app/src/main/java/com/example/nexogo/firebase/FirebaseTestData.kt",
    "app/src/main/java/com/example/nexogo/modules/auth/AuthRepository.kt",
    "app/src/main/java/com/example/nexogo/modules/auth/AuthViewModel.kt",
    "app/src/main/java/com/example/nexogo/modules/auth/RegisterScreen.kt",
    "app/src/main/java/com/example/nexogo/modules/dashboard/DashboardScreen.kt",
    "app/src/main/java/com/example/nexogo/modules/dashboard/DashboardViewModel.kt",
    "app/src/main/java/com/example/nexogo/ui/screens/auth/ModernLoginScreen.kt",
    "app/src/main/java/com/example/nexogo/ui/screens/auth/UltraSimpleLoginScreen.kt",
    "app/src/main/java/com/example/nexogo/ui/screens/chat/NewChatScreen.kt",
    "app/src/main/java/com/example/nexogo/ui/screens/settings/SettingsScreen.kt",
    "app/src/main/java/com/example/nexogo/viewmodel/AuthViewModel.kt",
    "app/src/main/java/com/example/nexogo/viewmodel/ChatViewModel.kt"
)

# Reemplazos a realizar
$replacements = @{
    "UserRole\.PATIENT" = "UserRole.USER"
    "UserRole\.ASSISTANT" = "UserRole.VET_ASSISTANT"
    "UserRole\.PENDING_VET" = "UserRole.VET"
    "UserRole\.PENDING_ASSISTANT" = "UserRole.VET_ASSISTANT"
    "PATIENT" = "USER"
    "ASSISTANT" = "VET_ASSISTANT"
    "PENDING_VET" = "VET"
    "PENDING_ASSISTANT" = "VET_ASSISTANT"
}

foreach ($file in $files) {
    if (Test-Path $file) {
        Write-Host "📝 Procesando: $file" -ForegroundColor Yellow
        
        $content = Get-Content $file -Raw
        
        foreach ($search in $replacements.Keys) {
            $replace = $replacements[$search]
            $content = $content -replace $search, $replace
        }
        
        Set-Content $file -Value $content -NoNewline
        Write-Host "✅ Corregido: $file" -ForegroundColor Green
    } else {
        Write-Host "❌ Archivo no encontrado: $file" -ForegroundColor Red
    }
}

Write-Host "🎉 Corrección masiva completada!" -ForegroundColor Green
Write-Host "📋 Ejecutando compilación..." -ForegroundColor Cyan
