package ca.on.sudbury.hojat.smartgallery.dialogs


import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.LinearLayoutManager
import ca.on.sudbury.hojat.smartgallery.extensions.areSystemAnimationsEnabled
import ca.on.sudbury.hojat.smartgallery.extensions.baseConfig
import ca.on.sudbury.hojat.smartgallery.extensions.getAlertDialogBuilder
import ca.on.sudbury.hojat.smartgallery.extensions.getAndroidSAFFileItems
import ca.on.sudbury.hojat.smartgallery.extensions.getColoredDrawableWithColor
import ca.on.sudbury.hojat.smartgallery.extensions.getContrastColor
import ca.on.sudbury.hojat.smartgallery.extensions.getDoesFilePathExist
import ca.on.sudbury.hojat.smartgallery.extensions.getFilenameFromPath
import ca.on.sudbury.hojat.smartgallery.extensions.getIsPathDirectory
import ca.on.sudbury.hojat.smartgallery.extensions.getParentPath
import ca.on.sudbury.hojat.smartgallery.extensions.getProperPrimaryColor
import ca.on.sudbury.hojat.smartgallery.extensions.getProperTextColor
import ca.on.sudbury.hojat.smartgallery.extensions.getSomeAndroidSAFDocument
import ca.on.sudbury.hojat.smartgallery.extensions.getSomeDocumentFile
import ca.on.sudbury.hojat.smartgallery.extensions.getTextSize
import ca.on.sudbury.hojat.smartgallery.extensions.handleHiddenFolderPasswordProtection
import ca.on.sudbury.hojat.smartgallery.extensions.handleLockedFolderOpening
import ca.on.sudbury.hojat.smartgallery.extensions.internalStoragePath
import ca.on.sudbury.hojat.smartgallery.extensions.isAccessibleWithSAFSdk30
import ca.on.sudbury.hojat.smartgallery.extensions.isInDownloadDir
import ca.on.sudbury.hojat.smartgallery.extensions.isRestrictedSAFOnlyRoot
import ca.on.sudbury.hojat.smartgallery.extensions.isRestrictedWithSAFSdk30
import ca.on.sudbury.hojat.smartgallery.extensions.setupDialogStuff
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.adapters.FilepickerFavoritesAdapter
import ca.on.sudbury.hojat.smartgallery.adapters.FilePickerItemsAdapter
import ca.on.sudbury.hojat.smartgallery.databinding.DialogFilepickerBinding
import ca.on.sudbury.hojat.smartgallery.extensions.getDocumentSdk30
import ca.on.sudbury.hojat.smartgallery.extensions.getFastDocumentSdk30
import ca.on.sudbury.hojat.smartgallery.extensions.getLongValue
import ca.on.sudbury.hojat.smartgallery.extensions.getStringValue
import ca.on.sudbury.hojat.smartgallery.models.FileDirItem
import ca.on.sudbury.hojat.smartgallery.usecases.BeVisibleOrGoneUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.CalculateDirectChildrenUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.GetFileSizeUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsPathOnOtgUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.RunOnBackgroundThreadUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.ShowSafeToastUseCase
import ca.on.sudbury.hojat.smartgallery.views.Breadcrumbs
import timber.log.Timber
import java.io.File
import java.net.URLDecoder
import java.util.Locale
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * This is the only filepicker constructor with a couple of optional parameters
 *
 * @param activity has to be activity to avoid some Theme.AppCompat issues
 * @param currPath initial path of the dialog, defaults to the external storage
 * @param pickFile toggle used to determine if we are picking a file or a folder
 * @param showHidden toggle for showing hidden items, whose name starts with a dot
 * @param showFAB toggle the displaying of a Floating Action Button for creating new folders
 * @param callback the callback used for returning the selected file/folder
 */

@SuppressLint("SetTextI18n")
class FilePickerDialog(
    val activity: BaseSimpleActivity,
    var currPath: String = Environment.getExternalStorageDirectory().toString(),
    val pickFile: Boolean = true,
    var showHidden: Boolean = false,
    val showFAB: Boolean = false,
    val canAddShowHiddenButton: Boolean = false,
    private val forceShowRoot: Boolean = false,
    private val showFavoritesButton: Boolean = false,
    private val enforceStorageRestrictions: Boolean = true,
    val callback: (pickedPath: String) -> Unit
) : Breadcrumbs.BreadcrumbsListener {

    private var mFirstUpdate = true
    private var mPrevPath = ""
    private var mScrollStates = HashMap<String, Parcelable>()
    private var mDialog: AlertDialog? = null

    private var binding = DialogFilepickerBinding.inflate(activity.layoutInflater)

    init {
        Timber.d("Hojat Ghasemi : FilePickerDialog was called")
        if (!activity.getDoesFilePathExist(currPath)) {
            currPath = activity.internalStoragePath
        }
        if (!activity.getIsPathDirectory(currPath)) {
            currPath = currPath.getParentPath()
        }
        // do not allow copying files in the recycle bin manually
        if (currPath.startsWith(activity.filesDir.absolutePath)) {
            currPath = activity.internalStoragePath
        }
        binding.filepickerBreadcrumbs.apply {
            listener = this@FilePickerDialog
            updateFontSize(activity.getTextSize(), false)
            isShownInDialog = true
        }
        tryUpdateItems()
        setupFavorites()
        val builder = activity.getAlertDialogBuilder()
            .setNegativeButton(R.string.cancel, null)
            .setOnKeyListener { _, i, keyEvent ->
                if (keyEvent.action == KeyEvent.ACTION_UP && i == KeyEvent.KEYCODE_BACK) {
                    val breadcrumbs = binding.filepickerBreadcrumbs
                    if (breadcrumbs.getItemCount() > 1) {
                        breadcrumbs.removeBreadcrumb()
                        currPath = breadcrumbs.getLastItem().path.trimEnd('/')
                        tryUpdateItems()
                    } else {
                        mDialog?.dismiss()
                    }
                }
                true
            }
        if (!pickFile) {
            builder.setPositiveButton(R.string.ok, null)
        }
        if (showFAB) {
            binding.filepickerFab.apply {
                visibility = View.VISIBLE
                setOnClickListener { createNewFolder() }
            }
        }
        val secondaryFabBottomMargin =
            activity.resources.getDimension(if (showFAB) R.dimen.secondary_fab_bottom_margin else R.dimen.activity_margin)
                .toInt()
        binding.filepickerFabsHolder.apply {
            (layoutParams as CoordinatorLayout.LayoutParams).bottomMargin = secondaryFabBottomMargin
        }
        binding.filepickerPlaceholder.setTextColor(activity.getProperTextColor())
        binding.filepickerFastscroller.updateColors(activity.getProperPrimaryColor())
        binding.filepickerFabShowHidden.apply {
            BeVisibleOrGoneUseCase(this, !showHidden && canAddShowHiddenButton)
            setOnClickListener {
                activity.handleHiddenFolderPasswordProtection {
                    visibility = View.GONE
                    showHidden = true
                    tryUpdateItems()
                }
            }
        }
        binding.filepickerFavoritesLabel.text = "${activity.getString(R.string.favorites)}:"
        binding.filepickerFabShowFavorites.apply {
            BeVisibleOrGoneUseCase(
                this,
                showFavoritesButton && context.baseConfig.favorites.isNotEmpty()
            )
            setOnClickListener {
                if (binding.filepickerFavoritesHolder.visibility == View.VISIBLE) {
                    hideFavorites()
                } else {
                    showFavorites()
                }
            }
        }
        builder.apply {
            activity.setupDialogStuff(binding.root, this, getTitle()) { alertDialog ->
                mDialog = alertDialog
            }
        }
        if (!pickFile) {
            mDialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
                verifyPath()
            }
        }
    }

    private fun getTitle() = if (pickFile) R.string.select_file else R.string.select_folder

    private fun createNewFolder() {
        CreateNewFolderDialog(activity, currPath) {
            callback(it)
            mDialog?.dismiss()
        }
    }

    private fun tryUpdateItems() {
        RunOnBackgroundThreadUseCase {
            getItems(currPath) {
                activity.runOnUiThread {
                    binding.filepickerPlaceholder.visibility = View.GONE
                    updateItems(it as ArrayList<FileDirItem>)
                }
            }
        }
    }

    private fun updateItems(items: ArrayList<FileDirItem>) {
        if (!containsDirectory(items) && !mFirstUpdate && !pickFile && !showFAB) {
            verifyPath()
            return
        }

        val sortedItems =
            items.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase(Locale.ROOT) }))
        val adapter = FilePickerItemsAdapter(activity, sortedItems, binding.filepickerList) {
            if ((it as FileDirItem).isDirectory) {
                activity.handleLockedFolderOpening(it.path) { success ->
                    if (success) {
                        currPath = it.path
                        tryUpdateItems()
                    }
                }
            } else if (pickFile) {
                currPath = it.path
                verifyPath()
            }
        }

        val layoutManager = binding.filepickerList.layoutManager as LinearLayoutManager
        mScrollStates[mPrevPath.trimEnd('/')] = layoutManager.onSaveInstanceState()!!

        binding.apply {
            filepickerList.adapter = adapter
            filepickerBreadcrumbs.setBreadcrumb(currPath)

            if (root.context.areSystemAnimationsEnabled) {
                filepickerList.scheduleLayoutAnimation()
            }

            layoutManager.onRestoreInstanceState(mScrollStates[currPath.trimEnd('/')])
        }

        mFirstUpdate = false
        mPrevPath = currPath
    }

    private fun verifyPath() {
        when {
            activity.isRestrictedSAFOnlyRoot(currPath) -> {
                val document = activity.getSomeAndroidSAFDocument(currPath) ?: return
                sendSuccessForDocumentFile(document)
            }
            IsPathOnOtgUseCase(activity, currPath) -> {
                val fileDocument = activity.getSomeDocumentFile(currPath) ?: return
                sendSuccessForDocumentFile(fileDocument)
            }
            activity.isAccessibleWithSAFSdk30(currPath) -> {
                if (enforceStorageRestrictions) {
                    activity.handleSAFDialogSdk30(currPath) {
                        if (it) {
                            val document = getSomeDocumentSdk30(activity, currPath)
                            sendSuccessForDocumentFile(document ?: return@handleSAFDialogSdk30)
                        }
                    }
                } else {
                    sendSuccessForDirectFile()
                }

            }
            activity.isRestrictedWithSAFSdk30(currPath) -> {
                if (enforceStorageRestrictions) {
                    if (activity.isInDownloadDir(currPath)) {
                        sendSuccessForDirectFile()
                    } else {
                        ShowSafeToastUseCase(
                            activity,
                            R.string.system_folder_restriction,
                            Toast.LENGTH_LONG
                        )
                    }
                } else {
                    sendSuccessForDirectFile()
                }
            }
            else -> {
                sendSuccessForDirectFile()
            }
        }
    }

    private fun sendSuccessForDocumentFile(document: DocumentFile) {
        if ((pickFile && document.isFile) || (!pickFile && document.isDirectory)) {
            sendSuccess()
        }
    }

    private fun sendSuccessForDirectFile() {
        val file = File(currPath)
        if ((pickFile && file.isFile) || (!pickFile && file.isDirectory)) {
            sendSuccess()
        }
    }

    private fun sendSuccess() {
        currPath = if (currPath.length == 1) {
            currPath
        } else {
            currPath.trimEnd('/')
        }

        callback(currPath)
        mDialog?.dismiss()
    }

    private fun getItems(path: String, callback: (List<FileDirItem>) -> Unit) {
        when {
            activity.isRestrictedSAFOnlyRoot(path) -> {
                activity.handleAndroidSAFDialog(path) {
                    activity.getAndroidSAFFileItems(path, showHidden) {
                        callback(it)
                    }
                }
            }
            IsPathOnOtgUseCase(activity, path) -> getOTGItems(
                activity,
                path,
                showHidden,
                false,
                callback
            )
            else -> {
                val lastModifieds = getFolderLastModifieds(activity, path)
                getRegularItems(path, lastModifieds, callback)
            }
        }
    }

    private fun getRegularItems(
        path: String,
        lastModifieds: HashMap<String, Long>,
        callback: (List<FileDirItem>) -> Unit
    ) {
        val items = ArrayList<FileDirItem>()
        val files = File(path).listFiles()?.filterNotNull()
        if (files == null) {
            callback(items)
            return
        }

        for (file in files) {
            if (!showHidden && file.name.startsWith('.')) {
                continue
            }

            val curPath = file.absolutePath
            val curName = curPath.getFilenameFromPath()
            val size = file.length()
            var lastModified = lastModifieds.remove(curPath)
            val isDirectory = if (lastModified != null) false else file.isDirectory
            if (lastModified == null) {
                lastModified =
                    0    // we don't actually need the real lastModified that badly, do not check file.lastModified()
            }

            val children =
                if (isDirectory) CalculateDirectChildrenUseCase(file, activity, showHidden)
                else 0
            items.add(FileDirItem(curPath, curName, isDirectory, children, size, lastModified))
        }
        callback(items)
    }

    private fun containsDirectory(items: List<FileDirItem>) = items.any { it.isDirectory }

    private fun setupFavorites() {
        FilepickerFavoritesAdapter(
            activity,
            activity.baseConfig.favorites.toMutableList(),
            binding.filepickerFavoritesList
        ) {
            currPath = it as String
            verifyPath()
        }.apply {
            binding.filepickerFavoritesList.adapter = this
        }
    }

    private fun showFavorites() {
        binding.apply {
            filepickerFavoritesHolder.visibility = View.VISIBLE
            filepickerFilesHolder.visibility = View.GONE
            val drawable = activity.resources.getColoredDrawableWithColor(
                R.drawable.ic_folder_vector,
                activity.getProperPrimaryColor().getContrastColor()
            )
            filepickerFabShowFavorites.setImageDrawable(drawable)
        }
    }

    private fun hideFavorites() {
        binding.apply {
            filepickerFavoritesHolder.visibility = View.GONE
            filepickerFilesHolder.visibility = View.VISIBLE
            val drawable = activity.resources.getColoredDrawableWithColor(
                R.drawable.ic_star_vector,
                activity.getProperPrimaryColor().getContrastColor()
            )
            filepickerFabShowFavorites.setImageDrawable(drawable)
        }
    }

    override fun breadcrumbClicked(id: Int) {
        if (id == 0) {
            StoragePickerDialog(activity, currPath, forceShowRoot, true) {
                currPath = it
                tryUpdateItems()
            }
        } else {
            val item = binding.filepickerBreadcrumbs.getItem(id)
            if (currPath != item.path.trimEnd('/')) {
                currPath = item.path
                tryUpdateItems()
            }
        }
    }

    private fun getSomeDocumentSdk30(owner: Context, path: String): DocumentFile? =
        owner.getFastDocumentSdk30(path) ?: owner.getDocumentSdk30(path)

    private fun getFolderLastModifieds(owner: Context, folder: String): HashMap<String, Long> {
        val lastModifieds = java.util.HashMap<String, Long>()
        val projection = arrayOf(
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_MODIFIED
        )

        val uri = MediaStore.Files.getContentUri("external")
        val selection =
            "${MediaStore.Images.Media.DATA} LIKE ? AND ${MediaStore.Images.Media.DATA} NOT LIKE ? AND ${MediaStore.Images.Media.MIME_TYPE} IS NOT NULL" // avoid selecting folders
        val selectionArgs = arrayOf("$folder/%", "$folder/%/%")


        val cursor =
            owner.contentResolver.query(uri, projection, selection, selectionArgs, null)
        cursor?.use {
            if (cursor.moveToFirst()) {
                do {
                    val lastModified =
                        cursor.getLongValue(MediaStore.Images.Media.DATE_MODIFIED) * 1000
                    if (lastModified != 0L) {
                        val name =
                            cursor.getStringValue(MediaStore.Images.Media.DISPLAY_NAME)
                        lastModifieds["$folder/$name"] = lastModified
                    }
                } while (cursor.moveToNext())
            }
        }


        return lastModifieds
    }

    private fun getOTGItems(
        owner: Context,
        path: String,
        shouldShowHidden: Boolean,
        getProperFileSize: Boolean,
        callback: (ArrayList<FileDirItem>) -> Unit
    ) {
        val items = java.util.ArrayList<FileDirItem>()
        val otgTreeUri = owner.baseConfig.otgTreeUri
        var rootUri = try {
            DocumentFile.fromTreeUri(owner.applicationContext, Uri.parse(otgTreeUri))
        } catch (e: Exception) {
            ShowSafeToastUseCase(owner, e.toString())
            owner.baseConfig.otgPath = ""
            owner.baseConfig.otgTreeUri = ""
            owner.baseConfig.otgPartition = ""
            null
        }

        if (rootUri == null) {
            callback(items)
            return
        }

        val parts = path.split("/").dropLastWhile { it.isEmpty() }
        for (part in parts) {
            if (path == owner.baseConfig.otgPath) {
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

        val basePath = "${owner.baseConfig.otgTreeUri}/document/${owner.baseConfig.otgPartition}%3A"
        for (file in files) {
            val name = file.name ?: continue
            if (!shouldShowHidden && name.startsWith(".")) {
                continue
            }

            val isDirectory = file.isDirectory
            val filePath = file.uri.toString().substring(basePath.length)
            val decodedPath = owner.baseConfig.otgPath + "/" + URLDecoder.decode(filePath, "UTF-8")
            val fileSize = when {
                getProperFileSize -> GetFileSizeUseCase(file, shouldShowHidden)
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
}
