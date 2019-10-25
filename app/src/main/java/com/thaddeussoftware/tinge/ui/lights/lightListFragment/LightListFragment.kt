package com.thaddeussoftware.tinge.ui.lights.lightListFragment

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.Observable
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.thaddeussoftware.tinge.BR
import com.thaddeussoftware.tinge.R

import com.thaddeussoftware.tinge.databinding.FragmentLightListBinding
import me.tatarka.bindingcollectionadapter2.ItemBinding
import com.thaddeussoftware.tinge.helpers.ColorHelper
import com.thaddeussoftware.tinge.helpers.UiHelper
import com.thaddeussoftware.tinge.ui.lights.WeightedStripedColorDrawable
import com.thaddeussoftware.tinge.ui.lights.LightsUiHelper
import com.thaddeussoftware.tinge.ui.lights.groupView.GroupViewModel
import com.thaddeussoftware.tinge.ui.lights.lightView.LightView
import com.thaddeussoftware.tinge.ui.lights.lightView.LightViewModel
import com.thaddeussoftware.tinge.ui.mainActivity.MultiColouredToolbarActivity
import kotlin.math.max
import kotlin.math.min


class LightListFragment : Fragment() {

    private val viewModel: LightListFragmentViewModel = LightListFragmentViewModel()

    private var binding: FragmentLightListBinding? = null

    private var listener: LightListFragmentListener? = null

    private var toolbarDrawable: WeightedStripedColorDrawable? = null

    val groupListRecyclerViewItemBinding = ItemBinding.of<GroupViewModel>(BR.viewModel, R.layout.holder_view_group)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
        }
        toolbarDrawable = WeightedStripedColorDrawable(UiHelper.getPxFromDp(context!!, 1f))
        (activity as? MultiColouredToolbarActivity)?.setStatusBarAndToolbarToDrawable(toolbarDrawable!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentLightListBinding.inflate(inflater, container, false)

        binding?.view = this
        binding?.viewModel = viewModel
        binding?.lightListRecyclerView?.setPadding(
                0,
                (activity as? MultiColouredToolbarActivity)?.topFragmentPadding ?: 0,
                0,
                (activity as? MultiColouredToolbarActivity)?.bottomFragmentPadding ?: 0)
        binding?.lightListRecyclerView?.clipToPadding = false

        binding?.lightListRecyclerView?.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                redrawGlassToolbar()
            }
        })

        viewModel.onAnyLightInAnyHubUpdatedLiveEvent.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                redrawGlassToolbar()
            }
        })

        viewModel.refreshListOfHubsAndLights()

        return binding?.root
    }

    private val lightListViewMap = HashMap<LightViewModel, LightView>()

    private fun iterateThroughAllViewChildrenLookingFor(
            parentView: ViewGroup, viewTypeLookingFor: Class<out View>, runWhenFound: (View) -> Unit) {
        for (i in 0..parentView.childCount) {
            val childView = parentView.getChildAt(i)
            if (viewTypeLookingFor.isInstance(childView)) {
                runWhenFound(childView)
            } else if (childView is ViewGroup) {
                iterateThroughAllViewChildrenLookingFor(childView, viewTypeLookingFor, runWhenFound)
            }
        }
    }

    private fun populateLightListViewMap() {
        lightListViewMap.clear()
        iterateThroughAllViewChildrenLookingFor(binding!!.root as ViewGroup, LightView::class.java) {
            lightListViewMap.put((it as LightView).viewModel!!, it)
        }
    }

    private fun redrawGlassToolbar() {
        populateLightListViewMap()

        val colourList = ArrayList<WeightedStripedColorDrawable.GlassToolbarWeightedColor>()

        var previousColor:Int? = null

        viewModel.individualGroupViewModels.forEach { groupViewModel ->
            groupViewModel.individualLightViewModels.forEach {  lightViewModel ->
                if (lightViewModel.lightController.isReachable.get() == true) {

                    val hue = lightViewModel.lightController.hue.stagedValueOrLastValueFromHub ?: 0f
                    val sat = lightViewModel.lightController.saturation.stagedValueOrLastValueFromHub ?: 0f
                    val brightness = lightViewModel.lightController.brightness.stagedValueOrLastValueFromHub ?: 0f
                    val isOn = lightViewModel.lightController.isOn.stagedValueOrLastValueFromHub ?: false

                    val newColor = LightsUiHelper.getGlassToolbarColorFromLightColor(hue, sat, brightness, isOn)

                    if (colourList.size > 0 && previousColor != null) {
                        val halfWayColor = ColorHelper.mergeColorsPreservingSaturationAndValue(previousColor!!, newColor, 0.5f)
                        colourList.add(
                                WeightedStripedColorDrawable.GlassToolbarWeightedColor(
                                        ColorHelper.changeOpacityOfColor(halfWayColor, 0.6f),
                                        12f, 0f)
                        )
                    }

                    var lightHeight = 0f
                    val locationOnScreen = IntArray(2)
                    if (lightListViewMap.containsKey(lightViewModel)) {
                        lightListViewMap[lightViewModel]?.getLocationOnScreen(locationOnScreen)
                        var yOnScreen = locationOnScreen[1]
                        var y2OnScreen = locationOnScreen[1] + lightListViewMap[lightViewModel]!!.height

                        val screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels + (activity as MultiColouredToolbarActivity).bottomFragmentPadding
                        val screen0 = (activity as MultiColouredToolbarActivity).topFragmentPadding

                        yOnScreen = min(max(0, yOnScreen - screen0), screenHeight - screen0)
                        y2OnScreen = min(max(0, y2OnScreen - screen0), screenHeight - screen0)
                        lightHeight = (y2OnScreen - yOnScreen).toFloat()
                    }

                    colourList.add(
                            WeightedStripedColorDrawable.GlassToolbarWeightedColor(
                                    ColorHelper.changeOpacityOfColor(newColor, 0.85f),
                                    if (lightViewModel.lightController.isOn.stagedValueOrLastValueFromHub == true) 24f else 12f,
                                    if (lightViewModel.lightController.isOn.stagedValueOrLastValueFromHub == true) lightHeight+0.001f else lightHeight*0.5f))

                    previousColor = newColor
                }
            }

        }

        toolbarDrawable?.weightedColors = colourList
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is LightListFragmentListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement LightListFragmentListener")
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
