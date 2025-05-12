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



//TODO rename activity 2 and items in nav bar
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

        historyList = findViewById(R.id.history)
        updateHistoryList()
        historyList.onItemClickListener =
            android.widget.AdapterView.OnItemClickListener { _, _, position, _ ->
                val title  = MainActivity.history.getNames()[position]
                intent = Intent(this, StopDetailActivity::class.java).apply {
                    putExtra("EXTRA_TITLE", title)
                }
                startActivity(intent)
            }

        setupGestureDetector()

        Toast.makeText(this,"Tap once for details, double‑tap to delete a stop.", Toast.LENGTH_LONG).show()

        val backButton = findViewById<Button>(R.id.backButton)

        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        clearButton = findViewById(R.id.clearButton)
        clearButton.setOnClickListener {
            MainActivity.history.clearAllLocations(this)
            updateHistoryList()
        }

        var adView : AdView = AdView( this ) //advertisement at bottom of screen
        var adSize: AdSize = AdSize(AdSize.FULL_WIDTH,AdSize.AUTO_HEIGHT)
        adView.setAdSize(adSize)

        var adUnitId : String = "ca-app-pub-3940256099942544/6300978111"
        adView.adUnitId = adUnitId
        var builder: AdRequest.Builder = AdRequest.Builder()
        builder.addKeyword("travel")
        var request: AdRequest = builder.build()

        var adLayout : LinearLayout = findViewById( R.id.adview )
        adLayout.addView( adView )
        adView.loadAd( request )

    }

    private fun updateHistoryList() {
        val rows = MainActivity.history.getNames()
            .zip(MainActivity.history.getTimestamps())
            .map { (n, t) -> "$n\n$t" }

        if (::adapter.isInitialized) {
            adapter.clear()
            adapter.addAll(rows)
            adapter.notifyDataSetChanged()
        } else {
            adapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,rows.toMutableList())
            historyList.adapter = adapter
        }
    }

    private fun setupGestureDetector() {
        gestures = GestureDetector(
            this,
            object : GestureDetector.SimpleOnGestureListener() {

                override fun onDoubleTap(e: MotionEvent): Boolean {
                    val pos = historyList.pointToPosition(e.x.toInt(), e.y.toInt())
                    if (pos != ListView.INVALID_POSITION) {
                        val title = MainActivity.history.getNames()[pos]
                        if (MainActivity.history.clearLocation(title)) {
                            MainActivity.history.setPreferences(this@StopHistory)
                            updateHistoryList()
                        }
                    }
                    return true
                }
            })
        historyList.setOnTouchListener { _, ev ->
            gestures.onTouchEvent(ev)
            false
        }
    }

}