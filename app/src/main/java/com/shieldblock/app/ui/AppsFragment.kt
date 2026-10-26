package com.shieldblock.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shieldblock.app.data.PreferencesManager
import com.shieldblock.app.databinding.DialogAddItemBinding
import com.shieldblock.app.databinding.FragmentAppsBinding
import com.shieldblock.app.model.AppItem
import com.shieldblock.app.util.PredefinedRules

class AppsFragment : Fragment() {

    private var _binding: FragmentAppsBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: PreferencesManager
    private lateinit var adapter: AppsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAppsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferencesManager.getInstance(requireContext())

        setupRecyclerView()
        setupAddButton()
    }

    override fun onResume() {
        super.onResume()
        refreshAppList()
    }

    private fun setupRecyclerView() {
        adapter = AppsAdapter(
            items = emptyList(),
            onToggle = { item, isBlocked ->
                prefs.setAppBlocked(item.packageName, isBlocked)
            },
            onDelete = { item ->
                prefs.removeCustomApp(item.packageName)
                refreshAppList()
            }
        )
        binding.rvApps.layoutManager = LinearLayoutManager(requireContext())
        binding.rvApps.adapter = adapter
        refreshAppList()
    }

    private fun refreshAppList() {
        val blockedSet = prefs.getBlockedApps()
        val predefined = PredefinedRules.DEFAULT_APPS.map { app ->
            app.copy(isBlocked = blockedSet.contains(app.packageName))
        }
        val custom = prefs.getCustomApps().map { app ->
            app.copy(isBlocked = blockedSet.contains(app.packageName))
        }
        val combined = predefined + custom
        adapter.updateList(combined)
    }

    private fun setupAddButton() {
        binding.btnAddCustomApp.setOnClickListener {
            showAddAppDialog()
        }
    }

    private fun showAddAppDialog() {
        val dialogBinding = DialogAddItemBinding.inflate(layoutInflater)
        dialogBinding.tvDialogTitle.text = "Add Custom App"
        dialogBinding.tvDialogDescription.text = "Enter package name of the app to block (e.g. com.google.android.youtube)"
        dialogBinding.tilInput.hint = "Package Name"

        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton("Block App") { _, _ ->
                val pkgName = dialogBinding.etInput.text?.toString()?.trim() ?: ""
                if (pkgName.isNotEmpty() && pkgName.contains(".")) {
                    val appItem = AppItem(
                        name = pkgName.substringAfterLast(".").replaceFirstChar { it.uppercase() },
                        packageName = pkgName,
                        isCustom = true,
                        isBlocked = true
                    )
                    prefs.addCustomApp(appItem)
                    refreshAppList()
                    Toast.makeText(requireContext(), "App added to blocklist", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Invalid package name format", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
