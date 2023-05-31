package ca.on.sudbury.hojat.smartgallery.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.extensions.baseConfig

/**
 * In the main page of the app, click and hold on a folder and from the context menu,
 * choose "Lock folder" and the resulting dialog is created by this class.
 */
class FolderLockingNoticeDialogFragment(val callback: () -> Unit) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setMessage(getString(R.string.lock_folder_notice))
            .setPositiveButton(R.string.ok) { _, _ ->
                requireActivity().baseConfig.wasFolderLockingNoticeShown = true
                callback()
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                dismiss()
            }
            .create()

    companion object {
        const val TAG = "FolderLockingNoticeDialogFragment"
    }
}