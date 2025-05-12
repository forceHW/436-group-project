package com.example.groupproject

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.SimpleAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView



//TODO rename activity 2 and items in nav bar
//TODO implement something meaningful
class StopHistory : AppCompatActivity() {

    private lateinit var clearButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_stop_history)

        updateHistoryList()
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

    fun updateHistoryList() {
        findViewById<ListView>(R.id.history).adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                MainActivity.history.getNames().mapIndexed { i, n ->
                    "$n\n${MainActivity.history.getTimestamps()[i]}"
                }
            )
    }
}