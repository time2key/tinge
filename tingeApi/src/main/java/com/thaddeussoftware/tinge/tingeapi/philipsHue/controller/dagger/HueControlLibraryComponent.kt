package com.thaddeussoftware.tinge.tingeapi.philipsHue.dagger

import com.thaddeussoftware.tinge.tingeapi.philipsHue.controller.HueHubController
import com.thaddeussoftware.tinge.tingeapi.philipsHue.finder.retrofitInterfaces.CredentialsObtainerRetrofitInterface
import com.thaddeussoftware.tinge.tingeapi.internalnetworkingclasses.philipsHue.retrofitInterfaces.LightsRetrofitInterface
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