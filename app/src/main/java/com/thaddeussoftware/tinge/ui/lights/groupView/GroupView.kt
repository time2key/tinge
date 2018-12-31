package com.thaddeussoftware.tinge.ui.lights.groupView

import android.content.Context
import android.databinding.Observable
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.support.v4.graphics.ColorUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.thaddeussoftware.tinge.BR
import com.thaddeussoftware.tinge.R
import com.thaddeussoftware.tinge.databinding.ViewGroupBinding
import com.thaddeussoftware.tinge.databinding.ViewLightBinding
import com.thaddeussoftware.tinge.helpers.UiHelper
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
                    setupBrightnessSlider()
                    setupSaturationTrack()
                }
            })
            viewModel?.meanBrightness?.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(p0: Observable?, p1: Int) {
                    setupGroupImage()
                }
            })
            viewModel?.meanSaturation?.addOnPropertyChangedCallback(object: Observable.OnPropertyChangedCallback() {
                override fun onPropertyChanged(p0: Observable?, p1: Int) {
                    setupGroupImage()
                    setupBrightnessSlider()
                }
            })

            viewModel?.lightGroupController?.lightsNotInSubgroup?.forEach {
                individualBitmaps.add(
                        UiHelper.whiteTintBitmapPhotographOfLight(BitmapFactory.decodeResource(resources, if (Math.random()<0.5f) R.drawable.top_light_image else R.drawable.left_selenite_lamp))
                )
            }

            setupHueSlider()
            setupBrightnessSlider()
            setupSaturationTrack()
            setupGroupImage()
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

    private fun setupBrightnessSlider() {
        val color2 = getColorFromHsv(viewModel?.meanHue?.get() ?: 0f, viewModel?.meanSaturation?.get() ?: 0f*HSV_SATURATION, HSV_VALUE)
        val color1 = mergeColors(0xff444444.toInt(), color2, 0.1f)
        binding.innerLightView.brightnessSeekBar.setTrackToColors(color1, color2)
    }

    fun setupHueSlider() {
        val colors = IntArray(20)
        for (i in 0..19) {
            colors[i] = getColorFromHsv(i/19f, HSV_SATURATION, HSV_VALUE)
        }
        binding.innerLightView.hueSeekBar.setTrackToColors(*colors)
    }

    fun setupSaturationTrack() {
        val color2 = getColorFromHsv(viewModel?.meanHue?.get() ?: 0f, HSV_SATURATION, HSV_VALUE)
        binding.innerLightView.saturationSeekBar.setTrackToColors(0xffeeeeee.toInt(), color2)
    }

    private fun setupGroupImage() {
        binding.innerLightView.leftImageView.setImageDrawable(BitmapDrawable(resources, getBitmapForGroupImage()))
    }

    private val individualBitmaps = ArrayList<Bitmap>()


    private fun getBitmapForGroupImage(): Bitmap {

        val canvas = Canvas()
        val width = 200
        val height = 200
        val returnValue = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        canvas.setBitmap(returnValue)

        val paint = Paint()

        individualBitmaps.forEachIndexed { i, bitmap ->
            val startAngle = Math.PI * 2 * i / individualBitmaps.size
            val endAngle = Math.PI * 2 * (i + 1) / individualBitmaps.size
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

            canvas.drawColor(viewModel?.individualLightViewModels?.get(i)?.colorForPreviewImageView?.get() ?: 0xffffffff.toInt(), PorterDuff.Mode.MULTIPLY)

            canvas.restore()
        }

        return returnValue
    }

    private fun getColorFromHsv(h:Float, s:Float, v:Float) = Color.HSVToColor(floatArrayOf(h*360f, s, v))

    private fun mergeColors(color1: Int, color2: Int, mergeAmount: Float) = ColorUtils.blendARGB(color1, color2, mergeAmount)
}