package ca.on.sudbury.hojat.smartgallery.usecases

import androidx.exifinterface.media.ExifInterface
import kotlin.math.roundToInt

/**
 * You just give it an [ExifInterface] instance; it will return normal EXIF information of that
 * picture (in form of [String]) such as F number, focal length of the lens in mm, exposure time
 * in seconds, and sensitivity of the camera.
 */
object GetGeneralPropertiesUseCase {
    operator fun invoke(exifInterface: ExifInterface): String {
        var exifString = ""
        exifInterface.getAttribute(ExifInterface.TAG_F_NUMBER).let {
            if (it?.isNotEmpty() == true) {
                val number = it.trimEnd('0').trimEnd('.')
                exifString += "F/$number  "
            }
        }

        exifInterface.getAttribute(ExifInterface.TAG_FOCAL_LENGTH).let {
            if (it?.isNotEmpty() == true) {
                val values = it.split('/')
                val focalLength = "${values[0].toDouble() / values[1].toDouble()}mm"
                exifString += "$focalLength  "
            }
        }

        exifInterface.getAttribute(ExifInterface.TAG_EXPOSURE_TIME).let {
            if (it?.isNotEmpty() == true) {
                val exposureValue = it.toFloat()
                exifString += if (exposureValue > 1f) {
                    "${exposureValue}s  "
                } else {
                    "1/${(1 / exposureValue).roundToInt()}s  "
                }
            }
        }

        exifInterface.getAttribute(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY).let {
            if (it?.isNotEmpty() == true) {
                exifString += "ISO-$it"
            }
        }

        return exifString.trim()
    }
}