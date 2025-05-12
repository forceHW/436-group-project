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

        val stopId = intent.getStringExtra("EXTRA_STOP_ID") ?: ""
        // find the TextView by ID:
        val stopIdView = findViewById<TextView>(R.id.stopIdView)
        stopIdView.text = "Stop ID: $stopId"
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
