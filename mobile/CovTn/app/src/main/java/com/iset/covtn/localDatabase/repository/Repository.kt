package com.iset.covtn.localDatabase.repository

import androidx.lifecycle.LiveData
import com.iset.covtn.localDatabase.dao.DaoUser
import com.iset.covtn.models.CacheGeo
import com.iset.covtn.models.User

class Repository(private val dao : DaoUser) {

    private val user = dao.getUser();

    suspend fun getMe() = dao.getMe()


    suspend fun getGeo(lat : String,lon : String) = dao.getGeo(lat,lon)

    suspend fun inserGeo(geo: CacheGeo){
        dao.insertGeo(geo)
    }
    suspend fun insertUser(user: User){
        dao.insert(user)
    }
    fun getUser(): LiveData<User>{
        return user

    }

    suspend fun clear(){
        dao.clear()
    }

    fun fetchGeo(lat: String,lon: String) = dao.getLiveGeo(lat,lon)
}