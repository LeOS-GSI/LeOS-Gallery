package ca.on.sudbury.hojat.smartgallery.usecases

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.exifinterface.media.ExifInterface
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.extensions.getCompressionFormat
import ca.on.sudbury.hojat.smartgallery.extensions.getFileInputStreamSync
import ca.on.sudbury.hojat.smartgallery.extensions.getFileKey
import ca.on.sudbury.hojat.smartgallery.extensions.getFileOutputStream
import ca.on.sudbury.hojat.smartgallery.extensions.getFileOutputStreamSync
import ca.on.sudbury.hojat.smartgallery.extensions.getFilenameFromPath
import ca.on.sudbury.hojat.smartgallery.extensions.getMimeType
import ca.on.sudbury.hojat.smartgallery.extensions.getSomeDocumentFile
import ca.on.sudbury.hojat.smartgallery.extensions.isSDCardSetAsDefaultStorage
import ca.on.sudbury.hojat.smartgallery.extensions.recycleBinPath
import ca.on.sudbury.hojat.smartgallery.extensions.rescanPaths
import ca.on.sudbury.hojat.smartgallery.extensions.tryDeleteFileDirItem
import ca.on.sudbury.hojat.smartgallery.extensions.updateLastModified
import ca.on.sudbury.hojat.smartgallery.models.FileDirItem
import ca.on.sudbury.hojat.smartgallery.photoedit.usecases.IsNougatPlusUseCase
import ca.on.sudbury.hojat.smartgallery.photoedit.usecases.IsRPlusUseCase
import com.bumptech.glide.Glide
import com.squareup.picasso.Picasso
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

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
                    copyFile(owner, oldPath, tmpPath)
                    saveExifRotation(ExifInterface(tmpPath), newDegrees)
                } else {
                    val inputstream = owner.getFileInputStreamSync(oldPath)
                    val bitmap = BitmapFactory.decodeStream(inputstream)
                    saveFile(tmpPath, bitmap, it as FileOutputStream, newDegrees)
                }
                with(owner) {
                    copyFile(this, tmpPath, newPath)
                    applicationContext.rescanPaths(arrayListOf(newPath))
                    fileRotatedSuccessfully(this, newPath, oldLastModified)
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
            if (saveImageRotation(owner, path, degrees)) {
                fileRotatedSuccessfully(owner, path, oldLastModified)
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

    private fun fileRotatedSuccessfully(owner: Activity, path: String, lastModified: Long) {
        if (owner.config.keepLastModified && lastModified != 0L) {
            File(path).setLastModified(lastModified)
            owner.updateLastModified(path, lastModified)
        }

        Picasso.get().invalidate(path.getFileKey(lastModified))
        // we cannot refresh a specific image in Glide Cache, so just clear it all
        val glide = Glide.get(owner.applicationContext)
        glide.clearDiskCache()
        owner.runOnUiThread {
            glide.clearMemory()
        }
    }

    private fun copyFile(owner: BaseSimpleActivity, source: String, destination: String) {
        var inputStream: InputStream? = null
        var out: OutputStream? = null
        try {
            out = owner.getFileOutputStreamSync(destination, source.getMimeType())
            inputStream = owner.getFileInputStreamSync(source)
            inputStream!!.copyTo(out!!)
        } catch (e: Exception) {
            ShowSafeToastUseCase(owner, e.toString())
        } finally {
            inputStream?.close()
            out?.close()
        }
    }

    private fun saveFile(path: String, bitmap: Bitmap, out: FileOutputStream, degrees: Int) {
        val matrix = Matrix()
        matrix.postRotate(degrees.toFloat())
        val bmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        bmp.compress(path.getCompressionFormat(), 90, out)
    }

    @SuppressLint("Recycle")
    @RequiresApi(Build.VERSION_CODES.N)
    private fun saveImageRotation(owner: Context, path: String, degrees: Int): Boolean {
        if (!(!IsRPlusUseCase() &&
                    (IsPathOnSdUseCase(owner, path) ||
                            IsPathOnOtgUseCase(owner, path)) &&
                    !owner.isSDCardSetAsDefaultStorage())
        ) {
            saveExifRotation(ExifInterface(path), degrees)
            return true
        } else if (IsNougatPlusUseCase()) {
            val documentFile = owner.getSomeDocumentFile(path)
            if (documentFile != null) {
                val parcelFileDescriptor =
                    owner.contentResolver.openFileDescriptor(documentFile.uri, "rw")
                val fileDescriptor = parcelFileDescriptor!!.fileDescriptor
                saveExifRotation(ExifInterface(fileDescriptor), degrees)
                return true
            }
        }
        return false
    }

    private fun saveExifRotation(exif: ExifInterface, degrees: Int) {
        val orientation =
            exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        val orientationDegrees = (getDegreesFromOrientation(orientation) + degrees) % 360
        exif.setAttribute(
            ExifInterface.TAG_ORIENTATION,
            orientationFromDegrees(orientationDegrees)
        )
        exif.saveAttributes()
    }

    private fun getDegreesFromOrientation(orientation: Int) = when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_270 -> 270
        ExifInterface.ORIENTATION_ROTATE_180 -> 180
        ExifInterface.ORIENTATION_ROTATE_90 -> 90
        else -> 0
    }

    private fun orientationFromDegrees(degree: Int) = when (degree) {
        270 -> ExifInterface.ORIENTATION_ROTATE_270
        180 -> ExifInterface.ORIENTATION_ROTATE_180
        90 -> ExifInterface.ORIENTATION_ROTATE_90
        else -> ExifInterface.ORIENTATION_NORMAL
    }.toString()

}