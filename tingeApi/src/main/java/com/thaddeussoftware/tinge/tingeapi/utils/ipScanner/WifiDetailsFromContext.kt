package com.thaddeussoftware.tinge.tingeapi.utils.ipScanner

import android.content.Context
import android.net.wifi.WifiManager

/**
 * Provides current Wifi connection details from a given [Context] instance
 *
 * Created by thaddeusreason on 10/03/2018.
 */
class WifiDetailsFromContext(val context: Context): WifiDetails {

    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    override val ipAddressIPV4: Int = reverseBytes(wifiManager.connectionInfo.ipAddress)

    override val netMask: Int = reverseBytes(wifiManager.dhcpInfo.netmask)

    private fun reverseBytes(inputBytes:Int):Int {
        //Get the four sections/bytes in the returned, correct format address:
        val correctOrderSection1 = inputBytes and 0xff
        val correctOrderSection2 = inputBytes.shr(8) and 0xff
        val correctOrderSection3 = inputBytes.shr(16) and 0xff
        val correctOrderSection4 = inputBytes.shr(24) and 0xff

        return correctOrderSection1.shl(24) or correctOrderSection2.shl(16) or
                correctOrderSection3.shl(8) or correctOrderSection4
    }
}