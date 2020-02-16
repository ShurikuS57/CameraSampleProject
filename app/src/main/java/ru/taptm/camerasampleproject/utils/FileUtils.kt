package ru.taptm.camerasampleproject.utils

import android.content.Context
import android.os.Environment
import ru.taptm.camerasampleproject.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class FileUtils {
    companion object {

        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PHOTO_EXTENSION = ".jpg"

        fun createImageFile(context: Context) =
            File(getOutputDirectory(context), SimpleDateFormat(FILENAME, Locale.US)
                    .format(System.currentTimeMillis()) + PHOTO_EXTENSION
            )

        private fun getOutputDirectory(context: Context): File {
            val appContext = context.applicationContext
            val mediaDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)?.let {
                File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() }
            }
            return if (mediaDir != null && mediaDir.exists())
                mediaDir else appContext.filesDir
        }
    }
}