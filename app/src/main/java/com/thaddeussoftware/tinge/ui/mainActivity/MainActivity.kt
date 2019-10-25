package com.thaddeussoftware.tinge.ui.mainActivity

import androidx.databinding.DataBindingUtil
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.core.graphics.ColorUtils
import com.thaddeussoftware.tinge.R
import com.thaddeussoftware.tinge.database.DatabaseSingleton
import com.thaddeussoftware.tinge.databinding.ActivityMainBinding
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.finder.HubSearchFoundResult
import com.thaddeussoftware.tinge.ui.hubs.connectToHubFragment.ConnectToHubFragment
import com.thaddeussoftware.tinge.ui.lights.lightListFragment.LightListFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import android.view.WindowManager
import android.util.TypedValue
import com.thaddeussoftware.tinge.helpers.ColorHelper


class MainActivity : AppCompatActivity(), LightListFragment.LightListFragmentListener, ConnectToHubFragment.ConnectToHubFragmentListener, MultiColouredToolbarActivity {

    val statusBarHeight: Int by lazy {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        result
    }


    val actionBarHeight: Int by lazy {
        var actionBarHeight = 0
        val tv = TypedValue()
        if (theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
        }
        actionBarHeight
    }

    val navigationBarHeight: Int by lazy {
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else 0
    }

    private var binding:ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setBackgroundDrawable(ColorDrawable(0xaaaa0000.toInt()))
        window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // Uncomment to make top status bar text black:
        //window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        // Uncomment to make toolbar text white:
        //binding?.toolbar?.setTitleTextColor(0xffffffff.toInt())

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(binding?.toolbar)
        binding?.statusBarBackgroundView?.layoutParams?.height = statusBarHeight
        binding?.toolbarHolder?.layoutParams?.height = actionBarHeight + statusBarHeight


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

    override fun setStatusBarAndToolbarToDrawable(drawable: Drawable) {
        binding?.toolbarHolder?.background = drawable
    }

    override fun setToolbarText(text: String) {
        binding?.toolbar?.title = "Tinge"
        binding?.toolbar?.subtitle = text
    }

    override val topFragmentPadding: Int
        get() = statusBarHeight + actionBarHeight

    override val bottomFragmentPadding: Int
        get() = navigationBarHeight


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

interface MultiColouredToolbarActivity {

    /**
     * The status bar and toolbar are shown on top of fragments at the top of the screen.
     * */
    val topFragmentPadding: Int

    val bottomFragmentPadding: Int

    fun setStatusBarAndToolbarToDrawable(drawable: Drawable)

    fun setToolbarText(text: String)
}
