package com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.finder

import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.finder.retrofitInterfaces.CredentialsObtainerRetrofitInterface
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.IllegalArgumentException
import java.util.concurrent.TimeUnit

/**
 * This class is responsible for repeatedly polling a hue bridge to get a response when the button
 * has been pressed, meaning that username credentials are available.
 *
 * To use this class:
 *
 * * Set the ipAddress via [ipAddress].
 *
 * * Observe [credentialsObservable] to get told when this instance has succesfully obtained
 * authorisation credentials from the hub.
 *
 * * [start] must be called initially to start scanning.
 *
 * * [pause] and [start] must be called to pause / resume this instance when the app goes into
 * the background/foreground.
 *
 * */
open class HueHubCredentialsObtainer(
        /**
         * Used to make web requests to try and obtain credentials
         * */
        private val hueHubCredentialsRequestMaker: HueHubCredentialsRequestMaker =
                HueHubCredentialsRequestMaker()
) {

    private val TRY_TO_ATTEMPTY_CREDENTIALS_EVERY_X_MS = 1_500

    /**
     * Ip address that will be repeatedly contacted to try and obtain authentication credentials.
     * It is safe to change this value while the class is running.
     * */
    var ipAddress: String? = null

    /**
     * Observable for the credentials:
     * * Will emit the username as a string if/when the hub returns it.
     * * Will not ever emit an error or complete.
     *
     * This is a hot observable.
     * */
    val credentialsObservable = Observable.create<String> { emitter ->
        credentialsObservableEmitter = emitter
    }


    private var credentialsObservableEmitter: ObservableEmitter<String>? = null

    private var isPaused = true

    private var repeatedlyTryToObtainUsernameDisposable: Disposable? = null


    /**
     * Starts / resumes the functionality of repeatedly polling the hub to obtain credentials.
     * The instance will initially be paused, so [start] must be called initially to start
     * scanning.
     * */
    fun start() {
        if (!isPaused) return
        isPaused = false

        repeatedlyTryToObtainUsernameDisposable = Observable.interval(
                TRY_TO_ATTEMPTY_CREDENTIALS_EVERY_X_MS.toLong(), TimeUnit.MILLISECONDS).doOnNext {

            // Cache this each time immediately before making the web request:
            val cachedIpAddress = ipAddress

            if (cachedIpAddress != null) {
                hueHubCredentialsRequestMaker.obtainUsernameTokenOrNullFrom(
                        cachedIpAddress)
                        ?.subscribeOn(Schedulers.io())
                        ?.subscribe(
                                { result ->
                                    // Check to make sure the ip address has not been changed since
                                    // making the web request, if it has then we don't return the
                                    // credentials:
                                    if (cachedIpAddress == ipAddress) {
                                        credentialsObservableEmitter?.onNext(result)
                                    }
                                },
                                { error ->

                                }
                        )
            }
        }.subscribe()
    }

    /**
     * Pauses the functionality of repeatedly polling the hub to obtain credentials. To resume,
     * call [start].
     * */
    fun pause() {
        if (isPaused) return
        isPaused = true

        repeatedlyTryToObtainUsernameDisposable?.dispose()
        repeatedlyTryToObtainUsernameDisposable = null
    }


    /**
     * This class is responsible for performing a single web request at a time to obtain a username
     * token from Hue Hubs.
     */
    open class HueHubCredentialsRequestMaker(
            /**
             * Retrofit builder instance - will be used to generate Retrofit instances when required.
             * Must have RxJava2CallAdapterFactory and GsonConverterFactor with lenient parsing enabled.
             * */
            private val retrofitBuilder: Retrofit.Builder = Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())) {

        /**
         * The response code returned if the user has not pressed the button, so no username token is
         * provided.
         * */
        private val RESPONSE_CODE_NO_USERNAME = 101


        private var previousIpAddress: String? = null

        private var previousRetrofitInterface: CredentialsObtainerRetrofitInterface? = null


        /**
         * Connects to the hub at the given ip address to get a username token.
         *
         * @param ipAddress
         * The ip address to get the token from. If there is no prefix added to it (e.g. http://),
         * this will be prefixed with http:// before connecting.
         *
         * @return
         * A [Single] that will complete with the username as a string if successful, or an error
         * otherwise.
         * If an unspecified exception occurs, null will be returned.
         * */
        open fun obtainUsernameTokenOrNullFrom(ipAddress: String): Single<String>? {
            try {
                var ipAddress = ipAddress
                if (!ipAddress.contains("://")) { ipAddress = "http://$ipAddress" }

                if (previousIpAddress != ipAddress || previousRetrofitInterface == null) {
                    // Previous retrofit interface cannot be reused - make new one:

                    previousIpAddress = ipAddress

                    previousRetrofitInterface =
                            retrofitBuilder
                                    .baseUrl(ipAddress)
                                    .build().create(CredentialsObtainerRetrofitInterface::class.java)
                }

                return previousRetrofitInterface!!.newUsername(CredentialsObtainerRetrofitInterface.CredentialsObtainerRetrofitBodyArguments()).map { response ->
                    if (response.code() == RESPONSE_CODE_NO_USERNAME) {
                        //Will cause onError in RxJava:
                        throw RuntimeException(
                                "Could not obtain credentials - button on bridge not pressed yet")
                    } else response.body()?.username
                }
            } catch (e: IllegalArgumentException) {
                // Can occur if url invalid:
                return null
            }
        }
    }
}