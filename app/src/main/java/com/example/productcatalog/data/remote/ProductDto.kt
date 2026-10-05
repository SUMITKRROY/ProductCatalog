package com.example.productcatalog.data.remote

import com.example.productcatalog.domain.Product

data class ProductsResponse(
    val products: List<ProductDto>,
    val total: Int,
    val skip: Int,
    val limit: Int
)

data class ProductDto(
    val id: Int,
    val title: String?,
    val description: String?,
    val price: Double?,
    val rating: Double?,
    val category: String?,
    val brand: String?,
    val stock: Int?,
    val thumbnail: String?
)

fun ProductDto.toDomain() = Product(
    id = id,
    title = title.orEmpty(),
    description = description.orEmpty(),
    price = price ?: 0.0,
    rating = rating ?: 0.0,
    category = category.orEmpty(),
    brand = brand,
    stock = stock ?: 0,
    thumbnail = thumbnail.orEmpty()
)
