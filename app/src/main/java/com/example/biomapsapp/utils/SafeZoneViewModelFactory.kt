package com.example.biomapsapp.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.biomapsapp.repository.SafeZoneRepository
import com.example.biomapsapp.viewmodel.SafeZoneViewModel

class SafeZoneViewModelFactory(private val repository: SafeZoneRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SafeZoneViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SafeZoneViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
