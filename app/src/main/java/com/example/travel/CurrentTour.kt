package com.example.travel

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "CurrentTours")
data class CurrentTour(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    @ColumnInfo(name = "name")
    var name: String,
    @ColumnInfo(name = "country")
    var country: String,
    @ColumnInfo(name = "date")
    var date: String,
    @ColumnInfo(name = "price")
    var price: Int,
    @ColumnInfo(name = "flagUrl")
    var flagUrl: String,
    @ColumnInfo(name = "imageUrl")
    var imageUrl: String,
    @ColumnInfo(name = "description")
    var description: String = "",
    @ColumnInfo(name = "available_seats")
    var availableSeats: Int = 20,
    @ColumnInfo(name = "duration_days")
    var durationDays: Int = 7
)