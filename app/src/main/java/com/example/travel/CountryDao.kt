package com.example.travel

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CountryDao {

    @Upsert
    suspend fun insertCountry(country: Country)

    @Query("SELECT * FROM Countries ORDER BY popularity DESC")
    fun getAllCountries(): Flow<List<Country>>

    @Query("SELECT * FROM Countries WHERE country_name = :countryName")
    suspend fun getCountryByName(countryName: String): Country?

    @Query("DELETE FROM Countries")
    suspend fun deleteAllCountries()
}