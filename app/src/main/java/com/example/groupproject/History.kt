package com.example.groupproject

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

class History {

    private var names: MutableList<String> = mutableListOf()
    private var times: MutableList<String> = mutableListOf()
    private var ids: MutableList<String> = mutableListOf()

    //constructs history from shared preferences
    constructor(context: Context) {
        var pref: SharedPreferences =
            context.getSharedPreferences(context.packageName + "_preferences", Context.MODE_PRIVATE)

        val idSet   = pref.getStringSet(PREFERENCE_IDS,emptySet()) ?: emptySet()
        val nameSet = pref.getStringSet(PREFERENCE_NAMES,emptySet()) ?: emptySet()
        val timeSet = pref.getStringSet(PREFERENCE_TIMES,emptySet()) ?: emptySet()

        ids   = LinkedHashSet(idSet ).toMutableList()
        names = LinkedHashSet(nameSet).toMutableList()
        times = LinkedHashSet(timeSet).toMutableList()


        //to make sure the lists are the same size
        val minSize = minOf(names.size, times.size, ids.size)
        if (names.size > minSize){
            names = names.subList(0, minSize)
        }
        if (times.size > minSize) {
            times = times.subList(0, minSize)
        }
        if (ids.size > minSize) {
            ids = ids.subList(0, minSize)
        }
    }

    //adds a new location to the history
    fun addLocation(id: String, newLocation: String) {
        if (newLocation.isNotBlank() || id.isNotBlank()) {
            val index = ids.indexOf(id)
            if (index != -1) {
                names.removeAt(index)
                times.removeAt(index)
                ids.removeAt(index)
            }
            names.add(0, newLocation)
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
        val index = ids.indexOf(id)
        if (index != -1) {
            names.removeAt(index)
            times.removeAt(index)
            ids.removeAt(index)
            return true
        } else{
            return false
        }
    }

    //clears all locations from history
    fun clearAllLocations(context: Context) {
        if (names.isNotEmpty() || times.isNotEmpty()) {
            names.clear()
            times.clear()
            ids.clear()
            setPreferences(context)
        }
    }

    //saves history to shared preferences
    fun setPreferences(context: Context) {
        var pref: SharedPreferences =
            context.getSharedPreferences(context.packageName + "_preferences", Context.MODE_PRIVATE)
        var editor: SharedPreferences.Editor = pref.edit()
        editor.putStringSet(PREFERENCE_NAMES, names.toSet())
        editor.putStringSet(PREFERENCE_TIMES, times.toSet())
        editor.putStringSet(PREFERENCE_IDS, ids.toSet())
        editor.commit()
    }

    //returns current time as a string
    private fun getCurrentTimestamp(): String {
        var time: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return time.format(Date())
    }

    companion object {
        private const val PREFERENCE_IDS   = "location_ids"
        private const val PREFERENCE_NAMES: String = "location_names"
        private const val PREFERENCE_TIMES: String = "location_times"
    }
}