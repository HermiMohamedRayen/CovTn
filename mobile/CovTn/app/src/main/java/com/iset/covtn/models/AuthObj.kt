package com.iset.covtn.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize

class AuthObj (
    val email : String,
    val id : String,
    var code : String,
    var token : String?
) : Parcelable