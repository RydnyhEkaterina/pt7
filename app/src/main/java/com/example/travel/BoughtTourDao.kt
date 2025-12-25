package com.example.travel

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BoughtTourDao {

    @Insert
    fun insertItem(item : BoughtTour)
    @Query("SELECT * FROM BoughtTours WHERE userLogin = :sentLogin")
    fun getAllUserItems(sentLogin : String) : Flow<List<BoughtTour>>
    @Query("SELECT EXISTS(SELECT 1 FROM BoughtTours WHERE name = :sentname AND userLogin = :sentLogin AND username = :sentUserName LIMIT 1)")
    fun tourBought(sentname: String, sentLogin: String, sentUserName : String): Boolean
    @Query("DELETE FROM BoughtTours WHERE userLogin = :sentLogin")
    fun deleteRemovedUserTours(sentLogin: String)
    @Query("DELETE FROM BoughtTours WHERE name = :sentName")
    fun deleteRemovedTour(sentName: String)
}