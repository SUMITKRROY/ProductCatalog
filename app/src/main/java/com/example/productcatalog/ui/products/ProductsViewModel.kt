package com.example.productcatalog.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.productcatalog.CatalogApp
import com.example.productcatalog.data.repository.ProductRepository
import com.example.productcatalog.domain.Product
import com.example.productcatalog.util.toUserMessage
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

sealed interface ProductsUiState {
    data object Loading : ProductsUiState
    data object Empty : ProductsUiState
    data class Error(val message: String) : ProductsUiState
    data class Success(val products: List<Product>) : ProductsUiState
}

@OptIn(FlowPreview::class)
class ProductsViewModel(private val repository: ProductRepository) : ViewModel() {

    private val _state = MutableStateFlow<ProductsUiState>(ProductsUiState.Loading)
    val state: StateFlow<ProductsUiState> = _state

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    init {
        viewModelScope.launch {
            _query
                .debounce { if (it.isEmpty()) 0L else 400L } // debounce typing, not clearing
                .distinctUntilChanged()
                .collectLatest { load(it) } // new query cancels the in-flight request
        }
    }

    fun onQueryChange(value: String) {
        _query.value = value
    }

    fun retry() {
        viewModelScope.launch { load(_query.value) }
    }

    private suspend fun load(query: String) {
        _state.value = ProductsUiState.Loading
        repository.getProducts(query).fold(
            onSuccess = { list ->
                _state.value = if (list.isEmpty()) ProductsUiState.Empty else ProductsUiState.Success(list)
            },
            onFailure = { _state.value = ProductsUiState.Error(it.toUserMessage()) }
        )
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as CatalogApp
                ProductsViewModel(app.container.productRepository)
            }
        }
    }
}
