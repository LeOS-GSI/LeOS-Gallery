package ca.on.sudbury.hojat.smartgallery.helpers

/**
 * The location of the medium/file we're showing in this gallery
 * is one of these types:
 * 1- Internal
 * 2- SD-Card
 * 3- OTG
 */
sealed class FileLocation {
    object Internal : FileLocation() {
        const val id = 1
    }

    object SdCard : FileLocation() {
        const val id = 2
    }

    object Otg : FileLocation() {
        const val id = 3
    }
}
