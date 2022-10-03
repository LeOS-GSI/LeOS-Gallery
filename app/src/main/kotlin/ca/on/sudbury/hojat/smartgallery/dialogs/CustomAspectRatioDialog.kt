package ca.on.sudbury.hojat.smartgallery.dialogs

import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.databinding.DialogCustomAspectRatioBinding
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.extensions.getAlertDialogBuilder
import ca.on.sudbury.hojat.smartgallery.extensions.setupDialogStuff
import com.simplemobiletools.commons.extensions.showKeyboard
import ca.on.sudbury.hojat.smartgallery.extensions.value

class CustomAspectRatioDialog(
    val activity: BaseSimpleActivity,
    private val defaultCustomAspectRatio: Pair<Float, Float>?,
    val callback: (aspectRatio: Pair<Float, Float>) -> Unit
) {

    // we create the binding by referencing the owner Activity
    var binding = DialogCustomAspectRatioBinding.inflate(activity.layoutInflater)

    init {
        binding.apply {
            aspectRatioWidth.setText(defaultCustomAspectRatio?.first?.toInt()?.toString() ?: "")
            aspectRatioHeight.setText(defaultCustomAspectRatio?.second?.toInt()?.toString() ?: "")
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this) { alertDialog ->
                    alertDialog.showKeyboard(binding.aspectRatioWidth)
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        val width = getViewValue(binding.aspectRatioWidth)
                        val height = getViewValue(binding.aspectRatioHeight)
                        callback(Pair(width, height))
                        alertDialog.dismiss()
                    }
                }
            }
    }

    private fun getViewValue(view: EditText): Float {
        val textValue = view.value
        return if (textValue.isEmpty()) 0f else textValue.toFloat()
    }
}
