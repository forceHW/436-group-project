package com.example.groupproject

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.navigation.NavigationView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar

class MapView(context: Context) : FrameLayout(context), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var locations: Locations
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarToggle: ActionBarDrawerToggle

    init {
        LayoutInflater.from(context).inflate(R.layout.activity_main, this, true)



        drawerLayout = findViewById(R.id.drawer)
        actionBarToggle = ActionBarDrawerToggle(context as MainActivity, drawerLayout, R.string.nav_open, R.string.nav_close)
        drawerLayout.addDrawerListener(actionBarToggle)
        actionBarToggle.syncState()

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        context.setSupportActionBar(toolbar)

        val mapFragment = (context as MainActivity).supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val navView: NavigationView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener { menuItem ->
            (context as MainActivity).handleNavItemClick(menuItem)
            true
        }
    }

    override fun onMapReady(p0: GoogleMap) {
        map = p0
        locations = Locations(map) //locations controller

        val umd = LatLng(38.98726435649732, -76.94266159109166)
        val camera = CameraUpdateFactory.newLatLngZoom(umd, 15.0f)
        map.moveCamera(camera)


        locations.plotAllBusStops()

        map.setOnMarkerClickListener { marker ->
            marker.showInfoWindow()
            true
        }

        map.setOnInfoWindowClickListener { marker ->
            val stopId = marker.tag as? String ?: return@setOnInfoWindowClickListener
            val title  = marker.title

            Intent(context, StopDetailActivity::class.java).apply {
                putExtra("EXTRA_STOP_ID", stopId)
                putExtra("EXTRA_TITLE",   title)
                context.startActivity(this)
            }
        }

    }
}
