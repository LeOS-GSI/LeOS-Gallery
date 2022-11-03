package ca.on.sudbury.hojat.smartgallery.dialogs

import android.app.Activity
import android.text.Html
import android.text.method.LinkMovementMethod
import androidx.appcompat.app.AlertDialog
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.databinding.DialogTextviewBinding
import ca.on.sudbury.hojat.smartgallery.extensions.getAlertDialogBuilder
import ca.on.sudbury.hojat.smartgallery.extensions.launchViewIntent
import ca.on.sudbury.hojat.smartgallery.extensions.setupDialogStuff
import timber.log.Timber

/**
 * I couldn't find the place this dialog is being used. That's
 * why I've left that timber log in the init block for now.
 */
class AppSideloadedDialog(
    val activity: Activity,
    val callback: () -> Unit
) {

    private var dialog: AlertDialog? = null
    private val url =
        "https://play.google.com/store/apps/details?id=${activity.getString(R.string.package_name)}"

    init {
        Timber.d("Hojat Ghasemi : AppSideloadedDialog was called")
        val binding = DialogTextviewBinding.inflate(activity.layoutInflater).apply {
            val text = String.format(activity.getString(R.string.sideloaded_app), url)
            textView.text = Html.fromHtml(text)
            textView.movementMethod = LinkMovementMethod.getInstance()
        }

        activity.getAlertDialogBuilder()
            .setNegativeButton(R.string.cancel) { _, _ -> negativePressed() }
            .setPositiveButton(R.string.download, null)
            .setOnCancelListener { negativePressed() }
            .apply {
                activity.setupDialogStuff(binding.root, this, R.string.app_corrupt) { alertDialog ->
                    dialog = alertDialog
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        downloadApp()
                    }
                }
            }
    }

    private fun downloadApp() {
        activity.launchViewIntent(url)
    }

    private fun negativePressed() {
        dialog?.dismiss()
        callback()
    }
}
