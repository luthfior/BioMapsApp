package com.example.biomapsapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.biomapsapp.data.local.SafeZoneDatabase
import com.example.biomapsapp.model.SafeZoneEntity
import com.example.biomapsapp.repository.SafeZoneRepository
import kotlinx.coroutines.launch

class SafeZoneViewModel(private val repository: SafeZoneRepository) : ViewModel()  {

    val zones: LiveData<List<SafeZoneEntity>> = repository.zones.asLiveData()

    fun addZone(zone: SafeZoneEntity) {
        viewModelScope.launch {
            repository.addZone(zone)
        }
    }
}