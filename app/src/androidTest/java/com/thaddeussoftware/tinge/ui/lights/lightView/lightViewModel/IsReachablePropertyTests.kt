package com.thaddeussoftware.tinge.ui.lights.lightView.lightViewModel

import androidx.databinding.Observable
import com.thaddeussoftware.tinge.testHelpers.ControllerTestHelpers
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.controller.LightController
import com.thaddeussoftware.tinge.ui.lights.lightView.LightViewModel
import junit.framework.Assert.*
import org.junit.Before
import org.junit.Test

class IsReachablePropertyTests {

    /**
     * Wait some time to ensure that observables have time to get called and take effect
     * */
    private val TIME_TO_WAIT_MS: Long = 20

    private var wasCorrectMethodCalled = false

    /**
     * Blank fake [LightController] with no logic in it
     * */
    private lateinit var lightController: LightController


    @Before
    fun setup() {
        lightController = ControllerTestHelpers.getEmptyLightController()
    }


    @Test
    fun isReachableChangedOnController_isReachableUpdatedOnViewModel() {
        // Arrange:
        lightController.isReachable.set(false)
        val lightViewModel = LightViewModel(lightController)

        lightViewModel.isReachable.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                assertEquals(true, lightViewModel.isReachable.get())
                wasCorrectMethodCalled = true
            }
        })

        // Act:
        lightController.isReachable.set(true)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Assert:
        assertTrue(wasCorrectMethodCalled)
    }


}