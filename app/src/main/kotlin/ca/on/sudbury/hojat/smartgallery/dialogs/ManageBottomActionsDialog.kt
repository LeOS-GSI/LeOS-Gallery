package ca.on.sudbury.hojat.smartgallery.dialogs

import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.databinding.DialogManageBottomActionsBinding
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.extensions.getAlertDialogBuilder
import ca.on.sudbury.hojat.smartgallery.extensions.setupDialogStuff
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.helpers.BottomAction
import timber.log.Timber

class ManageBottomActionsDialog(
    val activity: BaseSimpleActivity,
    val callback: (result: Int) -> Unit
) {

    // we create the binding by referencing the owner Activity
    var binding = DialogManageBottomActionsBinding.inflate(activity.layoutInflater)

    init {
        Timber.d("Hojat Ghasemi : ManageBottomActionsDialog was called")
        val actions = activity.config.visibleBottomActions
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

        activity.config.visibleBottomActions = result
        callback(result)
    }
}
