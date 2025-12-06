package com.iset.covtn

import android.app.Application
import com.iset.covtn.localDatabase.Localatabase
import com.iset.covtn.localDatabase.repository.Repository

class CovTnApplication : Application() {

    private val database by lazy{
        Localatabase.getDatabase(this)
    }

    val repository by lazy {
        Repository(database.getUserDao())
    }

}