package ca.on.sudbury.hojat.smartgallery.extensions

import android.graphics.Color
import android.media.ExifInterface
import ca.on.sudbury.hojat.smartgallery.helpers.DARK_GREY
import ca.on.sudbury.hojat.smartgallery.helpers.SORT_DESCENDING
import java.util.Locale
import kotlin.math.roundToInt

fun Int.adjustAlpha(factor: Float): Int {
    val alpha = (Color.alpha(this) * factor).roundToInt()
    val red = Color.red(this)
    val green = Color.green(this)
    val blue = Color.blue(this)
    return Color.argb(alpha, red, green, blue)
}

fun Int.getContrastColor(): Int {
    val y = (299 * Color.red(this) + 587 * Color.green(this) + 114 * Color.blue(this)) / 1000
    return if (y >= 149 && this != Color.BLACK) DARK_GREY else Color.WHITE
}

fun Int.getFormattedDuration(forceShowHours: Boolean = false): String {
    val sb = StringBuilder(8)
    val hours = this / 3600
    val minutes = this % 3600 / 60
    val seconds = this % 60

    if (this >= 3600) {
        sb.append(String.format(Locale.getDefault(), "%02d", hours)).append(":")
    } else if (forceShowHours) {
        sb.append("0:")
    }

    sb.append(String.format(Locale.getDefault(), "%02d", minutes))
    sb.append(":").append(String.format(Locale.getDefault(), "%02d", seconds))
    return sb.toString()
}

fun Int.isSortingAscending() = this and SORT_DESCENDING == 0

fun Int.toHex() = String.format("#%06X", 0xFFFFFF and this).uppercase(Locale.getDefault())

fun Int.orientationFromDegrees() = when (this) {
    270 -> ExifInterface.ORIENTATION_ROTATE_270
    180 -> ExifInterface.ORIENTATION_ROTATE_180
    90 -> ExifInterface.ORIENTATION_ROTATE_90
    else -> ExifInterface.ORIENTATION_NORMAL
}.toString()

fun Int.ensureTwoDigits(): String {
    return if (toString().length == 1) {
        "0$this"
    } else {
        toString()
    }
}

