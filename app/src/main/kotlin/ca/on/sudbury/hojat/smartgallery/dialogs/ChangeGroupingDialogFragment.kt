package ca.on.sudbury.hojat.smartgallery.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.databinding.DialogFragmentChangeGroupingBinding
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.helpers.Config
import ca.on.sudbury.hojat.smartgallery.helpers.GroupBy
import ca.on.sudbury.hojat.smartgallery.helpers.SHOW_ALL
import ca.on.sudbury.hojat.smartgallery.usecases.BeVisibleOrGoneUseCase

class ChangeGroupingDialogFragment(
    val path: String = "",
    val callback: () -> Unit
) : DialogFragment() {

    // the binding
    private var _binding: DialogFragmentChangeGroupingBinding? = null
    private val binding get() = _binding!!

    // configuration related variables
    private var currGrouping = 0
    private val pathToUse = path.ifEmpty { SHOW_ALL }
    private lateinit var config: Config

    /**
     * Create the UI of the dialog
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // load the binding
        _binding = DialogFragmentChangeGroupingBinding.inflate(inflater, container, false)

        // load config (I have to do it here cause it's used for loading UI)
        config = requireActivity().config

        loadDialogUi()
        return binding.root
    }

    /**
     * Register listeners for views.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.changeGroupingDialogBottomRow.btnOk.setOnClickListener {
            dialogConfirmed()
            dismiss()
        }
        binding.changeGroupingDialogBottomRow.btnCancel.setOnClickListener { dismiss() }
    }

    /**
     * Clean all used resources.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadDialogUi() {
        currGrouping = config.getFolderGrouping(pathToUse)
        binding.apply {
            groupingDialogUseForThisFolder.isChecked = config.hasCustomGrouping(pathToUse)
            BeVisibleOrGoneUseCase(groupingDialogRadioFolder, path.isEmpty())
        }

        setupGroupRadio()
        setupOrderRadio()
        binding.groupingDialogShowFileCount.isChecked =
            currGrouping and GroupBy.ShowFileCount.id != 0

    }

    private fun dialogConfirmed() {

        val groupingRadio = binding.groupingDialogRadioGrouping
        var grouping = when (groupingRadio.checkedRadioButtonId) {
            R.id.grouping_dialog_radio_none -> GroupBy.None.id
            R.id.grouping_dialog_radio_last_modified_daily -> GroupBy.LastModifiedDaily.id
            R.id.grouping_dialog_radio_last_modified_monthly -> GroupBy.LastModifiedMonthly.id
            R.id.grouping_dialog_radio_date_taken_daily -> GroupBy.DateTakenDaily.id
            R.id.grouping_dialog_radio_date_taken_monthly -> GroupBy.DateTakenMonthly.id
            R.id.grouping_dialog_radio_file_type -> GroupBy.FileType.id
            R.id.grouping_dialog_radio_extension -> GroupBy.Extension.id
            else -> GroupBy.Folder.id

        }

        if (binding.groupingDialogRadioOrder.checkedRadioButtonId == R.id.grouping_dialog_radio_descending) {
            grouping = grouping or GroupBy.Descending.id
        }

        if (binding.groupingDialogShowFileCount.isChecked) {
            grouping = grouping or GroupBy.ShowFileCount.id
        }

        if (binding.groupingDialogUseForThisFolder.isChecked) {
            config.saveFolderGrouping(pathToUse, grouping)
        } else {
            config.removeFolderGrouping(pathToUse)
            config.groupBy = grouping
        }

        callback()
    }

    private fun setupGroupRadio() {

        val groupBtn = when {
            currGrouping and GroupBy.None.id != 0 -> binding.groupingDialogRadioNone
            currGrouping and GroupBy.LastModifiedDaily.id != 0 -> binding.groupingDialogRadioLastModifiedDaily
            currGrouping and GroupBy.LastModifiedMonthly.id != 0 -> binding.groupingDialogRadioLastModifiedMonthly
            currGrouping and GroupBy.DateTakenDaily.id != 0 -> binding.groupingDialogRadioDateTakenDaily
            currGrouping and GroupBy.DateTakenMonthly.id != 0 -> binding.groupingDialogRadioDateTakenMonthly
            currGrouping and GroupBy.FileType.id != 0 -> binding.groupingDialogRadioFileType
            currGrouping and GroupBy.Extension.id != 0 -> binding.groupingDialogRadioExtension
            else -> binding.groupingDialogRadioFolder
        }
        groupBtn.isChecked = true
    }

    private fun setupOrderRadio() {
        var orderBtn = binding.groupingDialogRadioAscending

        if (currGrouping and GroupBy.Descending.id != 0) {
            orderBtn = binding.groupingDialogRadioDescending
        }
        orderBtn.isChecked = true
    }
}