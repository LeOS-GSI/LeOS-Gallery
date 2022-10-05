package ca.on.sudbury.hojat.smartgallery.extensions

import android.content.Context
import com.simplemobiletools.commons.models.FileDirItem

fun FileDirItem.isDownloadsFolder() = path.isDownloadsFolder()

fun FileDirItem.isRecycleBinPath(context: Context): Boolean {
    return path.startsWith(context.recycleBinPath)
}
