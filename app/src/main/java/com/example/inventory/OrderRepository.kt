package com.example.inventory

import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class OrderRepository(private val orderDao: OrderDao) {
    private val firebaseDb = FirebaseDatabase
        .getInstance("https://ecomart-9f6f8-default-rtdb.asia-southeast1.firebasedatabase.app")
        .reference
        .child("orders")

    // Exposes the continuous stream of database changes to the ViewModel
    val allOrders: Flow<List<OrderEntity>> = orderDao.getAllOrders()

    // Inserts an order on a background thread
    suspend fun insert(order: OrderEntity) {
        orderDao.insertOrder(order)

        val cloudKey = order.billId.ifEmpty { order.id.toString() }
        firebaseDb.child(cloudKey).child(order.name).setValue(order)
    }


    // Updates an order status on a background thread
    suspend fun update(order: OrderEntity) {
        orderDao.updateOrder(order)

        val cloudKey = order.billId.ifEmpty { order.id.toString() }
        firebaseDb.child(cloudKey).child(order.name).setValue(order)
    }

    suspend fun syncFromCloud() {
        try {
            val snapshot = firebaseDb.get().await()
            if (snapshot.exists()) {

                orderDao.deleteAll()

                for (billSnapshot in snapshot.children) {
                    for (orderSnapshot in billSnapshot.children) {
                        val order = orderSnapshot.getValue(OrderEntity::class.java)
                        if (order != null) {
                            orderDao.insertOrder(order)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}