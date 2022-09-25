package ca.on.sudbury.hojat.smartgallery.database

import com.simplemobiletools.commons.models.FileDirItem
import ca.on.sudbury.hojat.smartgallery.models.ThumbnailItem

interface MediaOperationsListener {
    fun refreshItems()

    fun tryDeleteFiles(fileDirItems: ArrayList<FileDirItem>)

    fun selectedPaths(paths: ArrayList<String>)

    fun updateMediaGridDecoration(media: ArrayList<ThumbnailItem>)
}
