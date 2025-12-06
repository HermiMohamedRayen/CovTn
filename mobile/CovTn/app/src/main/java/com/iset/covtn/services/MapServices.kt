package com.iset.covtn.services

import android.util.Log
import com.google.gson.Gson
import com.iset.covtn.models.CacheGeo
import com.iset.covtn.models.GeoLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class MapServices {
    fun geoLocDistance(loc1 : GeoLocation, loc2 : GeoLocation): Double {
        val latDistance = (loc2.latitude - loc1.latitude) * 111.32
        val lonDistance = (loc2.longitude - loc1.longitude) * 40075 * kotlin.math.cos(((loc1.latitude + loc2.latitude) / 2) * Math.PI / 180) / 360

        return kotlin.math.sqrt(latDistance * latDistance + lonDistance * lonDistance)
    }

    fun calculateZoomLevel(distanceKm: Double): Int {
        val z = kotlin.math.floor(kotlin.math.log2(distanceKm) + 0.000000000000001 / 5).toInt()
        return 14 - z
    }

    suspend fun getAddress(lat: String, lon: String): CacheGeo? {
        return withContext(Dispatchers.IO) {

            var result: CacheGeo? = null
            val url = URL("https://nominatim.openstreetmap.org/reverse?lat=$lat&lon=$lon&format=json")

            try {
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")

                    // REQUIRED BY NOMINATIM (Otherwise you get blocked)
                    setRequestProperty("User-Agent", "MyAndroidApp/1.0 tn@test.com")

                    setRequestProperty("Accept-Language","fr")

                    connectTimeout = 8000
                    readTimeout = 8000
                }

                if (conn.responseCode in 200..299) {

                    // Safely read input stream
                    val json = conn.inputStream.bufferedReader().use { it.readText() }


                    result = Gson().fromJson(json, CacheGeo::class.java)
                } else {
                    // Read error for debugging
                    val error = conn.errorStream?.bufferedReader()?.use { it.readText() }
                    Log.e("GeoAPI", "Error: $error")
                }

                conn.disconnect()

            } catch (e: Exception) {
                Log.e("GeoAPI", "Exception: ${e.message}")
            }

            result
        }
    }

    companion object{
        @Volatile
        private var instance : MapServices? = null
        fun getMapServices(): MapServices{
            return instance ?: synchronized(this){
                val inst = MapServices()
                instance = inst
                inst
            }
        }
    }

}