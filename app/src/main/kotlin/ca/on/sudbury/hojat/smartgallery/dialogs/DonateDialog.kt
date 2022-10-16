package ca.on.sudbury.hojat.smartgallery.dialogs

import android.app.Activity
import android.text.Html
import android.text.method.LinkMovementMethod
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.databinding.DialogTextviewBinding
import ca.on.sudbury.hojat.smartgallery.extensions.getAlertDialogBuilder
import ca.on.sudbury.hojat.smartgallery.extensions.launchViewIntent
import ca.on.sudbury.hojat.smartgallery.extensions.setupDialogStuff
import timber.log.Timber

/**
 * where it's called ?
 */
class DonateDialog(val activity: Activity) {
    init {
        Timber.d("Hojat Ghasemi : DonateDialog was called")
        val binding = DialogTextviewBinding.inflate(activity.layoutInflater).apply {
            textView.text = Html.fromHtml(activity.getString(R.string.donate_please))
            textView.movementMethod = LinkMovementMethod.getInstance()
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.purchase) { _, _ -> activity.launchViewIntent(R.string.thank_you_url) }
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this, cancelOnTouchOutside = false)
            }
    }
}
