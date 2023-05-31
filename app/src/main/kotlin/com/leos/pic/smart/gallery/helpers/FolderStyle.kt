package ca.on.sudbury.hojat.smartgallery.helpers

/**
 * The folders that are shown in the gallery can have different styles such as [Square] and [RoundedCorners].
 */
sealed class FolderStyle {
    object Square : FolderStyle() {
        const val id = 1
    }

    object RoundedCorners : FolderStyle() {
        const val id = 2
    }
}
