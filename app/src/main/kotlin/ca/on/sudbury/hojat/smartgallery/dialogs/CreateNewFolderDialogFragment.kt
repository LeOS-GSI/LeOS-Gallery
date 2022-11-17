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
import ca.on.sudbury.hojat.smartgallery.databinding.DialogFragmentCreateNewFolderBinding
import ca.on.sudbury.hojat.smartgallery.extensions.createAndroidSAFDirectory
import ca.on.sudbury.hojat.smartgallery.extensions.createSAFDirectorySdk30
import ca.on.sudbury.hojat.smartgallery.extensions.getDocumentFile
import ca.on.sudbury.hojat.smartgallery.extensions.getFilenameFromPath
import ca.on.sudbury.hojat.smartgallery.extensions.getParentPath
import ca.on.sudbury.hojat.smartgallery.extensions.humanizePath
import ca.on.sudbury.hojat.smartgallery.extensions.isAStorageRootFolder
import ca.on.sudbury.hojat.smartgallery.extensions.isAValidFilename
import ca.on.sudbury.hojat.smartgallery.extensions.isAccessibleWithSAFSdk30
import ca.on.sudbury.hojat.smartgallery.extensions.isRestrictedSAFOnlyRoot
import ca.on.sudbury.hojat.smartgallery.extensions.isSDCardSetAsDefaultStorage
import ca.on.sudbury.hojat.smartgallery.usecases.IsPathOnOtgUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsPathOnSdUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsRPlusUseCase
import java.io.File

/**
 * In the main page, click on 3 dots and choose "Create new folder".
 */
class CreateNewFolderDialogFragment(
    val path: String,
    val callback: (path: String) -> Unit
) : DialogFragment() {

    // the binding
    private var _binding: DialogFragmentCreateNewFolderBinding? = null
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
        _binding = DialogFragmentCreateNewFolderBinding.inflate(inflater, container, false)

        loadDialogUi()
        return binding.root

    }

    @SuppressLint("SetTextI18n")
    private fun loadDialogUi() {
        binding.folderPath.text = "${requireActivity().humanizePath(path).trimEnd('/')}/"
    }

    /**
     * Register listeners for views.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.createNewFolderDialogBottomRow) {
            btnOk.setOnClickListener {
                val name = binding.folderName.text.toString().trim()
                when {
                    name.isEmpty() -> {
                        Toast.makeText(
                            activity,
                            R.string.empty_name, Toast.LENGTH_LONG
                        ).show()
                    }
                    name.isAValidFilename() -> {
                        val file = File(path, name)
                        if (file.exists()) {
                            Toast.makeText(
                                activity,
                                R.string.name_taken,
                                Toast.LENGTH_LONG
                            ).show()
                            return@setOnClickListener
                        }

                        createFolder("$path/$name")
                    }
                    else -> Toast.makeText(
                        activity,
                        R.string.invalid_name,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            btnCancel.setOnClickListener { dismiss() }
        }
    }

    /**
     * Clean all used resources.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun createFolder(path: String) {
        try {
            when {
                requireActivity().isRestrictedSAFOnlyRoot(path) && requireActivity().createAndroidSAFDirectory(
                    path
                ) -> sendSuccess(
                    path
                )
                requireActivity().isAccessibleWithSAFSdk30(path) -> (requireActivity() as BaseSimpleActivity).handleSAFDialogSdk30(
                    path
                ) {
                    if (it && requireActivity().createSAFDirectorySdk30(path)) {
                        sendSuccess(path)
                    }
                }
                with(requireActivity()) {
                    !IsRPlusUseCase() && (
                            IsPathOnSdUseCase(this, path) ||
                                    IsPathOnOtgUseCase(this, path))
                            && !isSDCardSetAsDefaultStorage()
                } -> (requireActivity() as BaseSimpleActivity).handleSAFDialog(
                    path
                ) {
                    if (it) {
                        try {
                            val documentFile =
                                requireActivity().getDocumentFile(path.getParentPath())
                            val newDir = documentFile?.createDirectory(path.getFilenameFromPath())
                                ?: requireActivity().getDocumentFile(path)
                            if (newDir != null) {
                                sendSuccess(path)
                            } else {
                                Toast.makeText(
                                    activity,
                                    R.string.unknown_error_occurred,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } catch (e: SecurityException) {
                            Toast.makeText(activity, e.toString(), Toast.LENGTH_LONG).show()
                        }
                    }
                }
                File(path).mkdirs() -> sendSuccess(path)
                IsRPlusUseCase() && requireActivity().isAStorageRootFolder(path.getParentPath()) -> (requireActivity() as BaseSimpleActivity).handleSAFCreateDocumentDialogSdk30(
                    path
                ) {
                    if (it) {
                        sendSuccess(path)
                    }
                }
                else ->
                    Toast.makeText(
                        activity,
                        getString(
                            R.string.could_not_create_folder,
                            path.getFilenameFromPath()
                        ), Toast.LENGTH_LONG
                    ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(activity, e.toString(), Toast.LENGTH_LONG).show()
        }
    }

    private fun sendSuccess(path: String) {
        callback(path.trimEnd('/'))
        dismiss()
    }
}