package ca.on.sudbury.hojat.smartgallery.usecases

import java.io.File

/**
 * You give it a directory as a [File] and a boolean flag. It will return the number of files in that directory as an [Int].
 */
object CalculateDirectoryFileCountUseCase {
    operator fun invoke(directory: File, countHiddenItems: Boolean) = if (directory.isDirectory) {
        getDirectoryFileCount(directory, countHiddenItems)
    } else {
        1
    }

    private fun getDirectoryFileCount(dir: File, countHiddenItems: Boolean): Int {
        var count = -1
        if (dir.exists()) {
            val files = dir.listFiles()
            if (files != null) {
                count++
                for (i in files.indices) {
                    val file = files[i]
                    if (file.isDirectory) {
                        count++
                        count += getDirectoryFileCount(file, countHiddenItems)
                    } else if (!file.name.startsWith('.') || countHiddenItems) {
                        count++
                    }
                }
            }
        }
        return count
    }
}