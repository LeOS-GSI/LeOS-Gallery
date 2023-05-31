package ca.on.sudbury.hojat.smartgallery.usecases

import android.graphics.Point
import kotlin.math.roundToInt

/**
 * You give it a picture/video in form of a [Point] object and it returns the average megapixel of that picture as a [String].
 */
object GetMegaPixelUseCase {
    operator fun invoke(picture: Point?): String {
        if (picture == null) return "(0MP)"
        val px = picture.x * picture.y / 1000000.toFloat()
        val rounded = (px * 10).roundToInt() / 10.toFloat()
        return "(${rounded}MP)"
    }
}