package com.simplemobiletools.gallery.pro.dialogs

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.extensions.getAlertDialogBuilder
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.gallery.pro.R
import com.simplemobiletools.gallery.pro.databinding.DialogDeleteWithRememberBinding

class DeleteWithRememberDialog(val activity: Activity, message: String, val callback: (remember: Boolean) -> Unit) {

    // we create the binding by referencing the owner Activity
    var binding = DialogDeleteWithRememberBinding.inflate(activity.layoutInflater)

    private var dialog: AlertDialog? = null


    init {
        binding.deleteRememberTitle.text = message
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
        callback(binding.deleteRememberCheckbox.isChecked)
    }
}
