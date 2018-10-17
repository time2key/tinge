package com.thaddeussoftware.tinge.ui.lights.lightListFragment

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableArrayList
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.thaddeussoftware.tinge.database.DatabaseSingleton
import com.thaddeussoftware.tinge.database.phillipsHue.hubs.HueHubsDao
import com.thaddeussoftware.tinge.database.phillipsHue.lights.HueLightsDao
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.controller.HubController
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.controller.LightGroupController
import com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.controller.HueHubController
import com.thaddeussoftware.tinge.helpers.CollectionComparisonHelper
import com.thaddeussoftware.tinge.ui.lights.lightView.LightViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LightListFragmentViewModel(
        val retrofitBuilder: Retrofit.Builder = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()),
        val hueHubsDao: HueHubsDao = DatabaseSingleton.database.hueHubsDao(),
        val hueLightsDao: HueLightsDao = DatabaseSingleton.database.hueLightsDao()
): ViewModel() {

    /**
     * View models of each of the lights that should be shown on screen
     * */
    var individualLightViewModels = ObservableArrayList<LightViewModel>()

    /*init {
        hueHubsDao.getAllSavedHueHubs()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { listResult ->
                    listResult.forEach {

                    }
                }
    }*/

    /**
     * All of the hub controllers currently added in the app
     * */
    private var hubControllers = HashMap<String, HubController>()

    init {
        //refreshListOfHubsAndLights()
    }

    /**
     * Refreshes [hubControllers] to match the current database of stored hubs, then calls
     * [refreshListOfLights] upon completion.
     * */
    fun refreshListOfHubsAndLights() {
        hueHubsDao.getAllSavedHueHubs()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { hubList ->
                    hubList.forEach {
                        if (hubControllers[it.hubId] == null) {
                            // Add new controller:
                            hubControllers[it.hubId] = HueHubController(it.lastKnownIpAddress, it.hubId, it.lastKnownHubName, it.usernameCredentials)
                        } else {
                            // Update existing controller:
                            // TODO
                        }
                    }
                    refreshListOfLights()
                }
    }

    /**
     * Contacts each hub to get a list of all lights, then updates the database and updates
     * this view model to reflect the lights scanned.
     * */
    fun refreshListOfLights() {
        hubControllers.forEach { entry ->
            val hubController = entry.value
            hubController
                    .refresh(LightGroupController.DataInGroupType.LIGHTS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        CollectionComparisonHelper.compareCollectionsAndIdentifyMissingElements(
                                individualLightViewModels.filter { it.lightController.hubController == hubController },
                                hubController.lightsInGroupOrSubgroups,
                                { lightViewModel, lightController ->
                                    lightViewModel.lightController == lightController
                                },
                                {
                                    //if (it.lightController.hubController == hubController) {
                                        individualLightViewModels.remove(it)
                                    //}
                                },
                                {
                                    individualLightViewModels.add(LightViewModel(it))
                                },
                                { lightViewModel, _ ->
                                    lightViewModel.refreshToMatchController()
                                }
                        )
                    }

        }
    }
}