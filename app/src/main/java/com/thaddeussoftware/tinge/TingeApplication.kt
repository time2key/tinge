package com.thaddeussoftware.tinge

import android.app.Application
import android.content.Context
import com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.dagger.HueControlLibraryComponent

/**
 * Created by thaddeusreason on 14/01/2018.
 */

class TingeApplication: Application() {
    companion object {
        var tingeApplication: Context? = null
    }

    lateinit var hueControlLibraryComponent:HueControlLibraryComponent;

    init {
        tingeApplication = this
    }

    override fun onCreate() {
        super.onCreate()

        //hueControlLibraryComponent =
        //        DaggerHueControlLibraryComponent.builder()
        //                .hueControlLibraryModule(HueControlLibraryModule())
        //                .build()
    }
}
