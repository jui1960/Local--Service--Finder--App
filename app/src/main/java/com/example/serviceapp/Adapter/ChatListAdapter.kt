package com.example.serviceapp.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.serviceapp.Model.ChatUser
import com.example.serviceapp.R

class ChatListAdapter(
    private var userList: List<ChatUser>,
    private val onClick: (ChatUser) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.UserViewHolder>() {

    // Filter korar jonno original data-r copy
    private var originalList = userList.toList()

    fun updateData(newList: List<ChatUser>) {
        this.userList = newList
        this.originalList = newList.toList()
        notifyDataSetChanged()
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvInitials    : TextView = itemView.findViewById(R.id.tvInitials)
        val tvUserName    : TextView = itemView.findViewById(R.id.tvUserName)
        val tvLastMessage : TextView = itemView.findViewById(R.id.tvLastMessage)
        val tvTime        : TextView = itemView.findViewById(R.id.tvTime)
        val tvUnreadCount : TextView = itemView.findViewById(R.id.tvUnreadCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.chatlist_item, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]

        // ১. Name Display Logic (Jodi name empty thake, tobe backup text show kora)
        val displayName = if (user.name.isNullOrBlank()) "Unknown User" else user.name
        holder.tvUserName.text = displayName

        // ২. Initials Logic
        holder.tvInitials.text = if (!user.initials.isNullOrBlank()) {
            user.initials
        } else {
            displayName.take(1).uppercase()
        }

        holder.tvLastMessage.text = user.lastMessage
        holder.tvTime.text        = user.time

        // আনরিড কাউন্ট লজিক
        if (user.unreadCount > 0) {
            holder.tvUnreadCount.visibility = View.VISIBLE
            holder.tvUnreadCount.text       = user.unreadCount.toString()
        } else {
            holder.tvUnreadCount.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { onClick(user) }
    }
    override fun getItemCount() = userList.size

    // ৩. সার্চ ফিল্টার
    fun filter(query: String) {
        userList = if (query.isEmpty()) {
            originalList
        } else {
            originalList.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }
        notifyDataSetChanged()
    }
}