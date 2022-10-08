package ca.on.sudbury.hojat.smartgallery.dialogs


import android.annotation.SuppressLint
import android.app.Activity
import android.text.format.DateFormat
import ca.on.sudbury.hojat.smartgallery.helpers.DATE_FORMAT_EIGHT
import ca.on.sudbury.hojat.smartgallery.helpers.DATE_FORMAT_FIVE
import ca.on.sudbury.hojat.smartgallery.helpers.DATE_FORMAT_FOUR
import ca.on.sudbury.hojat.smartgallery.helpers.DATE_FORMAT_ONE
import ca.on.sudbury.hojat.smartgallery.helpers.DATE_FORMAT_SEVEN
import ca.on.sudbury.hojat.smartgallery.helpers.DATE_FORMAT_SIX
import ca.on.sudbury.hojat.smartgallery.helpers.DATE_FORMAT_THREE
import ca.on.sudbury.hojat.smartgallery.helpers.DATE_FORMAT_TWO
import com.simplemobiletools.commons.R
import com.simplemobiletools.commons.R.id.change_date_time_dialog_radio_one
import com.simplemobiletools.commons.R.id.change_date_time_dialog_radio_two
import com.simplemobiletools.commons.R.id.change_date_time_dialog_radio_three
import com.simplemobiletools.commons.R.id.change_date_time_dialog_radio_four
import com.simplemobiletools.commons.R.id.change_date_time_dialog_radio_five
import com.simplemobiletools.commons.R.id.change_date_time_dialog_radio_six
import com.simplemobiletools.commons.R.id.change_date_time_dialog_radio_seven
import ca.on.sudbury.hojat.smartgallery.extensions.baseConfig
import ca.on.sudbury.hojat.smartgallery.extensions.getAlertDialogBuilder
import ca.on.sudbury.hojat.smartgallery.extensions.setupDialogStuff
import kotlinx.android.synthetic.main.dialog_change_date_time_format.view.*
import java.util.Calendar
import java.util.Locale

class ChangeDateTimeFormatDialog(val activity: Activity, val callback: () -> Unit) {
    @SuppressLint("InflateParams")
    private val view =
        activity.layoutInflater.inflate(R.layout.dialog_change_date_time_format, null)!!
    private val sampleTS = 1613422500000    // February 15, 2021

    init {
        view.apply {
            change_date_time_dialog_radio_one.text = formatDateSample(DATE_FORMAT_ONE)
            change_date_time_dialog_radio_two.text = formatDateSample(DATE_FORMAT_TWO)
            change_date_time_dialog_radio_three.text = formatDateSample(DATE_FORMAT_THREE)
            change_date_time_dialog_radio_four.text = formatDateSample(DATE_FORMAT_FOUR)
            change_date_time_dialog_radio_five.text = formatDateSample(DATE_FORMAT_FIVE)
            change_date_time_dialog_radio_six.text = formatDateSample(DATE_FORMAT_SIX)
            change_date_time_dialog_radio_seven.text = formatDateSample(DATE_FORMAT_SEVEN)
            change_date_time_dialog_radio_eight.text = formatDateSample(DATE_FORMAT_EIGHT)

            change_date_time_dialog_24_hour.isChecked = activity.baseConfig.use24HourFormat

            val formatButton = when (activity.baseConfig.dateFormat) {
                DATE_FORMAT_ONE -> change_date_time_dialog_radio_one
                DATE_FORMAT_TWO -> change_date_time_dialog_radio_two
                DATE_FORMAT_THREE -> change_date_time_dialog_radio_three
                DATE_FORMAT_FOUR -> change_date_time_dialog_radio_four
                DATE_FORMAT_FIVE -> change_date_time_dialog_radio_five
                DATE_FORMAT_SIX -> change_date_time_dialog_radio_six
                DATE_FORMAT_SEVEN -> change_date_time_dialog_radio_seven
                else -> change_date_time_dialog_radio_eight
            }
            formatButton.isChecked = true
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok) { _, _ -> dialogConfirmed() }
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view, this)
            }
    }

    private fun dialogConfirmed() {
        activity.baseConfig.dateFormat =
            when (view.change_date_time_dialog_radio_group.checkedRadioButtonId) {
                change_date_time_dialog_radio_one -> DATE_FORMAT_ONE
                change_date_time_dialog_radio_two -> DATE_FORMAT_TWO
                change_date_time_dialog_radio_three -> DATE_FORMAT_THREE
                change_date_time_dialog_radio_four -> DATE_FORMAT_FOUR
                change_date_time_dialog_radio_five -> DATE_FORMAT_FIVE
                change_date_time_dialog_radio_six -> DATE_FORMAT_SIX
                change_date_time_dialog_radio_seven -> DATE_FORMAT_SEVEN
                else -> DATE_FORMAT_EIGHT
            }

        activity.baseConfig.use24HourFormat = view.change_date_time_dialog_24_hour.isChecked
        callback()
    }

    private fun formatDateSample(format: String): String {
        val cal = Calendar.getInstance(Locale.ENGLISH)
        cal.timeInMillis = sampleTS
        return DateFormat.format(format, cal).toString()
    }
}
