package com.shieldblock.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.shieldblock.app.R
import com.shieldblock.app.data.PreferencesManager
import com.shieldblock.app.databinding.DialogAddItemBinding
import com.shieldblock.app.databinding.FragmentWebBinding
import com.shieldblock.app.databinding.ItemCategoryBinding

class WebFragment : Fragment() {

    private var _binding: FragmentWebBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefs: PreferencesManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWebBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = PreferencesManager.getInstance(requireContext())

        setupCategories()
        setupDomainsSection()
        setupKeywordsSection()
    }

    override fun onResume() {
        super.onResume()
        renderDomains()
        renderKeywords()
    }

    private fun setupCategories() {
        binding.layoutCategoriesList.removeAllViews()
        val categories = prefs.getCategories()

        for (cat in categories) {
            val catBinding = ItemCategoryBinding.inflate(layoutInflater, binding.layoutCategoriesList, false)
            catBinding.tvCategoryName.text = cat.title
            catBinding.tvCategoryDesc.text = cat.description
            catBinding.tvCategoryKeywordsSample.text = "Keywords: " + cat.keywords.take(6).joinToString(", ") + "..."

            catBinding.switchCategoryActive.isChecked = cat.isEnabled
            catBinding.switchCategoryActive.setOnCheckedChangeListener { _, isChecked ->
                prefs.setCategoryEnabled(cat.id, isChecked)
            }

            binding.layoutCategoriesList.addView(catBinding.root)
        }
    }

    private fun setupDomainsSection() {
        binding.btnAddDomain.setOnClickListener {
            showAddDomainDialog()
        }
        renderDomains()
    }

    private fun renderDomains() {
        binding.chipGroupDomains.removeAllViews()
        val domains = prefs.getBlockedDomains()

        for (domain in domains) {
            val chip = Chip(requireContext()).apply {
                text = domain
                isCloseIconVisible = true
                setChipBackgroundColorResource(R.color.card_bg_secondary)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                setCloseIconTintResource(R.color.text_muted)
                setOnCloseIconClickListener {
                    prefs.removeBlockedDomain(domain)
                    renderDomains()
                }
            }
            binding.chipGroupDomains.addView(chip)
        }
    }

    private fun showAddDomainDialog() {
        val dialogBinding = DialogAddItemBinding.inflate(layoutInflater)
        dialogBinding.tvDialogTitle.text = "Add Blocked Website"
        dialogBinding.tvDialogDescription.text = "Enter website domain to block across all browsers (e.g. reddit.com or youtube.com)"
        dialogBinding.tilInput.hint = "Domain Name"

        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton("Block Domain") { _, _ ->
                val input = dialogBinding.etInput.text?.toString()?.trim() ?: ""
                if (input.isNotEmpty()) {
                    prefs.addBlockedDomain(input)
                    renderDomains()
                    Toast.makeText(requireContext(), "Domain added to blocklist", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupKeywordsSection() {
        binding.btnAddKeyword.setOnClickListener {
            showAddKeywordDialog()
        }
        renderKeywords()
    }

    private fun renderKeywords() {
        binding.chipGroupKeywords.removeAllViews()
        val keywords = prefs.getCustomKeywords()

        for (kw in keywords) {
            val chip = Chip(requireContext()).apply {
                text = kw
                isCloseIconVisible = true
                setChipBackgroundColorResource(R.color.card_bg_secondary)
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                setCloseIconTintResource(R.color.text_muted)
                setOnCloseIconClickListener {
                    prefs.removeBlockedKeyword(kw)
                    renderKeywords()
                }
            }
            binding.chipGroupKeywords.addView(chip)
        }
    }

    private fun showAddKeywordDialog() {
        val dialogBinding = DialogAddItemBinding.inflate(layoutInflater)
        dialogBinding.tvDialogTitle.text = "Add Blocked Keyword"
        dialogBinding.tvDialogDescription.text = "Any web page or browser URL containing this term will be instantly blocked (works in Incognito & VPN)."
        dialogBinding.tilInput.hint = "Keyword (e.g. shorts, reels, gamble)"

        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton("Block Keyword") { _, _ ->
                val input = dialogBinding.etInput.text?.toString()?.trim() ?: ""
                if (input.isNotEmpty()) {
                    prefs.addBlockedKeyword(input)
                    renderKeywords()
                    Toast.makeText(requireContext(), "Keyword added to blocklist", Toast.LENGTH_SHORT).show()
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
