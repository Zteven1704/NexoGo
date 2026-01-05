# Script para solucionar PERMISSION_DENIED en Firestore
# Ejecutar como administrador

Write-Host "🔧 SOLUCIONANDO ERROR PERMISSION_DENIED EN FIRESTORE" -ForegroundColor Yellow
Write-Host "=================================================" -ForegroundColor Yellow

Write-Host "`n📋 OPCIONES DISPONIBLES:" -ForegroundColor Green
Write-Host "1. Aplicar reglas desde Firebase Console (RECOMENDADO)"
Write-Host "2. Instalar Firebase CLI y aplicar reglas"
Write-Host "3. Ver reglas actuales"
Write-Host "4. Salir"

$opcion = Read-Host "`nSelecciona una opción (1-4)"

switch ($opcion) {
    "1" {
        Write-Host "`n🌐 ABRIENDO FIREBASE CONSOLE..." -ForegroundColor Blue
        Write-Host "1. Ve a: https://console.firebase.google.com/"
        Write-Host "2. Selecciona tu proyecto NexoGo"
        Write-Host "3. Ve a Firestore Database > Rules"
        Write-Host "4. Copia el contenido de 'firestore_rules_simple.rules'"
        Write-Host "5. Pega en el editor y haz clic en 'Publish'"
        
        # Abrir Firebase Console
        Start-Process "https://console.firebase.google.com/"
        
        Write-Host "`n✅ Después de aplicar las reglas, reinicia la app y prueba agendar una cita." -ForegroundColor Green
    }
    
    "2" {
        Write-Host "`n📦 INSTALANDO FIREBASE CLI..." -ForegroundColor Blue
        
        # Verificar si Node.js está instalado
        try {
            $nodeVersion = node --version
            Write-Host "✅ Node.js encontrado: $nodeVersion" -ForegroundColor Green
            
            # Instalar Firebase CLI
            Write-Host "Instalando Firebase CLI..." -ForegroundColor Yellow
            npm install -g firebase-tools
            
            Write-Host "`n🔐 AUTENTICÁNDOSE EN FIREBASE..." -ForegroundColor Blue
            firebase login
            
            Write-Host "`n🚀 APLICANDO REGLAS..." -ForegroundColor Blue
            firebase deploy --only firestore:rules
            
            Write-Host "`n✅ Reglas aplicadas exitosamente!" -ForegroundColor Green
        }
        catch {
            Write-Host "❌ Node.js no está instalado. Instálalo desde: https://nodejs.org/" -ForegroundColor Red
            Write-Host "Luego ejecuta este script nuevamente." -ForegroundColor Yellow
        }
    }
    
    "3" {
        Write-Host "`n📄 REGLAS ACTUALES:" -ForegroundColor Blue
        Write-Host "==================" -ForegroundColor Blue
        Get-Content "firestore_rules_simple.rules" | Write-Host -ForegroundColor Cyan
    }
    
    "4" {
        Write-Host "`n👋 ¡Hasta luego!" -ForegroundColor Green
        exit
    }
    
    default {
        Write-Host "`n❌ Opción inválida. Por favor selecciona 1-4." -ForegroundColor Red
    }
}

Write-Host "`n🎯 DESPUÉS DE APLICAR LAS REGLAS:" -ForegroundColor Yellow
Write-Host "1. Reinicia la aplicación en el dispositivo"
Write-Host "2. Intenta agendar una cita"
Write-Host "3. Verifica que no aparezcan errores de permisos"

Write-Host "`n📱 Si sigues teniendo problemas:" -ForegroundColor Red
Write-Host "- Verifica que el usuario esté autenticado"
Write-Host "- Revisa la consola de Firebase"
Write-Host "- Asegúrate de que las reglas se aplicaron correctamente"

Read-Host "`nPresiona Enter para continuar..."

