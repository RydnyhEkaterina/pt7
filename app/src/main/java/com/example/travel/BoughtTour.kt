package com.example.travel

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "BoughtTours")
data class BoughtTour(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,

    @ColumnInfo(name = "userLogin")
    val userLogin: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "username")
    val username: String,

    @ColumnInfo(name = "country")
    val country: String,

    @ColumnInfo(name = "date")
    val date: String,

    @ColumnInfo(name = "flagUrl")
    var flagUrl: String,

    @ColumnInfo(name = "imageUrl")
    var imageUrl: String,

    @ColumnInfo(name = "original_price")
    val originalPrice: Int = 0,

    @ColumnInfo(name = "discount_applied")
    val discountApplied: Int = 0,

    @ColumnInfo(name = "final_price")
    val finalPrice: Int = 0
)