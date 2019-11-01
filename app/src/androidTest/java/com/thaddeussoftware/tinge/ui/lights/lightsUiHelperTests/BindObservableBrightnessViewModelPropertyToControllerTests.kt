package com.thaddeussoftware.tinge.ui.lights.lightsUiHelperTests

import androidx.databinding.ObservableField
import com.thaddeussoftware.tinge.tingeapi.generic.controller.ControllerInternalStageableProperty
import com.thaddeussoftware.tinge.ui.lights.LightsUiHelper
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class BindObservableBrightnessViewModelPropertyToControllerTests {

    /**
     * Wait some time to ensure that observables have time to get called and take effect
     * */
    private val TIME_TO_WAIT_MS: Long = 20

    lateinit var isOnStageableProperty: ControllerInternalStageableProperty<Boolean?>
    lateinit var brightnessStageableProperty: ControllerInternalStageableProperty<Float?>
    lateinit var brightnessViewModelProperty: ObservableField<Float?>

    @Before
    fun setup() {
        isOnStageableProperty = ControllerInternalStageableProperty()
        brightnessStageableProperty = ControllerInternalStageableProperty()
        brightnessViewModelProperty = ObservableField()
    }

    //region Initially binding observable

    /**
     * Tests when isOn is true
     * */
    @Test
    fun isOnTrue___viewModelPropertySetToBrightness_controllerPropertiesUnchanged() {
        // Arrange:
        brightnessStageableProperty.setValueRetrievedFromHub(0f)
        isOnStageableProperty.setValueRetrievedFromHub(true)

        // Act:
        LightsUiHelper.bindObservableBrightnessViewModelPropertyToController(
                brightnessViewModelProperty,
                isOnStageableProperty,
                brightnessStageableProperty)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Assert:
        assertEquals(0f, brightnessViewModelProperty.get())
        // Make sure nothing in the internally stageable properties has changed:
        assertEquals(null, brightnessStageableProperty.stagedValue)
        assertEquals(null, isOnStageableProperty.stagedValue)
        assertEquals(0f, brightnessStageableProperty.lastValueRetrievedFromHub)
        assertEquals(true, isOnStageableProperty.lastValueRetrievedFromHub)
    }

    /**
     * Tests when isOn is false
     * */
    @Test
    fun isOnFalse___viewModelPropertySetToMinusOne_controllerPropertiesUnchanged() {
        // Arrange:
        brightnessStageableProperty.setValueRetrievedFromHub(0.5f)
        isOnStageableProperty.setValueRetrievedFromHub(false)

        // Act:
        LightsUiHelper.bindObservableBrightnessViewModelPropertyToController(
                brightnessViewModelProperty,
                isOnStageableProperty,
                brightnessStageableProperty)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Assert:
        assertEquals(-1f, brightnessViewModelProperty.get())
        // Make sure nothing in the internally stageable properties has changed:
        assertEquals(null, brightnessStageableProperty.stagedValue)
        assertEquals(null, isOnStageableProperty.stagedValue)
        assertEquals(0.5f, brightnessStageableProperty.lastValueRetrievedFromHub)
        assertEquals(false, isOnStageableProperty.lastValueRetrievedFromHub)
    }

    //endregion



    //region Binding observable then modifying controller properties

    /**
     * Tests that the isOn property being received from the hub behaves correctly
     * */
    @Test
    fun isOnTrueReceivedFromHub___viewModelPropertySetToBrightness_controllerPropertiesUnchanged() {
        // Arrange:
        brightnessStageableProperty.setValueRetrievedFromHub(0.5f)
        isOnStageableProperty.setValueRetrievedFromHub(false)

        LightsUiHelper.bindObservableBrightnessViewModelPropertyToController(
                brightnessViewModelProperty,
                isOnStageableProperty,
                brightnessStageableProperty)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Act:
        isOnStageableProperty.setValueRetrievedFromHub(true)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Assert:
        assertEquals(0.5f, brightnessViewModelProperty.get())
        // Make sure nothing in the internally stageable properties has changed:
        assertEquals(null, brightnessStageableProperty.stagedValue)
        assertEquals(null, isOnStageableProperty.stagedValue)
        assertEquals(0.5f, brightnessStageableProperty.lastValueRetrievedFromHub)
        assertEquals(true, isOnStageableProperty.lastValueRetrievedFromHub)
    }

    /**
     * Tests that the isOn property being staged behaves correctly
     * */
    @Test
    fun isOnFalseStaged___viewModelPropertySetToMinusOne_controllerPropertiesUnchanged() {
        // Arrange:
        brightnessStageableProperty.setValueRetrievedFromHub(0.5f)
        isOnStageableProperty.setValueRetrievedFromHub(true)

        LightsUiHelper.bindObservableBrightnessViewModelPropertyToController(
                brightnessViewModelProperty,
                isOnStageableProperty,
                brightnessStageableProperty)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Act:
        isOnStageableProperty.stageValue(false)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Assert:
        assertEquals(-1f, brightnessViewModelProperty.get())
        // Make sure nothing in the internally stageable properties has changed:
        assertEquals(null, brightnessStageableProperty.stagedValue)
        assertEquals(false, isOnStageableProperty.stagedValue)
        assertEquals(0.5f, brightnessStageableProperty.lastValueRetrievedFromHub)
        assertEquals(true, isOnStageableProperty.lastValueRetrievedFromHub)
    }

    /**
     * Tests that the brightness property being received from the hub behaves correctly when
     * isOn is true
     * */
    @Test
    fun isOnTrue___brightnessReceivedFromHub___viewModelPropertySetToBrightness_controllerPropertiesUnchanged() {
        // Arrange:
        brightnessStageableProperty.setValueRetrievedFromHub(1f)
        isOnStageableProperty.setValueRetrievedFromHub(true)

        LightsUiHelper.bindObservableBrightnessViewModelPropertyToController(
                brightnessViewModelProperty,
                isOnStageableProperty,
                brightnessStageableProperty)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Act:
        brightnessStageableProperty.setValueRetrievedFromHub(0.5f)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Assert:
        assertEquals(0.5f, brightnessViewModelProperty.get())
        // Make sure nothing in the internally stageable properties has changed:
        assertEquals(null, brightnessStageableProperty.stagedValue)
        assertEquals(null, isOnStageableProperty.stagedValue)
        assertEquals(0.5f, brightnessStageableProperty.lastValueRetrievedFromHub)
        assertEquals(true, isOnStageableProperty.lastValueRetrievedFromHub)
    }

    /**
     * Tests that the brightness property being received from the hub behaves correctly when
     * isOn is false
     * */
    @Test
    fun isOnFalse___brightnessReceivedFromHub___viewModelPropertyStaysMinusOne_controllerPropertiesUnchanged() {
        // Arrange:
        brightnessStageableProperty.setValueRetrievedFromHub(0.5f)
        isOnStageableProperty.setValueRetrievedFromHub(false)

        LightsUiHelper.bindObservableBrightnessViewModelPropertyToController(
                brightnessViewModelProperty,
                isOnStageableProperty,
                brightnessStageableProperty)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Act:
        brightnessStageableProperty.setValueRetrievedFromHub(1f)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Assert:
        assertEquals(-1f, brightnessViewModelProperty.get())
        // Make sure nothing in the internally stageable properties has changed:
        assertEquals(null, brightnessStageableProperty.stagedValue)
        assertEquals(null, isOnStageableProperty.stagedValue)
        assertEquals(1f, brightnessStageableProperty.lastValueRetrievedFromHub)
        assertEquals(false, isOnStageableProperty.lastValueRetrievedFromHub)
    }

    /**
     * Tests that the brightness property being staged hub behaves correctly when isOn is false
     * */
    @Test
    fun isOnFalse___brightnessStaged___viewModelPropertySetToMinusOne_controllerPropertiesUnchanged() {
        // Arrange:
        brightnessStageableProperty.setValueRetrievedFromHub(1f)
        isOnStageableProperty.setValueRetrievedFromHub(false)

        LightsUiHelper.bindObservableBrightnessViewModelPropertyToController(
                brightnessViewModelProperty,
                isOnStageableProperty,
                brightnessStageableProperty)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Act:
        brightnessStageableProperty.stageValue(0.5f)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Assert:
        assertEquals(-1f, brightnessViewModelProperty.get())
        // Make sure nothing in the internally stageable properties has changed:
        assertEquals(0.5f, brightnessStageableProperty.stagedValue)
        assertEquals(null, isOnStageableProperty.stagedValue)
        assertEquals(1f, brightnessStageableProperty.lastValueRetrievedFromHub)
        assertEquals(false, isOnStageableProperty.lastValueRetrievedFromHub)
    }

    /**
     * Tests that the brightness property being staged behaves correctly when isOn is true
     * */
    @Test
    fun isOnTrue___brightnessStaged___viewModelPropertySetToBrightness_controllerPropertiesUnchanged() {
        // Arrange:
        brightnessStageableProperty.setValueRetrievedFromHub(1f)
        isOnStageableProperty.setValueRetrievedFromHub(true)

        LightsUiHelper.bindObservableBrightnessViewModelPropertyToController(
                brightnessViewModelProperty,
                isOnStageableProperty,
                brightnessStageableProperty)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Act:
        brightnessStageableProperty.stageValue(0.5f)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Assert:
        assertEquals(0.5f, brightnessViewModelProperty.get())
        // Make sure nothing in the internally stageable properties has changed:
        assertEquals(0.5f, brightnessStageableProperty.stagedValue)
        assertEquals(null, isOnStageableProperty.stagedValue)
        assertEquals(1f, brightnessStageableProperty.lastValueRetrievedFromHub)
        assertEquals(true, isOnStageableProperty.lastValueRetrievedFromHub)
    }

    //endregion



    //region Binding observable them modifying ViewModel property

    /**
     * Tests that the viewmodel property being modified to another brightness behaves correctly
     * when isOn is true
     * */
    @Test
    fun isOnTrue___viewModelPropertyMovedToAnotherBrightness___brightnessValueStaged_otherControllerPropertiesUnchanged() {
        // Arrange:
        brightnessStageableProperty.setValueRetrievedFromHub(1f)
        isOnStageableProperty.setValueRetrievedFromHub(true)

        LightsUiHelper.bindObservableBrightnessViewModelPropertyToController(
                brightnessViewModelProperty,
                isOnStageableProperty,
                brightnessStageableProperty)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Act:
        brightnessViewModelProperty.set(0.5f)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Assert:
        assertEquals(0.5f, brightnessStageableProperty.stagedValue)
        // Make sure no other internally stageable properties have changed:
        assertEquals(1f, brightnessStageableProperty.lastValueRetrievedFromHub)
        assertEquals(null, isOnStageableProperty.stagedValue)
        assertEquals(true, isOnStageableProperty.lastValueRetrievedFromHub)

        assertEquals(0.5f, brightnessViewModelProperty.get())
    }

    /**
     * Tests that the viewmodel property being modified to off behaves correctly when isOn is true
     * */
    @Test
    fun isOnTrue___viewModelPropertyMovedToOff___isOnValueStaged_otherControllerPropertiesUnchanged() {
        // Arrange:
        brightnessStageableProperty.setValueRetrievedFromHub(1f)
        isOnStageableProperty.setValueRetrievedFromHub(true)

        LightsUiHelper.bindObservableBrightnessViewModelPropertyToController(
                brightnessViewModelProperty,
                isOnStageableProperty,
                brightnessStageableProperty)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Act:
        brightnessViewModelProperty.set(-1f)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Assert:
        assertEquals(false, isOnStageableProperty.stagedValue)
        // Make sure no other internally stageable properties have changed:
        assertEquals(null, brightnessStageableProperty.stagedValue)
        assertEquals(1f, brightnessStageableProperty.lastValueRetrievedFromHub)
        assertEquals(true, isOnStageableProperty.lastValueRetrievedFromHub)

        assertEquals(-1f, brightnessViewModelProperty.get())
    }

    /**
     * Tests that the viewmodel property being modified to a brightness behaves correctly
     * when isOn is false
     * */
    @Test
    fun isOnFalse___viewModelPropertyMovedToABrightness___brightnessValueStaged_isOnTrueStaged_otherControllerPropertiesUnchanged() {
        // Arrange:
        brightnessStageableProperty.setValueRetrievedFromHub(1f)
        isOnStageableProperty.setValueRetrievedFromHub(false)

        LightsUiHelper.bindObservableBrightnessViewModelPropertyToController(
                brightnessViewModelProperty,
                isOnStageableProperty,
                brightnessStageableProperty)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Act:
        brightnessViewModelProperty.set(0.5f)

        Thread.sleep(TIME_TO_WAIT_MS)

        // Assert:
        assertEquals(0.5f, brightnessStageableProperty.stagedValue)
        assertEquals(true, isOnStageableProperty.stagedValue)
        // Make sure no other internally stageable properties have changed:
        assertEquals(1f, brightnessStageableProperty.lastValueRetrievedFromHub)
        assertEquals(false, isOnStageableProperty.lastValueRetrievedFromHub)

        assertEquals(0.5f, brightnessViewModelProperty.get())
    }

    //endregion

}