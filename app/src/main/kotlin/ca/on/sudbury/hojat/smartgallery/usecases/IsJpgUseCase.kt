package ca.on.sudbury.hojat.smartgallery.usecases

/**
 * You give it path of a file as a [String] and if it's jpg file, this will return true; otherwise, false.
 */
object IsJpgUseCase {
    operator fun invoke(path: String) = path.endsWith(".jpg", true) or path.endsWith(".jpeg", true)
}