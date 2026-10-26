package com.shieldblock.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.shieldblock.app.R
import com.shieldblock.app.databinding.ActivityBlockedOverlayBinding

class BlockedOverlayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBlockedOverlayBinding

    companion object {
        const val EXTRA_REASON_TYPE = "extra_reason_type"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_DETAIL = "extra_detail"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlockedOverlayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val reasonType = intent.getStringExtra(EXTRA_REASON_TYPE) ?: "app"
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Restricted Content"
        val detail = intent.getStringExtra(EXTRA_DETAIL) ?: "This content violates active ShieldBlock policies."

        when (reasonType) {
            "app" -> {
                binding.tvBlockedReason.text = getString(R.string.blocked_reason_app, title)
            }
            "domain" -> {
                binding.tvBlockedReason.text = getString(R.string.blocked_reason_domain, title)
            }
            "keyword" -> {
                binding.tvBlockedReason.text = getString(R.string.blocked_reason_keyword, title)
            }
            "tamper" -> {
                binding.tvBlockedReason.text = "Settings Access Restricted"
            }
            "vpn" -> {
                binding.tvBlockedReason.text = "VPN Applications Restricted"
            }
            else -> {
                binding.tvBlockedReason.text = title
            }
        }

        binding.tvBlockedDetails.text = detail

        // Exit to home button
        binding.btnExitHome.setOnClickListener {
            goToHomeScreen()
        }

        // Intercept back button to prevent returning to the blocked activity
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goToHomeScreen()
            }
        })
    }

    private fun goToHomeScreen() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(homeIntent)
        finish()
    }
}
