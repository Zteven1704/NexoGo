package com.example.nexogo.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onNavigateBack: () -> Unit
) {
    var expandedItems by remember { mutableStateOf<Set<Int>>(emptySet()) }
    
    val faqItems = listOf(
        FAQItem(
            question = "¿Cómo agendar una cita?",
            answer = "Para agendar una cita, ve al módulo 'Agendamiento' y haz clic en el botón 'Agregar Cita'. Selecciona la fecha, hora y mascota, luego completa los detalles de la cita."
        ),
        FAQItem(
            question = "¿Cómo puedo ver el historial clínico de mi mascota?",
            answer = "En el módulo 'Historial Clínico' puedes ver todos los registros médicos de tus mascotas. Los pacientes solo pueden ver en modo lectura, mientras que los profesionales pueden editar y agregar nuevos registros."
        ),
        FAQItem(
            question = "¿Cómo funciona el chat?",
            answer = "El chat te permite comunicarte directamente con veterinarios y asistentes. Puedes enviar mensajes de texto y adjuntar archivos multimedia como fotos, videos y documentos."
        ),
        FAQItem(
            question = "¿Cómo gestionar el inventario?",
            answer = "Los profesionales pueden gestionar el inventario desde el módulo correspondiente. Pueden agregar productos, ver alertas de stock bajo y actualizar cantidades."
        ),
        FAQItem(
            question = "¿Cómo cambiar mi perfil?",
            answer = "Ve a 'Configuración' y selecciona 'Editar perfil'. Ahí puedes actualizar tu información personal, foto de perfil y datos de contacto."
        ),
        FAQItem(
            question = "¿Cómo cambiar la contraseña?",
            answer = "En 'Configuración' > 'Cambiar contraseña', se enviará un enlace de restablecimiento a tu correo electrónico registrado."
        ),
        FAQItem(
            question = "¿Cómo activar las notificaciones?",
            answer = "En 'Configuración' > 'Notificaciones' puedes activar o desactivar las notificaciones push, recordatorios de citas y mensajes."
        ),
        FAQItem(
            question = "¿Qué roles de usuario existen?",
            answer = "Existen 4 roles: Paciente (dueños de mascotas), Veterinario, Asistente Veterinario y Administrador. Cada rol tiene acceso a diferentes funcionalidades."
        ),
        FAQItem(
            question = "¿Cómo contactar soporte?",
            answer = "Puedes enviar comentarios desde 'Configuración' > 'Enviar comentarios' o contactar directamente a través de los canales de soporte disponibles."
        ),
        FAQItem(
            question = "¿Cómo respaldar mis datos?",
            answer = "En 'Configuración' > 'Datos' puedes activar el respaldo automático que sincroniza tus datos en la nube de forma segura."
        )
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ayuda y FAQ") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Help,
                            contentDescription = "Ayuda",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Centro de Ayuda",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Encuentra respuestas a las preguntas más frecuentes sobre NexoGo",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
            
            // FAQ List
            items(faqItems.size) { index ->
                    val item = faqItems[index]
                    val isExpanded = expandedItems.contains(index)
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.HelpOutline,
                                    contentDescription = "Pregunta",
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Text(
                                    text = item.question,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                IconButton(
                                    onClick = {
                                        expandedItems = if (isExpanded) {
                                            expandedItems - index
                                        } else {
                                            expandedItems + index
                                        }
                                    }
                                ) {
                                    Icon(
                                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = if (isExpanded) "Contraer" else "Expandir"
                                    )
                                }
                            }
                            
                            if (isExpanded) {
                                Divider()
                                
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = item.answer,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Contact support section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "¿No encontraste lo que buscabas?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Contacta a nuestro equipo de soporte",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { /* TODO: Open email */ }
                        ) {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = "Email",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Email")
                        }
                        
                        OutlinedButton(
                            onClick = { /* TODO: Open phone */ }
                        ) {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = "Teléfono",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Teléfono")
                        }
                    }
                }
            }
        }
}

data class FAQItem(
    val question: String,
    val answer: String
)

