package com.thaddeussoftware.tinge.database.phillipsHue.lights

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import io.reactivex.Single

@Dao
interface HueLightsDao {
    @Query("SELECT * FROM HueLight")
    fun getAllSavedHueLights(): Single<List<HueLightEntity>>

    //fun getSavedLightWithId(lightId: String): Single<HueLightEntity>

    //fun getAllSavedLightsForHub(): Single<List<HueLightEntity>>


}