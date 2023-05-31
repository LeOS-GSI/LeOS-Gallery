package ca.on.sudbury.hojat.smartgallery.usecases

import androidx.documentfile.provider.DocumentFile

/**
 * You give it a [DocumentFile] and a boolean to determine whether you wanna consider hidden items as well, or not. And it returns total number of files in form of an [Int].
 */
object GetFileCountUseCase {
    operator fun invoke(directory: DocumentFile?, countHiddenItems: Boolean): Int {
        if (directory == null) return 0
        return if (directory.isDirectory) {
            getDirectoryFileCount(directory, countHiddenItems)
        } else {
            1
        }
    }

    private fun getDirectoryFileCount(dir: DocumentFile, countHiddenItems: Boolean): Int {
        var count = 0
        if (dir.exists()) {
            val files = dir.listFiles()
            for (i in files.indices) {
                val file = files[i]
                if (file.isDirectory) {
                    count++
                    count += getDirectoryFileCount(file, countHiddenItems)
                } else if (!file.name!!.startsWith(".") || countHiddenItems) {
                    count++
                }
            }
        }
        return count
    }
}