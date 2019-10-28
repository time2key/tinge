package com.thaddeussoftware.tinge.ui.lights.lightView.lightViewModel

import com.thaddeussoftware.tinge.testHelpers.ControllerTestHelpers
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.controller.LightController
import com.thaddeussoftware.tinge.helpers.ColorHelper
import com.thaddeussoftware.tinge.testHelpers.ColourTestHelpers
import com.thaddeussoftware.tinge.ui.lights.LightsUiHelper
import com.thaddeussoftware.tinge.ui.lights.lightView.LightViewModel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class HueSaturationBrightnessHandleTests {

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

    //region Initial values

    @Test
    fun brightRedInitialValue_handlesSetupCorrectly() {
        // Arrange:
        lightController.displayName.setValueRetrievedFromHub("My light")
        lightController.hue.setValueRetrievedFromHub(0f) // Red
        lightController.saturation.setValueRetrievedFromHub(1f)
        lightController.brightness.setValueRetrievedFromHub(1f)
        lightController.isOn.setValueRetrievedFromHub(true)

        val viewModel = LightViewModel(lightController)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Assert:
        assertHandlesSetupAsGivenValues(
                viewModel,
                0f, 1f, 1f,
                "My light")
    }

    @Test
    fun greenHalfBrightnessHalfSaturationInitialValue_handlesSetupCorrectly() {
        // Arrange:
        lightController.displayName.setValueRetrievedFromHub("My pale green light")
        lightController.hue.setValueRetrievedFromHub(120f/360f) // Green
        lightController.saturation.setValueRetrievedFromHub(0.5f)
        lightController.brightness.setValueRetrievedFromHub(0.5f)
        lightController.isOn.setValueRetrievedFromHub(true)

        val viewModel = LightViewModel(lightController)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Assert:
        assertHandlesSetupAsGivenValues(
                viewModel,
                120f/360f, 0.5f, 0.5f,
                "My pale green light")
    }

    @Test
    fun offInitialValue_handlesSetupCorrectly() {
        // Arrange:
        lightController.displayName.setValueRetrievedFromHub("My light")
        lightController.hue.setValueRetrievedFromHub(0f) // Red
        lightController.saturation.setValueRetrievedFromHub(1f)
        lightController.brightness.setValueRetrievedFromHub(1f)
        lightController.isOn.setValueRetrievedFromHub(false)

        val viewModel = LightViewModel(lightController)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Assert:
        assertHandlesSetupAsGivenValues(
                viewModel,
                0f, 1f, -1f,
                "My light")
    }

    //endregion



    //region Changing hue values

    @Test
    fun hueRetrievedFromServer_handlesSetupCorrectly() {
        // Arrange:
        lightController.displayName.setValueRetrievedFromHub("My light")
        lightController.hue.setValueRetrievedFromHub(0f) // Red
        lightController.saturation.setValueRetrievedFromHub(1f)
        lightController.brightness.setValueRetrievedFromHub(1f)
        lightController.isOn.setValueRetrievedFromHub(true)

        val viewModel = LightViewModel(lightController)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Act:
        lightController.hue.setValueRetrievedFromHub(240f / 360f) // Blue

        // Assert:
        assertHandlesSetupAsGivenValues(
                viewModel,
                240f / 360f, 1f, 1f,
                "My light")
    }

    //endregion



    //region Changing saturation values

    @Test
    fun saturationStaged_handlesSetupCorrectly() {
        // Arrange:
        lightController.displayName.setValueRetrievedFromHub("My light")
        lightController.hue.setValueRetrievedFromHub(320f / 360f) // Purple
        lightController.saturation.setValueRetrievedFromHub(1f)
        lightController.brightness.setValueRetrievedFromHub(1f)
        lightController.isOn.setValueRetrievedFromHub(true)

        val viewModel = LightViewModel(lightController)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Act:
        lightController.saturation.stageValue(0.5f)

        // Assert:
        assertHandlesSetupAsGivenValues(
                viewModel,
                320f / 360f, 0.5f, 1f,
                "My light")
    }

    //endregion



    //region Changing brightness and isOn values

    @Test
    fun hubIsOn_brightnessReceivedFromHub_handlesSetupCorrectly() {
        // Arrange:
        lightController.displayName.setValueRetrievedFromHub("My light")
        lightController.hue.setValueRetrievedFromHub(60f / 360f) // Yellow
        lightController.saturation.setValueRetrievedFromHub(1f)
        lightController.brightness.setValueRetrievedFromHub(1f)
        lightController.isOn.setValueRetrievedFromHub(true)

        val viewModel = LightViewModel(lightController)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Act:
        lightController.brightness.setValueRetrievedFromHub(0.8f)

        // Assert:
        assertHandlesSetupAsGivenValues(
                viewModel,
                60f / 360f, 1f, 0.8f,
                "My light")
    }

    /**
     * Handles should be setup with the brightness slider set to -1, because the light is
     * still off, even though the brightness has changed on the hub
     * */
    @Test
    fun hubIsOff_brightnessReceivedFromHub_handlesSetupCorrectly() {
        // Arrange:
        lightController.displayName.setValueRetrievedFromHub("My light")
        lightController.hue.setValueRetrievedFromHub(60f / 360f) // Yellow
        lightController.saturation.setValueRetrievedFromHub(1f)
        lightController.brightness.setValueRetrievedFromHub(1f)
        lightController.isOn.setValueRetrievedFromHub(false)

        val viewModel = LightViewModel(lightController)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Act:
        lightController.brightness.setValueRetrievedFromHub(0.8f)

        // Assert:
        assertHandlesSetupAsGivenValues(
                viewModel,
                60f / 360f, 1f, -1f,
                "My light")
    }

    @Test
    fun hubIsOff_isOnTrueReceivedFromHun_handlesSetupCorrectly() {
        // Arrange:
        lightController.displayName.setValueRetrievedFromHub("My light")
        lightController.hue.setValueRetrievedFromHub(60f / 360f) // Yellow
        lightController.saturation.setValueRetrievedFromHub(1f)
        lightController.brightness.setValueRetrievedFromHub(0.8f)
        lightController.isOn.setValueRetrievedFromHub(false)

        val viewModel = LightViewModel(lightController)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Act:
        lightController.isOn.setValueRetrievedFromHub(true)

        // Assert:
        assertHandlesSetupAsGivenValues(
                viewModel,
                60f / 360f, 1f, 0.8f,
                "My light")
    }

    //endregion



    /**
     * Asserts that the handles in a given ViewModel are correctly setup according to specified
     * values.
     * */
    private fun assertHandlesSetupAsGivenValues(
            viewModel: LightViewModel,
            hue: Float,
            saturation: Float,
            brightnessAndIsOn: Float,
            displayName: String) {
        assertEquals(1, viewModel.hueHandles.size)
        assertEquals(1, viewModel.saturationHandles.size)
        assertEquals(1, viewModel.brightnessHandles.size)

        assertEquals(displayName, viewModel.hueHandles[0].displayName)
        assertEquals(displayName, viewModel.saturationHandles[0].displayName)
        assertEquals(displayName, viewModel.brightnessHandles[0].displayName)

        assertEquals(hue, viewModel.hueHandles[0].value.get())
        assertEquals(saturation, viewModel.saturationHandles[0].value.get())
        assertEquals(brightnessAndIsOn, viewModel.brightnessHandles[0].value.get())

        // Just checks that the colours match the values from LightsUiHelper.get... methods
        // - there are separate tests around the LightsUiHelper methods checking these are right:
        val expectedHueColour = ColorHelper.colorFromHsv(hue, 1f, 1f)
        val expectedSaturationColour = LightsUiHelper.getColorForSaturationSlider(hue, saturation)
        val expectedBrightnessColour = LightsUiHelper.getColorForBrightnessSliderHandle(
                hue, saturation, brightnessAndIsOn, brightnessAndIsOn > 0f)

        ColourTestHelpers.assertColoursEqual(expectedHueColour, viewModel.hueHandles[0].color.get())
        ColourTestHelpers.assertColoursEqual(expectedSaturationColour, viewModel.saturationHandles[0].color.get())
        ColourTestHelpers.assertColoursEqual(expectedBrightnessColour, viewModel.brightnessHandles[0].color.get())
    }
}