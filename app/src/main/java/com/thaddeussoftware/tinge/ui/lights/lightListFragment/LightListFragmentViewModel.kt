package com.thaddeussoftware.tinge.ui.lights.lightListFragment

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModel
import android.databinding.Observable
import android.databinding.ObservableArrayList
import android.databinding.ObservableField
import android.os.Handler
import android.os.Looper
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.thaddeussoftware.tinge.database.DatabaseSingleton
import com.thaddeussoftware.tinge.database.phillipsHue.hubs.HueHubsDao
import com.thaddeussoftware.tinge.database.phillipsHue.lights.HueLightsDao
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.controller.HubController
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.controller.LightGroupController
import com.thaddeussoftware.tinge.deviceControlLibrary.philipsHue.controller.HueHubController
import com.thaddeussoftware.tinge.helpers.CollectionComparisonHelper
import com.thaddeussoftware.tinge.ui.lights.groupView.GroupViewModel
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

    var individualGroupViewModels = ObservableArrayList<GroupViewModel>()

    val onAnyLightInAnyHubUpdatedLiveEvent = ObservableField<Any>()

    /**
     * All of the hub controllers currently added in the app
     * */
    private var hubControllers = HashMap<String, HubController>()

    init {
        //refreshListOfHubsAndLights()
    }


    /**
     * Refreshes [hubControllers] to match the current database of stored hubs, then calls
     * [refreshListOfGroups] upon completion.
     * */
    @SuppressLint("CheckResult")
    fun refreshListOfHubsAndLights() {
        hueHubsDao.getAllSavedHueHubs()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { hubList ->
                    hubList.forEach {
                        if (hubControllers[it.hubId] == null) {
                            // Add new controller:
                            val hueHubController = HueHubController(it.lastKnownIpAddress, it.hubId, it.lastKnownHubName, it.usernameCredentials)
                            hubControllers[it.hubId] = hueHubController
                            hueHubController.startUpdateThread()

                            hueHubController.onLightsOrSubgroupsAddedOrRemovedSingleLiveEvent.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
                                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                                    Handler(Looper.getMainLooper()).post() {
                                        refreshListOfGroupsForHub(hueHubController)
                                    }
                                }
                            })
                            refreshListOfGroupsForHub(hueHubController)

                            hueHubController.onLightPropertyModifiedSingleLiveEvent
                                    .stagedValueOrValueFromHubUpdatedLiveEvent
                                    .addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
                                        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                                            onAnyLightInAnyHubUpdatedLiveEvent.notifyChange()
                                        }
                                    })
                        } else {
                            // Update existing controller:
                            // TODO
                        }
                    }
                }
    }

    fun refreshListOfGroupsForHub(hubController: HubController) {
        CollectionComparisonHelper.compareCollectionsAndIdentifyMissingElements(
                individualGroupViewModels.filter { it.lightGroupController.hubController == hubController },
                hubController.lightGroups,
                { groupViewModel, lightGroupController ->
                    groupViewModel.lightGroupController == lightGroupController
                },
                {
                    //if (it.lightController.hubController == hubController) {
                    individualGroupViewModels.remove(it)
                    //}
                },
                {
                    individualGroupViewModels.add(GroupViewModel(it))
                },
                { groupViewModel, _ ->
                    groupViewModel.refreshListOfLightsToMatchController()
                }
        )
    }


}