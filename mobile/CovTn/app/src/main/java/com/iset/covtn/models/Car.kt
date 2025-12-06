package com.iset.covtn.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Car (
    val id: Long,
    val matriculationNumber: String,
    val model: String,
    val photos: ArrayList<String>,
    val airConditioner: Boolean,
    val smoker: Boolean,
    val seats: Int
): Parcelable