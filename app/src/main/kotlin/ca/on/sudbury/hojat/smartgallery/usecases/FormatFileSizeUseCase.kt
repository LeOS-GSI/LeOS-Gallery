package ca.on.sudbury.hojat.smartgallery.usecases

import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

/**
 * You give it the size of a file (in form of a [Long] number) and receive the nice [String] equivalent of it.
 */
object FormatFileSizeUseCase {
    operator fun invoke(fileSize: Long): String {
        if (fileSize <= 0) {
            return "0 B"
        }

        val units = arrayOf("B", "kB", "MB", "GB", "TB")
        val digitGroups = (log10(fileSize.toDouble()) / log10(1024.0)).toInt()
        return "${
            DecimalFormat("#,##0.#").format(
                fileSize / 1024.0.pow(digitGroups.toDouble())
            )
        } ${units[digitGroups]}"
    }
}