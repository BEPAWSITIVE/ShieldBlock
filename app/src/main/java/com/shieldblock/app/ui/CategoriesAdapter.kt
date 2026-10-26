package com.shieldblock.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shieldblock.app.databinding.ItemCategoryBinding
import com.shieldblock.app.model.BlockCategory

class CategoriesAdapter(
    private var categories: List<BlockCategory>,
    private val onToggle: (BlockCategory, Boolean) -> Unit
) : RecyclerView.Adapter<CategoriesAdapter.CategoryViewHolder>() {

    fun updateCategories(newCategories: List<BlockCategory>) {
        categories = newCategories
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size

    inner class CategoryViewHolder(private val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: BlockCategory) {
            binding.tvCategoryName.text = category.title
            binding.tvCategoryDesc.text = category.description
            binding.tvCategoryKeywordsSample.text = "Keywords: " + category.keywords.take(6).joinToString(", ") + "..."

            binding.switchCategoryActive.setOnCheckedChangeListener(null)
            binding.switchCategoryActive.isChecked = category.isEnabled

            binding.switchCategoryActive.setOnCheckedChangeListener { _, isChecked ->
                category.isEnabled = isChecked
                onToggle(category, isChecked)
            }
        }
    }
}
