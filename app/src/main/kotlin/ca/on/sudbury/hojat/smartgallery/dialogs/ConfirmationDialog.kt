package ca.on.sudbury.hojat.smartgallery.dialogs

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.databinding.DialogMessageBinding
import ca.on.sudbury.hojat.smartgallery.extensions.getAlertDialogBuilder
import ca.on.sudbury.hojat.smartgallery.extensions.setupDialogStuff
import timber.log.Timber

/**
 * A simple dialog without any view, just a messageId, a positive button and optionally a negative button
 * I couldn't find the place this dialog is used in app.
 *
 * @param activity has to be activity context to avoid some Theme.AppCompat issues
 * @param message the dialogs message, can be any String. If empty, messageId is used
 * @param messageId the dialogs messageId ID. Used only if message is empty
 * @param positive positive buttons text ID
 * @param negative negative buttons text ID (optional)
 * @param callback an anonymous function
 */

class ConfirmationDialog(
    activity: Activity,
    message: String = "",
    messageId: Int = R.string.proceed_with_deletion,
    positive: Int = R.string.yes,
    negative: Int = R.string.no,
    private val cancelOnTouchOutside: Boolean = true,
    val callback: () -> Unit
) {
    private var dialog: AlertDialog? = null

    init {
        Timber.d("Hojat Ghasemi : ConfirmationDialog was called")
        val binding = DialogMessageBinding.inflate(activity.layoutInflater)
        binding.message.text =
            message.ifEmpty { activity.resources.getString(messageId) }

        val builder = activity.getAlertDialogBuilder()
            .setPositiveButton(positive) { _, _ -> dialogConfirmed() }

        if (negative != 0) {
            builder.setNegativeButton(negative, null)
        }

        builder.apply {
            activity.setupDialogStuff(
                binding.root,
                this,
                cancelOnTouchOutside = cancelOnTouchOutside
            ) { alertDialog ->
                dialog = alertDialog
            }
        }
    }

    private fun dialogConfirmed() {
        dialog?.dismiss()
        callback()
    }
}
