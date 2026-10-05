package com.example.productcatalog.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CartDao {

    @Query("SELECT * FROM cart_items ORDER BY addedAt ASC")
    abstract fun observeAll(): Flow<List<CartItemEntity>>

    @Query("SELECT * FROM cart_items WHERE productId = :id")
    abstract suspend fun getById(id: Int): CartItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsert(item: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE productId = :id")
    abstract suspend fun delete(id: Int)

    /** Adds the item, or bumps quantity by 1 if it is already in the cart. */
    @Transaction
    open suspend fun addOrIncrement(item: CartItemEntity) {
        val existing = getById(item.productId)
        upsert(if (existing == null) item else existing.copy(quantity = existing.quantity + 1))
    }

    /** Changes quantity by [delta]; removes the row when it reaches 0. */
    @Transaction
    open suspend fun changeQuantity(id: Int, delta: Int) {
        val existing = getById(id) ?: return
        val newQty = existing.quantity + delta
        if (newQty <= 0) delete(id) else upsert(existing.copy(quantity = newQty))
    }
}
