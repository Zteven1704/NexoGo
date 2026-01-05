package com.example.nexogo.modules.sales.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.util.Log
import com.example.nexogo.modules.sales.model.SaleItemType

@Composable
fun CompletelyPrimitiveDialog(
    onSave: (String, String, Int, Double, SaleItemType) -> Unit,
    onDismiss: () -> Unit
) {
    var itemName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var unitPrice by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(SaleItemType.PRODUCT) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Item") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tipo de item
                Text(
                    text = "Tipo de Item",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        onClick = { selectedType = SaleItemType.PRODUCT },
                        label = { Text("Producto") },
                        selected = selectedType == SaleItemType.PRODUCT,
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        onClick = { selectedType = SaleItemType.SERVICE },
                        label = { Text("Servicio") },
                        selected = selectedType == SaleItemType.SERVICE,
                        modifier = Modifier.weight(1f)
                    )
                }
                
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
                    Log.d("NEXOGO_SALES", "selectedType: $selectedType")
                    
                    try {
                        val qty = quantity.toIntOrNull() ?: 1
                        val price = unitPrice.toDoubleOrNull() ?: 0.0
                        
                        Log.d("NEXOGO_SALES", "qty parsed: $qty")
                        Log.d("NEXOGO_SALES", "price parsed: $price")
                        
                        if (itemName.isNotBlank() && description.isNotBlank() && qty > 0 && price > 0) {
                            Log.d("NEXOGO_SALES", "Validación exitosa, llamando onSave...")
                            onSave(itemName, description, qty, price, selectedType)
                            Log.d("NEXOGO_SALES", "onSave llamado exitosamente")
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
