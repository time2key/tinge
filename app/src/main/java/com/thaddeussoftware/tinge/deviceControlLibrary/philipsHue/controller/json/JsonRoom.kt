package com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.controller.json

import com.google.gson.annotations.SerializedName

class JsonRoom {
    @SerializedName("name")
    var name: String? = null

    @SerializedName("lights")
    var lightNumbersInBridge: List<Int>? = null
}