package com.iset.covtn.localDatabase.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.iset.covtn.models.CacheGeo
import com.iset.covtn.models.Ride
import com.iset.covtn.models.User


@Dao
interface DaoUser {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Query("select * from user limit 1")
    fun getUser() : LiveData<User>

    @Query("delete from user")
    suspend fun clear()

    @Query("select * from user limit 1")
    suspend fun getMe() : User?



    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGeo(geo : CacheGeo)


    @Query("select * from cachegeo where lat = :lat and lon = :lon limit 1")
    suspend fun getGeo(lat : String,lon : String) : CacheGeo?


    @Query("select * from cachegeo where lat = :lat and lon = :lon limit 1")
    fun getLiveGeo(lat : String,lon : String) : LiveData<CacheGeo>





}