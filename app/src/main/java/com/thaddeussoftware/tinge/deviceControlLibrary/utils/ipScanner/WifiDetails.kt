package com.thaddeussoftware.tinge.deviceControlLibrary.utils.ipScanner

/**
 * Provides details of the current WiFi connection: IP address and netmask
 *
 * Created by thaddeusreason on 10/03/2018.
 */
interface WifiDetails {

    /**
     * Current device ipV4 ip address of the current WiFi connection
     *
     * Stored in integer format - i.e. each of the four bytes in this int corresponds with one
     * of the four parts in the ip address.
     * */
    val ipAddressIPV4: Int

    /**
     * netMask of the current connection. Bits set to 1 indicate the part of the ip address that
     * corresponds to parts of the ip address that are shared between all devices on the network.
     * */
    val netMask: Int

}