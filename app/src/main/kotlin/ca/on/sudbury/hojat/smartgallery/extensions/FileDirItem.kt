package ca.on.sudbury.hojat.smartgallery.extensions

import android.content.Context
import ca.on.sudbury.hojat.smartgallery.models.FileDirItem

fun FileDirItem.isRecycleBinPath(context: Context): Boolean {
    return path.startsWith(context.recycleBinPath)
}
