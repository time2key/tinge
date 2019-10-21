package com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.controller

import android.util.Log
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.thaddeussoftware.tinge.BuildConfig
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.controller.ControllerInternalStageableProperty
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.controller.HubController
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.controller.LightController
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.controller.LightGroupController
import com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.controller.json.JsonRoom
import com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.controller.retrofitInterfaces.LightsRetrofitInterface
import com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.controller.retrofitInterfaces.RoomsRetrofitInterface
import com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.json.JsonLight
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.RuntimeException
import java.net.SocketException

/**
 * A hue controller manages/interacts with a given hue bridge at a given IP address.
 * See [HubController].
 *
 * Created by thaddeusreason on 14/01/2018.
 */

class HueHubController constructor(
        private val hubIpAddress: String,
        private val hubId: String,
        private val hubName: String?,
        private val hubUsernameCredentials: String,
        private val retrofit: Retrofit = Retrofit.Builder()
                .baseUrl("http://$hubIpAddress")
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build(),
        private val lightsRetrofitInterface: LightsRetrofitInterface = retrofit.create(LightsRetrofitInterface::class.java),
        private val roomsRetrofitInterface: RoomsRetrofitInterface = retrofit.create(RoomsRetrofitInterface::class.java)
): HubController() {

    /**
     * Map of all lights currently found - used as the backing property for [lights].
     *
     * Map Key is the light id, Map Value is the [LightController] instance.
     * */
    private val lightsBackingMap = HashMap<Int, HueLightController>()


    override var name: ControllerInternalStageableProperty<String?> = ControllerInternalStageableProperty(hubName)

    override val hubController: HubController
        get() = this

    override val parentLightGroupController: LightGroupController?
        get() = null

    override val id: String
        get() = hubId

    override val ipAddress: String
        get() = hubIpAddress

    override val lightsInGroupOrSubgroups: List<LightController>
        get() = lightsBackingMap.values.toList()

    override val lightsNotInSubgroup: List<LightController>
        get() = ArrayList(0) // TODO add lights not in room

    private val roomsBackingList = HashMap<Int, HueRoomGroupController>()

    override val lightGroups: List<LightGroupController>
        get() {
            val list = ArrayList<LightGroupController>()
            roomsBackingList.values.forEach { list.add(it) }
            return list
        }

    /**
     * Sometimes, multiple api calls are made at the same time. If these both complete at the
     * same time, and both update properties, this can create problems such as
     * [ConcurrentModificationException]s. To prevent this, all completion events from
     * network calls are synchronised against this.
     * */
    private val synchronisationLockObject = ""

    fun performNetworkRefresh(): Completable {
        // Refresh:
        val lightsCompletable = lightsRetrofitInterface.getAllLights(hubUsernameCredentials)
                .subscribeOn(Schedulers.io())
                .flatMapCompletable { resultMap ->
                    synchronized(synchronisationLockObject) {
                        updateLightListToMatchLights(resultMap)
                    }
                    return@flatMapCompletable Completable.complete()
                }
        val roomsCompletable = roomsRetrofitInterface.getAllLights(hubUsernameCredentials)
                .subscribeOn(Schedulers.io())
                .flatMapCompletable { resultMap ->
                    synchronized(synchronisationLockObject) {
                        updateRoomListToMatchRooms(resultMap)
                    }
                    return@flatMapCompletable Completable.complete()
                }
        return Completable.mergeArrayDelayError(lightsCompletable, roomsCompletable)

        // Update:

    }

    private fun getCompletableForUpdatingEveryLightInThisGroup(): UpdateLightCompletableWithNumberOfZigbeeOperationsRequired {
        var numberOfOperationsPerformed = 0
        val completables = lightsInGroupOrSubgroups.mapNotNull {
            val result = (it as? HueLightController)?.completableForUpdatingLightAndNumberOfUpdatedProperties()
            numberOfOperationsPerformed += result?.numberOfZigbeeOperations ?: 0
            return@mapNotNull result?.completableForUpdatingLight
        }
        return UpdateLightCompletableWithNumberOfZigbeeOperationsRequired(
                Completable.mergeDelayError(completables),
                numberOfOperationsPerformed)
    }


    private var updateThreadRunnable: Runnable? = null

    private val SLEEP_TIME_PER_ZIGBEE_OPERATION_MS = 70
    private val INITIAL_THREAD_SLEEP_TIME_MS = 200
    private val SLEEP_TIME_IF_NO_ZIGBEE_OPERATIONS_MS = 70

    private val WAIT_TIME_BETWEEN_REFRESHING_LIGHTS_MS = 5000

    fun startUpdateThread() {
        updateThreadRunnable = object: Runnable {
            override fun run() {
                Thread.sleep(INITIAL_THREAD_SLEEP_TIME_MS.toLong())
                performNetworkRefresh().subscribe(
                        {
                            // Do nothing on complete
                        },
                        {
                            if (BuildConfig.DEBUG) throw RuntimeException(it)
                        }
                )

                while (updateThreadRunnable == this) {
                    val applyUpdatesResult = getCompletableForUpdatingEveryLightInThisGroup()
                    applyUpdatesResult.completableForUpdatingLight?.subscribe(
                            {
                                // Do nothing on complete
                            },
                            {
                                if (it is HttpException) {
                                    Log.e("tinge", "Response from server: ${it.response()}")
                                } else if (it is SocketException) {
                                    return@subscribe
                                }
                                if (BuildConfig.DEBUG) throw RuntimeException(it)
                            })
                    if (applyUpdatesResult.numberOfZigbeeOperations > 0) {
                        Thread.sleep((SLEEP_TIME_PER_ZIGBEE_OPERATION_MS * applyUpdatesResult.numberOfZigbeeOperations).toLong())
                    } else {
                        Thread.sleep(SLEEP_TIME_IF_NO_ZIGBEE_OPERATIONS_MS.toLong())
                    }
                }
            }
        }
        Thread(updateThreadRunnable).start()
    }

    fun stopUpdateThread() {
        updateThreadRunnable = null
    }

    /**
     * Called whenever the light list has been redownloaded from the hub.
     * Works out whether the lights have changed, and updates them as required, including calling
     * aggregate observables if appropriate.
     *
     * @param jsonLightsMap
     * Lights in the form they come from the Hue web api.
     * (Int id of light -> [JsonLight])
     * */
    private fun updateLightListToMatchLights(jsonLightsMap: Map<Int, JsonLight>) {
        var hasAnythingBeenAddedOrRemoved = false
        jsonLightsMap.forEach { mapEntry ->
            if (lightsBackingMap[mapEntry.key] == null) { // Add new light:
                lightsBackingMap[mapEntry.key] = HueLightController(this, mapEntry.key, mapEntry.value, lightsRetrofitInterface, hubUsernameCredentials)
                hasAnythingBeenAddedOrRemoved = true
            } else { // Update existing light:
                lightsBackingMap[mapEntry.key]?.jsonLight = mapEntry.value
                hasAnythingBeenAddedOrRemoved = true
            }
        }
        roomsBackingList.forEach {
            it.value.lightsMap = lightsBackingMap
        }
        if (hasAnythingBeenAddedOrRemoved) {
            onLightsOrSubgroupsAddedOrRemovedSingleLiveEvent.notifyChange()
        }
    }

    private fun updateRoomListToMatchRooms(rooms: Map<Int, JsonRoom>) {
        var hasAnythingBeenAddedOrRemoved = false
        rooms.forEach { roomEntry ->
            if (roomsBackingList[roomEntry.key] == null) { // Add new room:
                roomsBackingList[roomEntry.key] = HueRoomGroupController(this, lightsBackingMap, roomEntry.key, roomEntry.value)
                hasAnythingBeenAddedOrRemoved = true
            } else { // Update existing room:
                roomsBackingList[roomEntry.key]?.jsonRoom = roomEntry.value
                hasAnythingBeenAddedOrRemoved = true
            }
        }
        if (hasAnythingBeenAddedOrRemoved) {
            onLightsOrSubgroupsAddedOrRemovedSingleLiveEvent.notifyChange()
        }
    }

    /**
     * Called by a [HueLightController] when a value is staged for any property on it.
     *
     * This class then updates aggregate properties and calls events to reflect this change.
     * */
    fun onPropertyStagedFromLight(hueLightController: HueLightController) {
        onLightPropertyModifiedSingleLiveEvent.onEventStagedToHappen()
        onAnythingModifiedSingleLiveEvent.onEventStagedToHappen()
        Log.v("tinge", "live events updated for hub")
        lightGroups.forEach {
            if (it.lightsNotInSubgroup.contains(hueLightController)) {
                Log.v("tinge", "live events updated for room")
                it.onLightPropertyModifiedSingleLiveEvent.onEventStagedToHappen()
                it.onAnythingModifiedSingleLiveEvent.onEventStagedToHappen()
            }
        }
    }

    //TODO add in support for uniform... and average... properties

}
