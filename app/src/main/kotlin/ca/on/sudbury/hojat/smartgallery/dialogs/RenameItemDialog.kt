package ca.on.sudbury.hojat.smartgallery.dialogs

import androidx.appcompat.app.AlertDialog
import ca.on.sudbury.hojat.smartgallery.extensions.beGone
import ca.on.sudbury.hojat.smartgallery.extensions.getDoesFilePathExist
import ca.on.sudbury.hojat.smartgallery.extensions.getFilenameFromPath
import ca.on.sudbury.hojat.smartgallery.extensions.getIsPathDirectory
import ca.on.sudbury.hojat.smartgallery.extensions.getParentPath
import ca.on.sudbury.hojat.smartgallery.extensions.isAValidFilename
import ca.on.sudbury.hojat.smartgallery.extensions.setupDialogStuff
import ca.on.sudbury.hojat.smartgallery.extensions.showKeyboard
import ca.on.sudbury.hojat.smartgallery.extensions.toast
import ca.on.sudbury.hojat.smartgallery.extensions.value
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.databinding.DialogRenameItemBinding
import ca.on.sudbury.hojat.smartgallery.extensions.renameFile

/**
 * The dialog for renaming pictures, videos, and folders is created via this class.
 */
class RenameItemDialog(
    val activity: BaseSimpleActivity,
    val path: String,
    val callback: (newPath: String) -> Unit
) {
    init {
        var ignoreClicks = false
        val fullName = path.getFilenameFromPath()
        val dotAt = fullName.lastIndexOf(".")
        var name = fullName
        val binding = DialogRenameItemBinding.inflate(activity.layoutInflater).apply {
            if (dotAt > 0 && !activity.getIsPathDirectory(path)) {
                name = fullName.substring(0, dotAt)
                val extension = fullName.substring(dotAt + 1)
                renameItemExtension.setText(extension)
            } else {
                renameItemExtensionHint.beGone()
            }

            renameItemName.setText(name)
        }

        AlertDialog.Builder(activity)
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .create().apply {
                activity.setupDialogStuff(binding.root, this, R.string.rename) {
                    showKeyboard(binding.renameItemName)
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        if (ignoreClicks) {
                            return@setOnClickListener
                        }

                        var newName = binding.renameItemName.value
                        val newExtension = binding.renameItemExtension.value

                        if (newName.isEmpty()) {
                            activity.toast(R.string.empty_name)
                            return@setOnClickListener
                        }

                        if (!newName.isAValidFilename()) {
                            activity.toast(R.string.invalid_name)
                            return@setOnClickListener
                        }

                        val updatedPaths = ArrayList<String>()
                        updatedPaths.add(path)
                        if (newExtension.isNotEmpty()) {
                            newName += ".$newExtension"
                        }

                        if (!activity.getDoesFilePathExist(path)) {
                            activity.toast(
                                String.format(
                                    activity.getString(R.string.source_file_doesnt_exist),
                                    path
                                )
                            )
                            return@setOnClickListener
                        }

                        val newPath = "${path.getParentPath()}/$newName"

                        if (path == newPath) {
                            activity.toast(R.string.name_taken)
                            return@setOnClickListener
                        }

                        if (!path.equals(
                                newPath,
                                ignoreCase = true
                            ) && activity.getDoesFilePathExist(newPath)
                        ) {
                            activity.toast(R.string.name_taken)
                            return@setOnClickListener
                        }

                        updatedPaths.add(newPath)
                        ignoreClicks = true
                        activity.renameFile(path, newPath, false) { success, _ ->
                            ignoreClicks = false
                            if (success) {
                                callback(newPath)
                                dismiss()
                            } else {
                                activity.toast(R.string.unknown_error_occurred)
                            }
                        }
                    }
                }
            }
    }
}
