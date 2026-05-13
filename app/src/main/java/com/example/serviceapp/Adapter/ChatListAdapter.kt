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

    private var originalList = userList.toList()

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

        holder.tvInitials.text    = user.initials
        holder.tvUserName.text    = user.name
        holder.tvLastMessage.text = user.lastMessage
        holder.tvTime.text        = user.time

        if (user.unreadCount > 0) {
            holder.tvUnreadCount.visibility = View.VISIBLE
            holder.tvUnreadCount.text       = user.unreadCount.toString()
        } else {
            holder.tvUnreadCount.visibility = View.GONE
        }

        holder.itemView.setOnClickListener { onClick(user) }
    }

    override fun getItemCount() = userList.size

    fun filter(query: String) {
        userList = if (query.isEmpty()) originalList
        else originalList.filter {
            it.name.contains(query, ignoreCase = true)
        }
        notifyDataSetChanged()
    }
}