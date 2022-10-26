package ca.on.sudbury.hojat.smartgallery.usecases

import ca.on.sudbury.hojat.smartgallery.extensions.getMimeType
import java.util.HashSet

/**
 * You give it a [List<String>] and it returns the correct mimetype as a [String].
 */
object GetMimeTypeUseCase {
    operator fun invoke(input: List<String>): String {
        val mimeGroups = HashSet<String>(input.size)
        val subtypes = HashSet<String>(input.size)
        input.forEach {
            val parts = it.getMimeType().split("/")
            if (parts.size == 2) {
                mimeGroups.add(parts.getOrElse(0) { "" })
                subtypes.add(parts.getOrElse(1) { "" })
            } else {
                return "*/*"
            }
        }

        return when {
            subtypes.size == 1 -> "${mimeGroups.first()}/${subtypes.first()}"
            mimeGroups.size == 1 -> "${mimeGroups.first()}/*"
            else -> "*/*"
        }


    }
}