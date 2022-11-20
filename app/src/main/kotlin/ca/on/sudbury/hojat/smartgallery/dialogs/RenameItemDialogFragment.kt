package ca.on.sudbury.hojat.smartgallery.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.databinding.DialogFragmentRenameItemBinding
import ca.on.sudbury.hojat.smartgallery.extensions.getDoesFilePathExist
import ca.on.sudbury.hojat.smartgallery.extensions.getFilenameFromPath
import ca.on.sudbury.hojat.smartgallery.extensions.getIsPathDirectory
import ca.on.sudbury.hojat.smartgallery.extensions.getParentPath
import ca.on.sudbury.hojat.smartgallery.extensions.isAValidFilename
import ca.on.sudbury.hojat.smartgallery.extensions.renameFile

/**
 * The dialog for renaming pictures, videos, and folders is
 * created via this class.
 */
class RenameItemDialogFragment(
    val path: String,
    val callbackAfterSuccessfulRenaming: (newPath: String) -> Unit
) : DialogFragment() {

    // the binding
    private var _binding: DialogFragmentRenameItemBinding? = null
    private val binding get() = _binding!!

    /**
     * Create the UI of the dialog
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = DialogFragmentRenameItemBinding.inflate(inflater, container, false)
        loadDialogUI()
        return binding.root
    }

    private fun loadDialogUI() {

        val fullName = path.getFilenameFromPath()
        val dotAt = fullName.lastIndexOf(".")
        var name = fullName
        with(binding) {
            if (dotAt > 0 && !requireActivity().getIsPathDirectory(path)) {
                name = fullName.substring(0, dotAt)
                val extension = fullName.substring(dotAt + 1)
                renameItemExtension.setText(extension)
            } else {
                renameItemExtensionHint.visibility = View.GONE
            }
            renameItemName.setText(name)
        }
    }

    /**
     * Register listeners for views.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding.renameItemDialogBottomRow) {
            btnOk.setOnClickListener {
                dialogConfirmed()
            }
            btnCancel.setOnClickListener { dismiss() }
        }
    }

    private fun dialogConfirmed() {


        var newName = binding.renameItemName.text.toString().trim()
        val newExtension = binding.renameItemExtension.text.toString().trim()

        if (newName.isEmpty()) {
            Toast.makeText(activity, R.string.empty_name, Toast.LENGTH_LONG).show()
            return
        }

        if (!newName.isAValidFilename()) {
            Toast.makeText(activity, R.string.invalid_name, Toast.LENGTH_LONG).show()
            return
        }

        val updatedPaths = ArrayList<String>()
        updatedPaths.add(path)
        if (newExtension.isNotEmpty()) {
            newName += ".$newExtension"
        }

        if (!requireActivity().getDoesFilePathExist(path)) {
            Toast.makeText(
                activity,
                String.format(
                    getString(R.string.source_file_doesnt_exist),
                    path
                ), Toast.LENGTH_LONG
            ).show()
            return
        }

        val newPath = "${path.getParentPath()}/$newName"

        if (path == newPath) {
            Toast.makeText(activity, R.string.name_taken, Toast.LENGTH_LONG).show()
            return
        }

        if (!path.equals(
                newPath,
                ignoreCase = true
            ) && requireActivity().getDoesFilePathExist(newPath)
        ) {
            Toast.makeText(activity, R.string.name_taken, Toast.LENGTH_LONG).show()
            return
        }

        updatedPaths.add(newPath)
        (requireActivity() as BaseSimpleActivity).renameFile(path, newPath, false) { success, _ ->
            if (success) {
                callbackAfterSuccessfulRenaming(newPath)
                dismiss()
            } else {
                Toast.makeText(activity, R.string.unknown_error_occurred, Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Cleaning the stuff
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}