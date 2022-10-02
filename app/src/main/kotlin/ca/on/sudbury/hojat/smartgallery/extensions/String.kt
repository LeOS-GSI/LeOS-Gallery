package ca.on.sudbury.hojat.smartgallery.extensions

import android.os.Environment
import android.provider.MediaStore
import com.simplemobiletools.commons.extensions.getMimeType
import com.simplemobiletools.commons.extensions.isExternalStorageManager
import com.simplemobiletools.commons.helpers.NOMEDIA
import com.simplemobiletools.commons.helpers.audioExtensions
import com.simplemobiletools.commons.helpers.isRPlus
import com.simplemobiletools.commons.helpers.normalizeRegex
import com.simplemobiletools.commons.helpers.photoExtensions
import com.simplemobiletools.commons.helpers.rawExtensions
import com.simplemobiletools.commons.helpers.videoExtensions
import java.io.File
import java.io.IOException
import java.text.Normalizer
import java.util.Locale
import kotlin.collections.HashMap

fun String.getFilenameExtension() = substring(lastIndexOf(".") + 1)

fun String.getFilenameFromPath() = substring(lastIndexOf("/") + 1)

fun String.getParentPath() = removeSuffix("/${getFilenameFromPath()}")

fun String.isThisOrParentIncluded(includedPaths: MutableSet<String>) =
    includedPaths.any { equals(it, true) } || includedPaths.any {
        "$this/".startsWith(
            "$it/",
            true
        )
    }

fun String.isThisOrParentExcluded(excludedPaths: MutableSet<String>) =
    excludedPaths.any { equals(it, true) } || excludedPaths.any {
        "$this/".startsWith(
            "$it/",
            true
        )
    }

fun String.isApng() = endsWith(".apng", true)

fun String.isAudioFast() = audioExtensions.any { endsWith(it, true) }

fun String.isGif() = endsWith(".gif", true)

fun String.isMediaFile() =
    isImageFast() || isVideoFast() || isGif() || isRawFast() || isSvg() || isPortrait()

fun String.isImageFast() = photoExtensions.any { endsWith(it, true) }

fun String.isPortrait() = getFilenameFromPath().contains(
    "portrait",
    true
) && File(this).parentFile?.name?.startsWith("img_", true) == true

fun String.isImageSlow() = isImageFast() || getMimeType().startsWith("image") || startsWith(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString())

fun String.isJpg() = endsWith(".jpg", true) or endsWith(".jpeg", true)

fun String.isPng() = endsWith(".png", true)

fun String.isRawFast() = rawExtensions.any { endsWith(it, true) }

fun String.isSvg() = endsWith(".svg", true)
fun String.isVideoSlow() = isVideoFast() || getMimeType().startsWith("video") || startsWith(
    MediaStore.Video.Media.EXTERNAL_CONTENT_URI.toString()
)

fun String.isWebP() = endsWith(".webp", true)

// cache which folders contain .nomedia files to avoid checking them over and over again
fun String.shouldFolderBeVisible(
    excludedPaths: MutableSet<String>,
    includedPaths: MutableSet<String>,
    showHidden: Boolean,
    folderNoMediaStatuses: HashMap<String, Boolean>,
    callback: (path: String, hasNoMedia: Boolean) -> Unit): Boolean {
    if (isEmpty()) {
        return false
    }

    val file = File(this)
    val filename = file.name
    if (filename.startsWith("img_", true) && file.isDirectory) {
        val files = file.list()
        if (files != null) {
            if (files.any { it.contains("burst", true) }) {
                return false
            }
        }
    }

    if (!showHidden && filename.startsWith('.')) {
        return false
    } else if (includedPaths.contains(this)) {
        return true
    }

    val containsNoMedia = if (showHidden) {
        false
    } else {
        folderNoMediaStatuses.getOrElse("$this/$NOMEDIA") { false } || ((!isRPlus() || isExternalStorageManager()) && File(
            this,
            NOMEDIA
        ).exists())
    }

    return if (!showHidden && containsNoMedia) {
        false
    } else if (excludedPaths.contains(this)) {
        false
    } else if (isThisOrParentIncluded(includedPaths)) {
        true
    } else if (isThisOrParentExcluded(excludedPaths)) {
        false
    } else if (!showHidden) {
        var containsNoMediaOrDot = containsNoMedia || contains("/.")
        if (!containsNoMediaOrDot) {
            var curPath = this
            for (i in 0 until count { it == '/' } - 1) {
                curPath = curPath.substringBeforeLast('/')
                val pathToCheck = "$curPath/$NOMEDIA"
                if (folderNoMediaStatuses.contains(pathToCheck)) {
                    if (folderNoMediaStatuses[pathToCheck] == true) {
                        containsNoMediaOrDot = true
                        break
                    }
                } else {
                    val noMediaExists =
                        folderNoMediaStatuses.getOrElse(pathToCheck) { false } || File(pathToCheck).exists()
                    callback(pathToCheck, noMediaExists)
                    if (noMediaExists) {
                        containsNoMediaOrDot = true
                        break
                    }
                }
            }
        }
        !containsNoMediaOrDot
    } else {
        true
    }
}

// recognize /sdcard/DCIM as the same folder as /storage/emulated/0/DCIM
fun String.getDistinctPath(): String {
    return try {
        File(this).canonicalPath.lowercase(Locale.getDefault())
    } catch (e: IOException) {
        lowercase(Locale.getDefault())
    }
}

fun String.isDownloadsFolder() = equals(
    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString(), true
)

// fast extension checks, not guaranteed to be accurate
fun String.isVideoFast() = videoExtensions.any { endsWith(it, true) }

// remove diacritics, for example č -> c
fun String.normalizeString() = Normalizer.normalize(this, Normalizer.Form.NFD).replace(
    normalizeRegex, ""
)
