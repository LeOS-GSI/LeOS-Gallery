package ca.on.sudbury.hojat.smartgallery.dialogs

import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.databinding.DialogChangeViewTypeBinding
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.extensions.getAlertDialogBuilder
import ca.on.sudbury.hojat.smartgallery.extensions.setupDialogStuff
import ca.on.sudbury.hojat.smartgallery.helpers.VIEW_TYPE_GRID
import ca.on.sudbury.hojat.smartgallery.helpers.VIEW_TYPE_LIST
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.helpers.SHOW_ALL
import ca.on.sudbury.hojat.smartgallery.usecases.BeVisibleOrGoneUseCase

class ChangeViewTypeDialog(
    val activity: BaseSimpleActivity,
    private val fromFoldersView: Boolean,
    val path: String = "",
    val callback: () -> Unit
) {

    // we create the binding by referencing the owner Activity
    var binding = DialogChangeViewTypeBinding.inflate(activity.layoutInflater)

    private var config = activity.config
    private var pathToUse = path.ifEmpty { SHOW_ALL }

    init {
        binding.apply {
            val viewToCheck = if (fromFoldersView) {
                if (config.viewTypeFolders == VIEW_TYPE_GRID) {
                    changeViewTypeDialogRadioGrid.id
                } else {
                    changeViewTypeDialogRadioList.id
                }
            } else {
                val currViewType = config.getFolderViewType(pathToUse)
                if (currViewType == VIEW_TYPE_GRID) {
                    changeViewTypeDialogRadioGrid.id
                } else {
                    changeViewTypeDialogRadioList.id
                }
            }

            changeViewTypeDialogRadio.check(viewToCheck)
            changeViewTypeDialogGroupDirectSubfolders.apply {
                BeVisibleOrGoneUseCase(this, fromFoldersView)
                isChecked = config.groupDirectSubfolders
            }

            changeViewTypeDialogUseForThisFolder.apply {
                BeVisibleOrGoneUseCase(this, !fromFoldersView)
                isChecked = config.hasCustomViewType(pathToUse)
            }
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok) { _, _ -> dialogConfirmed() }
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this)
            }
    }

    private fun dialogConfirmed() {
        val viewType =
            if (binding.changeViewTypeDialogRadio.checkedRadioButtonId == binding.changeViewTypeDialogRadioGrid.id) {
                VIEW_TYPE_GRID
            } else {
                VIEW_TYPE_LIST
            }

        if (fromFoldersView) {
            config.viewTypeFolders = viewType
            config.groupDirectSubfolders =
                binding.changeViewTypeDialogGroupDirectSubfolders.isChecked
        } else {
            if (binding.changeViewTypeDialogUseForThisFolder.isChecked) {
                config.saveFolderViewType(pathToUse, viewType)
            } else {
                config.removeFolderViewType(pathToUse)
                config.viewTypeFiles = viewType
            }
        }


        callback()
    }
}
