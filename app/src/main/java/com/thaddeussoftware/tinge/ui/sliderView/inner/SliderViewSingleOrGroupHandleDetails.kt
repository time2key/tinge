package com.thaddeussoftware.tinge.ui.sliderView.inner

import android.animation.Animator
import android.view.View
import android.view.ViewGroup

abstract class SliderViewSingleOrGroupHandleDetails {

    companion object {

        const val ANIMATION_DURATION_TOUCH_DOWN_MS = 100L
        const val ANIMATION_DURATION_TOUCH_UP_MS = 400L

        const val ANIMATION_DURATION_TOUCH_DOWN_SINGLE_CLICK_MS = 100L
        const val ANIMATION_DURATION_TOUCH_UP_SINGLE_CLICK_MS = 400L


        const val ANIMATION_DURATION_TO_HOVERING_OVER_STATE = 100L
        const val ANIMATION_ALPHA_HOVERING_OVER = 0.6f

        const val ANIMATION_DURATION_TO_HOVERED_OVER_STATE = 100L
        const val ANIMATION_ALPHA_HOVERED_OVER = 0.6f


        const val ANIMATION_DURATION_APPEAR = 500L
        const val ANIMATION_DURATION_LABEL_APPEAR = 100L
        const val ANIMATION_APPEAR_BIG_TO_SMALL_INITIAL_SCALE = 1.4f

        const val ANIMATION_DURATION_DISAPPEAR = 100L
    }



    abstract val handleView: View

    abstract val onMoveLabelTextView: View

    abstract val rootView: View


    abstract val scaleIfTouchHeldDown: Float

    abstract val scaleIfHoveredOverForMerge: Float

    abstract val scaleIfHoveringOverForMerge: Float


    abstract fun getCurrentHandleValue(): Float?

    abstract fun setCurrentHandleValue(value: Float)


    abstract fun updateHandleDrawableForCurrentColor()


    enum class AnimatableState {
        NORMAL,
        TOUCH_DOWN,
        HOVERED_OVER_FOR_MERGE,
        HOVERING_OVER_FOR_MERGE,

        HIDDEN,
        REMOVED
    }

    var currentViewStateAnimatedInto = AnimatableState.NORMAL

    /**
     * Animates this view to a state which indicates it is currently being held / touched, if
     * it is not currently in this state.
     * */
    fun animateToCurrentlyBeingHeldStateIfAppropriate() {
        if (currentViewStateAnimatedInto != AnimatableState.TOUCH_DOWN) {
            // Animate to touch down state:
            currentViewStateAnimatedInto = AnimatableState.TOUCH_DOWN

            handleView.clearAnimation()
            handleView
                    .animate()
                    .scaleX(scaleIfTouchHeldDown)
                    .scaleY(scaleIfTouchHeldDown)
                    .alpha(1f)
                    .setDuration(ANIMATION_DURATION_TOUCH_DOWN_MS)
                    .setListener(null)

            onMoveLabelTextView.clearAnimation()
            onMoveLabelTextView
                    .animate()
                    .alpha(1f)
                    .setDuration(ANIMATION_DURATION_TOUCH_DOWN_MS)
                    .setListener(null)
        }
    }

    /**
     * Aniamtes this view to the regular, non-held, state, if it is not currently in this state.
     * */
    fun animateToNormalStateIfAppropriate() {
        if (currentViewStateAnimatedInto != AnimatableState.NORMAL) {
            // Animate to touch up state:
            currentViewStateAnimatedInto = AnimatableState.NORMAL

            handleView
                    .animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(ANIMATION_DURATION_TOUCH_UP_MS)
                    .setStartDelay(0)
                    .setListener(null)

            onMoveLabelTextView
                    .animate()
                    .alpha(0f)
                    .setDuration(ANIMATION_DURATION_TOUCH_UP_MS)
                    .setStartDelay(0)
                    .setListener(null)
        }
    }

    /**
     * Animates this view to indicate that a click has happened on it - it will grow larger and
     * then auto return to the normal state.
     * */
    fun animateInThenOutForSingleClick(updateUiOfMaxAndMinLabelVisibilityOnCompleteLambda: () -> Unit) {
        val firstAnimationDoneStartSecondAnimationListener = object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {}
            override fun onAnimationCancel(p0: Animator?) {}
            override fun onAnimationStart(p0: Animator?) {}

            override fun onAnimationEnd(p0: Animator?) {

                handleView
                        .animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(ANIMATION_DURATION_TOUCH_UP_SINGLE_CLICK_MS)
                        .setStartDelay(0)
                        .setListener(null)

                onMoveLabelTextView
                        .animate()
                        .alpha(0f)
                        .setDuration(ANIMATION_DURATION_TOUCH_UP_SINGLE_CLICK_MS)
                        .setStartDelay(0)
                        .setListener(null)

                updateUiOfMaxAndMinLabelVisibilityOnCompleteLambda()

            }
        }

        handleView.clearAnimation()
        handleView
                .animate()
                .scaleX(scaleIfTouchHeldDown)
                .scaleY(scaleIfTouchHeldDown)
                .alpha(1f)
                .setDuration(ANIMATION_DURATION_TOUCH_DOWN_SINGLE_CLICK_MS)
                .setStartDelay(0)
                .setListener(firstAnimationDoneStartSecondAnimationListener)

        onMoveLabelTextView.clearAnimation()
        onMoveLabelTextView
                .animate()
                .alpha(1f)
                .setDuration(ANIMATION_DURATION_TOUCH_DOWN_SINGLE_CLICK_MS)
                .setStartDelay(0)
                .setListener(null)
    }

    /**
     * Animates this to a state which indicates that another handle is going to be merged
     * into this one.
     * */
    fun animateToHoveredOverForMergeState() {
        if (currentViewStateAnimatedInto != AnimatableState.HOVERED_OVER_FOR_MERGE) {
            // Animate to touch down state:
            currentViewStateAnimatedInto = AnimatableState.HOVERED_OVER_FOR_MERGE

            handleView.clearAnimation()
            handleView
                    .animate()
                    .scaleX(scaleIfHoveredOverForMerge)
                    .scaleY(scaleIfHoveredOverForMerge)
                    .alpha(ANIMATION_ALPHA_HOVERED_OVER)
                    .setDuration(ANIMATION_DURATION_TO_HOVERED_OVER_STATE)
                    .setStartDelay(0)
                    .setListener(null)

            onMoveLabelTextView
                    .animate()
                    .alpha(1f)
                    .setDuration(ANIMATION_DURATION_TO_HOVERED_OVER_STATE)
                    .setStartDelay(0)
                    .setListener(null)
        }
    }

    /**
     * Animates this to a state which indicates that this handle is going to be merged into
     * another one.
     * */
    fun animateToHoveringOverForMergeState() {
        if (currentViewStateAnimatedInto != AnimatableState.HOVERING_OVER_FOR_MERGE) {
            // Animate to touch down state:
            currentViewStateAnimatedInto = AnimatableState.HOVERING_OVER_FOR_MERGE

            handleView.clearAnimation()
            handleView
                    .animate()
                    .scaleX(scaleIfHoveringOverForMerge)
                    .scaleY(scaleIfHoveringOverForMerge)
                    .alpha(ANIMATION_ALPHA_HOVERING_OVER)
                    .setDuration(ANIMATION_DURATION_TO_HOVERING_OVER_STATE)
                    .setStartDelay(0)
                    .setListener(null)

            onMoveLabelTextView
                    .animate()
                    .alpha(0f)
                    .setDuration(ANIMATION_DURATION_TO_HOVERING_OVER_STATE)
                    .setStartDelay(0)
                    .setListener(null)
        }
    }

    fun animateOutThenRemoveFromLayout() {
        currentViewStateAnimatedInto = AnimatableState.REMOVED
        privateAnimateOutThenRunLambda { (rootView.parent as ViewGroup).removeView(rootView) }
    }

    fun animateOutThenBackIn() {
        currentViewStateAnimatedInto = AnimatableState.HIDDEN
        privateAnimateOutThenRunLambda {
            currentViewStateAnimatedInto = AnimatableState.NORMAL
            privateAnimateInFromBigToSmall()
        }
    }

    fun animateOutThenHide() {
        currentViewStateAnimatedInto = AnimatableState.HIDDEN
        privateAnimateOutThenRunLambda {
            handleView.scaleX = 1f
            handleView.scaleY = 1f
            handleView.alpha = 0f
        }
    }

    fun animateIn() {
        currentViewStateAnimatedInto = AnimatableState.NORMAL
        privateAnimateIn()
    }

    fun animateInAfterDelay() {
        currentViewStateAnimatedInto = AnimatableState.NORMAL
        privateAnimateInFromBigToSmall(ANIMATION_DURATION_DISAPPEAR)
    }


    private fun privateAnimateOutThenRunLambda(toRunAfterAnimationFinished: (() -> Unit)?) {
        val animationDoneRunLambda = object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {}
            override fun onAnimationCancel(p0: Animator?) {}
            override fun onAnimationStart(p0: Animator?) {}
            override fun onAnimationEnd(p0: Animator?) {
                toRunAfterAnimationFinished?.let {
                    it()
                }
            }
        }

        handleView.clearAnimation()
        handleView.animate()
                .scaleX(0f)
                .scaleY(0f)
                .alpha(handleView.alpha/2f)
                .setDuration(ANIMATION_DURATION_DISAPPEAR)
                .setStartDelay(0)
                .setListener(animationDoneRunLambda)

        onMoveLabelTextView.animate()
                .alpha(0f)
                .setDuration(ANIMATION_DURATION_DISAPPEAR)
                .setStartDelay(0)
                .setListener(null)
    }

    private fun privateAnimateIn(startDelay: Long = 0) {
        handleView.scaleX = 0f
        handleView.scaleY = 0f
        handleView.alpha = 0.5f

        handleView.clearAnimation()
        handleView.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(ANIMATION_DURATION_APPEAR)
                .setStartDelay(startDelay)
                .setListener(null)

        onMoveLabelTextView.animate()
                .alpha(0f)
                .setDuration(ANIMATION_DURATION_LABEL_APPEAR)
                .setStartDelay(startDelay)
                .setListener(null)
    }

    private fun privateAnimateInFromBigToSmall(startDelay: Long = 0) {
        handleView.scaleX = ANIMATION_APPEAR_BIG_TO_SMALL_INITIAL_SCALE
        handleView.scaleY = ANIMATION_APPEAR_BIG_TO_SMALL_INITIAL_SCALE
        handleView.alpha = 0f

        handleView.clearAnimation()
        handleView.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(ANIMATION_DURATION_APPEAR)
                .setStartDelay(startDelay)
                .setListener(null)

        onMoveLabelTextView.animate()
                .alpha(0f)
                .setDuration(ANIMATION_DURATION_LABEL_APPEAR)
                .setStartDelay(startDelay)
                .setListener(null)
    }
}