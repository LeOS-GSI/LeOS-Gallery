package ca.on.sudbury.hojat.smartgallery.extensions

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import androidx.exifinterface.media.ExifInterface
import com.simplemobiletools.commons.extensions.formatDate
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.text.SimpleDateFormat
import java.util.*

fun ExifInterface.copyNonDimensionAttributesTo(destination: ExifInterface) {
    val attributes = ExifInterfaceAttributes.AllNonDimensionAttributes

    attributes.forEach {
        val value = getAttribute(it)
        if (value != null) {
            destination.setAttribute(it, value)
        }
    }

    try {
        destination.saveAttributes()
    } catch (ignored: Exception) {
    }
}

fun ExifInterface.getExifCameraModel(): String {
    getAttribute(ExifInterface.TAG_MAKE).let {
        if (it?.isNotEmpty() == true) {
            val model = getAttribute(ExifInterface.TAG_MODEL)
            return "$it $model".trim()
        }
    }
    return ""
}

@TargetApi(Build.VERSION_CODES.N)
fun ExifInterface.getExifDateTaken(context: Context): String {
    val dateTime = getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL) ?: getAttribute(ExifInterface.TAG_DATETIME)
    dateTime.let {
        if (it?.isNotEmpty() == true) {
            try {
                val simpleDateFormat = SimpleDateFormat("yyyy:MM:dd kk:mm:ss", Locale.ENGLISH)
                return simpleDateFormat.parse(it).time.formatDate(context).trim()
            } catch (ignored: Exception) {
            }
        }
    }
    return ""
}

fun ExifInterface.getExifProperties(): String {
    var exifString = ""
    getAttribute(ExifInterface.TAG_F_NUMBER).let {
        if (it?.isNotEmpty() == true) {
            val number = it.trimEnd('0').trimEnd('.')
            exifString += "F/$number  "
        }
    }

    getAttribute(ExifInterface.TAG_FOCAL_LENGTH).let {
        if (it?.isNotEmpty() == true) {
            val values = it.split('/')
            val focalLength = "${values[0].toDouble() / values[1].toDouble()}mm"
            exifString += "$focalLength  "
        }
    }

    getAttribute(ExifInterface.TAG_EXPOSURE_TIME).let {
        if (it?.isNotEmpty() == true) {
            val exposureValue = it.toFloat()
            exifString += if (exposureValue > 1f) {
                "${exposureValue}s  "
            } else {
                "1/${Math.round(1 / exposureValue)}s  "
            }
        }
    }

    getAttribute(ExifInterface.TAG_ISO_SPEED_RATINGS).let {
        if (it?.isNotEmpty() == true) {
            exifString += "ISO-$it"
        }
    }

    return exifString.trim()
}

private class ExifInterfaceAttributes {
    companion object {
        val AllNonDimensionAttributes = getAllNonDimensionExifAttributes()

        private fun getAllNonDimensionExifAttributes(): List<String> {
            val tagFields = ExifInterface::class.java.fields.filter { field -> isExif(field) }

            val excludeAttributes = arrayListOf(
                ExifInterface.TAG_IMAGE_LENGTH,
                ExifInterface.TAG_IMAGE_WIDTH,
                ExifInterface.TAG_PIXEL_X_DIMENSION,
                ExifInterface.TAG_PIXEL_Y_DIMENSION,
                ExifInterface.TAG_THUMBNAIL_IMAGE_LENGTH,
                ExifInterface.TAG_THUMBNAIL_IMAGE_WIDTH,
                ExifInterface.TAG_ORIENTATION)

            return tagFields
                .map { tagField -> tagField.get(null) as String }
                .filter { x -> !excludeAttributes.contains(x) }
                .distinct()
        }

        private fun isExif(field: Field): Boolean {
            return field.type == String::class.java &&
                    isPublicStaticFinal(field.modifiers) &&
                    field.name.startsWith("TAG_")
        }

        private const val publicStaticFinal = Modifier.PUBLIC or Modifier.STATIC or Modifier.FINAL

        private fun isPublicStaticFinal(modifiers: Int): Boolean {
            return modifiers and publicStaticFinal > 0
        }
    }
}
