package com.shieldblock.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shieldblock.app.R
import com.shieldblock.app.data.PreferencesManager
import com.shieldblock.app.databinding.DialogSetPinBinding
import com.shieldblock.app.databinding.FragmentStrictBinding
import com.shieldblock.app.util.PermissionHelper

class StrictFragment : Fragment() {

    private var _binding: FragmentStrictBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: PreferencesManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStrictBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferencesManager.getInstance(requireContext())

        setupSwitches()
        setupPinButton()
    }

    override fun onResume() {
        super.onResume()
        updateUIState()
    }

    private fun setupSwitches() {
        binding.switchLockSettings.isChecked = prefs.isLockSettingsEnabled
        binding.switchLockSettings.setOnCheckedChangeListener { _, isChecked ->
            prefs.isLockSettingsEnabled = isChecked
        }

        binding.switchBlockVpn.isChecked = prefs.isBlockVpnEnabled
        binding.switchBlockVpn.setOnCheckedChangeListener { _, isChecked ->
            prefs.isBlockVpnEnabled = isChecked
        }

        binding.switchDeviceAdmin.setOnClickListener {
            val isActive = PermissionHelper.isDeviceAdminActive(requireContext())
            if (!isActive) {
                startActivity(PermissionHelper.getDeviceAdminIntent(requireContext()))
            } else {
                Toast.makeText(
                    requireContext(),
                    "Device Admin is active. To remove, visit Android Settings > Security > Device Admin apps.",
                    Toast.LENGTH_LONG
                ).show()
                binding.switchDeviceAdmin.isChecked = true
            }
        }
    }

    private fun setupPinButton() {
        binding.btnConfigPin.setOnClickListener {
            showPinConfigDialog()
        }
    }

    private fun updateUIState() {
        val isAdminActive = PermissionHelper.isDeviceAdminActive(requireContext())
        binding.switchDeviceAdmin.isChecked = isAdminActive

        if (prefs.isPinProtectionEnabled) {
            binding.tvPinStatus.text = "Active (Security PIN configured)"
            binding.btnConfigPin.text = "Change PIN"
        } else {
            binding.tvPinStatus.text = "No PIN set. Settings can be modified freely."
            binding.btnConfigPin.text = "Set PIN"
        }
    }

    private fun showPinConfigDialog() {
        val dialogBinding = DialogSetPinBinding.inflate(layoutInflater)

        if (prefs.isPinProtectionEnabled) {
            dialogBinding.tvPinDialogTitle.text = "Change or Remove PIN"
            dialogBinding.tvPinDialogSubtitle.text = "Enter a new 4-6 digit PIN, or leave blank to remove protection."
        }

        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton("Save") { _, _ ->
                val entered = dialogBinding.etPinInput.text?.toString()?.trim() ?: ""
                if (entered.isEmpty()) {
                    prefs.pinCode = null
                    Toast.makeText(requireContext(), "PIN removed", Toast.LENGTH_SHORT).show()
                } else if (entered.length >= 4) {
                    prefs.pinCode = entered
                    Toast.makeText(requireContext(), "PIN protection active", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "PIN must be at least 4 digits", Toast.LENGTH_SHORT).show()
                }
                updateUIState()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
