package ca.on.sudbury.hojat.smartgallery.usecases

import ca.on.sudbury.hojat.smartgallery.helpers.NOMEDIA
import java.io.File

/**
 * returns true if this directory contains a ".nomedia" file inside it.
 */
object DoesContainNoMediaUseCase {
    operator fun invoke(directory: File): Boolean {
        return if (!directory.isDirectory) {
            // if this file isn't a directory, it means it doesn't contain ".nomedia"
            false
        } else {
            File(directory, NOMEDIA).exists()
        }
    }
    operator fun invoke(directory:String) = DoesContainNoMediaUseCase(File(directory))
}