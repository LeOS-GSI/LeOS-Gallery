package ca.on.sudbury.hojat.smartgallery.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.databinding.DialogFragmentDeleteWithRememberBinding

/**
 * The dialog that asks user for confirmation before deleting a file;
 * and has an option for remembering to not show this dialog again.
 */
class DeleteWithRememberDialogFragment(
    val message: String,
    val callback: (remember: Boolean) -> Unit
) : DialogFragment() {

    // the binding
    private var _binding: DialogFragmentDeleteWithRememberBinding? = null
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
        _binding = DialogFragmentDeleteWithRememberBinding.inflate(inflater, container, false)

        loadDialogUi()
        return binding.root
    }

    /**
     * Register listeners for views.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.deleteWithRememberDialogBottomRow) {
            btnOk.setOnClickListener {
                dismiss()
                callback(binding.deleteRememberCheckbox.isChecked)
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

    private fun loadDialogUi() {
        with(binding) {
            deleteWithRememberDialogBottomRow.btnOk.text = getString(R.string.yes)
            deleteWithRememberDialogBottomRow.btnCancel.text = getString(R.string.no)
            deleteRememberTitle.text = message
        }
    }

}