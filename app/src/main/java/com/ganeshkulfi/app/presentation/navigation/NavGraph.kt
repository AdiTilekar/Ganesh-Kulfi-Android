package com.ganeshkulfi.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ganeshkulfi.app.presentation.ui.auth.LoginScreen
import com.ganeshkulfi.app.presentation.ui.auth.SignUpScreen
import com.ganeshkulfi.app.presentation.ui.cart.CartScreen
import com.ganeshkulfi.app.presentation.ui.home.HomeScreen
import com.ganeshkulfi.app.presentation.ui.orders.OrdersScreen
import com.ganeshkulfi.app.presentation.ui.profile.ProfileScreen
import com.ganeshkulfi.app.presentation.ui.splash.SplashScreen
import com.ganeshkulfi.app.presentation.ui.admin.AdminDashboardScreen
import com.ganeshkulfi.app.presentation.ui.admin.AdminOrdersScreen
import com.ganeshkulfi.app.presentation.ui.admin.InventoryManagementScreen
import com.ganeshkulfi.app.presentation.ui.admin.RetailerManagementScreen
import com.ganeshkulfi.app.presentation.ui.retailer.RetailerHomeScreen
import com.ganeshkulfi.app.presentation.screens.PricingManagementScreen
import com.ganeshkulfi.app.presentation.screens.ReportsAnalyticsScreen
import com.ganeshkulfi.app.presentation.viewmodel.AuthViewModel

@Composable
fun NavGraph(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val currentUser by authViewModel.currentUser.collectAsState()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToAdmin = {
                    navController.navigate(Screen.Admin.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToRetailerHome = {
                    navController.navigate(Screen.RetailerHome.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route)
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToAdmin = {
                    navController.navigate(Screen.Admin.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRetailerHome = {
                    navController.navigate(Screen.RetailerHome.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.SignUp.route) {
            SignUpScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.SignUp.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToCart = {
                    navController.navigate(Screen.Cart.route)
                },
                onNavigateToOrders = {
                    navController.navigate(Screen.Orders.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAdmin = {
                    navController.navigate(Screen.Admin.route)
                }
            )
        }
        
        composable(Screen.Cart.route) {
            CartScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Orders.route) {
            OrdersScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Admin.route) {
            AdminDashboardScreen(
                onNavigateToInventory = {
                    navController.navigate("admin/inventory")
                },
                onNavigateToRetailers = {
                    navController.navigate("admin/retailers")
                },
                onNavigateToPricing = {
                    navController.navigate("admin/pricing")
                },
                onNavigateToOrders = {
                    navController.navigate(Screen.AdminOrders.route)
                },
                onNavigateToReports = {
                    navController.navigate(Screen.AdminReports.route)
                },
                onNavigateBack = {
                    // Navigate to home screen (customer view) so admin can see what customers see
                    navController.navigate(Screen.Home.route)
                }
            )
        }
        
        composable("admin/inventory") {
            InventoryManagementScreen(
                onNavigateBack = {
                    navController.navigate(Screen.Admin.route) {
                        popUpTo("admin/inventory") { inclusive = true }
                    }
                }
            )
        }
        
        composable("admin/retailers") {
            RetailerManagementScreen(
                onNavigateBack = {
                    navController.navigate(Screen.Admin.route) {
                        popUpTo("admin/retailers") { inclusive = true }
                    }
                },
                onRetailerClick = { retailerId ->
                    // TODO: Navigate to retailer details
                }
            )
        }
        
        composable("admin/pricing") {
            PricingManagementScreen(
                onBackClick = {
                    navController.navigate(Screen.Admin.route) {
                        popUpTo("admin/pricing") { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.AdminOrders.route) {
            val adminViewModel: com.ganeshkulfi.app.presentation.viewmodel.AdminViewModel = hiltViewModel()
            AdminOrdersScreen(
                adminViewModel = adminViewModel,
                onBackClick = {
                    navController.navigate(Screen.Admin.route) {
                        popUpTo(Screen.AdminOrders.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.AdminReports.route) {
            ReportsAnalyticsScreen(
                onBackClick = {
                    navController.navigate(Screen.Admin.route) {
                        popUpTo(Screen.AdminReports.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Retailer Routes
        composable(Screen.RetailerHome.route) {
            RetailerHomeScreen(
                onNavigateToOrders = {
                    navController.navigate(Screen.RetailerOrders.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.RetailerProfile.route)
                },
                onNavigateToOrderKulfi = {
                    navController.navigate(Screen.RetailerOrderKulfi.route)
                }
            )
        }

        composable(Screen.RetailerOrderKulfi.route) {
            com.ganeshkulfi.app.presentation.ui.retailer.RetailerOrderKulfiScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.RetailerOrders.route) {
            // TODO: Create RetailerOrdersScreen
            OrdersScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.RetailerProfile.route) {
            ProfileScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.RetailerHome.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAdmin = {}
            )
        }
        
        // RetailerPlaceOrder screen removed - orders are now placed directly from home screen
    }
}
