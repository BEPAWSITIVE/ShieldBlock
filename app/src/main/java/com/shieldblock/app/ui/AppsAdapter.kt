package com.shieldblock.app.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shieldblock.app.databinding.ItemAppToggleBinding
import com.shieldblock.app.model.AppItem

class AppsAdapter(
    private var items: List<AppItem>,
    private val onToggle: (AppItem, Boolean) -> Unit,
    private val onDelete: (AppItem) -> Unit
) : RecyclerView.Adapter<AppsAdapter.AppViewHolder>() {

    fun updateList(newItems: List<AppItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemAppToggleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class AppViewHolder(private val binding: ItemAppToggleBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AppItem) {
            binding.tvAppName.text = item.name
            binding.tvPackageName.text = item.packageName

            // Clear previous listener before setting isChecked to avoid feedback loops
            binding.switchBlocked.setOnCheckedChangeListener(null)
            binding.switchBlocked.isChecked = item.isBlocked

            binding.switchBlocked.setOnCheckedChangeListener { _, isChecked ->
                item.isBlocked = isChecked
                onToggle(item, isChecked)
            }

            if (item.isCustom) {
                binding.ivDeleteCustomApp.visibility = View.VISIBLE
                binding.ivDeleteCustomApp.setOnClickListener {
                    onDelete(item)
                }
            } else {
                binding.ivDeleteCustomApp.visibility = View.GONE
            }
        }
    }
}
