package ca.on.sudbury.hojat.smartgallery.dialogs

import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.databinding.DialogCustomAspectRatioBinding
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.extensions.getAlertDialogBuilder
import ca.on.sudbury.hojat.smartgallery.extensions.setupDialogStuff
import ca.on.sudbury.hojat.smartgallery.usecases.ShowKeyboardUseCase
import timber.log.Timber

class CustomAspectRatioDialog(
    val activity: BaseSimpleActivity,
    private val defaultCustomAspectRatio: Pair<Float, Float>?,
    val callback: (aspectRatio: Pair<Float, Float>) -> Unit
) {

    // we create the binding by referencing the owner Activity
    var binding = DialogCustomAspectRatioBinding.inflate(activity.layoutInflater)

    init {
        Timber.d("Hojat Ghasemi : CustomAspectRatioDialog was created")
        binding.apply {
            aspectRatioWidth.setText(defaultCustomAspectRatio?.first?.toInt()?.toString() ?: "")
            aspectRatioHeight.setText(defaultCustomAspectRatio?.second?.toInt()?.toString() ?: "")
        }
        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this) { alertDialog ->
                    ShowKeyboardUseCase(alertDialog, binding.aspectRatioWidth)
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
        val textValue = view.text.toString().trim()
        return if (textValue.isEmpty()) 0f else textValue.toFloat()
    }
}
