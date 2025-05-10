package com.example.groupproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var map : GoogleMap
    lateinit var drawerLayout: DrawerLayout
    lateinit var actionBarToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer)    //nav bar initialization
        actionBarToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close)
        drawerLayout.addDrawerListener(actionBarToggle)
        actionBarToggle.syncState()
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)  //show nav bar icon


        val navView: NavigationView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener { menuItem ->
            if(menuItem.itemId == R.id.act2){
                val intent : Intent = Intent(this, act2::class.java)
                startActivity(intent)
                true
            }else{
                false
            }


        }


        var mapFragment : SupportMapFragment =
            supportFragmentManager.findFragmentById( R.id.map ) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    override fun onMapReady(p0: GoogleMap) {
        Log.w( "MainActivity", "Inside onMapRead" )
        map = p0

        var umd : LatLng = LatLng(38.98726435649732, -76.94266159109166)
        var camera : CameraUpdate = CameraUpdateFactory.newLatLngZoom( umd, 13.0f )
        map.moveCamera(  camera )


        val locations = Locations(map)
        locations.plotAllBusStops()



//        var mOptions : MarkerOptions = MarkerOptions( )
//        mOptions.position( umd )
//        mOptions.title( "UMD" )
//        mOptions.snippet( "HI" )
//        var marker : Marker? = map.addMarker( mOptions )
//        if( marker != null )
//            Log.w( "MainActivity", "id of marker is " + marker.id )


        var handler : ClickHandler = ClickHandler()
        map.setOnMarkerClickListener( handler )


    }

    inner class ClickHandler : GoogleMap.OnMarkerClickListener {
        override fun onMarkerClick(p0: Marker): Boolean {
            Log.w( "MainActivity", "Inside onMarkerClick" )
            Log.w( "MainActivity", "marker's id is " + p0.id )
            return false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (actionBarToggle.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }
}


