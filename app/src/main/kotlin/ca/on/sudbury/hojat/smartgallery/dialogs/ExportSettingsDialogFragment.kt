package ca.on.sudbury.hojat.smartgallery.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.databinding.DialogFragmentExportSettingsBinding
import ca.on.sudbury.hojat.smartgallery.extensions.baseConfig
import ca.on.sudbury.hojat.smartgallery.extensions.getDoesFilePathExist
import ca.on.sudbury.hojat.smartgallery.extensions.getFilenameFromPath
import ca.on.sudbury.hojat.smartgallery.extensions.humanizePath
import ca.on.sudbury.hojat.smartgallery.extensions.internalStoragePath
import ca.on.sudbury.hojat.smartgallery.extensions.isAValidFilename

/**
 * In settings page go to "Migrating" section and click on "Export settings".
 * The resulting dialog is created by this class.
 */
class ExportSettingsDialogFragment(
    private val defaultFilename: String,
    private val hidePath: Boolean,
    private val callback: (path: String, filename: String) -> Unit
) : DialogFragment() {

    // the binding
    private var _binding: DialogFragmentExportSettingsBinding? = null
    private val binding get() = _binding!!

    // some configurations
    private lateinit var folder: String

    /**
     * Create the UI of the dialog
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFragmentExportSettingsBinding.inflate(inflater, container, false)

        // before drawing the UI, get some needed info from shared prefs
        val lastUsedFolder = requireActivity().baseConfig.lastExportedSettingsFolder
        folder =
            if (lastUsedFolder.isNotEmpty() && requireActivity().getDoesFilePathExist(lastUsedFolder)) {
                lastUsedFolder
            } else {
                requireActivity().internalStoragePath
            }

        loadDialogUI()
        return binding.root
    }

    private fun loadDialogUI() {
        with(binding) {
            exportSettingsFilename.setText(defaultFilename.removeSuffix(".txt"))
            if (hidePath) {
                exportSettingsPathLabel.visibility = View.GONE
                exportSettingsPath.visibility = View.GONE
            } else {
                exportSettingsPath.text = requireActivity().humanizePath(folder)
            }
        }
    }

    /**
     * Register listeners for views.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            if (!hidePath) {
                // the path is being shown, so we register a listener for it
                exportSettingsPath.setOnClickListener {
                    FilePickerDialog(
                        requireActivity() as BaseSimpleActivity,
                        folder,
                        false,
                        showFAB = true
                    ) {
                        exportSettingsPath.text = requireActivity().humanizePath(it)
                        folder = it
                    }
                }

            }
            binding.exportSettingsDialogBottomRow.btnOk.setOnClickListener {
                dialogConfirmed()
                dismiss()
            }
            binding.exportSettingsDialogBottomRow.btnCancel.setOnClickListener { dismiss() }
        }
    }

    private fun dialogConfirmed() {
        var filename = binding.exportSettingsFilename.text.toString().trim()
        if (filename.isEmpty()) {
            Toast.makeText(
                activity,
                R.string.filename_cannot_be_empty,
                Toast.LENGTH_LONG
            ).show()
            return
        }

        filename += ".txt"
        val newPath = "${folder.trimEnd('/')}/$filename"
        if (!newPath.getFilenameFromPath().isAValidFilename()) {
            Toast.makeText(
                activity,
                R.string.filename_invalid_characters,
                Toast.LENGTH_LONG
            ).show()
            return
        }

        requireActivity().baseConfig.lastExportedSettingsFolder = folder
        if (!hidePath && requireActivity().getDoesFilePathExist(newPath)) {
            val title = String.format(
                getString(R.string.file_already_exists_overwrite),
                newPath.getFilenameFromPath()
            )
            ConfirmationDialog(requireActivity(), title) {
                callback(newPath, filename)
                dismiss()
            }
        } else {
            callback(newPath, filename)
            dismiss()
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