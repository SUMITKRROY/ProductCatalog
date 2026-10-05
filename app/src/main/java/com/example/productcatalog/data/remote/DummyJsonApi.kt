package com.example.productcatalog.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DummyJsonApi {

    @GET("products")
    suspend fun getProducts(
        @Query("limit") limit: Int = 30,
        @Query("skip") skip: Int = 0
    ): ProductsResponse

    @GET("products/search")
    suspend fun searchProducts(@Query("q") query: String): ProductsResponse

    @GET("products/{id}")
    suspend fun getProduct(@Path("id") id: Int): ProductDto

    @GET("products/category-list")
    suspend fun getCategories(): List<String>

    @GET("products/category/{category}")
    suspend fun getProductsByCategory(@Path("category") category: String): ProductsResponse
}
