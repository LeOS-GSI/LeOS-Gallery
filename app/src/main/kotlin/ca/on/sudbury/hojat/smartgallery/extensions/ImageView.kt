package ca.on.sudbury.hojat.smartgallery.extensions

import android.graphics.drawable.GradientDrawable
import android.widget.ImageView
import timber.log.Timber

fun ImageView.setFillWithStroke(
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

