package ca.on.sudbury.hojat.smartgallery.dialogs

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.databinding.DialogMessageBinding
import ca.on.sudbury.hojat.smartgallery.extensions.getAlertDialogBuilder
import ca.on.sudbury.hojat.smartgallery.extensions.setupDialogStuff
import timber.log.Timber

/**
 * similar to ConfirmationDialog, but has a callback for negative button too.
 *
 * It's called from various places:
 *
 * 1- In the "About" page, click on "email"; the resulting dialog is created via this class.
 *
 *
 *
 */
class ConfirmationAdvancedDialog(
    activity: Activity,
    message: String = "",
    messageId: Int = R.string.proceed_with_deletion,
    positive: Int = R.string.yes,
    negative: Int = R.string.no,
    private val cancelOnTouchOutside: Boolean = true,
    val callback: (result: Boolean) -> Unit
) {
    private var dialog: AlertDialog? = null

    init {
        Timber.d("Hojat Ghasemi : ConfirmationAdvancedDialog was called")
        val binding = DialogMessageBinding.inflate(activity.layoutInflater)
        binding.message.text = message.ifEmpty { activity.resources.getString(messageId) }

        val builder = activity.getAlertDialogBuilder()
            .setPositiveButton(positive) { _, _ -> positivePressed() }

        if (negative != 0) {
            builder.setNegativeButton(negative) { _, _ -> negativePressed() }
        }

        if (!cancelOnTouchOutside) {
            builder.setOnCancelListener { negativePressed() }
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

    private fun positivePressed() {
        dialog?.dismiss()
        callback(true)
    }

    private fun negativePressed() {
        dialog?.dismiss()
        callback(false)
    }
}
