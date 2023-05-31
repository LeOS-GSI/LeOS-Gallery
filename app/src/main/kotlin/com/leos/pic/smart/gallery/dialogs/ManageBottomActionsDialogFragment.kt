package ca.on.sudbury.hojat.smartgallery.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import ca.on.sudbury.hojat.smartgallery.databinding.DialogFragmentManageBottomActionsBinding
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.helpers.BottomAction


/**
 * In the settings page, in the section "Bottom Actions" click on "Manage visible bottom actions".
 * The resulting dialog is created via this class.
 */
class ManageBottomActionsDialogFragment(
    val callbackAfterDialogConfirmed: (result: Int) -> Unit
) :
    DialogFragment() {

    // the binding
    private var _binding: DialogFragmentManageBottomActionsBinding? = null
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
        _binding = DialogFragmentManageBottomActionsBinding.inflate(
            inflater,
            container,
            false
        )
        loadDialogUI()
        return binding.root
    }

    private fun loadDialogUI() {
        val actions = requireActivity().config.visibleBottomActions
        binding.apply {
            manageBottomActionsToggleFavorite.isChecked =
                actions and BottomAction.ToggleFavorite.id != 0
            manageBottomActionsEdit.isChecked = actions and BottomAction.Edit.id != 0
            manageBottomActionsShare.isChecked = actions and BottomAction.Share.id != 0
            manageBottomActionsDelete.isChecked = actions and BottomAction.Delete.id != 0
            manageBottomActionsRotate.isChecked = actions and BottomAction.Rotate.id != 0
            manageBottomActionsProperties.isChecked = actions and BottomAction.Properties.id != 0
            manageBottomActionsChangeOrientation.isChecked =
                actions and BottomAction.ChangeOrientation.id != 0
            manageBottomActionsSlideshow.isChecked = actions and BottomAction.SlideShow.id != 0
            manageBottomActionsShowOnMap.isChecked = actions and BottomAction.ShowOnMap.id != 0
            manageBottomActionsToggleVisibility.isChecked =
                actions and BottomAction.ToggleVisibility.id != 0
            manageBottomActionsRename.isChecked = actions and BottomAction.Rename.id != 0
            manageBottomActionsSetAs.isChecked = actions and BottomAction.SetAs.id != 0
            manageBottomActionsCopy.isChecked = actions and BottomAction.Copy.id != 0
            manageBottomActionsMove.isChecked = actions and BottomAction.Move.id != 0
            manageBottomActionsResize.isChecked = actions and BottomAction.Resize.id != 0
        }
    }

    /**
     * Register listeners for views.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding.manageBottomActionsDialogBottomRow) {
            btnOk.setOnClickListener {
                dialogConfirmed()
                dismiss()
            }
            btnCancel.setOnClickListener { dismiss() }
        }
    }

    private fun dialogConfirmed() {
        var result = 0
        binding.apply {
            if (manageBottomActionsToggleFavorite.isChecked)
                result += BottomAction.ToggleFavorite.id
            if (manageBottomActionsEdit.isChecked)
                result += BottomAction.Edit.id
            if (manageBottomActionsShare.isChecked)
                result += BottomAction.Share.id
            if (manageBottomActionsDelete.isChecked)
                result += BottomAction.Delete.id
            if (manageBottomActionsRotate.isChecked)
                result += BottomAction.Rotate.id
            if (manageBottomActionsProperties.isChecked)
                result += BottomAction.Properties.id
            if (manageBottomActionsChangeOrientation.isChecked)
                result += BottomAction.ChangeOrientation.id
            if (manageBottomActionsSlideshow.isChecked)
                result += BottomAction.SlideShow.id
            if (manageBottomActionsShowOnMap.isChecked)
                result += BottomAction.ShowOnMap.id
            if (manageBottomActionsToggleVisibility.isChecked)
                result += BottomAction.ToggleVisibility.id
            if (manageBottomActionsRename.isChecked)
                result += BottomAction.Rename.id
            if (manageBottomActionsSetAs.isChecked)
                result += BottomAction.SetAs.id
            if (manageBottomActionsCopy.isChecked)
                result += BottomAction.Copy.id
            if (manageBottomActionsMove.isChecked)
                result += BottomAction.Move.id
            if (manageBottomActionsResize.isChecked)
                result += BottomAction.Resize.id
        }

        requireActivity().config.visibleBottomActions = result
        callbackAfterDialogConfirmed(result)
    }


    /**
     * Clean all used resources.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object{
        const val TAG = "ManageBottomActionsDialogFragment"
    }
}