package ca.on.sudbury.hojat.smartgallery.dialogs

import androidx.appcompat.app.AlertDialog
import ca.on.sudbury.hojat.smartgallery.extensions.getDoesFilePathExist
import ca.on.sudbury.hojat.smartgallery.extensions.getFilenameFromPath
import ca.on.sudbury.hojat.smartgallery.extensions.getParentPath
import ca.on.sudbury.hojat.smartgallery.extensions.isAValidFilename
import ca.on.sudbury.hojat.smartgallery.extensions.setupDialogStuff
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.databinding.DialogRenameItemsBinding
import ca.on.sudbury.hojat.smartgallery.extensions.renameFile
import ca.on.sudbury.hojat.smartgallery.usecases.GetFileExtensionUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsPathOnSdUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.ShowKeyboardUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.ShowSafeToastUseCase
import timber.log.Timber

/**
 * I couldn't ascertain where exactly this dialog is being used.
 */
class RenameItemsDialog(
    val activity: BaseSimpleActivity,
    val paths: ArrayList<String>,
    val callback: () -> Unit
) {
    init {
        Timber.d("Hojat Ghasemi : RenameItemsDialog was called")
        var ignoreClicks = false
        val binding = DialogRenameItemsBinding.inflate(activity.layoutInflater)

        AlertDialog.Builder(activity)
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .create().apply {
                activity.setupDialogStuff(binding.root, this, R.string.rename) {
                    ShowKeyboardUseCase(this, binding.renameItemsValue)
                    getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        if (ignoreClicks) {
                            return@setOnClickListener
                        }

                        val valueToAdd = binding.renameItemsValue.text.toString()
                        val append =
                            binding.renameItemsRadioGroup.checkedRadioButtonId == binding.renameItemsRadioAppend.id

                        if (valueToAdd.isEmpty()) {
                            callback()
                            dismiss()
                            return@setOnClickListener
                        }

                        if (!valueToAdd.isAValidFilename()) {
                            ShowSafeToastUseCase(activity, R.string.invalid_name)
                            return@setOnClickListener
                        }

                        val validPaths = paths.filter { activity.getDoesFilePathExist(it) }
                        val sdFilePath = validPaths.firstOrNull { IsPathOnSdUseCase(activity, it) }
                            ?: validPaths.firstOrNull()
                        if (sdFilePath == null) {
                            ShowSafeToastUseCase(activity, R.string.unknown_error_occurred)
                            dismiss()
                            return@setOnClickListener
                        }

                        activity.handleSAFDialog(sdFilePath) {
                            if (!it) {
                                return@handleSAFDialog
                            }

                            ignoreClicks = true
                            var pathsCnt = validPaths.size
                            for (path in validPaths) {
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

                                if (activity.getDoesFilePathExist(newPath)) {
                                    continue
                                }

                                activity.renameFile(
                                    path,
                                    newPath,
                                    true
                                ) { success, _ ->
                                    if (success) {
                                        pathsCnt--
                                        if (pathsCnt == 0) {
                                            callback()
                                            dismiss()
                                        }
                                    } else {
                                        ignoreClicks = false
                                        ShowSafeToastUseCase(
                                            activity,
                                            R.string.unknown_error_occurred
                                        )
                                        dismiss()
                                    }
                                }
                            }
                        }
                    }
                }
            }
    }
}
