package ca.on.sudbury.hojat.smartgallery.dialogs

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.databinding.DialogFragmentSaveAsBinding
import ca.on.sudbury.hojat.smartgallery.extensions.getDoesFilePathExist
import ca.on.sudbury.hojat.smartgallery.extensions.getFileUrisFromFileDirItems
import ca.on.sudbury.hojat.smartgallery.extensions.getFilenameFromPath
import ca.on.sudbury.hojat.smartgallery.extensions.getParentPath
import ca.on.sudbury.hojat.smartgallery.extensions.getPicturesDirectoryPath
import ca.on.sudbury.hojat.smartgallery.extensions.humanizePath
import ca.on.sudbury.hojat.smartgallery.extensions.isAValidFilename
import ca.on.sudbury.hojat.smartgallery.extensions.isExternalStorageManager
import ca.on.sudbury.hojat.smartgallery.extensions.isInDownloadDir
import ca.on.sudbury.hojat.smartgallery.extensions.isInSubFolderInDownloadDir
import ca.on.sudbury.hojat.smartgallery.extensions.isRestrictedWithSAFSdk30
import ca.on.sudbury.hojat.smartgallery.extensions.toFileDirItem
import ca.on.sudbury.hojat.smartgallery.usecases.HideKeyboardUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsRPlusUseCase
import java.io.File

class SaveAsDialogFragment(
    val path: String,
    private val appendFilename: Boolean,
    private val cancelCallback: (() -> Unit)? = null,
    val callback: (savePath: String) -> Unit
) : DialogFragment() {

    // the binding
    private var _binding: DialogFragmentSaveAsBinding? = null
    private val binding get() = _binding!!

    private lateinit var realPath: String

    /**
     * Create the UI of the dialog
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // load the binding
        _binding = DialogFragmentSaveAsBinding.inflate(inflater, container, false)

        // need to do this stuff before drawing the UI
        realPath = path.getParentPath()
        if (requireActivity().isRestrictedWithSAFSdk30(realPath) && !requireActivity().isInDownloadDir(
                realPath
            )
        ) {
            realPath = requireActivity().getPicturesDirectoryPath(realPath)
        }

        loadDialogUi()
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun loadDialogUi() {
        binding.apply {
            folderValue.setText("${requireActivity().humanizePath(realPath).trimEnd('/')}/")

            val fullName = path.getFilenameFromPath()
            val dotAt = fullName.lastIndexOf(".")
            var name = fullName

            if (dotAt > 0) {
                name = fullName.substring(0, dotAt)
                val extension = fullName.substring(dotAt + 1)
                binding.extensionValue.setText(extension)
            }

            if (appendFilename) {
                name += "_1"
            }

            binding.filenameValue.setText(name)
            binding.folderValue.setOnClickListener {
                HideKeyboardUseCase(requireActivity(), binding.folderValue)
                FilePickerDialog(
                    requireActivity() as BaseSimpleActivity,
                    realPath,
                    pickFile = false,
                    showHidden = false,
                    showFAB = true,
                    canAddShowHiddenButton = true
                ) {
                    binding.folderValue.setText(requireActivity().humanizePath(it))
                    realPath = it
                }
            }
        }
    }

    /**
     * Register listeners for views.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding.saveAsDialogBottomRow) {
            btnOk.setOnClickListener {
                dismiss()
                dialogConfirmed()
            }
            btnCancel.setOnClickListener {
                dismiss()
                cancelCallback?.invoke()
            }
        }

        binding.folderValue.setOnClickListener {
            HideKeyboardUseCase(requireActivity(), binding.folderValue)
            FilePickerDialog(
                requireActivity() as BaseSimpleActivity,
                realPath,
                pickFile = false,
                showHidden = false,
                showFAB = true,
                canAddShowHiddenButton = true
            ) {
                binding.folderValue.setText(requireActivity().humanizePath(it))
                realPath = it
            }
        }

    }

    private fun dialogConfirmed() {

        val filename = binding.filenameValue.text.toString().trim()
        val extension = binding.extensionValue.text.toString().trim()

        if (filename.isEmpty()) {
            Toast.makeText(
                activity,
                R.string.filename_cannot_be_empty,
                Toast.LENGTH_LONG
            ).show()
            return
        }

        if (extension.isEmpty()) {
            Toast.makeText(
                activity,
                R.string.extension_cannot_be_empty,
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val newFilename = "$filename.$extension"
        val newPath = "${realPath.trimEnd('/')}/$newFilename"
        if (!newFilename.isAValidFilename()) {
            Toast.makeText(
                activity,
                R.string.filename_invalid_characters,
                Toast.LENGTH_LONG
            ).show()
            return
        }

        if (requireActivity().getDoesFilePathExist(newPath)) {
            val title = String.format(
                requireActivity().getString(R.string.file_already_exists_overwrite),
                newFilename
            )
            val callback = {
                val newFile = File(newPath)
                val isInDownloadDir = requireActivity().isInDownloadDir(newPath)
                val isInSubFolderInDownloadDir =
                    requireActivity().isInSubFolderInDownloadDir(newPath)
                if ((IsRPlusUseCase() && !isExternalStorageManager()) && isInDownloadDir && !isInSubFolderInDownloadDir && !newFile.canWrite()) {
                    val fileDirItem =
                        arrayListOf(File(newPath).toFileDirItem(requireActivity()))
                    val fileUris = requireActivity().getFileUrisFromFileDirItems(fileDirItem)
                    (requireActivity() as BaseSimpleActivity).updateSDK30Uris(fileUris) { success ->
                        if (success) {
                            selectPath(newPath)
                        }
                    }
                } else {
                    selectPath(newPath)
                }
            }
            ConfirmationDialogFragment(
                message = title,
                callbackAfterDialogConfirmed = callback
            ).show(requireActivity().supportFragmentManager, "ConfirmationDialogFragment")
        } else {
            selectPath(newPath)
        }
    }

    /**
     * Clean all used resources.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun selectPath(newPath: String) {
        (requireActivity() as BaseSimpleActivity).handleSAFDialogSdk30(newPath) {
            if (!it) {
                return@handleSAFDialogSdk30
            }
            callback(newPath)
            dismiss()
        }
    }

}