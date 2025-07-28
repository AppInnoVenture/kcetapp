package com.kea.pyp

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.ktor.client.network.sockets.ConnectTimeoutException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UpdatesViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext

    private val _infoItemsLiveData = MutableLiveData<List<InfoItem>>()
    val infoItemsLiveData: LiveData<List<InfoItem>> = _infoItemsLiveData.distinctUntilChanged()

    val lastUpdatedLiveData = MutableLiveData<String?>()
    val uiEvent = SingleLiveEvent<InfoEvent>()
    val isLoading = MutableLiveData<Boolean>()

    private val connectivityManager by lazy {
        appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private var refreshJob: Job? = null

    companion object {

          // some private codes are removed, currently it shows only simplified version 
    

    fun loadData(isSwipeRefresh: Boolean = false) {
          // some private codes are removed, currently it shows only simplified version 
    
    }

    private suspend fun fetchDataFromSupabase(isSwipeRefresh: Boolean, currentTime: Long) {
          // some private codes are removed, currently it shows only simplified version 
    
    }

    private fun handleNoInternet(isSwipeRefresh: Boolean) {
        lastUpdatedLiveData.postValue(null)
        loadCachedData()
        if (isSwipeRefresh) {
            uiEvent.postValue(InfoEvent.NoInternetForRefresh("Please enable internet to refresh"))
        }
    }

    private fun handleError(isSwipeRefresh: Boolean, message: String) {
        loadCachedData()
        if (isSwipeRefresh) {
            uiEvent.postValue(InfoEvent.NoInternetForRefresh("Refresh failed: $message"))
        } else {
            uiEvent.postValue(InfoEvent.Error(message))
        }
    }

    private fun loadCachedData() {
        val items = Prefs.getInfoItems(appContext)
        _infoItemsLiveData.postValue(items)
        val lastUpdated = Prefs.getLastUpdated(appContext)
        if (lastUpdated != -1L) {
            lastUpdatedLiveData.postValue(formatLastUpdated(lastUpdated))
        }
    }

    private fun formatLastUpdated(time: Long): String {
        return "Last Updated: ${dateFormat.format(Date(time * 1000))}"
    }

    fun checkInternetAvailability(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

  // some private codes are removed, currently it shows only simplified version 
    

    override fun onCleared() {
        refreshJob?.cancel()
        super.onCleared()
    }
}