package com.simplemobiletools.gallery.pro.dialogs

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.extensions.getAlertDialogBuilder
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.gallery.pro.R
import com.simplemobiletools.gallery.pro.databinding.DialogConfirmDeleteFolderBinding

class ConfirmDeleteFolderDialog(
    activity: Activity,
    message: String,
    warningMessage: String,
    val callback: () -> Unit
) {

    // we create the binding by referencing the owner Activity
    var binding = DialogConfirmDeleteFolderBinding.inflate(activity.layoutInflater)

    private var dialog: AlertDialog? = null

    init {
        binding.message.text = message
        binding.messageWarning.text = warningMessage

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.yes) { _, _ -> dialogConfirmed() }
            .setNegativeButton(R.string.no, null)
            .apply {
                activity.setupDialogStuff(binding.root, this) { alertDialog ->
                    dialog = alertDialog
                }
            }
    }

    private fun dialogConfirmed() {
        dialog?.dismiss()
        callback()
    }
}
