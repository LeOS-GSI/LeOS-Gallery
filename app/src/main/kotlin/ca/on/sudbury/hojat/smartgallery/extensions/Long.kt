package ca.on.sudbury.hojat.smartgallery.extensions

import android.content.Context
import android.text.format.DateFormat
import java.util.Calendar
import java.util.Locale

fun Long.formatDate(
    context: Context,
    dateFormat: String? = null,
    timeFormat: String? = null
): String {
    val useDateFormat = dateFormat ?: context.baseConfig.dateFormat
    val useTimeFormat = timeFormat ?: context.getTimeFormat()
    val cal = Calendar.getInstance(Locale.ENGLISH)
    cal.timeInMillis = this
    return DateFormat.format("$useDateFormat, $useTimeFormat", cal).toString()
}

