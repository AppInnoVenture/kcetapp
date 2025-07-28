package com.kea.pyp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.kea.pyp.databinding.ActivityDonateBinding
import androidx.databinding.DataBindingUtil.setContentView

class DonateActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        val binding: ActivityDonateBinding = setContentView(this, R.layout.activity_donate)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied UPI", "appinnoventure@airtel")

        binding.idlayout.setOnClickListener {
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "UPI ID copied to clipboard", Toast.LENGTH_SHORT).show()
        }
        binding.upiBtn.setOnClickListener {
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "UPI ID copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        val uri = Uri.parse("upi://pay?pa=appinnoventure@airtel&pn=AppInnoVenture")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        val packageManager: PackageManager = packageManager

        binding.appLayout.setOnClickListener {
            if (intent.resolveActivity(packageManager) != null)
                startActivity(intent)
            else
                Toast.makeText(this, "No UPI app found", Toast.LENGTH_SHORT).show()
        }
        binding.upiApp.setOnClickListener {
            if (intent.resolveActivity(packageManager) != null)
                startActivity(intent)
            else
                Toast.makeText(this, "No UPI app found", Toast.LENGTH_SHORT).show()
        }
    }
}