package com.example.mosibit.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mosibit.BuildConfig.NUMBER
import com.example.mosibit.databinding.ActivityBugReportBinding

@Suppress("SameParameterValue")
class BugReportActivity : AppCompatActivity() {
    private lateinit var _binding: ActivityBugReportBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityBugReportBinding.inflate(layoutInflater)
        setContentView(_binding.root)

        _binding.buttomBug.setOnClickListener {
            val text = _binding.inputBug.text.toString()
            try {
                sendMessage(text)
            } catch (e: Exception) {
                e.printStackTrace()
                val appPackageName = "com.whatsapp"
                try {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=$appPackageName")
                        )
                    )
                } catch (e: android.content.ActivityNotFoundException) {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName%22")
                        )
                    )
                }
            }
        }
    }
    private fun sendMessage(message: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
            putExtra("jid", "${NUMBER}@s.whatsapp.net")
            type = "text/plain"
            setPackage("com.whatsapp")
        }
        startActivity(sendIntent)
    }

}