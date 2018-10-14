package com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.dagger

import com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.controller.HueHubController
import com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.finder.retrofitInterfaces.CredentialsObtainerRetrofitInterface
import com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.controller.retrofitInterfaces.LightsRetrofitInterface
import dagger.Component

/**
 * Created by thaddeusreason on 14/01/2018.
 */
@BaseBridgeUrlScope
@Component(
        modules = [
            HueControlLibraryModule::class
        ]
)
interface HueControlLibraryComponent {

    fun configurationService(): CredentialsObtainerRetrofitInterface

    fun lightsService(): LightsRetrofitInterface

    fun injectHueController(hueHubController: HueHubController)

}