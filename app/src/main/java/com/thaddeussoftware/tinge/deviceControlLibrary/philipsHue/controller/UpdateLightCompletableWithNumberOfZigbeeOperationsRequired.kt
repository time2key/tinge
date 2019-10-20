package com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.controller

import io.reactivex.Completable

data class UpdateLightCompletableWithNumberOfZigbeeOperationsRequired(
        val completableForUpdatingLight: Completable?,
        val numberOfZigbeeOperations: Int
)