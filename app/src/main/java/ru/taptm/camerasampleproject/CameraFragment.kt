package ru.taptm.camerasampleproject

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_camera.*
import rebus.permissionutils.FullCallback
import rebus.permissionutils.PermissionEnum
import rebus.permissionutils.PermissionManager
import rebus.permissionutils.PermissionUtils
import ru.taptm.camerabutton.ColorChangeEvent
import ru.taptm.camerasampleproject.commons.extensions.startFlashAnimation
import ru.taptm.camerasampleproject.commons.extensions.toast
import ru.taptm.camerasampleproject.utils.FileUtils
import java.util.*
import java.util.concurrent.Executor

class CameraFragment : Fragment() {
    private lateinit var mainExecutor: Executor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainExecutor = ContextCompat.getMainExecutor(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_camera, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view_camera.post {
            updateCameraUi()
        }
        view_camera.setOnSwipeCallback {
            button_camera_capture.changeColorAnimation(if (it) ColorChangeEvent.NEXT else ColorChangeEvent.BACK)
        }
        checkPermissions()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        checkPermissions()
        updateCameraUi()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionManager.handleResult(this, requestCode, permissions, grantResults)
    }

    private fun checkPermissions() {
        PermissionManager.Builder()
            .permissions(arrayListOf(PermissionEnum.CAMERA, PermissionEnum.WRITE_EXTERNAL_STORAGE))
            .callback(object : FullCallback {
                override fun result(
                    permissionsGranted: ArrayList<PermissionEnum>?,
                    permissionsDenied: ArrayList<PermissionEnum>?,
                    permissionsDeniedForever: ArrayList<PermissionEnum>?,
                    permissionsAsked: ArrayList<PermissionEnum>?
                ) {
                    if (permissionsDenied?.size != 0 || permissionsDeniedForever?.size != 0) {
                        showPermissionDialog()
                        return
                    }
                    view_camera.restart()
                }

            })
            .ask(this)
    }

    private fun updateCameraUi() {
        button_camera_capture.setOnTapCallback {
            if (PermissionUtils.isGranted(
                    requireContext(),
                    PermissionEnum.CAMERA,
                    PermissionEnum.WRITE_EXTERNAL_STORAGE
                )
            ) {
                takeAndSaveImage()
                container_camera.startFlashAnimation()
            } else {
                checkPermissions()
            }
        }
    }

    private fun takeAndSaveImage() {
        view_camera.takeAndSaveImageTo(
            FileUtils.createImageFile(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    requireContext().toast("Image saved")
                }

                override fun onError(exception: ImageCaptureException) {
                    requireContext().toast("Fail capture: ${exception.message}")
                }

            })
    }

    private fun showPermissionDialog() {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setMessage(getString(R.string.dialog_permission_message))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.button_go_settings)) { dialog, _ ->
                dialog.cancel()
                PermissionUtils.openApplicationSettings(
                    requireContext(),
                    MainActivity::class.java.`package`?.name
                )
            }
            .setNegativeButton(getString(R.string.button_finish)) { dialog, _ ->
                dialog.cancel()
                requireActivity().finish()
            }
        val alert = dialogBuilder.create()
        alert.setTitle(getString(R.string.dialog_permission_title))
        alert.show()
    }

    companion object {
        fun newInstance(): CameraFragment {
            return CameraFragment()
        }
    }
}