package ca.on.sudbury.hojat.smartgallery.usecases

import android.annotation.TargetApi
import android.app.Activity
import android.graphics.BitmapFactory
import android.os.Build
import androidx.exifinterface.media.ExifInterface
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.extensions.copyFile
import ca.on.sudbury.hojat.smartgallery.extensions.fileRotatedSuccessfully
import ca.on.sudbury.hojat.smartgallery.extensions.getFileInputStreamSync
import ca.on.sudbury.hojat.smartgallery.extensions.getFileOutputStream
import ca.on.sudbury.hojat.smartgallery.extensions.getFilenameFromPath
import ca.on.sudbury.hojat.smartgallery.extensions.recycleBinPath
import ca.on.sudbury.hojat.smartgallery.extensions.rescanPaths
import ca.on.sudbury.hojat.smartgallery.extensions.saveExifRotation
import ca.on.sudbury.hojat.smartgallery.extensions.saveFile
import ca.on.sudbury.hojat.smartgallery.extensions.saveImageRotation
import ca.on.sudbury.hojat.smartgallery.extensions.tryDeleteFileDirItem
import ca.on.sudbury.hojat.smartgallery.models.FileDirItem
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * You give it the owner [BaseSimpleActivity], picture's current path, new path you wanna save the picture in, and the required degrees of rotation. It saves the rotated picture in that path as a new picture.
 */
object SaveRotatedImageUseCase {
    operator fun invoke(
        owner: BaseSimpleActivity?,
        oldPath: String,
        newPath: String,
        degrees: Int,
        showToasts: Boolean,
        callback: () -> Unit
    ) {
        if (owner == null) return
        var newDegrees = degrees
        if (newDegrees < 0) {
            newDegrees += 360
        }

        if (oldPath == newPath && IsJpgUseCase(oldPath)) {
            if (tryRotateByExif(owner, oldPath, newDegrees, showToasts, callback)) {
                return
            }
        }

        val tmpPath = "${owner.recycleBinPath}/.tmp_${newPath.getFilenameFromPath()}"
        val tmpFileDirItem = FileDirItem(tmpPath, tmpPath.getFilenameFromPath())
        try {
            owner.getFileOutputStream(tmpFileDirItem) {
                if (it == null) {
                    if (showToasts) {
                        ShowSafeToastUseCase(owner, R.string.unknown_error_occurred)
                    }
                    return@getFileOutputStream
                }

                val oldLastModified = File(oldPath).lastModified()
                if (IsJpgUseCase(oldPath)) {
                    owner.copyFile(oldPath, tmpPath)
                    saveExifRotation(ExifInterface(tmpPath), newDegrees)
                } else {
                    val inputstream = owner.getFileInputStreamSync(oldPath)
                    val bitmap = BitmapFactory.decodeStream(inputstream)
                    saveFile(tmpPath, bitmap, it as FileOutputStream, newDegrees)
                }
                with(owner) {
                    copyFile(tmpPath, newPath)
                    applicationContext.rescanPaths(arrayListOf(newPath))
                    fileRotatedSuccessfully(newPath, oldLastModified)
                }


                it.flush()
                it.close()
                callback.invoke()
            }
        } catch (e: OutOfMemoryError) {
            if (showToasts) {
                ShowSafeToastUseCase(owner, R.string.out_of_memory_error)
            }
        } catch (e: Exception) {
            if (showToasts) {
                ShowSafeToastUseCase(owner, e.toString())
            }
        } finally {
            owner.tryDeleteFileDirItem(
                tmpFileDirItem,
                allowDeleteFolder = false,
                deleteFromDatabase = true
            )
        }

    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun tryRotateByExif(
        owner: Activity,
        path: String,
        degrees: Int,
        showToasts: Boolean,
        callback: () -> Unit
    ): Boolean {
        return try {
            val file = File(path)
            val oldLastModified = file.lastModified()
            if (owner.saveImageRotation(path, degrees)) {
                owner.fileRotatedSuccessfully(path, oldLastModified)
                callback.invoke()
                if (showToasts) {
                    ShowSafeToastUseCase(owner, R.string.file_saved)
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            // lets not show IOExceptions, rotating is saved just fine even with them
            if (showToasts && e !is IOException) {
                ShowSafeToastUseCase(owner, e.toString())
            }
            false
        }
    }
}