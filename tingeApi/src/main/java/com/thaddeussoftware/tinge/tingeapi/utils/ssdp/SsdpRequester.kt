package com.thaddeussoftware.tinge.tingeapi.utils.ssdp

import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.schedulers.Schedulers
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException


/**
 * Makes SSDP multicast requests to find devices.
 *
 * To use, optionally setup properties and then call [beginSearchForDevices]. It is safe to retain
 * the instance to call [beginSearchForDevices] on it again later.
 *
 * Created by thaddeusreason on 27/02/2018.
 */

class SsdpRequester {
    /**
     * Special IP address used for multicasting - this will cause the router to multicast the
     * request to all devices on the network.
     * */
    val MULTICAST_IP_ADDRESS = "239.255.255.250"
    /**
     * Port to use for multicasting - see [MULTICAST_IP_ADDRESS]
     * */
    val MULTICAST_PORT = 1900

    /**
     * When sending out a multicast request, devices are asked to send their response at a random
     * time between 0 and this specified number of seconds. This prevents all responses being
     * received at once and flooding the searcher.
     * */
    var secondsToRequestThatRespondersDelayResponses = 2
    /**
     * The searcher will wait this many seconds for devices to respond. This number should be
     * larger than [secondsToRequestThatRespondersDelayResponses] or some responses are likely
     * to not be received.
     * */
    var secondsToWaitForResponses = 5
    /**
     * Search target - devices will respond only if this matches a service type the device supports,
     * "uuid:" and the uuid of the specific device, or "ssdp:all" to get all devices
     * */
    var searchTarget = "ssdp:all"
    /**
     * User agent to use for the request, should be in the format
     * "OS/version UPnP/1.1 product/version"
     * */
    var userAgent = "Android/Android UPnP/1.1 "

    /**
     * Begins a search for devices using the current settings setup in this instance. Calling this
     * method will cause a SSDP request to be multicast, which devices on the network will then
     * respond to.
     *
     * It is safe to call this multiple times on the same instance, in order to reuse the instance.
     *
     * @return
     * An observable that will emit each response as they are received.
     * */
    fun beginSearchForDevices(): Observable<SsdpResponseFromDevice> = Observable.create(ObservableOnSubscribe<SsdpResponseFromDevice> { emitter ->
        try {

            val stringBuilder = StringBuilder()
            stringBuilder.append("M-SEARCH * HTTP/1.1\n")
            stringBuilder.append("HOST: $MULTICAST_IP_ADDRESS:$MULTICAST_PORT\n")
            stringBuilder.append("MAN: \"ssdp:discover\"\n")
            stringBuilder.append("MX: $secondsToRequestThatRespondersDelayResponses\n")
            stringBuilder.append("ST: $searchTarget\n")
            stringBuilder.append("USER-AGENT: $userAgent\n\n")

            val sendData = stringBuilder.toString().toByteArray()

            val sendPacket = DatagramPacket(sendData, sendData.size,
                    InetAddress.getByName(MULTICAST_IP_ADDRESS), MULTICAST_PORT)

            val sendSocket = DatagramSocket()
            sendSocket.soTimeout = secondsToWaitForResponses*1000
            sendSocket.send(sendPacket)

            while (true) {
                try {
                    val receiveData = ByteArray(1024)
                    val receivePacket = DatagramPacket(receiveData, receiveData.size)
                    sendSocket.receive(receivePacket)
                    emitter.onNext(SsdpResponseFromDevice(receivePacket))
                } catch (e: SocketTimeoutException) {
                    break
                }
            }
            emitter.onComplete()
        } catch (e: Exception) {
            emitter.onError(e)
        }
    }).subscribeOn(Schedulers.newThread())

}
