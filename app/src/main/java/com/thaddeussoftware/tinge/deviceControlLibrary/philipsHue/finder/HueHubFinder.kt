package com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.finder

import android.util.Log
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.finder.GenericHubFinder
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.finder.HubSearchFoundResult
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.finder.HubSearchMethodUpdate
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.finder.HubSearchMethodUpdate.*
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.finder.HubSearchMethodUpdate.HubSearchMethodState.*
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.finder.HubSearchMethodUpdate.HubSearchMethodType.*
import com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.finder.inner.ContactWebServerHueFinder
import com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.finder.inner.HueIpAddressScannerFinder
import com.thaddeussoftware.tinge.deviceControlLibrary.utils.ipScanner.ScannableIpRangeCalculator
import com.thaddeussoftware.tinge.deviceControlLibrary.utils.ssdp.SsdpRequester
import io.reactivex.*
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import java.util.NoSuchElementException

/**
 * This class finds Hue hubs on the local network.
 *
 * For usage details, see [GenericHubFinder].
 *
 * Internally, this class behaves in the following way:
 * * First, it starts a SSDP request and searches the results for Hue Hubs.
 * * Meanwhile, it contacts the Philips Hue server to find Hue Hubs.
 * * Any Hue Hubs found during these steps are contacted at their IP address to find out
 * more information about them.
 * * When both these scans are finished, if their results were consistent and contained more than
 * one response, nothing further happens. Otherwise, a full IP scan is performed.
 *
 * Created by thaddeusreason on 29/03/2018.
 */

class HueHubFinder(
        private val scannableIpRangeCalculator: ScannableIpRangeCalculator,
        private val ssdpRequester: SsdpRequester = SsdpRequester()
): GenericHubFinder() {

    /**
     * See [InternalIpAddressesToScanFlowableGenerator].
     * */
    private var internalIpAddressesToScanFlowableGenerator
            = InternalIpAddressesToScanFlowableGenerator(scannableIpRangeCalculator)

    // Disposables for all observables subscribed to are kept track of, so that they can be
    // disposed in reset():
    private var ssdpRequestDisposable: Disposable? = null
    private var webRequestDisposable: Disposable? = null
    private var mergedRequestsDisposable: Disposable? = null
    private var ipAddressScannerDisposable: Disposable? = null

    private var wasFullSearchForIpAddressesStarted = false



    override fun reset() {
        ssdpRequestDisposable?.dispose()
        webRequestDisposable?.dispose()
        mergedRequestsDisposable?.dispose()
        ipAddressScannerDisposable?.dispose()
        wasFullSearchForIpAddressesStarted = false
        internalIpAddressesToScanFlowableGenerator = InternalIpAddressesToScanFlowableGenerator(scannableIpRangeCalculator)
    }

    override fun startFindingHubs() {
        val ssdpRequestObservable = ssdpRequester.beginSearchForDevices().publish()
        ssdpRequestDisposable = ssdpRequestObservable.subscribe(
                { result ->
                    deviceFound(null,
                            result.ipAddress,
                            result.headersUpperCase["HUE-BRIDGEID"],
                            PHILLIPS_HUE_SSDP)
                },
                { error -> hubSearchMethodUpdateHasHappened(PHILLIPS_HUE_SSDP, FAILURE) },
                { hubSearchMethodUpdateHasHappened(PHILLIPS_HUE_SSDP, SUCCESS) }
        )

        val webRequestObservable = ContactWebServerHueFinder().getHueBridgesFromServer().publish()
        webRequestDisposable = webRequestObservable.subscribe(
                { result ->
                    deviceFound(result.name,
                            result.internalIpAddress?:"",
                            result.id,
                            PHILLIPS_HUE_CONTACT_SERVER)
                },
                { error ->
                    hubSearchMethodUpdateHasHappened(PHILLIPS_HUE_CONTACT_SERVER, FAILURE)
                    Log.e("tinge", "fail", error)
                },
                { hubSearchMethodUpdateHasHappened(PHILLIPS_HUE_CONTACT_SERVER, SUCCESS) }
        )

        mergedRequestsDisposable = Completable.mergeDelayError(
                arrayOf<CompletableSource>(
                        ssdpRequestObservable.ignoreElements(),
                        webRequestObservable.ignoreElements()
                ).asIterable())
                .subscribe(
                        { hueSsdpAndWebRequestSearchesFinished(true) },
                        { error -> hueSsdpAndWebRequestSearchesFinished(false) }
                )

        ipAddressScannerDisposable = HueIpAddressScannerFinder()
                .scanIpAddressesForHueBridges(
                        internalIpAddressesToScanFlowableGenerator.generateFlowable())
                .subscribe (
                        { result ->
                            deviceFound(
                                    result.jsonConfigurationDetails.name,
                                    result.ipAddress,
                                    result.jsonConfigurationDetails.bridgeId,
                                    if (wasFullSearchForIpAddressesStarted)
                                        PHILLIPS_HUE_IP_SCAN
                                    else
                                        PHILLIPS_HUE_IP_LOOKUP)
                        },
                        { error ->

                        },
                        {
                            if (wasFullSearchForIpAddressesStarted) {
                                hubSearchMethodUpdateHasHappened(PHILLIPS_HUE_IP_SCAN, SUCCESS)
                            } else {
                                hubSearchMethodUpdateHasHappened(PHILLIPS_HUE_IP_LOOKUP, SUCCESS)
                            }
                        }
                )


        hubSearchMethodUpdateHasHappened(PHILLIPS_HUE_SSDP, IN_PROGRESS)
        hubSearchMethodUpdateHasHappened(PHILLIPS_HUE_CONTACT_SERVER, IN_PROGRESS)
        hubSearchMethodUpdateHasHappened(PHILLIPS_HUE_IP_LOOKUP, IN_PROGRESS)
        ssdpRequestObservable.connect()
        webRequestObservable.connect()
    }

    /**
     * Internally called when both the SSDP and Web searches have finished (in success or error).
     *
     * This method will choose whether to start a further full IP scan of all devices on the
     * network, or whether to just scan the IP addresses of the found devices for further info.
     * */
    private fun hueSsdpAndWebRequestSearchesFinished(bothSuccessful: Boolean) {
        var performFullIpScan = false

        if (bothSuccessful) {

            currentlyFoundResults.forEach { (_, hubSearchFoundResult) ->
                var hasBeenFoundBySsdp = false
                var hasBeenFoundByWebRequest = false
                hubSearchFoundResult.individualResults.forEach { item ->
                    if (item.hubSearchMethodType == PHILLIPS_HUE_SSDP) {
                        hasBeenFoundBySsdp = true
                    } else if (item.hubSearchMethodType == PHILLIPS_HUE_CONTACT_SERVER) {
                        hasBeenFoundByWebRequest = true
                    }
                }
                if (!hasBeenFoundBySsdp || !hasBeenFoundByWebRequest) {
                    performFullIpScan = true
                }
            }
        } else {
            //hubSearchMethodUpdateHasHappened(PHILLIPS_HUE_IP_SCAN, CANCELLED)
            performFullIpScan = true
        }

        if (performFullIpScan) {
            hubSearchMethodUpdateHasHappened(PHILLIPS_HUE_IP_LOOKUP, SUCCESS)
            hubSearchMethodUpdateHasHappened(PHILLIPS_HUE_IP_SCAN, IN_PROGRESS)
            wasFullSearchForIpAddressesStarted = true
            internalIpAddressesToScanFlowableGenerator.stopEmittingIpListStartEmittingAllInRange()
        } else {
            hubSearchMethodUpdateHasHappened(PHILLIPS_HUE_IP_LOOKUP, SUCCESS)
            hubSearchMethodUpdateHasHappened(PHILLIPS_HUE_IP_SCAN, CANCELLED)
            wasFullSearchForIpAddressesStarted = false
            internalIpAddressesToScanFlowableGenerator.stopEmittingIpListStopFlowable()
        }
    }

    /**
     * Should be called internally whenever a device is found.
     *
     * If secondaryId is null, this function will return without doing anything, as a value must be
     * specified for secondaryId.
     * */
    private fun deviceFound(
            hubName: String?, ipAddressPrimaryId:String, secondaryId:String?,
            hubSearchMethodType: HubSearchMethodUpdate.HubSearchMethodType) {

        if (secondaryId == null) return

        val wasDeviceFoundBefore = (currentlyFoundResults[ipAddressPrimaryId] != null)
        if (!wasDeviceFoundBefore) {
            internalIpAddressesToScanFlowableGenerator.addIpToList(ipAddressPrimaryId)
        }

        hubSearchMethodIndividualResultFound(HubSearchFoundResult.HubSearchIndividualResult(
                hubName, ipAddressPrimaryId, secondaryId, hubSearchMethodType))
    }

    /**
     * This internal class is designed to generate a flowable that can output a list of given ip
     * addresses initially (while [HubSearchMethodType.PHILLIPS_HUE_IP_LOOKUP] is in progress), and
     * then can start outputting all ip addresses on the subnet (while performing
     * [HubSearchMethodType.PHILLIPS_HUE_IP_SCAN]) if required, or finish if not required.
     *
     * This will behave in the following way:
     *
     * * Initially / while in state [State.EMITTING_LIST_OF_IP_ADDRESSES], this will wait for
     * [addIpToList] to be called. When [addIpToList] is called, the added ip address will be
     * emitted on the flowable.
     *
     * * If multiple calls to [addIpToList] are made in a very short time period, they will be queued.
     *
     * * Then, to transition this flowable to scanning for all ip addresses in range, call
     * [stopEmittingIpListStartEmittingAllInRange]. If you wish to finish the flowable without
     * instead without doing this, call [stopEmittingIpListStopFlowable].
     *
     * To get the flowable for ip addresses, call [generateFlowable].
     * */
    private class InternalIpAddressesToScanFlowableGenerator(val scannableIpRangeCalculator: ScannableIpRangeCalculator) {

        enum class State {
            EMITTING_LIST_OF_IP_ADDRESSES,
            STOPPED,
            EMITTING_ALL_IP_ADDRESSES_IN_SCANNABLE_RANGE,
        }

        var currentState = State.EMITTING_LIST_OF_IP_ADDRESSES
            private set

        /**
         * Used to ensure that calls modifying the state of [currentState] and [ipAddressesList]
         * are synchronised against the flowable.
         *
         * Also, if [currentState] or [ipAddressesList] are modified, [Object.notify] should be
         * called on this.
         * */
        private val threadLock = java.lang.Object()

        /**
         * Ip addresses that have been / will be returned by the flowable while in state
         * [State.EMITTING_LIST_OF_IP_ADDRESSES]. Items should not be removed from this list, only
         * added.
         * */
        private var ipAddressesList = ArrayList<String>()

        /**
         * The flowable that will be used to get all ip addresses in the scannable range.
         * Instantiated in [stopEmittingIpListStartEmittingAllInRange].
         * */
        private var scannableIpRangeFlowable: Flowable<String>? = null

        /**
         * Add an ip address to the list of ips to be returned. This will get emitted by the
         * flowable.
         * */
        fun addIpToList(ipAddress: String) {
            synchronized(threadLock) {
                ipAddressesList.add(ipAddress)
                threadLock.notify()
            }
        }

        /**
         * Call this to stop emitting a list of ips added in [addIpToList] and instead start
         * emitting all scannable ip addresses in the subnet.
         * */
        fun stopEmittingIpListStartEmittingAllInRange() {
            synchronized(threadLock) {
                scannableIpRangeFlowable = scannableIpRangeCalculator.searchableIpAddressesInSubnet()
                currentState = State.EMITTING_ALL_IP_ADDRESSES_IN_SCANNABLE_RANGE
                threadLock.notify()
            }
        }

        /**
         * Call this to stop the flowable (without emitting all ip addresses in the network)
         * */
        fun stopEmittingIpListStopFlowable() {
            synchronized(threadLock) {
                currentState = State.STOPPED
                threadLock.notify()
            }
        }


        fun generateFlowable() = Flowable.generate(object: Consumer<Emitter<String>> {
            /**
             * The number of ip addresses currently emitted from [ipAddressesList]
             * */
            var currentlyEmittedIpAddressCount = 0

            override fun accept(emitter: Emitter<String>) {
                if (currentState == State.EMITTING_ALL_IP_ADDRESSES_IN_SCANNABLE_RANGE) {
                    try {
                        emitter.onNext(scannableIpRangeFlowable!!.blockingFirst())
                    } catch (e: NoSuchElementException) {
                        emitter.onComplete()
                    }
                    return
                }

                synchronized(threadLock) {
                    while (true) {
                        if (currentlyEmittedIpAddressCount < ipAddressesList.size) {
                            emitter.onNext(ipAddressesList[currentlyEmittedIpAddressCount])
                            currentlyEmittedIpAddressCount += 1
                            return
                        } else if (currentState == State.STOPPED) {
                            emitter.onComplete()
                            return
                        }

                        threadLock.wait()
                    }
                }
            }
        })

    }

}
