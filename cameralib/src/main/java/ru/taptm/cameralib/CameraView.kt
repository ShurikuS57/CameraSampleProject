package ru.taptm.cameralib

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.camera.core.ImageCapture
import androidx.camera.view.PreviewView
import java.io.File
import kotlin.math.abs

typealias OnSwipeCallback = (Boolean) -> Unit

class CameraView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : PreviewView(context, attrs, defStyleAttr) {

    private val previewView = PreviewView(context)
    private val cameraDelegate = CameraManager(context, this)
    private var swipeCallback: OnSwipeCallback? = null

    private val scaleGestureDetector =
        ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                cameraDelegate.onZoom(detector)
                return true
            }
        })

    private val gestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                cameraDelegate.switchCamera()
                return true
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent?,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                val deltaX = (e1?.x ?: 0f) - (e2?.x ?: 0f)
                val deltaXAbs = abs(deltaX)
                if (deltaXAbs in 300.0..1000.0) {
                    if (deltaX > 0) {
                        // "Swipe to left"
                        swipeCallback?.invoke(false)
                    } else {
                        //"Swipe to right"
                        swipeCallback?.invoke(true)
                    }
                }
                return true
            }
        })

    init {
        addView(previewView)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setUpGestureDetectors()
    }

    private fun setUpGestureDetectors() {
        setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            gestureDetector.onTouchEvent(event)
            return@setOnTouchListener true
        }
    }

    fun setOnSwipeCallback(newSwipeCallback: OnSwipeCallback) {
        swipeCallback = newSwipeCallback
    }

    fun takeAndSaveImageTo(file: File, callback: ImageCapture.OnImageSavedCallback) {
        cameraDelegate.takeAndSaveImageTo(file, callback)
    }

    fun restart() {
        cameraDelegate.onRestart()
    }
}