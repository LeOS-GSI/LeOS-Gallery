package ca.on.sudbury.hojat.smartgallery.usecases

import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity

/**
 * Receives an instance of Activity and an optional View. Hides the keyboard.
 */
object HideKeyboardUseCase {

    operator fun invoke(activity: AppCompatActivity, view: View? = null) {
        if (view == null) {
            if (IsMainThreadUseCase()) {
                hideKeyboardSync(activity)
            } else {
                Handler(Looper.getMainLooper()).post {
                    hideKeyboardSync(activity)
                }
            }
        } else {
            val inputMethodManager =
                activity.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun hideKeyboardSync(activity: AppCompatActivity) {
        val inputMethodManager =
            activity.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(
            (activity.currentFocus ?: View(activity)).windowToken, 0
        )
        activity.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        activity.currentFocus?.clearFocus()
    }

}