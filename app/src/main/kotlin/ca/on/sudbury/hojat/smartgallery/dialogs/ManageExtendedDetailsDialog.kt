package ca.on.sudbury.hojat.smartgallery.dialogs

import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.databinding.DialogManageExtendedDetailsBinding
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.extensions.getAlertDialogBuilder
import ca.on.sudbury.hojat.smartgallery.extensions.setupDialogStuff
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.helpers.EXT_NAME
import ca.on.sudbury.hojat.smartgallery.helpers.EXT_PATH
import ca.on.sudbury.hojat.smartgallery.helpers.EXT_SIZE
import ca.on.sudbury.hojat.smartgallery.helpers.EXT_RESOLUTION
import ca.on.sudbury.hojat.smartgallery.helpers.EXT_LAST_MODIFIED
import ca.on.sudbury.hojat.smartgallery.helpers.EXT_DATE_TAKEN
import ca.on.sudbury.hojat.smartgallery.helpers.EXT_CAMERA_MODEL
import ca.on.sudbury.hojat.smartgallery.helpers.EXT_EXIF_PROPERTIES
import ca.on.sudbury.hojat.smartgallery.helpers.EXT_GPS


class ManageExtendedDetailsDialog(
    val activity: BaseSimpleActivity,
    val callback: (result: Int) -> Unit
) {

    // we create the binding by referencing the owner Activity
    var binding = DialogManageExtendedDetailsBinding.inflate(activity.layoutInflater)

    init {
        val details = activity.config.extendedDetails
        binding.apply {
            manageExtendedDetailsName.isChecked = details and EXT_NAME != 0
            manageExtendedDetailsPath.isChecked = details and EXT_PATH != 0
            manageExtendedDetailsSize.isChecked = details and EXT_SIZE != 0
            manageExtendedDetailsResolution.isChecked = details and EXT_RESOLUTION != 0
            manageExtendedDetailsLastModified.isChecked = details and EXT_LAST_MODIFIED != 0
            manageExtendedDetailsDateTaken.isChecked = details and EXT_DATE_TAKEN != 0
            manageExtendedDetailsCamera.isChecked = details and EXT_CAMERA_MODEL != 0
            manageExtendedDetailsExif.isChecked = details and EXT_EXIF_PROPERTIES != 0
            manageExtendedDetailsGpsCoordinates.isChecked = details and EXT_GPS != 0
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
                result += EXT_NAME
            if (manageExtendedDetailsPath.isChecked)
                result += EXT_PATH
            if (manageExtendedDetailsSize.isChecked)
                result += EXT_SIZE
            if (manageExtendedDetailsResolution.isChecked)
                result += EXT_RESOLUTION
            if (manageExtendedDetailsLastModified.isChecked)
                result += EXT_LAST_MODIFIED
            if (manageExtendedDetailsDateTaken.isChecked)
                result += EXT_DATE_TAKEN
            if (manageExtendedDetailsCamera.isChecked)
                result += EXT_CAMERA_MODEL
            if (manageExtendedDetailsExif.isChecked)
                result += EXT_EXIF_PROPERTIES
            if (manageExtendedDetailsGpsCoordinates.isChecked)
                result += EXT_GPS
        }

        activity.config.extendedDetails = result
        callback(result)
    }
}
