package com.example.travel

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TourDao {
    @Insert
    fun insertItem(item : CurrentTour)
    @Query("SELECT * FROM CurrentTours")
    fun getAllItems() : Flow<List<CurrentTour>>
    @Query("SELECT EXISTS(SELECT 1 FROM currenttours WHERE name = :sentname LIMIT 1)")
    fun tourExists(sentname : String): Boolean
    @Delete
    fun delete(item : CurrentTour)
    @Update
    fun updateTour(item : CurrentTour)
    @Query("SELECT * FROM CurrentTours WHERE LOWER(country) = LOWER(:countryName)")
    suspend fun getToursByCountry(countryName: String): List<CurrentTour>
    @Query("SELECT * FROM CurrentTours WHERE country = :countryName AND available_seats > 0")
    fun getAvailableToursByCountry(countryName: String): List<CurrentTour>
}