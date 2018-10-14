package com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.finder.inner

import android.util.Log
import com.google.gson.GsonBuilder
import com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.finder.json.JsonConfigurationDetails
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.schedulers.Schedulers
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

/**
 * Connects to ip addresses provided with a flowable to detect whether they are hue hubs and
 * if so return details about them.
 *
 * This is done in a number of different threads - see [numberOfConcurrentConnections].
 *
 * Designed to be used to scan a large number of ip addresses, but can also be used to scan for
 * known hubs at given ip addresses, determined by the flowable provided as input.
 *
 * Created by thaddeusreason on 02/03/2018.
 */

class HueIpAddressScannerFinder() {

    /**
     * The number of concurrent threads to use for connecting to IP addresses
     * */
    val numberOfConcurrentConnections = 15

    /**
     * Gets a new observable for scanning ip addresses for hue bridges. A number of threads will be
     * used to do this, and the observable returned will be the merged result of all the threads.
     *
     * The returned observable will never call onError().
     * */
    fun scanIpAddressesForHueBridges(ipAddressFlowable: Flowable<String>): Observable<JsonConfigurationDetailsAndIpAddress> {
        val threads = ArrayList<Observable<JsonConfigurationDetailsAndIpAddress>>()

        for (i in 0..numberOfConcurrentConnections) {
            threads.add(getNewObservableForScanningIpAddresses(ipAddressFlowable).subscribeOn(Schedulers.io()))
        }

        return Observable.merge(threads)
    }

    /**
     * Get a single observable that connects to ip addresses to check if they are a hue bridge,
     * and outputs [JsonConfigurationDetails] instances whenever one is found.
     *
     * Multiple of these can be created and share the same flowable, in order to do the work on
     * multiple threads.
     *
     * @param ipAddressFlowable
     * This passed flowable should provide the ip addresses to attempt to connect to when requested.
     * Accessed in a blocking way so that multiple observables can take ip addresses one after the
     * other and connect to them.
     * */
    private fun getNewObservableForScanningIpAddresses(ipAddressFlowable: Flowable<String>) =
            Observable.create(ObservableOnSubscribe<JsonConfigurationDetailsAndIpAddress> { emitter ->
                val client = OkHttpClient()

                val gson = GsonBuilder().setLenient().create()

                while (true) {
                    var ipAddress = ""
                    try {
                        synchronized(this) {ipAddress = ipAddressFlowable.blockingFirst() }
                        Log.v("tinge","Scanning ip address $ipAddress")
                    } catch (e: Exception) {
                        Log.v("tinge","Ip address scanning finished")
                        emitter.onComplete()
                        break
                    }

                    try {
                        val request = Request.Builder()
                                .url("http://$ipAddress/description.xml")
                                .cacheControl(CacheControl.FORCE_NETWORK)
                                .build()

                        val response = client.newCall(request).execute()
                        emitter.onNext(
                                JsonConfigurationDetailsAndIpAddress(
                                        gson.fromJson(response.body()?.string(),
                                                JsonConfigurationDetails::class.java),
                                        ipAddress)
                                /*HueBridgeXmlDeviceDescription(ipAddress, response.body()?.string()
                                ?: "")*/)
                    } catch (e: IOException) {
                        //Could not connect - do nothing and next ip address will be scanned
                    }
                }

            })

    data class JsonConfigurationDetailsAndIpAddress(
            val jsonConfigurationDetails: JsonConfigurationDetails,
            val ipAddress: String)
}
