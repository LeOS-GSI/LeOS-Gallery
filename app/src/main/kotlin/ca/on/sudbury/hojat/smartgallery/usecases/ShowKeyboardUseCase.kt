package ca.on.sudbury.hojat.smartgallery.usecases

import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import ca.on.sudbury.hojat.smartgallery.extensions.onGlobalLayout

/**
 * You give it an [AlertDialog] and an [AppCompatEditText] which exists in that dialog; and it forces the keyboard to show up.
 */
object ShowKeyboardUseCase {
    operator fun invoke(dialog: AlertDialog, editText: AppCompatEditText) {
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        editText.apply {
            requestFocus()
            onGlobalLayout {
                setSelection(text.toString().length)
            }
        }

    }
}