package ca.on.sudbury.hojat.smartgallery.usecases

import android.view.View
import androidx.appcompat.app.AppCompatActivity

/**
 * Just receives an instance of an AppCompatActivity and hides the system UI controls. Returns Unit.
 */
object HideSystemUiUseCase {
   operator fun invoke(activity: AppCompatActivity) {
        activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_IMMERSIVE
    }
}