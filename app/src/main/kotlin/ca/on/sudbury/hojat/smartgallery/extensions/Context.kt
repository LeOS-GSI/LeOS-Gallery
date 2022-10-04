package ca.on.sudbury.hojat.smartgallery.extensions

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.NotificationManager
import android.app.role.RoleManager
import android.appwidget.AppWidgetManager
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ShortcutManager
import android.content.res.Configuration
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.PictureDrawable
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbManager
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.media.MediaScannerConnection
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.provider.BaseColumns
import android.provider.BlockedNumberContract
import android.provider.ContactsContract
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.MediaStore.Files
import android.provider.MediaStore.Images
import android.provider.OpenableColumns
import android.provider.Settings
import android.telecom.TelecomManager
import android.telephony.PhoneNumberUtils
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.documentfile.provider.DocumentFile
import androidx.exifinterface.media.ExifInterface
import androidx.loader.content.CursorLoader
import ca.on.sudbury.hojat.smartgallery.R
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
import com.simplemobiletools.commons.extensions.doesThisOrParentHaveNoMedia
import com.simplemobiletools.commons.helpers.FAVORITES
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.commons.helpers.NOMEDIA
import com.simplemobiletools.commons.helpers.SORT_BY_NAME
import com.simplemobiletools.commons.helpers.SORT_BY_PATH
import com.simplemobiletools.commons.helpers.SORT_BY_SIZE
import com.simplemobiletools.commons.helpers.SORT_BY_DATE_MODIFIED
import com.simplemobiletools.commons.helpers.SORT_BY_DATE_TAKEN
import com.simplemobiletools.commons.helpers.sumByLong
import com.simplemobiletools.commons.helpers.SORT_BY_CUSTOM
import com.simplemobiletools.commons.helpers.SORT_BY_RANDOM
import com.simplemobiletools.commons.helpers.SORT_USE_NUMERIC_VALUE
import com.simplemobiletools.commons.helpers.AlphanumericComparator
import com.simplemobiletools.commons.helpers.SORT_DESCENDING
import com.simplemobiletools.commons.views.MySquareImageView
import ca.on.sudbury.hojat.smartgallery.asynctasks.GetMediaAsynctask
import ca.on.sudbury.hojat.smartgallery.database.WidgetsDao
import ca.on.sudbury.hojat.smartgallery.database.MediumDao
import ca.on.sudbury.hojat.smartgallery.database.DirectoryDao
import ca.on.sudbury.hojat.smartgallery.database.FavoritesDao
import ca.on.sudbury.hojat.smartgallery.database.DateTakensDao
import ca.on.sudbury.hojat.smartgallery.helpers.TYPE_GIFS
import ca.on.sudbury.hojat.smartgallery.helpers.TYPE_IMAGES
import ca.on.sudbury.hojat.smartgallery.helpers.TYPE_PORTRAITS
import ca.on.sudbury.hojat.smartgallery.helpers.TYPE_VIDEOS
import ca.on.sudbury.hojat.smartgallery.helpers.TYPE_RAWS
import ca.on.sudbury.hojat.smartgallery.helpers.RECYCLE_BIN
import ca.on.sudbury.hojat.smartgallery.helpers.MyWidgetProvider
import ca.on.sudbury.hojat.smartgallery.helpers.SHOW_ALL
import ca.on.sudbury.hojat.smartgallery.helpers.TYPE_SVGS
import ca.on.sudbury.hojat.smartgallery.helpers.LOCATION_SD
import ca.on.sudbury.hojat.smartgallery.helpers.LOCATION_OTG
import ca.on.sudbury.hojat.smartgallery.helpers.LOCATION_INTERNAL
import ca.on.sudbury.hojat.smartgallery.helpers.ROUNDED_CORNERS_NONE
import ca.on.sudbury.hojat.smartgallery.helpers.ROUNDED_CORNERS_SMALL
import ca.on.sudbury.hojat.smartgallery.helpers.PicassoRoundedCornersTransformation
import ca.on.sudbury.hojat.smartgallery.helpers.MediaFetcher
import ca.on.sudbury.hojat.smartgallery.helpers.IsoTypeReader
import ca.on.sudbury.hojat.smartgallery.helpers.GROUP_BY_DATE_TAKEN_DAILY
import ca.on.sudbury.hojat.smartgallery.helpers.GROUP_BY_DATE_TAKEN_MONTHLY
import ca.on.sudbury.hojat.smartgallery.helpers.GROUP_BY_LAST_MODIFIED_DAILY
import ca.on.sudbury.hojat.smartgallery.helpers.GROUP_BY_LAST_MODIFIED_MONTHLY
import ca.on.sudbury.hojat.smartgallery.helpers.Config
import ca.on.sudbury.hojat.smartgallery.databases.GalleryDatabase
import ca.on.sudbury.hojat.smartgallery.models.Directory
import ca.on.sudbury.hojat.smartgallery.models.Medium
import ca.on.sudbury.hojat.smartgallery.models.ThumbnailItem
import ca.on.sudbury.hojat.smartgallery.models.Favorite
import ca.on.sudbury.hojat.smartgallery.models.AlbumCover
import ca.on.sudbury.hojat.smartgallery.svg.SvgSoftwareLayerSetter
import com.github.ajalt.reprint.core.Reprint
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.simplemobiletools.commons.extensions.applyColorFilter
import ca.on.sudbury.hojat.smartgallery.extensions.areDigitsOnly
import com.simplemobiletools.commons.extensions.degreesFromOrientation
import ca.on.sudbury.hojat.smartgallery.extensions.getBasePath
import com.simplemobiletools.commons.extensions.getContrastColor
import com.simplemobiletools.commons.extensions.getDirectChildrenCount
import ca.on.sudbury.hojat.smartgallery.extensions.getFilenameFromPath
import ca.on.sudbury.hojat.smartgallery.extensions.getFirstParentDirName
import ca.on.sudbury.hojat.smartgallery.extensions.getFirstParentPath
import com.simplemobiletools.commons.extensions.getIntValue
import com.simplemobiletools.commons.extensions.getIntValueOrNull
import com.simplemobiletools.commons.extensions.getItemSize
import com.simplemobiletools.commons.extensions.getLongValue
import com.simplemobiletools.commons.extensions.getMimeType
import ca.on.sudbury.hojat.smartgallery.extensions.getParentPath
import com.simplemobiletools.commons.extensions.getStringValue
import com.simplemobiletools.commons.extensions.isAudioSlow
import com.simplemobiletools.commons.extensions.isImageSlow
import com.simplemobiletools.commons.extensions.isMediaFile
import com.simplemobiletools.commons.extensions.isVideoFast
import com.simplemobiletools.commons.extensions.isVideoSlow
import com.simplemobiletools.commons.extensions.lightenColor
import com.simplemobiletools.commons.extensions.moveLastItemToFront
import com.simplemobiletools.commons.extensions.orientationFromDegrees
import com.simplemobiletools.commons.extensions.showErrorToast
import com.simplemobiletools.commons.extensions.toast
import ca.on.sudbury.hojat.smartgallery.extensions.trimToComparableNumber
import com.simplemobiletools.commons.helpers.BaseConfig
import com.simplemobiletools.commons.helpers.DARK_GREY
import com.simplemobiletools.commons.helpers.DAY_SECONDS
import com.simplemobiletools.commons.helpers.EXTERNAL_STORAGE_PROVIDER_AUTHORITY
import com.simplemobiletools.commons.helpers.ExternalStorageProviderHack
import com.simplemobiletools.commons.helpers.FONT_SIZE_LARGE
import com.simplemobiletools.commons.helpers.FONT_SIZE_MEDIUM
import com.simplemobiletools.commons.helpers.FONT_SIZE_SMALL
import com.simplemobiletools.commons.helpers.FRIDAY_BIT
import com.simplemobiletools.commons.helpers.HOUR_SECONDS
import com.simplemobiletools.commons.helpers.INVALID_NAVIGATION_BAR_COLOR
import com.simplemobiletools.commons.helpers.MINUTE_SECONDS
import com.simplemobiletools.commons.helpers.MONDAY_BIT
import com.simplemobiletools.commons.helpers.MONTH_SECONDS
import com.simplemobiletools.commons.helpers.MyContactsContentProvider
import com.simplemobiletools.commons.helpers.MyContentProvider
import com.simplemobiletools.commons.helpers.PERMISSION_CALL_PHONE
import com.simplemobiletools.commons.helpers.PERMISSION_CAMERA
import com.simplemobiletools.commons.helpers.PERMISSION_GET_ACCOUNTS
import com.simplemobiletools.commons.helpers.PERMISSION_MEDIA_LOCATION
import com.simplemobiletools.commons.helpers.PERMISSION_READ_CALENDAR
import com.simplemobiletools.commons.helpers.PERMISSION_READ_CALL_LOG
import com.simplemobiletools.commons.helpers.PERMISSION_READ_CONTACTS
import com.simplemobiletools.commons.helpers.PERMISSION_READ_PHONE_STATE
import com.simplemobiletools.commons.helpers.PERMISSION_READ_SMS
import com.simplemobiletools.commons.helpers.PERMISSION_READ_STORAGE
import com.simplemobiletools.commons.helpers.PERMISSION_RECORD_AUDIO
import com.simplemobiletools.commons.helpers.PERMISSION_SEND_SMS
import com.simplemobiletools.commons.helpers.PERMISSION_WRITE_CALENDAR
import com.simplemobiletools.commons.helpers.PERMISSION_WRITE_CALL_LOG
import com.simplemobiletools.commons.helpers.PERMISSION_WRITE_CONTACTS
import com.simplemobiletools.commons.helpers.PERMISSION_WRITE_STORAGE
import com.simplemobiletools.commons.helpers.PREFS_KEY
import com.simplemobiletools.commons.helpers.SATURDAY_BIT
import com.simplemobiletools.commons.helpers.SD_OTG_PATTERN
import com.simplemobiletools.commons.helpers.SD_OTG_SHORT
import com.simplemobiletools.commons.helpers.SUNDAY_BIT
import com.simplemobiletools.commons.helpers.THURSDAY_BIT
import com.simplemobiletools.commons.helpers.TIME_FORMAT_12
import com.simplemobiletools.commons.helpers.TIME_FORMAT_24
import com.simplemobiletools.commons.helpers.TUESDAY_BIT
import com.simplemobiletools.commons.helpers.WEDNESDAY_BIT
import com.simplemobiletools.commons.helpers.WEEK_SECONDS
import com.simplemobiletools.commons.helpers.YEAR_SECONDS
import com.simplemobiletools.commons.helpers.YOUR_ALARM_SOUNDS_MIN_ID
import com.simplemobiletools.commons.helpers.appIconColorStrings
import com.simplemobiletools.commons.helpers.isMarshmallowPlus
import com.simplemobiletools.commons.helpers.isNougatPlus
import com.simplemobiletools.commons.helpers.isOnMainThread
import com.simplemobiletools.commons.helpers.isQPlus
import com.simplemobiletools.commons.helpers.isRPlus
import com.simplemobiletools.commons.helpers.isSPlus
import com.simplemobiletools.commons.helpers.proPackages
import com.simplemobiletools.commons.models.AlarmSound
import com.simplemobiletools.commons.models.BlockedNumber
import com.simplemobiletools.commons.models.FileDirItem
import com.simplemobiletools.commons.models.SharedTheme
import com.simplemobiletools.commons.views.MyAppCompatCheckbox
import com.simplemobiletools.commons.views.MyAppCompatSpinner
import com.simplemobiletools.commons.views.MyAutoCompleteTextView
import com.simplemobiletools.commons.views.MyButton
import com.simplemobiletools.commons.views.MyCompatRadioButton
import com.simplemobiletools.commons.views.MyEditText
import com.simplemobiletools.commons.views.MyFloatingActionButton
import com.simplemobiletools.commons.views.MySeekBar
import com.simplemobiletools.commons.views.MySwitchCompat
import com.simplemobiletools.commons.views.MyTextInputLayout
import com.simplemobiletools.commons.views.MyTextView
import com.squareup.picasso.Picasso
import pl.droidsonroids.gif.GifDrawable
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.net.URLDecoder
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.collections.LinkedHashSet
import kotlin.collections.set

private const val DOWNLOAD_DIR = "Download"
private const val ANDROID_DIR = "Android"
private val DIRS_INACCESSIBLE_WITH_SAF_SDK_30 = listOf(DOWNLOAD_DIR, ANDROID_DIR)


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
    val storageId = getSAFStorageId(fullPath)
    val level = getFirstParentLevel(fullPath)
    val rootParentDirName = fullPath.getFirstParentDirName(this, level)
    val firstParentId = "$storageId:$rootParentDirName"
    return DocumentsContract.buildTreeDocumentUri(
        EXTERNAL_STORAGE_PROVIDER_AUTHORITY,
        firstParentId
    )
}

// is the app a Media Management App on Android 12+?
fun Context.canManageMedia(): Boolean {
    return isSPlus() && MediaStore.canManageMedia(this)
}

fun Context.createDocumentUriUsingFirstParentTreeUri(fullPath: String): Uri {
    val storageId = getSAFStorageId(fullPath)
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
        showErrorToast(e)
        false
    }
}

fun Context.createSAFFileSdk30(path: String): Boolean {
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
            path.getMimeType(),
            path.getFilenameFromPath()
        ) != null
    } catch (e: IllegalStateException) {
        showErrorToast(e)
        false
    }
}

fun Context.ensurePublicUri(path: String, applicationId: String): Uri? {
    return when {
        hasProperStoredAndroidTreeUri(path) && isRestrictedSAFOnlyRoot(path) -> {
            getAndroidSAFUri(path)
        }
        hasProperStoredDocumentUriSdk30(path) && isAccessibleWithSAFSdk30(path) -> {
            createDocumentUriUsingFirstParentTreeUri(path)
        }
        isPathOnOTG(path) -> {
            getDocumentFile(path)?.uri
        }
        else -> {
            val uri = Uri.parse(path)
            if (uri.scheme == "content") {
                uri
            } else {
                val newPath = if (uri.toString().startsWith("/")) uri.toString() else uri.path
                val file = File(newPath)
                getFilePublicUri(file, applicationId)
            }
        }
    }
}

fun Context.ensurePublicUri(uri: Uri, applicationId: String): Uri {
    return if (uri.scheme == "content") {
        uri
    } else {
        val file = File(uri.path)
        getFilePublicUri(file, applicationId)
    }
}

fun Context.getDoesFilePathExistSdk30(path: String): Boolean {
    return when {
        isAccessibleWithSAFSdk30(path) -> getFastDocumentSdk30(path)?.exists() ?: false
        else -> File(path).exists()
    }
}

fun Context.getMediaContent(path: String, uri: Uri): Uri? {
    val projection = arrayOf(Images.Media._ID)
    val selection = Images.Media.DATA + "= ?"
    val selectionArgs = arrayOf(path)
    try {
        val cursor = contentResolver.query(uri, projection, selection, selectionArgs, null)
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
    CursorLoader(this, MyContentProvider.MY_CONTENT_URI, null, null, null, null)

fun Context.getSharedPrefs() = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)

fun Context.getStringsPackageName() = getString(R.string.package_name)

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
            showErrorToast(e)
        }
    }
}

private fun Context.queryCursorDesc(
    uri: Uri,
    projection: Array<String>,
    sortColumn: String,
    limit: Int,
): Cursor? {
    return if (isRPlus()) {
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

private fun isDownloadsDocument(uri: Uri) =
    uri.authority == "com.android.providers.downloads.documents"

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

val Context.baseConfig: BaseConfig get() = BaseConfig.newInstance(this)

val Context.recycleBinPath: String get() = filesDir.absolutePath

val Context.audioManager get() = getSystemService(Context.AUDIO_SERVICE) as AudioManager

fun Context.getAndroidSAFDirectChildrenCount(path: String, countHidden: Boolean): Int {
    val treeUri = getAndroidTreeUri(path).toUri()
    if (treeUri == Uri.EMPTY) {
        return 0
    }

    val documentId = createAndroidSAFDocumentId(path)
    val rootDocId = getStorageRootIdForAndroidDir(path)
    return getDirectChildrenCount(rootDocId, treeUri, documentId, countHidden)
}

fun Context.getAndroidSAFUri(path: String): Uri {
    val treeUri = getAndroidTreeUri(path).toUri()
    val documentId = createAndroidSAFDocumentId(path)
    return DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
}

fun Context.getAndroidTreeUri(path: String): String {
    return when {
        isPathOnOTG(path) -> if (isAndroidDataDir(path)) baseConfig.otgAndroidDataTreeUri else baseConfig.otgAndroidObbTreeUri
        isPathOnSD(path) -> if (isAndroidDataDir(path)) baseConfig.sdAndroidDataTreeUri else baseConfig.sdAndroidObbTreeUri
        else -> if (isAndroidDataDir(path)) baseConfig.primaryAndroidDataTreeUri else baseConfig.primaryAndroidObbTreeUri
    }
}

fun Context.getAppIconColors() =
    resources.getIntArray(R.array.md_app_icon_colors).toCollection(ArrayList())

@SuppressLint("NewApi")
fun Context.getBottomNavigationBackgroundColor(): Int {
    val baseColor = baseConfig.backgroundColor
    val bottomColor = when {
        baseConfig.isUsingSystemTheme -> resources.getColor(R.color.you_status_bar_color, theme)
        baseColor == Color.WHITE -> resources.getColor(R.color.bottom_tabs_light_background)
        else -> baseConfig.backgroundColor.lightenColor(4)
    }
    return bottomColor
}

fun Context.getCanAppBeUpgraded() = proPackages.contains(
    baseConfig.appId.removeSuffix(".debug").removePrefix("com.simplemobiletools.")
)

fun Context.getCurrentFormattedDateTime(): String {
    val simpleDateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault())
    return simpleDateFormat.format(Date(System.currentTimeMillis()))
}

fun Context.getCustomizeColorsString(): String {
    val textId = if (isOrWasThankYouInstalled()) {
        R.string.customize_colors
    } else {
        R.string.customize_colors_locked
    }

    return getString(textId)
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
    val isOTG = isPathOnOTG(path)
    var relativePath = path.substring(if (isOTG) otgPath.length else sdCardPath.length)
    if (relativePath.startsWith(File.separator)) {
        relativePath = relativePath.substring(1)
    }

    return try {
        val treeUri = Uri.parse(if (isOTG) baseConfig.OTGTreeUri else baseConfig.sdTreeUri)
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
    val otgPath = otgPathToUse ?: baseConfig.OTGPath
    return when {
        isRestrictedSAFOnlyRoot(path) -> getFastAndroidSAFDocument(path)?.exists() ?: false
        otgPath.isNotEmpty() && path.startsWith(otgPath) -> getOTGFastDocumentFile(path)?.exists()
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
        isPathOnOTG(path) -> {
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
    var uri = if (file.isMediaFile()) {
        getMediaContentUri(file.absolutePath)
    } else {
        getMediaContent(file.absolutePath, Files.getContentUri("external"))
    }

    if (uri == null) {
        uri = FileProvider.getUriForFile(this, "$applicationId.provider", file)
    }

    return uri!!
}

fun Context.getFileUri(path: String) = when {
    path.isImageSlow() -> Images.Media.EXTERNAL_CONTENT_URI
    path.isVideoSlow() -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    path.isAudioSlow() -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    else -> Files.getContentUri("external")
}

fun Context.getFileUrisFromFileDirItems(fileDirItems: List<FileDirItem>): List<Uri> {
    val fileUris = getUrisPathsFromFileDirItems(fileDirItems).second
    if (fileUris.isEmpty()) {
        fileDirItems.map { fileDirItem ->
            fileUris.add(fileDirItem.assembleContentUri())
        }
    }

    return fileUris
}

fun Context.getFirstParentLevel(path: String): Int {
    return when {
        isRPlus() && (isInAndroidDir(path) || isInSubFolderInDownloadDir(path)) -> 1
        else -> 0
    }
}

fun Context.getHumanizedFilename(path: String): String {
    val humanized = humanizePath(path)
    return humanized.substring(humanized.lastIndexOf("/") + 1)
}

fun Context.getHumanReadablePath(path: String): String {
    return getString(
        when (path) {
            "/" -> R.string.root
            internalStoragePath -> R.string.internal
            otgPath -> R.string.usb
            else -> R.string.sd_card
        }
    )
}

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

fun Context.getLaunchIntent() = packageManager.getLaunchIntentForPackage(baseConfig.appId)

fun Context.getLinkTextColor(): Int {
    return if (baseConfig.primaryColor == resources.getColor(R.color.color_primary)) {
        baseConfig.primaryColor
    } else {
        baseConfig.textColor
    }
}

fun Context.getMediaContentUri(path: String): Uri? {
    val uri = when {
        path.isImageFast() -> Images.Media.EXTERNAL_CONTENT_URI
        path.isVideoFast() -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        else -> Files.getContentUri("external")
    }

    return getMediaContent(path, uri)
}

fun Context.getPermissionString(id: Int) = when (id) {
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
    PERMISSION_MEDIA_LOCATION -> if (isQPlus()) Manifest.permission.ACCESS_MEDIA_LOCATION else ""
    else -> ""
}

fun Context.getPicturesDirectoryPath(fullPath: String): String {
    val basePath = fullPath.getBasePath(this)
    return File(basePath, Environment.DIRECTORY_PICTURES).absolutePath
}

fun Context.getSAFOnlyDirs(): List<String> {
    return DIRS_ACCESSIBLE_ONLY_WITH_SAF.map { "$internalStoragePath$it" } +
            DIRS_ACCESSIBLE_ONLY_WITH_SAF.map { "$sdCardPath$it" }
}

fun Context.getSAFStorageId(fullPath: String): String {
    return if (fullPath.startsWith('/')) {
        when {
            fullPath.startsWith(internalStoragePath) -> "primary"
            else -> fullPath.substringAfter("/storage/", "").substringBefore('/')
        }
    } else {
        fullPath.substringBefore(':', "").substringAfterLast('/')
    }
}

fun Context.getSizeFromContentUri(uri: Uri): Long {
    val projection = arrayOf(OpenableColumns.SIZE)
    try {
        val cursor = contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (cursor.moveToFirst()) {
                return cursor.getLongValue(OpenableColumns.SIZE)
            }
        }
    } catch (e: Exception) {
    }
    return 0L
}

fun Context.getSomeDocumentSdk30(path: String): DocumentFile? =
    getFastDocumentSdk30(path) ?: getDocumentSdk30(path)

fun Context.getStorageRootIdForAndroidDir(path: String) =
    getAndroidTreeUri(path).removeSuffix(
        if (isAndroidDataDir(
                path
            )
        ) "%3AAndroid%2Fdata" else "%3AAndroid%2Fobb"
    ).substringAfterLast('/').trimEnd('/')

fun Context.getTimeFormat() = if (baseConfig.use24HourFormat) TIME_FORMAT_24 else TIME_FORMAT_12

fun Context.getUriMimeType(path: String, newUri: Uri): String {
    var mimeType = path.getMimeType()
    if (mimeType.isEmpty()) {
        mimeType = getMimeTypeFromUri(newUri)
    }
    return mimeType
}

fun Context.getMimeTypeFromUri(uri: Uri): String {
    var mimetype = uri.path?.getMimeType() ?: ""
    if (mimetype.isEmpty()) {
        try {
            mimetype = contentResolver.getType(uri) ?: ""
        } catch (_: IllegalStateException) {
        }
    }
    return mimetype
}

fun Context.getTimePickerDialogTheme() = when {
    baseConfig.isUsingSystemTheme -> if (isUsingSystemDarkTheme()) {
        R.style.MyTimePickerMaterialTheme_Dark
    } else {
        R.style.MyDateTimePickerMaterialTheme
    }
    baseConfig.backgroundColor.getContrastColor() == Color.WHITE -> R.style.MyDialogTheme_Dark
    else -> R.style.MyDialogTheme
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
    val basePath = path.getBasePath(this)
    return when (basePath) {
        "/" -> "${getHumanReadablePath(basePath)}$trimmedPath"
        else -> trimmedPath.replaceFirst(basePath, getHumanReadablePath(basePath))
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
        DIRS_INACCESSIBLE_WITH_SAF_SDK_30.all { !firstParentDir.equals(it, true) }
    return isRPlus() && isValidName && isDirectory && isAnAccessibleDirectory
}

fun Context.isAStorageRootFolder(path: String): Boolean {
    val trimmed = path.trimEnd('/')
    return trimmed.isEmpty() || trimmed.equals(internalStoragePath, true) || trimmed.equals(
        sdCardPath,
        true
    ) || trimmed.equals(otgPath, true)
}

fun Context.isBlackAndWhiteTheme() =
    baseConfig.textColor == Color.WHITE && baseConfig.primaryColor == Color.BLACK && baseConfig.backgroundColor == Color.BLACK

fun Context.isInAndroidDir(path: String): Boolean {
    if (path.startsWith(recycleBinPath)) {
        return false
    }
    val firstParentDir = path.getFirstParentDirName(this, 0)
    return firstParentDir.equals(ANDROID_DIR, true)
}

fun Context.isPathOnInternalStorage(path: String) =
    internalStoragePath.isNotEmpty() && path.startsWith(internalStoragePath)

fun Context.isPathOnSD(path: String) = sdCardPath.isNotEmpty() && path.startsWith(sdCardPath)

fun Context.isSDCardSetAsDefaultStorage() =
    sdCardPath.isNotEmpty() && Environment.getExternalStorageDirectory().absolutePath.equals(
        sdCardPath,
        true
    )

fun Context.isWhiteTheme() =
    baseConfig.textColor == DARK_GREY && baseConfig.primaryColor == Color.WHITE && baseConfig.backgroundColor == Color.WHITE

val Context.widgetsDB: WidgetsDao
    get() = GalleryDatabase.getInstance(applicationContext).WidgetsDao()

val Context.mediaDB: MediumDao get() = GalleryDatabase.getInstance(applicationContext).MediumDao()

val Context.navigationBarBottom: Boolean get() = usableScreenSize.y < realScreenSize.y

val Context.navigationBarHeight: Int get() = if (navigationBarBottom && navigationBarSize.y != usableScreenSize.y) navigationBarSize.y else 0

val Context.navigationBarRight: Boolean get() = usableScreenSize.x < realScreenSize.x && usableScreenSize.x > usableScreenSize.y

val Context.navigationBarSize: Point
    get() = when {
        navigationBarRight -> Point(newNavigationBarHeight, usableScreenSize.y)
        navigationBarBottom -> Point(usableScreenSize.x, newNavigationBarHeight)
        else -> Point()
    }

val Context.navigationBarWidth: Int get() = if (navigationBarRight) navigationBarSize.x else 0

// no need to use DocumentFile if an SD card is set as the default storage
fun Context.needsStupidWritePermissions(path: String) =
    !isRPlus() && (isPathOnSD(path) || isPathOnOTG(path)) && !isSDCardSetAsDefaultStorage()

val Context.newNavigationBarHeight: Int
    get() {
        var navigationBarHeight = 0
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0) {
            navigationBarHeight = resources.getDimensionPixelSize(resourceId)
        }
        return navigationBarHeight
    }

val Context.otgPath: String get() = baseConfig.OTGPath

val Context.directoryDao: DirectoryDao
    get() = GalleryDatabase.getInstance(applicationContext).DirectoryDao()

val Context.favoritesDB: FavoritesDao
    get() = GalleryDatabase.getInstance(applicationContext).FavoritesDao()

val Context.dateTakensDB: DateTakensDao
    get() = GalleryDatabase.getInstance(applicationContext).DateTakensDao()

fun Context.isAProApp() = true

fun Context.isInDownloadDir(path: String): Boolean {
    if (path.startsWith(recycleBinPath)) {
        return false
    }
    val firstParentDir = path.getFirstParentDirName(this, 0)
    return firstParentDir.equals(DOWNLOAD_DIR, true)
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
            firstParentDir.startsWith(DOWNLOAD_DIR, true)
        val hasAtLeast1PathSegment = firstParentDir.split("/").filter { it.isNotEmpty() }.size > 1
        val firstParentPath = path.getFirstParentPath(this, 1)
        startsWithDownloadDir && hasAtLeast1PathSegment && File(firstParentPath).isDirectory
    }
}

fun Context.isPackageInstalled(pkgName: String): Boolean {
    return try {
        packageManager.getPackageInfo(pkgName, 0)
        true
    } catch (e: Exception) {
        false
    }
}

fun Context.isPathOnOTG(path: String) = otgPath.isNotEmpty() && path.startsWith(otgPath)

fun Context.isRestrictedSAFOnlyRoot(path: String): Boolean {
    return isRPlus() && isSAFOnlyRoot(path)
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
        DIRS_INACCESSIBLE_WITH_SAF_SDK_30.any {
            firstParentDir.equals(
                it,
                true
            )
        }
    return isRPlus() && (isInvalidName || (isDirectory && isARestrictedDirectory))
}

val Context.isRTLLayout: Boolean get() = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL

fun Context.isSAFOnlyRoot(path: String): Boolean {
    return getSAFOnlyDirs().any { "${path.trimEnd('/')}/".startsWith(it) }
}

fun Context.isThankYouInstalled() = true

fun Context.isOrWasThankYouInstalled(): Boolean {
    return when {
        resources.getBoolean(R.bool.pretend_thank_you_installed) -> true
        baseConfig.hadThankYouInstalled -> true
        isThankYouInstalled() -> {
            baseConfig.hadThankYouInstalled = true
            true
        }
        else -> false
    }
}

val Context.recycleBin: File get() = filesDir

fun Context.launchActivityIntent(intent: Intent) {
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        toast(R.string.no_app_found)
    } catch (e: Exception) {
        showErrorToast(e)
    }
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

// format day bits to strings like "Mon, Tue, Wed"
fun Context.getSelectedDaysString(bitMask: Int): String {
    val dayBits = arrayListOf(
        MONDAY_BIT,
        TUESDAY_BIT,
        WEDNESDAY_BIT,
        THURSDAY_BIT,
        FRIDAY_BIT,
        SATURDAY_BIT,
        SUNDAY_BIT
    )
    val weekDays =
        resources.getStringArray(R.array.week_days_short).toList() as java.util.ArrayList<String>

    if (baseConfig.isSundayFirst) {
        dayBits.moveLastItemToFront()
        weekDays.moveLastItemToFront()
    }

    var days = ""
    dayBits.forEachIndexed { index, bit ->
        if (bitMask and bit != 0) {
            days += "${weekDays[index]}, "
        }
    }
    return days.trim().trimEnd(',')
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

        dirs.mapTo(newDirsOrdered, { it })
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
                    o1.sortValue.normalizeString().toLowerCase(Locale.ROOT)
                        .compareTo(o2.sortValue.normalizeString().toLowerCase(Locale.ROOT))
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
        if (isMarshmallowPlus()) {
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

        val parentDirs = getDirectParentSubfolders(dirs, currentPathPrefix)
        updateSubfolderCounts(dirs, parentDirs)

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

fun Context.getDirectParentSubfolders(
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
                val isSortingAscending = config.sorting.isSortingAscending()
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
                        getFolderNameFromPath(parent),
                        subDirs.sumBy { it.mediaCnt },
                        lastModified,
                        dateTaken,
                        subDirs.sumByLong { it.size },
                        getPathLocation(parent),
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
        getDirectParentSubfolders(dirsToShow, currentPathPrefix)
    } else {
        dirsToShow
    }
}

fun Context.getFastDocumentFile(path: String): DocumentFile? {
    if (isPathOnOTG(path)) {
        return getOTGFastDocumentFile(path)
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

fun Context.getFontSizeText() = getString(
    when (baseConfig.fontSize) {
        FONT_SIZE_SMALL -> R.string.small
        FONT_SIZE_MEDIUM -> R.string.medium
        FONT_SIZE_LARGE -> R.string.large
        else -> R.string.extra_large
    }
)

fun Context.updateSDCardPath() {
    ensureBackgroundThread {
        val oldPath = baseConfig.sdCardPath
        baseConfig.sdCardPath = getSDCardPath()
        if (oldPath != baseConfig.sdCardPath) {
            baseConfig.sdTreeUri = ""
        }
    }
}

fun Context.updateSubfolderCounts(
    children: ArrayList<Directory>,
    parentDirs: ArrayList<Directory>
) {
    for (child in children) {
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
                ) || children.any { it.path.equals(File(child.path).parent, true) }
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
}

fun Context.getNoMediaFolders(callback: (folders: ArrayList<String>) -> Unit) {
    ensureBackgroundThread {
        callback(getNoMediaFoldersSync())
    }
}

fun Context.getNoMediaFoldersSync(): ArrayList<String> {
    val folders = ArrayList<String>()

    val uri = Files.getContentUri("external")
    val projection = arrayOf(Files.FileColumns.DATA)
    val selection = "${Files.FileColumns.MEDIA_TYPE} = ? AND ${Files.FileColumns.TITLE} LIKE ?"
    val selectionArgs = arrayOf(Files.FileColumns.MEDIA_TYPE_NONE.toString(), "%$NOMEDIA%")
    val sortOrder = "${Files.FileColumns.DATE_MODIFIED} DESC"
    val OTGPath = config.OTGPath

    var cursor: Cursor? = null
    try {
        cursor = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)
        if (cursor?.moveToFirst() == true) {
            do {
                val path = cursor.getStringValue(Files.FileColumns.DATA) ?: continue
                val noMediaFile = File(path)
                if (getDoesFilePathExist(
                        noMediaFile.absolutePath,
                        OTGPath
                    ) && noMediaFile.name == NOMEDIA
                ) {
                    folders.add(noMediaFile.parent)
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
    ensureBackgroundThread {
        rescanFolderMediaSync(path)
    }
}

fun Context.rescanFolderMediaSync(path: String) {
    getCachedMedia(path) { cached ->
        GetMediaAsynctask(
            applicationContext,
            path,
            isPickImage = false,
            isPickVideo = false,
            showAll = false
        ) { newMedia ->
            ensureBackgroundThread {
                val media = newMedia.filterIsInstance<Medium>() as ArrayList<Medium>
                try {
                    mediaDB.insertAll(media)

                    cached.forEach { thumbnailItem ->
                        if (!newMedia.contains(thumbnailItem)) {
                            val mediumPath = (thumbnailItem as? Medium)?.path
                            if (mediumPath != null) {
                                deleteDBPath(mediumPath)
                            }
                        }
                    }
                } catch (ignored: Exception) {
                }
            }
        }.execute()
    }
}

fun Context.storeDirectoryItems(items: ArrayList<Directory>) {
    ensureBackgroundThread {
        directoryDao.insertAll(items)
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
        sdCardPath -> getString(R.string.sd_card)
        otgPath -> getString(R.string.usb)
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
    if (type == TYPE_IMAGES || type == TYPE_VIDEOS || type == TYPE_RAWS || type == TYPE_PORTRAITS) {
        if (type == TYPE_IMAGES && path.isPng()) {
            loadPng(path, target, cropThumbnails, roundCorners, signature, skipMemoryCacheAtPaths)
        } else {
            loadJpg(path, target, cropThumbnails, roundCorners, signature, skipMemoryCacheAtPaths)
        }
    } else if (type == TYPE_GIFS) {
        if (!animateGifs) {
            loadStaticGIF(
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
                path,
                target,
                cropThumbnails,
                roundCorners,
                signature,
                skipMemoryCacheAtPaths
            )
        } catch (e: OutOfMemoryError) {
            loadStaticGIF(
                path,
                target,
                cropThumbnails,
                roundCorners,
                signature,
                skipMemoryCacheAtPaths
            )
        }
    } else if (type == TYPE_SVGS) {
        loadSVG(path, target, cropThumbnails, roundCorners, signature)
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
            getPathLocation(tempFolderPath),
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

fun Context.getPathLocation(path: String): Int {
    return when {
        isPathOnSD(path) -> LOCATION_SD
        isPathOnOTG(path) -> LOCATION_OTG
        else -> LOCATION_INTERNAL
    }
}

fun Context.loadPng(
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
    var builder = Glide.with(applicationContext)
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
                tryLoadingWithPicasso(path, target, cropThumbnails, roundCorners, signature)
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
        val cornerRadius = resources.getDimension(cornerSize).toInt()
        builder = builder.transform(CenterCrop(), RoundedCorners(cornerRadius))
    }

    builder.into(target)
}

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

fun Context.loadStaticGIF(
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
        .asBitmap() // make sure the GIF wont animate
        .load(path)
        .apply(options)

    if (roundCorners != ROUNDED_CORNERS_NONE) {
        val cornerSize =
            if (roundCorners == ROUNDED_CORNERS_SMALL) R.dimen.rounded_corner_radius_small else R.dimen.rounded_corner_radius_big
        val cornerRadius = resources.getDimension(cornerSize).toInt()
        builder = builder.transform(CenterCrop(), RoundedCorners(cornerRadius))
    }

    builder.into(target)
}

fun Context.loadSVG(
    path: String,
    target: MySquareImageView,
    cropThumbnails: Boolean,
    roundCorners: Int,
    signature: ObjectKey
) {
    target.scaleType =
        if (cropThumbnails) ImageView.ScaleType.CENTER_CROP else ImageView.ScaleType.FIT_CENTER

    val options = RequestOptions().signature(signature)
    var builder = Glide.with(applicationContext)
        .`as`(PictureDrawable::class.java)
        .listener(SvgSoftwareLayerSetter())
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

// intended mostly for Android 11 issues, that fail loading PNG files bigger than 10 MB
fun Context.tryLoadingWithPicasso(
    path: String,
    view: MySquareImageView,
    cropThumbnails: Boolean,
    roundCorners: Int,
    signature: ObjectKey
) {
    var pathToLoad = "file://$path"
    pathToLoad = pathToLoad.replace("%", "%25").replace("#", "%23")

    try {
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
            val cornerRadius = resources.getDimension(cornerSize).toInt()
            builder = builder.transform(PicassoRoundedCornersTransformation(cornerRadius.toFloat()))
        }

        builder.into(view)
    } catch (e: Exception) {
    }
}

fun Context.formatMinutesToTimeString(totalMinutes: Int) =
    formatSecondsToTimeString(totalMinutes * 60)

fun Context.formatSecondsToTimeString(totalSeconds: Int): String {
    val days = totalSeconds / DAY_SECONDS
    val hours = (totalSeconds % DAY_SECONDS) / HOUR_SECONDS
    val minutes = (totalSeconds % HOUR_SECONDS) / MINUTE_SECONDS
    val seconds = totalSeconds % MINUTE_SECONDS
    val timesString = StringBuilder()
    if (days > 0) {
        val daysString = String.format(resources.getQuantityString(R.plurals.days, days, days))
        timesString.append("$daysString, ")
    }

    if (hours > 0) {
        val hoursString = String.format(resources.getQuantityString(R.plurals.hours, hours, hours))
        timesString.append("$hoursString, ")
    }

    if (minutes > 0) {
        val minutesString =
            String.format(resources.getQuantityString(R.plurals.minutes, minutes, minutes))
        timesString.append("$minutesString, ")
    }

    if (seconds > 0) {
        val secondsString =
            String.format(resources.getQuantityString(R.plurals.seconds, seconds, seconds))
        timesString.append(secondsString)
    }

    var result = timesString.toString().trim().trimEnd(',')
    if (result.isEmpty()) {
        result = String.format(resources.getQuantityString(R.plurals.minutes, 0, 0))
    }
    return result
}

fun Context.getCachedDirectories(
    getVideosOnly: Boolean = false,
    getImagesOnly: Boolean = false,
    forceShowHidden: Boolean = false,
    callback: (ArrayList<Directory>) -> Unit
) {
    ensureBackgroundThread {
        try {
            Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE)
        } catch (ignored: Exception) {
        }

        val directories = try {
            directoryDao.getAll() as ArrayList<Directory>
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
            getVideosOnly -> filteredDirectories.filter { it.types and TYPE_VIDEOS != 0 }
            getImagesOnly -> filteredDirectories.filter { it.types and TYPE_IMAGES != 0 }
            else -> filteredDirectories.filter {
                (filterMedia and TYPE_IMAGES != 0 && it.types and TYPE_IMAGES != 0) ||
                        (filterMedia and TYPE_VIDEOS != 0 && it.types and TYPE_VIDEOS != 0) ||
                        (filterMedia and TYPE_GIFS != 0 && it.types and TYPE_GIFS != 0) ||
                        (filterMedia and TYPE_RAWS != 0 && it.types and TYPE_RAWS != 0) ||
                        (filterMedia and TYPE_SVGS != 0 && it.types and TYPE_SVGS != 0) ||
                        (filterMedia and TYPE_PORTRAITS != 0 && it.types and TYPE_PORTRAITS != 0)
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
    ensureBackgroundThread {
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

        if (config.filterMedia and TYPE_PORTRAITS != 0) {
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
            getVideosOnly -> media.filter { it.type == TYPE_VIDEOS }
            getImagesOnly -> media.filter { it.type == TYPE_IMAGES }
            else -> media.filter {
                (filterMedia and TYPE_IMAGES != 0 && it.type == TYPE_IMAGES) ||
                        (filterMedia and TYPE_VIDEOS != 0 && it.type == TYPE_VIDEOS) ||
                        (filterMedia and TYPE_GIFS != 0 && it.type == TYPE_GIFS) ||
                        (filterMedia and TYPE_RAWS != 0 && it.type == TYPE_RAWS) ||
                        (filterMedia and TYPE_SVGS != 0 && it.type == TYPE_SVGS) ||
                        (filterMedia and TYPE_PORTRAITS != 0 && it.type == TYPE_PORTRAITS)
            }
        }) as ArrayList<Medium>

        val pathToUse = if (path.isEmpty()) SHOW_ALL else path
        mediaFetcher.sortMedia(media, config.getFolderSorting(pathToUse))
        val grouped = mediaFetcher.groupMedia(media, pathToUse)
        callback(grouped.clone() as ArrayList<ThumbnailItem>)
        val OTGPath = config.OTGPath

        try {
            val mediaToDelete = ArrayList<Medium>()
            // creating a new thread intentionally, do not reuse the common background thread
            Thread {
                media.filter { !getDoesFilePathExist(it.path, OTGPath) }.forEach {
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
                            favoritesDB.deleteFavoritePath(it.path)
                        }
                    } catch (ignored: Exception) {
                    }
                }
            }.start()
        } catch (ignored: Exception) {
        }
    }
}

fun Context.getFormattedMinutes(minutes: Int, showBefore: Boolean = true) =
    getFormattedSeconds(if (minutes == -1) minutes else minutes * 60, showBefore)

fun Context.getFormattedSeconds(seconds: Int, showBefore: Boolean = true) = when (seconds) {
    -1 -> getString(R.string.no_reminder)
    0 -> getString(R.string.at_start)
    else -> {
        when {
            seconds < 0 && seconds > -60 * 60 * 24 -> {
                val minutes = -seconds / 60
                getString(R.string.during_day_at).format(minutes / 60, minutes % 60)
            }
            seconds % YEAR_SECONDS == 0 -> {
                val base = if (showBefore) R.plurals.years_before else R.plurals.by_years
                resources.getQuantityString(base, seconds / YEAR_SECONDS, seconds / YEAR_SECONDS)
            }
            seconds % MONTH_SECONDS == 0 -> {
                val base = if (showBefore) R.plurals.months_before else R.plurals.by_months
                resources.getQuantityString(base, seconds / MONTH_SECONDS, seconds / MONTH_SECONDS)
            }
            seconds % WEEK_SECONDS == 0 -> {
                val base = if (showBefore) R.plurals.weeks_before else R.plurals.by_weeks
                resources.getQuantityString(base, seconds / WEEK_SECONDS, seconds / WEEK_SECONDS)
            }
            seconds % DAY_SECONDS == 0 -> {
                val base = if (showBefore) R.plurals.days_before else R.plurals.by_days
                resources.getQuantityString(base, seconds / DAY_SECONDS, seconds / DAY_SECONDS)
            }
            seconds % HOUR_SECONDS == 0 -> {
                val base = if (showBefore) R.plurals.hours_before else R.plurals.by_hours
                resources.getQuantityString(base, seconds / HOUR_SECONDS, seconds / HOUR_SECONDS)
            }
            seconds % MINUTE_SECONDS == 0 -> {
                val base = if (showBefore) R.plurals.minutes_before else R.plurals.by_minutes
                resources.getQuantityString(
                    base,
                    seconds / MINUTE_SECONDS,
                    seconds / MINUTE_SECONDS
                )
            }
            else -> {
                val base = if (showBefore) R.plurals.seconds_before else R.plurals.by_seconds
                resources.getQuantityString(base, seconds, seconds)
            }
        }
    }
}

fun Context.removeInvalidDBDirectories(dirs: ArrayList<Directory>? = null) {
    val dirsToCheck = dirs ?: directoryDao.getAll()
    val OTGPath = config.OTGPath
    dirsToCheck.filter {
        !it.areFavorites() && !it.isRecycleBin() && !getDoesFilePathExist(
            it.path,
            OTGPath
        ) && it.path != config.tempFolderPath
    }.forEach {
        try {
            directoryDao.deleteDirPath(it.path)
        } catch (ignored: Exception) {
        }
    }
}

fun Context.tryFastDocumentDelete(path: String, allowDeleteFolder: Boolean): Boolean {
    val document = getFastDocumentFile(path)
    return if (document?.isFile == true || allowDeleteFolder) {
        try {
            DocumentsContract.deleteDocument(contentResolver, document?.uri!!)
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
    var fileDeleted = tryFastDocumentDelete(fileDirItem.path, allowDeleteFolder)
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
        favoritesDB.updateFavorite(newFilename, newPath, newParentPath, oldPath)
    } catch (ignored: Exception) {
    }
}

fun Context.updateDBDirectory(directory: Directory) {
    try {
        directoryDao.updateDirectory(
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
        showErrorToast(e)
    }
}

fun Context.getOTGFastDocumentFile(path: String, otgPathToUse: String? = null): DocumentFile? {
    if (baseConfig.OTGTreeUri.isEmpty()) {
        return null
    }

    val otgPath = otgPathToUse ?: baseConfig.OTGPath
    if (baseConfig.OTGPartition.isEmpty()) {
        baseConfig.OTGPartition =
            baseConfig.OTGTreeUri.removeSuffix("%3A").substringAfterLast('/').trimEnd('/')
        updateOTGPathFromPartition()
    }

    val relativePath = Uri.encode(path.substring(otgPath.length).trim('/'))
    val fullUri = "${baseConfig.OTGTreeUri}/document/${baseConfig.OTGPartition}%3A$relativePath"
    return DocumentFile.fromSingleUri(this, Uri.parse(fullUri))
}

fun Context.getOTGFolderChildren(path: String) = getDocumentFile(path)?.listFiles()

fun Context.getOTGFolderChildrenNames(path: String) =
    getOTGFolderChildren(path)?.map { it.name }?.toMutableList()

fun Context.getFavoritePaths(): ArrayList<String> {
    return try {
        favoritesDB.getValidFavoritePaths() as ArrayList<String>
    } catch (e: Exception) {
        ArrayList()
    }
}

fun Context.getFavoriteFromPath(path: String) =
    Favorite(null, path, path.getFilenameFromPath(), path.getParentPath())

// Convert paths like /storage/emulated/0/Pictures/Screenshots/first.jpg to content://media/external/images/media/131799
// so that we can refer to the file in the MediaStore.
// If we found no mediastore uri for a given file, do not return its path either to avoid some mismatching
fun Context.getUrisPathsFromFileDirItems(fileDirItems: List<FileDirItem>): Pair<java.util.ArrayList<String>, java.util.ArrayList<Uri>> {
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

val Context.notificationManager: NotificationManager get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

val Context.portrait get() = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT

fun Context.updateFavorite(path: String, isFavorite: Boolean) {
    try {
        if (isFavorite) {
            favoritesDB.insert(getFavoriteFromPath(path))
        } else {
            favoritesDB.deleteFavoritePath(path)
        }
    } catch (e: Exception) {
        toast(R.string.unknown_error_occurred)
    }
}

fun Context.toast(id: Int, length: Int = Toast.LENGTH_SHORT) {
    toast(getString(id), length)
}

fun Context.toast(msg: String, length: Int = Toast.LENGTH_SHORT) {
    try {
        if (isOnMainThread()) {
            doToast(this, msg, length)
        } else {
            Handler(Looper.getMainLooper()).post {
                doToast(this, msg, length)
            }
        }
    } catch (_: Exception) {
    }
}

private fun doToast(context: Context, message: String, length: Int) {
    if (context is Activity) {
        if (!context.isFinishing && !context.isDestroyed) {
            Toast.makeText(context, message, length).show()
        }
    } else {
        Toast.makeText(context, message, length).show()
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
        isRestrictedSAFOnlyRoot(path) -> getFastAndroidSAFDocument(path)?.isDirectory ?: false
        isPathOnOTG(path) -> getOTGFastDocumentFile(path)?.isDirectory ?: false
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
    deleteMediumWithPath(path.replaceFirst(recycleBinPath, RECYCLE_BIN))
}

fun Context.deleteMediumWithPath(path: String) {
    try {
        mediaDB.deleteMediumPath(path)
    } catch (ignored: Exception) {
    }
}

fun Context.updateWidgets() {
    val widgetIDs = AppWidgetManager.getInstance(applicationContext)
        ?.getAppWidgetIds(ComponentName(applicationContext, MyWidgetProvider::class.java))
        ?: return
    if (widgetIDs.isNotEmpty()) {
        Intent(applicationContext, MyWidgetProvider::class.java).apply {
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
    val FILE_CHANNEL_CONTAINERS = arrayListOf("moov", "trak", "mdia", "minf", "udta", "stbl")
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

            if (FILE_CHANNEL_CONTAINERS.contains(type)) {
                parseFileChannel(path, fc, level + 1, begin + 8, newEnd, callback)
            }

            fc.position(newEnd)
        }
    } catch (ignored: Exception) {
    }
}

fun Context.addPathToDB(path: String) {
    ensureBackgroundThread {
        if (!getDoesFilePathExist(path)) {
            return@ensureBackgroundThread
        }

        val type = when {
            path.isVideoFast() -> TYPE_VIDEOS
            path.isGif() -> TYPE_GIFS
            path.isRawFast() -> TYPE_RAWS
            path.isSvg() -> TYPE_SVGS
            path.isPortrait() -> TYPE_PORTRAITS
            else -> TYPE_IMAGES
        }

        try {
            val isFavorite = favoritesDB.isFavorite(path)
            val videoDuration = if (type == TYPE_VIDEOS) getDuration(path) ?: 0 else 0
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
    val storageId = getSAFStorageId(fullPath)

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
    val OTGPath = config.OTGPath
    val grouped = MediaFetcher(this).groupMedia(curMedia, path)
    var thumbnail: String? = null

    albumCovers.forEach {
        if (it.path == path && getDoesFilePathExist(it.tmb, OTGPath)) {
            thumbnail = it.tmb
        }
    }

    if (thumbnail == null) {
        val sortedMedia = grouped.filter { it is Medium }.toMutableList() as ArrayList<Medium>
        thumbnail = sortedMedia.firstOrNull { getDoesFilePathExist(it.path, OTGPath) }?.path ?: ""
    }

    if (config.OTGPath.isNotEmpty() && thumbnail!!.startsWith(config.OTGPath)) {
        thumbnail = thumbnail!!.getOTGPublicPath(applicationContext)
    }

    val isSortingAscending = config.directorySorting.isSortingAscending()
    val defaultMedium = Medium(0, "", "", "", 0L, 0L, 0L, 0, 0, false, 0L, 0L)
    val firstItem = curMedia.firstOrNull() ?: defaultMedium
    val lastItem = curMedia.lastOrNull() ?: defaultMedium
    val dirName = checkAppendingHidden(path, hiddenString, includedFolders, noMediaFolders)
    val lastModified =
        if (isSortingAscending) Math.min(firstItem.modified, lastItem.modified) else Math.max(
            firstItem.modified,
            lastItem.modified
        )
    val dateTaken = if (isSortingAscending) Math.min(
        firstItem.taken,
        lastItem.taken
    ) else Math.max(firstItem.taken, lastItem.taken)
    val size = if (getProperFileSize) curMedia.sumByLong { it.size } else 0L
    val mediaTypes = curMedia.getDirMediaTypes()
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
        getPathLocation(path),
        mediaTypes,
        sortValue
    )
}

fun Context.createFirstParentTreeUriUsingRootTree(fullPath: String): Uri {
    val storageId = getSAFStorageId(fullPath)
    val level = getFirstParentLevel(fullPath)
    val rootParentDirName = fullPath.getFirstParentDirName(this, level)
    val treeUri =
        DocumentsContract.buildTreeDocumentUri(EXTERNAL_STORAGE_PROVIDER_AUTHORITY, "$storageId:")
    val documentId = "${storageId}:$rootParentDirName"
    return DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
}


fun Context.getDataColumn(
    uri: Uri,
    selection: String? = null,
    selectionArgs: Array<String>? = null
): String? {
    try {
        val projection = arrayOf(Files.FileColumns.DATA)
        val cursor = contentResolver.query(uri, projection, selection, selectionArgs, null)
        cursor?.use {
            if (cursor.moveToFirst()) {
                val data = cursor.getStringValue(Files.FileColumns.DATA)
                if (data != "null") {
                    return data
                }
            }
        }
    } catch (e: Exception) {
    }
    return null
}

fun Context.getDatePickerDialogTheme() = when {
    baseConfig.isUsingSystemTheme -> R.style.MyDateTimePickerMaterialTheme
    baseConfig.backgroundColor.getContrastColor() == Color.WHITE -> R.style.MyDialogTheme_Dark
    else -> R.style.MyDialogTheme
}

fun Context.getDefaultAlarmSound(type: Int) =
    AlarmSound(0, getDefaultAlarmTitle(type), RingtoneManager.getDefaultUri(type).toString())

fun Context.getDefaultAlarmTitle(type: Int): String {
    val alarmString = getString(R.string.alarm)
    return try {
        RingtoneManager.getRingtone(this, RingtoneManager.getDefaultUri(type))?.getTitle(this)
            ?: alarmString
    } catch (e: Exception) {
        alarmString
    }
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

    val relevantMedium = if (sorting.isSortingAscending()) {
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
                return Math.round(cursor.getIntValue(MediaStore.MediaColumns.DURATION) / 1000.toDouble())
                    .toInt()
            }
        }
    } catch (ignored: Exception) {
    }

    return try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(path)
        Math.round(
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!
                .toInt() / 1000f
        )
    } catch (ignored: Exception) {
        null
    }
}

fun Context.getPopupMenuTheme(): Int {
    return if (isSPlus() && baseConfig.isUsingSystemTheme) {
        R.style.AppTheme_YouPopupMenuStyle
    } else if (isWhiteTheme()) {
        R.style.AppTheme_PopupMenuLightStyle
    } else {
        R.style.AppTheme_PopupMenuDarkStyle
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

fun Context.getProUrl() =
    "https://play.google.com/store/apps/details?id=${baseConfig.appId.removeSuffix(".debug")}.pro"

fun Context.getSAFDocumentId(path: String): String {
    val basePath = path.getBasePath(this)
    val relativePath = path.substring(basePath.length).trim('/')
    val storageId = getSAFStorageId(path)
    return "$storageId:$relativePath"
}

@SuppressLint("NewApi")
fun Context.getProperStatusBarColor() = when {
    baseConfig.isUsingSystemTheme -> resources.getColor(R.color.you_status_bar_color, theme)
    else -> baseConfig.primaryColor
}

fun Context.getStoreUrl() =
    "https://play.google.com/store/apps/details?id=${packageName.removeSuffix(".debug")}"

fun Context.getTitle(path: String): String? {
    val projection = arrayOf(
        MediaStore.MediaColumns.TITLE
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

fun Context.getTextSize() = when (baseConfig.fontSize) {
    FONT_SIZE_SMALL -> resources.getDimension(R.dimen.smaller_text_size)
    FONT_SIZE_MEDIUM -> resources.getDimension(R.dimen.bigger_text_size)
    FONT_SIZE_LARGE -> resources.getDimension(R.dimen.big_text_size)
    else -> resources.getDimension(R.dimen.extra_big_text_size)
}

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
            grouping and GROUP_BY_DATE_TAKEN_DAILY != 0 ||
            grouping and GROUP_BY_DATE_TAKEN_MONTHLY != 0

    val getProperLastModified = config.directorySorting and SORT_BY_DATE_MODIFIED != 0 ||
            sorting and SORT_BY_DATE_MODIFIED != 0 ||
            grouping and GROUP_BY_LAST_MODIFIED_DAILY != 0 ||
            grouping and GROUP_BY_LAST_MODIFIED_MONTHLY != 0

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
            is MyAppCompatSpinner -> it.setColors(textColor, accentColor, backgroundColor)
            is MySwitchCompat -> it.setColors(textColor, accentColor, backgroundColor)
            is MyCompatRadioButton -> it.setColors(textColor, accentColor, backgroundColor)
            is MyAppCompatCheckbox -> it.setColors(textColor, accentColor, backgroundColor)
            is MyEditText -> it.setColors(textColor, accentColor, backgroundColor)
            is MyAutoCompleteTextView -> it.setColors(textColor, accentColor, backgroundColor)
            is MyFloatingActionButton -> it.setColors(textColor, accentColor, backgroundColor)
            is MySeekBar -> it.setColors(textColor, accentColor, backgroundColor)
            is MyButton -> it.setColors(textColor, accentColor, backgroundColor)
            is MyTextInputLayout -> it.setColors(textColor, accentColor, backgroundColor)
            is ViewGroup -> updateTextColors(it)
        }
    }
}

fun Context.getAlbum(path: String): String? {
    val projection = arrayOf(
        MediaStore.Audio.Media.ALBUM
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

fun Context.getArtist(path: String): String? {
    val projection = arrayOf(
        MediaStore.Audio.Media.ARTIST
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

fun Context.getMyContactsCursor(favoritesOnly: Boolean, withPhoneNumbersOnly: Boolean) = try {
    val getFavoritesOnly = if (favoritesOnly) "1" else "0"
    val getWithPhoneNumbersOnly = if (withPhoneNumbersOnly) "1" else "0"
    val args = arrayOf(getFavoritesOnly, getWithPhoneNumbersOnly)
    CursorLoader(
        this,
        MyContactsContentProvider.CONTACTS_CONTENT_URI,
        null,
        null,
        args,
        null
    ).loadInBackground()
} catch (e: Exception) {
    null
}

// some helper functions were taken from https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
fun Context.getRealPathFromURI(uri: Uri): String? {
    if (uri.scheme == "file") {
        return uri.path
    }

    if (isDownloadsDocument(uri)) {
        val id = DocumentsContract.getDocumentId(uri)
        if (id.areDigitsOnly()) {
            val newUri = ContentUris.withAppendedId(
                Uri.parse("content://downloads/public_downloads"),
                id.toLong()
            )
            val path = getDataColumn(newUri)
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
        val type = split[0]

        val contentUri = when (type) {
            "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            else -> Images.Media.EXTERNAL_CONTENT_URI
        }

        val selection = "_id=?"
        val selectionArgs = arrayOf(split[1])
        val path = getDataColumn(contentUri, selection, selectionArgs)
        if (path != null) {
            return path
        }
    }

    return getDataColumn(uri)
}

fun Context.getSharedTheme(callback: (sharedTheme: SharedTheme?) -> Unit) {
    if (!isThankYouInstalled()) {
        callback(null)
    } else {
        val cursorLoader = getMyContentProviderCursorLoader()
        ensureBackgroundThread {
            callback(getSharedThemeSync(cursorLoader))
        }
    }
}

fun Context.getSharedThemeSync(cursorLoader: CursorLoader): SharedTheme? {
    val cursor = cursorLoader.loadInBackground()
    cursor?.use {
        if (cursor.moveToFirst()) {
            try {
                val textColor = cursor.getIntValue(MyContentProvider.COL_TEXT_COLOR)
                val backgroundColor = cursor.getIntValue(MyContentProvider.COL_BACKGROUND_COLOR)
                val primaryColor = cursor.getIntValue(MyContentProvider.COL_PRIMARY_COLOR)
                val accentColor = cursor.getIntValue(MyContentProvider.COL_ACCENT_COLOR)
                val appIconColor = cursor.getIntValue(MyContentProvider.COL_APP_ICON_COLOR)
                val navigationBarColor =
                    cursor.getIntValueOrNull(MyContentProvider.COL_NAVIGATION_BAR_COLOR)
                        ?: INVALID_NAVIGATION_BAR_COLOR
                val lastUpdatedTS = cursor.getIntValue(MyContentProvider.COL_LAST_UPDATED_TS)
                return SharedTheme(
                    textColor,
                    backgroundColor,
                    primaryColor,
                    appIconColor,
                    navigationBarColor,
                    lastUpdatedTS,
                    accentColor
                )
            } catch (e: Exception) {
            }
        }
    }
    return null
}

fun Context.getMediaStoreLastModified(path: String): Long {
    val projection = arrayOf(
        MediaStore.MediaColumns.DATE_MODIFIED
    )

    val uri = getFileUri(path)
    val selection = "${BaseColumns._ID} = ?"
    val selectionArgs = arrayOf(path.substringAfterLast("/"))

    try {
        val cursor = contentResolver.query(uri, projection, selection, selectionArgs, null)
        cursor?.use {
            if (cursor.moveToFirst()) {
                return cursor.getLongValue(MediaStore.MediaColumns.DATE_MODIFIED) * 1000
            }
        }
    } catch (ignored: Exception) {
    }
    return 0
}

fun Context.grantReadUriPermission(uriString: String) {
    try {
        // ensure custom reminder sounds play well
        grantUriPermission(
            "com.android.systemui",
            Uri.parse(uriString),
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
    } catch (ignored: Exception) {
    }
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

fun Context.hasProperStoredFirstParentUri(path: String): Boolean {
    val firstParentUri = createFirstParentTreeUri(path)
    return contentResolver.persistedUriPermissions.any { it.uri.toString() == firstParentUri.toString() }
}

fun Context.isFingerPrintSensorAvailable() = isMarshmallowPlus() && Reprint.isHardwarePresent()

fun Context.isUsingSystemDarkTheme() =
    resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES != 0

val Context.realScreenSize: Point
    get() {
        val size = Point()
        windowManager.defaultDisplay.getRealSize(size)
        return size
    }

fun Context.renameAndroidSAFDocument(oldPath: String, newPath: String): Boolean {
    return try {
        val treeUri = getAndroidTreeUri(oldPath).toUri()
        val documentId = createAndroidSAFDocumentId(oldPath)
        val parentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
        DocumentsContract.renameDocument(
            contentResolver,
            parentUri,
            newPath.getFilenameFromPath()
        ) != null
    } catch (e: IllegalStateException) {
        showErrorToast(e)
        false
    }
}

fun Context.renameDocumentSdk30(oldPath: String, newPath: String): Boolean {
    return try {
        val treeUri = createFirstParentTreeUri(oldPath)
        val documentId = getSAFDocumentId(oldPath)
        val parentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
        DocumentsContract.renameDocument(
            contentResolver,
            parentUri,
            newPath.getFilenameFromPath()
        ) != null
    } catch (e: IllegalStateException) {
        showErrorToast(e)
        false
    }
}

fun Context.rescanPath(path: String, callback: (() -> Unit)? = null) {
    rescanPaths(arrayListOf(path), callback)
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

fun Context.saveExifRotation(exif: ExifInterface, degrees: Int) {
    val orientation =
        exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    val orientationDegrees = (orientation.degreesFromOrientation() + degrees) % 360
    exif.setAttribute(ExifInterface.TAG_ORIENTATION, orientationDegrees.orientationFromDegrees())
    exif.saveAttributes()
}

@SuppressLint("Recycle")
@RequiresApi(Build.VERSION_CODES.N)
fun Context.saveImageRotation(path: String, degrees: Int): Boolean {
    if (!needsStupidWritePermissions(path)) {
        saveExifRotation(ExifInterface(path), degrees)
        return true
    } else if (isNougatPlus()) {
        val documentFile = getSomeDocumentFile(path)
        if (documentFile != null) {
            val parcelFileDescriptor = contentResolver.openFileDescriptor(documentFile.uri, "rw")
            val fileDescriptor = parcelFileDescriptor!!.fileDescriptor
            saveExifRotation(ExifInterface(fileDescriptor), degrees)
            return true
        }
    }
    return false
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

val Context.sdCardPath: String get() = baseConfig.sdCardPath

val Context.shortcutManager: ShortcutManager
    @SuppressLint("NewApi")
    get() = getSystemService(ShortcutManager::class.java) as ShortcutManager

fun Context.showErrorToast(msg: String, length: Int = Toast.LENGTH_LONG) {
    toast(String.format(getString(R.string.error), msg), length)
}

fun Context.showErrorToast(exception: Exception, length: Int = Toast.LENGTH_LONG) {
    showErrorToast(exception.toString(), length)
}

fun Context.storeNewYourAlarmSound(resultData: Intent): AlarmSound {
    val uri = resultData.data
    var filename = getFilenameFromUri(uri!!)
    if (filename.isEmpty()) {
        filename = getString(R.string.alarm)
    }

    val token = object : TypeToken<java.util.ArrayList<AlarmSound>>() {}.type
    val yourAlarmSounds =
        Gson().fromJson<java.util.ArrayList<AlarmSound>>(baseConfig.yourAlarmSounds, token)
            ?: java.util.ArrayList()
    val newAlarmSoundId =
        (yourAlarmSounds.maxByOrNull { it.id }?.id ?: YOUR_ALARM_SOUNDS_MIN_ID) + 1
    val newAlarmSound = AlarmSound(newAlarmSoundId, filename, uri.toString())
    if (yourAlarmSounds.firstOrNull { it.uri == uri.toString() } == null) {
        yourAlarmSounds.add(newAlarmSound)
    }

    baseConfig.yourAlarmSounds = Gson().toJson(yourAlarmSounds)

    val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    contentResolver.takePersistableUriPermission(uri, takeFlags)

    return newAlarmSound
}

val Context.telecomManager: TelecomManager get() = getSystemService(Context.TELECOM_SERVICE) as TelecomManager

fun Context.toggleAppIconColor(appId: String, colorIndex: Int, color: Int, enable: Boolean) {
    val className =
        "${appId.removeSuffix(".debug")}.activities.SplashActivity${appIconColorStrings[colorIndex]}"
    val state =
        if (enable) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED
    try {
        packageManager.setComponentEnabledSetting(
            ComponentName(appId, className),
            state,
            PackageManager.DONT_KILL_APP
        )
        if (enable) {
            baseConfig.lastIconColor = color
        }
    } catch (_: Exception) {
    }
}

val Context.windowManager: WindowManager get() = getSystemService(Context.WINDOW_SERVICE) as WindowManager

fun isAndroidDataDir(path: String): Boolean {
    val resolvedPath = "${path.trimEnd('/')}/"
    return resolvedPath.contains(ANDROID_DATA_DIR)
}

fun isExternalStorageManager(): Boolean {
    return isRPlus() && Environment.isExternalStorageManager()
}

fun isNotExternalStorageManager(): Boolean {
    return isRPlus() && !Environment.isExternalStorageManager()
}

fun Context.checkAppIconColor() {
    val appId = baseConfig.appId
    if (appId.isNotEmpty() && baseConfig.lastIconColor != baseConfig.appIconColor) {
        getAppIconColors().forEachIndexed { index, color ->
            toggleAppIconColor(appId, index, color, false)
        }

        getAppIconColors().forEachIndexed { index, color ->
            if (baseConfig.appIconColor == color) {
                toggleAppIconColor(appId, index, color, true)
            }
        }
    }
}

val Context.statusBarHeight: Int
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

val Context.usableScreenSize: Point
    get() {
        val size = Point()
        windowManager.defaultDisplay.getSize(size)
        return size
    }

fun Context.getCornerRadius() = resources.getDimension(R.dimen.rounded_corner_radius_small)

// we need the Default Dialer functionality only in Simple Dialer and in Simple Contacts for now
@TargetApi(Build.VERSION_CODES.M)
fun Context.isDefaultDialer(): Boolean {
    return if (!packageName.startsWith("com.simplemobiletools.contacts") && !packageName.startsWith(
            "com.simplemobiletools.dialer"
        )
    ) {
        true
    } else if ((packageName.startsWith("com.simplemobiletools.contacts") || packageName.startsWith("com.simplemobiletools.dialer")) && isQPlus()) {
        val roleManager = getSystemService(RoleManager::class.java)
        roleManager!!.isRoleAvailable(RoleManager.ROLE_DIALER) && roleManager.isRoleHeld(RoleManager.ROLE_DIALER)
    } else {
        isMarshmallowPlus() && telecomManager.defaultDialerPackage == packageName
    }
}

@TargetApi(Build.VERSION_CODES.N)
fun Context.getBlockedNumbers(): java.util.ArrayList<BlockedNumber> {
    val blockedNumbers = java.util.ArrayList<BlockedNumber>()
    if (!isNougatPlus() || !isDefaultDialer()) {
        return blockedNumbers
    }

    val uri = BlockedNumberContract.BlockedNumbers.CONTENT_URI
    val projection = arrayOf(
        BlockedNumberContract.BlockedNumbers.COLUMN_ID,
        BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER,
        BlockedNumberContract.BlockedNumbers.COLUMN_E164_NUMBER
    )

    queryCursor(uri, projection) { cursor ->
        val id = cursor.getLongValue(BlockedNumberContract.BlockedNumbers.COLUMN_ID)
        val number =
            cursor.getStringValue(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER) ?: ""
        val normalizedNumber =
            cursor.getStringValue(BlockedNumberContract.BlockedNumbers.COLUMN_E164_NUMBER) ?: number
        val comparableNumber = normalizedNumber.trimToComparableNumber()
        val blockedNumber = BlockedNumber(id, number, normalizedNumber, comparableNumber)
        blockedNumbers.add(blockedNumber)
    }

    return blockedNumbers
}

@TargetApi(Build.VERSION_CODES.N)
fun Context.addBlockedNumber(number: String) {
    ContentValues().apply {
        put(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER, number)
        put(
            BlockedNumberContract.BlockedNumbers.COLUMN_E164_NUMBER,
            PhoneNumberUtils.normalizeNumber(number)
        )
        try {
            contentResolver.insert(BlockedNumberContract.BlockedNumbers.CONTENT_URI, this)
        } catch (e: Exception) {
            showErrorToast(e)
        }
    }
}

@TargetApi(Build.VERSION_CODES.N)
fun Context.deleteBlockedNumber(number: String) {
    val selection = "${BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER} = ?"
    val selectionArgs = arrayOf(number)
    contentResolver.delete(
        BlockedNumberContract.BlockedNumbers.CONTENT_URI,
        selection,
        selectionArgs
    )
}

fun Context.isNumberBlocked(
    number: String,
    blockedNumbers: java.util.ArrayList<BlockedNumber> = getBlockedNumbers()
): Boolean {
    if (!isNougatPlus()) {
        return false
    }

    val numberToCompare = number.trimToComparableNumber()
    return blockedNumbers.map { it.numberToCompare }
        .contains(numberToCompare) || blockedNumbers.map { it.number }.contains(numberToCompare)
}

fun Context.copyToClipboard(text: String) {
    val clip = ClipData.newPlainText(getString(R.string.simple_commons), text)
    (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
    val toastText = String.format(getString(R.string.value_copied_to_clipboard_show), text)
    toast(toastText)
}

fun Context.getPhoneNumberTypeText(type: Int, label: String): String {
    return if (type == ContactsContract.CommonDataKinds.BaseTypes.TYPE_CUSTOM) {
        label
    } else {
        getString(
            when (type) {
                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE -> R.string.mobile
                ContactsContract.CommonDataKinds.Phone.TYPE_HOME -> R.string.home
                ContactsContract.CommonDataKinds.Phone.TYPE_WORK -> R.string.work
                ContactsContract.CommonDataKinds.Phone.TYPE_MAIN -> R.string.main_number
                ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK -> R.string.work_fax
                ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME -> R.string.home_fax
                ContactsContract.CommonDataKinds.Phone.TYPE_PAGER -> R.string.pager
                else -> R.string.other
            }
        )
    }
}

fun Context.updateBottomTabItemColors(view: View?, isActive: Boolean) {
    val color = if (isActive) {
        getProperPrimaryColor()
    } else {
        getProperTextColor()
    }

    view?.findViewById<ImageView>(R.id.tab_item_icon)?.applyColorFilter(color)
    view?.findViewById<TextView>(R.id.tab_item_label)?.setTextColor(color)
}

fun Context.getFolderLastModifieds(folder: String): java.util.HashMap<String, Long> {
    val lastModifieds = java.util.HashMap<String, Long>()
    val projection = arrayOf(
        Images.Media.DISPLAY_NAME,
        Images.Media.DATE_MODIFIED
    )

    val uri = Files.getContentUri("external")
    val selection =
        "${Images.Media.DATA} LIKE ? AND ${Images.Media.DATA} NOT LIKE ? AND ${Images.Media.MIME_TYPE} IS NOT NULL" // avoid selecting folders
    val selectionArgs = arrayOf("$folder/%", "$folder/%/%")

    try {
        val cursor = contentResolver.query(uri, projection, selection, selectionArgs, null)
        cursor?.use {
            if (cursor.moveToFirst()) {
                do {
                    try {
                        val lastModified = cursor.getLongValue(Images.Media.DATE_MODIFIED) * 1000
                        if (lastModified != 0L) {
                            val name = cursor.getStringValue(Images.Media.DISPLAY_NAME)
                            lastModifieds["$folder/$name"] = lastModified
                        }
                    } catch (e: Exception) {
                    }
                } while (cursor.moveToNext())
            }
        }
    } catch (e: Exception) {
    }

    return lastModifieds
}

fun getMediaStoreIds(context: Context): java.util.HashMap<String, Long> {
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
            } catch (e: Exception) {
            }
        }
    } catch (e: Exception) {
    }

    return ids
}

fun Context.getAndroidSAFFileSize(path: String): Long {
    val treeUri = getAndroidTreeUri(path).toUri()
    val documentId = createAndroidSAFDocumentId(path)
    return getFileSize(treeUri, documentId)
}

fun Context.getAndroidSAFFileCount(path: String, countHidden: Boolean): Int {
    val treeUri = getAndroidTreeUri(path).toUri()
    if (treeUri == Uri.EMPTY) {
        return 0
    }

    val documentId = createAndroidSAFDocumentId(path)
    val rootDocId = getStorageRootIdForAndroidDir(path)
    return getProperChildrenCount(rootDocId, treeUri, documentId, countHidden)
}

fun Context.getAndroidSAFLastModified(path: String): Long {
    val treeUri = getAndroidTreeUri(path).toUri()
    if (treeUri == Uri.EMPTY) {
        return 0L
    }

    val documentId = createAndroidSAFDocumentId(path)
    val projection = arrayOf(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
    val documentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
    return contentResolver.query(documentUri, projection, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            cursor.getLongValue(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
        } else {
            0L
        }
    } ?: 0L
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
        showErrorToast(e)
        callback?.invoke(false)
        storeAndroidTreeUri(path, "")
    }
}

fun Context.updateOTGPathFromPartition() {
    val otgPath = "/storage/${baseConfig.OTGPartition}"
    baseConfig.OTGPath = if (getOTGFastDocumentFile(otgPath, otgPath)?.exists() == true) {
        "/storage/${baseConfig.OTGPartition}"
    } else {
        "/mnt/media_rw/${baseConfig.OTGPartition}"
    }
}

// these functions update the mediastore instantly, MediaScannerConnection.scanFileRecursively takes some time to really get applied
fun Context.deleteFromMediaStore(path: String, callback: ((needsRescan: Boolean) -> Unit)? = null) {
    if (getIsPathDirectory(path)) {
        callback?.invoke(false)
        return
    }

    ensureBackgroundThread {
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

fun Context.rescanAndDeletePath(path: String, callback: () -> Unit) {
    val SCAN_FILE_MAX_DURATION = 1000L
    val scanFileHandler = Handler(Looper.getMainLooper())
    scanFileHandler.postDelayed({
        callback()
    }, SCAN_FILE_MAX_DURATION)

    MediaScannerConnection.scanFile(applicationContext, arrayOf(path), null) { path, uri ->
        scanFileHandler.removeCallbacksAndMessages(null)
        try {
            applicationContext.contentResolver.delete(uri, null, null)
        } catch (e: Exception) {
        }
        callback()
    }
}

fun Context.updateInMediaStore(oldPath: String, newPath: String) {
    ensureBackgroundThread {
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

fun Context.getOTGItems(
    path: String,
    shouldShowHidden: Boolean,
    getProperFileSize: Boolean,
    callback: (java.util.ArrayList<FileDirItem>) -> Unit
) {
    val items = java.util.ArrayList<FileDirItem>()
    val OTGTreeUri = baseConfig.OTGTreeUri
    var rootUri = try {
        DocumentFile.fromTreeUri(applicationContext, Uri.parse(OTGTreeUri))
    } catch (e: Exception) {
        showErrorToast(e)
        baseConfig.OTGPath = ""
        baseConfig.OTGTreeUri = ""
        baseConfig.OTGPartition = ""
        null
    }

    if (rootUri == null) {
        callback(items)
        return
    }

    val parts = path.split("/").dropLastWhile { it.isEmpty() }
    for (part in parts) {
        if (path == otgPath) {
            break
        }

        if (part == "otg:" || part == "") {
            continue
        }

        val file = rootUri!!.findFile(part)
        if (file != null) {
            rootUri = file
        }
    }

    val files = rootUri!!.listFiles().filter { it.exists() }

    val basePath = "${baseConfig.OTGTreeUri}/document/${baseConfig.OTGPartition}%3A"
    for (file in files) {
        val name = file.name ?: continue
        if (!shouldShowHidden && name.startsWith(".")) {
            continue
        }

        val isDirectory = file.isDirectory
        val filePath = file.uri.toString().substring(basePath.length)
        val decodedPath = otgPath + "/" + URLDecoder.decode(filePath, "UTF-8")
        val fileSize = when {
            getProperFileSize -> file.getItemSize(shouldShowHidden)
            isDirectory -> 0L
            else -> file.length()
        }

        val childrenCount = if (isDirectory) {
            file.listFiles().size
        } else {
            0
        }

        val lastModified = file.lastModified()
        val fileDirItem =
            FileDirItem(decodedPath, name, isDirectory, childrenCount, fileSize, lastModified)
        items.add(fileDirItem)
    }

    callback(items)
}

@RequiresApi(Build.VERSION_CODES.O)
fun Context.getAndroidSAFFileItems(
    path: String,
    shouldShowHidden: Boolean,
    getProperFileSize: Boolean = true,
    callback: (java.util.ArrayList<FileDirItem>) -> Unit
) {
    val items = java.util.ArrayList<FileDirItem>()
    val rootDocId = getStorageRootIdForAndroidDir(path)
    val treeUri = getAndroidTreeUri(path).toUri()
    val documentId = createAndroidSAFDocumentId(path)
    val childrenUri = try {
        DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, documentId)
    } catch (e: Exception) {
        showErrorToast(e)
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
                    val filePath = docId.substring("${getStorageRootIdForAndroidDir(path)}:".length)
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
        showErrorToast(e)
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
    val storageId = getStorageRootIdForAndroidDir(path)
    return "$storageId:$relativePath"
}

fun Context.getAndroidSAFDocument(path: String): DocumentFile? {
    val basePath = path.getBasePath(this)
    val androidPath = File(basePath, "Android").path
    var relativePath = path.substring(androidPath.length)
    if (relativePath.startsWith(File.separator)) {
        relativePath = relativePath.substring(1)
    }

    return try {
        val treeUri = getAndroidTreeUri(path).toUri()
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

fun Context.getSomeAndroidSAFDocument(path: String): DocumentFile? =
    getFastAndroidSAFDocument(path) ?: getAndroidSAFDocument(path)

fun Context.getFastAndroidSAFDocument(path: String): DocumentFile? {
    val treeUri = getAndroidTreeUri(path)
    if (treeUri.isEmpty()) {
        return null
    }

    val uri = getAndroidSAFUri(path)
    return DocumentFile.fromSingleUri(this, uri)
}

fun Context.getAndroidSAFChildrenUri(path: String): Uri {
    val treeUri = getAndroidTreeUri(path).toUri()
    val documentId = createAndroidSAFDocumentId(path)
    return DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, documentId)
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
        showErrorToast(e)
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
        showErrorToast(e)
        false
    }
}

// http://stackoverflow.com/a/40582634/1967672
fun Context.getSDCardPath(): String {
    val directories = getStorageDirectories().filter {
        !it.equals(internalStoragePath) && !it.equals(
            "/storage/emulated/0",
            true
        ) && (baseConfig.OTGPartition.isEmpty() || !it.endsWith(baseConfig.OTGPartition))
    }

    val fullSDpattern = Pattern.compile(SD_OTG_PATTERN)
    var sdCardPath = directories.firstOrNull { fullSDpattern.matcher(it).matches() }
        ?: directories.firstOrNull {
            !physicalPaths.contains(
                it.toLowerCase()
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
        val SDpattern = Pattern.compile(SD_OTG_SHORT)
        try {
            File("/storage").listFiles()?.forEach {
                if (SDpattern.matcher(it.name).matches()) {
                    sdCardPath = "/storage/${it.name}"
                }
            }
        } catch (e: Exception) {
        }
    }

    val finalPath = sdCardPath.trimEnd('/')
    baseConfig.sdCardPath = finalPath
    return finalPath
}

fun Context.hasExternalSDCard() = sdCardPath.isNotEmpty()

fun Context.hasProperStoredTreeUri(isOTG: Boolean): Boolean {
    val uri = if (isOTG) baseConfig.OTGTreeUri else baseConfig.sdTreeUri
    val hasProperUri = contentResolver.persistedUriPermissions.any { it.uri.toString() == uri }
    if (!hasProperUri) {
        if (isOTG) {
            baseConfig.OTGTreeUri = ""
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
        isPathOnOTG(path) -> if (isAndroidDataDir(path)) baseConfig.otgAndroidDataTreeUri =
            treeUri else baseConfig.otgAndroidObbTreeUri = treeUri
        isPathOnSD(path) -> if (isAndroidDataDir(path)) baseConfig.sdAndroidDataTreeUri =
            treeUri else baseConfig.sdAndroidObbTreeUri = treeUri
        else -> if (isAndroidDataDir(path)) baseConfig.primaryAndroidDataTreeUri =
            treeUri else baseConfig.primaryAndroidObbTreeUri = treeUri
    }
}

fun Context.createDocumentUriFromRootTree(fullPath: String): Uri {
    val storageId = getSAFStorageId(fullPath)

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

fun Context.getMyFileUri(file: File): Uri {
    return if (isNougatPlus()) {
        FileProvider.getUriForFile(this, "$packageName.provider", file)
    } else {
        Uri.fromFile(file)
    }
}

fun Context.scanFileRecursively(file: File, callback: (() -> Unit)? = null) {
    scanFilesRecursively(arrayListOf(file), callback)
}

fun Context.scanFilesRecursively(files: List<File>, callback: (() -> Unit)? = null) {
    val allPaths = java.util.ArrayList<String>()
    for (file in files) {
        allPaths.addAll(getPaths(file))
    }
    rescanPaths(allPaths, callback)
}

fun Context.getRealInternalStoragePath() =
    if (File("/storage/emulated/0").exists()) "/storage/emulated/0" else Environment.getExternalStorageDirectory().absolutePath.trimEnd(
        '/'
    )
