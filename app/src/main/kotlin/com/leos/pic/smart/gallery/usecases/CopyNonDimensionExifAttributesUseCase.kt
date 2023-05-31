package ca.on.sudbury.hojat.smartgallery.usecases

import androidx.exifinterface.media.ExifInterface
import java.lang.reflect.Field
import java.lang.reflect.Modifier

/**
 * It copies non-dimensional EXIF information from one [ExifInterface] object to another one.
 * It's been used when user edits an image and saves the edited picture (with a different dimension).
 */
object CopyNonDimensionExifAttributesUseCase {
    operator fun invoke(source: ExifInterface, destination: ExifInterface) {
        val attributes = ExifInterfaceAttributes.AllNonDimensionAttributes
        attributes.forEach { exifAttribute ->
            val value = source.getAttribute(exifAttribute)
            if (value != null) {
                destination.setAttribute(exifAttribute, value)
            }
        }
        destination.saveAttributes()
    }

    private class ExifInterfaceAttributes {
        companion object {
            val AllNonDimensionAttributes = getAllNonDimensionExifAttributes()

            private fun getAllNonDimensionExifAttributes(): List<String> {

                val tagFields = ExifInterface::class.java.fields.filter { field -> isExif(field) }

                val excludeAttributes = arrayListOf(
                    ExifInterface.TAG_IMAGE_LENGTH,
                    ExifInterface.TAG_IMAGE_WIDTH,
                    ExifInterface.TAG_PIXEL_X_DIMENSION,
                    ExifInterface.TAG_PIXEL_Y_DIMENSION,
                    ExifInterface.TAG_THUMBNAIL_IMAGE_LENGTH,
                    ExifInterface.TAG_THUMBNAIL_IMAGE_WIDTH,
                    ExifInterface.TAG_ORIENTATION
                )

                return tagFields
                    .map { tagField -> tagField.get(null) as String }
                    .filter { x -> !excludeAttributes.contains(x) }
                    .distinct()
            }

            private fun isExif(field: Field): Boolean {
                return field.type == String::class.java &&
                        isPublicStaticFinal(field.modifiers) &&
                        field.name.startsWith("TAG_")
            }

            private const val publicStaticFinal =
                Modifier.PUBLIC or Modifier.STATIC or Modifier.FINAL

            private fun isPublicStaticFinal(modifiers: Int): Boolean {
                return modifiers and publicStaticFinal > 0
            }
        }
    }
}