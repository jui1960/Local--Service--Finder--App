package com.example.serviceapp.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.serviceapp.Model.ChatMessage
import com.example.serviceapp.R
import com.example.serviceapp.View.Chat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter(private val messageList: List<ChatMessage>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_SENT     = 1
        private const val TYPE_RECEIVED = 2
    }

    // Long → "10:30 AM" format
    private fun formatTime(timeMillis: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(timeMillis))
    }

    override fun getItemViewType(position: Int): Int {
        return if (messageList[position].isSentByMe) TYPE_SENT else TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SENT) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_sent, parent, false)
            SentViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_received, parent, false)
            ReceivedViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chatMessage = messageList[position]

        if (holder is SentViewHolder) {
            holder.tvMessage.text = chatMessage.message
            holder.tvTime.text    = formatTime(chatMessage.time)
        } else if (holder is ReceivedViewHolder) {
            holder.tvMessage.text  = chatMessage.message
            holder.tvTime.text     = formatTime(chatMessage.time)
            // Intent theke pawa name theke initials boshano
            holder.tvInitials.text = (holder.itemView.context as? Chat)?.intent?.getStringExtra("OTHER_USER_NAME")?.take(1)?.uppercase() ?: "?"
        }
    }
    override fun getItemCount() = messageList.size

    class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMessage : TextView = itemView.findViewById(R.id.tvMessage)
        val tvTime    : TextView = itemView.findViewById(R.id.tvTime)
    }

    class ReceivedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMessage  : TextView = itemView.findViewById(R.id.tvMessage)
        val tvTime     : TextView = itemView.findViewById(R.id.tvTime)
        val tvInitials : TextView = itemView.findViewById(R.id.tvInitials)
    }
}