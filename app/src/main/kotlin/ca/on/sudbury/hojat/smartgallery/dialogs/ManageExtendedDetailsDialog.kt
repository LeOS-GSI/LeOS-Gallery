package ca.on.sudbury.hojat.smartgallery.dialogs

import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.databinding.DialogManageExtendedDetailsBinding
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.extensions.getAlertDialogBuilder
import ca.on.sudbury.hojat.smartgallery.extensions.setupDialogStuff
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.helpers.ExtendedDetails

/**
 * In "settings" page, in the "Extended details" section, The dialog is created by this class.
 */
class ManageExtendedDetailsDialog(
    val activity: BaseSimpleActivity,
    val callback: (result: Int) -> Unit
) {

    // we create the binding by referencing the owner Activity
    var binding = DialogManageExtendedDetailsBinding.inflate(activity.layoutInflater)

    init {
        val details = activity.config.extendedDetails
        binding.apply {
            manageExtendedDetailsName.isChecked = details and ExtendedDetails.Name.id != 0
            manageExtendedDetailsPath.isChecked = details and ExtendedDetails.Path.id != 0
            manageExtendedDetailsSize.isChecked = details and ExtendedDetails.Size.id != 0
            manageExtendedDetailsResolution.isChecked =
                details and ExtendedDetails.Resolution.id != 0
            manageExtendedDetailsLastModified.isChecked =
                details and ExtendedDetails.LastModified.id != 0
            manageExtendedDetailsDateTaken.isChecked = details and ExtendedDetails.DateTaken.id != 0
            manageExtendedDetailsCamera.isChecked = details and ExtendedDetails.CameraModel.id != 0
            manageExtendedDetailsExif.isChecked = details and ExtendedDetails.ExifProperties.id != 0
            manageExtendedDetailsGpsCoordinates.isChecked = details and ExtendedDetails.Gps.id != 0
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok) { _, _ -> dialogConfirmed() }
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this)
            }
    }

    private fun dialogConfirmed() {
        var result = 0
        binding.apply {
            if (manageExtendedDetailsName.isChecked)
                result += ExtendedDetails.Name.id
            if (manageExtendedDetailsPath.isChecked)
                result += ExtendedDetails.Path.id
            if (manageExtendedDetailsSize.isChecked)
                result += ExtendedDetails.Size.id
            if (manageExtendedDetailsResolution.isChecked)
                result += ExtendedDetails.Resolution.id
            if (manageExtendedDetailsLastModified.isChecked)
                result += ExtendedDetails.LastModified.id
            if (manageExtendedDetailsDateTaken.isChecked)
                result += ExtendedDetails.DateTaken.id
            if (manageExtendedDetailsCamera.isChecked)
                result += ExtendedDetails.CameraModel.id
            if (manageExtendedDetailsExif.isChecked)
                result += ExtendedDetails.ExifProperties.id
            if (manageExtendedDetailsGpsCoordinates.isChecked)
                result += ExtendedDetails.Gps.id
        }

        activity.config.extendedDetails = result
        callback(result)
    }
}
