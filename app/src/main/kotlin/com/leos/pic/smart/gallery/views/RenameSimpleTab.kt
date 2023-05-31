package ca.on.sudbury.hojat.smartgallery.views

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import android.util.AttributeSet
import android.widget.RelativeLayout
import android.widget.Toast
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.extensions.baseConfig
import ca.on.sudbury.hojat.smartgallery.extensions.copySingleFileSdk30
import ca.on.sudbury.hojat.smartgallery.extensions.getDoesFilePathExist
import ca.on.sudbury.hojat.smartgallery.extensions.getFileUrisFromFileDirItemsTuple
import ca.on.sudbury.hojat.smartgallery.extensions.getFilenameFromPath
import ca.on.sudbury.hojat.smartgallery.extensions.getParentPath
import ca.on.sudbury.hojat.smartgallery.extensions.isAValidFilename
import ca.on.sudbury.hojat.smartgallery.extensions.renameFile
import ca.on.sudbury.hojat.smartgallery.extensions.scanPathsRecursively
import ca.on.sudbury.hojat.smartgallery.extensions.toFileDirItem
import ca.on.sudbury.hojat.smartgallery.extensions.updateInMediaStore
import ca.on.sudbury.hojat.smartgallery.extensions.updateTextColors
import ca.on.sudbury.hojat.smartgallery.interfaces.RenameTab
import ca.on.sudbury.hojat.smartgallery.models.Android30RenameFormat
import ca.on.sudbury.hojat.smartgallery.models.FileDirItem
import ca.on.sudbury.hojat.smartgallery.usecases.GetFileExtensionUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsPathOnSdUseCase
import kotlinx.android.synthetic.main.tab_rename_simple.view.*
import java.io.File

class RenameSimpleTab(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs),
    RenameTab {
    var ignoreClicks = false
    var stopLooping =
        false     // we should request the permission on Android 30+ for all uris at once, not one by one
    var activity: BaseSimpleActivity? = null
    var paths = ArrayList<String>()

    override fun onFinishInflate() {
        super.onFinishInflate()
        context.updateTextColors(rename_simple_holder)
    }

    override fun initTab(activity: BaseSimpleActivity, paths: ArrayList<String>) {
        this.activity = activity
        this.paths = paths
    }

    override fun dialogConfirmed(
        useMediaFileExtension: Boolean,
        callback: (success: Boolean) -> Unit
    ) {
        stopLooping = false
        val valueToAdd = rename_simple_value.text.toString()
        val append = rename_simple_radio_group.checkedRadioButtonId == rename_simple_radio_append.id

        if (valueToAdd.isEmpty()) {
            callback(false)
            return
        }

        if (!valueToAdd.isAValidFilename()) {
            Toast.makeText(activity, R.string.invalid_name, Toast.LENGTH_LONG).show()
            return
        }

        val validPaths = paths.filter { activity?.getDoesFilePathExist(it) == true }
        val firstPath = validPaths.firstOrNull()
        val sdFilePath = validPaths.firstOrNull { IsPathOnSdUseCase(activity, it) } ?: firstPath
        if (firstPath == null || sdFilePath == null) {
            Toast.makeText(activity, R.string.unknown_error_occurred, Toast.LENGTH_LONG).show()
            return
        }

        activity?.handleSAFDialog(sdFilePath) {
            if (!it) {
                return@handleSAFDialog
            }

            activity?.checkManageMediaOrHandleSAFDialogSdk30(firstPath) {
                if (!it) {
                    return@checkManageMediaOrHandleSAFDialogSdk30
                }

                ignoreClicks = true
                var pathsCnt = validPaths.size
                for (path in validPaths) {
                    if (stopLooping) {
                        return@checkManageMediaOrHandleSAFDialogSdk30
                    }

                    val fullName = path.getFilenameFromPath()
                    var dotAt = fullName.lastIndexOf(".")
                    if (dotAt == -1) {
                        dotAt = fullName.length
                    }

                    val name = fullName.substring(0, dotAt)
                    val extension =
                        if (fullName.contains(".")) ".${GetFileExtensionUseCase(fullName)}" else ""

                    val newName = if (append) {
                        "$name$valueToAdd$extension"
                    } else {
                        "$valueToAdd$fullName"
                    }

                    val newPath = "${path.getParentPath()}/$newName"

                    if (activity?.getDoesFilePathExist(newPath) == true) {
                        continue
                    }

                    activity?.renameFile(path, newPath, true) { success, android30Format ->
                        if (success) {
                            pathsCnt--
                            if (pathsCnt == 0) {
                                callback(true)
                            }
                        } else {
                            ignoreClicks = false
                            if (android30Format != Android30RenameFormat.NONE) {
                                stopLooping = true
                                renameAllFiles(
                                    validPaths,
                                    append,
                                    valueToAdd,
                                    android30Format,
                                    callback
                                )
                            } else {
                                Toast.makeText(
                                    activity,
                                    R.string.unknown_error_occurred,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
                stopLooping = false
            }
        }
    }

    private fun renameAllFiles(
        paths: List<String>,
        appendString: Boolean,
        stringToAdd: String,
        android30Format: Android30RenameFormat,
        callback: (success: Boolean) -> Unit
    ) {
        val fileDirItems = paths.map { File(it).toFileDirItem(context) }
        val uriPairs = context.getFileUrisFromFileDirItemsTuple(fileDirItems)
        val validPaths = uriPairs.first
        val uris = uriPairs.second
        val activity = activity
        activity?.updateSDK30Uris(uris) { success ->
            if (success) {
                try {
                    uris.forEachIndexed { index, uri ->
                        val path = validPaths[index]

                        val fullName = path.getFilenameFromPath()
                        var dotAt = fullName.lastIndexOf(".")
                        if (dotAt == -1) {
                            dotAt = fullName.length
                        }

                        val name = fullName.substring(0, dotAt)
                        val extension =
                            if (fullName.contains(".")) ".${GetFileExtensionUseCase(fullName)}" else ""

                        val newName = if (appendString) {
                            "$name$stringToAdd$extension"
                        } else {
                            "$stringToAdd$fullName"
                        }

                        when (android30Format) {
                            Android30RenameFormat.SAF -> {
                                val sourceFile = File(path).toFileDirItem(activity)
                                val newPath = "${path.getParentPath()}/$newName"
                                val destinationFile = FileDirItem(
                                    newPath,
                                    newName,
                                    sourceFile.isDirectory,
                                    sourceFile.children,
                                    sourceFile.size,
                                    sourceFile.modified
                                )
                                if (activity.copySingleFileSdk30(sourceFile, destinationFile)) {
                                    if (!activity.baseConfig.keepLastModified) {
                                        File(newPath).setLastModified(System.currentTimeMillis())
                                    }
                                    activity.contentResolver.delete(uri, null)
                                    activity.updateInMediaStore(path, newPath)
                                    activity.scanPathsRecursively(arrayListOf(newPath))
                                }
                            }
                            Android30RenameFormat.CONTENT_RESOLVER -> {
                                val values = ContentValues().apply {
                                    put(MediaStore.Images.Media.DISPLAY_NAME, newName)
                                }
                                context.contentResolver.update(uri, values, null, null)
                            }
                            Android30RenameFormat.NONE -> {
                                activity.runOnUiThread {
                                    callback(true)
                                }
                                return@forEachIndexed
                            }
                        }
                    }
                    activity.runOnUiThread {
                        callback(true)
                    }
                } catch (e: Exception) {
                    activity.runOnUiThread {
                        Toast.makeText(activity, e.toString(), Toast.LENGTH_LONG).show()
                        callback(false)
                    }
                }
            }
        }
    }
}
