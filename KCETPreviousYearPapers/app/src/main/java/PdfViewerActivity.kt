package com.kea.pyp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.Surface
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil.setContentView
import androidx.lifecycle.lifecycleScope
import androidx.transition.AutoTransition
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.github.barteksc.pdfviewer.util.Constants
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.kea.pyp.databinding.ActivityPdfViewerBinding
import com.kea.pyp.databinding.DialogZoomToBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.abs
import kotlin.math.sign

class PdfViewerActivity : BaseActivity() {

    private lateinit var pdfView: PDFView
    private var pdfFileName: String? = null
    private var ansPdf: String? = null
    private val sharedPreferences: SharedPreferences by lazy { getSharedPreferences("pdfPrefs", MODE_PRIVATE) }
    private var headPdf: String? = null
    private var curPdf: String? = null
    private var headansPdf: String? = null
    private var prefix: String? = null
    private var isDownloading = false
    private var isNight = false
    private var splitM = false
    private var belowL = false
    private var pdfOpened = false
    private var anspdfOpened = false
    private lateinit var binding: ActivityPdfViewerBinding
    private val ansPage: Int by lazy {
        when (prefix) {
            "phy" -> 0
            "che" -> 1
            "maths" -> 2
            else -> 3
        }
    }
    private var s = 0
    private var e = 2
    private val headYear by lazy { intent.getIntExtra("year", 2025) }
    private val pdfFile: File by lazy { File(filesDir, pdfFileName) }
    private val viewModel: PdfViewerViewModel by viewModels()
    private var isFullScreen = false
    private var isVolumeButtonsEnabled = false
    private var isSwipeHorizontal = false
    private var isRotationLocked = false
    private val imm by lazy { getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }
    private var isAutoScrolling = false
    private var belowLayout = false
    private var shouldHide = false
    private var scrollBy = -10.0
    private val scrollUnit = 10.0
    private var totalPgPdf = 1
    private var spacing = 0
    private var slideInterval = 3
    private var isSlideshow = false
    private var curSlideshow = false
    private var curAutoScrolling = false
    private var diff: String? = null
    private var slideShowJob: Job? = null
    private var autoScrollJob: Job? = null
    private var scrollDx: Float = 0F
    private var scrollDy: Float = 1F
    private var isIncreasing: Boolean? = null
    private var isPageSnap = false
    private var isPageFling = false
    private var screenshotJob: Job? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = setContentView(this, R.layout.activity_pdf_viewer)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        isNight = basePreferences.getBoolean("pdfNight", basePreferences.getBoolean("dark_mode", false))
        val snackBarTheme = ContextThemeWrapper(this, if (isNight) R.style.SnackbarNightTheme else R.style.SnackbarLightTheme)
        slideInterval = basePreferences.getInt("slideInterval", 3)
        val headDesc = intent.getStringExtra("desc").toString()
        prefix = intent.getStringExtra("prefix").toString()
        pdfView = binding.pdfView
        
        diff = intent.getStringExtra("diff").toString()
        Constants.Pinch.MINIMUM_ZOOM = 0.75f

        setSupportActionBar(binding.descPdf)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState != null) {
            belowL = savedInstanceState.getBoolean("belowL", false)
            requestedOrientation = savedInstanceState.getInt("orientation", -1)
            isVolumeButtonsEnabled = savedInstanceState.getBoolean("isVolumeButtonsEnabled", false)
            isRotationLocked = savedInstanceState.getBoolean("isRotationLocked", false)
            isSlideshow = savedInstanceState.getBoolean("isSlideshow", false)
            isSwipeHorizontal = savedInstanceState.getBoolean("isSwipeHorizontal", false)
            spacing = savedInstanceState.getInt("spacing", 0)
            shouldHide = savedInstanceState.getBoolean("shouldHide", false)
            isPageFling = savedInstanceState.getBoolean("isPageFling", false)
            isPageSnap = savedInstanceState.getBoolean("isPageSnap", false)
            if (savedInstanceState.getBoolean("isSlideshow", false)) curSlideshow = true
            if (savedInstanceState.getBoolean("isFullScreen", false)) toggleFullScreen()
            if (savedInstanceState.getBoolean("isAutoScrolling", false)) curAutoScrolling = true
            if (savedInstanceState.getBoolean("isAutoVisible", false)) openAutoScrollLayout()
        }

        when (diff[0]) {
            'n' -> {
                headPdf = if (("Practical" in headDesc) || ("Syllabus" in headDesc))
                    headDesc
                else
                    "$headDesc ${getString(R.string.tab_notes)}"
                supportActionBar?.title = headPdf
                if (savedInstanceState == null) {
                    Snackbar.make(snackBarTheme, binding.root, headPdf, Snackbar.LENGTH_SHORT).show()
                    spacing = 10
                }

                try {
                    pdfFileName = prefix.substring(0, 3) + ".pdf"
                    
                    openPdf()
                } catch (e: Exception) {
                    Toast.makeText(this, R.string.errr, Toast.LENGTH_SHORT).show()
                }
            }

            'u' -> {
                pdfFileName = prefix
                headPdf = headDesc
                supportActionBar?.title = headPdf
                if (savedInstanceState == null)
                    Snackbar.make(snackBarTheme, binding.root, headPdf, Snackbar.LENGTH_SHORT).show()
                openPdf()
            }

            'c' -> {
                pdfFileName = prefix
                headPdf = "$headDesc ${getString(R.string.tab_cutoff)}"
                supportActionBar?.title = headPdf
                if (savedInstanceState == null)
                    Snackbar.make(snackBarTheme, binding.root, headPdf, Snackbar.LENGTH_SHORT).show()
                openPdf()
            }

            else -> {
                spacing = savedInstanceState?.getInt("spacing", 0) ?: 10
                pdfFileName = "$prefix$headYear.pdf"

                if ("kan" in prefix) {
                    headPdf = "$headDesc $headYear ${getString(R.string.btn_qp)}"
                } else if (headYear > 2015) {
                    headPdf = "$headDesc $headYear ${getString(R.string.head_sol)}"
                } else {
                    belowLayout = true
                    if (!curSlideshow && !binding.autoScrollLayout.isVisible) openBelowLayout()
                    ansPdf = "ans_$headYear.pdf"
                    headPdf = "$headDesc $headYear ${getString(R.string.btn_qp)}"
                    headansPdf = "$headDesc $headYear ${getString(R.string.btn_ans)}"
                }
                supportActionBar?.title = headPdf
                if (headYear > 2019) {
                    diff = "offline"
                    openPdf()
                    if (savedInstanceState == null)
                        Snackbar.make(snackBarTheme, binding.root, headPdf, Snackbar.LENGTH_SHORT).show()
                } else {
                    viewModel.downloadPdf("$REDACTED$pdfFileName", pdfFile, basePreferences)

                    viewModel.pdfState.observe(this) { state ->
                        when (state) {
                            is PdfState.Idle ->
                                isDownloading = false

                            is PdfState.Exists -> {
                                isDownloading = false
                                if (savedInstanceState != null) {
                                    if (shouldHide)
                                        openAnsPdf()
                                    else
                                        openPdf()
                                } else {
                                    openPdf()
                                    Snackbar.make(snackBarTheme, binding.root, headPdf, Snackbar.LENGTH_SHORT).show()
                                }
                            }

                            is PdfState.Downloading -> {
                                isDownloading = true
                                binding.progressBar.visibility = View.VISIBLE
                                if (savedInstanceState == null)
                                    Toast.makeText(this, getString(R.string.dwding), Toast.LENGTH_SHORT).show()
                            }

                            is PdfState.Success -> {
                                isDownloading = false
                                binding.splbtn.isEnabled = true
                                binding.progressBar.visibility = View.GONE
                                if (state.file.length() > 0) {
                                    invalidateOptionsMenu()
                                    openPdf()
                                    basePreferences.edit { putBoolean("dwd", false) }
                                    Toast.makeText(this@PdfViewerActivity, R.string.dwdComp, Toast.LENGTH_SHORT).show()
                                } else {
                                    state.file.delete()
                                    Snackbar.make(snackBarTheme, binding.root, "Server Error", Snackbar.LENGTH_LONG)
                                        .setAction(R.string.snack_retry) {
                                            if (isInternetAvailable())
                                                viewModel.downloadPdf("$REDACTED$pdfFileName", pdfFile, basePreferences)
                                            else
                                                lifecycleScope.launch {
                                                    delay(300)
                                                    Toast.makeText(this@PdfViewerActivity, R.string.noNet,
                                                        Toast.LENGTH_SHORT).show() }
                                        }.show()
                                }
                            }

                            is PdfState.Error -> {
                                isDownloading = false
                                binding.splbtn.isEnabled = false
                                binding.progressBar.visibility = View.GONE
                                Snackbar.make(snackBarTheme, binding.root, state.message, Snackbar.LENGTH_LONG)
                                    .setAction(R.string.snack_retry) {
                                        if (isInternetAvailable())
                                            viewModel.downloadPdf("$REDACTED$pdfFileName", pdfFile, basePreferences)
                                        else
                                            lifecycleScope.launch {
                                                delay(300)
                                                Toast.makeText(this@PdfViewerActivity, R.string.noNet, Toast.LENGTH_SHORT).show()
                                            }
                                    }.show()
                            }
                        }
                    }

                    viewModel.progress.observe(this) { progress ->
                        binding.progressBar.progress = progress
                    }
                }
            }
        }

        setAutoScrollButtons()

        binding.ansbtn.setOnClickListener {
            if (!isDownloading)
                if (!shouldHide) {
                    openAnsPdf()
                } else
                    Toast.makeText(this, R.string.apdf, Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(this, R.string.waitt, Toast.LENGTH_SHORT).show()
        }

        binding.qpbtn.setOnClickListener {
            if (!isDownloading)
                if (shouldHide) {
                    supportActionBar?.title = headPdf
                    openPdf()
                    curPdf = pdfFileName
                } else
                    Toast.makeText(this, R.string.qpdf, Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(this, R.string.waitt, Toast.LENGTH_SHORT).show()
        }

        binding.splbtn.setOnClickListener {
            if (!isDownloading) {
                splitM = true
                val intent = Intent(this, SplitPdfActivity::class.java)
                intent.putExtra("pdf1", pdfFileName)
                intent.putExtra("pdf2", ansPdf)
                intent.putExtra("ansPage", ansPage)
                intent.putExtra("isNight", isNight)
                startActivity(intent)
            } else
                Toast.makeText(this, R.string.waitt, Toast.LENGTH_SHORT).show()
        }

        binding.btndn.setOnClickListener {
            belowL = true
            binding.btndn.animate().rotation(180f).setDuration(400).withEndAction {
                    TransitionManager.beginDelayedTransition(binding.root as RelativeLayout,
                        AutoTransition().setDuration(350))
                    binding.below.visibility = View.GONE
                }.start()
            binding.btnup.rotation = 0f
        }

        binding.btnup.setOnClickListener {
            belowL = false
            binding.btnup.animate().rotation(-180f).setDuration(500).start()
            binding.btndn.rotation = 0f
            TransitionManager.beginDelayedTransition(binding.root as RelativeLayout,
                AutoTransition().setDuration(500))
            binding.below.visibility = View.VISIBLE
        }

        onBackPressedDispatcher.addCallback(this) {
        if (viewModel.pdfState.value is PdfState.Downloading) {
            viewModel.downloadJob?.cancel()
            CoroutineScope(Dispatchers.IO).launch {
                delay(500)
                if (pdfFile.exists()) pdfFile.delete()
            }
         } else if (isFullScreen) {
            toggleFullScreen()
            return@addCallback
        }
        finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.apply {
            putBoolean("shouldHide", shouldHide)
            putBoolean("isFullScreen", isFullScreen)
            putBoolean("belowL", belowL)
            putBoolean("isRotationLocked", isRotationLocked)
            putBoolean("isVolumeButtonsEnabled", isVolumeButtonsEnabled)
            putBoolean("isSwipeHorizontal", isSwipeHorizontal)
            putBoolean("isSlideshow", isSlideshow)
            putInt("orientation", requestedOrientation)
            putInt("spacing", spacing)
            putBoolean("isAutoScrolling", isAutoScrolling)
            putBoolean("isAutoVisible", binding.autoScrollLayout.isVisible)
            putBoolean("isPageFling", isPageFling)
            putBoolean("isPageSnap", isPageSnap)}
    }

    override fun onResume() {
        super.onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (splitM) {
            pdfView.jumpTo(sharedPreferences.getInt(pdfFileName, 0))
            splitM = false
        }
    }

    override fun onPause() {
        super.onPause()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (!isDownloading)
            sharedPreferences.edit { putInt(if (!shouldHide) curPdf else pdfFileName, pdfView.currentPage) }
    }

    private fun openPdf() {
        
        if (!pdfOpened) {
            try {
                when (diff[0]) {
                    'n' -> {
                        pdfView.fromAsset(pdfFilename).nightMode(isNight).fitEachPage(true)
                            .password(REDACTED)
                            .swipeHorizontal(isSwipeHorizontal)
                            .spacing(spacing).scrollHandle(DefaultScrollHandle(this))
                            .defaultPage(sharedPreferences.getInt(prefix, 0)).onLoad { nbPages ->
                                totalPgPdf = nbPages
                                if (curSlideshow) startSlideshow(slideInterval.toDouble())
                                else if (curAutoScrolling) startAutoScroll()
                            }.pageSnap(isPageSnap).pageFling(isPageFling).load()
                        curPdf = prefix
                    }

                    'p' -> {
                        pdfView.fromFile(pdfFile).nightMode(isNight).fitEachPage(true).spacing(spacing)
                            .scrollHandle(DefaultScrollHandle(this)).password(REDACTED)
                            .swipeHorizontal(isSwipeHorizontal)
                            .onError { throwable ->
                                if (throwable is IOException) { pdfFile.delete()
                                    Snackbar.make(binding.root, R.string.errdwd, Snackbar.LENGTH_LONG)
                                        .setAction(R.string.snack_yes) {
                                            if (isInternetAvailable())
                                                viewModel.downloadPdf("$REDACTED$pdfFileName", pdfFile, basePreferences)
                                            else
                                                Toast.makeText(this, R.string.noNet, Toast.LENGTH_SHORT).show()
                                        }.show()
                                } else
                                    Toast.makeText(this, R.string.errr, Toast.LENGTH_SHORT).show()
                            }.defaultPage(sharedPreferences.getInt(pdfFileName, 0))
                            .onLoad { nbPages ->
                                totalPgPdf = nbPages
                                if (curSlideshow) startSlideshow(slideInterval.toDouble())
                                else if (curAutoScrolling) startAutoScroll()
                            }.pageSnap(isPageSnap).pageFling(isPageFling).load()
                        curPdf = pdfFileName
                    }

                    else -> {
                        pdfView.fromAsset(pdfFilename).nightMode(isNight).spacing(spacing)
                            .defaultPage(sharedPreferences.getInt(pdfFileName, 0))
                            .swipeHorizontal(isSwipeHorizontal).pageSnap(isPageSnap).pageFling(isPageFling)
                            .scrollHandle(DefaultScrollHandle(this)).onLoad { nbPages ->
                                totalPgPdf = nbPages
                                if (diff[0] == 'c') {
                                    pdfView.zoomTo(4f); pdfView.midZoom = 6f; pdfView.maxZoom = 8f
                                }
                                if (curSlideshow) startSlideshow(slideInterval.toDouble())
                                else if (curAutoScrolling) startAutoScroll()
                            }.password(REDACTED).fitEachPage(true).load()
                        curPdf = pdfFileName
                    }
                }
                pdfView.setBackgroundColor(if (spacing != 0) "#2C1D20".toColorInt() else if (isNight) Color.BLACK else Color.WHITE)
                binding.pdfContainer.setBackgroundColor(if (isNight) Color.BLACK else Color.WHITE)
                pdfOpened = true
            } catch (e: Exception) {
                Toast.makeText(this, R.string.errr, Toast.LENGTH_LONG).show()
            }
        }
        
        shouldHide = false
        // some private codes are removed, currently it shows only simplified version 
    
    }

    private fun openAnsPdf() {
        supportActionBar?.title = headansPdf
        
            if (!anspdfOpened) {
                try {
                    pdfView.fromAsset(ansPdf).nightMode(isNight).pages(ansPage)
                        .password(REDACTED).load()
                    anspdfOpened = true
                    anspdfView.setBackgroundColor(if (isNight) Color.BLACK else Color.WHITE)
                } catch (e: Exception) {
                    Toast.makeText(this, R.string.errr, Toast.LENGTH_SHORT).show()
                }
            }
            
            shouldHide = true
                   
    // some private codes are removed, currently it shows only simplified version 
    }

    override fun onSupportNavigateUp(): Boolean {
        if (viewModel.pdfState.value is PdfState.Downloading) {
            viewModel.downloadJob?.cancel()
            CoroutineScope(Dispatchers.IO).launch {
                delay(500)
                if (pdfFile.exists()) pdfFile.delete()
            }
        }
        finish()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_pdf_viewer, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.let {
            if (isDownloading) {
                it.clear()
                return true
            }

            if (belowLayout) {
                it.setGroupVisible(R.id.group_navigation, !shouldHide)
                it.setGroupVisible(R.id.group_interaction, !shouldHide)
            }

            it.findItem(R.id.menu_toggle_night_mode)?.title =
                if (isNight) getString(R.string.menu_toggle_night_mode_to_light) else getString(R.string.menu_toggle_night_mode_to_dark)
            it.findItem(R.id.menu_toggle_rotation_lock)?.title =
                if (isRotationLocked) getString(R.string.menu_toggle_rotation_lock_unlock) else getString(
                    R.string.menu_toggle_rotation_lock_lock
                )

            if (!shouldHide) {
                it.findItem(R.id.menu_toggle_volume_buttons)?.title =
                    if (isVolumeButtonsEnabled) getString(R.string.menu_toggle_volume_buttons_disable) else getString(
                        R.string.menu_toggle_volume_buttons_enable
                    )
                it.findItem(R.id.menu_swipe_horizontal)?.title =
                    if (isSwipeHorizontal) getString(R.string.menu_swipe_horizontal_to_vertical) else getString(
                        R.string.menu_swipe_horizontal_to_horizontal
                    )
                it.findItem(R.id.menu_toggle_spacing)?.title =
                    if (spacing == 0) getString(R.string.menu_toggle_spacing_add) else getString(R.string.menu_toggle_spacing_remove)
                it.findItem(R.id.menu_toggle_slideshow)?.title =
                    if (isSlideshow) getString(R.string.menu_toggle_slideshow_stop) else getString(R.string.menu_toggle_slideshow_start)
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_zoom_to -> {
                showZoomToDialog()
                true
            }

            R.id.menu_toggle_fullscreen -> {
                toggleFullScreen()
                true
            }

            R.id.menu_toggle_rotation_lock -> {
                isRotationLocked = !isRotationLocked
                if (isRotationLocked) {
                    Toast.makeText(this, getString(R.string.toast_locked_screen_rotation), Toast.LENGTH_SHORT).show()
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
                } else {
                    Toast.makeText(this, getString(R.string.toast_unlocked_screen_rotation), Toast.LENGTH_SHORT).show()
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
                }
                true
            }

            R.id.menu_toggle_volume_buttons -> {
                isVolumeButtonsEnabled = !isVolumeButtonsEnabled
                Toast.makeText(this, getString(R.string.toast_volume_button_scroll_enabled_state,
                    if (isVolumeButtonsEnabled) getString(R.string.state_enabled) else getString(R.string.state_disabled)),
                    Toast.LENGTH_SHORT).show()
                true
            }

            R.id.menu_go_to_last_page -> {
                val lastPage = totalPgPdf - 1
                if (pdfView.currentPage == lastPage) {
                    Toast.makeText(this, getString(R.string.toast_already_on_last_page), Toast.LENGTH_SHORT).show()
                } else {
                    pdfView.jumpTo(lastPage)
                    Toast.makeText(this, getString(R.string.toast_navigated_to_last_page), Toast.LENGTH_SHORT).show()
                }
                true
            }

            R.id.menu_go_to_first_page -> {
                if (pdfView.currentPage == 0) {
                    Toast.makeText(this, getString(R.string.toast_already_on_first_page), Toast.LENGTH_SHORT).show()
                } else {
                    pdfView.jumpTo(0)
                    Toast.makeText(this, getString(R.string.toast_navigated_to_first_page), Toast.LENGTH_SHORT).show()
                }
                true
            }

            R.id.menu_rotate_clockwise -> {
                rotateClockwise()
                true
            }

            R.id.menu_rotate_anticlockwise -> {
                rotateAntiClockwise()
                true
            }

            R.id.menu_jump_to_page -> {
                showJumpToPageDialog()
                true
            }

            R.id.menu_toggle_night_mode -> {
                isNight = !isNight
                basePreferences.edit { putBoolean("pdfNight", isNight) }
                if (isNight) {
                    if (spacing == 0) {
                        pdfView.setBackgroundColor(Color.BLACK)
                        if (anspdfOpened) pdfView.setBackgroundColor(Color.BLACK)
                    }
                    
                } else {
                    if (spacing == 0) {
                         pdfView.setBackgroundColor(Color.WHITE)
                        if (anspdfOpened) pdfView.setBackgroundColor(Color.WHITE)
                    }
                    
                }
                pdfView.setNightMode(isNight)
                
                // some private codes are removed, currently it shows only simplified version 
    
                
                true
            }

            R.id.menu_share_screenshot -> {
                shareScreenshot()
                true
            }

            R.id.menu_page_fling -> {
                setPageFling()
                true
            }

            R.id.menu_page_snap -> {
                setPageSnap()
                true
            }

            R.id.menu_exit -> {
                Toast.makeText(this, R.string.exitToast, Toast.LENGTH_SHORT).show()
                finishAffinity()
                true
            }

            R.id.menu_swipe_horizontal -> {
                isSwipeHorizontal = !isSwipeHorizontal
                
                // some private codes are removed, currently it shows only simplified version 
    
                sharedPreferences.edit { putInt(if (!shouldHide) curPdf else pdfFileName, pdfView.currentPage)}

                Toast.makeText(this, getString(R.string.toast_swipe_direction_enabled,
                    if (isSwipeHorizontal) getString(R.string.direction_horizontal) else getString(R.string.direction_vertical)),
                    Toast.LENGTH_SHORT).show()

                
                true
            }

            R.id.menu_toggle_auto_scroll -> {
                if (binding.autoScrollLayout.isVisible) {
                    stopAutoScroll()
                } else {
                    TransitionManager.beginDelayedTransition(binding.root as RelativeLayout,
                        if (belowLayout) AutoTransition().setDuration(700) else Slide().setDuration(600))

                    if (isSlideshow) stopSlideshow(false)
                    else {
                        if (binding.below.isVisible) binding.btnup.rotation = -180f
                        closeBelowLayout()
                    }

                    openAutoScrollLayout()
                }
                true
            }

            R.id.menu_toggle_slideshow -> {
                if (isSlideshow) {
                    stopSlideshow(true)
                    Toast.makeText(this, getString(R.string.toast_slideshow_stopped), Toast.LENGTH_SHORT).show()
                } else {
                    if (binding.autoScrollLayout.isVisible) {
                        stopAutoScroll()
                    }
                    showSlideshowDialog()
                }
                true
            }

            R.id.menu_toggle_spacing -> {
                if (spacing == 0) {
                    spacing = 10
                    Toast.makeText(this, R.string.toast_added_spacing, Toast.LENGTH_SHORT).show()
                } else {
                    spacing = 0
                    Toast.makeText(this, R.string.toast_removed_spacing, Toast.LENGTH_SHORT).show()
                }
                // some private codes are removed, currently it shows only simplified version 
    
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showJumpToPageDialog() {
        val dialogBinding = DialogZoomToBinding.inflate(layoutInflater)
        val currentPage = pdfView.currentPage
        dialogBinding.percentSymbol.text = "/${totalPgPdf}"
        dialogBinding.currentZoom = (currentPage + 1).toString()
        val editPageNumber = dialogBinding.editZoomLevel
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setTitle(getString(R.string.dialog_title_enter_page_number))
            .setPositiveButton(getString(R.string.dialog_button_ok)) { dialogInterface, _ ->
                val input = editPageNumber.text.toString().trim()
                if (input.isEmpty()) {
                    Toast.makeText(this, getString(R.string.toast_page_number_field_empty), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                try {
                    val page = input.toInt()
                    if (page in 1..totalPgPdf) {
                        if (page - 1 == currentPage) {
                            Toast.makeText(this, getString(R.string.toast_already_viewing_page, page), Toast.LENGTH_SHORT).show()
                        } else {
                            pdfView.jumpTo(page - 1)
                            Toast.makeText(this, getString(R.string.toast_navigated_to_page_number, page), Toast.LENGTH_SHORT).show()
                        }
                        dialogInterface.dismiss()
                    } else {
                        Toast.makeText(this, getString(R.string.toast_page_number_must_be_between, totalPgPdf), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, getString(R.string.toast_please_enter_valid_number), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.dialog_button_cancel), null)
            .create()

        dialog.show()
        showKeyboardForDialog(dialog, editPageNumber)
    }

    private fun showZoomToDialog(pdfView: PDFView) {
        val dialogBinding = DialogZoomToBinding.inflate(layoutInflater)
        val currentZoom = (pdfView.zoom * 100).toInt()
        dialogBinding.currentZoom = currentZoom.toString()
        val editZoomLevel = dialogBinding.editZoomLevel
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setTitle(getString(R.string.dialog_title_enter_zoom_level))
            .setPositiveButton(getString(R.string.dialog_button_ok)) { dialogInterface, _ ->
                val input = editZoomLevel.text.toString().trim()
                if (input.isEmpty()) {
                    Toast.makeText(this, getString(R.string.toast_zoom_level_field_empty), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                try {
                    val zoom = input.toInt()
                    if (zoom in 50..1000) {
                        if (zoom == currentZoom) {
                            Toast.makeText(this, getString(R.string.toast_zoom_level_already_at, zoom), Toast.LENGTH_SHORT).show()
                        } else {
                            pdfView.zoomWithAnimation(zoom / 100f)
                            Toast.makeText(this, getString(R.string.toast_zoomed_to_percent, zoom), Toast.LENGTH_SHORT).show()
                        }
                        dialogInterface.dismiss()
                    } else {
                        Toast.makeText(this, getString(R.string.toast_zoom_level_must_be_between), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, getString(R.string.toast_please_enter_valid_number), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.dialog_button_cancel), null)
            .create()

        dialog.show()
        showKeyboardForDialog(dialog, editZoomLevel)
    }

    private fun showSlideshowDialog() {
        val dialogBinding = DialogZoomToBinding.inflate(layoutInflater)
        dialogBinding.currentZoom = slideInterval.toString()
        dialogBinding.percentSymbol.visibility = View.GONE
        val editInterval = dialogBinding.editZoomLevel
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setTitle(getString(R.string.dialog_title_enter_slideshow_duration))
            .setPositiveButton(getString(R.string.dialog_button_start)) { dialogInterface, _ ->
                val input = editInterval.text.toString().trim()
                try {
                    val interval = input.toDouble()
                    if (interval > 0) {
                        slideInterval = interval.toInt() 
                        basePreferences.edit { putInt("slideInterval", slideInterval) }
                        Toast.makeText(this, getString(R.string.toast_slideshow_started), Toast.LENGTH_SHORT).show()
                        startSlideshow(interval)
                        dialogInterface.dismiss()
                    } else {
                        Toast.makeText(this, getString(R.string.toast_duration_must_be_greater_than_zero), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: NumberFormatException) {
                    Toast.makeText(this, getString(R.string.toast_please_enter_valid_number), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.dialog_button_cancel), null)
            .create()
        dialog.show()
        showKeyboardForDialog(dialog, editInterval)
    }

    private fun showKeyboardForDialog(dialog: AlertDialog, editText: TextInputEditText) {
        val keyboardJob = lifecycleScope.launch {
            delay(300)
            if (!dialog.isShowing || isFinishing) return@launch
            editText.apply {
                isFocusable = true
                isFocusableInTouchMode = true
                requestFocus()
                selectAll()
                imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        dialog.setOnDismissListener {
            keyboardJob.cancel()
        }
    }

    private fun startSlideshow(interval: Double) {
            
    // Dummy Placeholder
    // In main app it does slideshow with the specified interval, simplified here to keep it private; happy to demo it in an interview! 
    // Unit function, returns nothing 
    }

    private fun stopSlideshow(shouldOpen: Boolean) {
        isSlideshow = false
        slideShowJob?.cancel()
        slideShowJob = null
        if (shouldOpen && belowLayout) {
            TransitionManager.beginDelayedTransition(binding.root as RelativeLayout, AutoTransition().setDuration(500))
            openBelowLayout()
        }
    }

    @Suppress("DEPRECATION")
    private fun toggleFullScreen() {
       TransitionManager.beginDelayedTransition(binding.root as RelativeLayout, AutoTransition().setDuration(500))
        if (isFullScreen) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            } else {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                window.statusBarColor = Color.TRANSPARENT
                window.navigationBarColor = Color.TRANSPARENT
            }
            binding.descPdf.isVisible = true
            isFullScreen = false
            ViewCompat.requestApplyInsets(binding.root)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.let {
                    it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                    it.systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                window.decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                                View.SYSTEM_UI_FLAG_FULLSCREEN or
                                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        )
                window.statusBarColor = Color.TRANSPARENT
                window.navigationBarColor = Color.TRANSPARENT
            }
            binding.descPdf.isVisible = false
            isFullScreen = true
        }
        binding.root.requestLayout()
    }

    private fun shareScreenshot() {
        if (screenshotJob?.isActive == true) return

        Toast.makeText(this, R.string.toast_preparing_screenshot, Toast.LENGTH_SHORT).show()

        screenshotJob = lifecycleScope.launch(Dispatchers.Main) {
            try {
                val rootView = window.decorView.rootView
                val density = resources.displayMetrics.density

                val uri = withContext(Dispatchers.IO) {
                    val bitmap = createBitmap(
                        (rootView.width * density).toInt(),
                        (rootView.height * density).toInt(),
                        Bitmap.Config.ARGB_8888
                    )
                    Canvas(bitmap).apply { scale(density, density) }.let { rootView.draw(it) }

                    val file = File(cacheDir, "Screenshot.png")
                    file.delete()
                    FileOutputStream(file).use { output ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
                    }
                    bitmap.recycle()

                    FileProvider.getUriForFile(this@PdfViewerActivity, "${packageName}.fileprovider", file)
                }

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT,
                        "Screenshot from KCET Previous Question Papers Android App: https://play.google.com/store/apps/details?id=com.kcet.pyp")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(shareIntent, "Share Screenshot"))
            } catch (e: Exception) {
                if (isActive)
                    Toast.makeText(this@PdfViewerActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                screenshotJob = null
            }
        }
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (isVolumeButtonsEnabled && !shouldHide) {
            val currentPage = pdfView.currentPage
            when (keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    if (currentPage > 0) {
                        pdfView.jumpTo(currentPage - 1, true)
                    } else {
                        Toast.makeText(this, getString(R.string.toast_already_on_first_page), Toast.LENGTH_SHORT).show()
                    }
                    return true
                }

                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    if (currentPage < totalPgPdf - 1) {
                        pdfView.jumpTo(currentPage + 1, true)
                    } else {
                        Toast.makeText(this, getString(R.string.toast_already_on_last_page), Toast.LENGTH_SHORT).show()
                    }
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun setPageFling() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.dialog_title_enable_fast_page_swipe))
            .setMessage(getString(R.string.dialog_message_fast_page_swipe,
                if (isSwipeHorizontal) getString(R.string.direction_horizontally) else getString(R.string.direction_up_or_down)))
            .setPositiveButton(getString(R.string.dialog_button_enable)) { _, _ ->
                pdfView.setPageFling(true)
                isPageFling = true
                Toast.makeText(this, getString(R.string.toast_page_swipe_enabled), Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(getString(R.string.dialog_button_disable)) { _, _ ->
                pdfView.setPageFling(false)
                isPageFling = false
                Toast.makeText(this, getString(R.string.toast_page_swipe_disabled), Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun setPageSnap() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.dialog_title_enable_page_alignment))
            .setMessage(getString(R.string.dialog_message_page_alignment,
                if (isSwipeHorizontal) getString(R.string.direction_horizontally) else getString(R.string.direction_up_or_down)))
            .setPositiveButton(getString(R.string.dialog_button_enable)) { _, _ ->
                pdfView.isPageSnap = true
                isPageSnap = true
                Toast.makeText(this, getString(R.string.toast_page_alignment_enabled), Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(getString(R.string.dialog_button_disable)) { _, _ ->
                pdfView.isPageSnap = false
                isPageSnap = false
                Toast.makeText(this, getString(R.string.toast_page_alignment_disabled), Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun rotateAntiClockwise() {
               
    // Dummy Placeholder
    // In main app it determines current orientation and changes the orientation by 90° , simplified here to keep it private; happy to demo it in an interview! 
   
        isRotationLocked = true
        Toast.makeText(this,  R.string.toast_rotated_anticlockwise, Toast.LENGTH_SHORT).show()
    }

    private fun rotateClockwise() {
     // Dummy Placeholder
    // In main app it determines current orientation and changes the orientation by -90°, simplified here to keep it private; happy to demo it in an interview! 
    

        isRotationLocked = true
        Toast.makeText(this, R.string.toast_rotated_clockwise, Toast.LENGTH_SHORT).show()
    }


    private fun openAutoScrollLayout() {
               
    // Dummy Placeholder
    // In main app it opens auto scroll layout 
    // Unit function, returns nothing
    }

    private fun setAutoScrollButtons() {
               
    // Dummy Placeholder for button click listeners, simplified here to keep it private; happy to demo it in an interview! 
    // Unit function, returns nothing
    }

    private fun startAutoScroll() {
               
    // Dummy Placeholder
    // In main app it starts auto scrolling, simplified here to keep it private; happy to demo it in an interview! 
    // Unit function, returns nothing
    }

    private fun pauseAutoScroll() {
        // Dummy Placeholder
// In main app it pause auto scrolling, simplified here to keep it private; happy to demo it in an interview! 
// Unit function, returns nothing
    }

    private fun stopAutoScroll() {
        // Dummy Placeholder
// In main app it stops auto scrolling, simplified here to keep it private; happy to demo it in an interview! 
// Unit function, returns nothing
    }

    private fun updateScrollOffset() {
        // Dummy Placeholder
// In main app it updats scroll offset, simplified here to keep it private; happy to demo it in an interview! 
// Unit function, returns nothing
    }

    private fun saveScrollSpeed() {
        // Dummy Placeholder
// In main app it saves speed position, simplified here to keep it private; happy to demo it in an interview! 
// Unit function, returns nothing
    }


    private fun openBelowLayout() {
        // Dummy Placeholder
// In main app it will open PDF switching layout, simplified here to keep it private; happy to demo it in an interview! 
// Unit function, returns nothing
    }

    private fun closeBelowLayout() {
        
        // Dummy Placeholder
        // In main app it closes below layout, simplified here to keep it private; happy to demo it in an interview! 
        // Unit function, returns nothing
    }

    override fun onDestroy() {
        super.onDestroy()
        isAutoScrolling = false
        autoScrollJob?.cancel()
        autoScrollJob = null
        stopSlideshow(false)
    }
}