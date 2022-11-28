package ca.on.sudbury.hojat.smartgallery.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import ca.on.sudbury.hojat.smartgallery.databinding.DialogFragmentCustomAspectRatioBinding

/**
 * In the editing page, click on "other" and in the resulting
 * dialog, click on "custom". The second dialog is created by
 * this class (but I'm not really sure if this is a good user
 * experience).
 */
class CustomAspectRatioDialogFragment(
    private val defaultCustomAspectRatio: Pair<Float, Float>?,
    val callbackAfterDialogConfirmed: (aspectRatio: Pair<Float, Float>) -> Unit
) : DialogFragment() {

    // the binding
    private var _binding: DialogFragmentCustomAspectRatioBinding? = null
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
        _binding = DialogFragmentCustomAspectRatioBinding.inflate(
            inflater,
            container,
            false
        )
        loadDialogUI()
        return binding.root
    }

    private fun loadDialogUI() {
        binding.apply {
            aspectRatioWidth.setText(defaultCustomAspectRatio?.first?.toInt()?.toString() ?: "")
            aspectRatioHeight.setText(defaultCustomAspectRatio?.second?.toInt()?.toString() ?: "")
        }
    }

    /**
     * Register listeners for views.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding.customAspectRatioDialogBottomRow) {
            btnOk.setOnClickListener {
                val width = getViewValue(binding.aspectRatioWidth)
                val height = getViewValue(binding.aspectRatioHeight)
                callbackAfterDialogConfirmed(Pair(width, height))
                dismiss()
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

    private fun getViewValue(view: EditText): Float {
        val textValue = view.text.toString().trim()
        return if (textValue.isEmpty()) 0f else textValue.toFloat()
    }

    companion object {
        const val TAG = "CustomAspectRatioDialogFragment"
    }
}