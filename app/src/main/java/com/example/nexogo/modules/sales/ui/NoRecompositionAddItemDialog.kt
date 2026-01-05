package com.example.nexogo.modules.sales.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import android.util.Log

@Composable
fun NoRecompositionAddItemDialog(
    onSave: (String, String, Int, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var itemName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var unitPrice by remember { mutableStateOf("") }
    
    // Usar LaunchedEffect para manejar el guardado de forma segura
    var shouldSave by remember { mutableStateOf(false) }
    var saveData by remember { mutableStateOf<Triple<String, String, Pair<Int, Double>>?>(null) }
    
    LaunchedEffect(shouldSave) {
        if (shouldSave && saveData != null) {
            val (name, desc, qtyPrice) = saveData!!
            val (qty, price) = qtyPrice
            Log.d("NEXOGO_SALES", "LaunchedEffect - Llamando onSave con: $name, $desc, $qty, $price")
            onSave(name, desc, qty, price)
            shouldSave = false
            saveData = null
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Item") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Nombre *") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción *") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Cantidad *") },
                        modifier = Modifier.weight(1f)
                    )
                    
                    OutlinedTextField(
                        value = unitPrice,
                        onValueChange = { unitPrice = it },
                        label = { Text("Precio *") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    Log.d("NEXOGO_SALES", "=== BOTÓN AGREGAR PRESIONADO ===")
                    Log.d("NEXOGO_SALES", "itemName: '$itemName'")
                    Log.d("NEXOGO_SALES", "description: '$description'")
                    Log.d("NEXOGO_SALES", "quantity: '$quantity'")
                    Log.d("NEXOGO_SALES", "unitPrice: '$unitPrice'")
                    
                    try {
                        val qty = quantity.toIntOrNull() ?: 1
                        val price = unitPrice.toDoubleOrNull() ?: 0.0
                        
                        Log.d("NEXOGO_SALES", "qty parsed: $qty")
                        Log.d("NEXOGO_SALES", "price parsed: $price")
                        
                        if (itemName.isNotBlank() && description.isNotBlank() && qty > 0 && price > 0) {
                            Log.d("NEXOGO_SALES", "Validación exitosa, preparando datos para LaunchedEffect")
                            saveData = Triple(itemName, description, Pair(qty, price))
                            shouldSave = true
                            Log.d("NEXOGO_SALES", "shouldSave = true, saveData configurado")
                        } else {
                            Log.d("NEXOGO_SALES", "Validación falló")
                        }
                    } catch (e: Exception) {
                        Log.e("NEXOGO_SALES", "Error en botón: ${e.message}", e)
                    }
                }
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    Log.d("NEXOGO_SALES", "Botón cancelar presionado")
                    onDismiss()
                }
            ) {
                Text("Cancelar")
            }
        }
    )
}

