package com.example.travel

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert
    fun insertItem(item: User)

    @Query("SELECT * FROM Users")
    fun getAllItems(): Flow<List<User>>

    @Query("SELECT * FROM Users WHERE login = :sentLogin AND password = :sentPassword")
    fun getUserByCredentials(sentLogin: String, sentPassword: String): User?

    @Query("SELECT * FROM Users WHERE login = :sentLogin")
    fun getUserByLogin(sentLogin: String): User?

    @Query("SELECT * FROM Users WHERE email = :email")
    fun getUserByEmail(email: String): User?

    @Query("SELECT EXISTS(SELECT 1 FROM Users WHERE login = :sentLogin LIMIT 1)")
    fun loginExists(sentLogin: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM Users WHERE email = :email LIMIT 1)")
    fun emailExists(email: String): Boolean

    @Update
    fun updateUser(item: User)

    @Delete
    fun delete(item: User)

    @Query("UPDATE Users SET totalPurchases = :purchases, totalSpent = :spent, discountLevel = :discount WHERE login = :login")
    fun updatePurchaseStats(login: String, purchases: Int, spent: Double, discount: Int)

    @Query("UPDATE Users SET discountLevel = :discountLevel WHERE login = :login")
    fun updateDiscountLevel(login: String, discountLevel: Int)
}