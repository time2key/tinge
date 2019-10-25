package com.thaddeussoftware.tinge.ui.hubs.connectToHubFragment

import android.os.Handler
import android.os.Looper
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

    var wasCorrectMethodCalled = false

    @Before
    fun setup() {
        TingeApplication.tingeApplication = InstrumentationRegistry.getInstrumentation().targetContext
        wasCorrectMethodCalled = false
    }

    @Test
    fun test() {
        // Arrange:
        val deviceHubDao = Mockito.mock(HueHubsDao::class.java)

        val viewModel = ConnectToHubFragmentViewModel(
                getHubFinder(), getHueHubCredentialsObtainer(), deviceHubDao)

        viewModel.deviceAddedEvent.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                wasCorrectMethodCalled = true
            }
        })

        // Act:
        viewModel.startSearchingForHubs()
        viewModel.resumeViewModel()

        Thread.sleep(11000)

        // Assert:
        verify(deviceHubDao).addHueHub(anyObject())
        assertTrue(wasCorrectMethodCalled)
    }

    fun getHubFinder(): GenericHubFinder = object : GenericHubFinder() {

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

    fun getHueHubCredentialsObtainer() = HueHubCredentialsObtainer(
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