package com.example.travel

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Countries")
data class Country(
    @PrimaryKey
    @ColumnInfo(name = "country_name")
    val name: String,
    @ColumnInfo(name = "flag_url")
    val flagUrl: String,
    @ColumnInfo(name = "capital")
    val capital: String,
    @ColumnInfo(name = "description")
    val description: String,
    @ColumnInfo(name = "popularity") // 1-5 звезд
    val popularity: Int = 3
)