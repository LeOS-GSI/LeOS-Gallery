package ca.on.sudbury.hojat.smartgallery.dialogs

import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import ca.on.sudbury.hojat.smartgallery.extensions.getDoesFilePathExist
import ca.on.sudbury.hojat.smartgallery.extensions.getFilenameFromPath
import ca.on.sudbury.hojat.smartgallery.extensions.getIsPathDirectory
import ca.on.sudbury.hojat.smartgallery.extensions.getParentPath
import ca.on.sudbury.hojat.smartgallery.extensions.isAValidFilename
import ca.on.sudbury.hojat.smartgallery.extensions.setupDialogStuff
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.databinding.DialogRenameItemBinding
import ca.on.sudbury.hojat.smartgallery.extensions.renameFile
import ca.on.sudbury.hojat.smartgallery.usecases.ShowKeyboardUseCase
import timber.log.Timber

/**
 * The dialog for renaming pictures, videos, and folders is created via this class.
 */
class RenameItemDialog(
    val activity: BaseSimpleActivity,
    val path: String,
    val callback: (newPath: String) -> Unit
) {
    init {
        Timber.d("Hojat Ghasemi : RenameItemDialog was called")

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
                renameItemExtensionHint.visibility = View.GONE
            }

            renameItemName.setText(name)
        }

        AlertDialog.Builder(activity)
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .create().apply {
                activity.setupDialogStuff(binding.root, this, R.string.rename) {
                    ShowKeyboardUseCase(this, binding.renameItemName)
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        if (ignoreClicks) {
                            return@setOnClickListener
                        }

                        var newName = binding.renameItemName.text.toString().trim()
                        val newExtension = binding.renameItemExtension.text.toString().trim()

                        if (newName.isEmpty()) {
                            Toast.makeText(activity, R.string.empty_name,Toast.LENGTH_LONG).show()
                            return@setOnClickListener
                        }

                        if (!newName.isAValidFilename()) {
                            Toast.makeText(activity, R.string.invalid_name,Toast.LENGTH_LONG).show()
                            return@setOnClickListener
                        }

                        val updatedPaths = ArrayList<String>()
                        updatedPaths.add(path)
                        if (newExtension.isNotEmpty()) {
                            newName += ".$newExtension"
                        }

                        if (!activity.getDoesFilePathExist(path)) {
                            Toast.makeText(
                                activity,
                                String.format(
                                    activity.getString(R.string.source_file_doesnt_exist),
                                    path
                                ),Toast.LENGTH_LONG).show()
                            return@setOnClickListener
                        }

                        val newPath = "${path.getParentPath()}/$newName"

                        if (path == newPath) {
                            Toast.makeText(activity, R.string.name_taken,Toast.LENGTH_LONG).show()
                            return@setOnClickListener
                        }

                        if (!path.equals(
                                newPath,
                                ignoreCase = true
                            ) && activity.getDoesFilePathExist(newPath)
                        ) {
                            Toast.makeText(activity, R.string.name_taken,Toast.LENGTH_LONG).show()
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
                                Toast.makeText(activity, R.string.unknown_error_occurred,Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
    }
}
