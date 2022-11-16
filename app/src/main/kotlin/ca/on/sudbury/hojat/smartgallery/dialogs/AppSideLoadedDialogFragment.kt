package ca.on.sudbury.hojat.smartgallery.dialogs


import android.app.Dialog
import android.os.Bundle
import android.text.Html
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.extensions.launchViewIntent

/**
 * I couldn't find the place this dialog is being used. That's
 * why I've left that timber log in the init block for now.
 *
 * @param callback : will be called when user dismisses
 * this dialog.
 */
class AppSideLoadedDialogFragment(val callback: () -> Unit) : DialogFragment() {

    private val url =
        "https://play.google.com/store/apps/details?id=${getString(R.string.package_name)}"

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setMessage(Html.fromHtml(String().format(getString(R.string.sideloaded_app), url)))
            .setPositiveButton(getString(R.string.download)) { _, _ -> downloadApp() }
            .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                dismiss()
                callback()
            }
            .create()

    private fun downloadApp() {
        requireActivity().launchViewIntent(url)
    }

}