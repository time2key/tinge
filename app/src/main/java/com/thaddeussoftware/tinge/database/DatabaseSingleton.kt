package com.thaddeussoftware.tinge.database

import android.arch.persistence.room.Room
import android.content.Context
import com.thaddeussoftware.tinge.TingeApplication

object DatabaseSingleton {

    const val SINGLETON_DATABASE_NAME = "database"

    val database: Database by lazy {
        Room.databaseBuilder(
                TingeApplication.tingeApplication as Context,
                Database::class.java,
                SINGLETON_DATABASE_NAME).build()
    }
}