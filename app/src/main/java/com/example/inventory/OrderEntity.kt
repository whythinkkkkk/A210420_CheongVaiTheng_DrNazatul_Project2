package com.example.inventory

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders_table")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val billId: String = "",
    val name: String = "",
    val quantity: Int = 0,
    val totalPrice: Double = 0.0,
    val date: String = "",
    val status: String = "Delivering"
)
