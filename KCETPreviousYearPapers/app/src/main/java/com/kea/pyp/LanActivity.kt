package com.kea.pyp

import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Color
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.get
import androidx.databinding.DataBindingUtil
import androidx.databinding.DataBindingUtil.setContentView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.kea.pyp.databinding.ActivityLanBinding
import com.kea.pyp.databinding.ToolbarLanBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class LanActivity : BaseActivity() {

    private lateinit var prefix: String
    private lateinit var descr: String
    private lateinit var head: String
    private var shortcut: Boolean = false
    private var isDarkMode: Boolean = false
    private val img: Int by lazy { intent.getIntExtra("img", -1) }
    private lateinit var binding: ActivityLanBinding
    private lateinit var lanitems: List<YearItem>
    private var headYear = 2025

    override fun onCreate(savedInstanceState: Bundle?) {

        isDarkMode = basePreferences.getBoolean("dark_mode", false)
        shortcut = intent.getBooleanExtra("shortcut", false)

        if (shortcut)
            if (isDarkMode)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        binding = setContentView(this, R.layout.activity_lan)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        descr = intent.getStringExtra("desc").toString()
        
        head = "$descr ${getString(R.string.tab_qp)}"

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (isDarkMode) {
            binding.toolbar.navigationIcon?.setTint(Color.BLACK)
            binding.toolbar.overflowIcon?.setTint(Color.BLACK)
        }
        val customTitleViewBinding: ToolbarLanBinding = DataBindingUtil.inflate(
            LayoutInflater.from(this), R.layout.toolbar_lan, null, false)
        binding.toolbar.addView(
            customTitleViewBinding.root, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
        customTitleViewBinding.customToolbarTitle.apply {
            text = head
            isSelected = true
        }

        lanitems= mutableListOf(
            YearItem("2025", ""),
            YearItem("2024", ""),
            YearItem("2023", ""),
            YearItem("2022", ""),
            YearItem("2021", ""),
            YearItem("2020", ""),
            YearItem("2019", ""),
            YearItem("2018", ""),
            YearItem("2017", ""),
            YearItem("2016", ""),
            YearItem("2015", ""),
            YearItem("2014", ""),
            YearItem("2013", ""),
            YearItem("2012", ""),
            YearItem("2011", ""),
            YearItem("2010", ""),
            YearItem("2009", ""),
            YearItem("2008", ""),
            YearItem("2007", ""),
            YearItem("2006", ""),
            YearItem("2005", ""),
            YearItem("2004", "")
        )

        prefix = intent.getStringExtra("extraTag").toString()
        if(prefix[0]=='k') {
            lanitems = (lanitems.subList(1,3)+lanitems.subList(4,9)+lanitems.subList(10,13)).toMutableList()
            binding.note.visibility = View.VISIBLE
            binding.kanNote.text = getString(R.string.KanNote)           
        }
        name = intent.getStringExtra("name").toString()
        val itemAdapterLan = AdapterLan(lanitems){ item ->
            headYear = item.descLan.toIntOrNull() ?: 2025

            if (headYear>2019 || File(filesDir, "$prefix${item.descLan}.pdf").exists() || isInternetAvailable())
                openPdfViewer()
            else
                showRetrySnackbar()
        }

        binding.recyclerViewLan.setHasFixedSize(true)
        binding.recyclerViewLan.layoutManager = LinearLayoutManager(this).apply {
            isItemPrefetchEnabled=true
        }
        binding.recyclerViewLan.adapter = itemAdapterLan

        if (savedInstanceState == null){
            binding.recyclerViewLan.layoutAnimation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation)
        if (basePreferences.getBoolean("dwd", false)) {
            val file = File(filesDir, basePreferences.getString("dwdPdf", null).toString())
            if (file.exists()) file.delete()
            basePreferences.edit { putBoolean("dwd", false) }
        }
        }
         onBackPressedDispatcher.addCallback(this) {
            if (shortcut) {
                basePreferences.edit { putInt("frag", 0) }
                startActivity(Intent(this@LanActivity, MainActivity::class.java).apply {
                    setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtra("pinShortcut", false)
                })
            } else {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        }

    }

    private fun openPdfViewer() {
        val intent = Intent(this, PdfViewerActivity::class.java).apply {
            putExtra("desc", descr)
            putExtra("prefix", prefix)
            putExtra("year", headYear)
            putExtra("diff", "prev")
        }
        startActivity(intent)
    }

    private fun showRetrySnackbar() {
        Snackbar.make(binding.root, R.string.noNetRetry, Snackbar.LENGTH_LONG).setAction(R.string.snack_retry) {
            if (isInternetAvailable())
                openPdfViewer()
            else
                lifecycleScope.launch {
                    delay(300)
                    showRetrySnackbar()
                }
        }.show()
    }

    override fun onSupportNavigateUp(): Boolean {
        if (shortcut) {
            basePreferences.edit { putInt("frag", 0) }
            startActivity(Intent(this@LanActivity, MainActivity::class.java).apply {
                setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra("pinShortcut", false)
            })
        } else
            onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_lan, menu)
        menu?.get(2)?.isVisible = true

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.lan_share -> {
                startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_TEXT, getString(R.string.shareApp))
                    type = "text/plain" }, "Share app via"))
                return true
            }

            R.id.lan_exit -> {
                Toast.makeText(this, R.string.exitToast, Toast.LENGTH_SHORT).show()
                finishAffinity()
                return true
            }

            R.id.lan_delete -> {
                val filesToDelete = lanitems.map { File(filesDir, "$prefix${it.descLan}.pdf") }.filter { it.exists() }.toSet()

                handleFileDeletion(filesToDelete, getString(R.string.alertLanMsg), binding.root)
                return true
            }

            R.id.lan_shortcut -> {
                val intent = Intent(this, LanActivity::class.java)
                intent.action = Intent.ACTION_MAIN
                intent.putExtra("extraTag",prefix)
                intent.putExtra("desc", descr)
                intent.putExtra("img", img)
                intent.putExtra("shortcut", true)
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)

                if (Build.VERSION.SDK_INT > 25) {
                    val shortcutManager = getSystemService(ShortcutManager::class.java)

                    val shortcut = ShortcutInfo.Builder(this, head)
                        .setShortLabel(head)
                        .setLongLabel(head)
                        .setIcon(Icon.createWithResource(this, img))
                        .setIntent(intent)
                        .build()

                    shortcutManager.requestPinShortcut(shortcut, null)
                    Toast.makeText(this, R.string.lanShortcutToast, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Shortcut creation not supported on your phone", Toast.LENGTH_SHORT).show()
                }
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

}