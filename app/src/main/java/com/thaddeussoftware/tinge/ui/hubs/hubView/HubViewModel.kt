package com.thaddeussoftware.tinge.ui.hubs.hubView

import androidx.lifecycle.ViewModel
import androidx.databinding.ObservableField
import com.thaddeussoftware.tinge.R
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.finder.HubSearchFoundResult

/**
 * Created by thaddeusreason on 16/02/2018.
 */

class HubViewModel: ViewModel() {
    var drawableId = ObservableField<Int>(-1)

    var name = ObservableField<String>("")

    var ipAddress = ObservableField<String>("")

    var id = ObservableField<String>("")

    fun setup(hubSearchFoundResult: HubSearchFoundResult) {
        this.drawableId.set(R.drawable.bridge_v2)
        this.name.set(hubSearchFoundResult.highestPriorityIndividualResult?.name ?: "")
        this.ipAddress.set(hubSearchFoundResult.highestPriorityIndividualResult?.primaryId ?: "")
        this.id.set(hubSearchFoundResult.highestPriorityIndividualResult?.secondaryId ?: "")
    }

    init {
        //drawableId.set(R.drawable.bridge_v2)
        //name.set("Hue Bridge V2")
        //ipAddress.set("192.168.0.1")
    }
}
