package com.thaddeussoftware.tinge.ui.lights.lightView.lightViewModel

import com.thaddeussoftware.tinge.testHelpers.ControllerTestHelpers
import com.thaddeussoftware.tinge.tingeapi.generic.controller.LightController
import com.thaddeussoftware.tinge.ui.lights.lightView.LightViewModel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ShowTopRightExpandButtonTests {

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



    // region Testing initial LightViewModel values

    /**
     * Tests that isReachable true and isOn false sets showTopRightExpandButton to false
     * */
    @Test
    fun isReachableTrue_isOnFalse___showTopRightExpandButtonFalse() {
        // Arrange:
        lightController.isReachable.set(true)
        lightController.isOn.setValueRetrievedFromHub(false)

        // Act:
        val viewModel = LightViewModel(lightController)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Assert:
        assertEquals(false, viewModel.showTopRightExpandButton.get())
    }

    /**
     * Tests that isReachable true and isOn true sets showTopRightExpandButton to true
     * */
    @Test
    fun isReachableTrue_isOnTrue___showTopRightExpandButtonTrue() {
        // Arrange:
        lightController.isReachable.set(true)
        lightController.isOn.setValueRetrievedFromHub(true)

        // Act:
        val viewModel = LightViewModel(lightController)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Assert:
        assertEquals(true, viewModel.showTopRightExpandButton.get())
    }

    /**
     * Tests that isReachable false and isOn false sets showTopRightExpandButton to true
     * */
    @Test
    fun isReachableFalse_isOnFalse___showTopRightExpandButtonTrue() {
        // Arrange:
        lightController.isReachable.set(false)
        lightController.isOn.setValueRetrievedFromHub(false)

        // Act:
        val viewModel = LightViewModel(lightController)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Assert:
        assertEquals(true, viewModel.showTopRightExpandButton.get())
    }

    /**
     * Tests that isReachable false and isOn true sets showTopRightExpandButton to true
     * */
    @Test
    fun isReachableFalse_isOnTrue___showTopRightExpandButtonTrue() {
        // Arrange:
        lightController.isReachable.set(false)
        lightController.isOn.setValueRetrievedFromHub(true)

        // Act:
        val viewModel = LightViewModel(lightController)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Assert:
        assertEquals(true, viewModel.showTopRightExpandButton.get())
    }

    // endregion



    //region Modifying isOn controller value

    /**
     * Tests that isOn being staged to true correctly updates showTopRightExpandButton
     * */
    @Test
    fun isReachableTrue_isOnFalse___isOnTrueStaged___showTopRightExpandButtonTrue() {
        // Arrange:
        lightController.isReachable.set(true)
        lightController.isOn.setValueRetrievedFromHub(false)

        val viewModel = LightViewModel(lightController)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Act:
        lightController.isOn.stageValue(true)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Assert:
        assertEquals(true, viewModel.showTopRightExpandButton.get())
    }

    /**
     * Tests that isOn being staged to false correctly updates showTopRightExpandButton
     * */
    @Test
    fun isReachableTrue_isOnTrue___isOnFalseStaged___showTopRightExpandButtonFalse() {
        // Arrange:
        lightController.isReachable.set(true)
        lightController.isOn.setValueRetrievedFromHub(true)

        val viewModel = LightViewModel(lightController)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Act:
        lightController.isOn.stageValue(false)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Assert:
        assertEquals(false, viewModel.showTopRightExpandButton.get())
    }

    /**
     * Tests that isOn being updated from hub to true correctly updates showTopRightExpandButton
     * */
    @Test
    fun isReachableTrue_isOnFalse___isOnTrueReceivedFromHub___showTopRightExpandButtonTrue() {
        // Arrange:
        lightController.isReachable.set(true)
        lightController.isOn.setValueRetrievedFromHub(false)

        val viewModel = LightViewModel(lightController)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Act:
        lightController.isOn.setValueRetrievedFromHub(true)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Assert:
        assertEquals(true, viewModel.showTopRightExpandButton.get())
    }

    /**
     * Tests that isOn being updated from hub to false correctly updates showTopRightExpandButton
     * */
    @Test
    fun isReachableTrue_isOnTrue___isOnFalseReceivedFromHub___showTopRightExpandButtonFalse() {
        // Arrange:
        lightController.isReachable.set(true)
        lightController.isOn.setValueRetrievedFromHub(true)

        val viewModel = LightViewModel(lightController)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Act:
        lightController.isOn.setValueRetrievedFromHub(false)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Assert:
        assertEquals(false, viewModel.showTopRightExpandButton.get())
    }

    //endregion



    //region Modifying isReachable controller value

    /**
     * Tests that isReachable being updated from hub to false correctly updates
     * showTopRightExpandButton to true
     * */
    @Test
    fun isReachableTrue_isOnTrue___isReachableFalseSet___showTopRightExpandButtonTrue() {
        // Arrange:
        lightController.isReachable.set(true)
        lightController.isOn.setValueRetrievedFromHub(false)

        val viewModel = LightViewModel(lightController)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Act:
        lightController.isReachable.set(false)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Assert:
        assertEquals(true, viewModel.showTopRightExpandButton.get())
    }

    /**
     * Tests that isReachable being updated from hub to true correctly updates
     * showTopRightExpandButton to false
     * */
    @Test
    fun isReachableFalse_isOnTrue___isReachableFalseSet___showTopRightExpandButtonFalse() {
        // Arrange:
        lightController.isReachable.set(false)
        lightController.isOn.setValueRetrievedFromHub(false)

        val viewModel = LightViewModel(lightController)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Act:
        lightController.isReachable.set(true)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Assert:
        assertEquals(false, viewModel.showTopRightExpandButton.get())
    }

    //endregion
}