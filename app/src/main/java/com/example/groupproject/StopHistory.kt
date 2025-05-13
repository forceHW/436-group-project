package com.example.groupproject

import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView



//TODO rename items in nav bar
//TODO implement something meaningful
class StopHistory : AppCompatActivity() {

    private lateinit var clearButton: Button
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var gestures: GestureDetector
    private lateinit var historyList: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_stop_history)

        historyList = findViewById(R.id.historyView)
        updateHistoryList()


        //handles all possible gesture events for the history
        setupGestureDetector()
        historyList.setOnTouchListener { v, event ->
            if (gestures.onTouchEvent(event)) {
                true
            } else {
                v.onTouchEvent(event)
            }
        }

        //tells users how to use the history
        Toast.makeText(
            this,
            "Tap once for details, double‑tap to delete a stop.",
            Toast.LENGTH_LONG
        ).show()

        //allows users to go back to the map
        val backButton = findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        //clears all history
        clearButton = findViewById(R.id.clearButton)
        clearButton.setOnClickListener {
            MainActivity.history.clearAllLocations()
            updateHistoryList()
        }

        //handles banner ad at the bottom of the screen
        var adView: AdView = AdView(this) //advertisement at bottom of screen
        var adSize: AdSize = AdSize(AdSize.FULL_WIDTH, AdSize.AUTO_HEIGHT)
        adView.setAdSize(adSize)

        var adUnitId: String = "ca-app-pub-3940256099942544/6300978111"
        adView.adUnitId = adUnitId
        var builder: AdRequest.Builder = AdRequest.Builder()
        builder.addKeyword("travel")
        var request: AdRequest = builder.build()

        var adLayout: LinearLayout = findViewById(R.id.adview)
        adLayout.addView(adView)
        adView.loadAd(request)

    }

    //activates when activity is resumed
    override fun onResume() {
        super.onResume()
        updateHistoryList()
    }

    //actively updates the history list when changes are made
    private fun updateHistoryList() {
        val names = MainActivity.history.getNames()
        val times = MainActivity.history.getTimes()
        val rows = MutableList(names.size) { i -> "${names[i]}\n${times[i]}" }

        if (::adapter.isInitialized) {
            adapter.clear()
            adapter.addAll(rows)
            adapter.notifyDataSetChanged()
        } else {
            adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, rows.toMutableList())
            historyList.adapter = adapter
        }
    }

    //handles double tapping on a stop in history to delete and single tapping to view details
    private fun setupGestureDetector() {
        val listener = gestureListener()
        gestures = GestureDetector(this, listener).apply {
            setOnDoubleTapListener(listener)
        }
    }

    private inner class gestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean = true

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            val pos = historyList.pointToPosition(e.x.toInt(), e.y.toInt())
            if (pos != ListView.INVALID_POSITION) {
                val id = MainActivity.history.getIds()[pos]
                val title = MainActivity.history.getNames()[pos]
                val locations = Locations(map = null)

                locations.getAllRouteIds { allRouteIds ->
                    if (allRouteIds.isEmpty()) {
                        details(id, title, emptyList())
                        return@getAllRouteIds
                    }
                    locations.getRouteStopIds(allRouteIds) { mapOfStops ->
                        val matchingRoutes = mapOfStops.filter { (_, stops) ->
                            stops.contains(id)
                        }.keys.toList()
                        runOnUiThread {
                            details(id, title, matchingRoutes)
                        }
                    }
                }
            }
            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            val pos = historyList.pointToPosition(e.x.toInt(), e.y.toInt())
            if (pos != ListView.INVALID_POSITION) {
                val id = MainActivity.history.getIds()[pos]
                if (MainActivity.history.clearLocation(id)) {
                    MainActivity.history.setPreferences()
                    updateHistoryList()
                }
            }
            return true
        }
    }

    private fun details (id: String, title: String, routes: List<String>){
        val intent = Intent(this, StopDetailActivity::class.java).apply {
            putExtra("EXTRA_ID", id)
            putExtra("EXTRA_TITLE", title)
            putStringArrayListExtra("EXTRA_ROUTE_IDS", ArrayList(routes))
        }
        startActivity(intent)
    }
}