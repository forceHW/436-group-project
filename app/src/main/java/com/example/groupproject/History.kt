package com.example.groupproject

import android.content.Context
import android.content.SharedPreferences

class History {
    private var history: MutableList<String> = mutableListOf()

    constructor(context: Context) {
        val pref: SharedPreferences = context.getSharedPreferences(
            context.packageName + "_preferences",
            Context.MODE_PRIVATE
        )
        val historySet = pref.getStringSet(PREFERENCE_HISTORY, setOf()) ?: setOf()
        history = historySet.toMutableList()
    }

    constructor(existingHistory: List<String>) {
        history = existingHistory.toMutableList()
    }

    fun addLocation(location: String): Unit {
        if (location.isNotBlank()) {
            history.remove(location) // Avoid duplicates
            history.add(0, location) // Add to top
        }
    }

    fun getHistory(): List<String> {
        return history
    }

    fun clearHistory(): Unit {
        history.clear()
    }

    fun setPreferences(context: Context) {
        val pref: SharedPreferences = context.getSharedPreferences(context.packageName + "_preferences", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = pref.edit()
        editor.putStringSet(PREFERENCE_HISTORY, history.toSet())
        editor.commit()
    }

    companion object {
        private const val PREFERENCE_HISTORY: String = "location_history"
    }
}