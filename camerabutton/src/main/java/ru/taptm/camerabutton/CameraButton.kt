package ru.taptm.camerabutton

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.vectordrawable.graphics.drawable.ArgbEvaluator

typealias OnTapCallback = () -> Unit

class CameraButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {

    private var tapCallback: OnTapCallback? = null
    private var buttonColors = ButtonColors()

    private val gestureTapDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                tapCallback?.invoke()
                return true
            }
        })

    init {
        background = ContextCompat.getDrawable(context, R.drawable.ic_camera_button)
        background.setTint(buttonColors.getCurrentColor())
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setUpGestureDetectors()
    }

    private fun setUpGestureDetectors() {
        setOnTouchListener { _, event ->
            gestureTapDetector.onTouchEvent(event)
            checkScaleEvents(event)
            return@setOnTouchListener true
        }
    }

    private fun checkScaleEvents(event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_DOWN) {
            startScaleAnimation(ANIMATION_SCALE_UP)
        } else if (event.action == MotionEvent.ACTION_UP) {
            startScaleAnimation(ANIMATION_ORIGIN_SCALE)
        }
    }

    fun changeColorAnimation(event: ColorChangeEvent) {
        val colorStar = solidColor
        val colorEnd = buttonColors.getColor(event)
        val colorAnimator = ValueAnimator.ofObject(ArgbEvaluator(), colorStar, colorEnd)
        colorAnimator.duration = CHANGE_COLOR_DURATION
        colorAnimator.addUpdateListener { animator ->
            background.setTint((animator.animatedValue as Int))
        }
        colorAnimator.start()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            startScaleAnimation(ANIMATION_SCALE_UP)
        } else if (event?.action == MotionEvent.ACTION_UP) {
            startScaleAnimation(ANIMATION_ORIGIN_SCALE)
        }
        return true
    }

    fun setOnTapCallback(newTapCallback: OnTapCallback) {
        tapCallback = newTapCallback
    }

    private fun startScaleAnimation(endScale: Float) {
        animate().scaleX(endScale).scaleY(endScale).setDuration(300L).start()
    }

    companion object {
        private const val ANIMATION_SCALE_UP = 1.5F
        private const val ANIMATION_ORIGIN_SCALE = 1F
        private const val CHANGE_COLOR_DURATION = 300L
    }

}