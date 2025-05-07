package com.example.biomapsapp.repository

import androidx.lifecycle.LiveData
import com.example.biomapsapp.data.local.SafeZoneDao
import com.example.biomapsapp.model.SafeZoneEntity
import kotlinx.coroutines.flow.Flow

class SafeZoneRepository(private val dao: SafeZoneDao) {

    val zones: Flow<List<SafeZoneEntity>> = dao.getAllZones()

    suspend fun addZone(zone: SafeZoneEntity) = dao.insert(zone)
}
