package com.example.groupproject

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.groupproject.R
import com.google.firebase.auth.FirebaseAuth

class StopDetailActivity : AppCompatActivity() {
    private lateinit var favorites: Favorites
    private lateinit var button: Button
    private lateinit var back : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stop_detail)
        back = findViewById(R.id.backButton)
        back.setOnClickListener{ finish()}
        button = findViewById(R.id.favoriteButton)
        if (FirebaseAuth.getInstance().currentUser == null) {
            Toast.makeText(this, "Please sign in to favorite stops.", Toast.LENGTH_SHORT).show()
            button.isActivated = false
            return
        }else{
            button.isActivated = false
            favorites = Favorites()
        }
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
        var bruh : String = intent.getStringExtra("EXTRA_TITLE")!!
        var bruh2 : String = intent.getStringExtra("EXTRA_STOP_ID")!!
        findViewById<Button>(R.id.favoriteButton).setOnClickListener {
            favorites.addFavorite(bruh2, bruh) { error ->
                runOnUiThread {
                    if (error == null) {
                        Toast.makeText(
                            this,
                            "\"$bruh\" added to favorites!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            "Error adding to favorites:\n${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
