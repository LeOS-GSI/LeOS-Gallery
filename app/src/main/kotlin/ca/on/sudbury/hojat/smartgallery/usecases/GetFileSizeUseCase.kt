package ca.on.sudbury.hojat.smartgallery.usecases

import androidx.documentfile.provider.DocumentFile


/**
 * You give it a [DocumentFile] and a boolean to determine whether you wanna consider hidden items as well, or not. And it returns the size in form of a [Long] number.
 */
object GetFileSizeUseCase {
    operator fun invoke(directory: DocumentFile?, countHiddenItems: Boolean): Long {
        if (directory == null) return 0
        return if (directory.isDirectory) {
            getDirectorySize(directory, countHiddenItems)
        } else {
            directory.length()
        }
    }

    private fun getDirectorySize(dir: DocumentFile, countHiddenItems: Boolean): Long {
        var size = 0L
        if (dir.exists()) {
            val files = dir.listFiles()
            for (i in files.indices) {
                val file = files[i]
                if (file.isDirectory) {
                    size += getDirectorySize(file, countHiddenItems)
                } else if (!file.name!!.startsWith(".") || countHiddenItems) {
                    size += file.length()
                }
            }
        }
        return size
    }
}