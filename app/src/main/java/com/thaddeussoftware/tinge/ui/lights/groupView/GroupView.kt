package com.thaddeussoftware.tinge.ui.lights.groupView

import android.content.Context
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

    var viewModel: GroupViewModel? = viewModel
        set(value) {
            field = value
            removeAllViews()
            binding = ViewGroupBinding.inflate(LayoutInflater.from(context), this, true)
            binding.view = this
            binding.viewModel = viewModel
            binding.lightListLinearLayout.invalidate()
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
}