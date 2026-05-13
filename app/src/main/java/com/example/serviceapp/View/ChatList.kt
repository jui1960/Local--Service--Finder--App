package com.example.serviceapp.View

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.serviceapp.Adapter.ChatListAdapter
import com.example.serviceapp.Model.ChatUser
import com.example.serviceapp.databinding.ActivityChatListBinding

class ChatList : AppCompatActivity() {

    private lateinit var binding: ActivityChatListBinding
    private lateinit var adapter: ChatListAdapter

    private val userList = listOf(
        ChatUser("John Doe",   "JD", "That sounds perfect 🙌", "10:32 AM", 2),
        ChatUser("Sarah Khan", "SK", "See you tomorrow!",      "9:15 AM",  0),
        ChatUser("Rahim Uddin","RU", "Ok bhai, done ✅",       "Yesterday",5),
        ChatUser("Nadia Islam","NI", "Haha 😂 okay okay",      "Mon",      0),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupSearch()
    }

    private fun setupRecyclerView() {
        adapter = ChatListAdapter(userList) { user ->
            val intent = Intent(this, Chat::class.java)
            intent.putExtra("userName",     user.name)
            intent.putExtra("userInitials", user.initials)
            startActivity(intent)
        }

        binding.rvChatList.apply {
            adapter         = this@ChatList.adapter
            layoutManager   = LinearLayoutManager(this@ChatList)
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { adapter.filter(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }
}