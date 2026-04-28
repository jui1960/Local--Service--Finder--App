package com.example.serviceapp.View

import android.content.Context

class SearchHistoryManager(context: Context) {
    private val prefs = context.getSharedPreferences("search_history", Context.MODE_PRIVATE)
    private val KEY = "recent_searches"
    private val MAX = 6  // max recent searches to store

    // Get all recent searches (newest first)
    fun getHistory(): List<String> {
        val raw = prefs.getString(KEY, "") ?: ""
        return if (raw.isBlank()) emptyList()
        else raw.split("||").filter { it.isNotBlank() }
    }

    // Add a new search term
    fun addSearch(query: String) {
        if (query.isBlank()) return
        val list = getHistory().toMutableList()
        list.remove(query)       // remove duplicate if exists
        list.add(0, query)       // add to top
        val trimmed = list.take(MAX)
        prefs.edit().putString(KEY, trimmed.joinToString("||")).apply()
    }

    // Remove one specific search
    fun removeSearch(query: String) {
        val list = getHistory().toMutableList()
        list.remove(query)
        prefs.edit().putString(KEY, list.joinToString("||")).apply()
    }

    // Clear all history
    fun clearAll() {
        prefs.edit().remove(KEY).apply()
    }
}