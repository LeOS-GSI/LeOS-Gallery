package ca.on.sudbury.hojat.smartgallery.dialogs

import android.annotation.SuppressLint
import android.graphics.Point
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.databinding.DialogResizeImageWithPathBinding
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.extensions.getParentPath
import ca.on.sudbury.hojat.smartgallery.extensions.getFilenameFromPath
import ca.on.sudbury.hojat.smartgallery.extensions.getAlertDialogBuilder
import ca.on.sudbury.hojat.smartgallery.extensions.setupDialogStuff
import ca.on.sudbury.hojat.smartgallery.extensions.isAValidFilename
import ca.on.sudbury.hojat.smartgallery.extensions.getDoesFilePathExist
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.extensions.humanizePath
import ca.on.sudbury.hojat.smartgallery.usecases.ShowKeyboardUseCase
import timber.log.Timber

@SuppressLint("SetTextI18n", "InflateParams")
class ResizeWithPathDialog(
    val activity: BaseSimpleActivity,
    val size: Point,
    val path: String,
    val callback: (newSize: Point, newPath: String) -> Unit
) {
    // we create the binding by referencing the owner Activity
    var binding = DialogResizeImageWithPathBinding.inflate(activity.layoutInflater)

    init {
        Timber.d("Hojat Ghasemi : ResizeWithPathDialog was called")

        var realPath = path.getParentPath()
        binding.apply {
            folder.setText("${activity.humanizePath(realPath).trimEnd('/')}/")

            val fullName = path.getFilenameFromPath()
            val dotAt = fullName.lastIndexOf(".")
            var name = fullName

            if (dotAt > 0) {
                name = fullName.substring(0, dotAt)
                val extension = fullName.substring(dotAt + 1)
                extensionValue.setText(extension)
            }

            filenameValue.setText(name)
            folder.setOnClickListener {
                FilePickerDialog(
                    activity,
                    realPath,
                    false,
                    activity.config.shouldShowHidden,
                    showFAB = true,
                    canAddShowHiddenButton = true
                ) {
                    folder.setText(activity.humanizePath(it))
                    realPath = it
                }
            }
        }

        binding.resizeImageWidth.setText(size.x.toString())
        binding.resizeImageHeight.setText(size.y.toString())

        val ratio = size.x / size.y.toFloat()

        binding.resizeImageWidth.addTextChangedListener {
            if (binding.resizeImageWidth.hasFocus()) {
                var width = getViewValue(binding.resizeImageWidth)
                if (width > size.x) {
                    binding.resizeImageWidth.setText(size.x.toString())
                    width = size.x
                }

                binding.resizeImageHeight.setText((width / ratio).toInt().toString())
            }
        }

        binding.resizeImageHeight.addTextChangedListener {
            if (binding.resizeImageHeight.hasFocus()) {
                var height = getViewValue(binding.resizeImageHeight)
                if (height > size.y) {
                    binding.resizeImageHeight.setText(size.y.toString())
                    height = size.y
                }

                binding.resizeImageWidth.setText((height * ratio).toInt().toString())
            }
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this) { alertDialog ->
                    ShowKeyboardUseCase(alertDialog, binding.resizeImageWidth)
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val width = getViewValue(binding.resizeImageWidth)
                        val height = getViewValue(binding.resizeImageHeight)
                        if (width <= 0 || height <= 0) {
                            Toast.makeText(activity, R.string.invalid_values, Toast.LENGTH_LONG)
                                .show()
                            return@setOnClickListener
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
                            return@setOnClickListener
                        }

                        if (extension.isEmpty()) {
                            Toast.makeText(
                                activity,
                                R.string.extension_cannot_be_empty,
                                Toast.LENGTH_LONG
                            ).show()
                            return@setOnClickListener
                        }

                        val newFilename = "$filename.$extension"
                        val newPath = "${realPath.trimEnd('/')}/$newFilename"
                        if (!newFilename.isAValidFilename()) {
                            Toast.makeText(
                                activity,
                                R.string.filename_invalid_characters,
                                Toast.LENGTH_LONG
                            ).show()
                            return@setOnClickListener
                        }

                        if (activity.getDoesFilePathExist(newPath)) {
                            val title = String.format(
                                activity.getString(R.string.file_already_exists_overwrite),
                                newFilename
                            )
                            ConfirmationDialog(activity, title) {
                                callback(newSize, newPath)
                                alertDialog.dismiss()
                            }
                        } else {
                            callback(newSize, newPath)
                            alertDialog.dismiss()
                        }
                    }
                }
            }
    }

    private fun getViewValue(view: EditText): Int {
        val textValue = view.text.toString().trim()
        return if (textValue.isEmpty()) 0 else textValue.toInt()
    }
}
