package com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.json

/**
 * Created by thaddeusreason on 13/01/2018.
 */

class JsonUsernameResponse : ArrayList<JsonUsernameResponse.JsonInnerUsernameResponse>() {

    /**
     * The username returned from the bridge. This should be stored and used to make subsequent
     * api calls.
     * */
    val username: String?
        get() = if(size>0) get(0).success?.username else null

    class JsonInnerUsernameResponse {

        var success: JsonInnerSuccessUsernameResponse? = null
            private set

        class JsonInnerSuccessUsernameResponse {
            /**The username returned from the bridge*/
            var username: String? = null
        }
    }
}
