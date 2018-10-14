package com.thaddeussoftware.tinge.database.phillipsHue.hubs

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import io.reactivex.Maybe
import io.reactivex.Single

/**
 * Created by thaddeusreason on 19/01/2018.
 */

@Dao
interface HueHubsDao {
    @Query("SELECT * FROM HueHub")
    fun getAllSavedHueHubs(): Single<List<HueHubEntity>>

    @Query("SELECT * FROM HueHub WHERE hub_id = :hubId")
    fun getSavedHubWithId(hubId: String): Maybe<HueHubEntity?>

    @Insert
    fun addHueHub(deviceHubEntity: HueHubEntity)

    @Delete
    fun deleteHueHub(deviceHubEntity: HueHubEntity)
}
