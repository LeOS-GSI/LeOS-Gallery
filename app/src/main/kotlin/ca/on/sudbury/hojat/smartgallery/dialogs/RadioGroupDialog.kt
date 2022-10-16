package ca.on.sudbury.hojat.smartgallery.dialogs

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.databinding.DialogRadioGroupBinding
import ca.on.sudbury.hojat.smartgallery.extensions.getAlertDialogBuilder
import ca.on.sudbury.hojat.smartgallery.extensions.onGlobalLayout
import ca.on.sudbury.hojat.smartgallery.extensions.setupDialogStuff
import ca.on.sudbury.hojat.smartgallery.models.RadioItem
import timber.log.Timber

/**
 * I couldn't find the place this class is being used.
 */
@SuppressLint("InflateParams")
class RadioGroupDialog(
    val activity: Activity,
    val items: ArrayList<RadioItem>,
    private val checkedItemId: Int = -1,
    val titleId: Int = 0,
    showOKButton: Boolean = false,
    private val cancelCallback: (() -> Unit)? = null,
    val callback: (newValue: Any) -> Unit
) {
    private var dialog: AlertDialog? = null
    private var wasInit = false
    private var selectedItemId = -1

    init {
        Timber.d("Hojat Ghasemi : RadioGroupDialog was called")
        val binding = DialogRadioGroupBinding.inflate(activity.layoutInflater)
        binding.dialogRadioGroup.apply {
            for (i in 0 until items.size) {
                val radioButton = (activity.layoutInflater.inflate(
                    R.layout.radio_button,
                    null
                ) as RadioButton).apply {
                    text = items[i].title
                    isChecked = items[i].id == checkedItemId
                    id = i
                    setOnClickListener { itemSelected(i) }
                }

                if (items[i].id == checkedItemId) {
                    selectedItemId = i
                }

                addView(
                    radioButton,
                    RadioGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                )
            }
        }

        val builder = activity.getAlertDialogBuilder()
            .setOnCancelListener { cancelCallback?.invoke() }

        if (selectedItemId != -1 && showOKButton) {
            builder.setPositiveButton(R.string.ok) { _, _ -> itemSelected(selectedItemId) }
        }

        builder.apply {
            activity.setupDialogStuff(binding.root, this, titleId) { alertDialog ->
                dialog = alertDialog
            }
        }

        if (selectedItemId != -1) {
            binding.dialogRadioHolder.apply {
                onGlobalLayout {
                    scrollY =
                        binding.dialogRadioGroup.findViewById<View>(selectedItemId).bottom - height
                }
            }
        }

        wasInit = true
    }

    private fun itemSelected(checkedId: Int) {
        if (wasInit) {
            callback(items[checkedId].value)
            dialog?.dismiss()
        }
    }
}
