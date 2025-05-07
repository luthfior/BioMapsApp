package com.example.biomapsapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "safe_zones")
data class SafeZoneEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val addedBy: String,
    val timestamp: Long
)