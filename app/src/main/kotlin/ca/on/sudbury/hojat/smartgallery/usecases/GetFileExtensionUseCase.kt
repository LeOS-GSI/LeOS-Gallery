package ca.on.sudbury.hojat.smartgallery.usecases

/**
 * You give it the whole file name and it returns file's extension ().
 */
object GetFileExtensionUseCase {
    operator fun invoke(fileName: String) = fileName.substring(fileName.lastIndexOf(".") + 1)
}