package ca.on.sudbury.hojat.smartgallery.dialogs

import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.databinding.DialogFragmentChangeDateTimeFormatBinding
import ca.on.sudbury.hojat.smartgallery.extensions.baseConfig
import ca.on.sudbury.hojat.smartgallery.helpers.SmartGalleryDateFormat
import java.util.Locale
import java.util.Calendar

/**
 * In the Settings page, click on "Change date and time format". The dialog is created by this class.
 */
class ChangeDateTimeFormatDialogFragment : DialogFragment() {

    // the binding
    private var _binding: DialogFragmentChangeDateTimeFormatBinding? = null
    private val binding get() = _binding!!

    // sample time shown
    private val sampleTS = 1613422500000    // February 15, 2021

    /**
     * Create the UI of the dialog
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFragmentChangeDateTimeFormatBinding.inflate(inflater, container, false)
        loadDialogUI()
        return binding.root
    }

    /**
     * Register listeners for views.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.dateDialogBottomRow.btnOk.setOnClickListener {
            dialogConfirmed()
            dismiss()
        }
        binding.dateDialogBottomRow.btnCancel.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun formatDateSample(format: String): String {
        val cal = Calendar.getInstance(Locale.ENGLISH)
        cal.timeInMillis = sampleTS
        return DateFormat.format(format, cal).toString()
    }

    private fun loadDialogUI() {
        binding.changeDateTimeDialogRadioOne.text =
            formatDateSample(SmartGalleryDateFormat.One.format)
        binding.changeDateTimeDialogRadioTwo.text =
            formatDateSample(SmartGalleryDateFormat.Two.format)
        binding.changeDateTimeDialogRadioThree.text =
            formatDateSample(SmartGalleryDateFormat.Three.format)
        binding.changeDateTimeDialogRadioFour.text =
            formatDateSample(SmartGalleryDateFormat.Four.format)
        binding.changeDateTimeDialogRadioFive.text =
            formatDateSample(SmartGalleryDateFormat.Five.format)
        binding.changeDateTimeDialogRadioSix.text =
            formatDateSample(SmartGalleryDateFormat.Six.format)
        binding.changeDateTimeDialogRadioSeven.text =
            formatDateSample(SmartGalleryDateFormat.Seven.format)
        binding.changeDateTimeDialogRadioEight.text =
            formatDateSample(SmartGalleryDateFormat.Eight.format)
        binding.changeDateTimeDialog24Hour.isChecked = requireActivity().baseConfig.use24HourFormat

        val formatButton = when (requireActivity().baseConfig.dateFormat) {
            SmartGalleryDateFormat.One.format -> binding.changeDateTimeDialogRadioOne
            SmartGalleryDateFormat.Two.format -> binding.changeDateTimeDialogRadioTwo
            SmartGalleryDateFormat.Three.format -> binding.changeDateTimeDialogRadioThree
            SmartGalleryDateFormat.Four.format -> binding.changeDateTimeDialogRadioFour
            SmartGalleryDateFormat.Five.format -> binding.changeDateTimeDialogRadioFive
            SmartGalleryDateFormat.Six.format -> binding.changeDateTimeDialogRadioSix
            SmartGalleryDateFormat.Seven.format -> binding.changeDateTimeDialogRadioSeven
            else -> binding.changeDateTimeDialogRadioEight
        }
        formatButton.isChecked = true
    }

    private fun dialogConfirmed() {
        requireActivity().baseConfig.dateFormat =
            when (binding.changeDateTimeDialogRadioGroup.checkedRadioButtonId) {
                R.id.change_date_time_dialog_radio_one -> SmartGalleryDateFormat.One.format
                R.id.change_date_time_dialog_radio_two -> SmartGalleryDateFormat.Two.format
                R.id.change_date_time_dialog_radio_three -> SmartGalleryDateFormat.Three.format
                R.id.change_date_time_dialog_radio_four -> SmartGalleryDateFormat.Four.format
                R.id.change_date_time_dialog_radio_five -> SmartGalleryDateFormat.Five.format
                R.id.change_date_time_dialog_radio_six -> SmartGalleryDateFormat.Six.format
                R.id.change_date_time_dialog_radio_seven -> SmartGalleryDateFormat.Seven.format
                else -> SmartGalleryDateFormat.Eight.format
            }
        requireActivity().baseConfig.use24HourFormat = binding.changeDateTimeDialog24Hour.isChecked

    }
}