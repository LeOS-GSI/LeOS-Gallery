package ca.on.sudbury.hojat.smartgallery.usecases

import java.io.File

/**
 * Whenever you want to calculate the overall size of files in a specific
 * directory, you give that directory (as a [File]) and a boolean flag
 * to this UseCase and receive the directory's size as a [Long].
 */
object CalculateDirectorySizeUseCase {
    operator fun invoke(directory: File, countHiddenItems: Boolean): Long {
        return if (directory.isDirectory) {
            getDirectorySize(directory, countHiddenItems)
        } else {
            directory.length()
        }
    }

    private fun getDirectorySize(dir: File, countHiddenItems: Boolean): Long {
        var size = 0L
        if (dir.exists()) {
            val files = dir.listFiles()
            if (files != null) {
                for (i in files.indices) {
                    if (files[i].isDirectory) {
                        size += getDirectorySize(files[i], countHiddenItems)
                    } else if (!files[i].name.startsWith('.') && !dir.name.startsWith('.') || countHiddenItems) {
                        size += files[i].length()
                    }
                }
            }
        }
        return size
    }
}