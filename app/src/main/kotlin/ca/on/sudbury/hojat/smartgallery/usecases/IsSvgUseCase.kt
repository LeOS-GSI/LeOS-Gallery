package ca.on.sudbury.hojat.smartgallery.usecases

/**
 * You give it path of a file as a [String] and if it's SVG file, this will return true; otherwise, false.
 */
object IsSvgUseCase {
    operator fun invoke(path: String) = path.endsWith(".svg", true)
}