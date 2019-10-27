package com.thaddeussoftware.tinge.testHelpers

import android.graphics.Color

object ColourTestHelpers {
    /**
     * Asserts two colours are equal to each other with an accepted deviance, providing helpful
     * error messages if they don't match.
     *
     * @param acceptableDeviance
     * The acceptable deviance in red, green and blue pixels - e.g. 0 means they must exactly match
     * */
    fun assertColoursEqual(expectedColour: Int?, actualColour: Int?, acceptableDeviance: Int = 0) {
        if (expectedColour == null && actualColour == null) {
            // Colours match as null
            return
        } else if (expectedColour == null) {
            throw AssertionError("expected: null but was: not null")
        } else if (actualColour == null) {
            throw AssertionError("expected: not null but was: null")
        }

        val colour1Red = Color.red(expectedColour)
        val colour1Green = Color.green(expectedColour)
        val colour1Blue = Color.blue(expectedColour)
        val colour2Red = Color.red(actualColour)
        val colour2Green = Color.green(actualColour)
        val colour2Blue = Color.blue(actualColour)

        if (colour1Red + acceptableDeviance >= colour2Red &&
                colour2Red + acceptableDeviance >= colour1Red &&
                colour1Green + acceptableDeviance >= colour2Green &&
                colour2Green + acceptableDeviance >= colour1Green &&
                colour1Blue + acceptableDeviance >= colour2Blue &&
                colour2Blue + acceptableDeviance >= colour1Blue) {
            // Colours match, do nothing
            return
        } else {
            throw AssertionError("expected: rgb(${colour1Red},${colour1Green},${colour1Blue}) but was: rgb(${colour2Red},${colour2Green},${colour2Blue})")
        }
    }
}