package com.example.biomapsapp.dummy

import com.example.biomapsapp.data.local.SafeZoneDao
import com.example.biomapsapp.model.SafeZoneEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FakeSafeZoneDao : SafeZoneDao {
    override fun getAllZones(): Flow<List<SafeZoneEntity>> {
        return flowOf(
            listOf(
                SafeZoneEntity(
                    name = "Zona Dummy 1",
                    latitude = -6.2,
                    longitude = 106.8,
                    addedBy = "Fariz",
                    timestamp = convertDateToTimestamp("2025-01-04")
                ),
                SafeZoneEntity(
                    name = "Zona Dummy 2",
                    latitude = -7.0,
                    longitude = 110.4,
                    addedBy = "Bagus",
                    timestamp = convertDateToTimestamp("2025-02-04")
                ),
                SafeZoneEntity(
                    name = "Zona Dummy 3",
                    latitude = -7.0,
                    longitude = 110.4,
                    addedBy = "Deden",
                    timestamp = convertDateToTimestamp("2025-03-04")
                ),
                SafeZoneEntity(
                    name = "Zona Dummy 4",
                    latitude = -7.0,
                    longitude = 110.4,
                    addedBy = "Sepyan",
                    timestamp = convertDateToTimestamp("2025-04-04")
                )
            )
        )
    }

    override suspend fun insert(zone: SafeZoneEntity) {}

    private fun convertDateToTimestamp(dateString: String): Long {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date: Date = format.parse(dateString) ?: Date()
        return date.time
    }
}