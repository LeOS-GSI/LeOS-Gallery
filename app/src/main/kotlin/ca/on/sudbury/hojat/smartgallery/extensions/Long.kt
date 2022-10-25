package ca.on.sudbury.hojat.smartgallery.extensions

import android.content.Context
import android.text.format.DateFormat
import ca.on.sudbury.hojat.smartgallery.helpers.TIME_FORMAT_12
import ca.on.sudbury.hojat.smartgallery.helpers.TIME_FORMAT_24
import java.util.Calendar
import java.util.Locale

fun Long.formatDate(
    context: Context,
    dateFormat: String? = null,
    timeFormat: String? = null
): String {
    val useDateFormat = dateFormat ?: context.baseConfig.dateFormat
    val useTimeFormat = timeFormat
        ?: with(context) { if (baseConfig.use24HourFormat) TIME_FORMAT_24 else TIME_FORMAT_12 }
    val cal = Calendar.getInstance(Locale.ENGLISH)
    cal.timeInMillis = this
    return DateFormat.format("$useDateFormat, $useTimeFormat", cal).toString()
}

