package com.thaddeussoftware.tinge.ui.lights.groupView

import android.content.Context
import android.databinding.Observable
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.thaddeussoftware.tinge.BR
import com.thaddeussoftware.tinge.R
import com.thaddeussoftware.tinge.databinding.ViewGroupBinding
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.controller.LightController
import com.thaddeussoftware.tinge.helpers.ColorHelper
import com.thaddeussoftware.tinge.helpers.UiHelper
import com.thaddeussoftware.tinge.ui.lights.LightsUiHelper
import com.thaddeussoftware.tinge.ui.lights.WeightedStripedColorDrawable
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

            viewModel?.meanHue?.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(p0: Observable?, p1: Int) {
                    setupGroupImage()
                    setupBackgroundImage()
                    setupBrightnessSlider()
                    setupSaturationTrack()
                }
            })
            viewModel?.meanBrightness?.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(p0: Observable?, p1: Int) {
                    setupGroupImage()
                    setupBackgroundImage()
                }
            })
            viewModel?.meanSaturation?.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(p0: Observable?, p1: Int) {
                    setupGroupImage()
                    setupBackgroundImage()
                    setupBrightnessSlider()
                }
            })

            viewModel?.lightGroupController?.onLightsOrSubgroupsAddedOrRemovedSingleLiveEvent?.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
                    Log.v("tinge", "GroupView light bitmaps re-setup")
                    setupLightBitmaps()
                }
            })
            setupLightBitmaps()

            setupHueSlider()
            setupBrightnessSlider()
            setupSaturationTrack()
            setupGroupImage()
            setupBackgroundImage()
        }

    private fun getImage(lightController: LightController): Int {
        val name = lightController.displayName.lastValueRetrievedFromHub ?: ""
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


    /**
     * Required to auto bind the light list RecyclerView to the viewModel
     * */
    val lightListRecyclerViewItemBinding = ItemBinding.of<LightViewModel>(BR.viewModel, R.layout.holder_view_light)

    private var binding: ViewGroupBinding = ViewGroupBinding.inflate(LayoutInflater.from(context), this, true)

    private val backgroundDrawable = WeightedStripedColorDrawable(UiHelper.getPxFromDp(context, 1f))

    private val individualLightBitmaps = ArrayList<Bitmap>()

    init {
        binding.view = this
        binding.viewModel = viewModel
    }

    private fun setupLightBitmaps() {
        individualLightBitmaps.clear()
        viewModel?.lightGroupController?.lightsNotInSubgroup?.forEach {
            individualLightBitmaps.add(
                    UiHelper.whiteTintBitmapPhotographOfLight(BitmapFactory.decodeResource(resources, getImage(it)))
            )
        }
        Log.v("tinge", "GroupView light bitmaps setup - ${individualLightBitmaps.size} bitmaps")
    }

    private fun setupBrightnessSlider() {
        val hue = viewModel?.meanHue?.get() ?: 0f
        val saturation = viewModel?.meanSaturation?.get() ?: 0f

        binding.innerLightView.brightnessSeekBar.setTrackToColors(
                LightsUiHelper.getColorForBrightnessSlider(hue, saturation, 0f, true),
                LightsUiHelper.getColorForBrightnessSlider(hue, saturation, 1f, true))
    }

    fun setupHueSlider() {
        val colors = IntArray(20)
        for (i in 0..19) {
            colors[i] = ColorHelper.colorFromHsv(i/19f, HSV_SATURATION, HSV_VALUE)
        }
        binding.innerLightView.hueSeekBar.setTrackToColors(*colors)
    }

    fun setupSaturationTrack() {
        val color2 = ColorHelper.colorFromHsv(viewModel?.meanHue?.get() ?: 0f, HSV_SATURATION, HSV_VALUE)
        binding.innerLightView.saturationSeekBar.setTrackToColors(0xffeeeeee.toInt(), color2)
    }

    private fun setupGroupImage() {
        binding.innerLightView.leftImageView.setImageDrawable(BitmapDrawable(resources, getBitmapForGroupImage()))
    }

    private fun setupBackgroundImage() {
        val weightedColors = ArrayList<WeightedStripedColorDrawable.GlassToolbarWeightedColor>()

        viewModel?.individualLightViewModels?.forEach {
            val colour = LightsUiHelper.getFadedBackgroundColourFromLightColour(
                    it.lightController.hue.stagedValueOrLastValueFromHub,
                    it.lightController.saturation.stagedValueOrLastValueFromHub,
                    it.lightController.brightness.stagedValueOrLastValueFromHub,
                    it.lightController.isOn.stagedValueOrLastValueFromHub)
            val weight = if (it.lightController.isOn.stagedValueOrLastValueFromHub != true) 0.5f else 1f
            weightedColors.add(WeightedStripedColorDrawable.GlassToolbarWeightedColor(colour, 0f, weight))
        }
        backgroundDrawable.weightedColors = weightedColors
        binding.innerLightView.root.background = backgroundDrawable
    }

    private fun getBitmapForGroupImage(): Bitmap {

        val canvas = Canvas()
        val width = 200
        val height = 200
        val returnValue = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        canvas.setBitmap(returnValue)

        val paint = Paint()

        if (individualLightBitmaps.size == 1) {
            val bitmap = individualLightBitmaps.get(0)
            canvas.drawBitmap(bitmap,
                    Rect(0, 0, bitmap.width, bitmap.height),
                    Rect(0, 0, width, height),
                    paint)
            canvas.drawColor(LightsUiHelper.getPreviewImageTintColourFromLightController(
                    viewModel?.lightGroupController?.lightsNotInSubgroup?.getOrNull(0)),
                    PorterDuff.Mode.MULTIPLY)
        } else {
            individualLightBitmaps.forEachIndexed { i, bitmap ->
                val startAngle = Math.PI * 2 * i / individualLightBitmaps.size
                val endAngle = Math.PI * 2 * (i + 1) / individualLightBitmaps.size
                val midAngle = startAngle * 0.5 + endAngle * 0.5

                val path = Path()
                path.moveTo(width * 0.5f, height * 0.5f)
                path.lineTo(width * 0.5f + width * Math.sin(startAngle).toFloat(),
                        height * 0.5f + height * Math.cos(startAngle).toFloat())
                path.lineTo(width * 0.5f + width * Math.sin(midAngle).toFloat(),
                        height * 0.5f + height * Math.cos(midAngle).toFloat())
                path.lineTo(width * 0.5f + width * Math.sin(endAngle).toFloat(),
                        height * 0.5f + height * Math.cos(endAngle).toFloat())
                path.close()
                canvas.save()
                canvas.clipPath(path)

                val directionShiftX = 20f * Math.sin(midAngle).toFloat()
                val directionShiftY = 20f * Math.cos(midAngle).toFloat()

                canvas.drawBitmap(bitmap,
                        Rect(0, 0, bitmap.width, bitmap.height),
                        Rect(directionShiftX.toInt(), directionShiftY.toInt(), width + directionShiftX.toInt(), height + directionShiftY.toInt()),
                        paint)

                canvas.drawColor(
                        LightsUiHelper.getPreviewImageTintColourFromLightController(
                                viewModel?.lightGroupController?.lightsNotInSubgroup?.getOrNull(i)),
                        PorterDuff.Mode.MULTIPLY)

                canvas.restore()
            }
        }

        return returnValue
    }

}