package com.kea.pyp

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class Language(val name: String, val code: String, val displayName: String)
data class Item(val img: Int, val description: String, val extra: String)
data class YearItem(val descLan: String, val extra: String)

@Serializable
data class InfoItem(
    @SerialName("id") val id: Int,
    @SerialName("description") val description: String,
    @SerialName("type") val type: String,
    @SerialName("content") val content: String,
    @SerialName("pdffilename") val pdfFilename: String,
    @SerialName("offlineView") val offlineView: Boolean = false
)

@Serializable
data class InfoVersion(
    @SerialName("id") val id: Int = 1,
    @SerialName("version_number") val versionNumber: Int = 1,
    @SerialName("refresh_time") val refreshTime: Long = 300L
)

sealed class InfoEvent {
    data class Success(val message: String) : InfoEvent()
    data class Error(val message: String) : InfoEvent()
    data class NoInternetForRefresh(val message: String) : InfoEvent()
    data class NoInternetForItemClick(val item: InfoItem) : InfoEvent()
}