package ca.on.sudbury.hojat.smartgallery.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.databinding.DialogFragmentMessageBinding

/**
 * similar to ConfirmationDialog, but has a callback for negative button too.
 *
 * It's called from various places:
 *
 * 1- In the "About" page, click on "email"; the resulting dialog is created via this class.
 *
 *
 *
 */
class ConfirmationAdvancedDialogFragment(
    val message: String = "",
    private val messageId: Int = R.string.proceed_with_deletion,
    private val positive: Int = R.string.yes,
    private val negative: Int = R.string.no,
    val callback: (result: Boolean) -> Unit
) : DialogFragment() {

    // the binding
    private var _binding: DialogFragmentMessageBinding? = null
    private val binding get() = _binding!!

    /**
     * Create the UI of the dialog
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFragmentMessageBinding.inflate(
            inflater,
            container,
            false
        )
        loadDialogUI()
        return binding.root
    }

    private fun loadDialogUI() {

        binding.message.text = message.ifEmpty { resources.getString(messageId) }
        binding.btnPositive.text = resources.getString(positive)
        binding.btnNegative.text = resources.getString(negative)


    }

    /**
     * Register listeners for views.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            btnPositive.setOnClickListener {
                dismiss()
                callback(true)
            }
            btnNegative.setOnClickListener {
                dismiss()
                callback(false)
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