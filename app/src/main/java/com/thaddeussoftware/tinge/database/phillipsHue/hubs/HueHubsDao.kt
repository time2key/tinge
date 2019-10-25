package com.thaddeussoftware.tinge.database.phillipsHue.hubs

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
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
