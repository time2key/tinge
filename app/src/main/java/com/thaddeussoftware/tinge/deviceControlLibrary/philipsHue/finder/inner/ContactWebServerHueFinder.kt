package com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.finder.inner

import com.google.gson.annotations.SerializedName
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

/**
 * Assists in finding Philips Hue devices on the local network by contacting the phillips hue server
 * and asking it for devices on the local network.
 *
 * Hue bridges periodically contact the Philips web server and register their internal and external
 * ip addresses with it. This queries the Philips web server for Hue Bridges with the same external
 * ip address, and returns their internal ip addresses (in addition to other information about
 * them).
 *
 * To use, call [getHueBridgesFromServer]. It is safe to call this method more than once on the
 * same instance.
 *
 * Created by thaddeusreason on 01/03/2018.
 */
class ContactWebServerHueFinder(
        /**
         * Base url of the hue site that this instance will contact to retrieve the list.
         * */
        hueUrl: String = "https://www.meethue.com",
        /**
         * Retrofit instance that will be used for accessing the hue site to retrieve the list.
         * */
        private val retrofitInterface: RetrofitInterface = Retrofit.Builder()
                .baseUrl(hueUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(RetrofitInterface::class.java)
) {

    /**
     * Returns an observable of device responses from the web server.
     *
     * It is safe to call this method more than once on the same instance.
     *
     * Implementation note:
     * Currently, all of the responses will be emitted at the same time, once all the downloading
     * and parsing has been done.
     * Despite this, the method has been written to return an Observable of devices rather than
     * a Single of List of devices for consistency with other methods in this api, and to allow
     * consumers to more easily use methods to combine multiple observers.
     * */
    fun getHueBridgesFromServer() = Observable.create(ObservableOnSubscribe<JsonWebServerHueFinderDevice> { emitter ->
        try {
            retrofitInterface.getHueBridgesWithSameExternalIpAddress()
                    .subscribeOn(Schedulers.newThread())
                    .subscribe(
                    { result ->
                        result.forEach { item ->
                            emitter.onNext(item)
                        }
                        emitter.onComplete()
                    },
                    { error -> emitter.onError(error) }
            )
        } catch (e: Exception) {
            emitter.onError(e)
        }
    })

    interface RetrofitInterface {
        @GET("/api/nupnp")
        fun getHueBridgesWithSameExternalIpAddress(): Single<Array<JsonWebServerHueFinderDevice>>
    }

    class JsonWebServerHueFinderDevice {
        /**Unique id of the bridge*/
        @SerializedName("id")
        var id: String? = null
            private set

        /**Internal ip address of the bridge on the local network*/
        @SerializedName("internalipaddress")
        var internalIpAddress: String? = null
            private set

        /**Name that has been given to this device. Can be null.*/
        @SerializedName("name")
        var name: String? = null
            private set
    }
}