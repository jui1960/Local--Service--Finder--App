package com.example.serviceapp.View

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.serviceapp.Adapter.ChatAdapter
import com.example.serviceapp.Model.ChatMessage
import com.example.serviceapp.Repository.AppRepository
import com.example.serviceapp.databinding.ActivityChatBinding
import com.google.firebase.auth.FirebaseAuth

class Chat : AppCompatActivity() {

    private lateinit var binding     : ActivityChatBinding
    private lateinit var chatAdapter : ChatAdapter
    private val messageList          = mutableListOf<ChatMessage>()
    private val repository           = AppRepository()

    private lateinit var chatId      : String
    private lateinit var otherUserId : String
    private lateinit var otherUserName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatId        = intent.getStringExtra("CHAT_ID") ?: ""
        otherUserId   = intent.getStringExtra("OTHER_USER_ID") ?: ""
        otherUserName = intent.getStringExtra("OTHER_USER_NAME") ?: "User"

        // UI Setup
        binding.tvChatUserName.text   = otherUserName
        binding.tvAvatarInitials.text = otherUserName.take(1).uppercase()

        setupRecyclerView()
        setupSendButton()
        setupToolbar()
        loadMessages()
    }

    private fun loadMessages() {
        if (chatId.isEmpty()) return

        val myUid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        repository.getMessages(chatId) { messages ->
            runOnUiThread {
                // message model-e isSentByMe logic-ti ekhane set kore nite hobe
                val updatedMessages = messages.map { msg ->
                    msg.copy(isSentByMe = msg.senderId == myUid)
                }

                messageList.clear()
                messageList.addAll(updatedMessages)
                chatAdapter.notifyDataSetChanged()

                if (messageList.isNotEmpty()) {
                    binding.rvChatMessages.scrollToPosition(messageList.size - 1)
                }
            }
        }
    }
    private fun setupSendButton() {
        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener

            binding.etMessage.text?.clear()


            repository.sendMessage(chatId, text, otherUserId, otherUserName)
        }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(messageList)
        binding.rvChatMessages.apply {
            adapter       = chatAdapter
            layoutManager = LinearLayoutManager(this@Chat).apply {
                stackFromEnd = true
            }
        }
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener { finish() }
    }
}