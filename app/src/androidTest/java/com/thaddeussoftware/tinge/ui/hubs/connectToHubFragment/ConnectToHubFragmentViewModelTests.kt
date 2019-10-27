package com.thaddeussoftware.tinge.ui.hubs.connectToHubFragment

import androidx.databinding.Observable
import androidx.test.platform.app.InstrumentationRegistry
import com.thaddeussoftware.tinge.TingeApplication
import com.thaddeussoftware.tinge.database.phillipsHue.hubs.HueHubsDao
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.finder.GenericHubFinder
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.finder.HubSearchFoundResult
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.finder.HubSearchMethodUpdate
import com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.finder.HueHubCredentialsObtainer
import io.reactivex.Single
import junit.framework.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify

class ConnectToHubFragmentViewModelTests {

    /**
     * The username is requested from the hub every 1.5 seconds, so the tests wait 2 seconds to
     * make sure that the username will definitely have been requested from the hub.
     * */
    private val TIME_TO_WAIT_MS: Long = 2_000

    private var wasCorrectMethodCalled = false

    @Before
    fun setup() {
        TingeApplication.tingeApplication = InstrumentationRegistry.getInstrumentation().targetContext
        wasCorrectMethodCalled = false
    }

    @Test
    fun viewModelSetupWithFakeClassesThatFindAndAuthenticateHub_deviceAddedEventCalledAndHueHubAddedToRoomDao() {
        // Arrange:
        val deviceHubDao = Mockito.mock(HueHubsDao::class.java)

        val viewModel = ConnectToHubFragmentViewModel(
                getFakeHubFinderThatFindsAHub(), getFakeHueHubCredentialsObtainer(), deviceHubDao)

        viewModel.deviceAddedEvent.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                wasCorrectMethodCalled = true
            }
        })

        // Act:
        viewModel.startSearchingForHubs()
        viewModel.resumeViewModel()

        Thread.sleep(TIME_TO_WAIT_MS)

        // Assert:
        verify(deviceHubDao).addHueHub(anyObject())
        assertTrue(wasCorrectMethodCalled)
    }

    /**
     * @return
     * A fake [GenericHubFinder] that immediately finds a hub with id "0" when searching starts.
     * */
    private fun getFakeHubFinderThatFindsAHub(): GenericHubFinder = object : GenericHubFinder() {
        override fun startFindingHubs() {
            hubSearchMethodIndividualResultFound(
                    HubSearchFoundResult.HubSearchIndividualResult(
                            "name",
                            "0",
                            "test0",
                            HubSearchMethodUpdate.HubSearchMethodType.PHILLIPS_HUE_IP_SCAN))
        }

        override fun reset() {}
    }

    /**
     * @return
     * A fake [HueHubCredentialsObtainer] that returns the id "1" if the ipAddress is "0", or
     * otherwise throws an exception.
     * */
    private fun getFakeHueHubCredentialsObtainer() = HueHubCredentialsObtainer(
            object: HueHubCredentialsObtainer.HueHubCredentialsRequestMaker() {
                override fun obtainUsernameTokenOrNullFrom(ipAddress: String): Single<String>?
                        = if (ipAddress == "0") Single.just("1") else Single.error(RuntimeException())
            })


    private fun <T> anyObject(): T {
        Mockito.anyObject<T>()
        return uninitialized()
    }

    private fun <T> uninitialized(): T = null as T
}