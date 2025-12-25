package com.example.travel

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Insert
    fun insertOrder(order: Order)

    @Query("SELECT * FROM Orders")
    fun getAllOrders(): Flow<List<Order>>

    @Query("SELECT * FROM Orders WHERE client_id = :clientId")
    fun getClientOrders(clientId: Int): Flow<List<Order>>

    @Query("SELECT * FROM Orders WHERE client_login = :clientLogin")
    fun getClientOrdersByLogin(clientLogin: String): Flow<List<Order>>

    @Query("SELECT * FROM Orders GROUP BY client_login")
    fun getOrdersGroupedByClient(): Flow<List<Order>>

    @Query("SELECT * FROM Orders WHERE tour_id = :tourId")
    fun getOrdersForTour(tourId: Int): Flow<List<Order>>

    @Query("SELECT * FROM Orders WHERE order_number = :orderNumber")
    fun getOrderByNumber(orderNumber: Int): Order?

    @Query("SELECT COUNT(*) FROM Orders WHERE client_login = :clientLogin")
    fun getOrderCountForClient(clientLogin: String): Int

    @Query("SELECT SUM(final_price) FROM Orders WHERE client_login = :clientLogin")
    fun getTotalSpentByClient(clientLogin: String): Double?
}