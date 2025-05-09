package com.example.groupproject

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var map : GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


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
}


