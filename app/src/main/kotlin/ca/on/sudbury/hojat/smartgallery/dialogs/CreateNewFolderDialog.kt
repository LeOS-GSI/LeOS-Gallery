package ca.on.sudbury.hojat.smartgallery.dialogs

import android.annotation.SuppressLint
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.databinding.DialogCreateNewFolderBinding
import ca.on.sudbury.hojat.smartgallery.extensions.getAlertDialogBuilder
import ca.on.sudbury.hojat.smartgallery.extensions.setupDialogStuff
import ca.on.sudbury.hojat.smartgallery.extensions.isRestrictedSAFOnlyRoot
import ca.on.sudbury.hojat.smartgallery.extensions.createAndroidSAFDirectory
import ca.on.sudbury.hojat.smartgallery.extensions.isAccessibleWithSAFSdk30
import ca.on.sudbury.hojat.smartgallery.extensions.createSAFDirectorySdk30
import ca.on.sudbury.hojat.smartgallery.extensions.getDocumentFile
import ca.on.sudbury.hojat.smartgallery.extensions.getParentPath
import ca.on.sudbury.hojat.smartgallery.extensions.getFilenameFromPath
import ca.on.sudbury.hojat.smartgallery.extensions.isAStorageRootFolder
import ca.on.sudbury.hojat.smartgallery.extensions.isAValidFilename
import ca.on.sudbury.hojat.smartgallery.extensions.humanizePath
import ca.on.sudbury.hojat.smartgallery.extensions.isSDCardSetAsDefaultStorage
import ca.on.sudbury.hojat.smartgallery.usecases.IsRPlusUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsPathOnOtgUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsPathOnSdUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.ShowKeyboardUseCase
import timber.log.Timber
import java.io.File

/**
 * In the main page, click on 3 dots and choose "Create new folder".
 */
@SuppressLint("SetTextI18n")
class CreateNewFolderDialog(
    val activity: BaseSimpleActivity,
    val path: String,
    val callback: (path: String) -> Unit
) {
    init {
        Timber.d("Hojat Ghasemi : CreateNewFolderDialog was called")
        val binding = DialogCreateNewFolderBinding.inflate(activity.layoutInflater)
        binding.folderPath.text = "${activity.humanizePath(path).trimEnd('/')}/"
        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(
                    binding.root,
                    this,
                    R.string.create_new_folder
                ) { alertDialog ->
                    ShowKeyboardUseCase(alertDialog, binding.folderName)
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(View.OnClickListener {
                            val name = binding.folderName.text.toString().trim()
                            when {
                                name.isEmpty() -> {
                                    Toast.makeText(
                                        activity,
                                        R.string.empty_name, Toast.LENGTH_LONG
                                    ).show()
                                }
                                name.isAValidFilename() -> {
                                    val file = File(path, name)
                                    if (file.exists()) {
                                        Toast.makeText(
                                            activity,
                                            R.string.name_taken,
                                            Toast.LENGTH_LONG
                                        ).show()
                                        return@OnClickListener
                                    }

                                    createFolder("$path/$name", alertDialog)
                                }
                                else -> Toast.makeText(
                                    activity,
                                    R.string.invalid_name,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        })
                }
            }
    }

    private fun createFolder(path: String, alertDialog: AlertDialog) {
        try {
            when {
                activity.isRestrictedSAFOnlyRoot(path) && activity.createAndroidSAFDirectory(path) -> sendSuccess(
                    alertDialog,
                    path
                )
                activity.isAccessibleWithSAFSdk30(path) -> activity.handleSAFDialogSdk30(path) {
                    if (it && activity.createSAFDirectorySdk30(path)) {
                        sendSuccess(alertDialog, path)
                    }
                }
                with(activity) {
                    !IsRPlusUseCase() && (
                            IsPathOnSdUseCase(this, path) ||
                                    IsPathOnOtgUseCase(this, path))
                            && !isSDCardSetAsDefaultStorage()
                } -> activity.handleSAFDialog(
                    path
                ) {
                    if (it) {
                        try {
                            val documentFile = activity.getDocumentFile(path.getParentPath())
                            val newDir = documentFile?.createDirectory(path.getFilenameFromPath())
                                ?: activity.getDocumentFile(path)
                            if (newDir != null) {
                                sendSuccess(alertDialog, path)
                            } else {
                                Toast.makeText(
                                    activity,
                                    R.string.unknown_error_occurred,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } catch (e: SecurityException) {
                            Toast.makeText(activity, e.toString(), Toast.LENGTH_LONG).show()
                        }
                    }
                }
                File(path).mkdirs() -> sendSuccess(alertDialog, path)
                IsRPlusUseCase() && activity.isAStorageRootFolder(path.getParentPath()) -> activity.handleSAFCreateDocumentDialogSdk30(
                    path
                ) {
                    if (it) {
                        sendSuccess(alertDialog, path)
                    }
                }
                else ->
                    Toast.makeText(
                        activity,
                        activity.getString(
                            R.string.could_not_create_folder,
                            path.getFilenameFromPath()
                        ), Toast.LENGTH_LONG
                    ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(activity, e.toString(), Toast.LENGTH_LONG).show()
        }
    }

    private fun sendSuccess(alertDialog: AlertDialog, path: String) {
        callback(path.trimEnd('/'))
        alertDialog.dismiss()
    }
}
