package com.example.productcatalog.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CartItemEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao
}
