package com.example.biomapsapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.biomapsapp.model.SafeZoneEntity

@Database(entities = [SafeZoneEntity::class], version = 1)
abstract class SafeZoneDatabase : RoomDatabase() {
    abstract fun safeZoneDao(): SafeZoneDao

    companion object {
        @Volatile
        private var INSTANCE: SafeZoneDatabase? = null

        fun getDatabase(context: Context): SafeZoneDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SafeZoneDatabase::class.java,
                    "safe_zone_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}