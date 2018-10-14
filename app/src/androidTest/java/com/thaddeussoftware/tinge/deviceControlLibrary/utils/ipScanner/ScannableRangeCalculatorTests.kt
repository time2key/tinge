package com.thaddeussoftware.tinge.deviceControlLibrary.utils.ipScanner

import io.reactivex.Flowable
import junit.framework.Assert.assertEquals
import org.junit.Test

/**
 * Created by thaddeusreason on 10/03/2018.
 */
class ScannableRangeCalculatorTests {

    /** This limit is used when converting flowables to array lists in case an error in generation
     * of the flowable causes an infinite or extremely large number of values to be generated,
     * causing the test to run indefinitely. This limit should never be reached.*/
    private val LARGE_LIMIT_TO_PREVENT_INFINITE_LOOP = 5000

    /**
     * 192.168.0.0, standard netmask, standard limit unchanged (1 byte greater than netmask)
     *
     * Output: 192.168.(1-255)
     * (192.168.0.0 not present as it is current device ip address)
     * */
    @Test
    fun searchableIpAddressesInSubnet_standardNetmaskSmallerThanLimit_correctIpAddressesReturned() {
        //Arrange:
        val wifiDetails = getWifiDetails(
                getIntIpAddressFromComponents(192, 168, 0, 0),
                0b11111111_11111111_11111111_00000000.toInt())
        val scannableRangeCalculator = ScannableIpRangeCalculator(wifiDetails)

        //Act:
        val searchableIpAddresses = scannableRangeCalculator.searchableIpAddressesInSubnet()

        val resultsFromFlowable = getArrayListFromFlowableResults(
                searchableIpAddresses, LARGE_LIMIT_TO_PREVENT_INFINITE_LOOP)

        //Assert:
        //Manually assert a few notable values in case there is logic error in loop below:
        //192.168.0.0 not present as it is the current ip address
        assertEquals("192.168.0.1", resultsFromFlowable[0])
        assertEquals("192.168.0.2", resultsFromFlowable[1])
        assertEquals("192.168.0.255", resultsFromFlowable.last())
        //Assert all values in loop:
        assertEquals(255, resultsFromFlowable.size)
        for (i in 0..254) assertEquals("192.168.0.${i+1}", resultsFromFlowable[i])
    }

    /**
     * Inputs: 192.168.0.27, large netmask, standard limit 9 bits
     *
     * Output: 192.168.0.(0-255 except 27) followed by 192.168.1.(0-255)
     * (192.168.0.27 not present as it is current device ip address)
     * */
    @Test
    fun searchableIpAddressesInSubnet_largeNetmaskLargerThanLimit_correctIpAddressesReturned() {
        //Arrange:
        val wifiDetails = getWifiDetails(
                getIntIpAddressFromComponents(192, 168, 0, 27),
                0b11111111_11111111_00000000_00000000.toInt())
        val scannableRangeCalculator = ScannableIpRangeCalculator(wifiDetails)
        scannableRangeCalculator.permissibleBitsToChangeWhenSearching = 0b00000000_00000000_00000001_11111111

        //Act:
        val searchableIpAddresses = scannableRangeCalculator.searchableIpAddressesInSubnet()

        val resultsFromFlowable = getArrayListFromFlowableResults(
                searchableIpAddresses, LARGE_LIMIT_TO_PREVENT_INFINITE_LOOP)

        //Assert:

        //Manually assert a few notable values in case there is logic error in loop below:
        assertEquals("192.168.0.0", resultsFromFlowable[0])
        assertEquals("192.168.0.26", resultsFromFlowable[26])
        //192.168.0.27 not present as it is the current ip address
        assertEquals("192.168.0.28", resultsFromFlowable[27])
        assertEquals("192.168.0.255", resultsFromFlowable[254])
        assertEquals("192.168.1.0", resultsFromFlowable[255])
        assertEquals("192.168.1.255", resultsFromFlowable.last())

        //Assert all values in loop:
        assertEquals(256*2-1, resultsFromFlowable.size)
        for (i in 0..26) assertEquals("192.168.0.${i}", resultsFromFlowable[i])
        for (i in 27..254) assertEquals("192.168.0.${i+1}", resultsFromFlowable[i])
        for (i in 255..510) assertEquals("192.168.1.${i-255}", resultsFromFlowable[i])
    }

    /**
     * Inputs: 255.255.255.254, large netmask, standard limit 9 bits
     *
     * Output: 255.255.254.(0-255) followed by 255.255.255.(0-255 except 254)
     * (255.255.225.254 not present as it is current device ip address)
     * */
    @Test
    fun searchableIpAddressesInSubnet_maxIpAddressWithlargeNetmaskLargerThanLimit_correctIpAddressesReturned() {
        //Arrange:
        val wifiDetails = getWifiDetails(
                getIntIpAddressFromComponents(255, 255, 255, 254),
                0b11111111_11111111_00000000_00000000.toInt())
        val scannableRangeCalculator = ScannableIpRangeCalculator(wifiDetails)
        scannableRangeCalculator.permissibleBitsToChangeWhenSearching = 0b00000000_00000000_00000001_11111111

        //Act:
        val searchableIpAddresses = scannableRangeCalculator.searchableIpAddressesInSubnet()

        val resultsFromFlowable = getArrayListFromFlowableResults(
                searchableIpAddresses, LARGE_LIMIT_TO_PREVENT_INFINITE_LOOP)

        //Assert:

        //Manually assert a few notable values in case there is logic error in loop below:
        assertEquals("255.255.254.0", resultsFromFlowable[0])
        assertEquals("255.255.254.255", resultsFromFlowable[255])
        assertEquals("255.255.255.0", resultsFromFlowable[256])
        assertEquals("255.255.255.253", resultsFromFlowable[256+253])
        //255.255.255.254
        assertEquals("255.255.255.255", resultsFromFlowable[256+254])

        //Assert all values in loop:
        assertEquals(256*2-1, resultsFromFlowable.size)
        for (i in 0..255) assertEquals("255.255.254.${i}", resultsFromFlowable[i])
        for (i in 256..256+253) assertEquals("255.255.255.${i-256}", resultsFromFlowable[i])
        assertEquals("255.255.255.255", resultsFromFlowable.last())
    }

    /**
     * Inputs: 1.2.3.133, 4 bit (half normal size) netmask, standard limit unchanged
     *
     * Output: 1.2.3.(128-143 except 133)
     * (1.2.3 not present as it is current device ip address)
     *
     * */
    @Test
    fun searchableIpAddressesInSubnet_smallNetmaskSmallerThanLimit_correctIpAddresssReturned() {
        //Arrange:
        val wifiDetails = getWifiDetails(
                getIntIpAddressFromComponents(1, 2, 3, 133),
                0b11111111_11111111_11111111_11110000.toInt())
        val scannableRangeCalculator = ScannableIpRangeCalculator(wifiDetails)

        //Act:
        val searchableIpAddresses = scannableRangeCalculator.searchableIpAddressesInSubnet()

        val resultsFromFlowable = getArrayListFromFlowableResults(
                searchableIpAddresses, LARGE_LIMIT_TO_PREVENT_INFINITE_LOOP)

        //Assert:
        //Last ip address number is 133, which is 128+4+1. 128 is outside of the bitmask to change,
        //so it is shared between all ip addresses returned:
        val sharedComponentOfLastIpAddressNumber = 128

        //Manually assert each value:
        assertEquals("1.2.3.128", resultsFromFlowable[0])
        assertEquals("1.2.3.129", resultsFromFlowable[1])
        assertEquals("1.2.3.132", resultsFromFlowable[4])
        //1.2.3.133 not present as it is the current ip address
        assertEquals("1.2.3.134", resultsFromFlowable[5])
        assertEquals("1.2.3.135", resultsFromFlowable[6])
        assertEquals("1.2.3.136", resultsFromFlowable[7])
        assertEquals("1.2.3.137", resultsFromFlowable[8])
        assertEquals("1.2.3.138", resultsFromFlowable[9])
        assertEquals("1.2.3.139", resultsFromFlowable[10])
        assertEquals("1.2.3.140", resultsFromFlowable[11])
        assertEquals("1.2.3.141", resultsFromFlowable[12])
        assertEquals("1.2.3.142", resultsFromFlowable[13])
        assertEquals("1.2.3.143", resultsFromFlowable[14])
        assertEquals(15, resultsFromFlowable.size)
    }

    private fun<T> getArrayListFromFlowableResults(flowable: Flowable<T>, limit: Int):ArrayList<T> {
        val resultsFromFlowable = ArrayList<T>()
        for (i in 0..limit) {
            try {
                resultsFromFlowable.add(flowable.blockingFirst())
            } catch (e:Exception) { break }
        }
        return resultsFromFlowable
    }

    private fun getWifiDetails(ipAddress:Int, subnetMask:Int) = object: WifiDetails {
        override val ipAddressIPV4: Int = ipAddress
        override val netMask:Int = subnetMask
    }

    private fun getIntIpAddressFromComponents(part1:Int, part2:Int, part3:Int, part4:Int): Int {
        return part4 + part3.shl(8) + part2.shl(16) + part1.shl(24)
    }
}