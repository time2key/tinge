package com.thaddeussoftware.tinge.tingeapi.generic.controller

/**
 * Created by thaddeusreason on 15/01/2018.
 */
open class HubResponseError constructor(val errorMessage: String){

    /**
     * Could not connect to the hub
     * */
    class CouldNotConnectToHubError(errorMessage: String) : HubResponseError(errorMessage)

    /**
     * Returned when:
     * Authorisation credentials sent as part of a request are not valid
     * A call to get authorisation credentials fails
     * */
    class NotAuthorisedError(errorMessage: String) : HubResponseError(errorMessage)

    /**
     * The hub could not connect to the given smart device
     * */
    class CouldNotConnectToDeviceError(errorMessage: String) : HubResponseError(errorMessage)
}