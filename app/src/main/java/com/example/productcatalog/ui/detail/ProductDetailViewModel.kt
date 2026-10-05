package com.example.productcatalog.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.productcatalog.CatalogApp
import com.example.productcatalog.data.repository.CartRepository
import com.example.productcatalog.data.repository.ProductRepository
import com.example.productcatalog.domain.Product
import com.example.productcatalog.util.toUserMessage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Error(val message: String) : DetailUiState
    data class Success(val product: Product) : DetailUiState
}

class ProductDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val productId: Int = checkNotNull(savedStateHandle["productId"])

    private val _state = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val state: StateFlow<DetailUiState> = _state

    private val _messages = MutableSharedFlow<String>()
    val messages: SharedFlow<String> = _messages

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = DetailUiState.Loading
            productRepository.getProduct(productId).fold(
                onSuccess = { _state.value = DetailUiState.Success(it) },
                onFailure = { _state.value = DetailUiState.Error(it.toUserMessage()) }
            )
        }
    }

    fun addToCart() {
        val product = (_state.value as? DetailUiState.Success)?.product ?: return
        viewModelScope.launch {
            cartRepository.add(product)
            _messages.emit("Added to cart")
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as CatalogApp
                ProductDetailViewModel(
                    createSavedStateHandle(),
                    app.container.productRepository,
                    app.container.cartRepository
                )
            }
        }
    }
}
