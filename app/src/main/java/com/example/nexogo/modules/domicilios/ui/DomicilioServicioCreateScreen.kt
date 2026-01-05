package com.example.nexogo.modules.domicilios.ui

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nexogo.model.DatosServicio
import com.example.nexogo.model.TipoServicioVeterinario
import com.example.nexogo.modules.domicilios.DomicilioViewModel
import com.example.nexogo.repository.DomicilioRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.util.Date

/**
 * Pantalla para crear solicitud de domicilio de servicios veterinarios
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DomicilioServicioCreateScreen(
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
    var tipoServicio by remember { mutableStateOf(TipoServicioVeterinario.CONSULTA_GENERAL) }
    var tipoServicioOtro by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fechaSolicitada by remember { mutableStateOf<Date>(Date()) }
    var fotoUri by remember { mutableStateOf<Uri?>(null) }
    
    // Cargar dirección del perfil si existe
    LaunchedEffect(Unit) {
        // TODO: Cargar dirección del perfil del usuario desde Firestore
    }
    
    // Mostrar mensajes
    LaunchedEffect(successMessage) {
        successMessage?.let {
            onSuccess()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Domicilio de Servicios", fontWeight = FontWeight.Bold) },
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
            // Información del usuario
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Información de Contacto",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    OutlinedTextField(
                        value = nombreUsuario,
                        onValueChange = { nombreUsuario = it },
                        label = { Text("Nombre completo") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        leadingIcon = { Icon(Icons.Default.Person, null) }
                    )
                    
                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { telefono = it },
                        label = { Text("Teléfono") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        leadingIcon = { Icon(Icons.Default.Phone, null) }
                    )
                    
                    OutlinedTextField(
                        value = direccion,
                        onValueChange = { direccion = it },
                        label = { Text("Dirección de atención") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4,
                        enabled = !isLoading,
                        leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                        placeholder = { Text("Dirección completa donde se realizará el servicio") }
                    )
                }
            }
            
            // Tipo de servicio
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Tipo de Servicio",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    TipoServicioVeterinario.values().forEach { tipo ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = tipoServicio == tipo,
                                onClick = { tipoServicio = tipo }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = DomicilioUtils.getTipoServicioDisplayName(tipo),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    
                    if (tipoServicio == TipoServicioVeterinario.OTRO) {
                        OutlinedTextField(
                            value = tipoServicioOtro,
                            onValueChange = { tipoServicioOtro = it },
                            label = { Text("Especificar tipo de servicio") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        )
                    }
                }
            }
            
            // Descripción del caso
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción del caso") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                maxLines = 8,
                enabled = !isLoading,
                placeholder = { Text("Describe el motivo de la consulta, síntomas, etc.") }
            )
            
            // Fecha y hora deseada
            DatePickerSection(
                fechaSolicitada = fechaSolicitada,
                onFechaChange = { fechaSolicitada = it },
                enabled = !isLoading
            )
            
            // Foto adjunta (opcional)
            FotoSection(
                fotoUri = fotoUri,
                onFotoChange = { fotoUri = it },
                enabled = !isLoading,
                label = "Foto o receta médica (opcional)"
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
                    if (validarFormularioServicio(nombreUsuario, telefono, direccion, descripcion, tipoServicio, tipoServicioOtro)) {
                        val datosServicio = DatosServicio(
                            tipoServicio = tipoServicio,
                            descripcion = descripcion,
                            fechaSolicitada = Timestamp(fechaSolicitada),
                            tipoServicioOtro = if (tipoServicio == TipoServicioVeterinario.OTRO) tipoServicioOtro else null
                        )
                        viewModel.createDomicilioServicio(
                            nombreUsuario = nombreUsuario,
                            telefono = telefono,
                            direccion = direccion,
                            datosServicio = datosServicio,
                            fotoUri = fotoUri
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && validarFormularioServicio(nombreUsuario, telefono, direccion, descripcion, tipoServicio, tipoServicioOtro)
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


fun validarFormularioServicio(
    nombreUsuario: String,
    telefono: String,
    direccion: String,
    descripcion: String,
    tipoServicio: TipoServicioVeterinario,
    tipoServicioOtro: String
): Boolean {
    return nombreUsuario.isNotBlank() &&
            telefono.isNotBlank() &&
            direccion.isNotBlank() &&
            descripcion.isNotBlank() &&
            (tipoServicio != TipoServicioVeterinario.OTRO || tipoServicioOtro.isNotBlank())
}

