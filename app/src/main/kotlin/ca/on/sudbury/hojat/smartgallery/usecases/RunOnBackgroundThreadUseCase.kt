package ca.on.sudbury.hojat.smartgallery.usecases

/**
 * You just give it a block of code and it makes sure the callback will run on background thread. Later on, I will migrate it to Coroutines.
 */
object RunOnBackgroundThreadUseCase {
    operator fun invoke(callback: () -> Unit) {
        if (IsMainThreadUseCase()) {
            Thread {
                callback()
            }.start()
        } else {
            callback()
        }
    }
}