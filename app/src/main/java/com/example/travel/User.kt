package com.example.travel

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "Users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,

    @ColumnInfo(name = "login")
    var login: String,

    @ColumnInfo(name = "password")
    var password: String,

    @ColumnInfo(name = "email")
    var email: String,

    @ColumnInfo(name = "isAgent")
    var isAgent: Boolean,

    @ColumnInfo(name = "totalPurchases")
    var totalPurchases: Int = 0,

    @ColumnInfo(name = "discountLevel")
    var discountLevel: Int = 0,

    @ColumnInfo(name = "totalSpent")
    var totalSpent: Double = 0.0
) : Parcelable