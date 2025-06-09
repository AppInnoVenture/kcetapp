package com.kea.pyp

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Color
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil.setContentView
import com.google.android.material.tabs.TabLayoutMediator
import com.kea.pyp.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {

        val isDarkMode = basePreferences.getBoolean("dark_mode", false)

        if (isDarkMode)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        else
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        binding = setContentView(this, R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(binding.mainContent) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (Build.VERSION.SDK_INT > 24) {
            val shortcutManager = getSystemService(ShortcutManager::class.java)

            CoroutineScope(Dispatchers.IO).launch {
            if (shortcutManager?.dynamicShortcuts.isNullOrEmpty()) {

                val shortcut1 = ShortcutInfo.Builder(this@MainActivity, "shortcut_1")
                    .setShortLabel(getString(R.string.tab_notes))
                    .setLongLabel(getString(R.string.tab_notes))
                    .setIcon(Icon.createWithResource(this@MainActivity, R.drawable.baseline_sticky_note_2_24))
                    .setIntent(Intent(this@MainActivity, MainActivity::class.java).apply {
                        action = Intent.ACTION_MAIN
                        putExtra("shortcut", 1)
                	setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    })
                    .build()

                val shortcut2 = ShortcutInfo.Builder(this@MainActivity, "shortcut_2")
                    .setShortLabel(getString(R.string.tab_qp))
                    .setLongLabel(getString(R.string.tab_qp))
                    .setIcon(Icon.createWithResource(this@MainActivity, R.drawable.baseline_question_answer_24))
                    .setIntent(Intent(this@MainActivity, MainActivity::class.java).apply {
                        action = Intent.ACTION_MAIN
                        putExtra("shortcut", 0)
                	setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    })
                    .build()

                val shortcut3 = ShortcutInfo.Builder(this@MainActivity, "shortcut_3")
                    .setShortLabel(getString(R.string.tab_cutoff))
                    .setLongLabel(getString(R.string.tab_cutoff))
                    .setIcon(Icon.createWithResource(this@MainActivity, R.drawable.baseline_format_list_numbered_24))
                    .setIntent(Intent(this@MainActivity, MainActivity::class.java).apply {
                        action = Intent.ACTION_MAIN
                        putExtra("shortcut", 2)
                	setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    })
                    .build()

                val shortcut4 = ShortcutInfo.Builder(this@MainActivity, "shortcut_4")
                    .setShortLabel(getString(R.string.tab_up))
                    .setLongLabel(getString(R.string.tab_up))
                    .setIcon(Icon.createWithResource(this@MainActivity, R.drawable.up))
                    .setIntent(Intent(this@MainActivity, MainActivity::class.java).apply {
                        action = Intent.ACTION_MAIN
                        putExtra("shortcut", 3)
                        setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    })
                    .build()

                withContext(Dispatchers.Main) {
                    shortcutManager?.dynamicShortcuts = listOf(shortcut1, shortcut2, shortcut3, shortcut4)
                }
             }
            }
        }
        val drawerLayout = binding.drawerLayout
        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.nav_open, R.string.nav_close)
        if (!isDarkMode) toggle.drawerArrowDrawable.color = Color.BLACK

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val navView = binding.navView
        navView.setNavigationItemSelectedListener { menuItem ->

            when (menuItem.itemId) {
                R.id.nav_home -> drawerLayout.closeDrawer(GravityCompat.START)

                R.id.nav_telegram -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    startActivity(Intent(Intent.ACTION_VIEW, "https://t.me/karnataka_kea".toUri()))
                }
                R.id.nav_telegram2 -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    startActivity(Intent(Intent.ACTION_VIEW, "https://t.me/karnataka_neet".toUri()))
                }

//                R.id.nav_donate -> {
//                    drawerLayout.closeDrawer(GravityCompat.START)
//                    startActivity(Intent(this, DonateActivity::class.java))
               // }

                R.id.puc -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    startActivity(Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=com.puc.pyp".toUri()))
                }

                R.id.nav_1puc -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    startActivity(Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=com.first.puc".toUri()))
                }

                R.id.nav_pucqp -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    startActivity(Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=com.puc.notes".toUri()))
                }

                R.id.nav_dark -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    basePreferences.edit { putBoolean("dark_mode", !isDarkMode) }
                    recreate()
                }

                R.id.nav_delete ->  {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    val pdfFiles = filesDir.listFiles { file -> file.extension == "pdf"}?.toSet()
                    handleFileDeletion(pdfFiles, getString(R.string.alertMsg), binding.root)
                }

                R.id.nav_lang -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    basePreferences.edit { putString("language",
                        if (basePreferences.getString("language", "en") == "en") "kn" else "en") }
                    recreate()
                }

                R.id.nav_share -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                        putExtra(Intent.EXTRA_TEXT, getString(R.string.shareApp))
                        type = "text/plain" }, "Share app via"))
                }

                R.id.nav_privacy -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    startActivity(Intent(Intent.ACTION_VIEW, "https://sites.google.com/view/karpyp/home".toUri()))
                }

                R.id.nav_apps -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                    startActivity(Intent(Intent.ACTION_VIEW, "https://play.google.com/store/search?q=pub:AppInnoVenture".toUri()))
                }

                R.id.nav_exit -> {
                    Toast.makeText(this, R.string.exitToast, Toast.LENGTH_SHORT).show()
                    finish()
                }
            
            R.id.nav_mail -> {
                    try {
                        startActivity(Intent.createChooser(Intent(Intent.ACTION_SENDTO).apply {
                            data = "mailto:".toUri()
                            putExtra(Intent.EXTRA_EMAIL, arrayOf("ayg0702@gmail.com"))
                        }, "Send Email"))
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(this, "No email app found.", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this, "Failed to send email: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            navView.menu.setGroupCheckable(0, true, true)
            menuItem.isChecked = true
            navView.menu.setGroupCheckable(0, false, false)

            true
        }

        val adapter = ViewPagerAdapter(this)
        adapter.addFragment(PreviousYearPapersFragment(), getString(R.string.tab_qp))
        adapter.addFragment(StudyMaterialFragment(), getString(R.string.tab_notes))
        adapter.addFragment(CutoffFragment(), getString(R.string.tab_cutoff))
        adapter.addFragment(UpdatesFragment(), getString(R.string.tab_up))
        binding.viewPager.adapter = adapter
        binding.viewPager.setCurrentItem(
            intent.getIntExtra("shortcut", basePreferences.getInt("frag", 0)), false)

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = adapter.getPageTitle(position)
            tab.contentDescription = adapter.getPageTitle(position)
        }.attach()

        binding.viewPager.offscreenPageLimit = 3
        onBackPressedDispatcher.addCallback(this) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START))
                drawerLayout.closeDrawer(GravityCompat.START)
            else
                finish()
        }
    }

    override fun onPause() {
        super.onPause()
        basePreferences.edit { putInt("frag", binding.viewPager.currentItem) }
    }
}