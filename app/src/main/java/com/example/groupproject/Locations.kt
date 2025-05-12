package com.example.groupproject

import android.util.Log
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

data class BusStopInfo(
    val stop_id: String,
    val title: String
)

data class BusStopDetail(
    val stop_id: String,
    val title: String,
    val lat: Double,
    val long: Double
)

interface BusApiService {
    @GET("bus/stops")
    fun getBusStops(): Call<List<BusStopInfo>>

    @GET("bus/stops/{stop_id}")
    fun getBusStopDetails(@Path("stop_id") stopId: String): Call<List<BusStopDetail>>
}

class Locations(private val map: GoogleMap) {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.umd.io/v1/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(BusApiService::class.java)

    fun plotAllBusStops() {
        api.getBusStops().enqueue(object : Callback<List<BusStopInfo>> {
            override fun onResponse(
                call: Call<List<BusStopInfo>>,
                response: Response<List<BusStopInfo>>
            ) {
                if (response.isSuccessful) {
                    val busStops = response.body() ?: return
                    //Log.d("Locations", "Fetched ${busStops.size} bus stops")

                    for (stop in busStops) {
                        fetchStopDetails(stop.stop_id)
                    }
                } else {
                    Log.e("Locations", "API error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<BusStopInfo>>, t: Throwable) {
                Log.e("Locations", "Network failure: ${t.message}")
            }
        })
    }

    private fun fetchStopDetails(stopId: String) {
        api.getBusStopDetails(stopId).enqueue(object : Callback<List<BusStopDetail>> {
            override fun onResponse(
                call: Call<List<BusStopDetail>>,
                response: Response<List<BusStopDetail>>
            ) {
                if (response.isSuccessful) {
                    val stopDetails = response.body()
                    stopDetails?.forEach { detail ->
                        val pos = LatLng(detail.lat, detail.long)
                        val marker = map.addMarker(
                            MarkerOptions()
                                .position(pos)
                                .title(detail.title)
                        )
                        marker?.tag = detail.stop_id
                    }
                } else {
                    Log.e("Locations", "Failed to fetch details for stop $stopId: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<BusStopDetail>>, t: Throwable) {
                Log.e("Locations", "Network failure for stop $stopId: ${t.message}")
            }
        })
    }
}