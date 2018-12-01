package com.thaddeussoftware.tinge.ui.lights.lightView

import android.content.Context
import android.databinding.Observable
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.support.v4.graphics.ColorUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.thaddeussoftware.tinge.R
import com.thaddeussoftware.tinge.databinding.ViewLightBinding
import com.thaddeussoftware.tinge.helpers.UiHelper


/**
 * Created by thaddeusreason on 20/01/2018.
 */
class LightView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        viewModel: LightViewModel? = null,
        defStyle: Int = 0): FrameLayout(context, attrs, defStyle) {

    var viewModel: LightViewModel? = viewModel
    set(value) {
        field = value
        setupSliders()
        setupViewModel()
    }

    private val HSV_SATURATION = 1f
    private val HSV_VALUE = 1.0f//0.95f

    private var binding: ViewLightBinding = ViewLightBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        setupSliders()
        setupViewModel()
    }

    private fun setupViewModel() {
        binding.viewModel = viewModel

        viewModel?.hue?.addOnPropertyChangedCallback( object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(p0: Observable?, p1: Int) {
                setupBrightnessSlider()
                setupSaturationTrack()
            }
        })

        viewModel?.saturation?.addOnPropertyChangedCallback( object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(p0: Observable?, p1: Int) {
                setupBrightnessSlider()
            }
        })
    }

    private fun setupSliders() {

        var bitmap = BitmapFactory.decodeResource(resources, if (Math.random()<0.5f) R.drawable.top_light_image else R.drawable.left_selenite_lamp)
        binding.leftImageView.setImageDrawable(
                BitmapDrawable(resources, UiHelper.whiteTintBitmapPhotographOfLight(bitmap)))

        setupBrightnessSlider()
        setupHueSlider()
        setupSaturationTrack()
        setupWhiteTrack()

        binding.brightnessSeekBar.slidAmount = viewModel?.brightness?.get() ?: 0f

        binding.hueSeekBar.slidAmount = viewModel?.hue?.get() ?: 0f

        binding.saturationSeekBar.slidAmount = viewModel?.saturation?.get() ?: 0f
    }

    fun setupBrightnessSlider() {
        val color2 = getColorFromHsv(viewModel?.hue?.get() ?: 0f, viewModel?.saturation?.get() ?: 0f*HSV_SATURATION, HSV_VALUE)
        val color1 = mergeColors(0xff444444.toInt(), color2, 0.1f)
        binding.brightnessSeekBar.setTrackToColors(color1, color2)
        binding.brightnessSeekBar.setHandleToAutoColors(color1, color2)
    }

    fun setupHueSlider() {
        val colors = IntArray(20)
        for (i in 0..19) {
            colors[i] = getColorFromHsv(i/19f, HSV_SATURATION, HSV_VALUE)
        }
        binding.hueSeekBar.setTrackToColors(*colors)
        binding.hueSeekBar.setHandleToAutoColors(*colors)
    }

    fun setupSaturationTrack() {
        val color2 = getColorFromHsv(viewModel?.hue?.get() ?: 0f, HSV_SATURATION, HSV_VALUE)
        binding.saturationSeekBar.setTrackToColors(0xffeeeeee.toInt(), color2)
        binding.saturationSeekBar.setHandleToAutoColors(0xffeeeeee.toInt(), color2)
    }

    fun setupWhiteTrack() {
        val colors = IntArray(20)
        for (i in 0..19) {
            colors[i] = viewModel?.getColorFromWhiteAmount(i/20.0) ?: 0
        }
        binding.whiteSeekView.setTrackToColors(*colors)
        binding.whiteSeekView.setHandleToAutoColors(*colors)
    }


    private fun getColorFromHsv(h:Float, s:Float, v:Float) = Color.HSVToColor(floatArrayOf(h*360f, s, v))

    private fun mergeColors(color1: Int, color2: Int, mergeAmount: Float) = ColorUtils.blendARGB(color1, color2, mergeAmount)
}