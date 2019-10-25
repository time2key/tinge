package com.thaddeussoftware.tinge.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.thaddeussoftware.tinge.database.phillipsHue.hubs.HueHubsDao
import com.thaddeussoftware.tinge.database.phillipsHue.hubs.HueHubEntity
import com.thaddeussoftware.tinge.database.phillipsHue.lights.HueLightEntity
import com.thaddeussoftware.tinge.database.phillipsHue.lights.HueLightsDao

/**
 * Created by thaddeusreason on 19/01/2018.
 */
@Database(version = 1, entities = [HueHubEntity::class, HueLightEntity::class])
abstract class Database: RoomDatabase() {

    abstract fun hueHubsDao(): HueHubsDao

    abstract fun hueLightsDao(): HueLightsDao

}