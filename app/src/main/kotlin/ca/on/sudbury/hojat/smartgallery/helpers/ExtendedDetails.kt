package ca.on.sudbury.hojat.smartgallery.helpers

/**
 * In "settings" page, in the "Extended details" section, you can choose from all the details
 * that could be shown when picture is in fullscreen mode.
 */
sealed class ExtendedDetails {
    object Name : ExtendedDetails() {
        const val id = 1
    }

    object Path : ExtendedDetails() {
        const val id = 2
    }

    object Size : ExtendedDetails() {
        const val id = 4
    }

    object Resolution : ExtendedDetails() {
        const val id = 8
    }

    object LastModified : ExtendedDetails() {
        const val id = 16
    }

    object DateTaken : ExtendedDetails() {
        const val id = 32
    }

    object CameraModel : ExtendedDetails() {
        const val id = 64
    }

    object ExifProperties : ExtendedDetails() {
        const val id = 128
    }

    object Gps : ExtendedDetails() {
        const val id = 2048
    }
}
