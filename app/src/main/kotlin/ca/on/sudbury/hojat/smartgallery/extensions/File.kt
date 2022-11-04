package ca.on.sudbury.hojat.smartgallery.extensions

import android.content.Context
import ca.on.sudbury.hojat.smartgallery.helpers.NOMEDIA
import ca.on.sudbury.hojat.smartgallery.models.FileDirItem
import java.io.File
import java.util.HashMap

fun File.toFileDirItem(context: Context) = FileDirItem(
    absolutePath,
    name,
    context.getIsPathDirectory(absolutePath),
    0,
    length(),
    lastModified()
)

/**
 * returns true if this directory contains a ".nomedia" file.
 */
fun File.containsNoMedia(): Boolean {
    return if (!isDirectory) {
        // if this file isn't a directory, it means it doesn't contain ".nomedia"
        false
    } else {
        File(this, NOMEDIA).exists()
    }
}

fun File.doesThisOrParentHaveNoMedia(
    folderNoMediaStatuses: HashMap<String, Boolean>,
    callback: ((path: String, hasNoMedia: Boolean) -> Unit)?
): Boolean {
    var curFile = this
    while (true) {
        val noMediaPath = "${curFile.absolutePath}/$NOMEDIA"
        val hasNoMedia = if (folderNoMediaStatuses.keys.contains(noMediaPath)) {
            folderNoMediaStatuses[noMediaPath]!!
        } else {
            val contains = curFile.containsNoMedia()
            callback?.invoke(curFile.absolutePath, contains)
            contains
        }

        if (hasNoMedia) {
            return true
        }

        curFile = curFile.parentFile ?: break
        if (curFile.absolutePath == "/") {
            break
        }
    }
    return false
}
