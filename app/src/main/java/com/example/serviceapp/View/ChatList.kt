package com.example.serviceapp.View

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.serviceapp.Adapter.ChatListAdapter
import com.example.serviceapp.Model.ChatUser
import com.example.serviceapp.Repository.AppRepository
import com.example.serviceapp.databinding.ActivityChatListBinding

class ChatList : AppCompatActivity() {

    private lateinit var binding: ActivityChatListBinding
    private lateinit var adapter: ChatListAdapter
    private val repository = AppRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSearch()
        loadRealtimeChats()
    }

    private fun setupRecyclerView() {
        // Initials ba Name "User" jeno na hoy, tai adapter-e sothik data pathano hochche
        adapter = ChatListAdapter(emptyList()) { user ->
            val intent = Intent(this, Chat::class.java).apply {
                putExtra("CHAT_ID", user.chatId)
                putExtra("OTHER_USER_ID", user.otherUserId)
                // 🔥 Ekhane user.name thakay Chat Activity-r toolbar-e real name jabe
                putExtra("OTHER_USER_NAME", user.name)
            }
            startActivity(intent)
        }

        binding.rvChatList.apply {
            adapter = this@ChatList.adapter
            layoutManager = LinearLayoutManager(this@ChatList)
        }

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun loadRealtimeChats() {
        // Firebase theke data ashar por UI update hobe
        repository.listenToChatList { updatedList ->
            runOnUiThread {
                if (updatedList.isEmpty()) {
                    binding.rvChatList.visibility = View.GONE
                } else {
                    binding.rvChatList.visibility = View.VISIBLE
                    adapter.updateData(updatedList)
                }
            }
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                adapter.filter(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
}