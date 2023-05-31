package ca.on.sudbury.hojat.smartgallery.usecases

import androidx.exifinterface.media.ExifInterface

/**
 * You just give it an [ExifInterface] instance; it will either return the
 * camera's manufacturer and model; or just an empty [String].
 */
object GetCameraModelUseCase {
    operator fun invoke(exifInterface: ExifInterface): String {

        val cameraManufacturer = exifInterface.getAttribute(ExifInterface.TAG_MAKE)
        return if (cameraManufacturer.isNullOrBlank()) {
            ""
        } else {
            val cameraModel = exifInterface.getAttribute(ExifInterface.TAG_MODEL)
            return "$cameraManufacturer $cameraModel".trim()
        }
    }
}