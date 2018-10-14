package com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.finder.retrofitInterfaces

import com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.finder.json.JsonConfigurationDetails
import io.reactivex.Single
import retrofit2.http.GET

/**
 * Returns configuration information about the hue hub
 * */
interface GetConfigurationDetailsRetrofitInterface {

    /**
     * Get configuration details associated with the Bridge, such as its name and id.
     * */
    @GET("/api/0/config")
    fun getConfigurationDetails(): Single<JsonConfigurationDetails>
}