package ca.on.sudbury.hojat.smartgallery.dialogs

import android.annotation.SuppressLint
import android.app.Activity
import android.text.Html
import android.text.method.LinkMovementMethod
import androidx.appcompat.app.AlertDialog
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.extensions.baseConfig
import ca.on.sudbury.hojat.smartgallery.extensions.launchPurchaseThankYouIntent
import ca.on.sudbury.hojat.smartgallery.extensions.removeUnderlines
import ca.on.sudbury.hojat.smartgallery.extensions.setupDialogStuff
import kotlinx.android.synthetic.main.dialog_purchase_thank_you.view.*

@SuppressLint("InflateParams")
class PurchaseThankYouDialog(val activity: Activity) {
    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_purchase_thank_you, null).apply {
            var text = activity.getString(R.string.purchase_thank_you)
            if (activity.baseConfig.appId.removeSuffix(".debug").endsWith(".pro")) {
                text += "<br><br>${activity.getString(R.string.shared_theme_note)}"
            }

            purchase_thank_you.text = Html.fromHtml(text)
            purchase_thank_you.movementMethod = LinkMovementMethod.getInstance()
            purchase_thank_you.removeUnderlines()
        }

        AlertDialog.Builder(activity)
            .setPositiveButton(R.string.purchase) { _, _ -> activity.launchPurchaseThankYouIntent() }
            .setNegativeButton(R.string.cancel, null)
            .create().apply {
                activity.setupDialogStuff(view, this, cancelOnTouchOutside = false)
            }
    }
}
