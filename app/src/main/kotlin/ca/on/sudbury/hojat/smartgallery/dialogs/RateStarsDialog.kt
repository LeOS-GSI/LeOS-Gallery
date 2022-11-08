package ca.on.sudbury.hojat.smartgallery.dialogs

import android.app.Activity
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import ca.on.sudbury.hojat.smartgallery.extensions.baseConfig
import ca.on.sudbury.hojat.smartgallery.extensions.getAlertDialogBuilder
import ca.on.sudbury.hojat.smartgallery.extensions.getProperPrimaryColor
import ca.on.sudbury.hojat.smartgallery.extensions.redirectToRateUs
import ca.on.sudbury.hojat.smartgallery.extensions.setupDialogStuff
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.databinding.DialogRateStarsBinding
import ca.on.sudbury.hojat.smartgallery.usecases.ApplyColorFilterUseCase
import timber.log.Timber

/**
 * In settings page, in the "Help us" section click on "Rate us".
 */
class RateStarsDialog(val activity: Activity) {
    private var dialog: AlertDialog? = null

    init {
        Timber.d("Hojat Ghasemi : RateStarsDialog was called")
        val binding = DialogRateStarsBinding.inflate(activity.layoutInflater).apply {
            val primaryColor = activity.getProperPrimaryColor()
            arrayOf(rateStar1, rateStar2, rateStar3, rateStar4, rateStar5).forEach {
                ApplyColorFilterUseCase(it, primaryColor)
            }

            rateStar1.setOnClickListener { dialogCancelled(true) }
            rateStar2.setOnClickListener { dialogCancelled(true) }
            rateStar3.setOnClickListener { dialogCancelled(true) }
            rateStar4.setOnClickListener { dialogCancelled(true) }
            rateStar5.setOnClickListener {
                activity.redirectToRateUs()
                dialogCancelled(true)
            }
        }

        activity.getAlertDialogBuilder()
            .setNegativeButton(R.string.later) { _, _ -> dialogCancelled(false) }
            .setOnCancelListener { dialogCancelled(false) }
            .apply {
                activity.setupDialogStuff(
                    binding.root,
                    this,
                    cancelOnTouchOutside = false
                ) { alertDialog ->
                    dialog = alertDialog
                }
            }
    }

    private fun dialogCancelled(showThankYou: Boolean) {
        dialog?.dismiss()
        if (showThankYou) {
            Toast.makeText(activity, R.string.thank_you, Toast.LENGTH_LONG).show()
            activity.baseConfig.wasAppRated = true
        }
    }
}
