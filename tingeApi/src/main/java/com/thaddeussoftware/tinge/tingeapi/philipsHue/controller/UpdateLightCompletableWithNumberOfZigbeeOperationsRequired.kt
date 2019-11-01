package com.thaddeussoftware.tinge.tingeapi.philipsHue.controller

import io.reactivex.Completable

data class UpdateLightCompletableWithNumberOfZigbeeOperationsRequired(
        val completableForUpdatingLight: Completable?,
        val numberOfZigbeeOperations: Int
)