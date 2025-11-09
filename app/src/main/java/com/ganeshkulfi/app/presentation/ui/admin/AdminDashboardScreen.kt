package com.ganeshkulfi.app.presentation.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ganeshkulfi.app.presentation.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onNavigateToInventory: () -> Unit,
    onNavigateToRetailers: () -> Unit,
    onNavigateToPricing: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val dashboardStats by viewModel.dashboardStats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Home, "Go to Customer View")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Welcome Section
                Text(
                    text = "Welcome, Owner",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "Shree Ganesh Kulfi - Business Overview",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Stats Cards Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatsCard(
                        modifier = Modifier.weight(1f),
                        title = "Today's Sales",
                        value = "₹${String.format("%.0f", dashboardStats.todaySales)}",
                        icon = Icons.Default.TrendingUp,
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                    
                    StatsCard(
                        modifier = Modifier.weight(1f),
                        title = "Total Revenue",
                        value = "₹${String.format("%.0f", dashboardStats.totalRevenue)}",
                        icon = Icons.Default.AccountBalance,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                }

                // Stats Cards Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatsCard(
                        modifier = Modifier.weight(1f),
                        title = "Total Stock",
                        value = "${dashboardStats.totalStock} units",
                        icon = Icons.Default.Inventory,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                    
                    StatsCard(
                        modifier = Modifier.weight(1f),
                        title = "Low Stock",
                        value = "${dashboardStats.lowStockItems} items",
                        icon = Icons.Default.Warning,
                        containerColor = if (dashboardStats.lowStockItems > 0) 
                            MaterialTheme.colorScheme.errorContainer 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                // Stats Cards Row 3
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatsCard(
                        modifier = Modifier.weight(1f),
                        title = "Active Retailers",
                        value = "${dashboardStats.activeRetailers}",
                        icon = Icons.Default.Store,
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                    
                    StatsCard(
                        modifier = Modifier.weight(1f),
                        title = "Pending Payments",
                        value = "${dashboardStats.pendingPayments}",
                        icon = Icons.Default.PendingActions,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Actions
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                QuickActionCard(
                    title = "Manage Inventory",
                    subtitle = "Update stock, add new flavors, set prices",
                    icon = Icons.Default.Inventory2,
                    onClick = onNavigateToInventory
                )

                QuickActionCard(
                    title = "Manage Retailers",
                    subtitle = "View retailers, track payments, give stock",
                    icon = Icons.Default.Groups,
                    onClick = onNavigateToRetailers
                )

                QuickActionCard(
                    title = "Pricing Management",
                    subtitle = "Set pricing tiers, custom prices for retailers",
                    icon = Icons.Default.AttachMoney,
                    onClick = onNavigateToPricing
                )

                QuickActionCard(
                    title = "View Orders",
                    subtitle = "Process orders, update status",
                    icon = Icons.Default.ShoppingBag,
                    onClick = onNavigateToOrders
                )

                QuickActionCard(
                    title = "Reports & Analytics",
                    subtitle = "Sales reports, inventory analysis",
                    icon = Icons.Default.Analytics,
                    onClick = onNavigateToReports
                )
            }
        }
    }
}

@Composable
fun StatsCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp)
            )
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Go",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
