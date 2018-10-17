package com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.controller

import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.controller.ControllerInternalStageableProperty
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.controller.HubController
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.controller.LightController
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.controller.LightGroupController
import com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.controller.retrofitInterfaces.LightsRetrofitInterface
import com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.json.JsonLight
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

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
        private val lightsRetrofitInterface: LightsRetrofitInterface = retrofit.create(LightsRetrofitInterface::class.java)
): HubController() {

    /*@Inject
    lateinit var lightsRetrofitInterface: LightsRetrofitInterface

    @Inject
    lateinit var credentialsObtainerRetrofitInterface: CredentialsObtainerRetrofitInterface*/

    //init {

        /*val hueControlLibraryComponent =
                DaggerHueControlLibraryComponent.builder()
                        .hueControlLibraryModule(HueControlLibraryModule(hueBridgeBaseUrl))
                        .build()

        hueControlLibraryComponent.injectHueController(this)*/

        /*retrofit = Retrofit.Builder()
                .baseUrl(hueBridgeBaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        lightsRetrofitInterface = retrofit.create(LightsRetrofitInterface::class.java)

        credentialsObtainerRetrofitInterface = retrofit.create(CredentialsObtainerRetrofitInterface::class.java)*/
    //}


    /**
     * Map of all lights currently found - used as the backing property for [lights].
     *
     * Map Key is the light id, Map Value is the [LightController] instance.
     * */
    private val lightsBackingMap = HashMap<String, LightController>()


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

    override val lightGroups: List<LightGroupController>
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.


    override fun applyChanges(vararg dataInGroupTypes: DataInGroupType): Completable {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun refresh(vararg dataInGroupTypes: DataInGroupType): Completable {

        if (dataInGroupTypes.contains(DataInGroupType.LIGHTS)) {
            return lightsRetrofitInterface.getAllLights(hubUsernameCredentials)
                    .subscribeOn(Schedulers.io())
                    .flatMapCompletable { resultMap ->

                        updateLightListToMatchLights(resultMap)

                        Completable.complete()
                    }
        } else return Completable.complete()

    }

    private fun updateLightListToMatchLights(lights: Map<Int, JsonLight>) {
        lights.forEach { mapEntry ->
            //if (jsonLight.uniqueId == null) return@forEach

            if (lightsBackingMap[mapEntry.value.uniqueId!!] == null) { // Add new light:
                lightsBackingMap[mapEntry.value.uniqueId!!] = HueLightController(this, mapEntry.key, mapEntry.value, lightsRetrofitInterface, hubUsernameCredentials)
            } else { // Update existing light:

            }

        }
    }

}
