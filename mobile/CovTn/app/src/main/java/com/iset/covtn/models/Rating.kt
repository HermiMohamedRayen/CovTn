package com.iset.covtn.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Rating (
    val id: Long,
    val comment: String,
    val rating : Int,
    val user: User,

    val targetUser: User? = null
): Parcelable