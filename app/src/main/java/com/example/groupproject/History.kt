package com.example.groupproject

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

class History {

    private var names: MutableList<String> = mutableListOf()
    private var times: MutableList<String> = mutableListOf()

    constructor(context: Context) {
        var pref: SharedPreferences =
            context.getSharedPreferences(context.packageName + "_preferences", Context.MODE_PRIVATE)

        var namesSet: Set<String>? = pref.getStringSet(PREFERENCE_NAMES, setOf())
        var timesSet: Set<String>? = pref.getStringSet(PREFERENCE_TIMES, setOf())

        names = namesSet?.toMutableList() ?: mutableListOf()
        times = timesSet?.toMutableList() ?: mutableListOf()
    }

    constructor(existingNames: List<String>, existingTimes: List<String>) {
        names = existingNames.toMutableList()
        times = existingTimes.toMutableList()
    }

    fun addLocation(newLocation: String) {
        if (newLocation.isNotBlank()) {
            val index = names.indexOf(newLocation)
            if (index != -1) {
                names.removeAt(index)
                times.removeAt(index)
            }
            names.add(0, newLocation)
            times.add(0, getCurrentTimestamp())
        }
    }

    fun getNames(): List<String> {
        return names
    }

    fun getTimestamps(): List<String> {
        return times
    }

    fun clearLocation(location: String): Boolean {
        val index = names.indexOf(location)
        return if (index != -1) {
            names.removeAt(index)
            times.removeAt(index)
            true                       // something was deleted
        } else {
            false                      // nothing matched
        }
    }

    fun setPreferences(context: Context) {
        var pref: SharedPreferences =
            context.getSharedPreferences(context.packageName + "_preferences", Context.MODE_PRIVATE)
        var editor: SharedPreferences.Editor = pref.edit()
        editor.putStringSet(PREFERENCE_NAMES, names.toSet())
        editor.putStringSet(PREFERENCE_TIMES, times.toSet())
        editor.commit()
    }

    private fun getCurrentTimestamp(): String {
        var sdf: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    companion object {
        private const val PREFERENCE_NAMES: String = "location_names"
        private const val PREFERENCE_TIMES: String = "location_times"
    }
}