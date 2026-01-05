package com.example.nexogo.modules.domicilios.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.nexogo.modules.domicilios.DomicilioViewModel
import com.example.nexogo.repository.DomicilioRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DomicilioCreateScreen(
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit = {}
) {
    val viewModel = remember { DomicilioViewModel(DomicilioRepository()) }
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val successMessage by viewModel.successMessage.collectAsStateWithLifecycle()
    
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    
    var nombreUsuario by remember { mutableStateOf(currentUser?.displayName ?: "") }
    var telefono by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var tipoServicio by remember { mutableStateOf("") }
    var fechaSolicitada by remember { mutableStateOf<Date>(Date()) }
    var fotoUri by remember { mutableStateOf<Uri?>(null) }
    
    // Mostrar mensajes
    LaunchedEffect(successMessage) {
        successMessage?.let {
            onSuccess()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Solicitud de Domicilio", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = nombreUsuario,
                onValueChange = { nombreUsuario = it },
                label = { Text("Nombre completo") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            
            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )
            
            OutlinedTextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = { Text("Dirección completa") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                enabled = !isLoading
            )
            
            OutlinedTextField(
                value = tipoServicio,
                onValueChange = { tipoServicio = it },
                label = { Text("Tipo de servicio") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ej: Consulta, Vacunación, Cirugía menor") },
                enabled = !isLoading
            )
            
            // Selector de fecha y hora
            DatePickerSection(
                fechaSolicitada = fechaSolicitada,
                onFechaChange = { fechaSolicitada = it },
                enabled = !isLoading
            )
            
            // Foto adjunta (opcional)
            FotoSection(
                fotoUri = fotoUri,
                onFotoChange = { fotoUri = it },
                enabled = !isLoading
            )
            
            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Button(
                onClick = {
                    if (validarFormulario(nombreUsuario, telefono, direccion, tipoServicio)) {
                        viewModel.createDomicilio(
                            nombreUsuario = nombreUsuario,
                            telefono = telefono,
                            direccion = direccion,
                            tipoServicio = tipoServicio,
                            fechaSolicitada = Timestamp(fechaSolicitada),
                            fotoUri = fotoUri
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && validarFormulario(nombreUsuario, telefono, direccion, tipoServicio)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Crear Solicitud")
                }
            }
        }
    }
}

@Composable
fun DatePickerSection(
    fechaSolicitada: Date,
    onFechaChange: (Date) -> Unit,
    enabled: Boolean
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Fecha y hora solicitada",
            style = MaterialTheme.typography.labelMedium
        )
        
        OutlinedButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled
        ) {
            Text(DomicilioUtils.formatDate(fechaSolicitada))
        }
    }
    
    if (showDatePicker) {
        // Aquí podrías usar un DatePickerDialog personalizado
        // Por ahora, solo mostramos la fecha formateada
    }
}

@Composable
fun FotoSection(
    fotoUri: Uri?,
    onFotoChange: (Uri?) -> Unit,
    enabled: Boolean,
    label: String = "Foto adjunta (opcional)"
) {
    // Launcher para seleccionar imágenes
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            onFotoChange(it)
        }
    }
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium
        )
        
        if (fotoUri != null) {
            // Mostrar imagen previa
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                AsyncImage(
                    model = fotoUri,
                    contentDescription = "Imagen seleccionada",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f),
                    enabled = enabled
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cambiar")
                }
                
                OutlinedButton(
                    onClick = { onFotoChange(null) },
                    modifier = Modifier.weight(1f),
                    enabled = enabled,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Eliminar")
                }
            }
        } else {
            OutlinedButton(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Seleccionar foto")
            }
        }
    }
}

fun validarFormulario(
    nombreUsuario: String,
    telefono: String,
    direccion: String,
    tipoServicio: String
): Boolean {
    return nombreUsuario.isNotBlank() &&
            telefono.isNotBlank() &&
            direccion.isNotBlank() &&
            tipoServicio.isNotBlank()
}


