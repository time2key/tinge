package com.thaddeussoftware.tinge.tingeapi.philipsHue.finder.json

import com.google.gson.annotations.SerializedName

/**
 * Configuration details returned from the api associated with the bridge, such as its name and id.
 * */
class JsonConfigurationDetails {

    /**
     * Name of the bridge.
     * */
    @SerializedName("name")
    var name: String? = null

    /**
     * Version of the hue api in the format <major>.<minor>.<patch>
     * */
    @SerializedName("apiversion")
    var apiVersion: String? = null

    /**
     * Software version of the bridge.
     * */
    @SerializedName("swversion")
    var softwareVersion: String? = null

    /**
     * MAC address of the bridge.
     * */
    @SerializedName("mac")
    var macAddress: String? = null

    /**
     * Hardware model of the bridge (BSB001, BSB002).
     * */
    @SerializedName("modelid")
    var modelId: String? = null

    /**
     * The unique bridge id.
     * */
    @SerializedName("bridgeid")
    var bridgeId: String? = null

    /**
     * Indicates if the bridge settings are factory new.
     * */
    @SerializedName("factorynew")
    var factoryNew: Boolean? = null

    /**
     * If a bridge backup file has been restored on this bridge from a bridge with a different
     * bridgeid, it will indicate that bridgeid, otherwise null.
     * */
    @SerializedName("replacesbridgeid")
    var replacesBridgeId: String? = null

    /**
     * The version of the datastore.
     * */
    @SerializedName("datastoreversion")
    var datastoreVersion: String? = null

    /**
     * Name of the starterkit created in the factory.
     * */
    @SerializedName("starterkitid")
    var starterKitId: String? = null
}