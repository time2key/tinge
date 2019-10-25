package com.thaddeussoftware.tinge.ui.hubs.connectToHubFragment

import androidx.lifecycle.ViewModel
import android.content.Context
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableField
import android.util.Log
import com.thaddeussoftware.tinge.TingeApplication
import com.thaddeussoftware.tinge.database.DatabaseSingleton
import com.thaddeussoftware.tinge.database.phillipsHue.hubs.HueHubsDao
import com.thaddeussoftware.tinge.database.phillipsHue.hubs.HueHubEntity
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.finder.GenericHubFinder
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.finder.HubSearchFoundResult
import com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.finder.HueHubFinder
import com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.finder.HueHubCredentialsObtainer
import com.thaddeussoftware.tinge.deviceControlLibrary.utils.ipScanner.ScannableIpRangeCalculator
import com.thaddeussoftware.tinge.deviceControlLibrary.utils.ipScanner.WifiDetailsFromContext
import com.thaddeussoftware.tinge.ui.SingleLiveEvent
import com.thaddeussoftware.tinge.ui.hubs.hubView.HubViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by thaddeusreason on 26/02/2018.
 */

class ConnectToHubFragmentViewModel(
        /**
         * [HueHubFinder] instance to enable finding of hubs.
         * */
        private val hueHubFinder: GenericHubFinder =
                HueHubFinder(ScannableIpRangeCalculator(
                        WifiDetailsFromContext(TingeApplication.tingeApplication as Context))),
        /**
         * While this ViewModel is active, this instance will scan [currentlySelectedDevice] frequently
         * to determine whether the user has pressed the button yet granting the app access.
         * */
        private val hueHubCredentialsObtainer: HueHubCredentialsObtainer = HueHubCredentialsObtainer(),
        /**
         * This data access instance is used to store new hubs that are found.
         * */
        private val hueHubsDao: HueHubsDao = DatabaseSingleton.database.hueHubsDao()
): ViewModel() {

    /**
     * The current state the fragment is in - e.g. searching, one hub found, etc
     * */
    var state = ObservableField<ConnectToHubFragmentState>(ConnectToHubFragmentState.SEARCHING_FOR_HUBS)

    /**
     * The viewModels of hubs that have been found in the current scan. This will be wiped when a
     * new scan is started.
     * */
    var hubsFound = ObservableArrayList<HubViewModel>()

    /**
     * Whether a search is currently in progress or not. Used to determine whether to display
     * indication that a search for further devices is in progress when at least one has already
     * been found.
     * */
    var isSearching = ObservableField<Boolean>(false)

    /**
     * Whether the bottom details drawer containing more info about hubs is expanded.
     * */
    var bottomDetailsDrawerIsExpanded = ObservableField<Boolean>(false)

    /**
     * Called when a Phillips Hue Bridge has been successfully added to the app.
     * */
    var deviceAddedLiveEvent = SingleLiveEvent<DeviceAddedEventData>()

    data class DeviceAddedEventData(val hubSearchFoundResult: HubSearchFoundResult)



    /**
     * All of the currently found devices. See also [hubsFound]
     * */
    private val foundDevices = HashMap<String, HubSearchFoundResult>()

    /**
     * The currently selected device.
     *
     * If only one device has been found, this will be that device. Otherwise, this will be set to
     * the device the user selects (when the user selects it).
     *
     * If this is a Hue Bridge, this is the device that will be scanned frequently to determine
     * whether the user has pressed the button yet to grant the app access
     * - see [hueHubCredentialsObtainer]
     * */
    private var currentlySelectedDevice: HubSearchFoundResult? = null

    init {
        hueHubCredentialsObtainer.credentialsObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { usernameResult ->
                    val currentlySelectedDevice = currentlySelectedDevice
                    val currentlySelectedResult = currentlySelectedDevice?.highestPriorityIndividualResult

                    if (currentlySelectedDevice == null || currentlySelectedResult == null) {
                        return@subscribe
                    }

                    Observable.fromCallable {
                        hueHubsDao.addHueHub(HueHubEntity(
                                currentlySelectedResult.secondaryId,
                                currentlySelectedResult.primaryId,
                                currentlySelectedResult.name,
                                usernameResult))
                    }.subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe {
                                deviceAddedLiveEvent.call(DeviceAddedEventData(currentlySelectedDevice))
                            }

                }
    }

    /**
     * Should be called whenever a search is to be initiated (e.g. when the fragment is first
     * opened / when the user clicks the "rescan" button)
     * @param context required to get the [WifiDetailsFromContext]
     * */
    fun startSearchingForHubs() {
        hubsFound.clear()
        isSearching.set(true)
        state.set(ConnectToHubFragmentState.SEARCHING_FOR_HUBS)
        foundDevices.clear()

        hueHubFinder.reset()
        hueHubFinder.foundResultObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                { result ->
                    addDevice(result)
                },
                { error ->
                    scanningFinished()
                },
                {
                    scanningFinished()
                }
        )
        hueHubFinder.startFindingHubs()


    }

    /**
     * Should be called whenever onResume() is called on the corresponding fragment/activity,
     * so this instance can start/resume ongoing network tasks.
     * */
    fun resumeViewModel() {
        hueHubCredentialsObtainer.start()
    }

    /**
     * Should be called whenever onPause() is called on the corresponding fragment/activity,
     * so this instance can pause ongoing network tasks.
     * */
    fun pauseViewModel() {
        hueHubCredentialsObtainer.pause()
    }


    /**
     * Should be called whenever the user clicks to toggle expansion of the bottom details
     * drawer.
     * */
    fun onBottomDetailsDrawerToggleExpandedClicked() {
        bottomDetailsDrawerIsExpanded.set(! (bottomDetailsDrawerIsExpanded.get() ?: false))
    }


    private fun setCurrentlySelectedDevice(hubSearchFoundResult: HubSearchFoundResult) {
        currentlySelectedDevice = hubSearchFoundResult
        hueHubCredentialsObtainer.ipAddress =
                hubSearchFoundResult.highestPriorityIndividualResult?.primaryId
    }

    /**
     * Internally called whenever a response is received from a device.
     *
     * Multiple responses can be received from the same device as a result of different search
     * methods; this method is responsible for determining the one with the highest priority and
     * updating the view to use that.
     * */
    private fun addDevice(hubSearchFoundResult: HubSearchFoundResult) {

        Log.v("tinge", "Device found ${hubSearchFoundResult.highestPriorityIndividualResult?.primaryId}")

        if (hubSearchFoundResult.highestPriorityIndividualResult == null) return
        val highestPriorityResult = hubSearchFoundResult.highestPriorityIndividualResult!!

        if (!foundDevices.containsKey(highestPriorityResult.primaryId)) {
            // No device with this ip address was previously found:

            foundDevices[highestPriorityResult.primaryId] = hubSearchFoundResult

            val hubViewModel = HubViewModel()
            hubViewModel.setup(hubSearchFoundResult)

            hubsFound.add(hubViewModel)

            if (foundDevices.size == 1) {
                setCurrentlySelectedDevice(hubSearchFoundResult)
            }

        } else if (foundDevices.get(highestPriorityResult.primaryId)?.highestPriorityIndividualResult?.hubSearchMethodType?.priority?:0
                < hubSearchFoundResult.highestPriorityIndividualResult?.hubSearchMethodType?.priority?:0) {
            // A device with this ip address was previously found, but this new one is higher
            // priority:

            foundDevices[highestPriorityResult.primaryId] = hubSearchFoundResult

            hubsFound.forEach { item ->
                if (item.ipAddress.get() == highestPriorityResult.primaryId) {
                    item.setup(hubSearchFoundResult)
                }
            }
        }
        // If a device with this ip address was previously found, but this new one is lower priority,
        // then no action is performed.

        state.set(if (hubsFound.size > 1) ConnectToHubFragmentState.MANY_HUBS_FOUND else ConnectToHubFragmentState.ONE_HUB_FOUND)
    }

    private fun scanningFinished() {
        isSearching.set(false)
        if (hubsFound.size == 0) {
            state.set(ConnectToHubFragmentState.NO_HUBS_FOUND)
        }
    }


}

