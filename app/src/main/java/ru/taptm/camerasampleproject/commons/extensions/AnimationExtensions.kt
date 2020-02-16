package ru.taptm.camerasampleproject.commons.extensions

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.View

fun View.startFlashAnimation() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        this.postDelayed({
            this.foreground = ColorDrawable(Color.WHITE)
            this.postDelayed(
                { this.foreground = null }, 50L
            )
        }, 100L)
    }
}