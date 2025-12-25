package com.example.travel

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CurrentTour::class,
        BoughtTour::class,
        User::class,
        Order::class,
        Country::class
    ],
    version = 5,
    exportSchema = false
)
abstract class MainDB : RoomDatabase() {
    abstract fun getUserDao(): UserDao
    abstract fun getTourDao(): TourDao
    abstract fun getBoughtTourDao(): BoughtTourDao
    abstract fun getOrderDao(): OrderDao
    abstract fun getCountryDao(): CountryDao

    companion object {
        fun getDb(context: Context): MainDB {
            return Room.databaseBuilder(
                context.applicationContext,
                MainDB::class.java,
                "travel.db"
            )
                .fallbackToDestructiveMigration()
                // Для готовой базы раскомментируйте:
                // .createFromAsset("databases/prepopulated.db")
                .build()
        }
    }
}