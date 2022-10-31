package ca.on.sudbury.hojat.smartgallery.usecases

/**
 * You give it path of a file as a [String] and if it's WebP file, this will return true; otherwise, false.
 */
object IsWebpUseCase {
    operator fun invoke(path: String) = path.endsWith(".webp", true)
}