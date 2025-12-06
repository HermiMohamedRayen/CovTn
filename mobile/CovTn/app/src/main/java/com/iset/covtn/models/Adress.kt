package com.iset.covtn.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Adress (
    val county : String?,
    val city : String,
    val country: String,
    val suburb : String,
    val city_district : String
): Parcelable