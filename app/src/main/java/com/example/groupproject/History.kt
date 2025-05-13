package com.example.groupproject

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

class History(context: Context) {

    private var names: MutableList<String> = mutableListOf<String>()
    private var times: MutableList<String> = mutableListOf<String>()
    private var ids: MutableList<String> = mutableListOf<String>()

    private var pref: SharedPreferences =
    context.getSharedPreferences(context.packageName + "_preferences", Context.MODE_PRIVATE)

    //constructs history from shared preferences
    init {
        names = loadList(PREFERENCE_NAMES)
        times = loadList(PREFERENCE_TIMES)
        ids   = loadList(PREFERENCE_IDS)
        val min = minOf(names.size, times.size, ids.size)
        if (names.size > min){
            names = names.subList(0, min)
        }
        if (times.size > min){
            times = times.subList(0, min)
        }
        if (ids.size   > min){
            ids   = ids.subList(0, min)
        }
    }

    //adds a new location to the history
    fun addLocation(id: String, name: String) {
        if (id.isNotBlank() && name.isNotBlank()) {
            val i = ids.indexOf(id)
            if (i != -1) {
                names.removeAt(i)
                times.removeAt(i)
                ids.removeAt(i)
            }
            names.add(0, name)
            times.add(0, getCurrentTimestamp())
            ids.add(0, id)
        }
    }

    //returns name of all locations in history
    fun getNames(): List<String> {
        return names
    }

    //returns time of all locations in history
    fun getTimes(): List<String> {
        return times
    }

    //returns id of all locations in history
    fun getIds(): List<String> {
        return ids
    }

    //clears a specific location from history
    fun clearLocation(id: String): Boolean {
        val curr = ids.indexOf(id)
        return if (curr == -1) {
            false
        } else {
            ids.removeAt(curr)
            names.removeAt(curr)
            times.removeAt(curr)
            true
        }
    }

    //clears all locations from history
    fun clearAllLocations() {
        names.clear()
        times.clear()
        ids.clear()
        setPreferences()
    }

    //saves history to shared preferences
    fun setPreferences() {
        pref.edit().apply {
            putString(PREFERENCE_NAMES, JSONArray(names).toString())
            putString(PREFERENCE_TIMES, JSONArray(times).toString())
            putString(PREFERENCE_IDS,   JSONArray(ids  ).toString())
        }.commit()
    }

    //returns current time as a string
    private fun getCurrentTimestamp(): String {
        var time: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return time.format(Date())
    }

    //helper function to load lists from shared preferences and keep organization
    private fun loadList(key: String): MutableList<String> {
        val all = pref.all[key]
        val json = when (all) {
            is String -> all
            is Set<*> -> JSONArray(all.toList()).toString()
                .also { pref.edit().putString(key, it).apply() }
            else -> "[]"
        }
        val arr = JSONArray(json)
        return MutableList(arr.length()) { i -> arr.getString(i) }
    }

    companion object {
        private const val PREFERENCE_IDS   = "location_ids"
        private const val PREFERENCE_NAMES: String = "location_names"
        private const val PREFERENCE_TIMES: String = "location_times"
    }
}