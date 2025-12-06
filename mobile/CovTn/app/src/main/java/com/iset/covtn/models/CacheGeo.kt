package com.iset.covtn.models

import android.location.Address
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "cachegeo", primaryKeys = ["lat","lon"])
data class CacheGeo (

    var lat : String,
    var lon : String,

    val display_name : String,
    val address: Adress,

    val name : String?
): Parcelable