package ca.on.sudbury.hojat.smartgallery.extensions

import android.graphics.drawable.GradientDrawable
import android.widget.ImageView
import timber.log.Timber

/**
 * When user wants to choose the color for their editing brush on the image, the dialog which is being created, has 7 circles with colors inside them. Those are in fact some [ImageView]s which are filled with color. I'm leaving this extension function to be in here for now. Will replace it with composables later.
 */
fun ImageView.fillWithColor(
    fillColor: Int,
    backgroundColor: Int,
    drawRectangle: Boolean = false
) {
    Timber.d("Hojat Ghasemi : setFillWithStroke was called")
    GradientDrawable().apply {
        shape = if (drawRectangle) GradientDrawable.RECTANGLE else GradientDrawable.OVAL
        setColor(fillColor)
        background = this

        if (backgroundColor == fillColor || fillColor == -2 && backgroundColor == -1) {
            val strokeColor = backgroundColor.getContrastColor().adjustAlpha(0.5f)
            setStroke(2, strokeColor)
        }
    }
}

