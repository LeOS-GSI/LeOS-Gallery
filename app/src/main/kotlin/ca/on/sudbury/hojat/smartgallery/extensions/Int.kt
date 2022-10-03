package ca.on.sudbury.hojat.smartgallery.extensions

import android.content.Context
import android.graphics.Color
import android.text.format.DateFormat
import android.text.format.DateUtils
import com.simplemobiletools.commons.extensions.baseConfig
import com.simplemobiletools.commons.extensions.isThisYear
import com.simplemobiletools.commons.helpers.DARK_GREY
import com.simplemobiletools.commons.helpers.SORT_DESCENDING
import java.text.DecimalFormat
import java.util.Calendar
import java.util.Locale

fun Int.adjustAlpha(factor: Float): Int {
    val alpha = Math.round(Color.alpha(this) * factor)
    val red = Color.red(this)
    val green = Color.green(this)
    val blue = Color.blue(this)
    return Color.argb(alpha, red, green, blue)
}

fun Int.formatDate(
    context: Context,
    dateFormat: String? = null,
    timeFormat: String? = null
): String {
    val useDateFormat = dateFormat ?: context.baseConfig.dateFormat
    val useTimeFormat = timeFormat ?: context.getTimeFormat()
    val cal = Calendar.getInstance(Locale.ENGLISH)
    cal.timeInMillis = this * 1000L
    return DateFormat.format("$useDateFormat, $useTimeFormat", cal).toString()
}

// if the given date is today, we show only the time. Else we show the date and optionally the time too
fun Int.formatDateOrTime(
    context: Context,
    hideTimeAtOtherDays: Boolean,
    showYearEvenIfCurrent: Boolean
): String {
    val cal = Calendar.getInstance(Locale.ENGLISH)
    cal.timeInMillis = this * 1000L

    return if (DateUtils.isToday(this * 1000L)) {
        DateFormat.format(context.getTimeFormat(), cal).toString()
    } else {
        var format = context.baseConfig.dateFormat
        if (!showYearEvenIfCurrent && isThisYear()) {
            format = format.replace("y", "").trim().trim('-').trim('.').trim('/')
        }

        if (!hideTimeAtOtherDays) {
            format += ", ${context.getTimeFormat()}"
        }

        DateFormat.format(format, cal).toString()
    }
}

fun Int.formatSize(): String {
    if (this <= 0) {
        return "0 B"
    }

    val units = arrayOf("B", "kB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(toDouble()) / Math.log10(1024.0)).toInt()
    return "${
        DecimalFormat("#,##0.#").format(
            this / Math.pow(
                1024.0,
                digitGroups.toDouble()
            )
        )
    } ${units[digitGroups]}"
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

fun Int.toHex() = String.format("#%06X", 0xFFFFFF and this).toUpperCase()
