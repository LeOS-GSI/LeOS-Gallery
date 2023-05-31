package ca.on.sudbury.hojat.smartgallery.usecases

/**
 * You give it path of a file as a [String] and if it's PNG file, this will return true; otherwise, false.
 */
object IsPngUseCase {
    operator fun invoke(path: String) = path.endsWith(".png", true)
}