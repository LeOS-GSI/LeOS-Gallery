package ca.on.sudbury.hojat.smartgallery.helpers

/**
 * All the different kinds of grouping that we might have in a folder.
 */
sealed class GroupBy {
    object None : GroupBy() {
        const val id = 1
    }

    object LastModifiedDaily : GroupBy() {
        const val id = 2
    }

    object DateTakenDaily : GroupBy() {
        const val id = 4
    }

    object FileType : GroupBy() {
        const val id = 8
    }

    object Extension : GroupBy() {
        const val id = 16
    }

    object Folder : GroupBy() {
        const val id = 32
    }

    object LastModifiedMonthly : GroupBy() {
        const val id = 64
    }

    object DateTakenMonthly : GroupBy() {
        const val id = 128
    }

    object Descending : GroupBy() {
        const val id = 1024
    }

    object ShowFileCount : GroupBy() {
        const val id = 2048
    }
}
