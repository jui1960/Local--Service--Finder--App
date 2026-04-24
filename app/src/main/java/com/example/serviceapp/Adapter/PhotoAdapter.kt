package com.example.serviceapp.Adapter


import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.serviceapp.databinding.ItemPhotoPreviewBinding

class PhotoPreviewAdapter(
    private val onRemoveClick: (String) -> Unit
) : ListAdapter<String, PhotoPreviewAdapter.PhotoViewHolder>(PhotoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = ItemPhotoPreviewBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PhotoViewHolder(private val binding: ItemPhotoPreviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(uri: String) {
            // Glide diye URI theke photo load kora
            Glide.with(binding.ivPhoto.context)
                .load(Uri.parse(uri))
                .centerCrop()
                .into(binding.ivPhoto)

            // Remove icon click logic
            binding.ivRemove.setOnClickListener {
                onRemoveClick(uri)
            }
        }
    }

    // List logic handle korar jonno DiffUtil
    class PhotoDiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
    }
}