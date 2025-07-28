package com.kea.pyp

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

sealed class PdfState {
    data object Idle : PdfState()
    data object Downloading : PdfState()
    data class Exists(val file: File) : PdfState()
    data class Success(val file: File) : PdfState()
    data class Error(val message: String) : PdfState()
}

class PdfViewerViewModel : ViewModel() {
    private val _pdfState = MutableLiveData<PdfState>(PdfState.Idle)
    val pdfState: LiveData<PdfState> get() = _pdfState

    private val _progress = MutableLiveData(0)
    val progress: LiveData<Int> get() = _progress

    var downloadJob: Job? = null

    fun downloadPdf(pdfUrl: String, pdfFile: File, sharedPreferences: SharedPreferences) {
        if (_pdfState.value is PdfState.Downloading) return

        if (pdfFile.exists()) {
            _pdfState.value = PdfState.Exists(pdfFile)
            return
        }

        _pdfState.value = PdfState.Downloading
        
        downloadJob = viewModelScope.launch(Dispatchers.IO) {
            var inputStream: InputStream? = null
            var fileOutputStream: FileOutputStream? = null
            var connection: HttpURLConnection? = null
            try {
                connection = URL(pdfUrl).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                if (connection.responseCode in 200..299) {
                    val totalFileSize = connection.contentLength.takeIf { it > 0 } ?: -1
                    inputStream = BufferedInputStream(connection.inputStream, 8192)
                    fileOutputStream = FileOutputStream(pdfFile)

                    val buffer = ByteArray(8192)
                    var totalBytesRead = 0
                    var lastProgressUpdate = 0

                    while (isActive) {
                        val bytesRead = inputStream.read(buffer).takeIf { it != -1 } ?: break
                        fileOutputStream.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead
                        if (totalFileSize > 0) {
                            val currentProgress = totalBytesRead * 100 / totalFileSize
                            if (currentProgress - lastProgressUpdate >= 2) {
                                lastProgressUpdate = currentProgress
                                _progress.postValue(currentProgress)
                            }
                        }
                    }
                    fileOutputStream.flush()
                    if (isActive)
                        _pdfState.postValue(PdfState.Success(pdfFile))
                    else
                        if (pdfFile.exists()) pdfFile.delete()
                } else
                    _pdfState.postValue(PdfState.Error("Failed to download, Please check your Internet Connection"))
                
            } catch (e: Exception) {
                e.printStackTrace()
                delay(300)
                if (pdfFile.exists()) pdfFile.delete()
                _pdfState.postValue(PdfState.Error("Error Occurred. Please report in our Telegram Group"))
            } finally {
                _progress.postValue(0)
                fileOutputStream?.close()
                inputStream?.close()
                connection?.disconnect()
                if (isActive) downloadJob = null
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        downloadJob?.cancel()
    }
}