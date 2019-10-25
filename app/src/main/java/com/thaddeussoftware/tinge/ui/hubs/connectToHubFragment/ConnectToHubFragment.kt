package com.thaddeussoftware.tinge.ui.hubs.connectToHubFragment

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.thaddeussoftware.tinge.BR
import com.thaddeussoftware.tinge.R
import com.thaddeussoftware.tinge.databinding.FragmentConnectToHubBinding
import com.thaddeussoftware.tinge.deviceControlLibrary.generic.finder.HubSearchFoundResult
import com.thaddeussoftware.tinge.ui.hubs.hubView.HubViewModel
import me.tatarka.bindingcollectionadapter2.ItemBinding

/**
 * Created by thaddeusreason on 23/02/2018.
 */
class ConnectToHubFragment: Fragment() {

    private var binding: FragmentConnectToHubBinding? = null

    private var listener: ConnectToHubFragmentListener? = null

    private var viewModel: ConnectToHubFragmentViewModel? = null

    /**
     * Required to auto bind the hub list RecyclerView to the viewModel
     * */
    val hubListRecyclerViewItemBinding = ItemBinding.of<HubViewModel>(BR.viewModel, R.layout.view_hub)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(activity!!).get(ConnectToHubFragmentViewModel::class.java)

        viewModel?.deviceAddedLiveEvent?.observe(this, Observer { eventData ->
            if (eventData == null) return@Observer
            listener?.connectToHubFragmentDeviceAdded(eventData.hubSearchFoundResult)
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = FragmentConnectToHubBinding.inflate(inflater, container, false)
        binding?.viewModel = viewModel
        binding?.view = this

        viewModel?.startSearchingForHubs()

        return binding?.root
    }

    override fun onResume() {
        super.onResume()
        viewModel?.resumeViewModel()
    }

    override fun onPause() {
        super.onPause()
        viewModel?.pauseViewModel()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is ConnectToHubFragmentListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement ConnectToHubFragmentListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface ConnectToHubFragmentListener {

        /**
         * Called when a device has been successfully added to the app.
         * */
        fun connectToHubFragmentDeviceAdded(hubSearchFoundResult: HubSearchFoundResult)
    }

    companion object {

        fun newInstance(): ConnectToHubFragment {
            val fragment = ConnectToHubFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}