package com.ganeshkulfi.app.data.model

data class Order(
    val id: String = "",
    val orderNumber: String = "",
    val retailerId: String = "",
    val retailerEmail: String = "",
    val retailerName: String = "",
    val shopName: String? = null,
    
    // Order details
    val totalItems: Int = 0,
    val totalQuantity: Int = 0,
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val tax: Double = 0.0,
    val totalAmount: Double = 0.0,
    
    // Status
    val status: OrderStatus = OrderStatus.PENDING,
    val paymentStatus: PaymentStatus = PaymentStatus.UNPAID,
    
    // Notes
    val retailerNotes: String? = null,
    val factoryNotes: String? = null,
    val rejectionReason: String? = null,
    
    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val confirmedAt: Long? = null,
    val rejectedAt: Long? = null,
    val completedAt: Long? = null,
    
    // Who updated
    val confirmedBy: String? = null,
    val rejectedBy: String? = null,
    
    // Items
    val items: List<OrderItem> = emptyList()
)

data class OrderItem(
    val id: String = "",
    val orderId: String = "",
    val productId: String = "",
    val productName: String = "",
    val quantity: Int = 0,
    val unitPrice: Double = 0.0,
    val discountPercent: Double = 0.0,
    val discountAmount: Double = 0.0,
    val lineTotal: Double = 0.0
)

enum class OrderStatus {
    PENDING,      // Awaiting factory confirmation
    CONFIRMED,    // Factory confirmed
    REJECTED,     // Factory rejected
    COMPLETED,    // Delivered
    CANCELLED;    // Cancelled by retailer
    
    fun getDisplayName(): String = when (this) {
        PENDING -> "Pending Confirmation"
        CONFIRMED -> "Confirmed"
        REJECTED -> "Rejected"
        COMPLETED -> "Completed"
        CANCELLED -> "Cancelled"
    }
    
    fun getColor(): androidx.compose.ui.graphics.Color = when (this) {
        PENDING -> androidx.compose.ui.graphics.Color(0xFFFFA726) // Orange
        CONFIRMED -> androidx.compose.ui.graphics.Color(0xFF66BB6A) // Green
        REJECTED -> androidx.compose.ui.graphics.Color(0xFFEF5350) // Red
        COMPLETED -> androidx.compose.ui.graphics.Color(0xFF42A5F5) // Blue
        CANCELLED -> androidx.compose.ui.graphics.Color(0xFF9E9E9E) // Gray
    }
}

// PaymentStatus enum moved to StockTransaction.kt to avoid duplication
