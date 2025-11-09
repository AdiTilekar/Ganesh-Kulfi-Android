package com.ganeshkulfi.app.data.model

data class Order(
    val id: String = "",
    val userId: String = "",
    val items: List<OrderItem> = emptyList(),
    val subtotal: Double = 0.0,
    val discountAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val status: OrderStatus = OrderStatus.PENDING,
    val customerName: String = "",
    val customerPhone: String = "",
    val customerAddress: String = "",
    val shopName: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class OrderItem(
    val flavorId: String = "",
    val flavorName: String = "",
    val quantity: Int = 0,
    val basePrice: Double = 0.0,
    val discountedPrice: Double = 0.0,
    val subtotal: Double = 0.0
)

enum class OrderStatus {
    PENDING,
    CONFIRMED,
    PREPARING,
    READY,
    DISPATCHED,
    DELIVERED,
    CANCELLED
}
