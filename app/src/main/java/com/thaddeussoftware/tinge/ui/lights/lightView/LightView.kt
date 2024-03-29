package com.thaddeussoftware.tinge.ui.lights.lightView

import android.content.Context
import androidx.databinding.Observable
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import androidx.core.graphics.ColorUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.thaddeussoftware.tinge.R
import com.thaddeussoftware.tinge.databinding.ViewLightBinding
import com.thaddeussoftware.tinge.helpers.UiHelper
import com.thaddeussoftware.tinge.ui.lights.LightsUiHelper


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

        viewModel?.lightController?.hue?.stagedValueOrLastValueFromHubObservable?.addOnPropertyChangedCallback( object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(p0: Observable?, p1: Int) {
                setupBrightnessSlider()
                setupSaturationTrack()
            }
        })

        viewModel?.lightController?.saturation?.stagedValueOrLastValueFromHubObservable?.addOnPropertyChangedCallback( object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(p0: Observable?, p1: Int) {
                setupBrightnessSlider()
            }
        })

        viewModel?.lightController?.isOn?.stagedValueOrLastValueFromHubObservable?.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                setupBrightnessSlider()
            }
        })
    }

    private fun setupSliders() {

        var bitmap = BitmapFactory.decodeResource(resources, getImage())
        binding.innerLightView.leftImageView.setImageDrawable(
                BitmapDrawable(resources, UiHelper.whiteTintBitmapPhotographOfLight(bitmap)))

        setupBrightnessSlider()
        setupHueSlider()
        setupSaturationTrack()
        setupWhiteTrack()
    }

    private fun getImage(): Int {
        val name = viewModel?.displayName?.get() ?: ""
        if (name.startsWith("Overhead light")) {
            return R.drawable.top_light_image
        } else if (name == "Large desk lamp") {
            return R.drawable.selenite_lamp_1
        } else if (name == "Small desk lamp") {
            return R.drawable.selenite_lamp_2
        } else if (name == "Medium desk lamp") {
            return R.drawable.selenite_lamp_3
        } else if (name == "Large sideboard lamp") {
            return R.drawable.selenite_lamp_side_2
        } else if (name == "Twin peak sideboard lamp") {
            return R.drawable.selenite_lamp_side_2
        } else {
            return R.drawable.left_selenite_lamp
        }
    }

    fun setupBrightnessSlider() {
        val hue = viewModel?.lightController?.hue?.stagedValueOrLastValueFromHub ?: 0f
        val saturation = viewModel?.lightController?.saturation?.stagedValueOrLastValueFromHub ?: 0f

        binding.innerLightView.brightnessSeekBar.setTrackToColors(
                LightsUiHelper.getColorForBrightnessSliderTrack(hue, saturation, 0f,
                        viewModel?.lightController?.isOn?.stagedValueOrLastValueFromHub == true),
                LightsUiHelper.getColorForBrightnessSliderTrack(hue, saturation, 1f,
                        viewModel?.lightController?.isOn?.stagedValueOrLastValueFromHub == true))
    }

    fun setupHueSlider() {
        val colors = IntArray(20)
        for (i in 0..19) {
            colors[i] = getColorFromHsv(i/19f, HSV_SATURATION, HSV_VALUE)
        }
        binding.innerLightView.hueSeekBar.setTrackToColors(*colors)
    }

    fun setupSaturationTrack() {
        val color2 = getColorFromHsv(viewModel?.lightController?.hue?.stagedValueOrLastValueFromHub ?: 0f, HSV_SATURATION, HSV_VALUE)
        binding.innerLightView.saturationSeekBar.setTrackToColors(0xffeeeeee.toInt(), color2)
    }

    fun setupWhiteTrack() {
        val colors = IntArray(20)
        for (i in 0..19) {
            colors[i] = viewModel?.getColorFromWhiteAmount(i/20.0) ?: 0
        }
        binding.innerLightView.whiteSeekView.setTrackToColors(*colors)
    }


    private fun getColorFromHsv(h:Float, s:Float, v:Float) = Color.HSVToColor(floatArrayOf(h*360f, s, v))

    private fun mergeColors(color1: Int, color2: Int, mergeAmount: Float) = ColorUtils.blendARGB(color1, color2, mergeAmount)
}