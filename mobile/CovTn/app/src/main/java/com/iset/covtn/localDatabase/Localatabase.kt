package com.iset.covtn.localDatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.iset.covtn.localDatabase.converter.Converters
import com.iset.covtn.localDatabase.dao.DaoUser
import com.iset.covtn.models.CacheGeo
import com.iset.covtn.models.User

@Database(entities = [User::class, CacheGeo::class], version = 2, exportSchema = false)
@TypeConverters( Converters::class)
abstract class Localatabase : RoomDatabase() {

    abstract fun getUserDao() : DaoUser

    companion object{
        @Volatile
        private var INSTANCE: Localatabase? = null

        fun getDatabase(context: Context): Localatabase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    Localatabase::class.java,
                    "covtn_db",

                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}