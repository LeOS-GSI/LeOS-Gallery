package ca.on.sudbury.hojat.smartgallery.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.databinding.DialogFragmentRenameItemsBinding
import ca.on.sudbury.hojat.smartgallery.extensions.getDoesFilePathExist
import ca.on.sudbury.hojat.smartgallery.extensions.getFilenameFromPath
import ca.on.sudbury.hojat.smartgallery.extensions.getParentPath
import ca.on.sudbury.hojat.smartgallery.extensions.isAValidFilename
import ca.on.sudbury.hojat.smartgallery.extensions.renameFile
import ca.on.sudbury.hojat.smartgallery.usecases.GetFileExtensionUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsPathOnSdUseCase

/**
 * I couldn't ascertain where exactly this dialog is being used.
 */
class RenameItemsDialogFragment(
    val paths: ArrayList<String>,
    val callback: () -> Unit
) : DialogFragment() {

    private var ignoreClicks = false

    // the binding
    private var _binding: DialogFragmentRenameItemsBinding? = null
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
        _binding = DialogFragmentRenameItemsBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    /**
     * Register listeners for views.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding.renameItemsDialogBottomRow) {
            btnOk.setOnClickListener {
                if (ignoreClicks) {
                    return@setOnClickListener
                }

                val valueToAdd = binding.renameItemsValue.text.toString()
                val append =
                    binding.renameItemsRadioGroup.checkedRadioButtonId == binding.renameItemsRadioAppend.id

                if (valueToAdd.isEmpty()) {
                    callback()
                    dismiss()
                    return@setOnClickListener
                }

                if (!valueToAdd.isAValidFilename()) {
                    Toast.makeText(activity, R.string.invalid_name, Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                val validPaths = paths.filter { requireActivity().getDoesFilePathExist(it) }
                val sdFilePath = validPaths.firstOrNull { IsPathOnSdUseCase(activity, it) }
                    ?: validPaths.firstOrNull()
                if (sdFilePath == null) {
                    Toast.makeText(activity, R.string.unknown_error_occurred, Toast.LENGTH_LONG)
                        .show()
                    dismiss()
                    return@setOnClickListener
                }

                (requireActivity() as BaseSimpleActivity).handleSAFDialog(sdFilePath) {
                    if (!it) {
                        return@handleSAFDialog
                    }

                    ignoreClicks = true
                    var pathsCnt = validPaths.size
                    for (path in validPaths) {
                        val fullName = path.getFilenameFromPath()
                        var dotAt = fullName.lastIndexOf(".")
                        if (dotAt == -1) {
                            dotAt = fullName.length
                        }

                        val name = fullName.substring(0, dotAt)
                        val extension =
                            if (fullName.contains(".")) ".${GetFileExtensionUseCase(fullName)}" else ""

                        val newName = if (append) {
                            "$name$valueToAdd$extension"
                        } else {
                            "$valueToAdd$fullName"
                        }

                        val newPath = "${path.getParentPath()}/$newName"

                        if (requireActivity().getDoesFilePathExist(newPath)) {
                            continue
                        }

                        (requireActivity() as BaseSimpleActivity).renameFile(
                            path,
                            newPath,
                            true
                        ) { success, _ ->
                            if (success) {
                                pathsCnt--
                                if (pathsCnt == 0) {
                                    callback()
                                    dismiss()
                                }
                            } else {
                                ignoreClicks = false
                                Toast.makeText(
                                    activity,
                                    R.string.unknown_error_occurred, Toast.LENGTH_LONG
                                ).show()
                                dismiss()
                            }
                        }
                    }
                }
            }
            btnCancel.setOnClickListener {
                dismiss()
            }
        }
    }

    /**
     * Clean all used resources.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}