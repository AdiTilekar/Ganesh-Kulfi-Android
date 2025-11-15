package com.ganeshkulfi.app.data.model

data class StockTransaction(
    val id: String = "",
    val retailerId: String = "",
    val flavorId: String = "",
    val flavorName: String = "",
    val quantity: Int = 0,
    val unitPrice: Double = 0.0,
    val totalAmount: Double = 0.0,
    val transactionType: TransactionType = TransactionType.GIVEN,
    val paymentStatus: PaymentStatus = PaymentStatus.UNPAID,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class TransactionType {
    GIVEN,      // Stock given to retailer
    RETURNED,   // Stock returned by retailer
    SOLD        // Direct sale
}

enum class PaymentStatus {
    UNPAID,     // Not paid (replaces PENDING for consistency)
    PARTIAL,    // Partially paid
    PAID,       // Fully paid
    OVERDUE;    // Payment overdue
    
    fun getDisplayName(): String = when (this) {
        UNPAID -> "Unpaid"
        PARTIAL -> "Partially Paid"
        PAID -> "Paid"
        OVERDUE -> "Overdue"
    }
}
