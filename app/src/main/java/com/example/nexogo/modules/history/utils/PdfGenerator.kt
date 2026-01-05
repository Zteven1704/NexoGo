package com.example.nexogo.modules.history.utils

import android.content.Context
import android.net.Uri
import com.example.nexogo.modules.history.models.ClinicalRecord
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PdfGenerator(private val context: Context) {
    
    fun generateClinicalRecordPdf(record: ClinicalRecord): ByteArray {
        // TODO: Implement with iText 7
        return ByteArray(0)
    }
    
    fun generateSimplePdf(record: ClinicalRecord): ByteArray {
        // TODO: Implement with iText 7
        return ByteArray(0)
    }
}