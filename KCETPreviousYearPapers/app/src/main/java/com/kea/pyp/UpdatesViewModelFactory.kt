package com.kea.pyp

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class UpdatesViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UpdatesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UpdatesViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}