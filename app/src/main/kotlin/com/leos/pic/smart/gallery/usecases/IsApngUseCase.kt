package ca.on.sudbury.hojat.smartgallery.usecases

/**
 * You give it path of a file as a [String] and if it's APNG file, this will return true; otherwise, false.
 */
object IsApngUseCase {
    operator fun invoke(path: String) = path.endsWith(".apng", true)
}