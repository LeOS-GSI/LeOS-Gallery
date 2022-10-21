package ca.on.sudbury.hojat.smartgallery.usecases

import android.view.View
import androidx.appcompat.app.AppCompatActivity

/**
 * You just give it an instance of your AppCompatActivity and it shows the Android system's UI controls.
 */
object ShowSystemUiUseCase {
    fun invoke(activity: AppCompatActivity) {
        activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    }
}