package com.thaddeussoftware.tinge.tingeapi.philipsHue.controller.retrofitInterfaces

import com.thaddeussoftware.tinge.tingeapi.philipsHue.controller.json.JsonRoom
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface RoomsRetrofitInterface {
    @GET("api/{username}/groups")
    fun getAllLights(@Path("username") username: String): Single<Map<Int, JsonRoom>>
}