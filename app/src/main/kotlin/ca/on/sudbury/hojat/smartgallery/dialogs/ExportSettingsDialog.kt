package ca.on.sudbury.hojat.smartgallery.dialogs

import android.annotation.SuppressLint
import androidx.appcompat.app.AlertDialog
import ca.on.sudbury.hojat.smartgallery.extensions.baseConfig
import ca.on.sudbury.hojat.smartgallery.extensions.beGone
import ca.on.sudbury.hojat.smartgallery.extensions.getAlertDialogBuilder
import ca.on.sudbury.hojat.smartgallery.extensions.getDoesFilePathExist
import ca.on.sudbury.hojat.smartgallery.extensions.getFilenameFromPath
import ca.on.sudbury.hojat.smartgallery.extensions.humanizePath
import ca.on.sudbury.hojat.smartgallery.extensions.internalStoragePath
import ca.on.sudbury.hojat.smartgallery.extensions.isAValidFilename
import ca.on.sudbury.hojat.smartgallery.extensions.setupDialogStuff
import ca.on.sudbury.hojat.smartgallery.extensions.toast
import ca.on.sudbury.hojat.smartgallery.extensions.value
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import kotlinx.android.synthetic.main.dialog_export_settings.view.*

@SuppressLint("InflateParams")
class ExportSettingsDialog(
    val activity: BaseSimpleActivity, private val defaultFilename: String, private val hidePath: Boolean,
    callback: (path: String, filename: String) -> Unit
) {
    init {
        val lastUsedFolder = activity.baseConfig.lastExportedSettingsFolder
        var folder =
            if (lastUsedFolder.isNotEmpty() && activity.getDoesFilePathExist(lastUsedFolder)) {
                lastUsedFolder
            } else {
                activity.internalStoragePath
            }

        val view = activity.layoutInflater.inflate(R.layout.dialog_export_settings, null).apply {
            export_settings_filename.setText(defaultFilename.removeSuffix(".txt"))

            if (hidePath) {
                export_settings_path_label.beGone()
                export_settings_path.beGone()
            } else {
                export_settings_path.text = activity.humanizePath(folder)
                export_settings_path.setOnClickListener {
                    FilePickerDialog(activity, folder, false, showFAB = true) {
                        export_settings_path.text = activity.humanizePath(it)
                        folder = it
                    }
                }
            }
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view, this, R.string.export_settings) { alertDialog ->
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        var filename = view.export_settings_filename.value
                        if (filename.isEmpty()) {
                            activity.toast(R.string.filename_cannot_be_empty)
                            return@setOnClickListener
                        }

                        filename += ".txt"
                        val newPath = "${folder.trimEnd('/')}/$filename"
                        if (!newPath.getFilenameFromPath().isAValidFilename()) {
                            activity.toast(R.string.filename_invalid_characters)
                            return@setOnClickListener
                        }

                        activity.baseConfig.lastExportedSettingsFolder = folder
                        if (!hidePath && activity.getDoesFilePathExist(newPath)) {
                            val title = String.format(
                                activity.getString(R.string.file_already_exists_overwrite),
                                newPath.getFilenameFromPath()
                            )
                            ConfirmationDialog(activity, title) {
                                callback(newPath, filename)
                                alertDialog.dismiss()
                            }
                        } else {
                            callback(newPath, filename)
                            alertDialog.dismiss()
                        }
                    }
                }
            }
    }
}
