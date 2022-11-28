package ca.on.sudbury.hojat.smartgallery.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.databinding.DialogFragmentFileConflictBinding
import ca.on.sudbury.hojat.smartgallery.extensions.baseConfig
import ca.on.sudbury.hojat.smartgallery.helpers.CONFLICT_KEEP_BOTH
import ca.on.sudbury.hojat.smartgallery.helpers.CONFLICT_MERGE
import ca.on.sudbury.hojat.smartgallery.helpers.CONFLICT_OVERWRITE
import ca.on.sudbury.hojat.smartgallery.helpers.CONFLICT_SKIP
import ca.on.sudbury.hojat.smartgallery.models.FileDirItem
import ca.on.sudbury.hojat.smartgallery.usecases.BeVisibleOrGoneUseCase

/**
 * where it's called ?
 */
class FileConflictDialogFragment(
    val fileDirItem: FileDirItem,
    private val showApplyToAllCheckbox: Boolean,
    val callback: (resolution: Int, applyForAll: Boolean) -> Unit
) : DialogFragment() {

    // the binding
    private var _binding: DialogFragmentFileConflictBinding? = null
    private val binding get() = _binding!!

    /**
     * Create the UI of the dialog
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFragmentFileConflictBinding.inflate(inflater, container, false)

        loadDialogUi()
        return binding.root
    }

    /**
     * Register listeners for views.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fileConflictDialogBottomRow.btnOk.setOnClickListener {
            dialogConfirmed()
            dismiss()
        }
        binding.fileConflictDialogBottomRow.btnCancel.setOnClickListener { dismiss() }
    }

    /**
     * Cleaning the stuff
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadDialogUi() {

        val stringBase =
            if (fileDirItem.isDirectory) R.string.folder_already_exists else R.string.file_already_exists
        binding.conflictDialogTitle.text =
            String.format(requireActivity().getString(stringBase), fileDirItem.name)
        binding.conflictDialogApplyToAll.isChecked =
            requireActivity().baseConfig.lastConflictApplyToAll

        BeVisibleOrGoneUseCase(binding.conflictDialogApplyToAll, showApplyToAllCheckbox)
        BeVisibleOrGoneUseCase(binding.conflictDialogDivider.root, showApplyToAllCheckbox)
        BeVisibleOrGoneUseCase(binding.conflictDialogRadioMerge, fileDirItem.isDirectory)

        val resolutionButton = when (requireActivity().baseConfig.lastConflictResolution) {
            CONFLICT_OVERWRITE -> binding.conflictDialogRadioOverwrite
            CONFLICT_MERGE -> binding.conflictDialogRadioMerge
            else -> binding.conflictDialogRadioSkip
        }
        resolutionButton.isChecked = true
    }

    private fun dialogConfirmed() {
        val resolution = when (binding.conflictDialogRadioGroup.checkedRadioButtonId) {
            R.id.conflict_dialog_radio_skip -> CONFLICT_SKIP
            R.id.conflict_dialog_radio_merge -> CONFLICT_MERGE
            R.id.conflict_dialog_radio_keep_both -> CONFLICT_KEEP_BOTH
            else -> CONFLICT_OVERWRITE
        }

        val applyToAll = binding.conflictDialogApplyToAll.isChecked
        requireActivity().baseConfig.apply {
            lastConflictApplyToAll = applyToAll
            lastConflictResolution = resolution
        }

        callback(resolution, applyToAll)
    }

    companion object {
        const val TAG = "FileConflictDialogFragment"
    }
}