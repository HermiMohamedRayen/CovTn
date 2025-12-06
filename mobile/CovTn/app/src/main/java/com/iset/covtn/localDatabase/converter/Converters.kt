package com.iset.covtn.localDatabase.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.iset.covtn.models.Adress
import com.iset.covtn.models.CacheGeo
import com.iset.covtn.models.Car
import com.iset.covtn.models.Ride

class Converters {
    val gson = Gson()
    @TypeConverter
    fun fromJsonAdress(value: String): Adress{
        return gson.fromJson(value, Adress::class.java)
    }

    @TypeConverter
    fun adressToJsonAdress(value : Adress): String{
        return gson.toJson(value)
    }

    @TypeConverter
    fun fromJsonCar(value : String) : Car?{
        return gson.fromJson(value,Car::class.java)
    }

    @TypeConverter
    fun carToJsonCar(value : Car?): String{
        return gson.toJson(value)
    }

}