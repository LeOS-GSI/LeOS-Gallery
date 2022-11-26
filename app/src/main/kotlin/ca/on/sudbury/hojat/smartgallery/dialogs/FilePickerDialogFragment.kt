package ca.on.sudbury.hojat.smartgallery.dialogs

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.provider.MediaStore
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.adapters.FilePickerItemsAdapter
import ca.on.sudbury.hojat.smartgallery.adapters.FilepickerFavoritesAdapter
import ca.on.sudbury.hojat.smartgallery.databinding.DialogFragmentFilepickerBinding
import ca.on.sudbury.hojat.smartgallery.extensions.areSystemAnimationsEnabled
import ca.on.sudbury.hojat.smartgallery.extensions.baseConfig
import ca.on.sudbury.hojat.smartgallery.extensions.getAndroidSAFFileItems
import ca.on.sudbury.hojat.smartgallery.extensions.getColoredDrawableWithColor
import ca.on.sudbury.hojat.smartgallery.extensions.getContrastColor
import ca.on.sudbury.hojat.smartgallery.extensions.getDocumentSdk30
import ca.on.sudbury.hojat.smartgallery.extensions.getDoesFilePathExist
import ca.on.sudbury.hojat.smartgallery.extensions.getFastDocumentSdk30
import ca.on.sudbury.hojat.smartgallery.extensions.getFilenameFromPath
import ca.on.sudbury.hojat.smartgallery.extensions.getIsPathDirectory
import ca.on.sudbury.hojat.smartgallery.extensions.getLongValue
import ca.on.sudbury.hojat.smartgallery.extensions.getParentPath
import ca.on.sudbury.hojat.smartgallery.extensions.getProperPrimaryColor
import ca.on.sudbury.hojat.smartgallery.extensions.getProperTextColor
import ca.on.sudbury.hojat.smartgallery.extensions.getSomeAndroidSAFDocument
import ca.on.sudbury.hojat.smartgallery.extensions.getSomeDocumentFile
import ca.on.sudbury.hojat.smartgallery.extensions.getStringValue
import ca.on.sudbury.hojat.smartgallery.extensions.getTextSize
import ca.on.sudbury.hojat.smartgallery.extensions.handleHiddenFolderPasswordProtection
import ca.on.sudbury.hojat.smartgallery.extensions.handleLockedFolderOpening
import ca.on.sudbury.hojat.smartgallery.extensions.internalStoragePath
import ca.on.sudbury.hojat.smartgallery.extensions.isAccessibleWithSAFSdk30
import ca.on.sudbury.hojat.smartgallery.extensions.isInDownloadDir
import ca.on.sudbury.hojat.smartgallery.extensions.isRestrictedSAFOnlyRoot
import ca.on.sudbury.hojat.smartgallery.extensions.isRestrictedWithSAFSdk30
import ca.on.sudbury.hojat.smartgallery.models.FileDirItem
import ca.on.sudbury.hojat.smartgallery.usecases.BeVisibleOrGoneUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.CalculateDirectChildrenUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.GetFileSizeUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsPathOnOtgUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.RunOnBackgroundThreadUseCase
import ca.on.sudbury.hojat.smartgallery.views.Breadcrumbs
import java.io.File
import java.net.URLDecoder
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 *
 * It's been called from various places:
 *
 * 1- From menu in the main page choose "Create new folder".
 *
 * This is the only file-picker constructor with a couple of optional parameters
 *
 * The collor should be an activity to avoid some Theme.AppCompat issues
 * @param currPath initial path of the dialog, defaults to the external storage
 * @param pickFile toggle used to determine if we are picking a file or a folder
 * @param showHidden toggle for showing hidden items, whose name starts with a dot
 * @param showFAB toggle the displaying of a Floating Action Button for creating new folders
 * @param callback the callback used for returning the selected file/folder
 */
class FilePickerDialogFragment(
    private var currPath: String = Environment.getExternalStorageDirectory().toString(),
    private val pickFile: Boolean = true,
    var showHidden: Boolean = false,
    private val showFAB: Boolean = false,
    private val canAddShowHiddenButton: Boolean = false,
    private val forceShowRoot: Boolean = false,
    private val showFavoritesButton: Boolean = false,
    private val enforceStorageRestrictions: Boolean = true,
    val callback: (pickedPath: String) -> Unit
) : DialogFragment(), Breadcrumbs.BreadcrumbsListener {

    // the binding
    private var _binding: DialogFragmentFilepickerBinding? = null
    private val binding get() = _binding!!

    // Needed configs throughout the class
    private var mFirstUpdate = true
    private var mPrevPath = ""
    private var mScrollStates = HashMap<String, Parcelable>()


    /**
     * Create the UI of the dialog
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFragmentFilepickerBinding.inflate(
            inflater,
            container,
            false
        )
        loadDialogUI()
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun loadDialogUI() {
        if (!requireActivity().getDoesFilePathExist(currPath)) {
            currPath = requireActivity().internalStoragePath
        }
        if (!requireActivity().getIsPathDirectory(currPath)) {
            currPath = currPath.getParentPath()
        }
        // do not allow copying files in the recycle bin manually
        if (currPath.startsWith(requireActivity().filesDir.absolutePath)) {
            currPath = requireActivity().internalStoragePath
        }
        binding.filepickerBreadcrumbs.apply {
            listener = this@FilePickerDialogFragment
            updateFontSize(requireActivity().getTextSize(), false)
            isShownInDialog = true
        }
        tryUpdateItems()
        setupFavorites()
        if (pickFile) {
            binding.btnOkFilePickerDialog.visibility = View.GONE
        }
        if (showFAB) {
            binding.filepickerFab.apply {
                visibility = View.VISIBLE
                setOnClickListener { createNewFolder() }
            }
        }
        val secondaryFabBottomMargin =
            resources.getDimension(if (showFAB) R.dimen.secondary_fab_bottom_margin else R.dimen.activity_margin)
                .toInt()
        binding.filepickerFabsHolder.apply {
            (layoutParams as CoordinatorLayout.LayoutParams).bottomMargin = secondaryFabBottomMargin
        }
        binding.filepickerPlaceholder.setTextColor(requireActivity().getProperTextColor())
        binding.filepickerFastscroller.updateColors(requireActivity().getProperPrimaryColor())
        binding.filepickerFavoritesLabel.text = "${getString(R.string.favorites)}:"

    }

    /**
     * Register the listeners.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                val breadcrumbs = binding.filepickerBreadcrumbs
                if (breadcrumbs.getItemCount() > 1) {
                    breadcrumbs.removeBreadcrumb()
                    currPath = breadcrumbs.getLastItem().path.trimEnd('/')
                    tryUpdateItems()
                } else {
                    dismiss()
                }
            }
            true
        }

        if (!pickFile) {
            binding.btnOkFilePickerDialog.setOnClickListener { verifyPath() }
        }

        binding.btnCancelFilePickerDialog.setOnClickListener { dismiss() }
        binding.filepickerFabShowHidden.apply {
            BeVisibleOrGoneUseCase(this, !showHidden && canAddShowHiddenButton)
            setOnClickListener {
                requireActivity().handleHiddenFolderPasswordProtection {
                    visibility = View.GONE
                    showHidden = true
                    tryUpdateItems()
                }
            }
        }
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
    }

    private fun hideFavorites() {
        binding.apply {
            filepickerFavoritesHolder.visibility = View.GONE
            filepickerFilesHolder.visibility = View.VISIBLE
            val drawable = resources.getColoredDrawableWithColor(
                R.drawable.ic_star_vector,
                requireActivity().getProperPrimaryColor().getContrastColor()
            )
            filepickerFabShowFavorites.setImageDrawable(drawable)
        }
    }

    private fun showFavorites() {
        binding.apply {
            filepickerFavoritesHolder.visibility = View.VISIBLE
            filepickerFilesHolder.visibility = View.GONE
            val drawable = resources.getColoredDrawableWithColor(
                R.drawable.ic_folder_vector,
                requireActivity().getProperPrimaryColor().getContrastColor()
            )
            filepickerFabShowFavorites.setImageDrawable(drawable)
        }
    }

    /**
     * Cleaning the stuff
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun breadcrumbClicked(id: Int) {
        if (id == 0) {
            val callback: (String) -> Unit = { pickedPath ->
                currPath = pickedPath
                tryUpdateItems()
            }
            StoragePickerDialogFragment(
                currPath,
                forceShowRoot,
                true,
                callback
            ).show(
                requireActivity().supportFragmentManager,
                "StoragePickerDialogFragment"
            )
        } else {
            val item = binding.filepickerBreadcrumbs.getItem(id)
            if (currPath != item.path.trimEnd('/')) {
                currPath = item.path
                tryUpdateItems()
            }
        }
    }

    private fun createNewFolder() {
        val callback: (path: String) -> Unit = { path ->
            callback(path)
            dismiss()
        }
        CreateNewFolderDialogFragment(currPath, callback).show(
            requireActivity().supportFragmentManager,
            "CreateNewFolderDialogFragment"
        )
    }

    private fun tryUpdateItems() {
        RunOnBackgroundThreadUseCase {
            getItems(currPath) {
                requireActivity().runOnUiThread {
                    binding.filepickerPlaceholder.visibility = View.GONE
                    updateItems(it as ArrayList<FileDirItem>)
                }
            }
        }
    }

    private fun getItems(path: String, callback: (List<FileDirItem>) -> Unit) {
        when {
            requireActivity().isRestrictedSAFOnlyRoot(path) -> {
                (activity as BaseSimpleActivity).handleAndroidSAFDialog(path) {
                    requireActivity().getAndroidSAFFileItems(path, showHidden) {
                        callback(it)
                    }
                }
            }
            IsPathOnOtgUseCase(activity, path) -> getOTGItems(
                requireActivity(),
                path,
                showHidden,
                false,
                callback
            )
            else -> {
                val lastModifieds = getFolderLastModifieds(requireActivity(), path)
                getRegularItems(path, lastModifieds, callback)
            }
        }
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
            Toast.makeText(context, "FilePickerDialog : ${e.message}", Toast.LENGTH_LONG).show()

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


    private fun setupFavorites() {
        FilepickerFavoritesAdapter(
            requireActivity() as BaseSimpleActivity,
            requireActivity().baseConfig.favorites.toMutableList(),
            binding.filepickerFavoritesList
        ) {
            currPath = it as String
            verifyPath()
        }.apply {
            binding.filepickerFavoritesList.adapter = this
        }
    }


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
                if (isDirectory) CalculateDirectChildrenUseCase(file, requireActivity(), showHidden)
                else 0
            items.add(FileDirItem(curPath, curName, isDirectory, children, size, lastModified))
        }
        callback(items)
    }

    private fun updateItems(items: ArrayList<FileDirItem>) {
        if (!containsDirectory(items) && !mFirstUpdate && !pickFile && !showFAB) {
            verifyPath()
            return
        }

        val sortedItems =
            items.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase(Locale.ROOT) }))
        val adapter = FilePickerItemsAdapter(
            requireActivity() as BaseSimpleActivity,
            sortedItems,
            binding.filepickerList
        ) {
            if ((it as FileDirItem).isDirectory) {
                requireActivity().handleLockedFolderOpening(it.path) { success ->
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

    private fun containsDirectory(items: List<FileDirItem>) = items.any { it.isDirectory }

    private fun verifyPath() {
        when {
            requireActivity().isRestrictedSAFOnlyRoot(currPath) -> {
                val document = requireActivity().getSomeAndroidSAFDocument(currPath) ?: return
                sendSuccessForDocumentFile(document)
            }
            IsPathOnOtgUseCase(activity, currPath) -> {
                val fileDocument = requireActivity().getSomeDocumentFile(currPath) ?: return
                sendSuccessForDocumentFile(fileDocument)
            }
            requireActivity().isAccessibleWithSAFSdk30(currPath) -> {
                if (enforceStorageRestrictions) {
                    (requireActivity() as BaseSimpleActivity).handleSAFDialogSdk30(currPath) {
                        if (it) {
                            val document = getSomeDocumentSdk30(requireActivity(), currPath)
                            sendSuccessForDocumentFile(document ?: return@handleSAFDialogSdk30)
                        }
                    }
                } else {
                    sendSuccessForDirectFile()
                }

            }
            requireActivity().isRestrictedWithSAFSdk30(currPath) -> {
                if (enforceStorageRestrictions) {
                    if (requireActivity().isInDownloadDir(currPath)) {
                        sendSuccessForDirectFile()
                    } else {
                        Toast.makeText(
                            activity,
                            R.string.system_folder_restriction,
                            Toast.LENGTH_LONG
                        ).show()
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
        dismiss()
    }

    private fun getSomeDocumentSdk30(owner: Context, path: String): DocumentFile? =
        owner.getFastDocumentSdk30(path) ?: owner.getDocumentSdk30(path)

    private fun sendSuccessForDocumentFile(document: DocumentFile) {
        if ((pickFile && document.isFile) || (!pickFile && document.isDirectory)) {
            sendSuccess()
        }
    }
}