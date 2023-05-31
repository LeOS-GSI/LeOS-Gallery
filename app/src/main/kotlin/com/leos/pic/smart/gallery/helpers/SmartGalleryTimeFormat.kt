package ca.on.sudbury.hojat.smartgallery.helpers

/**
 * 2 types of time format that we show in this app. 12-hour and 24-hour
 * time formats.
 */
sealed class SmartGalleryTimeFormat {
    /**
     * 12-hour time format.
     */
    object HalfDay : SmartGalleryTimeFormat() {
        const val format = "hh:mm a"
    }

    /**
     * 24-hour time format.
     */
    object FullDay : SmartGalleryTimeFormat() {
        const val format = "HH:mm"
    }
}
