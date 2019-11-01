package com.thaddeussoftware.tinge.ui.lights.lightView.lightViewModel

import com.thaddeussoftware.tinge.testHelpers.ControllerTestHelpers
import com.thaddeussoftware.tinge.tingeapi.generic.controller.LightController
import com.thaddeussoftware.tinge.ui.lights.lightView.LightViewModel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class IsExpandedTests {

    /**
     * Wait some time to ensure that observables have time to get called and take effect
     * */
    private val TIME_TO_WAIT_MS: Long = 20

    /**
     * Blank fake [LightController] with no logic in it
     * */
    private lateinit var lightController: LightController


    @Before
    fun setup() {
        lightController = ControllerTestHelpers.getEmptyLightController()
    }

    @Test
    fun initialValue_false() {
        // Arrange:
        lightController.isReachable.set(true)
        val viewModel = LightViewModel(lightController)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Assert:
        assertEquals(false, viewModel.isExpanded.get())
    }

    @Test
    fun expandButtonClicked_true() {
        // Arrange:
        lightController.isReachable.set(true)
        val viewModel = LightViewModel(lightController)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Act:
        viewModel.onExpandContractButtonClicked()

        // Assert:
        assertEquals(true, viewModel.isExpanded.get())
    }

    /**
     * When a light is reachable and expanded, and is turned off, it should be unexpanded.
     * Tests this behaviour when light turned off by staging isOn property.
     * */
    @Test
    fun isReachable_alreadyExpanded___isOnStagedFalse___false() {
        // Arrange:
        lightController.isReachable.set(true)
        val viewModel = LightViewModel(lightController)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Click expand:
        viewModel.onExpandContractButtonClicked()

        Thread.sleep(TIME_TO_WAIT_MS)

        // Act:
        lightController.isOn.stageValue(false)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Assert:
        assertEquals(false, viewModel.isExpanded.get())
    }

    /**
     * When a light is reachable and expanded, and is turned off, it should be unexpanded.
     * Tests this behaviour when light turned off by isOn property being received from hub.
     * */
    @Test
    fun isReachable_alreadyExpanded___isOnReceivedFromHubFalse___false() {
        // Arrange:
        lightController.isReachable.set(true)
        val viewModel = LightViewModel(lightController)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Click expand:
        viewModel.onExpandContractButtonClicked()

        Thread.sleep(TIME_TO_WAIT_MS)

        // Act:
        lightController.isOn.setValueRetrievedFromHub(false)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Assert:
        assertEquals(false, viewModel.isExpanded.get())
    }

    /**
     * When a light is reachable and expanded, and is turned off, it should be unexpanded.
     * Tests this behaviour when light turned off by the brightness viewmodel property being set.
     * */
    @Test
    fun isReachable_alreadyExpanded___isOnSetOnViewModelProperty___false() {
        // Arrange:
        lightController.isReachable.set(true)
        val viewModel = LightViewModel(lightController)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Click expand:
        viewModel.onExpandContractButtonClicked()

        Thread.sleep(TIME_TO_WAIT_MS)

        // Act:
        viewModel.brightnessHandles.get(0).value.set(-1f)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Assert:
        assertEquals(false, viewModel.isExpanded.get())
    }

    /**
     * When a light is NOT reachable and expanded, and is turned off, it should NOT be unexpanded.
     * Tests this behaviour when light turned off by staging isOn property.
     * */
    @Test
    fun isNotReachable_alreadyExpanded___isOnStagedFalse___true() {
        // Arrange:
        lightController.isReachable.set(false)
        val viewModel = LightViewModel(lightController)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Click expand:
        viewModel.onExpandContractButtonClicked()

        Thread.sleep(TIME_TO_WAIT_MS)

        // Act:
        lightController.isOn.stageValue(false)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Assert:
        assertEquals(true, viewModel.isExpanded.get())
    }
}