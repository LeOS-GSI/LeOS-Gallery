package ca.on.sudbury.hojat.smartgallery.extensions

import android.Manifest
import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.PictureDrawable
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbManager
import android.media.MediaMetadataRetriever
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Process
import android.provider.BaseColumns
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.MediaStore.Files
import android.provider.MediaStore.Images
import android.provider.OpenableColumns
import android.provider.Settings
import android.text.TextUtils
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.documentfile.provider.DocumentFile
import androidx.loader.content.CursorLoader
import ca.on.hojat.palette.views.MyButton
import ca.on.hojat.palette.views.MySquareImageView
import ca.on.hojat.palette.views.MyTextView
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.asynctasks.GetMediaAsynctask
import ca.on.sudbury.hojat.smartgallery.database.MediumDao
import ca.on.sudbury.hojat.smartgallery.database.WidgetsDao
import ca.on.sudbury.hojat.smartgallery.databases.GalleryDatabase
import ca.on.sudbury.hojat.smartgallery.helpers.AlphanumericComparator
import ca.on.sudbury.hojat.smartgallery.helpers.BaseConfig
import ca.on.sudbury.hojat.smartgallery.helpers.BaseContentProvider
import ca.on.sudbury.hojat.smartgallery.helpers.Config
import ca.on.sudbury.hojat.smartgallery.helpers.DARK_GREY
import ca.on.sudbury.hojat.smartgallery.helpers.EXTERNAL_STORAGE_PROVIDER_AUTHORITY
import ca.on.sudbury.hojat.smartgallery.helpers.ExternalStorageProviderHack
import ca.on.sudbury.hojat.smartgallery.helpers.FAVORITES
import ca.on.sudbury.hojat.smartgallery.helpers.FONT_SIZE_LARGE
import ca.on.sudbury.hojat.smartgallery.helpers.FONT_SIZE_MEDIUM
import ca.on.sudbury.hojat.smartgallery.helpers.FONT_SIZE_SMALL
import ca.on.sudbury.hojat.smartgallery.helpers.FileLocation
import ca.on.sudbury.hojat.smartgallery.helpers.GroupBy
import ca.on.sudbury.hojat.smartgallery.helpers.INVALID_NAVIGATION_BAR_COLOR
import ca.on.sudbury.hojat.smartgallery.helpers.IsoTypeReader
import ca.on.sudbury.hojat.smartgallery.helpers.MediaFetcher
import ca.on.sudbury.hojat.smartgallery.helpers.MediaType
import ca.on.sudbury.hojat.smartgallery.helpers.NOMEDIA
import ca.on.sudbury.hojat.smartgallery.helpers.PERMISSION_CALL_PHONE
import ca.on.sudbury.hojat.smartgallery.helpers.PERMISSION_CAMERA
import ca.on.sudbury.hojat.smartgallery.helpers.PERMISSION_GET_ACCOUNTS
import ca.on.sudbury.hojat.smartgallery.helpers.PERMISSION_MEDIA_LOCATION
import ca.on.sudbury.hojat.smartgallery.helpers.PERMISSION_READ_CALENDAR
import ca.on.sudbury.hojat.smartgallery.helpers.PERMISSION_READ_CALL_LOG
import ca.on.sudbury.hojat.smartgallery.helpers.PERMISSION_READ_CONTACTS
import ca.on.sudbury.hojat.smartgallery.helpers.PERMISSION_READ_PHONE_STATE
import ca.on.sudbury.hojat.smartgallery.helpers.PERMISSION_READ_SMS
import ca.on.sudbury.hojat.smartgallery.helpers.PERMISSION_READ_STORAGE
import ca.on.sudbury.hojat.smartgallery.helpers.PERMISSION_RECORD_AUDIO
import ca.on.sudbury.hojat.smartgallery.helpers.PERMISSION_SEND_SMS
import ca.on.sudbury.hojat.smartgallery.helpers.PERMISSION_WRITE_CALENDAR
import ca.on.sudbury.hojat.smartgallery.helpers.PERMISSION_WRITE_CALL_LOG
import ca.on.sudbury.hojat.smartgallery.helpers.PERMISSION_WRITE_CONTACTS
import ca.on.sudbury.hojat.smartgallery.helpers.PERMISSION_WRITE_STORAGE
import ca.on.sudbury.hojat.smartgallery.helpers.PicassoRoundedCornersTransformation
import ca.on.sudbury.hojat.smartgallery.helpers.RECYCLE_BIN
import ca.on.sudbury.hojat.smartgallery.helpers.ROUNDED_CORNERS_NONE
import ca.on.sudbury.hojat.smartgallery.helpers.ROUNDED_CORNERS_SMALL
import ca.on.sudbury.hojat.smartgallery.helpers.SD_OTG_PATTERN
import ca.on.sudbury.hojat.smartgallery.helpers.SD_OTG_SHORT
import ca.on.sudbury.hojat.smartgallery.helpers.SHOW_ALL
import ca.on.sudbury.hojat.smartgallery.helpers.SORT_BY_CUSTOM
import ca.on.sudbury.hojat.smartgallery.helpers.SORT_BY_DATE_MODIFIED
import ca.on.sudbury.hojat.smartgallery.helpers.SORT_BY_DATE_TAKEN
import ca.on.sudbury.hojat.smartgallery.helpers.SORT_BY_NAME
import ca.on.sudbury.hojat.smartgallery.helpers.SORT_BY_PATH
import ca.on.sudbury.hojat.smartgallery.helpers.SORT_BY_RANDOM
import ca.on.sudbury.hojat.smartgallery.helpers.SORT_BY_SIZE
import ca.on.sudbury.hojat.smartgallery.helpers.SORT_DESCENDING
import ca.on.sudbury.hojat.smartgallery.helpers.SORT_USE_NUMERIC_VALUE
import ca.on.sudbury.hojat.smartgallery.helpers.SmartGalleryWidgetProvider
import ca.on.sudbury.hojat.smartgallery.helpers.sumByLong
import ca.on.sudbury.hojat.smartgallery.models.AlbumCover
import ca.on.sudbury.hojat.smartgallery.models.Directory
import ca.on.sudbury.hojat.smartgallery.models.Favorite
import ca.on.sudbury.hojat.smartgallery.models.FileDirItem
import ca.on.sudbury.hojat.smartgallery.models.Medium
import ca.on.sudbury.hojat.smartgallery.models.SharedTheme
import ca.on.sudbury.hojat.smartgallery.models.ThumbnailItem
import ca.on.sudbury.hojat.smartgallery.svg.SvgSoftwareLayerSetter
import ca.on.sudbury.hojat.smartgallery.usecases.IsGifUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsMarshmallowPlusUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsPathOnOtgUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsPathOnSdUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsPngUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsQPlusUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsRPlusUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsSvgUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.RunOnBackgroundThreadUseCase
import ca.on.sudbury.hojat.smartgallery.views.MyAppCompatCheckbox
import ca.on.sudbury.hojat.smartgallery.views.MyAppCompatSpinner
import ca.on.sudbury.hojat.smartgallery.views.MyAutoCompleteTextView
import ca.on.sudbury.hojat.smartgallery.views.MyCompatRadioButton
import ca.on.sudbury.hojat.smartgallery.views.MyEditText
import ca.on.sudbury.hojat.smartgallery.views.MyFloatingActionButton
import ca.on.sudbury.hojat.smartgallery.views.MySeekBar
import ca.on.sudbury.hojat.smartgallery.views.MySwitchCompat
import ca.on.sudbury.hojat.smartgallery.views.MyTextInputLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.ObjectKey
import com.squareup.picasso.Picasso
import pl.droidsonroids.gif.GifDrawable
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.net.URLDecoder
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.set
import kotlin.math.roundToInt

// avoid these being set as SD card paths
@SuppressLint("SdCardPath")
private val physicalPaths = arrayListOf(
    "/storage/sdcard1", // Motorola Xoom
    "/storage/extsdcard", // Samsung SGS3
    "/storage/sdcard0/external_sdcard", // User request
    "/mnt/extsdcard", "/mnt/sdcard/external_sd", // Samsung galaxy family
    "/mnt/external_sd", "/mnt/media_rw/sdcard1", // 4.4.2 on CyanogenMod S3
    "/removable/microsd", // Asus transformer prime
    "/mnt/emmc", "/storage/external_SD", // LG
    "/storage/ext_sd", // HTC One Max
    "/storage/removable/sdcard1", // Sony Xperia Z1
    "/data/sdext", "/data/sdext2", "/data/sdext3", "/data/sdext4", "/sdcard1", // Sony Xperia Z
    "/sdcard2", // HTC One M8s
    "/storage/usbdisk0",
    "/storage/usbdisk1",
    "/storage/usbdisk2"
)

val Context.areSystemAnimationsEnabled: Boolean
    get() = Settings.Global.getFloat(
        contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        0f
    ) > 0f

fun Context.createFirstParentTreeUri(fullPath: String): Uri {
    val storageId = getSAFStorageId(this, fullPath)
    val level = getFirstParentLevel(fullPath)
    val rootParentDirName = fullPath.getFirstParentDirName(this, level)
    val firstParentId = "$storageId:$rootParentDirName"
    return DocumentsContract.buildTreeDocumentUri(
        EXTERNAL_STORAGE_PROVIDER_AUTHORITY,
        firstParentId
    )
}

fun Context.createDocumentUriUsingFirstParentTreeUri(fullPath: String): Uri {
    val storageId = getSAFStorageId(this, fullPath)
    val relativePath = when {
        fullPath.startsWith(internalStoragePath) -> fullPath.substring(internalStoragePath.length)
            .trim('/')
        else -> fullPath.substringAfter(storageId).trim('/')
    }
    val treeUri = createFirstParentTreeUri(fullPath)
    val documentId = "${storageId}:$relativePath"
    return DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
}

fun Context.createSAFDirectorySdk30(path: String): Boolean {
    return try {
        val treeUri = createFirstParentTreeUri(path)
        val parentPath = path.getParentPath()
        if (!getDoesFilePathExistSdk30(parentPath)) {
            createSAFDirectorySdk30(parentPath)
        }

        val documentId = getSAFDocumentId(parentPath)
        val parentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
        DocumentsContract.createDocument(
            contentResolver,
            parentUri,
            DocumentsContract.Document.MIME_TYPE_DIR,
            path.getFilenameFromPath()
        ) != null
    } catch (e: IllegalStateException) {

        Timber.e(e)
        false
    }
}

fun Context.getDoesFilePathExistSdk30(path: String): Boolean {
    return when {
        isAccessibleWithSAFSdk30(path) -> getFastDocumentSdk30(path)?.exists() ?: false
        else -> File(path).exists()
    }
}

private fun getMediaContent(owner: Context, path: String, uri: Uri): Uri? {
    val projection = arrayOf(Images.Media._ID)
    val selection = Images.Media.DATA + "= ?"
    val selectionArgs = arrayOf(path)
    try {
        val cursor = owner.contentResolver.query(uri, projection, selection, selectionArgs, null)
        cursor?.use {
            if (cursor.moveToFirst()) {
                val id = cursor.getIntValue(Images.Media._ID).toString()
                return Uri.withAppendedPath(uri, id)
            }
        }
    } catch (_: Exception) {
    }
    return null
}

fun Context.getMyContentProviderCursorLoader() =
    CursorLoader(this, BaseContentProvider.MY_CONTENT_URI, null, null, null, null)

fun Context.queryCursor(
    uri: Uri,
    projection: Array<String>,
    selection: String? = null,
    selectionArgs: Array<String>? = null,
    sortOrder: String? = null,
    showErrors: Boolean = false,
    callback: (cursor: Cursor) -> Unit
) {
    try {
        val cursor = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)
        cursor?.use {
            if (cursor.moveToFirst()) {
                do {
                    callback(cursor)
                } while (cursor.moveToNext())
            }
        }
    } catch (e: Exception) {
        if (showErrors) {
            Timber.e(e)
        }
    }
}

private fun Context.queryCursorDesc(
    uri: Uri,
    projection: Array<String>,
    sortColumn: String,
    limit: Int,
): Cursor? {
    return if (IsRPlusUseCase()) {
        val queryArgs = bundleOf(
            ContentResolver.QUERY_ARG_LIMIT to limit,
            ContentResolver.QUERY_ARG_SORT_DIRECTION to ContentResolver.QUERY_SORT_DIRECTION_DESCENDING,
            ContentResolver.QUERY_ARG_SORT_COLUMNS to arrayOf(sortColumn),
        )
        contentResolver.query(uri, projection, queryArgs, null)
    } else {
        val sortOrder = "$sortColumn DESC LIMIT $limit"
        contentResolver.query(uri, projection, null, null, sortOrder)
    }
}

private fun isExternalStorageDocument(uri: Uri) =
    uri.authority == "com.android.externalstorage.documents"

private fun isMediaDocument(uri: Uri) = uri.authority == "com.android.providers.media.documents"


private const val ANDROID_DATA_DIR = "/Android/data/"
private const val ANDROID_OBB_DIR = "/Android/obb/"
val DIRS_ACCESSIBLE_ONLY_WITH_SAF = listOf(ANDROID_DATA_DIR, ANDROID_OBB_DIR)

fun getPaths(file: File): java.util.ArrayList<String> {
    val paths = arrayListOf<String>(file.absolutePath)
    if (file.isDirectory) {
        val files = file.listFiles() ?: return paths
        for (curFile in files) {
            paths.addAll(getPaths(curFile))
        }
    }
    return paths
}

// TODO : Everything about baseConfig should be extracted into a repository
val Context.baseConfig: BaseConfig get() = BaseConfig.newInstance(this)

val Context.recycleBinPath: String get() = filesDir.absolutePath

fun Context.getAndroidSAFDirectChildrenCount(path: String, countHidden: Boolean): Int {
    val treeUri = getAndroidTreeUri(path).toUri()
    if (treeUri == Uri.EMPTY) {
        return 0
    }

    val documentId = createAndroidSAFDocumentId(path)
    val rootDocId = getStorageRootIdForAndroidDir(this, path)
    return getDirectChildrenCount(rootDocId, treeUri, documentId, countHidden)
}

fun Context.getAndroidSAFUri(path: String): Uri {
    val treeUri = getAndroidTreeUri(path).toUri()
    val documentId = createAndroidSAFDocumentId(path)
    return DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
}

fun Context.getAndroidTreeUri(path: String): String {
    return when {
        IsPathOnOtgUseCase(this, path) ->
            if (isAndroidDataDir(path)) baseConfig.otgAndroidDataTreeUri else baseConfig.otgAndroidObbTreeUri
        IsPathOnSdUseCase(this, path) ->
            if (isAndroidDataDir(path)) baseConfig.sdAndroidDataTreeUri else baseConfig.sdAndroidObbTreeUri
        else -> if (isAndroidDataDir(path)) baseConfig.primaryAndroidDataTreeUri else baseConfig.primaryAndroidObbTreeUri
    }
}

fun getCurrentFormattedDateTime(): String {
    val simpleDateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault())
    return simpleDateFormat.format(Date(System.currentTimeMillis()))
}

fun Context.getDirectChildrenCount(
    rootDocId: String,
    treeUri: Uri,
    documentId: String,
    shouldShowHidden: Boolean
): Int {
    return try {
        val projection = arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, documentId)
        val rawCursor = contentResolver.query(childrenUri, projection, null, null, null)!!
        val cursor =
            ExternalStorageProviderHack.transformQueryResult(rootDocId, childrenUri, rawCursor)
        if (shouldShowHidden) {
            cursor.count
        } else {
            var count = 0
            cursor.use {
                while (cursor.moveToNext()) {
                    val docId = cursor.getStringValue(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                    if (!docId.getFilenameFromPath().startsWith('.') || shouldShowHidden) {
                        count++
                    }
                }
            }
            count
        }
    } catch (e: Exception) {
        0
    }
}

fun Context.getDocumentFile(path: String): DocumentFile? {
    val isOTG = IsPathOnOtgUseCase(this, path)
    var relativePath =
        path.substring(if (isOTG) baseConfig.otgPath.length else baseConfig.sdCardPath.length)
    if (relativePath.startsWith(File.separator)) {
        relativePath = relativePath.substring(1)
    }

    return try {
        val treeUri = Uri.parse(if (isOTG) baseConfig.otgTreeUri else baseConfig.sdTreeUri)
        var document = DocumentFile.fromTreeUri(applicationContext, treeUri)
        val parts = relativePath.split("/").filter { it.isNotEmpty() }
        for (part in parts) {
            document = document?.findFile(part)
        }
        document
    } catch (ignored: Exception) {
        null
    }
}

fun Context.getDocumentSdk30(path: String): DocumentFile? {
    val level = getFirstParentLevel(path)
    val firstParentPath = path.getFirstParentPath(this, level)
    var relativePath = path.substring(firstParentPath.length)
    if (relativePath.startsWith(File.separator)) {
        relativePath = relativePath.substring(1)
    }

    return try {
        val treeUri = createFirstParentTreeUri(path)
        var document = DocumentFile.fromTreeUri(applicationContext, treeUri)
        val parts = relativePath.split("/").filter { it.isNotEmpty() }
        for (part in parts) {
            document = document?.findFile(part)
        }
        document
    } catch (ignored: Exception) {
        null
    }
}

fun Context.getDoesFilePathExist(path: String, otgPathToUse: String? = null): Boolean {
    val otgPath = otgPathToUse ?: baseConfig.otgPath
    return when {
        isRestrictedSAFOnlyRoot(path) -> getFastAndroidSAFDocument(this, path)?.exists() ?: false
        otgPath.isNotEmpty() && path.startsWith(otgPath) -> getOTGFastDocumentFile(
            this,
            path
        )?.exists()
            ?: false
        else -> File(path).exists()
    }
}

fun Context.getFastDocumentSdk30(path: String): DocumentFile? {
    val uri = createDocumentUriUsingFirstParentTreeUri(path)
    return DocumentFile.fromSingleUri(this, uri)
}

fun Context.getFileInputStreamSync(path: String): InputStream? {
    return when {
        isRestrictedSAFOnlyRoot(path) -> {
            val uri = getAndroidSAFUri(path)
            applicationContext.contentResolver.openInputStream(uri)
        }
        isAccessibleWithSAFSdk30(path) -> {
            try {
                FileInputStream(File(path))
            } catch (e: Exception) {
                val uri = createDocumentUriUsingFirstParentTreeUri(path)
                applicationContext.contentResolver.openInputStream(uri)
            }
        }
        IsPathOnOtgUseCase(this, path) -> {
            val fileDocument = getSomeDocumentFile(path)
            applicationContext.contentResolver.openInputStream(fileDocument?.uri!!)
        }
        else -> FileInputStream(File(path))
    }
}

fun Context.getFilenameFromContentUri(uri: Uri): String? {
    val projection = arrayOf(
        OpenableColumns.DISPLAY_NAME
    )

    try {
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (cursor.moveToFirst()) {
                return cursor.getStringValue(OpenableColumns.DISPLAY_NAME)
            }
        }
    } catch (_: Exception) {
    }
    return null
}

fun Context.getFilenameFromUri(uri: Uri): String {
    return if (uri.scheme == "file") {
        File(uri.toString()).name
    } else {
        getFilenameFromContentUri(uri) ?: uri.lastPathSegment ?: ""
    }
}

fun Context.getFilePublicUri(file: File, applicationId: String): Uri {
    // for images/videos/gifs try getting a media content uri first, like content://media/external/images/media/438
    // if media content uri is null, get our custom uri like content://com.simplemobiletools.gallery.provider/external_files/emulated/0/DCIM/IMG_20171104_233915.jpg
    var uri = if (file.absolutePath.isMediaFile()) {
        getMediaContentUri(this, file.absolutePath)
    } else {
        getMediaContent(this, file.absolutePath, Files.getContentUri("external"))
    }

    if (uri == null) {
        uri = FileProvider.getUriForFile(this, "$applicationId.provider", file)
    }

    return uri!!
}

fun getFileUri(path: String): Uri = when {
    path.isImageSlow() -> Images.Media.EXTERNAL_CONTENT_URI
    path.isVideoSlow() -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    path.isAudioSlow() -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    else -> Files.getContentUri("external")
}

fun Context.getFileUrisFromFileDirItems(fileDirItems: List<FileDirItem>): List<Uri> {
    val fileUris = getUrisPathsFromFileDirItems(this, fileDirItems).second
    if (fileUris.isEmpty()) {
        fileDirItems.map { fileDirItem ->
            fileUris.add(fileDirItem.assembleContentUri())
        }
    }

    return fileUris
}

fun Context.getFirstParentLevel(path: String): Int {
    return when {
        IsRPlusUseCase() && (isInAndroidDir(this, path) || isInSubFolderInDownloadDir(path)) -> 1
        else -> 0
    }
}

private fun getHumanReadablePath(owner: Context, path: String) = owner.getString(
    when (path) {
        "/" -> R.string.root
        owner.internalStoragePath -> R.string.internal
        owner.baseConfig.otgPath -> R.string.usb
        else -> R.string.sd_card
    }
)

fun Context.getImageResolution(path: String): Point? {
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    if (isRestrictedSAFOnlyRoot(path)) {
        BitmapFactory.decodeStream(
            contentResolver.openInputStream(getAndroidSAFUri(path)),
            null,
            options
        )
    } else {
        BitmapFactory.decodeFile(path, options)
    }

    val width = options.outWidth
    val height = options.outHeight
    return if (width > 0 && height > 0) {
        Point(options.outWidth, options.outHeight)
    } else {
        null
    }
}

fun Context.getLatestMediaByDateId(uri: Uri = Files.getContentUri("external")): Long {
    val projection = arrayOf(
        BaseColumns._ID
    )
    try {
        val cursor = queryCursorDesc(uri, projection, Images.ImageColumns.DATE_TAKEN, 1)
        cursor?.use {
            if (cursor.moveToFirst()) {
                return cursor.getLongValue(BaseColumns._ID)
            }
        }
    } catch (ignored: Exception) {
    }
    return 0
}

fun Context.getLatestMediaId(uri: Uri = Files.getContentUri("external")): Long {
    val projection = arrayOf(
        BaseColumns._ID
    )
    try {
        val cursor = queryCursorDesc(uri, projection, BaseColumns._ID, 1)
        cursor?.use {
            if (cursor.moveToFirst()) {
                return cursor.getLongValue(BaseColumns._ID)
            }
        }
    } catch (ignored: Exception) {
    }
    return 0
}

private fun getMediaContentUri(owner: Context, path: String): Uri? {
    val uri = when {
        path.isImageFast() -> Images.Media.EXTERNAL_CONTENT_URI
        path.isVideoFast() -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        else -> Files.getContentUri("external")
    }

    return getMediaContent(owner, path, uri)
}

fun getPermissionString(id: Int) = when (id) {
    PERMISSION_READ_STORAGE -> Manifest.permission.READ_EXTERNAL_STORAGE
    PERMISSION_WRITE_STORAGE -> Manifest.permission.WRITE_EXTERNAL_STORAGE
    PERMISSION_CAMERA -> Manifest.permission.CAMERA
    PERMISSION_RECORD_AUDIO -> Manifest.permission.RECORD_AUDIO
    PERMISSION_READ_CONTACTS -> Manifest.permission.READ_CONTACTS
    PERMISSION_WRITE_CONTACTS -> Manifest.permission.WRITE_CONTACTS
    PERMISSION_READ_CALENDAR -> Manifest.permission.READ_CALENDAR
    PERMISSION_WRITE_CALENDAR -> Manifest.permission.WRITE_CALENDAR
    PERMISSION_CALL_PHONE -> Manifest.permission.CALL_PHONE
    PERMISSION_READ_CALL_LOG -> Manifest.permission.READ_CALL_LOG
    PERMISSION_WRITE_CALL_LOG -> Manifest.permission.WRITE_CALL_LOG
    PERMISSION_GET_ACCOUNTS -> Manifest.permission.GET_ACCOUNTS
    PERMISSION_READ_SMS -> Manifest.permission.READ_SMS
    PERMISSION_SEND_SMS -> Manifest.permission.SEND_SMS
    PERMISSION_READ_PHONE_STATE -> Manifest.permission.READ_PHONE_STATE
    PERMISSION_MEDIA_LOCATION -> if (IsQPlusUseCase()) Manifest.permission.ACCESS_MEDIA_LOCATION else ""
    else -> ""
}

fun Context.getPicturesDirectoryPath(fullPath: String): String {
    val basePath = fullPath.getBasePath(this)
    return File(basePath, Environment.DIRECTORY_PICTURES).absolutePath
}

private fun getSAFOnlyDirs(owner: Context): List<String> {
    return DIRS_ACCESSIBLE_ONLY_WITH_SAF.map { "${owner.internalStoragePath}$it" } +
            DIRS_ACCESSIBLE_ONLY_WITH_SAF.map { "${owner.baseConfig.sdCardPath}$it" }
}

private fun getSAFStorageId(owner: Context, fullPath: String): String {
    return if (fullPath.startsWith('/')) {
        when {
            fullPath.startsWith(owner.internalStoragePath) -> "primary"
            else -> fullPath.substringAfter("/storage/", "").substringBefore('/')
        }
    } else {
        fullPath.substringBefore(':', "").substringAfterLast('/')
    }
}

private fun getStorageRootIdForAndroidDir(owner: Context, path: String) =
    owner.getAndroidTreeUri(path).removeSuffix(
        if (isAndroidDataDir(path)
        ) "%3AAndroid%2Fdata" else "%3AAndroid%2Fobb"
    ).substringAfterLast('/').trimEnd('/')

fun Context.getUriMimeType(path: String, newUri: Uri): String {
    var mimeType = path.getMimeType()
    if (mimeType.isEmpty()) {
        mimeType = getMimeTypeFromUri(this, newUri)
    }
    return mimeType
}

private fun getMimeTypeFromUri(owner: Context, uri: Uri): String {
    var mimetype = uri.path?.getMimeType() ?: ""
    if (mimetype.isEmpty()) {
        mimetype = owner.contentResolver.getType(uri) ?: ""
    }
    return mimetype
}

@SuppressLint("Recycle")
fun Context.getVideoResolution(path: String): Point? {
    var point = try {
        val retriever = MediaMetadataRetriever()
        if (isRestrictedSAFOnlyRoot(path)) {
            retriever.setDataSource(this, getAndroidSAFUri(path))
        } else {
            retriever.setDataSource(path)
        }

        val width =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)!!.toInt()
        val height =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)!!.toInt()
        Point(width, height)
    } catch (ignored: Exception) {
        null
    }

    if (point == null && path.startsWith("content://", true)) {
        try {
            val fd = contentResolver.openFileDescriptor(Uri.parse(path), "r")?.fileDescriptor
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(fd)
            val width =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)!!.toInt()
            val height =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)!!
                    .toInt()
            point = Point(width, height)
        } catch (ignored: Exception) {
        }
    }

    return point
}

fun Context.humanizePath(path: String): String {
    val trimmedPath = path.trimEnd('/')
    return when (val basePath = path.getBasePath(this)) {
        "/" -> "${getHumanReadablePath(this, basePath)}$trimmedPath"
        else -> trimmedPath.replaceFirst(basePath, getHumanReadablePath(this, basePath))
    }
}

val Context.config: Config get() = Config.newInstance(applicationContext)

val Context.internalStoragePath: String get() = baseConfig.internalStoragePath

fun Context.isAccessibleWithSAFSdk30(path: String): Boolean {
    if (path.startsWith(recycleBinPath) || isExternalStorageManager()) {
        return false
    }

    val level = getFirstParentLevel(path)
    val firstParentDir = path.getFirstParentDirName(this, level)
    val firstParentPath = path.getFirstParentPath(this, level)

    val isValidName = firstParentDir != null
    val isDirectory = File(firstParentPath).isDirectory
    val isAnAccessibleDirectory =
        listOf("Download", "Android").all { !firstParentDir.equals(it, true) }
    return IsRPlusUseCase() && isValidName && isDirectory && isAnAccessibleDirectory
}

fun Context.isAStorageRootFolder(path: String): Boolean {
    val trimmed = path.trimEnd('/')
    return trimmed.isEmpty() || trimmed.equals(internalStoragePath, true) || trimmed.equals(
        baseConfig.sdCardPath,
        true
    ) || trimmed.equals(baseConfig.otgPath, true)
}

fun Context.isBlackAndWhiteTheme() =
    baseConfig.textColor == Color.WHITE && baseConfig.primaryColor == Color.BLACK && baseConfig.backgroundColor == Color.BLACK

private fun isInAndroidDir(owner: Context, path: String): Boolean {
    if (path.startsWith(owner.recycleBinPath)) {
        return false
    }
    val firstParentDir = path.getFirstParentDirName(owner, 0)
    return firstParentDir.equals("Android", true)
}

fun Context.isPathOnInternalStorage(path: String) =
    internalStoragePath.isNotEmpty() && path.startsWith(internalStoragePath)

fun Context.isSDCardSetAsDefaultStorage() =
    baseConfig.sdCardPath.isNotEmpty() && Environment.getExternalStorageDirectory().absolutePath.equals(
        baseConfig.sdCardPath,
        true
    )

fun Context.isWhiteTheme() =
    baseConfig.textColor == DARK_GREY && baseConfig.primaryColor == Color.WHITE && baseConfig.backgroundColor == Color.WHITE

val Context.widgetsDB: WidgetsDao
    get() = GalleryDatabase.getInstance(applicationContext).WidgetsDao()

val Context.mediaDB: MediumDao get() = GalleryDatabase.getInstance(applicationContext).MediumDao()

val Context.navigationBarHeight: Int
    get() = if ((usableScreenSize(this).y < realScreenSize.y) && navigationBarSize.y != usableScreenSize(
            this
        ).y
    ) navigationBarSize.y else 0

val Context.navigationBarRight: Boolean
    get() = usableScreenSize(this).x < realScreenSize.x && usableScreenSize(
        this
    ).x > usableScreenSize(this).y

val Context.navigationBarSize: Point
    get() = when {
        navigationBarRight -> Point(newNavigationBarHeight(this), usableScreenSize(this).y)
        (usableScreenSize(this).y < realScreenSize.y) -> Point(
            usableScreenSize(this).x,
            newNavigationBarHeight(this)
        )
        else -> Point()
    }

@SuppressLint("DiscouragedApi", "InternalInsetResource")
private fun newNavigationBarHeight(owner: Context): Int {
    var navigationBarHeight = 0
    val resourceId = owner.resources.getIdentifier("navigation_bar_height", "dimen", "android")
    if (resourceId > 0) {
        navigationBarHeight = owner.resources.getDimensionPixelSize(resourceId)
    }
    return navigationBarHeight
}

fun Context.isInDownloadDir(path: String): Boolean {
    if (path.startsWith(recycleBinPath)) {
        return false
    }
    val firstParentDir = path.getFirstParentDirName(this, 0)
    return firstParentDir.equals("Download", true)
}

fun Context.isInSubFolderInDownloadDir(path: String): Boolean {
    if (path.startsWith(recycleBinPath)) {
        return false
    }
    val firstParentDir = path.getFirstParentDirName(this, 1)
    return if (firstParentDir == null) {
        false
    } else {
        val startsWithDownloadDir =
            firstParentDir.startsWith("Download", true)
        val hasAtLeast1PathSegment = firstParentDir.split("/").filter { it.isNotEmpty() }.size > 1
        val firstParentPath = path.getFirstParentPath(this, 1)
        startsWithDownloadDir && hasAtLeast1PathSegment && File(firstParentPath).isDirectory
    }
}

fun Context.isRestrictedSAFOnlyRoot(path: String): Boolean {
    return IsRPlusUseCase() && getSAFOnlyDirs(this).any { "${path.trimEnd('/')}/".startsWith(it) }
}

fun Context.isRestrictedWithSAFSdk30(path: String): Boolean {
    if (path.startsWith(recycleBinPath) || isExternalStorageManager()) {
        return false
    }

    val level = getFirstParentLevel(path)
    val firstParentDir = path.getFirstParentDirName(this, level)
    val firstParentPath = path.getFirstParentPath(this, level)

    val isInvalidName = firstParentDir == null
    val isDirectory = File(firstParentPath).isDirectory
    val isARestrictedDirectory =
        listOf("Download", "Android").any {
            firstParentDir.equals(
                it,
                true
            )
        }
    return IsRPlusUseCase() && (isInvalidName || (isDirectory && isARestrictedDirectory))
}

fun Context.movePinnedDirectoriesToFront(dirs: ArrayList<Directory>): ArrayList<Directory> {
    val foundFolders = ArrayList<Directory>()
    val pinnedFolders = config.pinnedFolders

    dirs.forEach {
        if (pinnedFolders.contains(it.path)) {
            foundFolders.add(it)
        }
    }

    dirs.removeAll(foundFolders)
    dirs.addAll(0, foundFolders)
    if (config.tempFolderPath.isNotEmpty()) {
        val newFolder = dirs.firstOrNull { it.path == config.tempFolderPath }
        if (newFolder != null) {
            dirs.remove(newFolder)
            dirs.add(0, newFolder)
        }
    }

    if (config.showRecycleBinAtFolders && config.showRecycleBinLast) {
        val binIndex = dirs.indexOfFirst { it.isRecycleBin() }
        if (binIndex != -1) {
            val bin = dirs.removeAt(binIndex)
            dirs.add(bin)
        }
    }
    return dirs
}

fun Context.getSomeDocumentFile(path: String) = getFastDocumentFile(path) ?: getDocumentFile(path)

@Suppress("UNCHECKED_CAST")
fun Context.getSortedDirectories(source: ArrayList<Directory>): ArrayList<Directory> {
    val sorting = config.directorySorting
    val dirs = source.clone() as ArrayList<Directory>

    if (sorting and SORT_BY_RANDOM != 0) {
        dirs.shuffle()
        return movePinnedDirectoriesToFront(dirs)
    } else if (sorting and SORT_BY_CUSTOM != 0) {
        val newDirsOrdered = ArrayList<Directory>()
        config.customFoldersOrder.split("|||").forEach { path ->
            val index = dirs.indexOfFirst { it.path == path }
            if (index != -1) {
                val dir = dirs.removeAt(index)
                newDirsOrdered.add(dir)
            }
        }

        dirs.mapTo(newDirsOrdered) { it }
        return newDirsOrdered
    }

    dirs.sortWith { o1, o2 ->
        o1 as Directory
        o2 as Directory

        var result = when {
            sorting and SORT_BY_NAME != 0 -> {
                if (o1.sortValue.isEmpty()) {
                    o1.sortValue = o1.name.lowercase(Locale.getDefault())
                }

                if (o2.sortValue.isEmpty()) {
                    o2.sortValue = o2.name.lowercase(Locale.getDefault())
                }

                if (sorting and SORT_USE_NUMERIC_VALUE != 0) {
                    AlphanumericComparator().compare(
                        o1.sortValue.normalizeString().lowercase(Locale.ROOT),
                        o2.sortValue.normalizeString().lowercase(Locale.getDefault())
                    )
                } else {
                    o1.sortValue.normalizeString().lowercase(Locale.ROOT)
                        .compareTo(o2.sortValue.normalizeString().lowercase(Locale.ROOT))
                }
            }
            sorting and SORT_BY_PATH != 0 -> {
                if (o1.sortValue.isEmpty()) {
                    o1.sortValue = o1.path.lowercase(Locale.ROOT)
                }

                if (o2.sortValue.isEmpty()) {
                    o2.sortValue = o2.path.lowercase(Locale.ROOT)
                }

                if (sorting and SORT_USE_NUMERIC_VALUE != 0) {
                    AlphanumericComparator().compare(
                        o1.sortValue.lowercase(Locale.getDefault()),
                        o2.sortValue.lowercase(Locale.getDefault())
                    )
                } else {
                    o1.sortValue.lowercase(Locale.getDefault())
                        .compareTo(o2.sortValue.lowercase(Locale.getDefault()))
                }
            }
            sorting and SORT_BY_PATH != 0 -> AlphanumericComparator().compare(
                o1.sortValue.lowercase(Locale.getDefault()),
                o2.sortValue.lowercase(Locale.getDefault())
            )
            sorting and SORT_BY_SIZE != 0 -> (o1.sortValue.toLongOrNull()
                ?: 0).compareTo(o2.sortValue.toLongOrNull() ?: 0)
            sorting and SORT_BY_DATE_MODIFIED != 0 -> (o1.sortValue.toLongOrNull() ?: 0).compareTo(
                o2.sortValue.toLongOrNull() ?: 0
            )
            else -> (o1.sortValue.toLongOrNull() ?: 0).compareTo(o2.sortValue.toLongOrNull() ?: 0)
        }

        if (sorting and SORT_DESCENDING != 0) {
            result *= -1
        }
        result
    }

    return movePinnedDirectoriesToFront(dirs)
}

fun Context.getStorageDirectories(): Array<String> {
    val paths = java.util.HashSet<String>()
    val rawExternalStorage = System.getenv("EXTERNAL_STORAGE")
    val rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE")
    val rawEmulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET")
    if (TextUtils.isEmpty(rawEmulatedStorageTarget)) {
        if (IsMarshmallowPlusUseCase()) {
            getExternalFilesDirs(null).filterNotNull().map { it.absolutePath }
                .mapTo(paths) { it.substring(0, it.indexOf("Android/data")) }
        } else {
            if (TextUtils.isEmpty(rawExternalStorage)) {
                paths.addAll(physicalPaths)
            } else {
                paths.add(rawExternalStorage!!)
            }
        }
    } else {
        val path = Environment.getExternalStorageDirectory().absolutePath
        val folders = Pattern.compile("/").split(path)
        val lastFolder = folders[folders.size - 1]
        var isDigit = false
        try {
            Integer.valueOf(lastFolder)
            isDigit = true
        } catch (ignored: NumberFormatException) {
        }

        val rawUserId = if (isDigit) lastFolder else ""
        if (TextUtils.isEmpty(rawUserId)) {
            paths.add(rawEmulatedStorageTarget!!)
        } else {
            paths.add(rawEmulatedStorageTarget + File.separator + rawUserId)
        }
    }

    if (!TextUtils.isEmpty(rawSecondaryStoragesStr)) {
        val rawSecondaryStorages = rawSecondaryStoragesStr!!.split(File.pathSeparator.toRegex())
            .dropLastWhile(String::isEmpty).toTypedArray()
        Collections.addAll(paths, *rawSecondaryStorages)
    }
    return paths.map { it.trimEnd('/') }.toTypedArray()
}

fun Context.getDirsToShow(
    dirs: ArrayList<Directory>,
    allDirs: ArrayList<Directory>,
    currentPathPrefix: String
): ArrayList<Directory> {
    return if (config.groupDirectSubfolders) {
        dirs.forEach {
            it.subfoldersCount = 0
            it.subfoldersMediaCount = it.mediaCnt
        }

        val parentDirs = getDirectParentSubfolders(this, dirs, currentPathPrefix)

        // update the count of sub-folders
        for (child in dirs) {
            var longestSharedPath = ""
            for (parentDir in parentDirs) {
                if (parentDir.path == child.path) {
                    longestSharedPath = child.path
                    continue
                }

                if (child.path.startsWith(
                        parentDir.path,
                        true
                    ) && parentDir.path.length > longestSharedPath.length
                ) {
                    longestSharedPath = parentDir.path
                }
            }

            // make sure we count only the proper direct subfolders, grouped the same way as on the main screen
            parentDirs.firstOrNull { it.path == longestSharedPath }?.apply {
                if (path.equals(child.path, true) || path.equals(
                        File(child.path).parent,
                        true
                    ) || dirs.any { it.path.equals(File(child.path).parent, true) }
                ) {
                    if (child.containsMediaFilesDirectly) {
                        subfoldersCount++
                    }

                    if (path != child.path) {
                        subfoldersMediaCount += child.mediaCnt
                    }
                }
            }
        }

        // show the current folder as an available option too, not just subfolders
        if (currentPathPrefix.isNotEmpty()) {
            val currentFolder =
                allDirs.firstOrNull { parentDirs.firstOrNull { it.path == currentPathPrefix } == null && it.path == currentPathPrefix }
            currentFolder?.apply {
                subfoldersCount = 1
                parentDirs.add(this)
            }
        }

        getSortedDirectories(parentDirs)
    } else {
        dirs.forEach { it.subfoldersMediaCount = it.mediaCnt }
        dirs
    }
}

private fun getDirectParentSubfolders(
    owner: Context,
    dirs: ArrayList<Directory>,
    currentPathPrefix: String
): ArrayList<Directory> {
    val folders = dirs.map { it.path }.sorted().toMutableSet() as HashSet<String>
    val currentPaths = LinkedHashSet<String>()
    val foldersWithoutMediaFiles = ArrayList<String>()
    var newDirId = 1000L

    for (path in folders) {
        if (path == RECYCLE_BIN || path == FAVORITES) {
            continue
        }

        if (currentPathPrefix.isNotEmpty()) {
            if (!path.startsWith(currentPathPrefix, true)) {
                continue
            }

            if (!File(path).parent.equals(currentPathPrefix, true)) {
                continue
            }
        }

        if (currentPathPrefix.isNotEmpty() && path == currentPathPrefix || File(path).parent.equals(
                currentPathPrefix,
                true
            )
        ) {
            currentPaths.add(path)
        } else if (folders.any {
                !it.equals(path, true) && (File(path).parent.equals(
                    it,
                    true
                ) || File(it).parent.equals(File(path).parent, true))
            }) {
            // if we have folders like
            // /storage/emulated/0/Pictures/Images and
            // /storage/emulated/0/Pictures/Screenshots,
            // but /storage/emulated/0/Pictures is empty, still Pictures with the first folders thumbnails and proper other info
            val parent = File(path).parent
            if (parent != null && !folders.contains(parent) && dirs.none { it.path == parent }) {
                currentPaths.add(parent)
                val isSortingAscending = isSortingAscending(owner.config.sorting)
                val subDirs = dirs.filter {
                    File(it.path).parent.equals(
                        File(path).parent,
                        true
                    )
                } as ArrayList<Directory>
                if (subDirs.isNotEmpty()) {
                    val lastModified = if (isSortingAscending) {
                        subDirs.minByOrNull { it.modified }?.modified
                    } else {
                        subDirs.maxByOrNull { it.modified }?.modified
                    } ?: 0

                    val dateTaken = if (isSortingAscending) {
                        subDirs.minByOrNull { it.taken }?.taken
                    } else {
                        subDirs.maxByOrNull { it.taken }?.taken
                    } ?: 0

                    var mediaTypes = 0
                    subDirs.forEach {
                        mediaTypes = mediaTypes or it.types
                    }

                    val directory = Directory(
                        newDirId++,
                        parent,
                        subDirs.first().tmb,
                        owner.getFolderNameFromPath(parent),
                        subDirs.sumOf { it.mediaCnt },
                        lastModified,
                        dateTaken,
                        subDirs.sumByLong { it.size },
                        getPathLocation(owner, parent),
                        mediaTypes,
                        ""
                    )

                    directory.containsMediaFilesDirectly = false
                    dirs.add(directory)
                    currentPaths.add(parent)
                    foldersWithoutMediaFiles.add(parent)
                }
            }
        } else {
            currentPaths.add(path)
        }
    }

    var areDirectSubfoldersAvailable = false
    currentPaths.forEach {
        val path = it
        currentPaths.forEach {
            if (!foldersWithoutMediaFiles.contains(it) && !it.equals(
                    path,
                    true
                ) && File(it).parent?.equals(path, true) == true
            ) {
                areDirectSubfoldersAvailable = true
            }
        }
    }

    if (currentPathPrefix.isEmpty() && folders.contains(RECYCLE_BIN)) {
        currentPaths.add(RECYCLE_BIN)
    }

    if (currentPathPrefix.isEmpty() && folders.contains(FAVORITES)) {
        currentPaths.add(FAVORITES)
    }

    if (folders.size == currentPaths.size) {
        return dirs.filter { currentPaths.contains(it.path) } as ArrayList<Directory>
    }

    folders.clear()
    folders.addAll(currentPaths)

    val dirsToShow = dirs.filter { folders.contains(it.path) } as ArrayList<Directory>
    return if (areDirectSubfoldersAvailable) {
        getDirectParentSubfolders(owner, dirsToShow, currentPathPrefix)
    } else {
        dirsToShow
    }
}

fun Context.getFastDocumentFile(path: String): DocumentFile? {
    if (IsPathOnOtgUseCase(this, path)) {
        return getOTGFastDocumentFile(this, path)
    }

    if (baseConfig.sdCardPath.isEmpty()) {
        return null
    }

    val relativePath = Uri.encode(path.substring(baseConfig.sdCardPath.length).trim('/'))
    val externalPathPart =
        baseConfig.sdCardPath.split("/").lastOrNull(String::isNotEmpty)?.trim('/') ?: return null
    val fullUri = "${baseConfig.sdTreeUri}/document/$externalPathPart%3A$relativePath"
    return DocumentFile.fromSingleUri(this, Uri.parse(fullUri))
}

fun Context.getNoMediaFoldersSync(): ArrayList<String> {
    val folders = ArrayList<String>()

    val uri = Files.getContentUri("external")
    val projection = arrayOf(Files.FileColumns.DATA)
    val selection = "${Files.FileColumns.MEDIA_TYPE} = ? AND ${Files.FileColumns.TITLE} LIKE ?"
    val selectionArgs = arrayOf(Files.FileColumns.MEDIA_TYPE_NONE.toString(), "%$NOMEDIA%")
    val sortOrder = "${Files.FileColumns.DATE_MODIFIED} DESC"
    val otgPath = config.otgPath

    var cursor: Cursor? = null
    try {
        cursor = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)
        if (cursor?.moveToFirst() == true) {
            do {
                val path = cursor.getStringValue(Files.FileColumns.DATA) ?: continue
                val noMediaFile = File(path)
                if (getDoesFilePathExist(
                        noMediaFile.absolutePath,
                        otgPath
                    ) && noMediaFile.name == NOMEDIA
                ) {
                    noMediaFile.parent?.let { folders.add(it) }
                }
            } while (cursor.moveToNext())
        }
    } catch (ignored: Exception) {
    } finally {
        cursor?.close()
    }

    return folders
}

fun Context.rescanFolderMedia(path: String) {
    RunOnBackgroundThreadUseCase {
        rescanFolderMediaSync(this, path)
    }
}

private fun rescanFolderMediaSync(owner: Context, path: String) {
    owner.getCachedMedia(path) { cached ->
        GetMediaAsynctask(
            owner.applicationContext,
            path,
            isPickImage = false,
            isPickVideo = false,
            showAll = false
        ) { newMedia ->
            RunOnBackgroundThreadUseCase {
                val media = newMedia.filterIsInstance<Medium>() as ArrayList<Medium>
                try {
                    owner.mediaDB.insertAll(media)

                    cached.forEach { thumbnailItem ->
                        if (!newMedia.contains(thumbnailItem)) {
                            val mediumPath = (thumbnailItem as? Medium)?.path
                            if (mediumPath != null) {
                                owner.deleteDBPath(mediumPath)
                            }
                        }
                    }
                } catch (ignored: Exception) {
                }
            }
        }.execute()
    }
}

fun Context.checkAppendingHidden(
    path: String,
    hidden: String,
    includedFolders: MutableSet<String>,
    noMediaFolders: ArrayList<String>
): String {
    val dirName = getFolderNameFromPath(path)
    val folderNoMediaStatuses = HashMap<String, Boolean>()
    noMediaFolders.forEach { folder ->
        folderNoMediaStatuses["$folder/$NOMEDIA"] = true
    }

    return if (path.doesThisOrParentHaveNoMedia(
            folderNoMediaStatuses,
            null
        ) && !path.isThisOrParentIncluded(includedFolders)
    ) {
        "$dirName $hidden"
    } else {
        dirName
    }
}

fun Context.getFolderNameFromPath(path: String): String {
    return when (path) {
        internalStoragePath -> getString(R.string.internal)
        baseConfig.sdCardPath -> getString(R.string.sd_card)
        baseConfig.otgPath -> getString(R.string.usb)
        FAVORITES -> getString(R.string.favorites)
        RECYCLE_BIN -> getString(R.string.recycle_bin)
        else -> path.getFilenameFromPath()
    }
}

fun Context.loadImage(
    type: Int,
    path: String,
    target: MySquareImageView,
    horizontalScroll: Boolean,
    animateGifs: Boolean,
    cropThumbnails: Boolean,
    roundCorners: Int,
    signature: ObjectKey,
    skipMemoryCacheAtPaths: ArrayList<String>? = null
) {
    target.isHorizontalScrolling = horizontalScroll
    if (type == MediaType.Image.id || type == MediaType.Video.id || type == MediaType.Raw.id || type == MediaType.Portrait.id) {
        if (type == MediaType.Image.id && IsPngUseCase(path)) {
            loadPng(
                this,
                path,
                target,
                cropThumbnails,
                roundCorners,
                signature,
                skipMemoryCacheAtPaths
            )
        } else {
            loadJpg(path, target, cropThumbnails, roundCorners, signature, skipMemoryCacheAtPaths)
        }
    } else if (type == MediaType.Gif.id) {
        if (!animateGifs) {
            loadStaticGIF(
                this,
                path,
                target,
                cropThumbnails,
                roundCorners,
                signature,
                skipMemoryCacheAtPaths
            )
            return
        }

        try {
            val gifDrawable = GifDrawable(path)
            target.setImageDrawable(gifDrawable)
            gifDrawable.start()

            target.scaleType =
                if (cropThumbnails) ImageView.ScaleType.CENTER_CROP else ImageView.ScaleType.FIT_CENTER
        } catch (e: Exception) {
            loadStaticGIF(
                this,
                path,
                target,
                cropThumbnails,
                roundCorners,
                signature,
                skipMemoryCacheAtPaths
            )
        } catch (e: OutOfMemoryError) {
            loadStaticGIF(
                this,
                path,
                target,
                cropThumbnails,
                roundCorners,
                signature,
                skipMemoryCacheAtPaths
            )
        }
    } else if (type == MediaType.Svg.id) {
        loadSVG(this, path, target, cropThumbnails, roundCorners, signature)
    }
}

fun Context.addTempFolderIfNeeded(dirs: ArrayList<Directory>): ArrayList<Directory> {
    val tempFolderPath = config.tempFolderPath
    return if (tempFolderPath.isNotEmpty()) {
        val directories = ArrayList<Directory>()
        val newFolder = Directory(
            null,
            tempFolderPath,
            "",
            tempFolderPath.getFilenameFromPath(),
            0,
            0,
            0,
            0L,
            getPathLocation(this, tempFolderPath),
            0,
            ""
        )
        directories.add(newFolder)
        directories.addAll(dirs)
        directories
    } else {
        dirs
    }
}

private fun getPathLocation(owner: Context, path: String): Int {
    return when {
        IsPathOnSdUseCase(owner, path) -> FileLocation.SdCard.id
        IsPathOnOtgUseCase(owner, path) -> FileLocation.Otg.id
        else -> FileLocation.Internal.id
    }
}

@SuppressLint("CheckResult")
private fun loadPng(
    owner: Context,
    path: String,
    target: MySquareImageView,
    cropThumbnails: Boolean,
    roundCorners: Int,
    signature: ObjectKey,
    skipMemoryCacheAtPaths: ArrayList<String>? = null
) {
    val options = RequestOptions()
        .signature(signature)
        .skipMemoryCache(skipMemoryCacheAtPaths?.contains(path) == true)
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        .priority(Priority.LOW)
        .format(DecodeFormat.PREFER_ARGB_8888)

    if (cropThumbnails) options.centerCrop() else options.fitCenter()
    var builder = Glide.with(owner.applicationContext)
        .asBitmap()
        .load(path)
        .apply(options)
        .listener(object : RequestListener<Bitmap> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                targetBitmap: Target<Bitmap>?,
                isFirstResource: Boolean
            ): Boolean {
                tryLoadingWithPicasso(owner, path, target, cropThumbnails, roundCorners, signature)
                return true
            }

            override fun onResourceReady(
                resource: Bitmap?,
                model: Any?,
                targetBitmap: Target<Bitmap>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                return false
            }
        })

    if (roundCorners != ROUNDED_CORNERS_NONE) {
        val cornerSize =
            if (roundCorners == ROUNDED_CORNERS_SMALL) R.dimen.rounded_corner_radius_small else R.dimen.rounded_corner_radius_big
        val cornerRadius = owner.resources.getDimension(cornerSize).toInt()
        builder = builder.transform(CenterCrop(), RoundedCorners(cornerRadius))
    }

    builder.into(target)
}

@SuppressLint("CheckResult")
fun Context.loadJpg(
    path: String,
    target: MySquareImageView,
    cropThumbnails: Boolean,
    roundCorners: Int,
    signature: ObjectKey,
    skipMemoryCacheAtPaths: ArrayList<String>? = null
) {
    val options = RequestOptions()
        .signature(signature)
        .skipMemoryCache(skipMemoryCacheAtPaths?.contains(path) == true)
        .priority(Priority.LOW)
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)

    if (cropThumbnails) options.centerCrop() else options.fitCenter()
    var builder = Glide.with(applicationContext)
        .load(path)
        .apply(options)
        .transition(DrawableTransitionOptions.withCrossFade())

    if (roundCorners != ROUNDED_CORNERS_NONE) {
        val cornerSize =
            if (roundCorners == ROUNDED_CORNERS_SMALL) R.dimen.rounded_corner_radius_small else R.dimen.rounded_corner_radius_big
        val cornerRadius = resources.getDimension(cornerSize).toInt()
        builder = builder.transform(CenterCrop(), RoundedCorners(cornerRadius))
    }

    builder.into(target)
}

@SuppressLint("CheckResult")
private fun loadStaticGIF(
    owner: Context,
    path: String,
    target: MySquareImageView,
    cropThumbnails: Boolean,
    roundCorners: Int,
    signature: ObjectKey,
    skipMemoryCacheAtPaths: ArrayList<String>? = null
) {
    val options = RequestOptions()
        .signature(signature)
        .skipMemoryCache(skipMemoryCacheAtPaths?.contains(path) == true)
        .priority(Priority.LOW)
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)

    if (cropThumbnails) options.centerCrop() else options.fitCenter()
    var builder = Glide.with(owner.applicationContext)
        .asBitmap() // make sure the GIF wont animate
        .load(path)
        .apply(options)

    if (roundCorners != ROUNDED_CORNERS_NONE) {
        val cornerSize =
            if (roundCorners == ROUNDED_CORNERS_SMALL) R.dimen.rounded_corner_radius_small else R.dimen.rounded_corner_radius_big
        val cornerRadius = owner.resources.getDimension(cornerSize).toInt()
        builder = builder.transform(CenterCrop(), RoundedCorners(cornerRadius))
    }

    builder.into(target)
}

private fun loadSVG(
    owner: Context,
    path: String,
    target: MySquareImageView,
    cropThumbnails: Boolean,
    roundCorners: Int,
    signature: ObjectKey
) {
    target.scaleType =
        if (cropThumbnails) ImageView.ScaleType.CENTER_CROP else ImageView.ScaleType.FIT_CENTER

    val options = RequestOptions().signature(signature)
    var builder = Glide.with(owner.applicationContext)
        .`as`(PictureDrawable::class.java)
        .listener(SvgSoftwareLayerSetter())
        .load(path)
        .apply(options)
        .transition(DrawableTransitionOptions.withCrossFade())

    if (roundCorners != ROUNDED_CORNERS_NONE) {
        val cornerSize =
            if (roundCorners == ROUNDED_CORNERS_SMALL) R.dimen.rounded_corner_radius_small else R.dimen.rounded_corner_radius_big
        val cornerRadius = owner.resources.getDimension(cornerSize).toInt()
        builder = builder.transform(CenterCrop(), RoundedCorners(cornerRadius))
    }

    builder.into(target)
}

// intended mostly for Android 11 issues, that fail loading PNG files bigger than 10 MB
private fun tryLoadingWithPicasso(
    owner: Context,
    path: String,
    view: MySquareImageView,
    cropThumbnails: Boolean,
    roundCorners: Int,
    signature: ObjectKey
) {
    var pathToLoad = "file://$path"
    pathToLoad = pathToLoad.replace("%", "%25").replace("#", "%23")


    var builder = Picasso.get()
        .load(pathToLoad)
        .stableKey(signature.toString())

    builder = if (cropThumbnails) {
        builder.centerCrop().fit()
    } else {
        builder.centerInside()
    }

    if (roundCorners != ROUNDED_CORNERS_NONE) {
        val cornerSize =
            if (roundCorners == ROUNDED_CORNERS_SMALL) R.dimen.rounded_corner_radius_small else R.dimen.rounded_corner_radius_big
        val cornerRadius = owner.resources.getDimension(cornerSize).toInt()
        builder = builder.transform(PicassoRoundedCornersTransformation(cornerRadius.toFloat()))
    }

    builder.into(view)
}

fun Context.getCachedDirectories(
    getVideosOnly: Boolean = false,
    getImagesOnly: Boolean = false,
    forceShowHidden: Boolean = false,
    callback: (ArrayList<Directory>) -> Unit
) {
    RunOnBackgroundThreadUseCase {

        try {
            Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE)
        } catch (ignored: Exception) {
        }

        val directories = try {
            GalleryDatabase.getInstance(applicationContext).DirectoryDao()
                .getAll() as ArrayList<Directory>
        } catch (e: Exception) {
            ArrayList()
        }

        if (!config.showRecycleBinAtFolders) {
            directories.removeAll { it.isRecycleBin() }
        }

        val shouldShowHidden = config.shouldShowHidden || forceShowHidden
        val excludedPaths = if (config.temporarilyShowExcluded) {
            HashSet()
        } else {
            config.excludedFolders
        }

        val includedPaths = config.includedFolders

        val folderNoMediaStatuses = HashMap<String, Boolean>()
        val noMediaFolders = getNoMediaFoldersSync()
        noMediaFolders.forEach { folder ->
            folderNoMediaStatuses["$folder/$NOMEDIA"] = true
        }

        var filteredDirectories = directories.filter {
            it.path.shouldFolderBeVisible(
                excludedPaths,
                includedPaths,
                shouldShowHidden,
                folderNoMediaStatuses
            ) { path, hasNoMedia ->
                folderNoMediaStatuses[path] = hasNoMedia
            }
        } as ArrayList<Directory>
        val filterMedia = config.filterMedia

        filteredDirectories = (when {
            getVideosOnly -> filteredDirectories.filter { it.types and MediaType.Video.id != 0 }
            getImagesOnly -> filteredDirectories.filter { it.types and MediaType.Image.id != 0 }
            else -> filteredDirectories.filter {
                (filterMedia and MediaType.Image.id != 0 && it.types and MediaType.Image.id != 0) ||
                        (filterMedia and MediaType.Video.id != 0 && it.types and MediaType.Video.id != 0) ||
                        (filterMedia and MediaType.Gif.id != 0 && it.types and MediaType.Gif.id != 0) ||
                        (filterMedia and MediaType.Raw.id != 0 && it.types and MediaType.Raw.id != 0) ||
                        (filterMedia and MediaType.Svg.id != 0 && it.types and MediaType.Svg.id != 0) ||
                        (filterMedia and MediaType.Portrait.id != 0 && it.types and MediaType.Portrait.id != 0)
            }
        }) as ArrayList<Directory>

        if (shouldShowHidden) {
            val hiddenString = resources.getString(R.string.hidden)
            filteredDirectories.forEach {
                val noMediaPath = "${it.path}/$NOMEDIA"
                val hasNoMedia = if (folderNoMediaStatuses.keys.contains(noMediaPath)) {
                    folderNoMediaStatuses[noMediaPath]!!
                } else {
                    it.path.doesThisOrParentHaveNoMedia(folderNoMediaStatuses) { path, hasNoMedia ->
                        val newPath = "$path/$NOMEDIA"
                        folderNoMediaStatuses[newPath] = hasNoMedia
                    }
                }

                it.name = if (hasNoMedia && !it.path.isThisOrParentIncluded(includedPaths)) {
                    "${it.name.removeSuffix(hiddenString).trim()} $hiddenString"
                } else {
                    it.name.removeSuffix(hiddenString).trim()
                }
            }
        }

        val clone = filteredDirectories.clone() as ArrayList<Directory>
        callback(clone.distinctBy { it.path.getDistinctPath() } as ArrayList<Directory>)
        removeInvalidDBDirectories(filteredDirectories)
    }
}

fun Context.getCachedMedia(
    path: String,
    getVideosOnly: Boolean = false,
    getImagesOnly: Boolean = false,
    callback: (ArrayList<ThumbnailItem>) -> Unit
) {
    RunOnBackgroundThreadUseCase {

        val mediaFetcher = MediaFetcher(this)
        val foldersToScan =
            if (path.isEmpty()) mediaFetcher.getFoldersToScan() else arrayListOf(path)
        var media = ArrayList<Medium>()
        if (path == FAVORITES) {
            media.addAll(mediaDB.getFavorites())
        }

        if (path == RECYCLE_BIN) {
            media.addAll(getUpdatedDeletedMedia())
        }

        if (config.filterMedia and MediaType.Portrait.id != 0) {
            val foldersToAdd = ArrayList<String>()
            for (folder in foldersToScan) {
                val allFiles = File(folder).listFiles() ?: continue
                allFiles.filter { it.name.startsWith("img_", true) && it.isDirectory }.forEach {
                    foldersToAdd.add(it.absolutePath)
                }
            }
            foldersToScan.addAll(foldersToAdd)
        }

        val shouldShowHidden = config.shouldShowHidden
        foldersToScan.filter { path.isNotEmpty() || !config.isFolderProtected(it) }.forEach {
            try {
                val currMedia = mediaDB.getMediaFromPath(it)
                media.addAll(currMedia)
            } catch (ignored: Exception) {
            }
        }

        if (!shouldShowHidden) {
            media = media.filter { !it.path.contains("/.") } as ArrayList<Medium>
        }

        val filterMedia = config.filterMedia
        media = (when {
            getVideosOnly -> media.filter { it.type == MediaType.Video.id }
            getImagesOnly -> media.filter { it.type == MediaType.Image.id }
            else -> media.filter {
                (filterMedia and MediaType.Image.id != 0 && it.type == MediaType.Image.id) ||
                        (filterMedia and MediaType.Video.id != 0 && it.type == MediaType.Video.id) ||
                        (filterMedia and MediaType.Gif.id != 0 && it.type == MediaType.Gif.id) ||
                        (filterMedia and MediaType.Raw.id != 0 && it.type == MediaType.Raw.id) ||
                        (filterMedia and MediaType.Svg.id != 0 && it.type == MediaType.Svg.id) ||
                        (filterMedia and MediaType.Portrait.id != 0 && it.type == MediaType.Portrait.id)
            }
        }) as ArrayList<Medium>

        val pathToUse = path.ifEmpty { SHOW_ALL }
        mediaFetcher.sortMedia(media, config.getFolderSorting(pathToUse))
        val grouped = mediaFetcher.groupMedia(media, pathToUse)
        callback(grouped.clone() as ArrayList<ThumbnailItem>)
        val otgPath = config.otgPath

        try {
            val mediaToDelete = ArrayList<Medium>()
            // creating a new thread intentionally, do not reuse the common background thread
            Thread {
                media.filter { !getDoesFilePathExist(it.path, otgPath) }.forEach {
                    if (it.path.startsWith(recycleBinPath)) {
                        deleteDBPath(it.path)
                    } else {
                        mediaToDelete.add(it)
                    }
                }

                if (mediaToDelete.isNotEmpty()) {
                    try {
                        mediaDB.deleteMedia(*mediaToDelete.toTypedArray())

                        mediaToDelete.filter { it.isFavorite }.forEach {
                            GalleryDatabase.getInstance(applicationContext).FavoritesDao()
                                .deleteFavoritePath(it.path)
                        }
                    } catch (ignored: Exception) {
                    }
                }
            }.start()
        } catch (ignored: Exception) {
        }
    }
}

fun Context.removeInvalidDBDirectories(dirs: ArrayList<Directory>? = null) {
    val dirsToCheck =
        dirs ?: GalleryDatabase.getInstance(applicationContext).DirectoryDao().getAll()
    val otgPath = config.otgPath
    dirsToCheck.filter {
        !it.areFavorites() && !it.isRecycleBin() && !getDoesFilePathExist(
            it.path,
            otgPath
        ) && it.path != config.tempFolderPath
    }.forEach {
        try {
            GalleryDatabase.getInstance(applicationContext).DirectoryDao().deleteDirPath(it.path)
        } catch (ignored: Exception) {
        }
    }
}

private fun tryFastDocumentDelete(
    owner: Context,
    path: String,
    allowDeleteFolder: Boolean
): Boolean {
    val document = owner.getFastDocumentFile(path)
    return if (document?.isFile == true || allowDeleteFolder) {
        try {
            DocumentsContract.deleteDocument(owner.contentResolver, document?.uri!!)
        } catch (e: Exception) {
            false
        }
    } else {
        false
    }
}

fun Context.trySAFFileDelete(
    fileDirItem: FileDirItem,
    allowDeleteFolder: Boolean = false,
    callback: ((wasSuccess: Boolean) -> Unit)? = null
) {
    var fileDeleted = tryFastDocumentDelete(this, fileDirItem.path, allowDeleteFolder)
    if (!fileDeleted) {
        val document = getDocumentFile(fileDirItem.path)
        if (document != null && (fileDirItem.isDirectory == document.isDirectory)) {
            try {
                fileDeleted =
                    (document.isFile || allowDeleteFolder) && DocumentsContract.deleteDocument(
                        applicationContext.contentResolver,
                        document.uri
                    )
            } catch (ignored: Exception) {
                baseConfig.sdTreeUri = ""
                baseConfig.sdCardPath = ""
            }
        }
    }

    if (fileDeleted) {
        deleteFromMediaStore(fileDirItem.path)
        callback?.invoke(true)
    }
}

fun Context.updateDBMediaPath(oldPath: String, newPath: String) {
    val newFilename = newPath.getFilenameFromPath()
    val newParentPath = newPath.getParentPath()
    try {
        mediaDB.updateMedium(newFilename, newPath, newParentPath, oldPath)
        GalleryDatabase.getInstance(applicationContext).FavoritesDao()
            .updateFavorite(newFilename, newPath, newParentPath, oldPath)
    } catch (ignored: Exception) {
    }
}

fun Context.updateDBDirectory(directory: Directory) {
    try {
        GalleryDatabase.getInstance(applicationContext).DirectoryDao().updateDirectory(
            directory.path,
            directory.tmb,
            directory.mediaCnt,
            directory.modified,
            directory.taken,
            directory.size,
            directory.types,
            directory.sortValue
        )
    } catch (ignored: Exception) {
    }
}

fun Context.deleteDocumentWithSAFSdk30(
    fileDirItem: FileDirItem,
    allowDeleteFolder: Boolean,
    callback: ((wasSuccess: Boolean) -> Unit)?
) {
    try {
        var fileDeleted = false
        if (fileDirItem.isDirectory.not() || allowDeleteFolder) {
            val fileUri = createDocumentUriUsingFirstParentTreeUri(fileDirItem.path)
            fileDeleted = DocumentsContract.deleteDocument(contentResolver, fileUri)
        }

        if (fileDeleted) {
            deleteFromMediaStore(fileDirItem.path)
            callback?.invoke(true)
        }

    } catch (e: Exception) {
        callback?.invoke(false)

        Timber.e(e)
    }
}

private fun getOTGFastDocumentFile(
    owner: Context,
    path: String,
    otgPathToUse: String? = null
): DocumentFile? {
    if (owner.baseConfig.otgTreeUri.isEmpty()) {
        return null
    }

    val otgPath = otgPathToUse ?: owner.baseConfig.otgPath
    if (owner.baseConfig.otgPartition.isEmpty()) {
        owner.baseConfig.otgPartition =
            owner.baseConfig.otgTreeUri.removeSuffix("%3A").substringAfterLast('/').trimEnd('/')
        owner.updateOTGPathFromPartition()
    }

    val relativePath = Uri.encode(path.substring(otgPath.length).trim('/'))
    val fullUri =
        "${owner.baseConfig.otgTreeUri}/document/${owner.baseConfig.otgPartition}%3A$relativePath"
    return DocumentFile.fromSingleUri(owner, Uri.parse(fullUri))
}

fun Context.getFavoritePaths(): ArrayList<String> {
    return try {
        GalleryDatabase.getInstance(applicationContext).FavoritesDao()
            .getValidFavoritePaths() as ArrayList<String>
    } catch (e: Exception) {
        ArrayList()
    }
}

fun getFavoriteFromPath(path: String) =
    Favorite(null, path, path.getFilenameFromPath(), path.getParentPath())

// Convert paths like /storage/emulated/0/Pictures/Screenshots/first.jpg to content://media/external/images/media/131799
// so that we can refer to the file in the MediaStore.
// If we found no mediastore uri for a given file, do not return its path either to avoid some mismatching
private fun getUrisPathsFromFileDirItems(
    owner: Context,
    fileDirItems: List<FileDirItem>
): Pair<ArrayList<String>, ArrayList<Uri>> {
    val fileUris = ArrayList<Uri>()
    val successfulFilePaths = ArrayList<String>()
    val allIds = getMediaStoreIds(owner)
    val filePaths = fileDirItems.map { it.path }
    filePaths.forEach { path ->
        for ((filePath, mediaStoreId) in allIds) {
            if (filePath.lowercase() == path.lowercase()) {
                val baseUri = getFileUri(filePath)
                val uri = ContentUris.withAppendedId(baseUri, mediaStoreId)
                fileUris.add(uri)
                successfulFilePaths.add(path)
            }
        }
    }

    return Pair(successfulFilePaths, fileUris)
}

val Context.portrait get() = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

fun Context.updateFavorite(path: String, isFavorite: Boolean) {
    try {
        if (isFavorite) {
            GalleryDatabase.getInstance(applicationContext).FavoritesDao()
                .insert(getFavoriteFromPath(path))
        } else {
            GalleryDatabase.getInstance(applicationContext).FavoritesDao().deleteFavoritePath(path)
        }
    } catch (e: Exception) {
        Timber.e(e)
    }
}

// remove the "recycle_bin" from the file path prefix, replace it with real bin path /data/user...
fun Context.getUpdatedDeletedMedia(): ArrayList<Medium> {
    val media = try {
        mediaDB.getDeletedMedia() as ArrayList<Medium>
    } catch (ignored: Exception) {
        ArrayList()
    }

    media.forEach {
        it.path = File(recycleBinPath, it.path.removePrefix(RECYCLE_BIN)).toString()
    }
    return media
}

fun Context.getIsPathDirectory(path: String): Boolean {
    return when {
        isRestrictedSAFOnlyRoot(path) -> getFastAndroidSAFDocument(this, path)?.isDirectory ?: false
        IsPathOnOtgUseCase(this, path) -> getOTGFastDocumentFile(this, path)?.isDirectory ?: false
        else -> File(path).isDirectory
    }
}

fun Context.getResolution(path: String): Point? {
    return if (path.isImageFast() || path.isImageSlow()) {
        getImageResolution(path)
    } else if (path.isVideoFast() || path.isVideoSlow()) {
        getVideoResolution(path)
    } else {
        null
    }
}

fun Context.deleteDBPath(path: String) {
    deleteMediumWithPath(this, path.replaceFirst(recycleBinPath, RECYCLE_BIN))
}

private fun deleteMediumWithPath(owner: Context, path: String) {
    try {
        owner.mediaDB.deleteMediumPath(path)
    } catch (ignored: Exception) {
    }
}

fun Context.updateWidgets() {
    val widgetIDs = AppWidgetManager.getInstance(applicationContext)
        ?.getAppWidgetIds(ComponentName(applicationContext, SmartGalleryWidgetProvider::class.java))
        ?: return
    if (widgetIDs.isNotEmpty()) {
        Intent(applicationContext, SmartGalleryWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIDs)
            sendBroadcast(this)
        }
    }
}

// based on https://github.com/sannies/mp4parser/blob/master/examples/src/main/java/com/google/code/mp4parser/example/PrintStructure.java
fun Context.parseFileChannel(
    path: String,
    fc: FileChannel,
    level: Int,
    start: Long,
    end: Long,
    callback: () -> Unit
) {
    val fileChannelContainers = arrayListOf("moov", "trak", "mdia", "minf", "udta", "stbl")
    try {
        var iteration = 0
        var currEnd = end
        fc.position(start)
        if (currEnd <= 0) {
            currEnd = start + fc.size()
        }

        while (currEnd - fc.position() > 8) {
            // just a check to avoid deadloop at some videos
            if (iteration++ > 50) {
                return
            }

            val begin = fc.position()
            val byteBuffer = ByteBuffer.allocate(8)
            fc.read(byteBuffer)
            byteBuffer.rewind()
            val size = IsoTypeReader.readUInt32(byteBuffer)
            val type = IsoTypeReader.read4cc(byteBuffer)
            val newEnd = begin + size

            if (type == "uuid") {
                val fis = FileInputStream(File(path))
                fis.skip(begin)

                val sb = StringBuilder()
                val buffer = ByteArray(1024)
                while (sb.length < size) {
                    val n = fis.read(buffer)
                    if (n != -1) {
                        sb.append(String(buffer, 0, n))
                    } else {
                        break
                    }
                }

                val xmlString = sb.toString().lowercase(Locale.getDefault())
                if (xmlString.contains("gspherical:projectiontype>equirectangular") || xmlString.contains(
                        "gspherical:projectiontype=\"equirectangular\""
                    )
                ) {
                    callback.invoke()
                }
                return
            }

            if (fileChannelContainers.contains(type)) {
                parseFileChannel(path, fc, level + 1, begin + 8, newEnd, callback)
            }

            fc.position(newEnd)
        }
    } catch (ignored: Exception) {
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
fun Context.addPathToDB(path: String) {
    RunOnBackgroundThreadUseCase {

        if (!getDoesFilePathExist(path)) {
            return@RunOnBackgroundThreadUseCase
        }

        val type = when {
            path.isVideoFast() -> MediaType.Video.id
            IsGifUseCase(path) -> MediaType.Gif.id
            path.isRawFast() -> MediaType.Raw.id
            IsSvgUseCase(path) -> MediaType.Svg.id
            path.isPortrait() -> MediaType.Portrait.id
            else -> MediaType.Image.id
        }

        try {
            val isFavorite =
                GalleryDatabase.getInstance(applicationContext).FavoritesDao().isFavorite(path)
            val videoDuration = if (type == MediaType.Video.id) getDuration(path) ?: 0 else 0
            val medium = Medium(
                null,
                path.getFilenameFromPath(),
                path,
                path.getParentPath(),
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                File(path).length(),
                type,
                videoDuration,
                isFavorite,
                0L,
                0L
            )

            mediaDB.insert(medium)
        } catch (ignored: Exception) {
        }
    }
}

fun Context.buildDocumentUriSdk30(fullPath: String): Uri {
    val storageId = getSAFStorageId(this, fullPath)

    val relativePath = when {
        fullPath.startsWith(internalStoragePath) -> fullPath.substring(internalStoragePath.length)
            .trim('/')
        else -> fullPath.substringAfter(storageId).trim('/')
    }

    val documentId = "${storageId}:$relativePath"
    return DocumentsContract.buildDocumentUri(EXTERNAL_STORAGE_PROVIDER_AUTHORITY, documentId)
}

fun Context.createDirectoryFromMedia(
    path: String,
    curMedia: ArrayList<Medium>,
    albumCovers: ArrayList<AlbumCover>,
    hiddenString: String,
    includedFolders: MutableSet<String>,
    getProperFileSize: Boolean,
    noMediaFolders: ArrayList<String>
): Directory {
    val otgPath = config.otgPath
    val grouped = MediaFetcher(this).groupMedia(curMedia, path)
    var thumbnail: String? = null

    albumCovers.forEach {
        if (it.path == path && getDoesFilePathExist(it.thumbnail, otgPath)) {
            thumbnail = it.thumbnail
        }
    }

    if (thumbnail == null) {
        val sortedMedia = grouped.filterIsInstance<Medium>().toMutableList() as ArrayList<Medium>
        thumbnail = sortedMedia.firstOrNull { getDoesFilePathExist(it.path, otgPath) }?.path ?: ""
    }

    if (config.otgPath.isNotEmpty() && thumbnail!!.startsWith(config.otgPath)) {
        thumbnail = thumbnail!!.getOTGPublicPath(applicationContext)
    }

    val isSortingAscending = isSortingAscending(config.directorySorting)
    val defaultMedium = Medium(0, "", "", "", 0L, 0L, 0L, 0, 0, false, 0L, 0L)
    val firstItem = curMedia.firstOrNull() ?: defaultMedium
    val lastItem = curMedia.lastOrNull() ?: defaultMedium
    val dirName = checkAppendingHidden(path, hiddenString, includedFolders, noMediaFolders)
    val lastModified =
        if (isSortingAscending) firstItem.modified.coerceAtMost(lastItem.modified) else firstItem.modified.coerceAtLeast(
            lastItem.modified
        )
    val dateTaken =
        if (isSortingAscending) firstItem.taken.coerceAtMost(lastItem.taken) else firstItem.taken.coerceAtLeast(
            lastItem.taken
        )
    val size = if (getProperFileSize) curMedia.sumByLong { it.size } else 0L
    val mediaTypes = with(curMedia) {
        var types = 0
        if (any { it.isImage() }) {
            types += MediaType.Image.id
        }

        if (any { it.isVideo() }) {
            types += MediaType.Video.id
        }

        if (any { it.isGIF() }) {
            types += MediaType.Gif.id
        }

        if (any { it.isRaw() }) {
            types += MediaType.Raw.id
        }

        if (any { it.isSVG() }) {
            types += MediaType.Svg.id
        }

        if (any { it.isPortrait() }) {
            types += MediaType.Portrait.id
        }

        types
    }
    val sortValue = getDirectorySortingValue(curMedia, path, dirName, size)
    return Directory(
        null,
        path,
        thumbnail!!,
        dirName,
        curMedia.size,
        lastModified,
        dateTaken,
        size,
        getPathLocation(this, path),
        mediaTypes,
        sortValue
    )
}

fun Context.createFirstParentTreeUriUsingRootTree(fullPath: String): Uri {
    val storageId = getSAFStorageId(this, fullPath)
    val level = getFirstParentLevel(fullPath)
    val rootParentDirName = fullPath.getFirstParentDirName(this, level)
    val treeUri =
        DocumentsContract.buildTreeDocumentUri(EXTERNAL_STORAGE_PROVIDER_AUTHORITY, "$storageId:")
    val documentId = "${storageId}:$rootParentDirName"
    return DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
}

private fun getDataColumn(
    owner: Context,
    uri: Uri,
    selection: String? = null,
    selectionArgs: Array<String>? = null
): String? {
    try {
        val projection = arrayOf(Files.FileColumns.DATA)
        val cursor = owner.contentResolver.query(uri, projection, selection, selectionArgs, null)
        cursor?.use {
            if (cursor.moveToFirst()) {
                val data = cursor.getStringValue(Files.FileColumns.DATA)
                if (data != "null") {
                    return data
                }
            }
        }
    } catch (_: Exception) {
    }
    return null
}

fun Context.getDirectorySortingValue(
    media: ArrayList<Medium>,
    path: String,
    name: String,
    size: Long
): String {
    val sorting = config.directorySorting
    val sorted = when {
        sorting and SORT_BY_NAME != 0 -> return name
        sorting and SORT_BY_PATH != 0 -> return path
        sorting and SORT_BY_SIZE != 0 -> return size.toString()
        sorting and SORT_BY_DATE_MODIFIED != 0 -> media.sortedBy { it.modified }
        sorting and SORT_BY_DATE_TAKEN != 0 -> media.sortedBy { it.taken }
        else -> media
    }

    val relevantMedium = if (isSortingAscending(sorting)) {
        sorted.firstOrNull() ?: return ""
    } else {
        sorted.lastOrNull() ?: return ""
    }

    val result: Any = when {
        sorting and SORT_BY_DATE_MODIFIED != 0 -> relevantMedium.modified
        sorting and SORT_BY_DATE_TAKEN != 0 -> relevantMedium.taken
        else -> 0
    }

    return result.toString()
}

@RequiresApi(Build.VERSION_CODES.Q)
fun Context.getDuration(path: String): Int? {
    val projection = arrayOf(
        MediaStore.MediaColumns.DURATION
    )

    val uri = getFileUri(path)
    val selection =
        if (path.startsWith("content://")) "${BaseColumns._ID} = ?" else "${MediaStore.MediaColumns.DATA} = ?"
    val selectionArgs =
        if (path.startsWith("content://")) arrayOf(path.substringAfterLast("/")) else arrayOf(path)

    try {
        val cursor = contentResolver.query(uri, projection, selection, selectionArgs, null)
        cursor?.use {
            if (cursor.moveToFirst()) {
                return (cursor.getIntValue(MediaStore.MediaColumns.DURATION) / 1000.toDouble()).roundToInt()
            }
        }
    } catch (ignored: Exception) {
    }

    return try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        (retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!
            .toInt() / 1000f).roundToInt()
    } catch (ignored: Exception) {
        null
    }
}

@SuppressLint("NewApi")
fun Context.getProperBackgroundColor() = if (baseConfig.isUsingSystemTheme) {
    resources.getColor(R.color.you_background_color, theme)
} else {
    baseConfig.backgroundColor
}

@SuppressLint("NewApi")
fun Context.getProperPrimaryColor() = when {
    baseConfig.isUsingSystemTheme -> resources.getColor(R.color.you_primary_color, theme)
    isWhiteTheme() || isBlackAndWhiteTheme() -> baseConfig.accentColor
    else -> baseConfig.primaryColor
}

// handle system default theme (Material You) specially as the color is taken from the system, not hardcoded by us
@SuppressLint("NewApi")
fun Context.getProperTextColor() = if (baseConfig.isUsingSystemTheme) {
    resources.getColor(R.color.you_neutral_text_color, theme)
} else {
    baseConfig.textColor
}

fun Context.getSAFDocumentId(path: String): String {
    val basePath = path.getBasePath(this)
    val relativePath = path.substring(basePath.length).trim('/')
    val storageId = getSAFStorageId(this, path)
    return "$storageId:$relativePath"
}

fun Context.getStoreUrl() = "https://github.com/LeOS-GSI/LeOS-Gallery"

fun Context.getTextSize() = when (baseConfig.fontSize) {
    FONT_SIZE_SMALL -> resources.getDimension(R.dimen.smaller_text_size)
    FONT_SIZE_MEDIUM -> resources.getDimension(R.dimen.bigger_text_size)
    FONT_SIZE_LARGE -> resources.getDimension(R.dimen.big_text_size)
    else -> resources.getDimension(R.dimen.extra_big_text_size)
}

@RequiresApi(Build.VERSION_CODES.Q)
fun Context.updateDirectoryPath(path: String) {
    val mediaFetcher = MediaFetcher(applicationContext)
    val getImagesOnly = false
    val getVideosOnly = false
    val hiddenString = getString(R.string.hidden)
    val albumCovers = config.parseAlbumCovers()
    val includedFolders = config.includedFolders
    val noMediaFolders = getNoMediaFoldersSync()

    val sorting = config.getFolderSorting(path)
    val grouping = config.getFolderGrouping(path)
    val getProperDateTaken = config.directorySorting and SORT_BY_DATE_TAKEN != 0 ||
            sorting and SORT_BY_DATE_TAKEN != 0 ||
            grouping and GroupBy.DateTakenDaily.id != 0 ||
            grouping and GroupBy.DateTakenMonthly.id != 0

    val getProperLastModified = config.directorySorting and SORT_BY_DATE_MODIFIED != 0 ||
            sorting and SORT_BY_DATE_MODIFIED != 0 ||
            grouping and GroupBy.LastModifiedDaily.id != 0 ||
            grouping and GroupBy.LastModifiedMonthly.id != 0

    val getProperFileSize = config.directorySorting and SORT_BY_SIZE != 0

    val lastModifieds =
        if (getProperLastModified) mediaFetcher.getFolderLastModifieds(path) else HashMap()
    val dateTakens = mediaFetcher.getFolderDateTakens(path)
    val favoritePaths = getFavoritePaths()
    val curMedia = mediaFetcher.getFilesFrom(
        path,
        getImagesOnly,
        getVideosOnly,
        getProperDateTaken,
        getProperLastModified,
        getProperFileSize,
        favoritePaths,
        false,
        lastModifieds,
        dateTakens,
        null
    )
    val directory = createDirectoryFromMedia(
        path,
        curMedia,
        albumCovers,
        hiddenString,
        includedFolders,
        getProperFileSize,
        noMediaFolders
    )
    updateDBDirectory(directory)
}

fun Context.updateTextColors(viewGroup: ViewGroup) {
    val textColor = when {
        baseConfig.isUsingSystemTheme -> getProperTextColor()
        else -> baseConfig.textColor
    }

    val backgroundColor = baseConfig.backgroundColor
    val accentColor = when {
        isWhiteTheme() || isBlackAndWhiteTheme() -> baseConfig.accentColor
        else -> getProperPrimaryColor()
    }

    val cnt = viewGroup.childCount
    (0 until cnt).map { viewGroup.getChildAt(it) }.forEach {
        when (it) {
            is MyTextView -> it.setColors(textColor, accentColor, backgroundColor)
            is MyAppCompatSpinner -> it.setColors(textColor, backgroundColor)
            is MySwitchCompat -> it.setColors(textColor, accentColor, backgroundColor)
            is MyCompatRadioButton -> it.setColors(textColor, accentColor, backgroundColor)
            is MyAppCompatCheckbox -> it.setColors(textColor, accentColor)
            is MyEditText -> it.setColors(textColor, accentColor)
            is MyAutoCompleteTextView -> it.setColors(textColor, accentColor)
            is MyFloatingActionButton -> it.setColors(accentColor)
            is MySeekBar -> it.setColors(accentColor)
            is MyButton -> it.setColors(textColor, accentColor, backgroundColor)
            is MyTextInputLayout -> it.setColors(textColor, accentColor)
            is ViewGroup -> updateTextColors(it)
        }
    }
}

fun Context.getFileDateTaken(path: String): Long {
    val projection = arrayOf(
        Images.Media.DATE_TAKEN
    )

    val uri = Files.getContentUri("external")
    val selection = "${Images.Media.DATA} = ?"
    val selectionArgs = arrayOf(path)

    try {
        val cursor = contentResolver.query(uri, projection, selection, selectionArgs, null)
        cursor?.use {
            if (cursor.moveToFirst()) {
                return cursor.getLongValue(Images.Media.DATE_TAKEN)
            }
        }
    } catch (ignored: Exception) {
    }

    return 0L
}

// some helper functions were taken from https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
fun Context.getRealPathFromURI(uri: Uri): String? {
    if (uri.scheme == "file") {
        return uri.path
    }

    if (uri.authority == "com.android.providers.downloads.documents") {
        val id = DocumentsContract.getDocumentId(uri)
        if (id.areDigitsOnly()) {
            val newUri = ContentUris.withAppendedId(
                Uri.parse("content://downloads/public_downloads"),
                id.toLong()
            )
            val path = getDataColumn(this, newUri)
            if (path != null) {
                return path
            }
        }
    } else if (isExternalStorageDocument(uri)) {
        val documentId = DocumentsContract.getDocumentId(uri)
        val parts = documentId.split(":")
        if (parts[0].equals("primary", true)) {
            return "${Environment.getExternalStorageDirectory().absolutePath}/${parts[1]}"
        }
    } else if (isMediaDocument(uri)) {
        val documentId = DocumentsContract.getDocumentId(uri)
        val split = documentId.split(":").dropLastWhile { it.isEmpty() }.toTypedArray()

        val contentUri = when (split[0]) {
            "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            else -> Images.Media.EXTERNAL_CONTENT_URI
        }

        val selection = "_id=?"
        val selectionArgs = arrayOf(split[1])
        val path = getDataColumn(this, contentUri, selection, selectionArgs)
        if (path != null) {
            return path
        }
    }

    return getDataColumn(this, uri)
}

fun getSharedThemeSync(cursorLoader: CursorLoader): SharedTheme? {
    val cursor = cursorLoader.loadInBackground()
    cursor?.use {
        if (cursor.moveToFirst()) {
            try {
                val textColor = cursor.getIntValue(BaseContentProvider.COL_TEXT_COLOR)
                val backgroundColor = cursor.getIntValue(BaseContentProvider.COL_BACKGROUND_COLOR)
                val primaryColor = cursor.getIntValue(BaseContentProvider.COL_PRIMARY_COLOR)
                val accentColor = cursor.getIntValue(BaseContentProvider.COL_ACCENT_COLOR)
                val appIconColor = cursor.getIntValue(BaseContentProvider.COL_APP_ICON_COLOR)
                val navigationBarColor =
                    cursor.getIntValueOrNull(BaseContentProvider.COL_NAVIGATION_BAR_COLOR)
                        ?: INVALID_NAVIGATION_BAR_COLOR
                val lastUpdatedTS = cursor.getIntValue(BaseContentProvider.COL_LAST_UPDATED_TS)
                return SharedTheme(
                    textColor,
                    backgroundColor,
                    primaryColor,
                    appIconColor,
                    navigationBarColor,
                    lastUpdatedTS,
                    accentColor
                )
            } catch (_: Exception) {
            }
        }
    }
    return null
}

fun Context.hasOTGConnected(): Boolean {
    return try {
        (getSystemService(Context.USB_SERVICE) as UsbManager).deviceList.any {
            it.value.getInterface(0).interfaceClass == UsbConstants.USB_CLASS_MASS_STORAGE
        }
    } catch (e: Exception) {
        false
    }
}

fun Context.hasPermission(permId: Int) = ContextCompat.checkSelfPermission(
    this,
    getPermissionString(permId)
) == PackageManager.PERMISSION_GRANTED

fun Context.hasProperStoredDocumentUriSdk30(path: String): Boolean {
    val documentUri = buildDocumentUriSdk30(path)
    return contentResolver.persistedUriPermissions.any { it.uri.toString() == documentUri.toString() }
}

fun Context.isUsingSystemDarkTheme() =
    resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES != 0

val Context.realScreenSize: Point
    get() {
        val size = Point()
        windowManager(this).defaultDisplay.getRealSize(size)
        return size
    }

// avoid calling this multiple times in row, it can delete whole folder contents
fun Context.rescanPaths(paths: List<String>, callback: (() -> Unit)? = null) {
    if (paths.isEmpty()) {
        callback?.invoke()
        return
    }

    for (path in paths) {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).apply {
            data = Uri.fromFile(File(path))
            sendBroadcast(this)
        }
    }

    var cnt = paths.size
    MediaScannerConnection.scanFile(applicationContext, paths.toTypedArray(), null) { _, _ ->
        if (--cnt == 0) {
            callback?.invoke()
        }
    }
}

fun Context.scanPathRecursively(path: String, callback: (() -> Unit)? = null) {
    scanPathsRecursively(arrayListOf(path), callback)
}

fun Context.scanPathsRecursively(paths: List<String>, callback: (() -> Unit)? = null) {
    val allPaths = java.util.ArrayList<String>()
    for (path in paths) {
        allPaths.addAll(getPaths(File(path)))
    }
    rescanPaths(allPaths, callback)
}

private fun windowManager(owner: Context) =
    owner.getSystemService(Context.WINDOW_SERVICE) as WindowManager

fun isAndroidDataDir(path: String): Boolean {
    val resolvedPath = "${path.trimEnd('/')}/"
    return resolvedPath.contains(ANDROID_DATA_DIR)
}

fun isExternalStorageManager(): Boolean {
    return IsRPlusUseCase() && Environment.isExternalStorageManager()
}

val Context.statusBarHeight: Int
    @SuppressLint("DiscouragedApi", "InternalInsetResource")
    get() {
        var statusBarHeight = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusBarHeight = resources.getDimensionPixelSize(resourceId)
        }
        return statusBarHeight
    }

val Context.actionBarHeight: Int
    get() {
        val styledAttributes =
            theme.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
        val actionBarHeight = styledAttributes.getDimension(0, 0f)
        styledAttributes.recycle()
        return actionBarHeight.toInt()
    }

private fun usableScreenSize(owner: Context): Point {
    val size = Point()
    windowManager(owner).defaultDisplay.getSize(size)
    return size
}

fun Context.copyToClipboard(text: String) {
    val clip = ClipData.newPlainText(getString(R.string.simple_commons), text)
    (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
    val toastText = String.format(getString(R.string.value_copied_to_clipboard_show), text)
    Timber.d(toastText)
}

private fun getMediaStoreIds(context: Context): HashMap<String, Long> {
    val ids = java.util.HashMap<String, Long>()
    val projection = arrayOf(
        Images.Media.DATA,
        Images.Media._ID
    )

    val uri = Files.getContentUri("external")

    try {
        context.queryCursor(uri, projection) { cursor ->
            try {
                val id = cursor.getLongValue(Images.Media._ID)
                if (id != 0L) {
                    val path = cursor.getStringValue(Images.Media.DATA)
                    ids[path] = id
                }
            } catch (_: Exception) {
            }
        }
    } catch (_: Exception) {
    }

    return ids
}

fun Context.deleteAndroidSAFDirectory(
    path: String,
    allowDeleteFolder: Boolean = false,
    callback: ((wasSuccess: Boolean) -> Unit)? = null
) {
    val treeUri = getAndroidTreeUri(path).toUri()
    val documentId = createAndroidSAFDocumentId(path)
    try {
        val uri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
        val document = DocumentFile.fromSingleUri(this, uri)
        val fileDeleted =
            (document!!.isFile || allowDeleteFolder) && DocumentsContract.deleteDocument(
                applicationContext.contentResolver,
                document.uri
            )
        callback?.invoke(fileDeleted)
    } catch (e: Exception) {
        Timber.e(e)
        callback?.invoke(false)
        storeAndroidTreeUri(path, "")
    }
}

fun Context.updateOTGPathFromPartition() {
    val otgPath = "/storage/${baseConfig.otgPartition}"
    baseConfig.otgPath = if (getOTGFastDocumentFile(this, otgPath, otgPath)?.exists() == true) {
        "/storage/${baseConfig.otgPartition}"
    } else {
        "/mnt/media_rw/${baseConfig.otgPartition}"
    }
}

// these functions update the mediastore instantly, MediaScannerConnection.scanFileRecursively takes some time to really get applied
fun Context.deleteFromMediaStore(path: String, callback: ((needsRescan: Boolean) -> Unit)? = null) {
    if (getIsPathDirectory(path)) {
        callback?.invoke(false)
        return
    }

    RunOnBackgroundThreadUseCase {
        try {
            val where = "${MediaStore.MediaColumns.DATA} = ?"
            val args = arrayOf(path)
            val needsRescan = contentResolver.delete(getFileUri(path), where, args) != 1
            callback?.invoke(needsRescan)
        } catch (ignored: Exception) {
        }
        callback?.invoke(true)
    }

}

fun Context.updateInMediaStore(oldPath: String, newPath: String) {
    RunOnBackgroundThreadUseCase {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DATA, newPath)
            put(MediaStore.MediaColumns.DISPLAY_NAME, newPath.getFilenameFromPath())
            put(MediaStore.MediaColumns.TITLE, newPath.getFilenameFromPath())
        }
        val uri = getFileUri(oldPath)
        val selection = "${MediaStore.MediaColumns.DATA} = ?"
        val selectionArgs = arrayOf(oldPath)

        try {
            contentResolver.update(uri, values, selection, selectionArgs)
        } catch (ignored: Exception) {
        }
    }
}

fun Context.updateLastModified(path: String, lastModified: Long) {
    val values = ContentValues().apply {
        put(MediaStore.MediaColumns.DATE_MODIFIED, lastModified / 1000)
    }
    File(path).setLastModified(lastModified)
    val uri = getFileUri(path)
    val selection = "${MediaStore.MediaColumns.DATA} = ?"
    val selectionArgs = arrayOf(path)

    try {
        contentResolver.update(uri, values, selection, selectionArgs)
    } catch (ignored: Exception) {
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun Context.getAndroidSAFFileItems(
    path: String,
    shouldShowHidden: Boolean,
    getProperFileSize: Boolean = true,
    callback: (ArrayList<FileDirItem>) -> Unit
) {
    val items = java.util.ArrayList<FileDirItem>()
    val rootDocId = getStorageRootIdForAndroidDir(this, path)
    val treeUri = getAndroidTreeUri(path).toUri()
    val documentId = createAndroidSAFDocumentId(path)
    val childrenUri = try {
        DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, documentId)
    } catch (e: Exception) {

        Timber.e(e)
        storeAndroidTreeUri(path, "")
        null
    }

    if (childrenUri == null) {
        callback(items)
        return
    }

    val projection = arrayOf(
        DocumentsContract.Document.COLUMN_DOCUMENT_ID,
        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
        DocumentsContract.Document.COLUMN_MIME_TYPE,
        DocumentsContract.Document.COLUMN_LAST_MODIFIED
    )
    try {
        val rawCursor = contentResolver.query(childrenUri, projection, null, null)!!
        val cursor =
            ExternalStorageProviderHack.transformQueryResult(rootDocId, childrenUri, rawCursor)
        cursor.use {
            if (cursor.moveToFirst()) {
                do {
                    val docId = cursor.getStringValue(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                    val name = cursor.getStringValue(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                    val mimeType =
                        cursor.getStringValue(DocumentsContract.Document.COLUMN_MIME_TYPE)
                    val lastModified =
                        cursor.getLongValue(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
                    val isDirectory = mimeType == DocumentsContract.Document.MIME_TYPE_DIR
                    val filePath =
                        docId.substring("${getStorageRootIdForAndroidDir(this, path)}:".length)
                    if (!shouldShowHidden && name.startsWith(".")) {
                        continue
                    }

                    val decodedPath =
                        path.getBasePath(this) + "/" + URLDecoder.decode(filePath, "UTF-8")
                    val fileSize = when {
                        getProperFileSize -> getFileSize(treeUri, docId)
                        isDirectory -> 0L
                        else -> getFileSize(treeUri, docId)
                    }

                    val childrenCount = if (isDirectory) {
                        getDirectChildrenCount(rootDocId, treeUri, docId, shouldShowHidden)
                    } else {
                        0
                    }

                    val fileDirItem = FileDirItem(
                        decodedPath,
                        name,
                        isDirectory,
                        childrenCount,
                        fileSize,
                        lastModified
                    )
                    items.add(fileDirItem)
                } while (cursor.moveToNext())
            }
        }
    } catch (e: Exception) {

        Timber.e(e)
    }
    callback(items)
}

fun Context.getProperChildrenCount(
    rootDocId: String,
    treeUri: Uri,
    documentId: String,
    shouldShowHidden: Boolean
): Int {
    val projection = arrayOf(
        DocumentsContract.Document.COLUMN_DOCUMENT_ID,
        DocumentsContract.Document.COLUMN_MIME_TYPE
    )
    val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, documentId)
    val rawCursor = contentResolver.query(childrenUri, projection, null, null, null)!!
    val cursor = ExternalStorageProviderHack.transformQueryResult(rootDocId, childrenUri, rawCursor)
    return if (cursor.count > 0) {
        var count = 0
        cursor.use {
            while (cursor.moveToNext()) {
                val docId = cursor.getStringValue(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val mimeType = cursor.getStringValue(DocumentsContract.Document.COLUMN_MIME_TYPE)
                if (mimeType == DocumentsContract.Document.MIME_TYPE_DIR) {
                    count++
                    count += getProperChildrenCount(rootDocId, treeUri, docId, shouldShowHidden)
                } else if (!docId.getFilenameFromPath().startsWith('.') || shouldShowHidden) {
                    count++
                }
            }
        }
        count
    } else {
        1
    }
}

fun Context.getFileSize(treeUri: Uri, documentId: String): Long {
    val projection = arrayOf(DocumentsContract.Document.COLUMN_SIZE)
    val documentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
    return contentResolver.query(documentUri, projection, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            cursor.getLongValue(DocumentsContract.Document.COLUMN_SIZE)
        } else {
            0L
        }
    } ?: 0L
}

fun Context.createAndroidSAFDocumentId(path: String): String {
    val basePath = path.getBasePath(this)
    val relativePath = path.substring(basePath.length).trim('/')
    val storageId = getStorageRootIdForAndroidDir(this, path)
    return "$storageId:$relativePath"
}

private fun getAndroidSAFDocument(owner: Context, path: String): DocumentFile? {
    val basePath = path.getBasePath(owner)
    val androidPath = File(basePath, "Android").path
    var relativePath = path.substring(androidPath.length)
    if (relativePath.startsWith(File.separator)) {
        relativePath = relativePath.substring(1)
    }

    return try {
        val treeUri = owner.getAndroidTreeUri(path).toUri()
        var document = DocumentFile.fromTreeUri(owner.applicationContext, treeUri)
        val parts = relativePath.split("/").filter { it.isNotEmpty() }
        for (part in parts) {
            document = document?.findFile(part)
        }
        document
    } catch (ignored: Exception) {
        null
    }
}

fun Context.getSomeAndroidSAFDocument(path: String): DocumentFile? =
    getFastAndroidSAFDocument(this, path) ?: getAndroidSAFDocument(this, path)

private fun getFastAndroidSAFDocument(owner: Context, path: String): DocumentFile? {
    val treeUri = owner.getAndroidTreeUri(path)
    if (treeUri.isEmpty()) {
        return null
    }

    val uri = owner.getAndroidSAFUri(path)
    return DocumentFile.fromSingleUri(owner, uri)
}

fun Context.createAndroidSAFDirectory(path: String): Boolean {
    return try {
        val treeUri = getAndroidTreeUri(path).toUri()
        val parentPath = path.getParentPath()
        if (!getDoesFilePathExist(parentPath)) {
            createAndroidSAFDirectory(parentPath)
        }
        val documentId = createAndroidSAFDocumentId(parentPath)
        val parentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
        DocumentsContract.createDocument(
            contentResolver,
            parentUri,
            DocumentsContract.Document.MIME_TYPE_DIR,
            path.getFilenameFromPath()
        ) != null
    } catch (e: IllegalStateException) {

        Timber.e(e)
        false
    }
}

fun Context.createAndroidSAFFile(path: String): Boolean {
    return try {
        val treeUri = getAndroidTreeUri(path).toUri()
        val parentPath = path.getParentPath()
        if (!getDoesFilePathExist(parentPath)) {
            createAndroidSAFDirectory(parentPath)
        }

        val documentId = createAndroidSAFDocumentId(path.getParentPath())
        val parentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
        DocumentsContract.createDocument(
            contentResolver,
            parentUri,
            path.getMimeType(),
            path.getFilenameFromPath()
        ) != null
    } catch (e: IllegalStateException) {
        Timber.e(e)
        false
    }
}

// http://stackoverflow.com/a/40582634/1967672
fun Context.getSDCardPath(): String {
    val directories = getStorageDirectories().filter {
        it != internalStoragePath && !it.equals(
            "/storage/emulated/0",
            true
        ) && (baseConfig.otgPartition.isEmpty() || !it.endsWith(baseConfig.otgPartition))
    }

    val fullSDpattern = Pattern.compile(SD_OTG_PATTERN)
    var sdCardPath = directories.firstOrNull { fullSDpattern.matcher(it).matches() }
        ?: directories.firstOrNull {
            !physicalPaths.contains(
                it.lowercase(Locale.getDefault())
            )
        } ?: ""

    // on some devices no method retrieved any SD card path, so test if its not sdcard1 by any chance. It happened on an Android 5.1
    if (sdCardPath.trimEnd('/').isEmpty()) {
        val file = File("/storage/sdcard1")
        if (file.exists()) {
            return file.absolutePath
        }

        sdCardPath = directories.firstOrNull() ?: ""
    }

    if (sdCardPath.isEmpty()) {
        val sDPattern = Pattern.compile(SD_OTG_SHORT)
        try {
            File("/storage").listFiles()?.forEach {
                if (sDPattern.matcher(it.name).matches()) {
                    sdCardPath = "/storage/${it.name}"
                }
            }
        } catch (_: Exception) {
        }
    }

    val finalPath = sdCardPath.trimEnd('/')
    baseConfig.sdCardPath = finalPath
    return finalPath
}

fun Context.hasProperStoredTreeUri(isOTG: Boolean): Boolean {
    val uri = if (isOTG) baseConfig.otgTreeUri else baseConfig.sdTreeUri
    val hasProperUri = contentResolver.persistedUriPermissions.any { it.uri.toString() == uri }
    if (!hasProperUri) {
        if (isOTG) {
            baseConfig.otgTreeUri = ""
        } else {
            baseConfig.sdTreeUri = ""
        }
    }
    return hasProperUri
}

fun Context.hasProperStoredAndroidTreeUri(path: String): Boolean {
    val uri = getAndroidTreeUri(path)
    val hasProperUri = contentResolver.persistedUriPermissions.any { it.uri.toString() == uri }
    if (!hasProperUri) {
        storeAndroidTreeUri(path, "")
    }
    return hasProperUri
}

fun Context.storeAndroidTreeUri(path: String, treeUri: String) {
    return when {
        IsPathOnOtgUseCase(this, path) ->
            if (isAndroidDataDir(path)) baseConfig.otgAndroidDataTreeUri =
                treeUri else baseConfig.otgAndroidObbTreeUri = treeUri
        IsPathOnSdUseCase(this, path) ->
            if (isAndroidDataDir(path)) baseConfig.sdAndroidDataTreeUri =
                treeUri else baseConfig.sdAndroidObbTreeUri = treeUri
        else -> if (isAndroidDataDir(path)) baseConfig.primaryAndroidDataTreeUri =
            treeUri else baseConfig.primaryAndroidObbTreeUri = treeUri
    }
}

fun Context.createDocumentUriFromRootTree(fullPath: String): Uri {
    val storageId = getSAFStorageId(this, fullPath)

    val relativePath = when {
        fullPath.startsWith(internalStoragePath) -> fullPath.substring(internalStoragePath.length)
            .trim('/')
        else -> fullPath.substringAfter(storageId).trim('/')
    }

    val treeUri =
        DocumentsContract.buildTreeDocumentUri(EXTERNAL_STORAGE_PROVIDER_AUTHORITY, "$storageId:")
    val documentId = "${storageId}:$relativePath"
    return DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
}

fun Context.createAndroidDataOrObbPath(fullPath: String): String {
    return if (isAndroidDataDir(fullPath)) {
        fullPath.getBasePath(this).trimEnd('/').plus(ANDROID_DATA_DIR)
    } else {
        fullPath.getBasePath(this).trimEnd('/').plus(ANDROID_OBB_DIR)
    }
}

fun Context.createAndroidDataOrObbUri(fullPath: String): Uri {
    val path = createAndroidDataOrObbPath(fullPath)
    return createDocumentUriFromRootTree(path)
}

fun getRealInternalStoragePath() =
    if (File("/storage/emulated/0").exists()) "/storage/emulated/0" else Environment.getExternalStorageDirectory().absolutePath.trimEnd(
        '/'
    )

// Convert paths like /storage/emulated/0/Pictures/Screenshots/first.jpg to content://media/external/images/media/131799
// so that we can refer to the file in the MediaStore.
// If we found no mediastore uri for a given file, do not return its path either to avoid some mismatching
fun Context.getFileUrisFromFileDirItemsTuple(fileDirItems: List<FileDirItem>): Pair<ArrayList<String>, ArrayList<Uri>> {
    val fileUris = java.util.ArrayList<Uri>()
    val successfulFilePaths = java.util.ArrayList<String>()
    val allIds = getMediaStoreIds(this)
    val filePaths = fileDirItems.map { it.path }
    filePaths.forEach { path ->
        for ((filePath, mediaStoreId) in allIds) {
            if (filePath.lowercase() == path.lowercase()) {
                val baseUri = getFileUri(filePath)
                val uri = ContentUris.withAppendedId(baseUri, mediaStoreId)
                fileUris.add(uri)
                successfulFilePaths.add(path)
            }
        }
    }

    return Pair(successfulFilePaths, fileUris)
}

private fun isSortingAscending(sort: Int) = sort and SORT_DESCENDING == 0
