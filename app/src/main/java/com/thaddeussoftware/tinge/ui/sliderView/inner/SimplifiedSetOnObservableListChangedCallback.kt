package com.thaddeussoftware.tinge.ui.sliderView.inner

import androidx.databinding.ObservableList
import com.thaddeussoftware.tinge.helpers.CollectionComparisonHelper

/**
 * If you have an [ObservableList] which you want to observe changes on, but you do not care about
 * the order of the items in the list, then you can use this callback in place of
 * [ObservableList.OnListChangedCallback].
 *
 * This then provides you with just one callback method: [listModified]
 *
 * Which will be called whenever a change happens to the [ObservableList].
 * */
abstract class SimplifiedSetOnObservableListChangedCallback<T>: ObservableList.OnListChangedCallback<ObservableList<T>>() {

    val backingArrayList = ArrayList<T>()


    override fun onChanged(sender: ObservableList<T>?) {
        if (sender != null) refreshAgainstObservableList(sender)
    }

    override fun onItemRangeRemoved(sender: ObservableList<T>?, positionStart: Int, itemCount: Int) {
        if (sender != null) refreshAgainstObservableList(sender)
    }

    override fun onItemRangeMoved(sender: ObservableList<T>?, fromPosition: Int, toPosition: Int, itemCount: Int) {
        if (sender != null) refreshAgainstObservableList(sender)
    }

    override fun onItemRangeInserted(sender: ObservableList<T>?, positionStart: Int, itemCount: Int) {
        if (sender != null) refreshAgainstObservableList(sender)
    }

    override fun onItemRangeChanged(sender: ObservableList<T>?, positionStart: Int, itemCount: Int) {
        if (sender != null) refreshAgainstObservableList(sender)
    }


    abstract fun listModified(itemsAdded: Collection<T>, itemsRemoved: Collection<T>)


    private fun refreshAgainstObservableList(sender: ObservableList<T>) {
        val itemsAdded = ArrayList<T>()
        val itemsRemoved = ArrayList<T>()

        CollectionComparisonHelper.compareCollectionsAndIdentifyMissingElements(
                sender,
                backingArrayList,
                { item1, item2 ->
                    item1 == item2
                },
                {
                    backingArrayList.add(it)
                    itemsAdded.add(it)
                },
                {
                    backingArrayList.remove(it)
                    itemsAdded.remove(it)
                }
        )

        listModified(itemsAdded, itemsRemoved)
    }

}