package ru.taptm.camerabutton

import android.graphics.Color
import androidx.annotation.ColorInt

class ButtonColors {

    private val colors = listOf(Color.WHITE, Color.RED, Color.GREEN, Color.BLUE)
    private var currentColorIndex = 0

    @ColorInt
    fun getCurrentColor(): Int {
        return colors[currentColorIndex]
    }

    @ColorInt
    fun getColor(event: ColorChangeEvent): Int {
        if (event == ColorChangeEvent.BACK) {
            if (colors.size - 1 > currentColorIndex) {
                currentColorIndex++
            } else {
                currentColorIndex = 0
            }
        } else if (event == ColorChangeEvent.NEXT) {
            if (currentColorIndex == 0) {
                currentColorIndex = colors.size - 1
            } else {
                currentColorIndex--
            }
        }
        return colors[currentColorIndex]
    }
}