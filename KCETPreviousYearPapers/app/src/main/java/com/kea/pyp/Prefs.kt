package com.kea.pyp

import android.content.Context
import androidx.core.content.edit
import kotlinx.serialization.json.Json

object Prefs {
    private const val PREFS_NAME = "app_preferences"
    private const val KEY_VERSION = "info_key_version"
    private const val KEY_REFRESH_TIME = "refresh_time_key"
    private const val KEY_LAST_UPDATED = "last_updated_key"
    private const val KEY_ITEMS = "items_key"

    private val json = Json { ignoreUnknownKeys = true } 

    private fun getPrefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveVersionInfo(context: Context, versionNumber: Int, refreshTime: Long) {
        getPrefs(context).edit {
            putInt(KEY_VERSION, versionNumber)
            putLong(KEY_REFRESH_TIME, refreshTime)
        }
    }

    fun getVersionNumber(context: Context) = getPrefs(context).getInt(KEY_VERSION, -1)

    fun getRefreshTime(context: Context) = getPrefs(context).getLong(KEY_REFRESH_TIME, -1)

    fun saveLastUpdated(context: Context, time: Long) {
        getPrefs(context).edit { putLong(KEY_LAST_UPDATED, time) }
    }

    fun getLastUpdated(context: Context) = getPrefs(context).getLong(KEY_LAST_UPDATED, -1L)

    fun saveInfoItems(context: Context, items: List<InfoItem>) {
        val jsonString = json.encodeToString(items)
        getPrefs(context).edit { putString(KEY_ITEMS, jsonString) }
    }

    fun getInfoItems(context: Context): List<InfoItem> {
        return try {
            val jsonString = getPrefs(context).getString(KEY_ITEMS, null)
            jsonString?.let {
                json.decodeFromString<List<InfoItem>>(it)
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}