package ca.on.sudbury.hojat.smartgallery.usecases

import android.os.Looper

/**
 * Determines whether we're on the main thread or not; doesn't need any inputs.
 */
object IsMainThreadUseCase {
   operator fun invoke() = Looper.myLooper() == Looper.getMainLooper()
}