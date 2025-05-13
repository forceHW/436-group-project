package com.example.groupproject

import android.util.Log
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import okhttp3.ResponseBody
import org.json.JSONArray
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

data class RouteInfo(
    val route_id: String,
    val title: String
)

data class StopsContainer(
    val stops: List<BusStopDetail>
)

data class StopsResponse(
    val data: StopsContainer
)


interface BusApiService {
    @GET("bus/stops")     //find bus stop titles
    fun getBusStops(): Call<List<BusStopInfo>>

    @GET("bus/stops/{stop_id}")   //allows us to retrieve bus stop lat lon data
    fun getBusStopDetails(@Path("stop_id") stopId: String): Call<List<BusStopDetail>>

    @GET("bus/routes")
    fun getAllRoutes(): Call<List<RouteInfo>>

    @GET("bus/routes/{route_ids}")
    fun getRouteStops(@Path("route_ids") routeIds: String): Call<StopsResponse>

    // NEW: raw JSON endpoint
    @GET("bus/routes/{route_ids}")
    fun getRoutesRaw(@Path("route_ids") routeIds: String): Call<ResponseBody>
}

class Locations(private val map: GoogleMap? = null) {

    private val retrofit = Retrofit.Builder()  //retrofit init
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
                        fetchStopDetails(stop.stop_id)  //use auxillary function to plot lat long of each bus
                    }
                } else {
                    Log.e("Locations", "API err ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<BusStopInfo>>, t: Throwable) {
                Log.e("Locations", "Network failure on AllBusStops: ${t.message}")
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

                        var hue = BitmapDescriptorFactory.HUE_RED
                        val histoyrIds = MainActivity.history.getIds()
                        if (histoyrIds.contains(detail.stop_id)) {
                             hue = BitmapDescriptorFactory.HUE_BLUE
                        }
                        val pos = LatLng(detail.lat, detail.long)
                        val marker = map?.addMarker(    //plot marker onto map
                            MarkerOptions()
                                .position(pos)
                                .title(detail.title)
                                .icon(BitmapDescriptorFactory.defaultMarker(hue))
                        )
                        marker?.tag = detail.stop_id
                    }
                } else {
                    Log.e("Locations", "API err2 $stopId: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<BusStopDetail>>, t: Throwable) {
                Log.e("Locations", "Network failure on StopDetails $stopId: ${t.message}")
            }
        })
    }


    fun getAllRouteIds(callback: (List<String>) -> Unit) {
        val TAG = "Locations"
        Log.d(TAG, "Fetching all route IDs…")

        api.getAllRoutes().enqueue(object : Callback<List<RouteInfo>> {
            override fun onResponse(
                call: Call<List<RouteInfo>>,
                response: Response<List<RouteInfo>>
            ) {
                if (!response.isSuccessful) {
                    Log.e(TAG, "getAllRoutes failed HTTP ${response.code()}")
                    callback(emptyList())
                    return
                }
                val routes = response.body().orEmpty()
                val ids = routes.map { it.route_id }
                Log.d(TAG, "Fetched route IDs: $ids")
                callback(ids)
            }

            override fun onFailure(call: Call<List<RouteInfo>>, t: Throwable) {
                Log.e(TAG, "getAllRoutes network error: ${t.message}")
                callback(emptyList())
            }
        })
    }



    /**
     * NEW METHOD — prints the full raw JSON returned by
     * GET /bus/routes/{route_ids}
     * for any list of IDs you pass in.
     */
    fun getRouteStopIds(
        routeIds: List<String>,
        callback: (Map<String, List<String>>) -> Unit
    ) {
        if (routeIds.isEmpty()) {
            callback(emptyMap())
            return
        }
        val joined = routeIds.joinToString(",")
        api.getRoutesRaw(joined).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val result = mutableMapOf<String, List<String>>()
                if (!response.isSuccessful) {
                    // on error, return empty lists for each route
                    routeIds.forEach { result[it] = emptyList() }
                    callback(result)
                    return
                }
                val raw = response.body()?.string().orEmpty()
                try {
                    val arr = JSONArray(raw)
                    // parse each route object in the array
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val rid = obj.getString("route_id")
                        val sa = obj.getJSONArray("stops")
                        val stops = List(sa.length()) { idx ->
                            sa.getString(idx)
                        }
                        result[rid] = stops
                    }
                } catch (e: Exception) {
                    // on parse error, empty lists
                    routeIds.forEach { result[it] = emptyList() }
                }
                callback(result)
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // on network failure, empty lists
                val result = routeIds.associateWith { emptyList<String>() }
                callback(result)
            }
        })
    }

}