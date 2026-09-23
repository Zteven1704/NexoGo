package com.example.nexogo.platform.records.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nexogo.platform.clients.viewmodel.ClientViewModel
import com.example.nexogo.platform.company.session.CompanySessionManager
import com.example.nexogo.platform.company.ui.LocalActiveCompany
import com.example.nexogo.platform.records.viewmodel.RecordViewModel
import com.example.nexogo.viewmodel.PersistentAuthViewModel

/**
 * Minimal Records (expedientes) list + create linked to a client. S2 Cutover.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlatformRecordsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { CompanySessionManager.getInstance(context) }
    val recordVm = remember { RecordViewModel(sessionManager = sessionManager) }
    val clientVm = remember { ClientViewModel(sessionManager = sessionManager) }
    val company = LocalActiveCompany.current
    val authVm = remember { PersistentAuthViewModel.getInstance(context) }
    val currentUser by authVm.currentUser.collectAsStateWithLifecycle()
    val ui by recordVm.uiState.collectAsStateWithLifecycle()
    val clientsUi by clientVm.uiState.collectAsStateWithLifecycle()

    var showCreate by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var summary by remember { mutableStateOf("") }
    var selectedClientId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(company?.id) {
        val cid = company?.id
        if (!cid.isNullOrBlank()) {
            recordVm.bindCompany(cid)
            clientVm.bindCompany(cid)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Expedientes")
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
            FloatingActionButton(onClick = { showCreate = !showCreate }) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo expediente")
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (showCreate) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Nuevo expediente", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Cliente: " + (
                                clientsUi.clients.find { it.id == selectedClientId }?.displayName
                                    ?: "(elige abajo)"
                                ),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        clientsUi.clients.take(8).forEach { client ->
                            Text(
                                text = client.displayName,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedClientId = client.id }
                                    .padding(vertical = 6.dp),
                                color = if (client.id == selectedClientId) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Título") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = summary,
                            onValueChange = { summary = it },
                            label = { Text("Resumen") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                val clientId = selectedClientId
                                if (clientId.isNullOrBlank() || title.isBlank()) return@Button
                                recordVm.createClinicalHistory(
                                    clientId = clientId,
                                    title = title.trim(),
                                    authoredBy = currentUser?.id.orEmpty(),
                                    summary = summary.trim()
                                )
                                title = ""
                                summary = ""
                                showCreate = false
                            },
                            enabled = !selectedClientId.isNullOrBlank() &&
                                title.isNotBlank() &&
                                !ui.isLoading
                        ) { Text("Crear expediente") }

                        if (clientsUi.clients.isEmpty()) {
                            Text(
                                "Crea un cliente primero en Clientes.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            ui.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }
            ui.message?.let {
                Text(it, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
            }

            if (ui.isLoading && ui.records.isEmpty()) {
                Column(
                    Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) { CircularProgressIndicator() }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ui.records, key = { it.id }) { record ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                Text(record.title, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${record.recordType} · ${record.status}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (record.summary.isNotBlank()) {
                                    Text(record.summary, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
