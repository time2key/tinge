package com.thaddeussoftware.tinge.deviceControlLibrary.utils.ipScanner

import io.reactivex.Emitter
import io.reactivex.Flowable
import io.reactivex.functions.Consumer

/**
 * Class that assists in calculating IP addresses to scan when scanning the local WiFi network in
 * order to look for devices.
 *
 * To generate the flowable, call [searchableIpAddressesInSubnet]. It is safe to call this
 * method more than once.
 *
 * Created by thaddeusreason on 02/03/2018.
 */
class ScannableIpRangeCalculator(wifiDetails: WifiDetails) {

    /**
     * Permissible bits to change when searching, in order to limit the total number of devices
     * searched for (as attempting to establish connections to hundreds or thousands of ip
     * addresses in order to determine if they are valid devices is expensive).
     *
     * Bits marked with a 1 are permissible to search over, bits marked with a 0 are not (note that
     * this means this format is the inverse of the format used for [subnetMask])
     *
     * Most subnet masks encountered will specify that up to 256 devices can exist on the subnet.
     * However, for some networks, this could be as large as 256*256 = 65536 or greater valid ip
     * addresses on the subnet.
     * */
    var permissibleBitsToChangeWhenSearching = 0b00000000_00000000_00000001_11111111
        set(value) {
            field = value
            calculateMaskToSearch()
        }

    /**
     * The actual subnet mask of the current WiFi network, stored in an int format. Bits that are
     * part of the shared network and therefore should stay the same are marked with a 1, bits
     * that are not part of the network and therefore should be searched over are marked with a 0.
     *
     * (note that this is the inverse of the format stored in [maskToSearch] and
     * [permissibleBitsToChangeWhenSearching])
     *
     * Note that the actual range of ip addresses iterated over may be more limited than this - see
     * [maskToSearch]
     * */
    val subnetMask:Int = wifiDetails.netMask

    /**
     * The bits that will be modified in the current ip address when searching for devices on the
     * network. Bits marked with a 1 will be searched over, bits marked with an 0 will not.
     *
     * This may be a more limited set of ip addresses than the actual subnet mask in [subnetMask],
     * if the actual subnet mask is very large and therefore it is not practical to search over
     * all ip addresses in the range - see [permissibleBitsToChangeWhenSearching]
     * */
    var maskToSearch:Int = 0
        private set

    /**
     * iPv4 IP address of the current device
     *
     * This is stored in the bits of an int, see [getStringIpAddressFromInt]
     * */
    val ipAddressOfThisDeviceIPv4:Int = wifiDetails.ipAddressIPV4

    init {
        calculateMaskToSearch()
    }

    /*
    /**Get a list of all the ip addresses in the subnet to search for*/
    fun searchableIpAddressesInSubnet(): List<String> {
        val returnValue = ArrayList<String>()

        //The ip address component belonging to all devices in the searchable range
        val sharedIpAddressComponent = maskToSearch and ipAddressOfThisDeviceIPv4

        for (i in 0..maskToSearch) {
            val ipAddress = sharedIpAddressComponent or i
            if (ipAddress == ipAddressOfThisDeviceIPv4) continue
            returnValue.add(getStringIpAddressFromInt(ipAddress))
        }

        return returnValue
    }*/

    /**
     * Get a flowable that will return all ip addresses to search, one after the other when
     * requested.
     *
     * It is safe to call this more than once on the same instance.
     *
     * Implementation note:
     * This is implemented as a flowable as there are usually hundreds, sometimes thousands, of
     * ip addresses, and it would therefore block the calling thread, and allocate a lot of memory
     * at once, to calculate and return a list instead.
     * */
    fun searchableIpAddressesInSubnet() = Flowable.generate(object: Consumer<Emitter<String>> {
        /** The shared ip address component belonging to all devices in the searchable range */
        val sharedIpAddressComponent = maskToSearch.inv() and ipAddressOfThisDeviceIPv4
        /** The individual ip address component of the next ip address to be returned. Incremented
         * by 1 each time.*/
        var individualIpAddressComponent = 0
        /** Subnet mask used to search. Cached in case the value changes while the flowable is
         * still in progress */
        var cachedMaskToSearch = maskToSearch

        override fun accept(emitter: Emitter<String>) {
            if (individualIpAddressComponent-1 >= cachedMaskToSearch) {
                emitter.onComplete()
            } else {
                var ipAddress = sharedIpAddressComponent or individualIpAddressComponent
                if (ipAddress == ipAddressOfThisDeviceIPv4) {
                    individualIpAddressComponent++
                    ipAddress = sharedIpAddressComponent or individualIpAddressComponent
                }
                emitter.onNext(getStringIpAddressFromInt(ipAddress))
                individualIpAddressComponent++
            }
        }

    })

    /**
     * Ip addresses can be stored in bits of an int, and this function returns the string format of
     * an ip address given the int format.
     *
     * E.g. 255.255.255.0 could be stored as:
     * 11111111 11111111 11111111 00000000
     * */
    private fun getStringIpAddressFromInt(ipaddress: Int) =
            "${ipaddress.shr(24) and 0xff}.${ipaddress.shr(16) and 0xff}." +
                    "${ipaddress.shr(8) and 0xff}.${ipaddress and 0xff}"

    private fun calculateMaskToSearch() {
        maskToSearch = (permissibleBitsToChangeWhenSearching and subnetMask.inv())
    }
}