package com.example.productcatalog.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.productcatalog.CatalogApp
import com.example.productcatalog.data.repository.CartRepository
import com.example.productcatalog.domain.CartItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val totalItems: Int = 0,
    val totalPrice: Double = 0.0
)

class CartViewModel(private val repository: CartRepository) : ViewModel() {

    // Totals are derived from the DB flow, so they're always consistent and work offline.
    val state: StateFlow<CartUiState> = repository.observeCart()
        .map { items ->
            CartUiState(
                items = items,
                totalItems = items.sumOf { it.quantity },
                totalPrice = items.sumOf { it.price * it.quantity }
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CartUiState())

    fun increase(id: Int) { viewModelScope.launch { repository.increase(id) } }
    fun decrease(id: Int) { viewModelScope.launch { repository.decrease(id) } }
    fun remove(id: Int) { viewModelScope.launch { repository.remove(id) } }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as CatalogApp
                CartViewModel(app.container.cartRepository)
            }
        }
    }
}
