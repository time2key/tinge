package com.thaddeussoftware.tinge.database.phillipsHue.lights

import androidx.room.Dao
import androidx.room.Query
import io.reactivex.Single

@Dao
interface HueLightsDao {
    @Query("SELECT * FROM HueLight")
    fun getAllSavedHueLights(): Single<List<HueLightEntity>>

    //fun getSavedLightWithId(lightId: String): Single<HueLightEntity>

    //fun getAllSavedLightsForHub(): Single<List<HueLightEntity>>


}