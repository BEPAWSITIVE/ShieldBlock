package com.shieldblock.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.shieldblock.app.R
import com.shieldblock.app.data.PreferencesManager
import com.shieldblock.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = PreferencesManager.getInstance(this)

        setupNavigation()
        updateStatusChip()

        if (savedInstanceState == null) {
            loadFragment(DashboardFragment())
        }
    }

    override fun onResume() {
        super.onResume()
        updateStatusChip()
    }

    private fun setupNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    loadFragment(DashboardFragment())
                    true
                }
                R.id.nav_apps -> {
                    loadFragment(AppsFragment())
                    true
                }
                R.id.nav_web -> {
                    loadFragment(WebFragment())
                    true
                }
                R.id.nav_strict -> {
                    loadFragment(StrictFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun updateStatusChip() {
        val isActive = prefs.isMasterProtectionEnabled
        if (isActive) {
            binding.chipShieldStatus.text = getString(R.string.perm_granted)
            binding.chipShieldStatus.setTextColor(ContextCompat.getColor(this, R.color.status_green))
            binding.chipShieldStatus.setChipBackgroundColorResource(R.color.status_green_bg)
            binding.chipShieldStatus.setChipIconResource(R.drawable.ic_check)
            binding.chipShieldStatus.setChipIconTintResource(R.color.status_green)
        } else {
            binding.chipShieldStatus.text = "Paused"
            binding.chipShieldStatus.setTextColor(ContextCompat.getColor(this, R.color.status_orange))
            binding.chipShieldStatus.setChipBackgroundColorResource(R.color.card_bg_secondary)
            binding.chipShieldStatus.setChipIconResource(R.drawable.ic_warning)
            binding.chipShieldStatus.setChipIconTintResource(R.color.status_orange)
        }
    }
}
