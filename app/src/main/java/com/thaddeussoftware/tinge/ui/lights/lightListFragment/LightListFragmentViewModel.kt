package com.thaddeussoftware.tinge.ui.lights.lightListFragment

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.databinding.Observable
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableField
import android.os.Handler
import android.os.Looper
import com.thaddeussoftware.tinge.database.DatabaseSingleton
import com.thaddeussoftware.tinge.database.phillipsHue.hubs.HueHubsDao
import com.thaddeussoftware.tinge.database.phillipsHue.lights.HueLightsDao
import com.thaddeussoftware.tinge.tingeapi.generic.controller.HubController
import com.thaddeussoftware.tinge.tingeapi.philipsHue.controller.HueHubController
import com.thaddeussoftware.tinge.helpers.CollectionComparisonHelper
import com.thaddeussoftware.tinge.ui.lights.groupView.GroupViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class LightListFragmentViewModel(
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