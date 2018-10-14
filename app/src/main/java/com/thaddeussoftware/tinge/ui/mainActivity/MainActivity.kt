package com.thaddeussoftware.tinge.ui.mainActivity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import com.thaddeussoftware.tinge.R
import com.thaddeussoftware.tinge.database.DatabaseSingleton
import com.thaddeussoftware.tinge.databinding.ActivityMainBinding
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.finder.HubSearchFoundResult
import com.thaddeussoftware.tinge.ui.hubs.connectToHubFragment.ConnectToHubFragment
import com.thaddeussoftware.tinge.ui.lights.lightListFragment.LightListFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class MainActivity : AppCompatActivity(), LightListFragment.LightListFragmentListener, ConnectToHubFragment.ConnectToHubFragmentListener {

    private var binding:ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))

        DatabaseSingleton.database.hueHubsDao().getAllSavedHueHubs()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { resultList ->
            if (resultList.isEmpty()) {
                openLightListFragment()
                openConnectToHubFragment()
            } else {
                openLightListFragment()
            }
        }

    }

    private fun openConnectToHubFragment() {
        openNewFragment(ConnectToHubFragment.newInstance())
    }

    private fun openLightListFragment() {
        openNewFragment(LightListFragment.newInstance())
    }

    private fun openNewFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(binding?.fragmentHolder!!.id, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commitAllowingStateLoss()
    }

    private fun goBackAFragment() {
        supportFragmentManager.popBackStack()
    }

    override fun connectToHubFragmentDeviceAdded(hubSearchFoundResult: HubSearchFoundResult) {
        goBackAFragment()
    }
}
