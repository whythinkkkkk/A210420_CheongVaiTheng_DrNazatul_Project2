package com.example.inventory

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.inventory.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database by lazy { OrderDatabase.getDatabase(applicationContext) }
        val repository by lazy { OrderRepository(database.orderDao()) }

        setContent {
            AppTheme {
                val navController = rememberNavController()

                val orderViewModel: OrderViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return OrderViewModel(repository) as T
                        }
                    }
                )

                NavHost(
                    navController = navController,
                    startDestination = "login"
                ) {
                    composable("login") {
                        LoginScreen(
                            viewModel = orderViewModel,
                            onLoginSuccess = {
                                navController.navigate("marketplace") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("marketplace") {
                        MarketplaceScreen(
                            viewModel = orderViewModel,
                            onNavigateToCheckout = { navController.navigate("checkout") },
                            onNavigateToCart = { navController.navigate("cart") },
                            onNavigateToSettings = { navController.navigate("settings") }
                        )
                    }

                    composable("checkout") {
                        CheckoutScreen(
                            viewModel = orderViewModel,
                            onGoToCart = {
                                navController.navigate("cart") {
                                    popUpTo("checkout") { inclusive = true }
                                }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("cart") {
                        CartScreen(
                            viewModel = orderViewModel,
                            onNavigateToPayment = { navController.navigate("payment") },
                            onNavigateToCheckout = { navController.navigate("checkout") },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("payment") {
                        PaymentScreen(
                            viewModel = orderViewModel,
                            onConfirmPayment = {
                                navController.navigate("receipt") {
                                    popUpTo("cart") { inclusive = true }
                                }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("receipt") {
                        ReceiptScreen(
                            viewModel = orderViewModel,
                            onReturnHome = {
                                navController.popBackStack("marketplace", inclusive = false)
                            }
                        )
                    }

                    composable("settings") {
                        SettingsScreen(
                            viewModel = orderViewModel,
                            onBack = { navController.popBackStack() },
                            onNavigateToOrderHistory = { navController.navigate("order_history") }
                        )
                    }

                    composable("order_history") {
                        OrderHistoryScreen(
                            viewModel = orderViewModel,
                            onNavigateToDetail = { navController.navigate("order_detail") },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("order_detail") {
                        OrderDetailScreen(
                            viewModel = orderViewModel,
                            onBack = { navController.popBackStack() }                        )
                    }
                }
            }
        }
    }
}