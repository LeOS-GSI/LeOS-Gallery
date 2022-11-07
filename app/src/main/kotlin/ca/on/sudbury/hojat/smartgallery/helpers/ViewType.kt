package ca.on.sudbury.hojat.smartgallery.helpers

/**
 * Folders and the mediums inside each folder can be viewed in 2 different ways: Grid or list.
 */
sealed class ViewType {
    object Grid : ViewType() {
        const val id = 1
    }

    object List : ViewType() {
        const val id = 2
    }
}
