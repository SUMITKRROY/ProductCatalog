package com.example.productcatalog.data.repository

import com.example.productcatalog.data.remote.DummyJsonApi
import com.example.productcatalog.data.remote.toDomain
import com.example.productcatalog.domain.Product
import com.example.productcatalog.util.safeApiCall

class ProductRepository(private val api: DummyJsonApi) {

    /** Blank query -> full list, otherwise server-side search. */
    suspend fun getProducts(query: String): Result<List<Product>> = safeApiCall {
        val response = if (query.isBlank()) api.getProducts() else api.searchProducts(query.trim())
        response.products.map { it.toDomain() }
    }

    suspend fun getProduct(id: Int): Result<Product> = safeApiCall {
        api.getProduct(id).toDomain()
    }

    suspend fun getCategories(): Result<List<String>> = safeApiCall { api.getCategories() }
}
