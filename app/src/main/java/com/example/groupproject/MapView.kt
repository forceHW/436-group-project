package com.example.groupproject

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.util.Log
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory

class MapView(context: Context) : FrameLayout(context), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var locations: Locations
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarToggle: ActionBarDrawerToggle

    init {
        LayoutInflater.from(context).inflate(R.layout.activity_main, this, true)  //create child view, allows us to utilize all resources from main



//        drawerLayout = findViewById(R.id.drawer) //REDUNDAT CODE
//        actionBarToggle = ActionBarDrawerToggle(context as MainActivity, drawerLayout, R.string.nav_open, R.string.nav_close)
//        drawerLayout.addDrawerListener(actionBarToggle)
//        actionBarToggle.syncState()



        val mapFragment = (context as MainActivity).supportFragmentManager  //map initialization
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

        // **New**: on info‐window click, fetch all routes then print their stops
        map.setOnInfoWindowClickListener { marker ->
            val stopId = marker.tag as? String ?: return@setOnInfoWindowClickListener
            val title  = marker.title ?: "Stop Details"

            Log.d("MapView", "InfoWindow clicked for stopId=$stopId, title=$title")

            // add to history
            MainActivity.history.addLocation((marker.tag ?: "No ID") as String,marker.title ?: "Unnamed")
            MainActivity.history.setPreferences()
            Log.w("History", MainActivity.history.getNames().toString() + " " + MainActivity.history.getTimes().toString())

            //sets marker to blue
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))

            // 1) Fetch all route IDs
            locations.getAllRouteIds { allRouteIds ->
                Log.d("MapView", "All route IDs: $allRouteIds")

                if (allRouteIds.isEmpty()) {
                    Log.w("MapView", "No routes returned by API")
                    // still launch with empty list
                    Intent(context, StopDetailActivity::class.java).apply {
                        putExtra("EXTRA_STOP_ID", stopId)
                        putExtra("EXTRA_TITLE",   title)
                        putStringArrayListExtra("EXTRA_ROUTE_IDS", ArrayList<String>())
                        context.startActivity(this)
                    }
                } else {
                    // 2) Fetch every route's stops in one go
                    locations.getRouteStopIds(allRouteIds) { mapOfStops ->
                        // 3) Filter to only those routes whose stop list contains our stopId
                        val matchingRoutes = mapOfStops.filter { (_, stops) ->
                            containsString(stops, stopId)
                        }.keys.toList()

                        Log.d("MapView", "Routes serving $stopId: $matchingRoutes")

                        // 4) Launch detail activity with only the matching route IDs
                        Intent(context, StopDetailActivity::class.java).apply {
                            putExtra("EXTRA_STOP_ID", stopId)
                            putExtra("EXTRA_TITLE",   title)
                            putStringArrayListExtra(
                                "EXTRA_ROUTE_IDS",
                                ArrayList(matchingRoutes)
                            )
                            context.startActivity(this)
                        }
                    }
                }
            }

            true
        }

    }

    fun containsString(items: List<String>, target: String): Boolean {
        for (item in items) {
            if (item == target) return true
        }
        return false
    }
}
