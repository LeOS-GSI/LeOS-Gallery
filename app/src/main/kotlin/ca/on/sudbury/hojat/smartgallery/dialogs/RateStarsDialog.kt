package ca.on.sudbury.hojat.smartgallery.dialogs


import android.annotation.SuppressLint
import android.app.Activity
import androidx.appcompat.app.AlertDialog
import ca.on.sudbury.hojat.smartgallery.extensions.applyColorFilter
import ca.on.sudbury.hojat.smartgallery.extensions.baseConfig
import ca.on.sudbury.hojat.smartgallery.extensions.getAlertDialogBuilder
import ca.on.sudbury.hojat.smartgallery.extensions.getProperPrimaryColor
import ca.on.sudbury.hojat.smartgallery.extensions.redirectToRateUs
import ca.on.sudbury.hojat.smartgallery.extensions.setupDialogStuff
import ca.on.sudbury.hojat.smartgallery.extensions.toast
import com.simplemobiletools.commons.R
import kotlinx.android.synthetic.main.dialog_rate_stars.view.*

@SuppressLint("InflateParams")
class RateStarsDialog(val activity: Activity) {
    private var dialog: AlertDialog? = null

    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_rate_stars, null).apply {
            val primaryColor = activity.getProperPrimaryColor()
            arrayOf(rate_star_1, rate_star_2, rate_star_3, rate_star_4, rate_star_5).forEach {
                it.applyColorFilter(primaryColor)
            }

            rate_star_1.setOnClickListener { dialogCancelled(true) }
            rate_star_2.setOnClickListener { dialogCancelled(true) }
            rate_star_3.setOnClickListener { dialogCancelled(true) }
            rate_star_4.setOnClickListener { dialogCancelled(true) }
            rate_star_5.setOnClickListener {
                activity.redirectToRateUs()
                dialogCancelled(true)
            }
        }

        activity.getAlertDialogBuilder()
            .setNegativeButton(R.string.later) { _, _ -> dialogCancelled(false) }
            .setOnCancelListener { dialogCancelled(false) }
            .apply {
                activity.setupDialogStuff(view, this, cancelOnTouchOutside = false) { alertDialog ->
                    dialog = alertDialog
                }
            }
    }

    private fun dialogCancelled(showThankYou: Boolean) {
        dialog?.dismiss()
        if (showThankYou) {
            activity.toast(R.string.thank_you)
            activity.baseConfig.wasAppRated = true
        }
    }
}
