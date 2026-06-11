package com.example.inventory

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import androidx.room.Delete

@Dao
interface OrderDao {

    // Returns a continuous stream of changes directly from SQLite
    @Query("SELECT * FROM orders_table ORDER BY id DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    // Inserts a newly purchased item from the checkout screen
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    // Updates existing orders (crucial for changing delivery status)
    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("DELETE FROM orders_table")
    suspend fun deleteAll()

}