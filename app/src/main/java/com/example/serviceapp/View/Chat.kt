package com.example.serviceapp.View

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.serviceapp.Adapter.ChatAdapter
import com.example.serviceapp.Model.ChatMessage
import com.example.serviceapp.databinding.ActivityChatBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Chat : AppCompatActivity() {

    private lateinit var binding     : ActivityChatBinding
    private lateinit var chatAdapter : ChatAdapter
    private val messageList          = mutableListOf<ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Intent থেকে user data নেওয়া
        val userName     = intent.getStringExtra("userName")     ?: "User"
        val userInitials = intent.getStringExtra("userInitials") ?: "?"

        // Toolbar-এ set করা
        binding.tvChatUserName.text   = userName
        binding.tvAvatarInitials.text = userInitials

        setupRecyclerView()
        setupSendButton()
        setupToolbar()
        loadDummyMessages()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.btnCall.setOnClickListener { }
        binding.btnMore.setOnClickListener { }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(messageList)

        binding.rvChatMessages.apply {
            adapter       = chatAdapter
            layoutManager = LinearLayoutManager(this@Chat).also {
                it.stackFromEnd = true
            }
        }
    }

    private fun setupSendButton() {
        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener

            val newMessage = ChatMessage(
                message    = text,
                time       = getCurrentTime(),
                isSentByMe = true
            )
            messageList.add(newMessage)
            chatAdapter.notifyItemInserted(messageList.size - 1)
            binding.rvChatMessages.scrollToPosition(messageList.size - 1)
            binding.etMessage.text?.clear()
        }
    }

    private fun loadDummyMessages() {
        messageList.addAll(
            listOf(
                ChatMessage("Hey! Are you free this weekend? 👋", "10:28 AM", false),
                ChatMessage("Yeah, totally free on Saturday!",     "10:30 AM", true),
                ChatMessage("Maybe a hike then coffee after? ☕",  "10:31 AM", false),
                ChatMessage("That sounds perfect 🙌",              "10:32 AM", true),
            )
        )
        chatAdapter.notifyDataSetChanged()
        binding.rvChatMessages.scrollToPosition(messageList.size - 1)
    }

    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date())
    }
}