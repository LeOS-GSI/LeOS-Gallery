package ca.on.sudbury.hojat.smartgallery.usecases

import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.widget.ImageView

/**
 * You give it a [Drawable] or [ImageView] and a color (which is in form of an [Int]). The color will be applied to that drawable/imageview like a filter.
 */
object ApplyColorFilterUseCase {
    operator fun invoke(drawable: Drawable?, color: Int) =
        drawable?.mutate()?.setColorFilter(color, PorterDuff.Mode.SRC_IN)

    operator fun invoke(imageView: ImageView?, color: Int) =
        imageView?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
}