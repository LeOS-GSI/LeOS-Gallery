package ca.on.sudbury.hojat.smartgallery.activities

import android.os.Bundle
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.adapters.ManageHiddenFoldersAdapter
import ca.on.sudbury.hojat.smartgallery.base.SimpleActivity
import ca.on.sudbury.hojat.smartgallery.databinding.ActivityManageFoldersBinding
import ca.on.sudbury.hojat.smartgallery.dialogs.FilePickerDialogFragment
import ca.on.sudbury.hojat.smartgallery.extensions.addNoMedia
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.extensions.getNoMediaFoldersSync
import ca.on.sudbury.hojat.smartgallery.extensions.getProperTextColor
import ca.on.sudbury.hojat.smartgallery.helpers.NavigationIcon
import ca.on.sudbury.hojat.smartgallery.interfaces.RefreshRecyclerViewListener
import ca.on.sudbury.hojat.smartgallery.usecases.BeVisibleOrGoneUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.RunOnBackgroundThreadUseCase

class HiddenFoldersActivity : SimpleActivity(), RefreshRecyclerViewListener {

    private lateinit var binding: ActivityManageFoldersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageFoldersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        updateFolders()
        setupOptionsMenu()
        binding.manageFoldersToolbar.title = getString(R.string.hidden_folders)
    }

    override fun onResume() {
        super.onResume()
        setupToolbar(binding.manageFoldersToolbar, NavigationIcon.Arrow)
    }

    private fun updateFolders() {
        RunOnBackgroundThreadUseCase {
            runOnUiThread {
                binding.manageFoldersPlaceholder.apply {
                    text = getString(R.string.hidden_folders_placeholder)
                    BeVisibleOrGoneUseCase(this, getNoMediaFoldersSync().isEmpty())
                    setTextColor(getProperTextColor())
                }
                val adapter = ManageHiddenFoldersAdapter(
                    this,
                    getNoMediaFoldersSync(),
                    this,
                    binding.manageFoldersList
                ) {}
                binding.manageFoldersList.adapter = adapter
            }
        }
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
        val callback: (String) -> Unit = { pickedPath ->
            config.lastFilepickerPath = pickedPath
            RunOnBackgroundThreadUseCase {
                addNoMedia(pickedPath) {
                    updateFolders()
                }
            }
        }
        FilePickerDialogFragment(
            config.lastFilepickerPath,
            false,
            config.shouldShowHidden,
            showFAB = false,
            canAddShowHiddenButton = true,
            callback = callback
        ).show(supportFragmentManager, FilePickerDialogFragment.TAG)
    }
}
