package com.ganeshkulfi.app.data.repository

import com.ganeshkulfi.app.data.model.Order
import com.ganeshkulfi.app.data.model.OrderStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor() {
    
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val ordersFlow: Flow<List<Order>> = _orders.asStateFlow()

    suspend fun createOrder(order: Order): Result<String> {
        return try {
            val orderId = "order_${System.currentTimeMillis()}"
            val newOrder = order.copy(id = orderId)
            _orders.value = _orders.value + newOrder
            Result.success(orderId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserOrdersFlow(retailerId: String): Flow<List<Order>> {
        return ordersFlow.map { orders ->
            orders.filter { it.retailerId == retailerId }
                .sortedByDescending { it.createdAt }
        }
    }

    fun getAllOrdersFlow(): Flow<List<Order>> {
        return ordersFlow.map { orders ->
            orders.sortedByDescending { it.createdAt }
        }
    }

    suspend fun getOrderById(orderId: String): Result<Order> {
        return try {
            val order = _orders.value.find { it.id == orderId }
                ?: throw Exception("Order not found")
            Result.success(order)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateOrderStatus(orderId: String, status: OrderStatus): Result<Unit> {
        return try {
            _orders.value = _orders.value.map { order ->
                if (order.id == orderId) {
                    order.copy(
                        status = status,
                        updatedAt = System.currentTimeMillis()
                    )
                } else {
                    order
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelOrder(orderId: String): Result<Unit> {
        return updateOrderStatus(orderId, OrderStatus.CANCELLED)
    }

    suspend fun updatePaymentStatus(orderId: String, paymentStatus: com.ganeshkulfi.app.data.model.PaymentStatus): Result<Unit> {
        return try {
            _orders.value = _orders.value.map { order ->
                if (order.id == orderId) {
                    order.copy(
                        paymentStatus = paymentStatus,
                        updatedAt = System.currentTimeMillis()
                    )
                } else {
                    order
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getRetailerOrdersFlow(retailerId: String): Flow<List<Order>> {
        return ordersFlow.map { orders ->
            android.util.Log.d("OrderRepository", "Total orders: ${orders.size}, filtering for retailerId: $retailerId")
            orders.forEach { order ->
                android.util.Log.d("OrderRepository", "Order ${order.id}: retailerId=${order.retailerId}")
            }
            val filtered = orders.filter { it.retailerId == retailerId }
                .sortedByDescending { it.createdAt }
            android.util.Log.d("OrderRepository", "Filtered orders count: ${filtered.size}")
            filtered
        }
    }

    fun getPendingOrdersFlow(): Flow<List<Order>> {
        return ordersFlow.map { orders ->
            orders.filter { it.status == OrderStatus.PENDING }
                .sortedByDescending { it.createdAt }
        }
    }

    fun getOrdersByStatus(status: OrderStatus): Flow<List<Order>> {
        return ordersFlow.map { orders ->
            orders.filter { it.status == status }
                .sortedByDescending { it.createdAt }
        }
    }

    suspend fun getTotalRevenue(): Double {
        return _orders.value.filter { 
            it.status == OrderStatus.CONFIRMED || it.status == OrderStatus.COMPLETED 
        }.sumOf { it.totalAmount }
    }

    suspend fun getTotalOrderCount(): Int {
        return _orders.value.size
    }

    suspend fun getPendingOrderCount(): Int {
        return _orders.value.count { it.status == OrderStatus.PENDING }
    }
}
