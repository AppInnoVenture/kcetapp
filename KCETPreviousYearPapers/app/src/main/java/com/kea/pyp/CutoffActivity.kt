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
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.DataBindingUtil.setContentView
import androidx.recyclerview.widget.LinearLayoutManager
import com.kea.pyp.databinding.ActivityLanBinding
import com.kea.pyp.databinding.ToolbarLanBinding

class CutoffActivity : BaseActivity() {
   
    private lateinit var prefix: String
    private lateinit var descr: String
    private lateinit var head: String
    private var shortcut: Boolean = false
    private var isDarkMode: Boolean = false
    private val img: Int by lazy { intent.getIntExtra("img", -1) }
    
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
        val binding: ActivityLanBinding = setContentView(this, R.layout.activity_lan)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        descr = intent.getStringExtra("desc").toString()
        
        head = "$descr ${getString(R.string.tab_cutoff)}"
        
        prefix = intent.getStringExtra("extraTag").toString()

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

        val lanitems = when(prefix) {
            in setOf("dental_", "medi_") -> listOf(
                YearItem("2024: General", "cutoff_2024.pdf"),
                YearItem("2024: Hyd-Kar", "cutoff_2024_hk.pdf"),
                YearItem("2024: Private", "cutoff_2024_priv.pdf"),
                YearItem("2023: General", "cutoff_2023.pdf"),
                YearItem("2023: Hyd-Kar", "cutoff_2023_hk.pdf"),
                YearItem("2023: Private", "cutoff_2023_priv.pdf")
            )
            in setOf("agri_", "vet_") -> listOf(
                YearItem("2024: General","cutoff_2024.pdf"),
                YearItem("2024: Hyd-Kar","cutoff_2024_hk.pdf"),
                YearItem("2024: General (Practical)","prct_cutoff_2024.pdf"),
                YearItem("2024: Hyd-Kar (Practical)","prct_cutoff_2024_hk.pdf"),
                YearItem("2023: General","cutoff_2023.pdf"),
                YearItem("2023: Hyd-Kar","cutoff_2023_hk.pdf"),
                YearItem("2023: General (Practical)","prct_cutoff_2023.pdf"),
                YearItem("2023: Hyd-Kar (Practical)","prct_cutoff_2023_hk.pdf"),
            )
            "ismh_" -> listOf(
                YearItem("2024: General", "cutoff_2024.pdf"),
                YearItem("2024: Hyd-Kar", "cutoff_2024_hk.pdf"),
                YearItem("2024: Private", "cutoff_2024_priv.pdf")
            )
            in setOf("arch_", "engg_") -> listOf(
                YearItem("2024: General","cutoff_2024.pdf"),
                YearItem("2024: Hyd-Kar","cutoff_2024_hk.pdf"),
                YearItem("2023: General","cutoff_2023.pdf"),
                YearItem("2023: Hyd-Kar","cutoff_2023_hk.pdf")
            )
            "bnys_" -> listOf(
                YearItem("2022: General","cutoff_2022.pdf"),
                YearItem("2022: Hyd-Kar","cutoff_2022_hk.pdf"),
            )
            "ahs_" -> listOf(
                YearItem("2022","cutoff_2022.pdf"),
            )
            else -> listOf(
                YearItem("2024: General","cutoff_2024.pdf"),
                YearItem("2024: Hyd-Kar","cutoff_2024_hk.pdf"),
            )
        }

        
        val itemAdapterLan = AdapterLan(lanitems){ item ->
            val intent = Intent(this, PdfViewerActivity::class.java)
            intent.putExtra("prefix",(prefix+item.extra).replace("arch_cutoff_2024","engg_cutoff_2024"))
            intent.putExtra("desc","$descr ${item.descLan}")
            intent.putExtra("diff", "cutoff")
            startActivity(intent)
        }
        binding.recyclerViewLan.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewLan.adapter = itemAdapterLan

        if (savedInstanceState == null)
            binding.recyclerViewLan.layoutAnimation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation)

        onBackPressedDispatcher.addCallback(this) {
            if (shortcut) {
                basePreferences.edit { putInt("frag", 2) }
                startActivity(Intent(this@CutoffActivity, MainActivity::class.java).apply {
                    setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    putExtra("pinShortcut", false)
                })
            } else {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        if (shortcut) {
            basePreferences.edit { putInt("frag", 2) }
            startActivity(Intent(this@CutoffActivity, MainActivity::class.java).apply {
                setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra("pinShortcut", false)
            })
        } else
            onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_lan, menu)
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

            R.id.lan_shortcut -> {
                val intent = Intent(this, CutoffActivity::class.java)
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
                } else
                    Toast.makeText(this, "Shortcut creation not supported on your phone", Toast.LENGTH_SHORT).show()

                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

}