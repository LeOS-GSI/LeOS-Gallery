package ca.on.sudbury.hojat.smartgallery.helpers

/**
 * All the different types of date formats that we support in this app.
 */
sealed class SmartGalleryDateFormat {
    object One : SmartGalleryDateFormat() {
        const val format = "dd.MM.yyyy"
    }

    object Two : SmartGalleryDateFormat() {
        const val format = "dd/MM/yyyy"
    }

    object Three : SmartGalleryDateFormat() {
        const val format = "MM/dd/yyyy"
    }

    object Four : SmartGalleryDateFormat() {
        const val format = "yyyy-MM-dd"
    }

    object Five : SmartGalleryDateFormat() {
        const val format = "d MMMM yyyy"
    }

    object Six : SmartGalleryDateFormat() {
        const val format = "MMMM d yyyy"
    }

    object Seven : SmartGalleryDateFormat() {
        const val format = "MM-dd-yyyy"
    }

    object Eight : SmartGalleryDateFormat() {
        const val format = "dd-MM-yyyy"
    }
}
