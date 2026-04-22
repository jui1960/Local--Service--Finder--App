package com.example.serviceapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.serviceapp.databinding.ItemCategoryBinding

class CategoryAdapter(
    private val categories: List<Category>,
    private val onCategoryClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position], position == selectedPosition)
    }

    override fun getItemCount() = categories.size

    inner class CategoryViewHolder(private val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category, isSelected: Boolean) {
            binding.apply {
                ivCategoryIcon.setImageResource(category.iconRes)
                tvCategoryName.text = category.name

                val context = root.context

                // Highlight selected category
                val bgColor = if (isSelected)
                    context.getColor(R.color.primary)
                else
                    context.getColor(R.color.category_bg)

                val iconTint = if (isSelected)
                    context.getColor(R.color.white)
                else
                    context.getColor(R.color.primary)

                // Setting background and tint
                root.setOnClickListener {
                    val position = bindingAdapterPosition // adapterPosition-er poriborte

                    if (position != RecyclerView.NO_POSITION) {
                        val prev = selectedPosition
                        selectedPosition = position

                        // Purono select kora item-ti refresh kora
                        notifyItemChanged(prev)
                        // Notun select kora item-ti refresh kora
                        notifyItemChanged(selectedPosition)

                        onCategoryClick(category)
                    }
                }
            }
        }
    }
}
