package com.example.serviceapp.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.serviceapp.R
import com.example.serviceapp.Model.ServicePost
import com.example.serviceapp.databinding.ItemServiceCardBinding

class ServiceAdapter(
    private val onItemClick: (ServicePost) -> Unit,
    private val onBookmarkClick: (ServicePost) -> Unit
) : ListAdapter<ServicePost, ServiceAdapter.ServiceViewHolder>(ServiceDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        // XML layout ke Binding class-e convert kora
        val binding = ItemServiceCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ServiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ServiceViewHolder(private val binding: ItemServiceCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(service: ServicePost) {
            // binding reference use kore data set kora
            binding.apply {
                ivServiceImage.setImageResource(service.imageRes)
                tvTitle.text = service.title
                tvProviderName.text = service.providerName
                tvRating.text = service.rating.toString()
                tvReviewCount.text = "(${service.reviewCount})"
                tvPrice.text = "৳ ${service.price}"

                // Badge logic
                if (service.isOffering) {
                    tvBadge.text = "Offering Service"
                    tvBadge.setBackgroundResource(R.drawable.bg_badge_offering)
                    tvBadge.setTextColor(root.context.getColor(R.color.badge_offering_text))
                } else {
                    tvBadge.text = "Looking for Service"
                    tvBadge.setBackgroundResource(R.drawable.bg_badge_looking)
                    tvBadge.setTextColor(root.context.getColor(R.color.badge_looking_text))
                }

                // Bookmark icon state
                ivBookmark.setImageResource(
                    if (service.isBookmarked) R.drawable.bookmark
                    else R.drawable.bookmark2
                )

                // Click listeners
                root.setOnClickListener { onItemClick(service) }
                ivBookmark.setOnClickListener { onBookmarkClick(service) }
            }
        }
    }

    class ServiceDiffCallback : DiffUtil.ItemCallback<ServicePost>() {
        override fun areItemsTheSame(old: ServicePost, new: ServicePost) = old.id == new.id
        override fun areContentsTheSame(old: ServicePost, new: ServicePost) = old == new
    }
}