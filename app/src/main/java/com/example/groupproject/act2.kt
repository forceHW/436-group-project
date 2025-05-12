package com.example.groupproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView



//TODO rename activity 2 and items in nav bar
//TODO implement something meaningful
class act2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_act2)

        val backButton = findViewById<Button>(R.id.backButton)

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

        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}