package com.ganeshkulfi.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganeshkulfi.app.data.model.InventoryItem
import com.ganeshkulfi.app.data.model.Order
import com.ganeshkulfi.app.data.model.OrderStatus
import com.ganeshkulfi.app.data.model.Retailer
import com.ganeshkulfi.app.data.repository.InventoryRepository
import com.ganeshkulfi.app.data.repository.OrderRepository
import com.ganeshkulfi.app.data.repository.RetailerRepository
import com.ganeshkulfi.app.data.repository.StockTransactionRepository
import com.ganeshkulfi.app.data.repository.PricingRepository
import com.ganeshkulfi.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val orderRepository: OrderRepository,
    private val retailerRepository: RetailerRepository,
    private val stockTransactionRepository: StockTransactionRepository,
    private val pricingRepository: PricingRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // Dashboard Stats
    private val _dashboardStats = MutableStateFlow(DashboardStats())
    val dashboardStats: StateFlow<DashboardStats> = _dashboardStats.asStateFlow()

    // Inventory
    val inventory: StateFlow<List<InventoryItem>> = inventoryRepository.inventoryFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Orders
    val orders: StateFlow<List<Order>> = orderRepository.ordersFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Retailers
    val retailers: StateFlow<List<Retailer>> = retailerRepository.retailersFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Low Stock Items
    val lowStockItems: StateFlow<List<InventoryItem>> = inventoryRepository.inventoryFlow
        .map { items ->
            inventoryRepository.getLowStockItems(20) // threshold = 20
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadDashboardStats()
    }

    private fun loadDashboardStats() {
        viewModelScope.launch {
            _isLoading.value = true
            
            combine(
                inventoryRepository.inventoryFlow,
                retailerRepository.retailersFlow,
                stockTransactionRepository.transactionsFlow
            ) { inventory, retailers, transactions ->
                
                val totalStock = inventory.sumOf { it.totalStock }
                val totalValue = inventory.sumOf { it.totalStock * it.costPrice }
                val totalRevenue = inventory.sumOf { it.soldQuantity * it.sellingPrice }
                val todaySales = calculateTodaySales(transactions)
                val lowStockCount = inventory.count { it.availableStock < 20 }
                val activeRetailers = retailers.count { it.isActive }
                val totalOutstanding = retailers.sumOf { it.totalOutstanding }
                val pendingPayments = transactions.count { 
                    it.paymentStatus == com.ganeshkulfi.app.data.model.PaymentStatus.UNPAID 
                }
                
                DashboardStats(
                    todaySales = todaySales,
                    totalRevenue = totalRevenue,
                    totalStock = totalStock,
                    totalValue = totalValue,
                    lowStockItems = lowStockCount,
                    activeRetailers = activeRetailers,
                    totalOutstanding = totalOutstanding,
                    pendingPayments = pendingPayments
                )
            }.collect { stats ->
                _dashboardStats.value = stats
                _isLoading.value = false
            }
        }
    }

    private fun calculateTodaySales(transactions: List<com.ganeshkulfi.app.data.model.StockTransaction>): Double {
        val todayStart = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
        return transactions
            .filter { it.createdAt >= todayStart }
            .filter { it.transactionType == com.ganeshkulfi.app.data.model.TransactionType.GIVEN }
            .sumOf { it.totalAmount }
    }

    // Inventory Operations
    fun updateStock(flavorId: String, quantity: Int) {
        viewModelScope.launch {
            inventoryRepository.updateStock(flavorId, quantity)
        }
    }

    fun recordSale(flavorId: String, quantity: Int) {
        viewModelScope.launch {
            inventoryRepository.recordSale(flavorId, quantity)
        }
    }

    fun updateItemPrice(flavorId: String, costPrice: Double, sellingPrice: Double) {
        viewModelScope.launch {
            inventoryRepository.updatePrice(flavorId, costPrice, sellingPrice)
        }
    }

    // Retailer Operations
    fun addRetailer(retailer: Retailer) {
        viewModelScope.launch {
            retailerRepository.addRetailer(retailer)
        }
    }

    fun addRetailerWithCredentials(retailer: Retailer, email: String, password: String) {
        viewModelScope.launch {
            try {
                // First create the retailer to obtain a retailer id
                val createdResult = retailerRepository.createRetailerAccount(retailer, email, password)
                if (createdResult.isSuccess) {
                    val createdRetailer = createdResult.getOrThrow()

                    // Register retailer credentials WITHOUT changing current user session
                    // This ensures admin stays logged in after creating a retailer
                    val registerResult = authRepository.registerRetailerCredentials(
                        email = email,
                        password = password,
                        name = createdRetailer.name,
                        phone = createdRetailer.phone,
                        retailerId = createdRetailer.id,
                        shopName = createdRetailer.shopName,
                        pricingTier = createdRetailer.pricingTier
                    )

                    if (registerResult.isFailure) {
                        // If registration failed, log or revert retailer creation if necessary
                        registerResult.exceptionOrNull()?.printStackTrace()
                    }
                } else {
                    // Failed to create retailer
                    createdResult.exceptionOrNull()?.printStackTrace()
                }
            } catch (e: Exception) {
                // Handle error (you might want to add error state here)
                e.printStackTrace()
            }
        }
    }

    fun updateRetailer(retailer: Retailer) {
        viewModelScope.launch {
            retailerRepository.updateRetailer(retailer)
        }
    }

    fun deleteRetailer(retailerId: String) {
        viewModelScope.launch {
            retailerRepository.deleteRetailer(retailerId)
        }
    }

    // Stock Transaction Operations
    fun giveStockToRetailer(
        retailerId: String,
        flavorId: String,
        quantity: Int,
        basePrice: Double? = null  // Optional: if null, will use pricing from repository
    ) {
        viewModelScope.launch {
            // Get retailer
            val retailer = retailers.value.find { it.id == retailerId }
                ?: return@launch
            
            // Get flavor details for base price
            val flavor = inventory.value.find { it.flavorId == flavorId }
                ?: return@launch
            
            // Calculate retailer-specific price
            val actualBasePrice = basePrice ?: flavor.sellingPrice
            val (totalAmount, priceInfo) = pricingRepository.calculateTransactionAmount(
                retailer = retailer,
                flavorId = flavorId,
                basePrice = actualBasePrice,
                quantity = quantity
            )
            
            stockTransactionRepository.createTransaction(
                retailerId = retailerId,
                flavorId = flavorId,
                quantity = quantity,
                pricePerUnit = priceInfo.retailerPrice,  // Use retailer-specific price
                totalAmount = totalAmount,
                type = com.ganeshkulfi.app.data.model.TransactionType.GIVEN
            )
            
            // Update inventory
            inventoryRepository.giveStockToRetailer(flavorId, quantity)
        }
    }

    /**
     * Get price breakdown for a retailer and flavor
     */
    fun getPriceBreakdown(
        retailer: Retailer,
        flavorId: String,
        flavorName: String,
        basePrice: Double,
        quantity: Int
    ) = pricingRepository.getPriceBreakdown(retailer, flavorId, flavorName, basePrice, quantity)

    /**
     * Update retailer pricing tier
     */
    fun updateRetailerPricingTier(retailerId: String, newTier: com.ganeshkulfi.app.data.model.PricingTier) {
        viewModelScope.launch {
            val retailer = retailers.value.find { it.id == retailerId }
            retailer?.let {
                retailerRepository.updateRetailer(it.copy(pricingTier = newTier))
            }
        }
    }

    /**
     * Set custom price for a retailer-flavor combination
     */
    fun setCustomPrice(
        retailerId: String,
        flavorId: String,
        customPrice: Double,
        discount: Double = 0.0,
        minimumQuantity: Int = 0
    ) {
        pricingRepository.setCustomPrice(retailerId, flavorId, customPrice, discount, minimumQuantity)
    }

    /**
     * Remove custom price
     */
    fun removeCustomPrice(retailerId: String, flavorId: String) {
        pricingRepository.removeCustomPrice(retailerId, flavorId)
    }

    fun recordPayment(retailerId: String, amount: Double) {
        viewModelScope.launch {
            // Get retailer's pending transactions
            val pendingTransactions = stockTransactionRepository.getPendingPayments()
                .filter { it.retailerId == retailerId }
                .sortedBy { it.createdAt }
            
            var remainingAmount = amount
            
            // Apply payment to oldest transactions first
            for (transaction in pendingTransactions) {
                if (remainingAmount <= 0) break
                
                val transactionAmount = transaction.totalAmount
                if (remainingAmount >= transactionAmount) {
                    // Full payment for this transaction
                    stockTransactionRepository.updatePaymentStatus(
                        transaction.id,
                        com.ganeshkulfi.app.data.model.PaymentStatus.PAID
                    )
                    remainingAmount -= transactionAmount
                } else {
                    // Partial payment
                    stockTransactionRepository.updatePaymentStatus(
                        transaction.id,
                        com.ganeshkulfi.app.data.model.PaymentStatus.PARTIAL
                    )
                    remainingAmount = 0.0
                }
            }
        }
    }

    // Order Operations
    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        viewModelScope.launch {
            orderRepository.updateOrderStatus(orderId, newStatus)
        }
    }

    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            orderRepository.cancelOrder(orderId)
        }
    }

    fun markPaymentReceived(orderId: String) {
        viewModelScope.launch {
            orderRepository.updatePaymentStatus(orderId, com.ganeshkulfi.app.data.model.PaymentStatus.PAID)
        }
    }

    fun markPaymentPending(orderId: String) {
        viewModelScope.launch {
            orderRepository.updatePaymentStatus(orderId, com.ganeshkulfi.app.data.model.PaymentStatus.UNPAID)
        }
    }
}

data class DashboardStats(
    val todaySales: Double = 0.0,
    val totalRevenue: Double = 0.0,
    val totalStock: Int = 0,
    val totalValue: Double = 0.0,
    val lowStockItems: Int = 0,
    val activeRetailers: Int = 0,
    val totalOutstanding: Double = 0.0,
    val pendingPayments: Int = 0
)
