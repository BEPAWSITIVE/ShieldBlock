package com.shieldblock.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.shieldblock.app.R
import com.shieldblock.app.data.PreferencesManager
import com.shieldblock.app.databinding.FragmentDashboardBinding
import com.shieldblock.app.util.PermissionHelper

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: PreferencesManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferencesManager.getInstance(requireContext())

        setupMasterSwitch()
        setupPermissionButtons()
        updateDashboardState()
    }

    override fun onResume() {
        super.onResume()
        updateDashboardState()
    }

    private fun setupMasterSwitch() {
        binding.switchMasterProtection.isChecked = prefs.isMasterProtectionEnabled
        binding.switchMasterProtection.setOnCheckedChangeListener { _, isChecked ->
            prefs.isMasterProtectionEnabled = isChecked
            updateDashboardState()
        }
    }

    private fun setupPermissionButtons() {
        binding.btnGrantAccessibility.setOnClickListener {
            startActivity(PermissionHelper.getAccessibilitySettingsIntent())
        }

        binding.btnGrantOverlay.setOnClickListener {
            startActivity(PermissionHelper.getOverlaySettingsIntent(requireContext()))
        }

        binding.btnGrantUsage.setOnClickListener {
            startActivity(PermissionHelper.getUsageStatsSettingsIntent())
        }
    }

    private fun updateDashboardState() {
        val context = context ?: return
        val isMasterOn = prefs.isMasterProtectionEnabled

        // 1. Update Master Status UI
        if (isMasterOn) {
            binding.tvShieldStatus.text = getString(R.string.status_protected)
            binding.tvShieldSubtitle.text = getString(R.string.status_subtitle_active)
            binding.ivShieldIcon.setColorFilter(ContextCompat.getColor(context, R.color.primary))
        } else {
            binding.tvShieldStatus.text = getString(R.string.status_disabled)
            binding.tvShieldSubtitle.text = getString(R.string.status_subtitle_inactive)
            binding.ivShieldIcon.setColorFilter(ContextCompat.getColor(context, R.color.text_muted))
        }

        // 2. Update Permissions status
        val hasAccessibility = PermissionHelper.isAccessibilityServiceEnabled(context)
        val hasOverlay = PermissionHelper.canDrawOverlays(context)
        val hasUsage = PermissionHelper.hasUsageStatsPermission(context)

        // Accessibility status
        if (hasAccessibility) {
            binding.ivPermAccessibilityStatus.setImageResource(R.drawable.ic_check)
            binding.ivPermAccessibilityStatus.setColorFilter(ContextCompat.getColor(context, R.color.status_green))
            binding.btnGrantAccessibility.visibility = View.GONE
        } else {
            binding.ivPermAccessibilityStatus.setImageResource(R.drawable.ic_warning)
            binding.ivPermAccessibilityStatus.setColorFilter(ContextCompat.getColor(context, R.color.status_orange))
            binding.btnGrantAccessibility.visibility = View.VISIBLE
        }

        // Overlay status
        if (hasOverlay) {
            binding.ivPermOverlayStatus.setImageResource(R.drawable.ic_check)
            binding.ivPermOverlayStatus.setColorFilter(ContextCompat.getColor(context, R.color.status_green))
            binding.btnGrantOverlay.visibility = View.GONE
        } else {
            binding.ivPermOverlayStatus.setImageResource(R.drawable.ic_warning)
            binding.ivPermOverlayStatus.setColorFilter(ContextCompat.getColor(context, R.color.status_orange))
            binding.btnGrantOverlay.visibility = View.VISIBLE
        }

        // Usage stats status
        if (hasUsage) {
            binding.ivPermUsageStatus.setImageResource(R.drawable.ic_check)
            binding.ivPermUsageStatus.setColorFilter(ContextCompat.getColor(context, R.color.status_green))
            binding.btnGrantUsage.visibility = View.GONE
        } else {
            binding.ivPermUsageStatus.setImageResource(R.drawable.ic_warning)
            binding.ivPermUsageStatus.setColorFilter(ContextCompat.getColor(context, R.color.status_orange))
            binding.btnGrantUsage.visibility = View.VISIBLE
        }

        // 3. Update Counters
        binding.tvCountApps.text = prefs.getBlockedApps().size.toString()
        binding.tvCountDomains.text = prefs.getAllEffectiveBlockedDomains().size.toString()
        binding.tvCountKeywords.text = prefs.getAllEffectiveKeywords().size.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
