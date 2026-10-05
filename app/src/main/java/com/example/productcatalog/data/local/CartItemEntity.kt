package com.example.productcatalog.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.productcatalog.domain.CartItem

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val productId: Int,
    val title: String,
    val price: Double,
    val thumbnail: String,
    val quantity: Int,
    val addedAt: Long = System.currentTimeMillis()
)

fun CartItemEntity.toDomain() = CartItem(productId, title, price, thumbnail, quantity)
