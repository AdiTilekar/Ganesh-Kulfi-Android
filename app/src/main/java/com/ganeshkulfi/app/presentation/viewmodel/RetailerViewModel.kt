package com.ganeshkulfi.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ganeshkulfi.app.data.model.InventoryItem
import com.ganeshkulfi.app.data.model.Order
import com.ganeshkulfi.app.data.model.OrderItem
import com.ganeshkulfi.app.data.model.ProductCatalogItem
import com.ganeshkulfi.app.data.model.toProductCatalog
import com.ganeshkulfi.app.data.repository.InventoryRepository
import com.ganeshkulfi.app.data.repository.OrderRepository
import com.ganeshkulfi.app.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RetailerViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val orderRepository: OrderRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // Shopping Cart State
    private val _cartItems = MutableStateFlow<Map<String, CartItem>>(emptyMap())
    val cartItems: StateFlow<Map<String, CartItem>> = _cartItems.asStateFlow()

    // Order History
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val myOrders: StateFlow<List<Order>> = authRepository.currentUserFlow
        .flatMapLatest { user ->
            if (user != null) {
                android.util.Log.d("RetailerViewModel", "Filtering orders for email: ${user.email}")
                orderRepository.getRetailerOrdersFlow(user.email ?: "")
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Product Catalog for ordering (NO factory stock information exposed)
    // Retailers see products they can order, but NOT factory inventory levels
    val availableProducts: StateFlow<List<InventoryItem>> = inventoryRepository.inventoryFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart Summary
    val cartSummary: StateFlow<CartSummary> = combine(
        _cartItems,
        authRepository.currentUserFlow
    ) { items, user ->
        if (items.isEmpty()) {
            CartSummary()
        } else {
            val discountPercentage = user?.pricingTier?.discountPercentage ?: 0.0
            val subtotal = items.values.sumOf { it.subtotal }
            val discountAmount = subtotal * (discountPercentage / 100.0)
            val total = subtotal - discountAmount
            
            CartSummary(
                itemCount = items.values.sumOf { it.quantity },
                subtotal = subtotal,
                discountAmount = discountAmount,
                totalAmount = total
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CartSummary())

    // Order Placement Status
    private val _orderPlacementStatus = MutableStateFlow<OrderPlacementStatus>(OrderPlacementStatus.Idle)
    val orderPlacementStatus: StateFlow<OrderPlacementStatus> = _orderPlacementStatus.asStateFlow()

    fun addToCart(product: InventoryItem, quantity: Int = 1) {
        viewModelScope.launch {
            val currentUser = authRepository.currentUserFlow.first() ?: return@launch
            val discountPercentage = currentUser.pricingTier?.discountPercentage ?: 0.0
            val discountedPrice = product.sellingPrice * (1 - discountPercentage / 100.0)

            _cartItems.value = _cartItems.value.toMutableMap().apply {
                val existing = this[product.flavorId]
                if (existing != null) {
                    this[product.flavorId] = existing.copy(
                        quantity = existing.quantity + quantity,
                        subtotal = (existing.quantity + quantity) * discountedPrice
                    )
                } else {
                    this[product.flavorId] = CartItem(
                        flavorKey = product.flavorId,
                        flavorName = product.flavorName,
                        basePrice = product.sellingPrice,
                        discountedPrice = discountedPrice,
                        quantity = quantity,
                        subtotal = quantity * discountedPrice
                    )
                }
            }
        }
    }

    fun updateCartItemQuantity(flavorKey: String, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(flavorKey)
            return
        }

        _cartItems.value = _cartItems.value.toMutableMap().apply {
            this[flavorKey]?.let { item ->
                this[flavorKey] = item.copy(
                    quantity = quantity,
                    subtotal = quantity * item.discountedPrice
                )
            }
        }
    }

    fun removeFromCart(flavorKey: String) {
        _cartItems.value = _cartItems.value.toMutableMap().apply {
            remove(flavorKey)
        }
    }

    fun clearCart() {
        _cartItems.value = emptyMap()
    }

    fun placeOrder(notes: String = "") {
        viewModelScope.launch {
            val currentUser = authRepository.currentUserFlow.first()
            if (currentUser == null) {
                _orderPlacementStatus.value = OrderPlacementStatus.Error("User not logged in")
                return@launch
            }

            if (_cartItems.value.isEmpty()) {
                _orderPlacementStatus.value = OrderPlacementStatus.Error("Cart is empty")
                return@launch
            }

            _orderPlacementStatus.value = OrderPlacementStatus.Loading

            try {
                val summary = cartSummary.value
                val orderItems = _cartItems.value.values.map { cartItem ->
                    OrderItem(
                        productId = cartItem.flavorKey,
                        productName = cartItem.flavorName,
                        quantity = cartItem.quantity,
                        unitPrice = cartItem.basePrice,
                        discountAmount = cartItem.basePrice - cartItem.discountedPrice,
                        lineTotal = cartItem.subtotal
                    )
                }

                val order = Order(
                    retailerId = currentUser.email ?: "",  // Use email as stable identifier
                    retailerEmail = currentUser.email ?: "",
                    retailerName = currentUser.name ?: "",
                    shopName = currentUser.shopName,
                    items = orderItems,
                    subtotal = summary.subtotal,
                    discount = summary.discountAmount,
                    totalAmount = summary.totalAmount,
                    retailerNotes = notes
                )

                android.util.Log.d("RetailerViewModel", "Creating order with retailerId: ${order.retailerId}, email: ${order.retailerEmail}")
                val result = orderRepository.createOrder(order)
                if (result.isSuccess) {
                    clearCart()
                    _orderPlacementStatus.value = OrderPlacementStatus.Success(result.getOrNull() ?: "")
                } else {
                    _orderPlacementStatus.value = OrderPlacementStatus.Error(
                        result.exceptionOrNull()?.message ?: "Failed to place order"
                    )
                }
            } catch (e: Exception) {
                _orderPlacementStatus.value = OrderPlacementStatus.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun placeQuickOrder(product: InventoryItem, quantity: Int, notes: String = "") {
        viewModelScope.launch {
            val currentUser = authRepository.currentUserFlow.first()
            if (currentUser == null) {
                _orderPlacementStatus.value = OrderPlacementStatus.Error("User not logged in")
                return@launch
            }

            if (quantity <= 0) {
                _orderPlacementStatus.value = OrderPlacementStatus.Error("Invalid quantity")
                return@launch
            }

            _orderPlacementStatus.value = OrderPlacementStatus.Loading

            try {
                val discountPercentage = currentUser.pricingTier?.discountPercentage ?: 0.0
                val discountedPrice = product.sellingPrice * (1 - discountPercentage / 100.0)
                val itemSubtotal = discountedPrice * quantity
                val baseSubtotal = product.sellingPrice * quantity
                val discountAmount = baseSubtotal - itemSubtotal

                val orderItem = OrderItem(
                    productId = product.flavorId,
                    productName = product.flavorName,
                    quantity = quantity,
                    unitPrice = product.sellingPrice,
                    discountAmount = product.sellingPrice - discountedPrice,
                    lineTotal = itemSubtotal
                )

                val order = Order(
                    retailerId = currentUser.id ?: "",
                    retailerEmail = currentUser.email ?: "",
                    retailerName = currentUser.name ?: "",
                    shopName = currentUser.shopName,
                    items = listOf(orderItem),
                    subtotal = baseSubtotal,
                    discount = discountAmount,
                    totalAmount = itemSubtotal,
                    retailerNotes = notes
                )

                val result = orderRepository.createOrder(order)
                if (result.isSuccess) {
                    _orderPlacementStatus.value = OrderPlacementStatus.Success(result.getOrNull() ?: "")
                } else {
                    _orderPlacementStatus.value = OrderPlacementStatus.Error(
                        result.exceptionOrNull()?.message ?: "Failed to place order"
                    )
                }
            } catch (e: Exception) {
                _orderPlacementStatus.value = OrderPlacementStatus.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetOrderStatus() {
        _orderPlacementStatus.value = OrderPlacementStatus.Idle
    }
}

data class CartItem(
    val flavorKey: String,
    val flavorName: String,
    val basePrice: Double,
    val discountedPrice: Double,
    val quantity: Int,
    val subtotal: Double
)

data class CartSummary(
    val itemCount: Int = 0,
    val subtotal: Double = 0.0,
    val discountAmount: Double = 0.0,
    val totalAmount: Double = 0.0
)

sealed class OrderPlacementStatus {
    object Idle : OrderPlacementStatus()
    object Loading : OrderPlacementStatus()
    data class Success(val orderId: String) : OrderPlacementStatus()
    data class Error(val message: String) : OrderPlacementStatus()
}
