package ca.on.sudbury.hojat.smartgallery.database

import ca.on.sudbury.hojat.smartgallery.models.Directory
import java.io.File

interface DirectoryOperationsListener {
    fun refreshItems()

    fun deleteFolders(folders: ArrayList<File>)

    fun recheckPinnedFolders()

    fun updateDirectories(directories: ArrayList<Directory>)
}
