package com.thaddeussoftware.tinge.ui;

import androidx.databinding.BindingAdapter;
import android.view.View;
import android.view.ViewGroup;

public class BindingAdapters {

    @BindingAdapter("android:layout_width")
    public static void setLayoutWidth(View view, float width) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = (int) width;
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter("android:layout_height")
    public static void setLayoutHeight(View view, float height) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) height;
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter("android:layout_marginStart")
    public static void setLayoutMarginStartFloat(View view, float marginStart) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        layoutParams.setMarginStart((int) marginStart);
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter("android:layout_marginEnd")
    public static void setLayoutMarginEndFloat(View view, float marginEnd) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        layoutParams.setMarginEnd((int) marginEnd);
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter("android:layout_marginLeft")
    public static void setLayoutMarginLeftFloat(View view, float marginLeft) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        layoutParams.leftMargin = (int) marginLeft;
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter("android:layout_marginTop")
    public static void setLayoutMarginTopFloat(View view, float marginTop) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        layoutParams.topMargin = (int) marginTop;
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter("android:layout_marginRight")
    public static void setLayoutMarginRightFloat(View view, float marginRight) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        layoutParams.rightMargin = (int) marginRight;
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter("android:layout_marginBottom")
    public static void setLayoutMarginBottomFloat(View view, float marginBottom) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        layoutParams.bottomMargin = (int) marginBottom;
        view.setLayoutParams(layoutParams);
    }
}
