package com.example.biomapsapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.biomapsapp.model.SafeZoneEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SafeZoneDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(zone: SafeZoneEntity)

    @Query("SELECT * FROM safe_zones ORDER BY timestamp DESC")
    fun getAllZones(): Flow<List<SafeZoneEntity>>
}
