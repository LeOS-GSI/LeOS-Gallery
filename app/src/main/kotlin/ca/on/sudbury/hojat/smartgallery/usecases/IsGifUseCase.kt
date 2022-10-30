package ca.on.sudbury.hojat.smartgallery.usecases

/**
 * You give it path of a file as a [String] and if it's GIF file, this will return true; otherwise, false.
 */
object IsGifUseCase {
    operator fun invoke(path: String) = path.endsWith(".gif", true)
}