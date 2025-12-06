package com.iset.covtn.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class RideParticipation(
    var id: Long,
    var rider: User,
    var ride: Ride
): Parcelable
