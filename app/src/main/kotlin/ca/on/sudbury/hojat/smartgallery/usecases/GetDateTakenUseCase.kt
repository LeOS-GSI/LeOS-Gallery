package ca.on.sudbury.hojat.smartgallery.usecases

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import androidx.exifinterface.media.ExifInterface
import ca.on.sudbury.hojat.smartgallery.extensions.formatDate
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * You just give it an [ExifInterface] instance; it will either return the date
 * this picture/video was taken or just an empty [String].
 */
object GetDateTakenUseCase {
    @TargetApi(Build.VERSION_CODES.N)
    operator fun invoke(owner: Context?, exifInterface: ExifInterface): String {
        if (owner == null) return ""
        val dateTime = exifInterface.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
            ?: exifInterface.getAttribute(ExifInterface.TAG_DATETIME)
        return if (dateTime.isNullOrBlank()) {
            ""
        } else {
            val simpleDateFormat = SimpleDateFormat("yyyy:MM:dd kk:mm:ss", Locale.ENGLISH)
            simpleDateFormat.parse(dateTime)!!.time.formatDate(owner).trim()
        }
    }
}