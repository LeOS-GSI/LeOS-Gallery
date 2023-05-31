package ca.on.sudbury.hojat.smartgallery.dialogs

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import ca.on.hojat.palette.views.MyGridLayoutManager
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.adapters.DirectoryAdapter
import ca.on.sudbury.hojat.smartgallery.databinding.DialogFragmentDirectoryPickerBinding
import ca.on.sudbury.hojat.smartgallery.extensions.addTempFolderIfNeeded
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.extensions.getCachedDirectories
import ca.on.sudbury.hojat.smartgallery.extensions.getDirsToShow
import ca.on.sudbury.hojat.smartgallery.extensions.getDistinctPath
import ca.on.sudbury.hojat.smartgallery.extensions.getProperPrimaryColor
import ca.on.sudbury.hojat.smartgallery.extensions.getSortedDirectories
import ca.on.sudbury.hojat.smartgallery.extensions.handleHiddenFolderPasswordProtection
import ca.on.sudbury.hojat.smartgallery.extensions.handleLockedFolderOpening
import ca.on.sudbury.hojat.smartgallery.extensions.isInDownloadDir
import ca.on.sudbury.hojat.smartgallery.extensions.isRestrictedWithSAFSdk30
import ca.on.sudbury.hojat.smartgallery.helpers.ViewType
import ca.on.sudbury.hojat.smartgallery.models.Directory
import ca.on.sudbury.hojat.smartgallery.usecases.BeVisibleOrGoneUseCase

/**
 *
 * I converted this to DialogFragment but didn't add any bottom buttons. You might wanna do that in future.
 *
 * This dialog is meant to allow the user to choose a folder; and is being called from
 * various places in the app:
 *
 * 1- While you're adding the app widget of this app to your launcher, in the widget
 * configuration page click on the button below "Folder shown on the widget:" and the
 * resulting dialog is created via this class.
 *
 * 2- In any folders, long click on one or more of pics/vids and from context menu click
 * on "copy to" or "move to". The resulting dialog is created via this class.
 *
 * 3- .....
 *
 */
class PickDirectoryDialogFragment(
    val sourcePath: String,
    private val showFavoritesBin: Boolean,
    private val isPickingCopyMoveDestination: Boolean,
    val callback: (path: String) -> Unit
) : DialogFragment() {

    // The binding
    private var _binding: DialogFragmentDirectoryPickerBinding? = null
    private val binding get() = _binding!!

    // All the needed configurations
    private var shownDirectories = ArrayList<Directory>()
    private var allDirectories = ArrayList<Directory>()
    private var openedSubfolders = arrayListOf("")
    private var isGridViewType = false
    private var showHidden = false
    private var currentPathPrefix = ""


    /**
     * Create the UI of the dialog
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // load the binding
        _binding = DialogFragmentDirectoryPickerBinding.inflate(inflater, container, false)

        loadDialogUi()
        return binding.root
    }

    private fun loadDialogUi() {

        // We need to first load some configurations before drawing the UI
        isGridViewType = requireActivity().config.viewTypeFolders == ViewType.Grid.id
        showHidden = requireActivity().config.shouldShowHidden

        (binding.directoriesGrid.layoutManager as MyGridLayoutManager).apply {
            orientation =
                if (requireActivity().config.scrollHorizontally && isGridViewType) RecyclerView.HORIZONTAL else RecyclerView.VERTICAL
            spanCount = if (isGridViewType) requireActivity().config.dirColumnCnt else 1
        }
        binding.directoriesFastscroller.updateColors(requireActivity().getProperPrimaryColor())
        BeVisibleOrGoneUseCase(
            binding.directoriesShowHidden,
            !requireActivity().config.shouldShowHidden
        )
        fetchDirectories(false)
    }

    /**
     * Register listeners for views.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setOnKeyListener { _, keyCode, event ->

            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                backPressed()
            }
            true
        }
        binding.directoriesShowHidden.setOnClickListener {
            (requireActivity() as AppCompatActivity).handleHiddenFolderPasswordProtection {
                binding.directoriesShowHidden.visibility = View.GONE
                showHidden = true
                fetchDirectories(true)
            }
        }

    }

    /**
     * Clean all used resources.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun backPressed() {
        if (requireActivity().config.groupDirectSubfolders) {
            if (currentPathPrefix.isEmpty()) {
                dismiss()
            } else {
                openedSubfolders.removeAt(openedSubfolders.size - 1)
                currentPathPrefix = openedSubfolders.last()
                gotDirectories(allDirectories)
            }
        } else {
            dismiss()
        }
    }

    private fun gotDirectories(newDirs: ArrayList<Directory>) {
        if (allDirectories.isEmpty()) {
            allDirectories = newDirs.clone() as ArrayList<Directory>
        }

        val distinctDirs =
            newDirs.filter { showFavoritesBin || (!it.isRecycleBin() && !it.areFavorites()) }
                .distinctBy { it.path.getDistinctPath() }
                .toMutableList() as ArrayList<Directory>
        val sortedDirs = requireActivity().getSortedDirectories(distinctDirs)
        val dirs = requireActivity().getDirsToShow(sortedDirs, allDirectories, currentPathPrefix)
            .clone() as ArrayList<Directory>
        if (dirs.hashCode() == shownDirectories.hashCode()) {
            return
        }

        shownDirectories = dirs
        val adapter = DirectoryAdapter(
            requireActivity() as BaseSimpleActivity,
            dirs.clone() as ArrayList<Directory>,
            null,
            binding.directoriesGrid,
            true
        ) {
            val clickedDir = it as Directory
            val path = clickedDir.path
            if (clickedDir.subfoldersCount == 1 || !requireActivity().config.groupDirectSubfolders) {
                if (isPickingCopyMoveDestination && path.trimEnd('/') == sourcePath) {
                    Toast.makeText(
                        activity,
                        R.string.source_and_destination_same,
                        Toast.LENGTH_LONG
                    ).show()
                    return@DirectoryAdapter
                } else if (isPickingCopyMoveDestination && requireActivity().isRestrictedWithSAFSdk30(
                        path
                    ) && !requireActivity().isInDownloadDir(
                        path
                    )
                ) {
                    Toast.makeText(
                        activity,
                        R.string.system_folder_copy_restriction,
                        Toast.LENGTH_LONG
                    ).show()
                    return@DirectoryAdapter
                } else {
                    (requireActivity() as AppCompatActivity).handleLockedFolderOpening(path) { success ->
                        if (success) {
                            callback(path)
                        }
                    }
                    dismiss()
                }
            } else {
                currentPathPrefix = path
                openedSubfolders.add(path)
                gotDirectories(allDirectories)
            }
        }

        val scrollHorizontally = requireActivity().config.scrollHorizontally && isGridViewType
        binding.apply {
            directoriesGrid.adapter = adapter
            directoriesFastscroller.setScrollVertically(!scrollHorizontally)
        }
    }

    private fun fetchDirectories(forceShowHidden: Boolean) {
        requireActivity().getCachedDirectories(forceShowHidden = forceShowHidden) {
            if (it.isNotEmpty()) {
                it.forEach { directory ->
                    directory.subfoldersMediaCount = directory.mediaCnt
                }

                requireActivity().runOnUiThread {
                    gotDirectories(requireActivity().addTempFolderIfNeeded(it))
                }
            }
        }
    }

    companion object {
        const val TAG = "PickDirectoryDialogFragment"
    }
}