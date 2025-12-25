package com.example.travel

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Orders")
data class Order(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "order_number")
    val orderNumber: Int? = null,
    @ColumnInfo(name = "client_id")
    val clientId: Int,
    @ColumnInfo(name = "client_login")
    val clientLogin: String,
    @ColumnInfo(name = "tour_id")
    val tourId: Int,
    @ColumnInfo(name = "tour_name")
    val tourName: String,
    @ColumnInfo(name = "buyer_name")
    val buyerName: String,
    @ColumnInfo(name = "order_date")
    val orderDate: String,
    @ColumnInfo(name = "original_price")
    val originalPrice: Int,
    @ColumnInfo(name = "discount_percent")
    val discountPercent: Int,
    @ColumnInfo(name = "final_price")
    val finalPrice: Int,
    @ColumnInfo(name = "status") // "pending", "confirmed", "completed", "cancelled"
    val status: String = "confirmed"
)