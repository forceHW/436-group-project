package com.example.groupproject

import android.util.Log
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

// --- Models ---
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

// --- Retrofit service ---
interface BusApiService {
    @GET("bus/stops")
    fun getBusStops(): Call<List<BusStopInfo>>

    @GET("bus/stops/{stop_id}")
    fun getBusStopDetails(@Path("stop_id") stopId: String): Call<List<BusStopDetail>>

    @GET("bus/routes")
    fun getAllRoutes(): Call<List<RouteInfo>>

    @GET("bus/routes/{route_ids}")
    fun getRouteStops(@Path("route_ids") routeIds: String): Call<StopsResponse>

    // NEW: raw JSON endpoint
    @GET("bus/routes/{route_ids}")
    fun getRoutesRaw(@Path("route_ids") routeIds: String): Call<ResponseBody>
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
                if (!response.isSuccessful) {
                    Log.e("Locations", "API err ${response.code()}")
                    return
                }
                val busStops = response.body() ?: return
                for (stop in busStops) {
                    fetchStopDetails(stop.stop_id)
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
                if (!response.isSuccessful) {
                    Log.e("Locations", "API err2 $stopId: ${response.code()}")
                    return
                }
                response.body()?.forEach { detail ->
                    val pos = LatLng(detail.lat, detail.long)
                    val marker = map.addMarker(
                        MarkerOptions()
                            .position(pos)
                            .title(detail.title)
                    )
                    marker?.tag = detail.stop_id
                }
            }

            override fun onFailure(call: Call<List<BusStopDetail>>, t: Throwable) {
                Log.e("Locations", "Network failure on StopDetails $stopId: ${t.message}")
            }
        })
    }

    fun getRoutesForStop(
        stopId: String,
        callback: (List<RouteInfo>) -> Unit
    ) {
        api.getAllRoutes().enqueue(object : Callback<List<RouteInfo>> {
            override fun onResponse(
                call: Call<List<RouteInfo>>,
                response: Response<List<RouteInfo>>
            ) {
                if (!response.isSuccessful) {
                    callback(emptyList())
                    return
                }
                val routes = response.body().orEmpty()
                if (routes.isEmpty()) {
                    callback(emptyList())
                    return
                }

                val matches = mutableListOf<RouteInfo>()
                var remaining = routes.size

                routes.forEach { route ->
                    api.getRouteStops(route.route_id)
                        .enqueue(object : Callback<StopsResponse> {
                            override fun onResponse(
                                call: Call<StopsResponse>,
                                resp: Response<StopsResponse>
                            ) {
                                if (resp.isSuccessful) {
                                    val stops = resp.body()?.data?.stops.orEmpty()
                                    if (stops.any { it.stop_id == stopId }) {
                                        matches.add(route)
                                    }
                                }
                                if (--remaining == 0) {
                                    callback(matches)
                                }
                            }

                            override fun onFailure(call: Call<StopsResponse>, t: Throwable) {
                                if (--remaining == 0) {
                                    callback(matches)
                                }
                            }
                        })
                }
            }

            override fun onFailure(call: Call<List<RouteInfo>>, t: Throwable) {
                callback(emptyList())
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
    fun printRouteStopIds(routeIds: List<String>) {
        if (routeIds.isEmpty()) {
            Log.w("RouteStops", "printRouteStopIds: no route IDs provided")
            return
        }
        val joined = routeIds.joinToString(",")
        api.getRoutesRaw(joined).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (!response.isSuccessful) {
                    Log.e("RouteStops", "HTTP ${response.code()} for routes [$joined]")
                    return
                }
                val raw = response.body()?.string().orEmpty()
                try {
                    // This endpoint returns a JSON array at the root
                    val arr = JSONArray(raw)
                    for (i in 0 until arr.length()) {
                        val routeObj: JSONObject = arr.getJSONObject(i)
                        val rid = routeObj.getString("route_id")
                        val stopsArr: JSONArray = routeObj.getJSONArray("stops")
                        val stops = mutableListOf<String>()
                        for (j in 0 until stopsArr.length()) {
                            stops += stopsArr.getString(j)
                        }
                        Log.i("RouteStops", "route $rid stops: $stops")
                    }
                } catch (e: Exception) {
                    Log.e("RouteStops", "Failed to parse JSON: ${e.message}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("RouteStops", "Network error fetching stops for [$joined]: ${t.message}")
            }
        })
    }
}
