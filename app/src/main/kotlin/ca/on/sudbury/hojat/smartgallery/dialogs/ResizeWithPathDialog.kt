package ca.on.sudbury.hojat.smartgallery.dialogs

import android.annotation.SuppressLint
import android.graphics.Point
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.databinding.DialogResizeImageWithPathBinding
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.extensions.getParentPath
import ca.on.sudbury.hojat.smartgallery.extensions.getFilenameFromPath
import ca.on.sudbury.hojat.smartgallery.extensions.onTextChangeListener
import ca.on.sudbury.hojat.smartgallery.extensions.getAlertDialogBuilder
import ca.on.sudbury.hojat.smartgallery.extensions.setupDialogStuff
import ca.on.sudbury.hojat.smartgallery.extensions.showKeyboard
import ca.on.sudbury.hojat.smartgallery.extensions.toast
import ca.on.sudbury.hojat.smartgallery.extensions.value
import ca.on.sudbury.hojat.smartgallery.extensions.isAValidFilename
import ca.on.sudbury.hojat.smartgallery.extensions.getDoesFilePathExist
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.extensions.humanizePath

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

        val widthView = binding.resizeImageWidth
        val heightView = binding.resizeImageHeight

        widthView.setText(size.x.toString())
        heightView.setText(size.y.toString())

        val ratio = size.x / size.y.toFloat()

        widthView.onTextChangeListener {
            if (widthView.hasFocus()) {
                var width = getViewValue(widthView)
                if (width > size.x) {
                    widthView.setText(size.x.toString())
                    width = size.x
                }

                heightView.setText((width / ratio).toInt().toString())
            }
        }

        heightView.onTextChangeListener {
            if (heightView.hasFocus()) {
                var height = getViewValue(heightView)
                if (height > size.y) {
                    heightView.setText(size.y.toString())
                    height = size.y
                }

                widthView.setText((height * ratio).toInt().toString())
            }
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this) { alertDialog ->
                    alertDialog.showKeyboard(binding.resizeImageWidth)
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val width = getViewValue(widthView)
                        val height = getViewValue(heightView)
                        if (width <= 0 || height <= 0) {
                            activity.toast(R.string.invalid_values)
                            return@setOnClickListener
                        }

                        val newSize = Point(getViewValue(widthView), getViewValue(heightView))

                        val filename = binding.filenameValue.value
                        val extension = binding.extensionValue.value
                        if (filename.isEmpty()) {
                            activity.toast(R.string.filename_cannot_be_empty)
                            return@setOnClickListener
                        }

                        if (extension.isEmpty()) {
                            activity.toast(R.string.extension_cannot_be_empty)
                            return@setOnClickListener
                        }

                        val newFilename = "$filename.$extension"
                        val newPath = "${realPath.trimEnd('/')}/$newFilename"
                        if (!newFilename.isAValidFilename()) {
                            activity.toast(R.string.filename_invalid_characters)
                            return@setOnClickListener
                        }

                        if (activity.getDoesFilePathExist(newPath)) {
                            val title = String.format(activity.getString(R.string.file_already_exists_overwrite), newFilename)
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
        val textValue = view.value
        return if (textValue.isEmpty()) 0 else textValue.toInt()
    }
}
