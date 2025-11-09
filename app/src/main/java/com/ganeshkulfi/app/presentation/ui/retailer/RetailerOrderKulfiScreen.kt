package com.ganeshkulfi.app.presentation.ui.retailer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ganeshkulfi.app.data.model.InventoryItem
import com.ganeshkulfi.app.data.model.PricingTier
import com.ganeshkulfi.app.presentation.viewmodel.AuthViewModel
import com.ganeshkulfi.app.presentation.viewmodel.AdminViewModel
import com.ganeshkulfi.app.presentation.viewmodel.RetailerViewModel
import com.ganeshkulfi.app.presentation.viewmodel.OrderPlacementStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RetailerOrderKulfiScreen(
    onNavigateBack: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    adminViewModel: AdminViewModel = hiltViewModel(),
    retailerViewModel: RetailerViewModel = hiltViewModel()
) {
    val currentUser = authViewModel.currentUser.collectAsState().value
    val inventory = adminViewModel.inventory.collectAsState().value
    val orderStatus by retailerViewModel.orderPlacementStatus.collectAsState()
    
    // Get retailer's pricing tier
    val pricingTier = currentUser?.pricingTier ?: PricingTier.REGULAR

    // Quick order dialog state
    var selectedProduct by remember { mutableStateOf<InventoryItem?>(null) }
    var orderQuantity by remember { mutableStateOf(1) }
    var orderNotes by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Handle order status
    LaunchedEffect(orderStatus) {
        when (orderStatus) {
            is OrderPlacementStatus.Success -> {
                selectedProduct = null
                orderQuantity = 1
                orderNotes = ""
                showSuccessDialog = true
                retailerViewModel.resetOrderStatus()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Kulfi") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (inventory.isEmpty()) {
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
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(inventory) { product ->
                    KulfiImageCard(
                        product = product,
                        pricingTier = pricingTier,
                        onClick = { 
                            selectedProduct = product
                            orderQuantity = 1
                            orderNotes = ""
                        }
                    )
                }
            }
        }

        // Quick Order Dialog
        selectedProduct?.let { product ->
            QuickOrderDialog(
                product = product,
                pricingTier = pricingTier,
                quantity = orderQuantity,
                notes = orderNotes,
                onQuantityChange = { orderQuantity = it },
                onNotesChange = { orderNotes = it },
                onConfirm = {
                    retailerViewModel.placeQuickOrder(product, orderQuantity, orderNotes)
                },
                onDismiss = { 
                    selectedProduct = null
                    retailerViewModel.resetOrderStatus()
                },
                isLoading = orderStatus is OrderPlacementStatus.Loading,
                error = (orderStatus as? OrderPlacementStatus.Error)?.message
            )
        }

        // Success Dialog
        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = { showSuccessDialog = false },
                icon = { Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary) },
                title = { Text("Order Placed Successfully!") },
                text = { Text("Your order has been placed and will be processed soon.") },
                confirmButton = {
                    Button(onClick = { showSuccessDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KulfiImageCard(
    product: InventoryItem,
    pricingTier: PricingTier,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val discountedPrice = product.sellingPrice * (1 - (pricingTier.discountPercentage ?: 0.0) / 100.0)
    val savings = product.sellingPrice - discountedPrice
    val drawableName = getDrawableResourceId(product.flavorId)
    val drawableId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Kulfi Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(drawableId)
                        .crossfade(true)
                        .build(),
                    contentDescription = product.flavorName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Retailers don't need to see stock levels
                // Factory owner will fulfill any order quantity
            }

            // Product Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    product.flavorName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Price
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "₹${String.format("%.0f", discountedPrice)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (savings > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "₹${String.format("%.0f", product.sellingPrice)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                }

                if (savings > 0) {
                    Text(
                        "Save ₹${String.format("%.0f", savings)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// Helper function to get drawable resource ID
private fun getDrawableResourceId(flavorId: String): String {
    return when (flavorId.lowercase()) {
        "mango" -> "mango_kulfi"
        "rabdi", "rabadi" -> "rabdi_kulfi"
        "strawberry" -> "strawberry_kulfi"
        "chocolate" -> "chocolate_kulfi"
        "paan" -> "paan_kulfi"
        "pineapple" -> "pineapple_kulfi"
        "chikoo" -> "chikoo_kulfi"
        "guava" -> "guava_kulfi"
        "jamun" -> "jamun_kulfi"
        "fig" -> "fig_kulfi"
        "shitafal", "sitafal", "custard_apple" -> "sitafal_kulfi"
        "dry_fruit", "dryfruit" -> "dry_fruit_kulfi"
        "gulkand" -> "gulkand_kulfi"
        else -> "logo"
    }
}

@Composable
private fun QuickOrderDialog(
    product: InventoryItem,
    pricingTier: PricingTier,
    quantity: Int,
    notes: String,
    onQuantityChange: (Int) -> Unit,
    onNotesChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean,
    error: String?
) {
    val context = LocalContext.current
    val discountedPrice = product.sellingPrice * (1 - (pricingTier.discountPercentage ?: 0.0) / 100.0)
    val totalPrice = discountedPrice * quantity
    val totalSavings = (product.sellingPrice - discountedPrice) * quantity
    val drawableName = getDrawableResourceId(product.flavorId)
    val drawableId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { 
            Text(
                product.flavorName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Product Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(drawableId)
                            .crossfade(true)
                            .build(),
                        contentDescription = product.flavorName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Price Info
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Price per unit:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "₹${String.format("%.2f", discountedPrice)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (totalSavings > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "You save:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "₹${String.format("%.2f", totalSavings)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Quantity Selector with Direct Input
                Column {
                    Text(
                        "Quantity",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { if (quantity > 1) onQuantityChange(quantity - 1) },
                            enabled = !isLoading && quantity > 1
                        ) {
                            Icon(Icons.Default.Remove, "Decrease")
                        }

                        // Editable TextField for direct quantity input
                        var textValue by remember(quantity) { mutableStateOf(quantity.toString()) }
                        
                        OutlinedTextField(
                            value = textValue,
                            onValueChange = { input ->
                                // Allow only digits
                                if (input.all { it.isDigit() } || input.isEmpty()) {
                                    textValue = input
                                    
                                    // Parse and update quantity - NO STOCK LIMIT
                                    if (input.isNotEmpty()) {
                                        val newQuantity = input.toIntOrNull()
                                        if (newQuantity != null && newQuantity > 0) {
                                            // Accept any positive number - factory will fulfill
                                            onQuantityChange(newQuantity)
                                        }
                                    }
                                    // If empty, don't update quantity yet - user is still typing
                                }
                            },
                            modifier = Modifier.weight(1f),
                            textStyle = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            ),
                            singleLine = true,
                            enabled = !isLoading,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    // Validate and correct on done - NO STOCK LIMIT
                                    val finalQuantity = textValue.toIntOrNull()
                                    if (finalQuantity == null || finalQuantity <= 0) {
                                        // If invalid or empty, default to 1
                                        onQuantityChange(1)
                                        textValue = "1"
                                    } else {
                                        // Accept any valid positive number
                                        onQuantityChange(finalQuantity)
                                    }
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        IconButton(
                            onClick = { onQuantityChange(quantity + 1) },
                            enabled = !isLoading
                        ) {
                            Icon(Icons.Default.Add, "Increase")
                        }
                    }
                }

                // Notes Field
                OutlinedTextField(
                    value = notes,
                    onValueChange = onNotesChange,
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    enabled = !isLoading
                )

                // Total Amount
                Divider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Total Amount:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "₹${String.format("%.2f", totalPrice)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (error != null) {
                    Text(
                        error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
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
