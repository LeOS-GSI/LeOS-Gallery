package ca.on.sudbury.hojat.smartgallery.models

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.BaseColumns
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import ca.on.sudbury.hojat.smartgallery.extensions.createAndroidSAFDocumentId
import ca.on.sudbury.hojat.smartgallery.extensions.formatDate
import ca.on.sudbury.hojat.smartgallery.extensions.getAndroidSAFDirectChildrenCount
import ca.on.sudbury.hojat.smartgallery.extensions.getAndroidTreeUri
import ca.on.sudbury.hojat.smartgallery.extensions.getDirectChildrenCount
import ca.on.sudbury.hojat.smartgallery.extensions.getDocumentFile
import ca.on.sudbury.hojat.smartgallery.extensions.getDuration
import ca.on.sudbury.hojat.smartgallery.extensions.getFastDocumentFile
import ca.on.sudbury.hojat.smartgallery.extensions.getFileCount
import ca.on.sudbury.hojat.smartgallery.extensions.getFileSize
import ca.on.sudbury.hojat.smartgallery.extensions.getFileUri
import ca.on.sudbury.hojat.smartgallery.extensions.getFormattedDuration
import ca.on.sudbury.hojat.smartgallery.extensions.getImageResolution
import ca.on.sudbury.hojat.smartgallery.extensions.getLongValue
import ca.on.sudbury.hojat.smartgallery.extensions.getParentPath
import ca.on.sudbury.hojat.smartgallery.extensions.getProperChildrenCount
import ca.on.sudbury.hojat.smartgallery.extensions.getProperSize
import ca.on.sudbury.hojat.smartgallery.extensions.getResolution
import ca.on.sudbury.hojat.smartgallery.extensions.getStringValue
import ca.on.sudbury.hojat.smartgallery.extensions.isAndroidDataDir
import ca.on.sudbury.hojat.smartgallery.extensions.isImageFast
import ca.on.sudbury.hojat.smartgallery.extensions.isRestrictedSAFOnlyRoot
import ca.on.sudbury.hojat.smartgallery.extensions.isVideoFast
import ca.on.sudbury.hojat.smartgallery.extensions.normalizeString
import ca.on.sudbury.hojat.smartgallery.helpers.AlphanumericComparator
import ca.on.sudbury.hojat.smartgallery.helpers.SORT_BY_DATE_MODIFIED
import ca.on.sudbury.hojat.smartgallery.helpers.SORT_BY_EXTENSION
import ca.on.sudbury.hojat.smartgallery.helpers.SORT_BY_NAME
import ca.on.sudbury.hojat.smartgallery.helpers.SORT_BY_SIZE
import ca.on.sudbury.hojat.smartgallery.helpers.SORT_DESCENDING
import ca.on.sudbury.hojat.smartgallery.helpers.SORT_USE_NUMERIC_VALUE
import ca.on.sudbury.hojat.smartgallery.photoedit.usecases.IsNougatPlusUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.FormatFileSizeUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.GetFileCountUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.GetFileSizeUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsPathOnOtgUseCase
import com.bumptech.glide.signature.ObjectKey
import java.io.File
import java.util.Locale

open class FileDirItem(
    val path: String,
    val name: String = "",
    var isDirectory: Boolean = false,
    var children: Int = 0,
    var size: Long = 0L,
    var modified: Long = 0L,
    var mediaStoreId: Long = 0L
) :
    Comparable<FileDirItem> {
    companion object {
        var sorting = 0
    }

    override fun toString() =
        "FileDirItem(path=$path, name=$name, isDirectory=$isDirectory, children=$children, size=$size, modified=$modified, mediaStoreId=$mediaStoreId)"

    override fun compareTo(other: FileDirItem): Int {
        return if (isDirectory && !other.isDirectory) {
            -1
        } else if (!isDirectory && other.isDirectory) {
            1
        } else {
            var result: Int
            when {
                sorting and SORT_BY_NAME != 0 -> {
                    result = if (sorting and SORT_USE_NUMERIC_VALUE != 0) {
                        AlphanumericComparator().compare(
                            name.normalizeString().lowercase(Locale.ROOT),
                            other.name.normalizeString().lowercase(Locale.ROOT)
                        )
                    } else {
                        name.normalizeString().lowercase(Locale.ROOT)
                            .compareTo(other.name.normalizeString().lowercase(Locale.ROOT))
                    }
                }
                sorting and SORT_BY_SIZE != 0 -> result = when {
                    size == other.size -> 0
                    size > other.size -> 1
                    else -> -1
                }
                sorting and SORT_BY_DATE_MODIFIED != 0 -> {
                    result = when {
                        modified == other.modified -> 0
                        modified > other.modified -> 1
                        else -> -1
                    }
                }
                else -> {
                    result =
                        getExtension().lowercase(Locale.ROOT)
                            .compareTo(other.getExtension().lowercase(Locale.ROOT))
                }
            }

            if (sorting and SORT_DESCENDING != 0) {
                result *= -1
            }
            result
        }
    }

    fun getExtension() = if (isDirectory) name else path.substringAfterLast('.', "")

    fun getBubbleText(context: Context, dateFormat: String? = null, timeFormat: String? = null) =
        when {
            sorting and SORT_BY_SIZE != 0 -> FormatFileSizeUseCase(size)
            sorting and SORT_BY_DATE_MODIFIED != 0 -> modified.formatDate(
                context,
                dateFormat,
                timeFormat
            )
            sorting and SORT_BY_EXTENSION != 0 -> getExtension().lowercase(Locale.ROOT)
            else -> name
        }

    @SuppressLint("Recycle")
    fun getProperSize(context: Context, countHidden: Boolean): Long {
        return when {
            context.isRestrictedSAFOnlyRoot(path) -> getAndroidSAFFileSize(context, path)
            IsPathOnOtgUseCase(context, path) ->
                GetFileSizeUseCase(context.getDocumentFile(path), countHidden)
            IsNougatPlusUseCase() && path.startsWith("content://") -> {
                try {
                    context.contentResolver.openInputStream(Uri.parse(path))?.available()?.toLong()
                        ?: 0L
                } catch (e: Exception) {
                    getSizeFromContentUri(context, Uri.parse(path))
                }
            }
            else -> File(path).getProperSize(countHidden)
        }
    }

    fun getProperFileCount(context: Context, countHidden: Boolean): Int {
        return when {
            context.isRestrictedSAFOnlyRoot(path) -> getAndroidSAFFileCount(
                context,
                path,
                countHidden
            )
            IsPathOnOtgUseCase(context, path) -> GetFileCountUseCase(
                context.getDocumentFile(path),
                countHidden
            )
            else -> File(path).getFileCount(countHidden)
        }
    }

    fun getDirectChildrenCount(context: Context, countHiddenItems: Boolean): Int {
        return when {
            context.isRestrictedSAFOnlyRoot(path) -> context.getAndroidSAFDirectChildrenCount(
                path,
                countHiddenItems
            )
            IsPathOnOtgUseCase(context, path) -> context.getDocumentFile(path)?.listFiles()
                ?.filter { if (countHiddenItems) true else !it.name!!.startsWith(".") }?.size
                ?: 0
            else -> File(path).getDirectChildrenCount(context, countHiddenItems)
        }
    }

    fun getLastModified(context: Context): Long {
        return when {
            context.isRestrictedSAFOnlyRoot(path) -> getAndroidSAFLastModified(context, path)
            IsPathOnOtgUseCase(context, path) ->
                context.getFastDocumentFile(path)?.lastModified() ?: 0L
            IsNougatPlusUseCase() && path.startsWith("content://") -> getMediaStoreLastModified(
                context,
                path
            )
            else -> File(path).lastModified()
        }
    }

    fun getParentPath() = path.getParentPath()

    @RequiresApi(Build.VERSION_CODES.Q)
    fun getDuration(context: Context) = context.getDuration(path)?.getFormattedDuration()

    fun getArtist(context: Context) = getArtist(context, path)

    fun getAlbum(context: Context) = getAlbum(context, path)

    fun getTitle(context: Context) = getTitle(context, path)

    fun getResolution(context: Context) = context.getResolution(path)

    fun getImageResolution(context: Context) = context.getImageResolution(path)

    private fun getSignature(): String {
        val lastModified = if (modified > 1) {
            modified
        } else {
            File(path).lastModified()
        }

        return "$path-$lastModified-$size"
    }

    fun getKey() = ObjectKey(getSignature())

    fun assembleContentUri(): Uri {
        val uri = when {
            path.isImageFast() -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            path.isVideoFast() -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            else -> MediaStore.Files.getContentUri("external")
        }

        return Uri.withAppendedPath(uri, mediaStoreId.toString())
    }

    private fun getSizeFromContentUri(owner: Context, uri: Uri): Long {
        val projection = arrayOf(OpenableColumns.SIZE)
        val cursor = owner.contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (cursor.moveToFirst()) {
                return cursor.getLongValue(OpenableColumns.SIZE)
            }
        }
        return 0L
    }

    private fun getTitle(owner: Context, path: String): String? {
        val projection = arrayOf(
            MediaStore.MediaColumns.TITLE
        )

        val uri = getFileUri(path)
        val selection =
            if (path.startsWith("content://")) "${BaseColumns._ID} = ?" else "${MediaStore.MediaColumns.DATA} = ?"
        val selectionArgs =
            if (path.startsWith("content://")) arrayOf(path.substringAfterLast("/")) else arrayOf(
                path
            )

        try {
            val cursor =
                owner.contentResolver.query(uri, projection, selection, selectionArgs, null)
            cursor?.use {
                if (cursor.moveToFirst()) {
                    return cursor.getStringValue(MediaStore.MediaColumns.TITLE)
                }
            }
        } catch (ignored: Exception) {
        }

        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(path)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        } catch (ignored: Exception) {
            null
        }
    }

    private fun getAlbum(owner: Context, path: String): String? {
        val projection = arrayOf(
            MediaStore.Audio.Media.ALBUM
        )

        val uri = getFileUri(path)
        val selection =
            if (path.startsWith("content://")) "${BaseColumns._ID} = ?" else "${MediaStore.MediaColumns.DATA} = ?"
        val selectionArgs =
            if (path.startsWith("content://")) arrayOf(path.substringAfterLast("/")) else arrayOf(
                path
            )

        try {
            val cursor =
                owner.contentResolver.query(uri, projection, selection, selectionArgs, null)
            cursor?.use {
                if (cursor.moveToFirst()) {
                    return cursor.getStringValue(MediaStore.Audio.Media.ALBUM)
                }
            }
        } catch (ignored: Exception) {
        }

        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(path)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
        } catch (ignored: Exception) {
            null
        }
    }

    private fun getMediaStoreLastModified(owner: Context, path: String): Long {
        val projection = arrayOf(
            MediaStore.MediaColumns.DATE_MODIFIED
        )

        val uri = getFileUri(path)
        val selection = "${BaseColumns._ID} = ?"
        val selectionArgs = arrayOf(path.substringAfterLast("/"))

        try {
            val cursor =
                owner.contentResolver.query(uri, projection, selection, selectionArgs, null)
            cursor?.use {
                if (cursor.moveToFirst()) {
                    return cursor.getLongValue(MediaStore.MediaColumns.DATE_MODIFIED) * 1000
                }
            }
        } catch (ignored: Exception) {
        }
        return 0
    }

    private fun getAndroidSAFFileSize(owner: Context, path: String): Long {
        val treeUri = owner.getAndroidTreeUri(path).toUri()
        val documentId = owner.createAndroidSAFDocumentId(path)
        return owner.getFileSize(treeUri, documentId)
    }

    private fun getAndroidSAFFileCount(
        owner: Context,
        path: String,
        countHidden: Boolean
    ): Int {
        val treeUri = owner.getAndroidTreeUri(path).toUri()
        if (treeUri == Uri.EMPTY) {
            return 0
        }

        val documentId = owner.createAndroidSAFDocumentId(path)
        val rootDocId = getStorageRootIdForAndroidDir(owner, path)
        return owner.getProperChildrenCount(rootDocId, treeUri, documentId, countHidden)
    }

    private fun getStorageRootIdForAndroidDir(owner: Context, path: String) =
        owner.getAndroidTreeUri(path).removeSuffix(
            if (isAndroidDataDir(
                    path
                )
            ) "%3AAndroid%2Fdata" else "%3AAndroid%2Fobb"
        ).substringAfterLast('/').trimEnd('/')

    private fun getArtist(owner: Context, path: String): String? {
        val projection = arrayOf(
            MediaStore.Audio.Media.ARTIST
        )

        val uri = getFileUri(path)
        val selection =
            if (path.startsWith("content://")) "${BaseColumns._ID} = ?" else "${MediaStore.MediaColumns.DATA} = ?"
        val selectionArgs =
            if (path.startsWith("content://")) arrayOf(path.substringAfterLast("/")) else arrayOf(
                path
            )

        try {
            val cursor =
                owner.contentResolver.query(uri, projection, selection, selectionArgs, null)
            cursor?.use {
                if (cursor.moveToFirst()) {
                    return cursor.getStringValue(MediaStore.Audio.Media.ARTIST)
                }
            }
        } catch (ignored: Exception) {
        }

        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(path)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
        } catch (ignored: Exception) {
            null
        }
    }

    private fun getAndroidSAFLastModified(owner: Context, path: String): Long {
        val treeUri = owner.getAndroidTreeUri(path).toUri()
        if (treeUri == Uri.EMPTY) {
            return 0L
        }

        val documentId = owner.createAndroidSAFDocumentId(path)
        val projection = arrayOf(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
        val documentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
        return owner.contentResolver.query(documentUri, projection, null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getLongValue(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
                } else {
                    0L
                }
            } ?: 0L
    }

}
