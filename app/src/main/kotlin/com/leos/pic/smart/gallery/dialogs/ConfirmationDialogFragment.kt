package ca.on.sudbury.hojat.smartgallery.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.databinding.DialogFragmentMessageBinding

/**
 * A simple dialog without any view, just a messageId, a positive button and optionally a negative button
 * I couldn't find the place this dialog is used in app.
 *
 * activity has to be activity context to avoid some Theme.AppCompat issues
 * @param message the dialogs message, can be any String. If empty, messageId is used
 * @param messageId the dialogs messageId ID. Used only if message is empty
 * @param positive positive buttons text ID
 * @param negative negative buttons text ID (optional)
 * @param callbackAfterDialogConfirmed an anonymous function
 */
class ConfirmationDialogFragment(
    private val message: String = "",
    private val messageId: Int = R.string.proceed_with_deletion,
    private val positive: Int = R.string.yes,
    private val negative: Int = R.string.no,
    private val cancelOnTouchOutside: Boolean = true,
    val callbackAfterDialogConfirmed: () -> Unit
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
        if (negative != 0) {
            binding.btnNegative.text = resources.getString(negative)
        }
        isCancelable = cancelOnTouchOutside
    }

    /**
     * Register listeners for views.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            btnPositive.setOnClickListener {
               callbackAfterDialogConfirmed()
                dismiss()
            }
            btnNegative.setOnClickListener { dismiss() }
        }
    }

    /**
     * Cleaning the stuff
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ConfirmationDialogFragment"
    }
}