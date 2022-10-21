package ca.on.sudbury.hojat.smartgallery.activities

import android.os.Bundle
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.dialogs.FilePickerDialog
import ca.on.sudbury.hojat.smartgallery.extensions.beVisibleIf
import ca.on.sudbury.hojat.smartgallery.extensions.getProperTextColor
import ca.on.sudbury.hojat.smartgallery.extensions.internalStoragePath
import ca.on.sudbury.hojat.smartgallery.extensions.isExternalStorageManager
import ca.on.sudbury.hojat.smartgallery.helpers.NavigationIcon
import ca.on.sudbury.hojat.smartgallery.interfaces.RefreshRecyclerViewListener
import ca.on.sudbury.hojat.smartgallery.adapters.ManageFoldersAdapter
import ca.on.sudbury.hojat.smartgallery.base.SimpleActivity
import ca.on.sudbury.hojat.smartgallery.databinding.ActivityManageFoldersBinding
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.photoedit.usecases.IsRPlusUseCase

class ExcludedFoldersActivity : SimpleActivity(), RefreshRecyclerViewListener {

    private lateinit var binding: ActivityManageFoldersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageFoldersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        updateFolders()
        setupOptionsMenu()
        binding.manageFoldersToolbar.title = getString(R.string.excluded_folders)
    }

    override fun onResume() {
        super.onResume()
        setupToolbar(binding.manageFoldersToolbar, NavigationIcon.Arrow)
    }

    private fun updateFolders() {
        val folders = ArrayList<String>()
        config.excludedFolders.mapTo(folders) { it }
        var placeholderText = getString(R.string.excluded_activity_placeholder)
        binding.manageFoldersPlaceholder.apply {
            beVisibleIf(folders.isEmpty())
            setTextColor(getProperTextColor())

            if (IsRPlusUseCase() && !isExternalStorageManager()) {
                placeholderText = placeholderText.substringBefore("\n")
            }

            text = placeholderText
        }

        val adapter = ManageFoldersAdapter(this, folders, true, this, binding.manageFoldersList) {}
        binding.manageFoldersList.adapter = adapter
    }

    private fun setupOptionsMenu() {
        binding.manageFoldersToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.add_folder -> addFolder()
                else -> return@setOnMenuItemClickListener false
            }
            return@setOnMenuItemClickListener true
        }
    }

    override fun refreshItems() {
        updateFolders()
    }

    private fun addFolder() {
        FilePickerDialog(
            activity = this,
            internalStoragePath,
            pickFile = false,
            config.shouldShowHidden,
            showFAB = false,
            canAddShowHiddenButton = true,
            enforceStorageRestrictions = false,
        ) {
            config.lastFilepickerPath = it
            config.addExcludedFolder(it)
            updateFolders()
        }
    }
}
