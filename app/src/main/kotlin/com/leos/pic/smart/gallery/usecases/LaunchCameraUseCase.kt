package ca.on.sudbury.hojat.smartgallery.usecases

import android.content.Intent
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import timber.log.Timber

/**
 * You just give it an instance of the [AppCompatActivity] which is the owner of this UseCase and
 * it will launch an intent for opening one of device's camera apps.
 */
object LaunchCameraUseCase {
    operator fun invoke(owner: AppCompatActivity) {
        val intent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
        try {
            owner.startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}