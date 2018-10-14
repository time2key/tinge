package com.thaddeussoftware.tinge.deviceControlLibrary.utils.ssdp

import android.util.Log
import java.net.DatagramPacket

/**
 * A response from a device in response to a multicast ssdp M-SEARCH request.
 * Constructed with a response in the following format:
 * HTTP/1.1 200 OK
 * CACHE-CONTROL: max-age =
 * DATE:
 * EXT:
 * LOCATION:
 * SERVER:
 * ST:
 * USN:
 *
 * Created by thaddeusreason on 28/02/2018.
 */
class SsdpResponseFromDevice(resultDatagramPacket: DatagramPacket) {

    /**
     * The data string returned in the response
     * */
    var dataString: String
        private set

    /**
     * All headers returned in the response, such as "HOST", "CACHE-CONTROL" etc.
     *
     * All keys are stored uppercase trimmed of edge whitespace, regardless of the case they were
     * returned in.
     *
     * All values are trimmed of edge whitespace.
     * */
    var headersUpperCase = HashMap<String, String>()
        private set

    /**
     * The ip address that this response came from
     * */
    var ipAddress: String
        private set

    /** HOST header */
    val hostHeader = headersUpperCase["HOST"]

    /** ST header */
    val searchTargetHeader = headersUpperCase["ST"]

    /** LOCATION header */
    val locationHeader = headersUpperCase["LOCATION"]

    init {
        ipAddress = resultDatagramPacket.address.hostAddress.replace("/","")

        dataString = String(resultDatagramPacket.data)

        dataString.split("\n").forEach {
            line ->
            if (line.contains(":")) {
                val splitLine = line.split(":")
                headersUpperCase.put(splitLine[0].toUpperCase().trim(), splitLine[1].trim())
            }
        }

        Log.v("tinge", "SSDP response from ${resultDatagramPacket.address}: ${String(resultDatagramPacket.data)}")
    }

}