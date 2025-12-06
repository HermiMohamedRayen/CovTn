package com.iset.covtn.ui.viewModel

import android.Manifest
import android.R
import android.app.Notification
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.iset.covtn.localDatabase.dao.DaoUser
import com.iset.covtn.localDatabase.repository.Repository
import com.iset.covtn.models.Adress
import com.iset.covtn.models.CacheGeo
import com.iset.covtn.models.GeoLocation
import com.iset.covtn.models.Rating
import com.iset.covtn.models.Ride
import com.iset.covtn.models.User
import com.iset.covtn.services.AuthServices
import com.iset.covtn.services.MapServices
import com.iset.covtn.services.NotificationSSe
import com.iset.covtn.services.UserService
import com.iset.covtn.ui.home.MyParticipationsAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import java.io.File
import java.lang.Thread.sleep
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.collections.forEach

class UserViewModel(
    private val repository: Repository
) : ViewModel() {
    val user = repository.getUser()
    val userServices = UserService.getUserServices()

    private val _rides = MutableLiveData<List<Ride>>(mutableListOf<Ride>())

    val rides: LiveData<List<Ride>> = _rides

    private val _addresses = MutableLiveData<Map<GeoLocation, String>>(mutableMapOf())

    val addresses: LiveData<Map<GeoLocation, String>> = _addresses


    val drivers = mutableMapOf<String, User>()

    private val _image = MutableLiveData<File>(null)

    val image: LiveData<File> = _image

    private val _selectedRide = MutableLiveData<Ride>()

    val selectedRide: LiveData<Ride> = _selectedRide

    val mapService = MapServices.getMapServices()


    private val _errormsg = MutableLiveData("")
    val errormsg: LiveData<String> = _errormsg

    private val _success = MutableLiveData("")
    val success: LiveData<String> = _success

    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm")




    fun logout() {
        viewModelScope.launch {
            repository.clear()
        }
    }

    fun getImage(
        context: Context
    ) {
        viewModelScope.launch {
            val usr = repository.getMe()
            val image = usr?.profilePicture
            if (image == null) {
                return@launch
            }
            var file: File? = File(context.filesDir, image)
            if (file?.isFile ?: false) {
                _image.value = file
                return@launch
            }
            file =
                withContext(Dispatchers.IO) { userServices.getMyImage(context, image, usr.token) }
            if (file == null) {
                _errormsg.postValue("cannot retrieve profile picture")
                return@launch
            }
            _image.value = file
        }
    }

    fun getLatestRides() {
        viewModelScope.launch(Dispatchers.IO) {
            val usr = repository.getMe()
            try {
                if (usr != null) {
                    val resRides =
                        UserService.RetrofitInst.clientapi.getLatestRide(userServices.tokenize(usr.token))
                    if (resRides.isSuccessful && !resRides.body().isNullOrEmpty()) {

                        _rides.postValue(resRides.body())
                    }
                }
            } catch (e: Exception) {
                _errormsg.postValue("could not retrieve latest ride")
                Log.e("error", "getLatestRides error: ${e}")
            }
        }
    }

    fun getDisplayAddress(adr: CacheGeo): String {
        return when {
            !adr.address.suburb.isNullOrEmpty() -> adr.address.suburb
            !adr.address.county.isNullOrEmpty() -> adr.address.county
            !adr.address.city_district.isNullOrEmpty() -> adr.address.city_district
            !adr.address.city.isNullOrEmpty() -> adr.address.city
            !adr.name.isNullOrEmpty() -> adr.name
            else -> "Unknown" // fallback if all are null/empty
        }
    }

    fun updateAdresses(ride: Ride) {
        viewModelScope.launch {
            val current = _addresses.value!!.toMutableMap()
            val lat1 = String.format(Locale.US, "%.7f", ride.departure.latitude)
            val lon1 = String.format(Locale.US, "%.7f", ride.departure.longitude)

            val lat2 = String.format(Locale.US, "%.7f", ride.destination.latitude)
            val lon2 = String.format(Locale.US, "%.7f", ride.destination.longitude)

            val adr1 = getAddress(lat1, lon1)
            val adr2 = getAddress(lat2, lon2)
            current[GeoLocation(ride.departure.longitude, ride.departure.latitude)] =
                getDisplayAddress(adr1)
            current[GeoLocation(ride.destination.longitude, ride.destination.latitude)] =
                getDisplayAddress(adr2)
            _addresses.value = current

        }
    }

    private suspend fun getAddress(lat: String, lon: String): CacheGeo {
        var cacheGeo = CacheGeo(lat, lon, "not found", Adress(null, "not found", "", "", ""), null)
        var fromdb = repository.getGeo(lat, lon)
        if (fromdb == null) {
            fromdb = mapService.getAddress(lat, lon)
            if (fromdb != null) {
                fromdb.lat = lat
                fromdb.lon = lon
                cacheGeo = fromdb
                repository.inserGeo(fromdb)
            }
        } else {
            cacheGeo = fromdb
        }
        return cacheGeo
    }

    fun selectRide(ride: Ride) {
        _selectedRide.value = ride
    }

    fun fetchGeoCode(geo: GeoLocation): LiveData<CacheGeo> {
        val lt = String.format(Locale.US, "%.7f", geo.latitude)
        val lg = String.format(Locale.US, "%.7f", geo.longitude)
        return repository.fetchGeo(lt, lg)

    }

    fun getUserImage(
        context: Context,
        name: String?,
        userEmail: String,
        fileimg: MutableLiveData<File>
    ) {
        viewModelScope.launch {
            val usr = repository.getMe()
            if (usr == null || name == null) {
                return@launch
            }
            var file: File? = File(context.filesDir, name)
            if (file?.isFile ?: false) {
                fileimg.value = file
                return@launch
            }
            file = userServices.getUserImage(context, name, userEmail, usr.token)
            if (file == null) {
                _errormsg.postValue("cannot retrieve profile picture")
                return@launch
            }
            fileimg.value = file
        }
    }

    fun getCarImage(
        context: Context,
        name: String?,
        userEmail: String,
        fileimg: MutableLiveData<File>
    ) {
        viewModelScope.launch {
            val usr = repository.getMe()
            if (usr == null || name == null) {
                return@launch
            }
            var file: File? = File(context.filesDir, name)
            if (file?.isFile ?: false) {
                fileimg.value = file
                return@launch
            }
            file = userServices.getCarImage(context, name, userEmail, usr.token)
            if (file == null) {
                _errormsg.postValue("cannot retrieve car picture")
                return@launch
            }
            fileimg.value = file
        }
    }

    fun search(dep: LatLng, dest: LatLng, deptTime: Calendar, destTime: Calendar) {
        _rides.value = mutableListOf()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val usr = repository.getMe()
                if (usr != null) {
                    val res = UserService.RetrofitInst.clientapi.searchRide(
                        userServices.tokenize(usr.token),
                        dep.latitude,
                        dep.longitude,
                        dest.latitude,
                        dest.longitude,
                        formatter.format(deptTime.time),
                        formatter.format(destTime.time)
                    )
                    if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                        _rides.postValue(res.body())
                        return@launch
                    } else {
                        throw Exception(res.code().toString())
                    }
                }
            } catch (e: Exception) {
                _errormsg.postValue("could not retrieve search result")
                Log.e("error", "search error : ${e}")
            }

        }
    }

    fun refreshUserData() {
        viewModelScope.launch {
            val user = repository.getMe()
            if (user != null) {
                try {
                    val res = AuthServices.getAuthService().authentify(user.token)
                    if (res != null) {
                        val token = UserService.RetrofitInst.clientapi.refreshToken(
                            userServices.tokenize(user.token)
                        )
                        res.token = user.token
                        val str = token.body()?.string()
                        Log.i("TAG", "refreshUserData: ${str}")
                        if (token.isSuccessful && !str.isNullOrEmpty()) {
                            res.token = str
                        }
                        repository.insertUser(res)
                    }

                } catch (e: Exception) {
                    Log.e("TAG", "refreshUserData error: ${e}")
                }
            }
        }
    }

    fun getMyParticipation() {
        _rides.value = mutableListOf()
        viewModelScope.launch(Dispatchers.IO) {

            val user = repository.getMe()
            if (user == null) {
                return@launch
            }
            try {
                val res =
                    UserService.RetrofitInst.clientapi.getMyParticipation(userServices.tokenize(user.token))
                if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                    val ridespart = mutableListOf<Ride>()
                    res.body()?.forEach { participation ->
                        ridespart.add(participation.ride)
                    }
                    _rides.postValue(ridespart)
                }
            } catch (e: Exception) {
                Log.e("error", "getMyParticipation error: ${e}")
            }
        }
    }

    fun unParticipate(ride: Ride) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getMe()
            if (user == null) {
                return@launch
            }
            try {
                val res = UserService.RetrofitInst.clientapi.unParticipate(
                    userServices.tokenize(user.token),
                    ride.id
                )
                if (res.isSuccessful) {
                    withContext(Dispatchers.Main) { getMyParticipation() }
                    _success.postValue("Operation completed with success")
                } else if (res.code() == 410) {
                    _errormsg.postValue("cannot un-participate from an old ride !!!")
                } else {
                    throw Exception(res.code().toString())
                }
            } catch (e: Exception) {
                Log.e("error", "unParticipate: ${e}")
                _errormsg.postValue("cannot un-participate")
            }
        }
    }

    fun addComment(text: String, rate: Int) {

        viewModelScope.launch(Dispatchers.IO) {
            val usr = repository.getMe()
            if(selectedRide.value?.driver?.email.equals(usr?.email)){
                _errormsg.postValue("you cannot rate your self")
                return@launch
            }
            val rating = Rating(0, text, rate, usr!!, selectedRide.value?.driver)
            try {
                val res = UserService.RetrofitInst.clientapi.addComment(
                    userServices.tokenize(usr.token),
                    rating
                )
                if (res.isSuccessful) {
                    _success.postValue("comment added successfully")
                } else {
                    throw Exception(res.code().toString())
                }
            } catch (e: Exception) {
                Log.e("error", "addComment: ${e}")
                _errormsg.postValue("cannot add Comment")
            }
        }
    }

    fun participateIn() {
        viewModelScope.launch(Dispatchers.IO) {
            val usr = repository.getMe()
            if (usr == null) return@launch
            try {
                val res = UserService.RetrofitInst.clientapi.participateIn(
                    userServices.tokenize(usr.token),
                    selectedRide.value.id
                )
                if (res.isSuccessful) {
                    _success.postValue("you have been successfully participated")
                } else {
                    when (res.code()) {
                        406 -> _errormsg.postValue("you cannot participate in your own ride")
                        410 -> _errormsg.postValue("you cannot participate in an old ride")
                        409 -> _errormsg.postValue("you are already participated in this ride")
                        else -> throw Exception(res.code().toString())
                    }
                }
            } catch (e: Exception) {
                Log.e("error", "addComment: ${e}")
                _errormsg.postValue("failed while trying to add you")
            }
        }
    }

    fun addCar(
        token: String,
        car: okhttp3.RequestBody,
        files: List<MultipartBody.Part>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!UserService.RetrofitInst.clientapi.becomeDriver(userServices.tokenize(token)).isSuccessful) {
                    return@launch
                }
                val response = UserService.RetrofitInst.clientapi.addCar(
                    "Bearer $token",
                    car,
                    files
                )

                if (response.isSuccessful) {
                    _success.postValue("Car added successfully")
                } else {
                    val error = response.errorBody()?.string()
                    _errormsg.postValue("error while adding car")
                    Log.e("AddCar", "Error: ${response.code()} $error")
                }

            } catch (e: Exception) {
                _errormsg.postValue("error while adding car")
                Log.e("AddCar", "Exception", e)

            }
        }
    }

    fun addRide(dep: LatLng, dest: LatLng, depTime: Calendar, destTime: Calendar) {

        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getMe()
            if (user == null) return@launch
            try {
                val ride = Ride(
                    0,
                    GeoLocation(dep),
                    GeoLocation(dest),
                    depTime.time,
                    destTime.time,
                    false,
                    user,
                    mutableListOf()
                )
                val res = UserService.RetrofitInst.clientapi.proposeRide(
                    userServices.tokenize(user.token),
                    ride
                )
                if (res.isSuccessful) {
                    _success.postValue("ride added successfully")
                } else {
                    throw Exception(res.code().toString())
                }
            } catch (e: Exception) {
                Log.e("error", "addRide: ${e}")
                _errormsg.postValue("cannot add ride")
            }

        }


    }

    fun removeMyRide(ride: Ride) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getMe()
            if (user == null) return@launch
            try {
                val res = UserService.RetrofitInst.clientapi.removeMyRide(
                    userServices.tokenize(user.token),
                    ride.id
                )
                if (res.isSuccessful) {
                    getMyRides()
                    _success.postValue("ride removed successfully")
                } else {
                    throw Exception(res.code().toString())
                }
            } catch (e: Exception) {
                Log.e("error", "removeMyRide: ${e}")
                _errormsg.postValue("cannot remove ride")
            }
        }
    }

    fun getMyRides() {
        viewModelScope.launch(Dispatchers.IO) {
            _rides.postValue(mutableListOf())
            val user = repository.getMe()
            if (user == null) {
                return@launch
            }
            try {
                val res =
                    UserService.RetrofitInst.clientapi.getMyRides(userServices.tokenize(user.token))
                if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                    _rides.postValue(res.body())
                } else {
                    throw Exception(res.code().toString())
                }
            } catch (e: Exception) {
                Log.e("error", "getMyRides error: ${e}")
                _errormsg.postValue("you do not have any ride")
            }
        }
    }

    fun updatePhoneNumber(phoneNumber: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getMe()
            if (user == null) return@launch
            try {
                val res = UserService.RetrofitInst.clientapi.updatePhoneNumber(
                    userServices.tokenize(user.token),
                    phoneNumber
                )
                if (res.isSuccessful) {
                    _success.postValue("phone number updated successfully")
                } else {
                    throw Exception(res.code().toString())
                }
            } catch (e: Exception) {
                Log.e("error", "getLatestRides error: ${e}")
                _errormsg.postValue("cannot update phone number")
            }
        }
    }

    fun updateProfilePicture(file: MultipartBody.Part) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getMe()
            if (user == null) return@launch
            try {
                val res = UserService.RetrofitInst.clientapi.updatepic(
                    userServices.tokenize(user.token),
                    file
                )
                if (res.isSuccessful) {
                    _success.postValue("profile picture updated successfully")
                } else {
                    throw Exception(res.code().toString())
                }
            } catch (e: Exception) {
                Log.e("error", "getLatestRides error: ${e}")
                _errormsg.postValue("cannot update profile picture")
            }
        }
    }


    fun startSSE(context:Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val token = repository.getMe()?.token ?: return@launch

            val url = "${AuthServices.baseUrl}/notifications/stream?token=${token}"

            Log.e("TAG", "startSSE: ${url}" )

            val sseClient = NotificationSSe(
                url = url,
                onMessage = { msg ->
                    showTopNotification(context, "New notification", msg)
                },
                onError = { err ->
                    Log.e("SSE", err)
                }
            )

            sseClient.start()

        }
    }


    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showTopNotification(context: Context, title: String, message: String) {
        val builder = NotificationCompat.Builder(context, "sse_channel")
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)   // makes it appear on top
            .setCategory(Notification.CATEGORY_MESSAGE)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)

        val manager = NotificationManagerCompat.from(context)
        manager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    fun addError(msg: String) {
        _errormsg.value = msg
    }

}