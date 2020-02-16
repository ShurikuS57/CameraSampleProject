package ru.taptm.cameralib

import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.view.ScaleGestureDetector
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.util.concurrent.Executor
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class CameraManager(private val context: Context, private val cameraView: CameraView) {
    private var mainExecutor: Executor = ContextCompat.getMainExecutor(context)
    private var camera: Camera? = null
    private var preview: Preview? = null
    private var lensFacing: Int = CameraSelector.LENS_FACING_FRONT
    private var imageCapture: ImageCapture? = null

    fun onRestart() {
        cameraView.post {
            bindCameraUseCases()
        }
    }

    private fun bindCameraUseCases() {
        val metrics = DisplayMetrics().also { cameraView.display.getRealMetrics(it) }
        val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
        val rotation = cameraView.display.rotation

        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            preview = Preview.Builder()
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(rotation)
                .build()
            preview?.setSurfaceProvider(cameraView.previewSurfaceProvider)

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .setTargetAspectRatio(screenAspectRatio)
                .setTargetRotation(rotation)
                .build()
            cameraProvider.unbindAll()
            try {
                camera = cameraProvider.bindToLifecycle(
                    context as LifecycleOwner, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, mainExecutor)
    }

    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    fun onZoom(detector: ScaleGestureDetector) {
        val currentZoomRatio: Float = camera?.cameraInfo?.zoomState?.value?.zoomRatio ?: 0F
        val delta = detector.scaleFactor
        val zoomValue = currentZoomRatio * delta
        camera?.cameraControl?.setZoomRatio(zoomValue)
    }

    fun switchCamera() {
        lensFacing = if (CameraSelector.LENS_FACING_FRONT == lensFacing) {
            CameraSelector.LENS_FACING_BACK
        } else {
            CameraSelector.LENS_FACING_FRONT
        }
        bindCameraUseCases()
    }

    fun takeAndSaveImageTo(file: File, callback: ImageCapture.OnImageSavedCallback) {
        val fileOption = ImageCapture.OutputFileOptions.Builder(file)
            .build()
        imageCapture?.takePicture(fileOption, mainExecutor, callback)
    }

    companion object {
        private const val TAG = "CameraManager"
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }
}