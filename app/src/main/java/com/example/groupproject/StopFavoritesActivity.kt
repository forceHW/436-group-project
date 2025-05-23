// StopFavoritesActivity.kt
package com.example.groupproject

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class StopFavoritesActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private val favorites = Favorites()
    private var listener: ValueEventListener? = null
    private lateinit var bar: TextView

    private val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private var currentItems: List<Favorites.FavoriteItem> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // redirect to login if not signed in
        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, Logins::class.java))
            Toast.makeText(this,"You need to be logged in to do that!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setContentView(R.layout.activity_favorites)
        bar = findViewById(R.id.topbar)
        val displayName = FirebaseAuth.getInstance().currentUser?.displayName
            ?: FirebaseAuth.getInstance().currentUser?.email
            ?: "Me"
        bar.text = "$displayName’s Favorites"

        listView = findViewById(R.id.favoritesView)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, pos, _ ->
            val item   = currentItems[pos]
            val stopId = item.id
            val title  = item.name

            val locations = Locations(map = null)

            locations.getAllRouteIds { allRouteIds ->
                if (allRouteIds.isEmpty()) {
                    Intent(this, StopDetailActivity::class.java).apply {
                        putExtra("EXTRA_STOP_ID",    stopId)
                        putExtra("EXTRA_TITLE",      title)
                        putStringArrayListExtra(
                            "EXTRA_ROUTE_IDS",
                            arrayListOf<String>()
                        )
                    }.also { startActivity(it) }
                } else {
                    locations.getRouteStopIds(allRouteIds) { stopMap ->
                        val matchingRoutes = stopMap
                            .filter { (_, stops) -> stops.contains(stopId) }
                            .keys
                            .toList()

                        Intent(this, StopDetailActivity::class.java).apply {
                            putExtra("EXTRA_STOP_ID",    stopId)
                            putExtra("EXTRA_TITLE",      title)
                            putStringArrayListExtra(
                                "EXTRA_ROUTE_IDS",
                                ArrayList(matchingRoutes)
                            )
                        }.also { startActivity(it) }
                    }
                }
            }
        }

        listView.setOnItemLongClickListener { _, _, pos, _ ->
            val item = currentItems[pos]
            favorites.removeFavorite(item.id) { success, err ->
                runOnUiThread {
                    if (success) {
                        Toast.makeText(
                            this,
                            "Removed “${item.name}” from favorites",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            "Error removing favorite: ${err?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            true
        }

        findViewById<Button>(R.id.clearButton).setOnClickListener {
            favorites.clearAllFavorites { err ->
                runOnUiThread {
                    if (err == null) {
                        Toast.makeText(this, "All favorites cleared", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(
                            this,
                            "Error clearing favorites: ${err.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        findViewById<Button>(R.id.backButton).setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        Toast.makeText(this, "Hold down an entry to remove it", Toast.LENGTH_LONG).show()
        listener = favorites.getFavorites({ items ->
            runOnUiThread {
                currentItems = items
                adapter.clear()
                adapter.addAll(items.map {
                    "${it.name}\n${fmt.format(Date(it.timestamp))}"
                })
                adapter.notifyDataSetChanged()
            }
        }, { error ->
            Toast.makeText(
                this,
                "Failed to load favorites: ${error.message}",
                Toast.LENGTH_LONG
            ).show()
        })
    }

    override fun onPause() {
        super.onPause()
        listener?.let {
            FirebaseAuth.getInstance()
                .currentUser
                ?.uid
                ?.let { uid ->
                    val ref = com.google.firebase.database.FirebaseDatabase
                        .getInstance()
                        .reference
                        .child("users")
                        .child(uid)
                        .child("favorites")
                    ref.removeEventListener(it)
                }
        }
    }
}
