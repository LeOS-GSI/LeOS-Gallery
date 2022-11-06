package ca.on.sudbury.hojat.smartgallery.dialogs

import android.app.Activity
import android.text.format.DateFormat
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.R.id.change_date_time_dialog_radio_one
import ca.on.sudbury.hojat.smartgallery.R.id.change_date_time_dialog_radio_two
import ca.on.sudbury.hojat.smartgallery.R.id.change_date_time_dialog_radio_three
import ca.on.sudbury.hojat.smartgallery.R.id.change_date_time_dialog_radio_four
import ca.on.sudbury.hojat.smartgallery.R.id.change_date_time_dialog_radio_five
import ca.on.sudbury.hojat.smartgallery.R.id.change_date_time_dialog_radio_six
import ca.on.sudbury.hojat.smartgallery.R.id.change_date_time_dialog_radio_seven
import ca.on.sudbury.hojat.smartgallery.databinding.DialogChangeDateTimeFormatBinding
import ca.on.sudbury.hojat.smartgallery.extensions.baseConfig
import ca.on.sudbury.hojat.smartgallery.extensions.getAlertDialogBuilder
import ca.on.sudbury.hojat.smartgallery.extensions.setupDialogStuff
import ca.on.sudbury.hojat.smartgallery.helpers.SmartGalleryDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * In the Settings page, click on "Change date and time format". The dialog is created by this class.
 */
class ChangeDateTimeFormatDialog(
    val activity: Activity,
    val callback: () -> Unit
) {
    private val binding = DialogChangeDateTimeFormatBinding.inflate(activity.layoutInflater)
    private val sampleTS = 1613422500000    // February 15, 2021

    init {
        binding.apply {
            changeDateTimeDialogRadioOne.text = formatDateSample(SmartGalleryDateFormat.One.format)
            changeDateTimeDialogRadioTwo.text = formatDateSample(SmartGalleryDateFormat.Two.format)
            changeDateTimeDialogRadioThree.text =
                formatDateSample(SmartGalleryDateFormat.Three.format)
            changeDateTimeDialogRadioFour.text =
                formatDateSample(SmartGalleryDateFormat.Four.format)
            changeDateTimeDialogRadioFive.text =
                formatDateSample(SmartGalleryDateFormat.Five.format)
            changeDateTimeDialogRadioSix.text = formatDateSample(SmartGalleryDateFormat.Six.format)
            changeDateTimeDialogRadioSeven.text =
                formatDateSample(SmartGalleryDateFormat.Seven.format)
            changeDateTimeDialogRadioEight.text =
                formatDateSample(SmartGalleryDateFormat.Eight.format)
            changeDateTimeDialog24Hour.isChecked = activity.baseConfig.use24HourFormat

            val formatButton = when (activity.baseConfig.dateFormat) {
                SmartGalleryDateFormat.One.format -> changeDateTimeDialogRadioOne
                SmartGalleryDateFormat.Two.format -> changeDateTimeDialogRadioTwo
                SmartGalleryDateFormat.Three.format -> changeDateTimeDialogRadioThree
                SmartGalleryDateFormat.Four.format -> changeDateTimeDialogRadioFour
                SmartGalleryDateFormat.Five.format -> changeDateTimeDialogRadioFive
                SmartGalleryDateFormat.Six.format -> changeDateTimeDialogRadioSix
                SmartGalleryDateFormat.Seven.format -> changeDateTimeDialogRadioSeven
                else -> changeDateTimeDialogRadioEight
            }
            formatButton.isChecked = true
        }
        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok) { _, _ -> dialogConfirmed() }
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this)
            }
    }

    private fun dialogConfirmed() {
        activity.baseConfig.dateFormat =
            when (binding.changeDateTimeDialogRadioGroup.checkedRadioButtonId) {
                change_date_time_dialog_radio_one -> SmartGalleryDateFormat.One.format
                change_date_time_dialog_radio_two -> SmartGalleryDateFormat.Two.format
                change_date_time_dialog_radio_three -> SmartGalleryDateFormat.Three.format
                change_date_time_dialog_radio_four -> SmartGalleryDateFormat.Four.format
                change_date_time_dialog_radio_five -> SmartGalleryDateFormat.Five.format
                change_date_time_dialog_radio_six -> SmartGalleryDateFormat.Six.format
                change_date_time_dialog_radio_seven -> SmartGalleryDateFormat.Seven.format
                else -> SmartGalleryDateFormat.Eight.format
            }

        activity.baseConfig.use24HourFormat = binding.changeDateTimeDialog24Hour.isChecked
        callback()
    }

    private fun formatDateSample(format: String): String {
        val cal = Calendar.getInstance(Locale.ENGLISH)
        cal.timeInMillis = sampleTS
        return DateFormat.format(format, cal).toString()
    }
}
