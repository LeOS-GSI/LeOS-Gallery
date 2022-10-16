package ca.on.sudbury.hojat.smartgallery.dialogs

import android.app.Activity
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.R.id.conflict_dialog_radio_keep_both
import ca.on.sudbury.hojat.smartgallery.R.id.conflict_dialog_radio_merge
import ca.on.sudbury.hojat.smartgallery.R.id.conflict_dialog_radio_skip
import ca.on.sudbury.hojat.smartgallery.databinding.DialogFileConflictBinding
import ca.on.sudbury.hojat.smartgallery.extensions.baseConfig
import ca.on.sudbury.hojat.smartgallery.extensions.beVisibleIf
import ca.on.sudbury.hojat.smartgallery.extensions.getAlertDialogBuilder
import ca.on.sudbury.hojat.smartgallery.extensions.setupDialogStuff
import ca.on.sudbury.hojat.smartgallery.helpers.CONFLICT_KEEP_BOTH
import ca.on.sudbury.hojat.smartgallery.helpers.CONFLICT_MERGE
import ca.on.sudbury.hojat.smartgallery.helpers.CONFLICT_OVERWRITE
import ca.on.sudbury.hojat.smartgallery.helpers.CONFLICT_SKIP
import ca.on.sudbury.hojat.smartgallery.models.FileDirItem
import timber.log.Timber

/**
 * where it's called ?
 */
class FileConflictDialog(
    val activity: Activity,
    val fileDirItem: FileDirItem,
    private val showApplyToAllCheckbox: Boolean,
    val callback: (resolution: Int, applyForAll: Boolean) -> Unit
) {
    val binding = DialogFileConflictBinding.inflate(activity.layoutInflater)

    init {
        Timber.d("Hojat Ghasemi : FileConflictDialog was called")
        binding.apply {
            val stringBase =
                if (fileDirItem.isDirectory) R.string.folder_already_exists else R.string.file_already_exists
            conflictDialogTitle.text =
                String.format(activity.getString(stringBase), fileDirItem.name)
            conflictDialogApplyToAll.isChecked = activity.baseConfig.lastConflictApplyToAll
            conflictDialogApplyToAll.beVisibleIf(showApplyToAllCheckbox)
            conflictDialogDivider.root.beVisibleIf(showApplyToAllCheckbox)
            conflictDialogRadioMerge.beVisibleIf(fileDirItem.isDirectory)

            val resolutionButton = when (activity.baseConfig.lastConflictResolution) {
                CONFLICT_OVERWRITE -> conflictDialogRadioOverwrite
                CONFLICT_MERGE -> conflictDialogRadioMerge
                else -> conflictDialogRadioSkip
            }
            resolutionButton.isChecked = true
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok) { _, _ -> dialogConfirmed() }
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this)
            }
    }

    private fun dialogConfirmed() {
        val resolution = when (binding.conflictDialogRadioGroup.checkedRadioButtonId) {
            conflict_dialog_radio_skip -> CONFLICT_SKIP
            conflict_dialog_radio_merge -> CONFLICT_MERGE
            conflict_dialog_radio_keep_both -> CONFLICT_KEEP_BOTH
            else -> CONFLICT_OVERWRITE
        }

        val applyToAll = binding.conflictDialogApplyToAll.isChecked
        activity.baseConfig.apply {
            lastConflictApplyToAll = applyToAll
            lastConflictResolution = resolution
        }

        callback(resolution, applyToAll)
    }
}
