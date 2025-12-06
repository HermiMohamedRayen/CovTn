package com.iset.covtn.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import kotlinx.serialization.Transient
import java.util.Date


@Parcelize
data class Ride(
    var id: Long,
    var departure: GeoLocation,
    var destination: GeoLocation,
    var departureTime: Date,
    var arrivalTime: Date,
    var approved: Boolean,
    var driver: User,
    var rideParticipations : List<@RawValue Any>,

): Parcelable{

}
