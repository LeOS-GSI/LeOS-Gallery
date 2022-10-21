package ca.on.sudbury.hojat.smartgallery.usecases

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager

object HideKeyboardUseCase {

    operator fun invoke(activity: Activity, view: View? = null) {
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
                activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun hideKeyboardSync(activity: Activity) {
        val inputMethodManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(
            (activity.currentFocus ?: View(activity)).windowToken, 0
        )
        activity.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        activity.currentFocus?.clearFocus()
    }

}