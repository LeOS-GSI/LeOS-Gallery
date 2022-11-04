package ca.on.sudbury.hojat.smartgallery.usecases

import android.content.Context
import ca.on.sudbury.hojat.smartgallery.extensions.getAndroidSAFDirectChildrenCount
import ca.on.sudbury.hojat.smartgallery.extensions.isRestrictedSAFOnlyRoot
import java.io.File

/**
 * You give it a directory and it returns the number of direct children that directory has.
 */
object CalculateDirectChildrenUseCase {

    operator fun invoke(directory: File, owner: Context, countHiddenItems: Boolean): Int {
        val fileCount = if (owner.isRestrictedSAFOnlyRoot(directory.path)) {
            owner.getAndroidSAFDirectChildrenCount(
                directory.path,
                countHiddenItems
            )
        } else {
            directory.listFiles()?.filter {
                if (countHiddenItems) {
                    true
                } else {
                    !it.name.startsWith('.')
                }
            }?.size ?: 0
        }

        return fileCount
    }
}