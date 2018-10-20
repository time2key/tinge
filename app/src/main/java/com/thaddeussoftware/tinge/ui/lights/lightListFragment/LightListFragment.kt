package com.thaddeussoftware.tinge.ui.lights.lightListFragment

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.thaddeussoftware.tinge.BR
import com.thaddeussoftware.tinge.R

import com.thaddeussoftware.tinge.databinding.FragmentLightListBinding
import com.thaddeussoftware.tinge.ui.hubs.hubView.HubViewModel
import com.thaddeussoftware.tinge.ui.lights.lightView.LightViewModel
import me.tatarka.bindingcollectionadapter2.ItemBinding
import android.support.annotation.LayoutRes
import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView
import com.thaddeussoftware.tinge.ui.lights.groupView.GroupViewModel
import com.thaddeussoftware.tinge.ui.lights.lightView.LightView
import me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter



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

        viewModel.refreshListOfHubsAndLights()
        //binding?.lightListLinearLayout?.addView(lightView)

        return binding?.root
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
