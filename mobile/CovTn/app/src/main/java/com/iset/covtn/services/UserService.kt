package com.iset.covtn.services

import android.content.Context
import android.location.Geocoder
import android.util.Log
import android.widget.Toast
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.iset.covtn.CovTnApplication
import com.iset.covtn.models.CacheGeo
import com.iset.covtn.models.DateTypeAdapter
import com.iset.covtn.models.GeoLocation
import com.iset.covtn.models.Rating
import com.iset.covtn.models.Ride
import com.iset.covtn.models.RideParticipation
import com.iset.covtn.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class UserService {
    private val baseUrl = AuthServices.baseUrl
    private val gson = Gson()


    suspend fun getMyImage(context: Context,name : String,token : String) : File?{
        return withContext(Dispatchers.IO) {

                var file: File? = null
            try {

                val url = URI("${baseUrl}/user/profile/picture").toURL()
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"

                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                conn.setRequestProperty("Accept", "application/json")
                conn.setRequestProperty("Authorization", "Bearer ${token}")

                if (conn.responseCode in 200..299) {
                    file = File(context.filesDir, name)
                    conn.inputStream.use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }

                }
                conn.disconnect()
            }catch (e: Exception){
                Log.e("TAG", "getMyImage: ${e}")
            }
                file
        }

    }


/*

    suspend fun getLatestRides(token: String) : List<Ride>? {
        return withContext(Dispatchers.IO) {
            var list: List<Ride>? = null
            try {
                val url = URI("${baseUrl}/user/latestRide").toURL()
                val conn = url.openConnection() as HttpURLConnection
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                conn.setRequestProperty("Accept", "application/json")
                conn.setRequestProperty("Authorization", "Bearer ${token}")
                conn.requestMethod = "GET"
                if (conn.responseCode in 200..299) {
                    val rideListType = object : TypeToken<List<Ride>>() {}.type
                    list = gson.fromJson(
                        conn.inputStream.bufferedReader().use { it.readText() },
                        rideListType
                    )

                }
                conn.disconnect();
            }catch (e: Exception){
                Log.e("TAG", "getLatestRides: ${e}")
            }
            list;
        }

    }


 */



    suspend fun getDriver(email : String) : User?{
        return withContext(Dispatchers.IO) {
            var user : User? = null
            val url = URL("${baseUrl}/user/userInfo/${email}")
            try {
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")


                    connectTimeout = 8000
                    readTimeout = 8000
                }

                if (conn.responseCode in 200..299) {

                    val json = conn.inputStream.bufferedReader().use { it.readText() }


                    user = gson.fromJson(json, User::class.java)
                } else {
                    val error = conn.errorStream?.bufferedReader()?.use { it.readText() }
                    Log.e("GeoAPI", "Error: $error")
                }

                conn.disconnect()

            } catch (e: Exception) {
                Log.e("GeoAPI", "Exception: ${e.message}")
            }
            user
        }
    }


    suspend fun getUserImage(
        context: Context,
        name : String,
        userEmail : String,
        token: String
    ) : File?{
        return withContext(Dispatchers.IO){
            var file: File? = null
            try {
                val url = URI("${baseUrl}/user/userProfile/picture?email=${userEmail}").toURL()
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"

                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                conn.setRequestProperty("Accept", "application/json")
                conn.setRequestProperty("Authorization", "Bearer ${token}")

                if (conn.responseCode in 200..299) {
                    file = File(context.filesDir, name)
                    conn.inputStream.use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }

                }
                conn.disconnect()
            }catch (e: Exception){
                Log.e("TAG", "getUserImage: ${e}", )
            }
            file
        }
    }

    suspend fun getCarImage(
        context: Context,
        name : String,
        userEmail : String,
        token: String
    ) : File?{
        return withContext(Dispatchers.IO){
            var file: File? = null
            try {
                val url = URI("${baseUrl}/user/car/photo?email=${userEmail}&name=${name}").toURL()
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"

                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                conn.setRequestProperty("Accept", "application/json")
                conn.setRequestProperty("Authorization", "Bearer ${token}")

                if (conn.responseCode in 200..299) {
                    file = File(context.filesDir, name)
                    conn.inputStream.use { input ->
                        file.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }

                }
                conn.disconnect()
            }catch (e: Exception){
                Log.e("TAG", "getUserImage: ${e}", )
            }
            file
        }
    }








    companion object{
        @Volatile
        private var instance : UserService? = null


        fun getUserServices() : UserService{
            return instance ?: synchronized(this){
                val inst = UserService()
                instance = inst
                inst
            }
        }
    }




    fun tokenize(token : String) : String {
        return "Bearer ${token}"
    }


    interface UserApi{
        @GET("user/latestRide")
        suspend fun getLatestRide(@Header("Authorization") token : String) : Response<List<Ride>>

        @GET("auth/refreshToken")
        suspend fun refreshToken(@Header("Authorization") token : String) : Response<ResponseBody>

        @GET("user/searchRides")
        suspend fun searchRide(
            @Header("Authorization") token : String,
            @Query("deplat") deplat : Double,
            @Query("deplon") deplon : Double,
            @Query("destlat") destlat : Double,
            @Query("destlon") destlon : Double,
            @Query("depTime")  depTime : String,
            @Query("arrTime")  arrTime : String
        ): Response<List<Ride>>

        @GET("user/participations")
        suspend fun getMyParticipation(@Header("Authorization") token : String) : Response<List<RideParticipation>>

        @DELETE("user/ride/unparticipate/r/{ride_id}")
        suspend fun unParticipate(@Header("Authorization") token : String,@Path("ride_id") id : Long) : Response<Unit>

        @POST("user/comment")
        suspend fun addComment(@Header("Authorization") token : String,@Body comment : Rating) : Response<Unit>

        @POST("user/ride/participate/{id}")
        suspend fun participateIn(@Header("Authorization") token : String,@Path("id") id : Long) : Response<Unit>


        @Multipart
        @POST("driver/car") // Your endpoint URL
        suspend fun addCar(
            @Header("Authorization") token: String,
            @Part("car") car: RequestBody,
            @Part() files: List<MultipartBody.Part>
        ): Response<Void>

        @GET("user/becomeDriver")
        suspend fun becomeDriver(@Header("Authorization") token : String) : Response<Unit>

        @POST("driver/proposeRide")
        suspend fun proposeRide(
            @Header("Authorization") token : String,
            @Body ride : Ride
        ) : Response<Unit>

        @DELETE("driver/ride/{id}")
        suspend fun removeMyRide(
            @Header("Authorization") token : String,
            @Path("id") id : Long
            ) : Response<Unit>

        @GET("driver/rides")
        suspend fun getMyRides(
            @Header("Authorization") token : String
        ) : Response<List<Ride>>

        @PUT("user/phoneNumber/{phoneNumber}")
        suspend fun updatePhoneNumber(
            @Header("Authorization") token : String,
            @Path("phoneNumber") phoneNumber : String
        ) : Response<Unit>

        @Multipart
        @POST("user/profile/updatePicture")
        suspend fun updatepic(
            @Header("Authorization") token : String,
            @Part file : MultipartBody.Part
        ) : Response<ResponseBody>


    }

    object RetrofitInst{
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

        private val gsonn = GsonBuilder()
            .registerTypeAdapter(Date::class.java, DateTypeAdapter())
            .create()
        private val retrofil by lazy{
            Retrofit.Builder()
                .baseUrl("${AuthServices.baseUrl}/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gsonn))
                .build()
        }
        val clientapi : UserApi by lazy {
            retrofil.create(UserApi::class.java)
        }
    }



}