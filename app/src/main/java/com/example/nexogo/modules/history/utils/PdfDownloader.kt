package com.example.nexogo.modules.history.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PdfDownloader(private val context: Context) {
    
    companion object {
        private const val TAG = "NEXOGO_PDF"
    }
    
    fun downloadPdf(pdfBytes: ByteArray, fileName: String = "historial_clinico.pdf"): Boolean {
        return try {
            Log.d(TAG, "Iniciando descarga de PDF: $fileName")
            
            // Intentar usar el directorio de descargas externo primero
            val downloadsDir = try {
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "NexoGo")
            } catch (e: Exception) {
                Log.w(TAG, "No se puede acceder a descargas externas, usando directorio interno: ${e.message}")
                File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "NexoGo")
            }
            
            if (!downloadsDir.exists()) {
                val created = downloadsDir.mkdirs()
                Log.d(TAG, "Directorio creado: $created, ruta: ${downloadsDir.absolutePath}")
            }
            
            // Generar nombre único para el archivo
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val uniqueFileName = "historial_${timestamp}.pdf"
            val file = File(downloadsDir, uniqueFileName)
            
            // Escribir bytes al archivo
            FileOutputStream(file).use { fos ->
                fos.write(pdfBytes)
            }
            
            Log.d(TAG, "PDF guardado exitosamente en: ${file.absolutePath}")
            Log.d(TAG, "Tamaño del archivo: ${file.length()} bytes")
            
            // Abrir el archivo con la aplicación predeterminada
            openPdfFile(file)
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error descargando PDF: ${e.message}")
            e.printStackTrace()
            false
        }
    }
    
    private fun openPdfFile(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            context.startActivity(intent)
            Log.d(TAG, "PDF abierto con aplicación predeterminada")
        } catch (e: Exception) {
            Log.e(TAG, "Error abriendo PDF: ${e.message}")
        }
    }
    
    fun sharePdf(pdfBytes: ByteArray, fileName: String = "historial_clinico.pdf"): Boolean {
        return try {
            Log.d(TAG, "Compartiendo PDF: $fileName")
            
            // Crear archivo temporal
            val tempDir = File(context.cacheDir, "temp_pdfs")
            if (!tempDir.exists()) {
                tempDir.mkdirs()
            }
            
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val tempFile = File(tempDir, "historial_${timestamp}.pdf")
            
            FileOutputStream(tempFile).use { fos ->
                fos.write(pdfBytes)
            }
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )
            
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Historial Clínico - NexoGo")
                putExtra(Intent.EXTRA_TEXT, "Historial clínico generado desde NexoGo")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            val chooserIntent = Intent.createChooser(shareIntent, "Compartir Historial Clínico")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)
            
            Log.d(TAG, "PDF compartido exitosamente")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error compartiendo PDF: ${e.message}")
            false
        }
    }
}
