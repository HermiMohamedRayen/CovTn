package com.iset.covtn.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
@Entity(tableName = "user")
@Parcelize
data class User(
    @PrimaryKey(autoGenerate = false)
    val email: String,
    val firstName: String,
    val lastName: String,
    val password: String?,
    var profilePicture: String?,
    val car : Car? = null,
    val number: String,


) : Parcelable {

    @Ignore
    var roles: HashSet<String>? = null

    @Ignore
    val ratings: List<Rating>? = null



    @IgnoredOnParcel
    var token: String = ""
}
