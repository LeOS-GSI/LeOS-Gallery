package ca.on.sudbury.hojat.smartgallery.usecases

/**
 * You give it [Any] non-nullable object and it returns a [Boolean] equivalent.
 *
 * It's only used for importing app settings.
 * TODO: I need to get rid of it and replace it with something better in repository.
 */
object ConvertToBooleanUseCase {
    operator fun invoke(any: Any) = any.toString() == "true"
}