package com.kea.pyp

import android.animation.ValueAnimator
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil.setContentView
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.kea.pyp.databinding.ActivitySplitPdfBinding
import java.io.File
import java.io.FileNotFoundException
import androidx.core.content.edit

class SplitPdfActivity : AppCompatActivity() {

    private val sharedPreferences: SharedPreferences by lazy { getSharedPreferences("pdfPrefs", MODE_PRIVATE) }
    private lateinit var pdfAbove: PDFView
    private lateinit var pdf1: String
    private var initialY: Float = 0f
    private var weightThreshold: Float = 0f
    private val minWeight = 0.1f
    private val maxWeight = 0.9f
    private var currentWeight = 0.5f
    private var isDragging = false
    private var layoutHeight = 0
    private var isAttached = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        val binding: ActivitySplitPdfBinding = setContentView(this, R.layout.activity_split_pdf)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            v.setPadding(0, 0, 0, 0)
            insets
        }
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.decorView.systemUiVisibility = ( View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
        binding.root.requestLayout()

        pdf1 = intent.getStringExtra("pdf1").toString()
        val pdf2 = intent.getStringExtra("pdf2").toString()
        val isNight = intent.getBooleanExtra("isNight", false)
        val ansPage = intent.getIntExtra("ansPage", 0)

        pdfAbove = binding.pdfAbove
        val pdfBelow = binding.pdfBelow
        val handle = binding.handle
        val linearLayout = binding.linearLayout
        linearLayout.alpha = 0f
        val touchSlop by lazy { ViewConfiguration.get(this).scaledTouchSlop }
     
        try {   
            pdfAbove.fromFile(File(filesDir, pdf1)).nightMode(isNight).defaultPage(sharedPreferences.getInt(pdf1, 0))
                  .fitEachPage(true).scrollHandle(DefaultScrollHandle(this)).password(REDACTED).load()
        
            binding.pdfBelow.fromStream(assets.open(pdf2)).nightMode(isNight).pages(ansPage)
                .password(REDACTED).load()
        } catch (e: Exception) {
            Toast.makeText(this, R.string.errr, Toast.LENGTH_LONG).show()
       }
        linearLayout.animate().alpha(1f).setDuration(600).start()

        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        handle.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
            
    // Dummy Placeholder
                }

                MotionEvent.ACTION_MOVE -> {
                       
    // Dummy Placeholder 
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                      
    // Dummy Placeholder  
                }

                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onPause() {
        super.onPause()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        sharedPreferences.edit { putInt(pdf1, pdfAbove.currentPage) }
    }
}