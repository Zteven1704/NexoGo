package com.example.nexogo.modules.sales.utils

import com.example.nexogo.modules.sales.model.PaymentMethod
import java.text.NumberFormat
import java.util.*

object SalesUtils {
    
    fun formatPriceCOP(price: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
        return formatter.format(price)
    }
    
    fun getPaymentMethodText(method: PaymentMethod): String {
        return when (method) {
            PaymentMethod.CASH -> "Efectivo"
            PaymentMethod.CARD -> "Tarjeta"
            PaymentMethod.TRANSFER -> "Transferencia"
            PaymentMethod.CHECK -> "Cheque"
        }
    }
    
    fun generateSaleNumber(): String {
        val timestamp = System.currentTimeMillis()
        return "V${timestamp.toString().takeLast(8)}"
    }
    
    fun calculateSubtotal(items: List<com.example.nexogo.modules.sales.model.SaleItem>): Double {
        return items.sumOf { it.totalPrice }
    }
    
    fun calculateTax(subtotal: Double, taxRate: Double = 0.19): Double {
        return subtotal * taxRate
    }
    
    fun calculateTotal(subtotal: Double, tax: Double): Double {
        return subtotal + tax
    }
}

