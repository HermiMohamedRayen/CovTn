package com.iset.covtn.models

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import kotlinx.parcelize.Parcelize

@Parcelize
data class GeoLocation(
    val longitude : Double,
    val latitude : Double
): Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GeoLocation

        if (longitude != other.longitude) return false
        if (latitude != other.latitude) return false

        return true
    }

    override fun hashCode(): Int {
        var result = longitude.hashCode()
        result = 31 * result + latitude.hashCode()
        return result
    }

    fun toLatLng() : LatLng{
        return LatLng(latitude,longitude)
    }
    constructor( latLng: LatLng) : this(latLng.longitude,latLng.latitude)
}