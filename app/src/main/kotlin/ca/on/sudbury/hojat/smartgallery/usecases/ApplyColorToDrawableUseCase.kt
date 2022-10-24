package ca.on.sudbury.hojat.smartgallery.usecases

import android.graphics.PorterDuff
import android.graphics.drawable.Drawable

/**
 * You give it a [Drawable] and a color (which is in form of an [Int]). The color will be applied to that drawable like a filter (doesn't return the Drawable object).
 */
object ApplyColorToDrawableUseCase {
    operator fun invoke(drawable: Drawable?, color: Int) {
        drawable?.mutate()?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
    }
}