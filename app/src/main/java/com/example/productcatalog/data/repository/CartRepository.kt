package com.example.productcatalog.data.repository

import com.example.productcatalog.data.local.CartDao
import com.example.productcatalog.data.local.CartItemEntity
import com.example.productcatalog.data.local.toDomain
import com.example.productcatalog.domain.CartItem
import com.example.productcatalog.domain.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CartRepository(private val dao: CartDao) {

    fun observeCart(): Flow<List<CartItem>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun add(product: Product) = dao.addOrIncrement(
        CartItemEntity(
            productId = product.id,
            title = product.title,
            price = product.price,
            thumbnail = product.thumbnail,
            quantity = 1
        )
    )

    suspend fun increase(productId: Int) = dao.changeQuantity(productId, +1)
    suspend fun decrease(productId: Int) = dao.changeQuantity(productId, -1)
    suspend fun remove(productId: Int) = dao.delete(productId)
}
