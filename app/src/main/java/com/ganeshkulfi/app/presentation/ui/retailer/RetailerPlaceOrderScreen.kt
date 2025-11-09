package com.ganeshkulfi.app.presentation.ui.retailer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ganeshkulfi.app.data.model.InventoryItem
import com.ganeshkulfi.app.presentation.viewmodel.RetailerViewModel
import com.ganeshkulfi.app.presentation.viewmodel.OrderPlacementStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetailerPlaceOrderScreen(
    onNavigateBack: () -> Unit,
    retailerViewModel: RetailerViewModel = hiltViewModel()
) {
    val products by retailerViewModel.availableProducts.collectAsState()
    val cartItems by retailerViewModel.cartItems.collectAsState()
    val cartSummary by retailerViewModel.cartSummary.collectAsState()
    val orderStatus by retailerViewModel.orderPlacementStatus.collectAsState()

    var showCheckoutDialog by remember { mutableStateOf(false) }
    var orderNotes by remember { mutableStateOf("") }

    // Handle order placement status
    LaunchedEffect(orderStatus) {
        when (orderStatus) {
            is OrderPlacementStatus.Success -> {
                showCheckoutDialog = false
                orderNotes = ""
                // Show success message and navigate back
                onNavigateBack()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Place Order") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (cartItems.isNotEmpty()) {
                        IconButton(onClick = { showCheckoutDialog = true }) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error
                            ) {
                                Text("${cartSummary.itemCount}")
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                CartSummaryBar(
                    summary = cartSummary,
                    onCheckout = { showCheckoutDialog = true }
                )
            }
        }
    ) { paddingValues ->
        if (products.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Inventory,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No products available",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Instructions Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "All prices shown are your special discounted prices",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Product List
                items(products) { product ->
                    ProductOrderCard(
                        product = product,
                        cartQuantity = cartItems[product.flavorId]?.quantity ?: 0,
                        onAddToCart = { retailerViewModel.addToCart(product) },
                        onUpdateQuantity = { qty -> 
                            retailerViewModel.updateCartItemQuantity(product.flavorId, qty)
                        },
                        onRemove = { retailerViewModel.removeFromCart(product.flavorId) }
                    )
                }

                // Bottom padding for FAB
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        // Checkout Dialog
        if (showCheckoutDialog) {
            CheckoutDialog(
                summary = cartSummary,
                cartItems = cartItems.values.toList(),
                notes = orderNotes,
                onNotesChange = { orderNotes = it },
                onConfirm = {
                    retailerViewModel.placeOrder(orderNotes)
                },
                onDismiss = { 
                    showCheckoutDialog = false
                    retailerViewModel.resetOrderStatus()
                },
                isLoading = orderStatus is OrderPlacementStatus.Loading,
                error = (orderStatus as? OrderPlacementStatus.Error)?.message
            )
        }
    }
}

@Composable
fun ProductOrderCard(
    product: InventoryItem,
    cartQuantity: Int,
    onAddToCart: () -> Unit,
    onUpdateQuantity: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        product.flavorName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Retailers don't need to see factory stock levels
                    // They can order any quantity they need
                }
                
                Text(
                    "₹${String.format("%.2f", product.sellingPrice)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (cartQuantity > 0) {
                // Quantity Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onUpdateQuantity(cartQuantity - 1) }
                        ) {
                            Icon(Icons.Default.Remove, "Decrease")
                        }
                        
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                "$cartQuantity",
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        IconButton(
                            onClick = { onUpdateQuantity(cartQuantity + 1) },
                            enabled = true  // Retailers can order unlimited quantity
                        ) {
                            Icon(Icons.Default.Add, "Increase")
                        }
                    }
                    
                    TextButton(onClick = onRemove) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Remove")
                    }
                }
            } else {
                // Add to Cart Button - Retailers can always add to cart
                Button(
                    onClick = onAddToCart,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = true  // No stock restrictions for retailers
                ) {
                    Icon(Icons.Default.AddShoppingCart, "Add to cart")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add to Cart")
                }
            }
        }
    }
}

@Composable
fun CartSummaryBar(
    summary: com.ganeshkulfi.app.presentation.viewmodel.CartSummary,
    onCheckout: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "${summary.itemCount} items",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "₹${String.format("%.2f", summary.totalAmount)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (summary.discountAmount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "₹${String.format("%.2f", summary.subtotal)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                        )
                    }
                }
            }
            
            Button(
                onClick = onCheckout,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Checkout")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.ArrowForward, null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun CheckoutDialog(
    summary: com.ganeshkulfi.app.presentation.viewmodel.CartSummary,
    cartItems: List<com.ganeshkulfi.app.presentation.viewmodel.CartItem>,
    notes: String,
    onNotesChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean,
    error: String?
) {
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Confirm Order") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "Order Summary",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(cartItems) { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                item.flavorName,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "${item.quantity} × ₹${String.format("%.2f", item.discountedPrice)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            "₹${String.format("%.2f", item.subtotal)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                item { Divider() }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "₹${String.format("%.2f", summary.subtotal)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        if (summary.discountAmount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Discount",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "- ₹${String.format("%.2f", summary.discountAmount)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Total",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "₹${String.format("%.2f", summary.totalAmount)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = onNotesChange,
                        label = { Text("Order Notes (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        enabled = !isLoading
                    )
                }

                if (error != null) {
                    item {
                        Text(
                            error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Place Order")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        }
    )
}
