package ca.on.sudbury.hojat.smartgallery.dialogs

import android.content.DialogInterface
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.databinding.DialogChangeGroupingBinding
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.extensions.getAlertDialogBuilder
import ca.on.sudbury.hojat.smartgallery.extensions.setupDialogStuff
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.helpers.SHOW_ALL
import ca.on.sudbury.hojat.smartgallery.helpers.GroupBy
import ca.on.sudbury.hojat.smartgallery.usecases.BeVisibleOrGoneUseCase

class ChangeGroupingDialog(
    val activity: BaseSimpleActivity,
    val path: String = "",
    val callback: () -> Unit
) : DialogInterface.OnClickListener {

    // we create the binding by referencing the owner Activity
    var binding = DialogChangeGroupingBinding.inflate(activity.layoutInflater)


    private var currGrouping = 0
    private var config = activity.config
    private val pathToUse = path.ifEmpty { SHOW_ALL }

    init {
        currGrouping = config.getFolderGrouping(pathToUse)
        binding.apply {
            groupingDialogUseForThisFolder.isChecked = config.hasCustomGrouping(pathToUse)
            BeVisibleOrGoneUseCase(groupingDialogRadioFolder, path.isEmpty())
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, this)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this, R.string.group_by)
            }

        setupGroupRadio()
        setupOrderRadio()
        binding.groupingDialogShowFileCount.isChecked = currGrouping and GroupBy.ShowFileCount.id!= 0
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

    override fun onClick(dialog: DialogInterface, which: Int) {
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
}
