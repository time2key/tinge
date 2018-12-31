package com.thaddeussoftware.tinge.ui.lights.lightListFragment

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.thaddeussoftware.tinge.BR
import com.thaddeussoftware.tinge.R

import com.thaddeussoftware.tinge.databinding.FragmentLightListBinding
import me.tatarka.bindingcollectionadapter2.ItemBinding
import com.thaddeussoftware.tinge.helpers.ColorHelper
import com.thaddeussoftware.tinge.ui.lights.groupView.GroupViewModel
import com.thaddeussoftware.tinge.ui.mainActivity.MultiColouredToolbarActivity
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit


class LightListFragment : Fragment() {

    private val viewModel: LightListFragmentViewModel = LightListFragmentViewModel()

    private var binding: FragmentLightListBinding? = null

    private var listener: LightListFragmentListener? = null

    /**
     * Required to auto bind the light list RecyclerView to the viewModel
     * */
    //val lightListRecyclerViewItemBinding = ItemBinding.of<LightViewModel>(BR.viewModel, R.layout.holder_view_light)

    val groupListRecyclerViewItemBinding = ItemBinding.of<GroupViewModel>(BR.viewModel, R.layout.holder_view_group)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentLightListBinding.inflate(inflater, container, false)

        //val lightView = LightView(activity!!)
        //binding?.lightListLinearLayout?.removeAllViews()

        binding?.view = this
        binding?.viewModel = viewModel
        binding?.lightListLinearLayout?.setPadding(
                0,
                (activity as? MultiColouredToolbarActivity)?.topFragmentPadding ?: 0,
                0,
                (activity as? MultiColouredToolbarActivity)?.bottomFragmentPadding ?: 0)
        binding?.lightListLinearLayout?.clipToPadding = false

        viewModel.refreshListOfHubsAndLights()
        //binding?.lightListLinearLayout?.addView(lightView)

        return binding?.root
    }

    var runEverySecondDisposable: Disposable? = null

    override fun onResume() {
        super.onResume()
        if (runEverySecondDisposable == null) {
            runEverySecondDisposable = Observable.interval(1, 1, TimeUnit.SECONDS)
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        val colourList = ArrayList<Int>()

                        var previousColor:Int? = null

                        viewModel.individualGroupViewModels.forEach { groupViewModel ->
                            groupViewModel.individualLightViewModels.forEach {  lightViewModel ->
                                if (lightViewModel.lightController.isReachable) {
                                    val color = lightViewModel.colorForPreviewImageView.get() ?: 0
                                    val newColor =
                                            ColorHelper.colorFromHsv(
                                                    ColorHelper.hueFromColor(color),
                                                    if (lightViewModel.lightController.isOn.stagedValueOrLastValueFromHub == true) 0.32f + 0.32f*ColorHelper.saturationFromColor(color) else 0f,
                                                    if (lightViewModel.lightController.isOn.stagedValueOrLastValueFromHub == true) 1f else 0.65f)

                                    if (colourList.size > 0 && previousColor != null) {
                                        val halfWayColor = ColorHelper.mergeColorsPreservingSaturationAndValue(previousColor!!, newColor, 0.5f)
                                        for (j in 0..2) {
                                            colourList.add(ColorHelper.changeOpacityOfColor(halfWayColor, 0.6f))
                                        }
                                    }
                                    for (j in 0..if (lightViewModel.lightController.isOn.stagedValueOrLastValueFromHub == true) 12 else 4) {
                                        colourList.add(ColorHelper.changeOpacityOfColor(newColor, 0.85f))
                                    }
                                    previousColor = newColor
                                }
                            }

                        }

                        (activity as? MultiColouredToolbarActivity)?.setStatusBarAndToolbarToDrawable(GradientDrawable(GradientDrawable.Orientation.BL_TR, colourList.toIntArray()))
                        (activity as? MultiColouredToolbarActivity)?.setToolbarText("1 hub - 2 groups")
                    }
        }
    }

    override fun onPause() {
        super.onPause()
        runEverySecondDisposable?.dispose()
        runEverySecondDisposable = null
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is LightListFragmentListener) {
            listener = context
        } else {
            throw RuntimeException(context?.toString() + " must implement LightListFragmentListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface LightListFragmentListener {
    }

    companion object {

        fun newInstance(): LightListFragment {
            val fragment = LightListFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}
