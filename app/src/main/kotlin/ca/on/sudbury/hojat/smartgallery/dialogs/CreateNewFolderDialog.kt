package ca.on.sudbury.hojat.smartgallery.dialogs

import android.annotation.SuppressLint
import android.view.View
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
import ca.on.sudbury.hojat.smartgallery.extensions.showKeyboard
import ca.on.sudbury.hojat.smartgallery.extensions.isAValidFilename
import ca.on.sudbury.hojat.smartgallery.extensions.humanizePath
import ca.on.sudbury.hojat.smartgallery.extensions.isPathOnOTG
import ca.on.sudbury.hojat.smartgallery.extensions.isPathOnSD
import ca.on.sudbury.hojat.smartgallery.extensions.isSDCardSetAsDefaultStorage
import ca.on.sudbury.hojat.smartgallery.photoedit.usecases.IsRPlusUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.ShowSafeToastUseCase
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
                    alertDialog.showKeyboard(binding.folderName)
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(View.OnClickListener {
                            val name = binding.folderName.text.toString().trim()
                            when {
                                name.isEmpty() -> ShowSafeToastUseCase(
                                    activity,
                                    R.string.empty_name
                                )
                                name.isAValidFilename() -> {
                                    val file = File(path, name)
                                    if (file.exists()) {
                                        ShowSafeToastUseCase(activity, R.string.name_taken)
                                        return@OnClickListener
                                    }

                                    createFolder("$path/$name", alertDialog)
                                }
                                else -> ShowSafeToastUseCase(activity, R.string.invalid_name)
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
                with(activity) { !IsRPlusUseCase() && (isPathOnSD(path) || isPathOnOTG(path)) && !isSDCardSetAsDefaultStorage() } -> activity.handleSAFDialog(
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
                                ShowSafeToastUseCase(activity, R.string.unknown_error_occurred)
                            }
                        } catch (e: SecurityException) {
                            ShowSafeToastUseCase(activity, e.toString())
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
                else -> ShowSafeToastUseCase(
                    activity,
                    activity.getString(
                        R.string.could_not_create_folder,
                        path.getFilenameFromPath()
                    )
                )
            }
        } catch (e: Exception) {
            ShowSafeToastUseCase(activity, e.toString())
        }
    }

    private fun sendSuccess(alertDialog: AlertDialog, path: String) {
        callback(path.trimEnd('/'))
        alertDialog.dismiss()
    }
}
