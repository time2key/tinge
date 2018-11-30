package com.thaddeussoftware.tinge.ui.lights.groupView

import android.content.Context
import android.graphics.Color
import android.support.v4.graphics.ColorUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.thaddeussoftware.tinge.BR
import com.thaddeussoftware.tinge.R
import com.thaddeussoftware.tinge.databinding.ViewGroupBinding
import com.thaddeussoftware.tinge.databinding.ViewLightBinding
import com.thaddeussoftware.tinge.ui.lights.lightView.LightViewModel
import me.tatarka.bindingcollectionadapter2.ItemBinding

class GroupView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        viewModel: GroupViewModel? = null,
        defStyle: Int = 0): FrameLayout(context, attrs, defStyle) {

    private val HSV_SATURATION = 1f
    private val HSV_VALUE = 1.0f//0.95f

    var viewModel: GroupViewModel? = viewModel
        set(value) {
            field = value
            removeAllViews()
            binding = ViewGroupBinding.inflate(LayoutInflater.from(context), this, true)
            binding.view = this
            binding.viewModel = viewModel
            binding.lightListLinearLayout.invalidate()
            setupBrightnessSlider()
        }

    /**
     * Required to auto bind the light list RecyclerView to the viewModel
     * */
    val lightListRecyclerViewItemBinding = ItemBinding.of<LightViewModel>(BR.viewModel, R.layout.holder_view_light)

    private var binding: ViewGroupBinding = ViewGroupBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        binding.view = this
        binding.viewModel = viewModel
    }

    fun setupBrightnessSlider() {
        val color2 = getColorFromHsv(viewModel?.meanHue?.get() ?: 0f, viewModel?.meanSaturation?.get() ?: 0f*HSV_SATURATION, HSV_VALUE)
        val color1 = mergeColors(0xff444444.toInt(), color2, 0.1f)
        binding.brightnessSeekBar.setTrackToColors(color1, color2)
        binding.brightnessSeekBar.setHandleToAutoColors(color1, color2)
    }

    private fun getColorFromHsv(h:Float, s:Float, v:Float) = Color.HSVToColor(floatArrayOf(h*360f, s, v))

    private fun mergeColors(color1: Int, color2: Int, mergeAmount: Float) = ColorUtils.blendARGB(color1, color2, mergeAmount)
}