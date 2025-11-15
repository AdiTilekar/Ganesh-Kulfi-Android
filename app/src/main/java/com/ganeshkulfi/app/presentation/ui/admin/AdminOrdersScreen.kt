package com.ganeshkulfi.app.presentation.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrdersScreen(
    adminViewModel: com.ganeshkulfi.app.presentation.viewmodel.AdminViewModel,
    onBackClick: () -> Unit
) {
    val orders by adminViewModel.orders.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Orders Management") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        if (orders.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.ShoppingBag,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "No Orders Yet",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Orders will appear here when customers place them",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Summary Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Order Summary",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                SummaryItem(
                                    label = "Total Orders",
                                    value = orders.size.toString(),
                                    icon = Icons.Default.ShoppingBag
                                )
                                SummaryItem(
                                    label = "Pending",
                                    value = orders.count { it.status == com.ganeshkulfi.app.data.model.OrderStatus.PENDING }.toString(),
                                    icon = Icons.Default.PendingActions
                                )
                                SummaryItem(
                                    label = "Completed",
                                    value = orders.count { it.status == com.ganeshkulfi.app.data.model.OrderStatus.COMPLETED }.toString(),
                                    icon = Icons.Default.CheckCircle
                                )
                            }
                        }
                    }
                }

                // Orders List Header
                item {
                    Text(
                        "All Orders",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Orders
                items(orders) { order ->
                    OrderCard(
                        order = order,
                        onUpdateStatus = { orderId, newStatus ->
                            adminViewModel.updateOrderStatus(orderId, newStatus)
                        },
                        onMarkPaymentReceived = { orderId ->
                            adminViewModel.markPaymentReceived(orderId)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderCard(
    order: com.ganeshkulfi.app.data.model.Order,
    onUpdateStatus: (String, com.ganeshkulfi.app.data.model.OrderStatus) -> Unit,
    onMarkPaymentReceived: (String) -> Unit
) {
    var showDetailsDialog by remember { mutableStateOf(false) }
    var showStatusMenu by remember { mutableStateOf(false) }

    Card(
        onClick = { showDetailsDialog = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        order.id,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        order.retailerName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                StatusChip(status = order.status.name)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "${order.items.size} items",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "₹${order.totalAmount}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    // Payment status badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Icon(
                            if (order.paymentStatus == com.ganeshkulfi.app.data.model.PaymentStatus.PAID)
                                Icons.Default.CheckCircle
                            else
                                Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = if (order.paymentStatus == com.ganeshkulfi.app.data.model.PaymentStatus.PAID)
                                MaterialTheme.colorScheme.tertiary
                            else
                                MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            order.paymentStatus.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (order.paymentStatus == com.ganeshkulfi.app.data.model.PaymentStatus.PAID)
                                MaterialTheme.colorScheme.tertiary
                            else
                                MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        formatTime(order.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Action buttons row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Payment status button
                    if (order.paymentStatus != com.ganeshkulfi.app.data.model.PaymentStatus.PAID) {
                        FilledTonalButton(
                            onClick = { onMarkPaymentReceived(order.id) },
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Icon(
                                Icons.Default.Payment,
                                contentDescription = "Mark Payment Received",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Payment Received", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    
                    // Status update buttons
                    if (order.status != com.ganeshkulfi.app.data.model.OrderStatus.CANCELLED) {
                        Box {
                            FilledTonalButton(
                                onClick = { showStatusMenu = true },
                                modifier = Modifier.height(32.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Icon(
                                    Icons.Default.ChangeCircle,
                                    contentDescription = "Change Status",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Update Status", style = MaterialTheme.typography.labelSmall)
                            }

                            DropdownMenu(
                                expanded = showStatusMenu,
                                onDismissRequest = { showStatusMenu = false }
                            ) {
                                if (order.status == com.ganeshkulfi.app.data.model.OrderStatus.PENDING) {
                                    DropdownMenuItem(
                                        text = { Text("Mark as Confirmed") },
                                        onClick = {
                                            onUpdateStatus(order.id, com.ganeshkulfi.app.data.model.OrderStatus.CONFIRMED)
                                            showStatusMenu = false
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                                        }
                                    )
                                }
                                if (order.status == com.ganeshkulfi.app.data.model.OrderStatus.CONFIRMED) {
                                    DropdownMenuItem(
                                        text = { Text("Mark as Completed") },
                                        onClick = {
                                            onUpdateStatus(order.id, com.ganeshkulfi.app.data.model.OrderStatus.COMPLETED)
                                            showStatusMenu = false
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.Done, contentDescription = null)
                                        }
                                    )
                                }
                                if (order.status != com.ganeshkulfi.app.data.model.OrderStatus.COMPLETED) {
                                    DropdownMenuItem(
                                        text = { Text("Cancel Order") },
                                        onClick = {
                                            onUpdateStatus(order.id, com.ganeshkulfi.app.data.model.OrderStatus.CANCELLED)
                                            showStatusMenu = false
                                        },
                                        leadingIcon = {
                                            Icon(Icons.Default.Cancel, contentDescription = null)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDetailsDialog) {
        OrderDetailsDialog(
            order = order,
            onDismiss = { showDetailsDialog = false }
        )
    }
}

@Composable
private fun StatusChip(status: String) {
    val (containerColor, contentColor) = when (status) {
        "PENDING" -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        "CONFIRMED" -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        "COMPLETED" -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        "CANCELLED" -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        color = containerColor,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            status.lowercase().replaceFirstChar { it.uppercase() },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun OrderDetailsDialog(
    order: com.ganeshkulfi.app.data.model.Order,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.Receipt, contentDescription = null)
        },
        title = {
            Text("Order Details")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailRow("Order ID", order.id)
                DetailRow("Retailer", order.retailerName)
                order.shopName?.let {
                    DetailRow("Shop", it)
                }
                DetailRow("Status", order.status.name.lowercase().replaceFirstChar { it.uppercase() })
                
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Items:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                order.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "${item.productName} x${item.quantity}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "₹${item.lineTotal}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                DetailRow("Subtotal", "₹${order.subtotal}")
                if (order.discount > 0) {
                    DetailRow("Discount", "-₹${order.discount}")
                }
                DetailRow("Total Amount", "₹${order.totalAmount}")
                DetailRow("Payment", order.paymentStatus.name.lowercase().replaceFirstChar { it.uppercase() })
                DetailRow("Order Time", formatFullTime(order.createdAt))
                
                order.retailerNotes?.let {
                    if (it.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Notes:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val minutes = diff / 60000
    val hours = minutes / 60
    val days = hours / 24

    return when {
        minutes < 60 -> "$minutes min ago"
        hours < 24 -> "$hours hours ago"
        else -> "$days days ago"
    }
}

private fun formatFullTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
