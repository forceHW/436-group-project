package com.example.groupproject

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.groupproject.R

class StopDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stop_detail)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra("EXTRA_TITLE")

        // Show the stop ID
        val stopId = intent.getStringExtra("EXTRA_STOP_ID").orEmpty()
        findViewById<TextView>(R.id.stopIdView).text = "Stop ID: $stopId"

        // Grab the list of route IDs
        val routeIds = intent
            .getStringArrayListExtra("EXTRA_ROUTE_IDS")
            .orEmpty()

        // Display them
        val routesView = findViewById<TextView>(R.id.routesView)
        routesView.text = if (routeIds.isNotEmpty()) {
            routeIds.joinToString("\n")
        } else {
            "No routes serve this stop."
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

