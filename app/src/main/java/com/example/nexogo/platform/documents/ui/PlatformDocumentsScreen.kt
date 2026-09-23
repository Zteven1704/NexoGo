package com.example.nexogo.platform.documents.ui

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nexogo.platform.company.session.CompanySessionManager
import com.example.nexogo.platform.company.ui.LocalActiveCompany
import com.example.nexogo.platform.documents.viewmodel.DocumentViewModel
import com.example.nexogo.viewmodel.PersistentAuthViewModel

/**
 * Minimal Documents list + Storage upload (company-scoped). S2 Cutover.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatformDocumentsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { CompanySessionManager.getInstance(context) }
    val vm = remember { DocumentViewModel(sessionManager = sessionManager) }
    val company = LocalActiveCompany.current
    val authVm = remember { PersistentAuthViewModel.getInstance(context) }
    val currentUser by authVm.currentUser.collectAsStateWithLifecycle()
    val ui by vm.uiState.collectAsStateWithLifecycle()

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        val name = queryDisplayName(context, uri) ?: "document.bin"
        val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
        vm.uploadFromUri(
            context = context,
            uri = uri,
            fileName = name,
            mimeType = mime,
            createdBy = currentUser?.id.orEmpty()
        )
    }

    LaunchedEffect(company?.id) {
        val cid = company?.id
        if (!cid.isNullOrBlank()) vm.bindCompany(cid)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Documentos")
                        company?.name?.let {
                            Text(it, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    picker.launch(
                        arrayOf(
                            "application/pdf",
                            "image/*",
                            "application/msword",
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                            "application/vnd.ms-excel",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        )
                    )
                }
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = "Subir documento")
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Button(
                onClick = {
                    picker.launch(
                        arrayOf(
                            "application/pdf",
                            "image/*",
                            "application/msword",
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                        )
                    )
                },
                enabled = !ui.isUploading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (ui.isUploading) "Subiendo…" else "Subir documento")
            }
            Spacer(Modifier.height(12.dp))

            ui.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }
            ui.message?.let {
                Text(it, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
            }

            if ((ui.isLoading || ui.isUploading) && ui.documents.isEmpty()) {
                Column(
                    Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) { CircularProgressIndicator() }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ui.documents, key = { it.id }) { doc ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                Text(doc.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${doc.fileFamily} · ${doc.mimeType} · ${doc.size} bytes",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    doc.storagePath,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun queryDisplayName(context: android.content.Context, uri: Uri): String? {
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (idx >= 0 && cursor.moveToFirst()) {
            return cursor.getString(idx)
        }
    }
    return uri.lastPathSegment
}
