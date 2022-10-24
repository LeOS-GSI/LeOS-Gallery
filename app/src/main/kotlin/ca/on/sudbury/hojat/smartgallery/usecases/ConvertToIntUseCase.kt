package ca.on.sudbury.hojat.smartgallery.usecases

/**
 * You give it [Any] non-nullable object and it returns an [Int] equivalent.
 */
object ConvertToIntUseCase {
    operator fun invoke(any: Any) = Integer.parseInt(any.toString())
}