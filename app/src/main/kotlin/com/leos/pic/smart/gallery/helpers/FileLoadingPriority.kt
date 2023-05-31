package ca.on.sudbury.hojat.smartgallery.helpers

/**
 * Different kinds of file loading priority. This feature is accessible only
 * on some specific APIs.
 */
sealed class FileLoadingPriority {
    object Speed : FileLoadingPriority() {
        const val id = 0
    }

    object Compromise : FileLoadingPriority() {
        const val id = 1
    }

    object Validity : FileLoadingPriority() {
        const val id = 2
    }
}