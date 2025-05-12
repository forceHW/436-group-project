package com.example.groupproject

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.groupproject.R

class StopDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stop_detail)

        // Show Up arrow
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 1) Stop title
        val title = intent.getStringExtra("EXTRA_TITLE") ?: "Stop Details"
        supportActionBar?.title = title
        findViewById<TextView>(R.id.stopTitleView).text = title

        // 2) Route IDs
        val routeIds = intent.getStringArrayListExtra("EXTRA_ROUTE_IDS")
            ?: arrayListOf()
        val routesText = if (routeIds.isEmpty()) {
            "No routes serve this stop."
        } else {
            routeIds.joinToString(separator = "\n") { "- $it" }
        }
        findViewById<TextView>(R.id.routesListView).text = routesText
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
