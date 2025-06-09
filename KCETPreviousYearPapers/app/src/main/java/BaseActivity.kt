package com.kea.pyp

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

open class BaseActivity : AppCompatActivity() {

    protected lateinit var basePreferences: SharedPreferences
    override fun attachBaseContext(newBase: Context) {
        basePreferences = newBase.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val language = basePreferences.getString("language", null) ?:
            if (newBase.resources.configuration.locales[0].language == "kn") "kn" else "en"
                .also { basePreferences.edit { putString("language", it) } }

        super.attachBaseContext(newBase.createConfigurationContext(Configuration().apply {
            setLocale(Locale(language))
            Locale.setDefault(Locale(language))
        }))
    }

    protected fun createScrollHandle(): DefaultScrollHandle {
        val scrollHandle = DefaultScrollHandle(this)
        if (basePreferences.getBoolean("dark_mode", false))
            scrollHandle.setTextColor(Color.WHITE)
        return scrollHandle
    }

    protected fun handleFileDeletion(filesToDelete: Set<File>?, message: String, rootView: View) {
        if (filesToDelete.isNullOrEmpty()) {
            Toast.makeText(this, R.string.noFiles, Toast.LENGTH_SHORT).show()
        } else {
            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.alertTitle))
                .setMessage(message)
                .setPositiveButton(getString(R.string.alertPositive)) { dialog, _ ->
                    dialog.dismiss()
                    Toast.makeText(this, R.string.alertCancel, Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(getString(R.string.alertNegative)) { dialog, _ ->
                    dialog.dismiss()
                    Snackbar.make(rootView, R.string.alertDelete, Snackbar.LENGTH_LONG)
                        .setAction(getString(R.string.snack_undo)) {
                            lifecycleScope.launch {
                                delay(300)
                                Snackbar.make(rootView, R.string.alertRestore, Snackbar.LENGTH_SHORT).show()
                            }
                        }
                        .addCallback(object : Snackbar.Callback() {
                            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                if (event != DISMISS_EVENT_ACTION) {
                                    filesToDelete.forEach { it.delete() }
                                }
                            }
                        })
                        .show()
                }
                .show()
        }
    }

    protected fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}