package ca.on.sudbury.hojat.smartgallery.usecases

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