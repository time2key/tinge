package com.thaddeussoftware.tinge.tingeapi.philipsHue.finder.retrofitInterfaces

import com.google.gson.annotations.SerializedName
import com.thaddeussoftware.tinge.tingeapi.internalnetworkingclasses.philipsHue.json.JsonUsernameResponse
import io.reactivex.Single

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Created by thaddeusreason on 14/01/2018.
 */
interface CredentialsObtainerRetrofitInterface {

    /**
     * Call this to get a new username/login details for the hue hub (i.e. when the app is
     * first opened, or when connecting a new additional hub). If the user has pressed the
     * button (within the last 30 seconds), a response containing the username will be
     * returned. Otherwise, a 101 response will be returned.
     */
    @POST("/api")//@get:POST
    fun newUsername(@Body bodyArguments: CredentialsObtainerRetrofitBodyArguments): Single<Response<JsonUsernameResponse>>


    data class CredentialsObtainerRetrofitBodyArguments(
            @Transient
            val applicationName: String = "Tinge",
            @Transient
            val deviceName: String = "Android"
    ) {
        @SerializedName("devicetype")
        val deviceType = "$applicationName#$deviceName"
    }
}
