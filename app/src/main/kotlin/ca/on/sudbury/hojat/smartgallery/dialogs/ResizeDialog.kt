package ca.on.sudbury.hojat.smartgallery.dialogs

import android.annotation.SuppressLint
import android.graphics.Point
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.databinding.DialogResizeImageBinding
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.onTextChangeListener
import ca.on.sudbury.hojat.smartgallery.extensions.getAlertDialogBuilder
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.extensions.showKeyboard
import ca.on.sudbury.hojat.smartgallery.extensions.toast
import com.simplemobiletools.commons.extensions.value

@SuppressLint("InflateParams")
class ResizeDialog(
    val activity: BaseSimpleActivity,
    val size: Point,
    val callback: (newSize: Point) -> Unit
) {

    // we create the binding by referencing the owner Activity
    var binding = DialogResizeImageBinding.inflate(activity.layoutInflater)

    init {

        binding.resizeImageWidth.setText(size.x.toString())
        binding.resizeImageHeight.setText(size.y.toString())
        val ratio = size.x / size.y.toFloat()
        binding.resizeImageWidth.onTextChangeListener {
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

        binding.resizeImageHeight.onTextChangeListener {
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

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this, R.string.resize_and_save) { alertDialog ->
                    alertDialog.showKeyboard(binding.resizeImageWidth)
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val width = getViewValue(binding.resizeImageWidth)
                        val height = getViewValue(binding.resizeImageHeight)
                        if (width <= 0 || height <= 0) {
                            activity.toast(R.string.invalid_values)
                            return@setOnClickListener
                        }

                        val newSize = Point(getViewValue(binding.resizeImageWidth), getViewValue(binding.resizeImageHeight))
                        callback(newSize)
                        alertDialog.dismiss()
                    }
                }
            }
    }

    private fun getViewValue(view: EditText): Int {
        val textValue = view.value
        return if (textValue.isEmpty()) 0 else textValue.toInt()
    }
}
