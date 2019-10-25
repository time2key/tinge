package com.thaddeussoftware.tinge.ui.sliderView

import androidx.databinding.ObservableField

interface SliderViewHandle {
    /**
     * Display name given to this handle. This will not ordinarily be shown, but will be used to
     * differentiate two handles if
     * */
    val displayName: String

    /**
     * Amount this handle is slid:
     * If the slider is in the normal range, this value will be from 0 - 1
     * If [SliderView.supportsOffValue] is true, and this handle is in the 'Off' position,
     * this value will be -1
     * */
    val value: ObservableField<Float?>

    val color: ObservableField<Int>
}