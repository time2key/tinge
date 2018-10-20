package com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.controller.retrofitInterfaces

import com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.controller.json.JsonRoom
import com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.json.JsonLight
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface RoomsRetrofitInterface {
    @GET("api/{username}/groups")
    fun getAllLights(@Path("username") username: String): Single<Map<Int, JsonRoom>>
}