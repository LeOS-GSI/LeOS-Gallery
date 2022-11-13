package ca.on.sudbury.hojat.smartgallery.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import ca.on.sudbury.hojat.smartgallery.databinding.DialogFragmentDeleteFolderBinding

/**
 * Whenever user wants to delete a whole folder of the gallery (other than recycle bin folder),
 * this dialog will be shown and will ask them to confirm the deletion operation.
 */
class DeleteFolderDialogFragment(
    private val confirmationString: String,
    private val warningMessage: String,
    val callback: () -> Unit
) : DialogFragment() {

    // the binding
    private var _binding: DialogFragmentDeleteFolderBinding? = null
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
        _binding = DialogFragmentDeleteFolderBinding.inflate(inflater, container, false)

        loadDialogUi()
        return binding.root
    }


    /**
     * Register listeners for views.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.dialogDeleteFolderBottomRow.btnOk.setOnClickListener {
            dialogConfirmed()
            dismiss()
        }
        binding.dialogDeleteFolderBottomRow.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    /**
     * Clean all used resources.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadDialogUi() {
        binding.confirmationMessage.text = confirmationString
        binding.warningMessage.text = warningMessage
    }

    private fun dialogConfirmed() {
        dismiss()
        callback()
    }
}