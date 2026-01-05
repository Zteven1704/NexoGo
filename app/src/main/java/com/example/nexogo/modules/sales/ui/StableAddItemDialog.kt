package com.example.nexogo.modules.sales.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import android.util.Log

@Composable
fun StableAddItemDialog(
    onSave: (String, String, Int, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var itemName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var unitPrice by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = {
            if (!isProcessing) {
                onDismiss()
            }
        },
        title = {
            Text("Agregar Item")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { if (!isProcessing) itemName = it },
                    label = { Text("Nombre *") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isProcessing
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { if (!isProcessing) description = it },
                    label = { Text("Descripción *") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isProcessing
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { if (!isProcessing) quantity = it },
                        label = { Text("Cantidad *") },
                        modifier = Modifier.weight(1f),
                        enabled = !isProcessing
                    )
                    
                    OutlinedTextField(
                        value = unitPrice,
                        onValueChange = { if (!isProcessing) unitPrice = it },
                        label = { Text("Precio *") },
                        modifier = Modifier.weight(1f),
                        enabled = !isProcessing
                    )
                }
                
                if (isProcessing) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Procesando...")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isProcessing) return@Button
                    
                    Log.d("NEXOGO_SALES", "=== BOTÓN AGREGAR PRESIONADO ===")
                    Log.d("NEXOGO_SALES", "itemName: '$itemName'")
                    Log.d("NEXOGO_SALES", "description: '$description'")
                    Log.d("NEXOGO_SALES", "quantity: '$quantity'")
                    Log.d("NEXOGO_SALES", "unitPrice: '$unitPrice'")
                    
                    try {
                        isProcessing = true
                        Log.d("NEXOGO_SALES", "isProcessing = true")
                        
                        val qty = quantity.toIntOrNull() ?: 1
                        val price = unitPrice.toDoubleOrNull() ?: 0.0
                        
                        Log.d("NEXOGO_SALES", "qty parsed: $qty")
                        Log.d("NEXOGO_SALES", "price parsed: $price")
                        
                        if (itemName.isBlank()) {
                            Log.d("NEXOGO_SALES", "Error: itemName is blank")
                            isProcessing = false
                            return@Button
                        }
                        
                        if (description.isBlank()) {
                            Log.d("NEXOGO_SALES", "Error: description is blank")
                            isProcessing = false
                            return@Button
                        }
                        
                        if (qty <= 0) {
                            Log.d("NEXOGO_SALES", "Error: qty <= 0")
                            isProcessing = false
                            return@Button
                        }
                        
                        if (price <= 0) {
                            Log.d("NEXOGO_SALES", "Error: price <= 0")
                            isProcessing = false
                            return@Button
                        }
                        
                        Log.d("NEXOGO_SALES", "Validación exitosa, llamando onSave...")
                        onSave(itemName, description, qty, price)
                        Log.d("NEXOGO_SALES", "onSave llamado exitosamente")
                        
                    } catch (e: Exception) {
                        Log.e("NEXOGO_SALES", "Error en botón: ${e.message}", e)
                        isProcessing = false
                    }
                },
                enabled = !isProcessing
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isProcessing) "Procesando..." else "Agregar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    if (!isProcessing) {
                        Log.d("NEXOGO_SALES", "Botón cancelar presionado")
                        onDismiss()
                    }
                },
                enabled = !isProcessing
            ) {
                Text("Cancelar")
            }
        }
    )
}

