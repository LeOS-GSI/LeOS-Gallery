package ca.on.sudbury.hojat.smartgallery.dialogs

import android.annotation.SuppressLint
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
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.databinding.DialogFragmentResizeImageWithPathBinding
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.extensions.getDoesFilePathExist
import ca.on.sudbury.hojat.smartgallery.extensions.getFilenameFromPath
import ca.on.sudbury.hojat.smartgallery.extensions.getParentPath
import ca.on.sudbury.hojat.smartgallery.extensions.humanizePath
import ca.on.sudbury.hojat.smartgallery.extensions.isAValidFilename

/**
 * When you're viewing a picture, click on 3 dots on top right corner of the screen and click
 * on "resize" from the drop-down list of options. The resulting dialog is created via this
 * class.
 */
class ResizeWithPathDialogFragment(
    val size: Point,
    val path: String,
    val callback: (newSize: Point, newPath: String) -> Unit
) : DialogFragment() {

    // the binding
    private var _binding: DialogFragmentResizeImageWithPathBinding? = null
    private val binding get() = _binding!!

    // the configuration needed throughout this class
    private lateinit var realPath: String

    /**
     * Create the UI of the dialog
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFragmentResizeImageWithPathBinding.inflate(
            inflater,
            container,
            false
        )
        loadDialogUI()
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun loadDialogUI() {
        realPath = path.getParentPath()
        binding.apply {
            folder.setText("${requireActivity().humanizePath(realPath).trimEnd('/')}/")
            val fullName = path.getFilenameFromPath()
            val dotAt = fullName.lastIndexOf(".")
            var name = fullName
            if (dotAt > 0) {
                name = fullName.substring(0, dotAt)
                val extension = fullName.substring(dotAt + 1)
                extensionValue.setText(extension)
            }
            filenameValue.setText(name)

            resizeImageWidth.setText(size.x.toString())
            resizeImageHeight.setText(size.y.toString())
        }
    }

    /**
     * Register listeners for views.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            folder.setOnClickListener {
                FilePickerDialog(
                    requireActivity() as BaseSimpleActivity,
                    realPath,
                    false,
                    requireActivity().config.shouldShowHidden,
                    showFAB = true,
                    canAddShowHiddenButton = true
                ) {
                    folder.setText(requireActivity().humanizePath(it))
                    realPath = it
                }
            }
            resizeImageWithPathDialogBottomRow.btnOk.setOnClickListener {
                dialogConfirmed()
                dismiss()
            }
            resizeImageWithPathDialogBottomRow.btnCancel.setOnClickListener { dismiss() }

            val ratio = size.x / size.y.toFloat()
            resizeImageWidth.addTextChangedListener {
                if (resizeImageWidth.hasFocus()) {
                    var width = getViewValue(resizeImageWidth)
                    if (width > size.x) {
                        resizeImageWidth.setText(size.x.toString())
                        width = size.x
                    }
                    resizeImageHeight.setText((width / ratio).toInt().toString())
                }
            }
            resizeImageHeight.addTextChangedListener {
                if (resizeImageHeight.hasFocus()) {
                    var height = getViewValue(resizeImageHeight)
                    if (height > size.y) {
                        resizeImageHeight.setText(size.y.toString())
                        height = size.y
                    }

                    resizeImageWidth.setText((height * ratio).toInt().toString())
                }
            }
        }
    }

    private fun dialogConfirmed() {
        val width = getViewValue(binding.resizeImageWidth)
        val height = getViewValue(binding.resizeImageHeight)
        if (width <= 0 || height <= 0) {
            Toast.makeText(activity, R.string.invalid_values, Toast.LENGTH_LONG)
                .show()
            return
        }

        val newSize =
            Point(
                getViewValue(binding.resizeImageWidth),
                getViewValue(binding.resizeImageHeight)
            )

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
                getString(R.string.file_already_exists_overwrite),
                newFilename
            )
            val callback = {
                callback(newSize, newPath)
            }
            ConfirmationDialogFragment(
                message = title,
                callbackAfterDialogConfirmed = callback
            ).show(requireActivity().supportFragmentManager, "ConfirmationDialogFragment")
        } else {
            callback(newSize, newPath)
        }
    }

    /**
     * Cleaning the stuff
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getViewValue(view: EditText): Int {
        val textValue = view.text.toString().trim()
        return if (textValue.isEmpty()) 0 else textValue.toInt()
    }
}