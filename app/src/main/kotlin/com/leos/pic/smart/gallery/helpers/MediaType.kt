package ca.on.sudbury.hojat.smartgallery.helpers

/**
 * General types of media that are supported by the
 * app.
 */
sealed class MediaType {
    object Image : MediaType() {
        const val id = 1
    }

    object Video : MediaType() {
        const val id = 2
    }

    object Gif : MediaType() {
        const val id = 4
    }

    object Raw : MediaType() {
        const val id = 8
    }

    object Svg : MediaType() {
        const val id = 16
    }

    object Portrait : MediaType() {
        const val id = 32
    }
}
