package ca.on.sudbury.hojat.smartgallery.helpers

/**
 * The way that the number of each folder's media count is shown. It can be [on a separate line],
 * [in brackets], or none.
 */
sealed class FolderMediaCount {
    object SeparateLine : FolderMediaCount() {
        const val id = 1
    }

    object Brackets : FolderMediaCount() {
        const val id = 2
    }

    object None : FolderMediaCount() {
        const val id = 3
    }
}
