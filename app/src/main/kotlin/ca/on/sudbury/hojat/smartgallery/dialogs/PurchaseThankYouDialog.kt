package ca.on.sudbury.hojat.smartgallery.dialogs

import android.app.Activity
import android.text.Html
import android.text.method.LinkMovementMethod
import androidx.appcompat.app.AlertDialog
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.databinding.DialogPurchaseThankYouBinding
import ca.on.sudbury.hojat.smartgallery.extensions.baseConfig
import ca.on.sudbury.hojat.smartgallery.extensions.launchPurchaseThankYouIntent
import ca.on.sudbury.hojat.smartgallery.extensions.removeUnderlines
import ca.on.sudbury.hojat.smartgallery.extensions.setupDialogStuff

/**
 * Where this dialog is called ?
 */
class PurchaseThankYouDialog(val activity: Activity) {
    init {
        val binding = DialogPurchaseThankYouBinding.inflate(activity.layoutInflater).apply {
            var text = activity.getString(R.string.purchase_thank_you)
            if (activity.baseConfig.appId.removeSuffix(".debug").endsWith(".pro")) {
                text += "<br><br>${activity.getString(R.string.shared_theme_note)}"
            }

            purchaseThankYou.text = Html.fromHtml(text)
            purchaseThankYou.movementMethod = LinkMovementMethod.getInstance()
            purchaseThankYou.removeUnderlines()
        }

        AlertDialog.Builder(activity)
            .setPositiveButton(R.string.purchase) { _, _ -> activity.launchPurchaseThankYouIntent() }
            .setNegativeButton(R.string.cancel, null)
            .create().apply {
                activity.setupDialogStuff(binding.root, this, cancelOnTouchOutside = false)
            }
    }
}
