package com.example.nexogo.modules.sales.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import android.util.Log

@Composable
fun UltraSimpleAddItemDialog(
    onSave: (String, String, Int, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var itemName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var unitPrice by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Agregar Item")
        },
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
                    try {
                        Log.d("NEXOGO_SALES", "UltraSimple - Intentando agregar item...")
                        Log.d("NEXOGO_SALES", "UltraSimple - itemName: '$itemName'")
                        Log.d("NEXOGO_SALES", "UltraSimple - description: '$description'")
                        Log.d("NEXOGO_SALES", "UltraSimple - quantity: '$quantity'")
                        Log.d("NEXOGO_SALES", "UltraSimple - unitPrice: '$unitPrice'")
                        
                        val qty = quantity.toIntOrNull() ?: 1
                        val price = unitPrice.toDoubleOrNull() ?: 0.0
                        
                        Log.d("NEXOGO_SALES", "UltraSimple - qty parsed: $qty")
                        Log.d("NEXOGO_SALES", "UltraSimple - price parsed: $price")
                        
                        if (itemName.isNotBlank() && description.isNotBlank() && qty > 0 && price > 0) {
                            Log.d("NEXOGO_SALES", "UltraSimple - Validación exitosa, llamando onSave...")
                            onSave(itemName, description, qty, price)
                            Log.d("NEXOGO_SALES", "UltraSimple - onSave llamado exitosamente")
                        } else {
                            Log.d("NEXOGO_SALES", "UltraSimple - Validación falló")
                        }
                    } catch (e: Exception) {
                        Log.e("NEXOGO_SALES", "UltraSimple - Error: ${e.message}", e)
                    }
                }
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

