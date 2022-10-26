package ca.on.sudbury.hojat.smartgallery.extensions

import androidx.documentfile.provider.DocumentFile

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

fun DocumentFile.getFileCount(countHiddenItems: Boolean): Int {
    return if (isDirectory) {
        getDirectoryFileCount(this, countHiddenItems)
    } else {
        1
    }
}

