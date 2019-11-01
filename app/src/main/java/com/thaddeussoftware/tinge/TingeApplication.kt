package com.thaddeussoftware.tinge

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDexApplication
import com.thaddeussoftware.tinge.tingeapi.philipsHue.dagger.HueControlLibraryComponent

/**
 * Created by thaddeusreason on 14/01/2018.
 */

class TingeApplication: MultiDexApplication() {
    companion object {
        var tingeApplication: Context? = null
    }

    init {
        tingeApplication = this
    }
}
