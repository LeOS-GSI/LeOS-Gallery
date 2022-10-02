package ca.on.sudbury.hojat.smartgallery.extensions

import android.content.Context
import android.text.format.DateFormat
import com.simplemobiletools.commons.extensions.baseConfig
import com.simplemobiletools.commons.helpers.SORT_DESCENDING
import java.text.DecimalFormat
import java.util.Calendar
import java.util.Locale

fun Int.formatDate(
    context: Context,
    dateFormat: String? = null,
    timeFormat: String? = null): String {
    val useDateFormat = dateFormat ?: context.baseConfig.dateFormat
    val useTimeFormat = timeFormat ?: context.getTimeFormat()
    val cal = Calendar.getInstance(Locale.ENGLISH)
    cal.timeInMillis = this * 1000L
    return DateFormat.format("$useDateFormat, $useTimeFormat", cal).toString()
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

fun Int.isSortingAscending() = this and SORT_DESCENDING == 0
