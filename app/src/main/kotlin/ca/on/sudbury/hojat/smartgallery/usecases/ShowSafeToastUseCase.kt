package ca.on.sudbury.hojat.smartgallery.usecases

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import timber.log.Timber

/**
 * Receives the [Context] owner and the message itself (better be in form of string resource).
 * Shows the message in form of a [Toast].
 */
object ShowSafeToastUseCase {

    operator fun invoke(owner: Context?, msg: String, duration: Int = Toast.LENGTH_SHORT) {
        try {
            if (IsMainThreadUseCase()) {
                doToast(owner, msg, duration)
            } else {
                Handler(Looper.getMainLooper()).post {
                    doToast(owner, msg, duration)
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    operator fun invoke(owner: Context?, id: Int, duration: Int = Toast.LENGTH_SHORT) {
        if (owner != null) {
            invoke(owner, owner.getString(id), duration)
        } else {
            Timber.e("The owner of Toast message is null")
        }
    }

    private fun doToast(owner: Context?, message: String, duration: Int) {
        if (owner == null) {
            Timber.e("The owner of Toast message is null")
            return
        }
        if (owner is Activity) {
            if (!owner.isFinishing && !owner.isDestroyed) {
                Toast.makeText(owner, message, duration).show()
            }
        } else {
            Toast.makeText(owner, message, duration).show()
        }
    }
}

