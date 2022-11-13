package ca.on.sudbury.hojat.smartgallery.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import ca.on.sudbury.hojat.smartgallery.databinding.DialogFragmentManageExtendedDetailsBinding
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.helpers.ExtendedDetails

/**
 * In "settings" page, in the "Extended details" section, click on "manage extended details"
 * and the resulting dialog is created by this class.
 */
class ManageExtendedDetailsDialogFragment(val callback: (result: Int) -> Unit) : DialogFragment() {

    // the binding
    private var _binding: DialogFragmentManageExtendedDetailsBinding? = null
    private val binding get() = _binding!!

    /**
     * Create the UI of the dialog
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // load the binding
        _binding = DialogFragmentManageExtendedDetailsBinding.inflate(inflater, container, false)

        loadDialogUi()
        return binding.root
    }

    /**
     * Register listeners for views.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.manageExtendedDetailsDialogBottomRow.btnOk.setOnClickListener {
            dialogConfirmed()
            dismiss()
        }
        binding.manageExtendedDetailsDialogBottomRow.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    /**
     * Clean all used resources.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadDialogUi() {

        val details = requireActivity().config.extendedDetails
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

        requireActivity().config.extendedDetails = result
        callback(result)
    }

}