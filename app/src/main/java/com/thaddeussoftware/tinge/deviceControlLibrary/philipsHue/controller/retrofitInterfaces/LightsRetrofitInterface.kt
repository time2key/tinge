package com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.controller.retrofitInterfaces

import com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.json.JsonLight
import io.reactivex.Completable
import io.reactivex.Single

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Created by thaddeusreason on 14/01/2018.
 */
interface LightsRetrofitInterface {

    @GET("api/{username}/lights")
    fun getAllLights(@Path("username") username: String): Single<Map<Int, JsonLight>>

    /**
     * Returns all lights and switches discovered the last time a scan for new lights was
     * performed, as well as info on when the last scan for new devices was (or whether a scan
     * is currently in progress)
     */
    //@GET("api/{username}/lights/new")
    //Call<Map<Integer, JsonLight>> getNewDevices(@Path("username") String username);

    //@POST("api/{username}/lights")
    //Call<JsonSearchForDevicesResponse> searchForNewDevices(@Path("username") String username);
    @GET("api/{username}/lights/{lightId}")
    fun getLight(@Path("username") username: String, @Path("lightId") lightId: String): Single<JsonLight>

    //@POST("api/{username}/lights/{lightId}")
    //Call<JsonRenameLightResponse> renameLight(@Path("username") String username, @Path("lightId") String lightId, @Body String renameLightBodyString);

    @PUT("api/{username}/lights/{lightId}/state")
    fun updateLightState(@Path("username") username: String, @Path("lightId") lightId: String, @Body jsonLightState: JsonLight.JsonState): Single<Any>

    //@DELETE("api/{username}/lights/{lightId}")
    //Call deleteLight(@Path("username") String username, @Path("lightId") String lightId);
}
