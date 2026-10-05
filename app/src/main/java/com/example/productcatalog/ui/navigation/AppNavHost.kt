package com.example.productcatalog.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.productcatalog.ui.cart.CartScreen
import com.example.productcatalog.ui.cart.CartViewModel
import com.example.productcatalog.ui.detail.ProductDetailScreen
import com.example.productcatalog.ui.products.ProductsScreen

object Routes {
    const val PRODUCTS = "products"
    const val CART = "cart"
    const val DETAIL = "product/{productId}"
    fun detail(id: Int) = "product/$id"
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    // Activity-scoped so the badge on the list screen and the cart screen share state.
    val cartViewModel: CartViewModel = viewModel(factory = CartViewModel.Factory)
    val cartState by cartViewModel.state.collectAsStateWithLifecycle()

    NavHost(navController = navController, startDestination = Routes.PRODUCTS) {
        composable(Routes.PRODUCTS) {
            ProductsScreen(
                cartCount = cartState.totalItems,
                onProductClick = { navController.navigate(Routes.detail(it)) },
                onCartClick = { navController.navigate(Routes.CART) }
            )
        }
        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) {
            ProductDetailScreen(
                cartCount = cartState.totalItems,
                cartViewModel = cartViewModel,
                onBack = { navController.popBackStack() },
                onCartClick = { navController.navigate(Routes.CART) }
            )
        }
        composable(Routes.CART) {
            CartScreen(
                viewModel = cartViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
