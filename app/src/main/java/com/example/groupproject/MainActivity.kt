package com.example.groupproject

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout

class MainActivity : AppCompatActivity() {

    private lateinit var mapView: MapView
    lateinit var actionBarToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {

        history = History(this)
        super.onCreate(savedInstanceState)

        mapView = MapView(this)
        setContentView(mapView)


        val drawerLayout: DrawerLayout = findViewById(R.id.drawer)   //nav bar initialization + onclick init
        actionBarToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close)
        drawerLayout.addDrawerListener(actionBarToggle)
        actionBarToggle.syncState()

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)  //nav bar icon
    }

    fun handleNavItemClick(menuItem: MenuItem): Boolean {  //items in nav menu onlcick
        return when (menuItem.itemId) {
            R.id.act2 -> {
                val intent = Intent(this, StopHistory::class.java)
                startActivity(intent)
                true
            }

            R.id.act3 ->{
                val intent = Intent(this, Logins::class.java)
                startActivity(intent)
                true
            }

            R.id.act4 ->{
                val intent = Intent(this, StopFavoritesActivity::class.java)
                startActivity(intent)
                true
            }
            else -> false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (actionBarToggle.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }

    companion object {
        lateinit var history: History
    }
}
