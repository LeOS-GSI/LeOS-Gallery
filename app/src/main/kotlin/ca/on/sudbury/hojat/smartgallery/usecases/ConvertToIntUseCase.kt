package ca.on.sudbury.hojat.smartgallery.usecases

/**
 * You give it [Any] non-nullable object and it returns an [Int] equivalent.
 *
 * It's only used for importing app settings.
 * TODO: I need to get rid of it and replace it with something better in repository.
 */
object ConvertToIntUseCase {
    operator fun invoke(any: Any) = Integer.parseInt(any.toString())
}