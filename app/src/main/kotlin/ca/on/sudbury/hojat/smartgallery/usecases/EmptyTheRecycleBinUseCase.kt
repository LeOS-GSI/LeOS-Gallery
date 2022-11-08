package ca.on.sudbury.hojat.smartgallery.usecases

import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.databases.GalleryDatabase
import ca.on.sudbury.hojat.smartgallery.extensions.mediaDB
import timber.log.Timber

/**
 * You give it an owner of type [BaseSimpleActivity] and also a callback.
 * If it managed to empty the recycle bin successfully, the callback will be called.
 */
object EmptyTheRecycleBinUseCase {
    operator fun invoke(owner: BaseSimpleActivity, callback: (() -> Unit)? = null) {
        RunOnBackgroundThreadUseCase {
            try {
                owner.baseContext.filesDir.deleteRecursively()
                owner.mediaDB.clearRecycleBin()
                GalleryDatabase.getInstance(owner.applicationContext).DirectoryDao()
                    .deleteRecycleBin()
                Timber.d("Emptied the recycle bin") // TODO : to be replaced by Toast later on.
                callback?.invoke()
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }
}