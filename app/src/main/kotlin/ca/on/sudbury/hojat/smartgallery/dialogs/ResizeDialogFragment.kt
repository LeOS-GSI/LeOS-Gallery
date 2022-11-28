package ca.on.sudbury.hojat.smartgallery.dialogs

import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.databinding.DialogFragmentResizeImageBinding

/**
 * In the editor, if user clicks on "resize" button; this dialog will be shown.
 */
class ResizeDialogFragment(
    val size: Point,
    val callback: (newSize: Point) -> Unit
) : DialogFragment() {

    // the binding
    private var _binding: DialogFragmentResizeImageBinding? = null
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
        _binding = DialogFragmentResizeImageBinding.inflate(inflater, container, false)

        loadDialogUi()
        return binding.root
    }

    private fun loadDialogUi() {
        binding.resizeImageWidth.setText(size.x.toString())
        binding.resizeImageHeight.setText(size.y.toString())
    }

    /**
     * Register listeners for views.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding.resizeImageDialogBottomRow) {
            btnOk.setOnClickListener {
                val width = getViewValue(binding.resizeImageWidth)
                val height = getViewValue(binding.resizeImageHeight)
                if (width <= 0 || height <= 0) {
                    Toast.makeText(activity, R.string.invalid_values, Toast.LENGTH_LONG)
                        .show()
                    return@setOnClickListener
                }

                val newSize = Point(
                    getViewValue(binding.resizeImageWidth),
                    getViewValue(binding.resizeImageHeight)
                )
                callback(newSize)
                dismiss()
            }
            btnCancel.setOnClickListener { dismiss() }
        }

        val ratio = size.x / size.y.toFloat()

        binding.resizeImageWidth.addTextChangedListener {
            if (binding.resizeImageWidth.hasFocus()) {
                var width = getViewValue(binding.resizeImageWidth)
                if (width > size.x) {
                    binding.resizeImageWidth.setText(size.x.toString())
                    width = size.x
                }

                if (binding.keepAspectRatio.isChecked) {
                    binding.resizeImageHeight.setText((width / ratio).toInt().toString())
                }
            }
        }
        binding.resizeImageHeight.addTextChangedListener {
            if (binding.resizeImageHeight.hasFocus()) {
                var height = getViewValue(binding.resizeImageHeight)
                if (height > size.y) {
                    binding.resizeImageHeight.setText(size.y.toString())
                    height = size.y
                }

                if (binding.keepAspectRatio.isChecked) {
                    binding.resizeImageWidth.setText((height * ratio).toInt().toString())
                }
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

    private fun getViewValue(view: EditText): Int {
        val textValue = view.text.toString().trim()
        return if (textValue.isEmpty()) 0 else textValue.toInt()
    }

    companion object {
        const val TAG = "ResizeDialogFragment"
    }
}