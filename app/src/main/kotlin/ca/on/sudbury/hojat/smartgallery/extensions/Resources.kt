package ca.on.sudbury.hojat.smartgallery.extensions

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.util.TypedValue
import com.simplemobiletools.commons.extensions.applyColorFilter

fun Resources.getActionBarHeight(context: Context): Int {
    val tv = TypedValue()
    return if (context.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
        TypedValue.complexToDimensionPixelSize(tv.data, displayMetrics)
    } else
        0
}

fun Resources.getColoredDrawableWithColor(drawableId: Int, color: Int, alpha: Int = 255): Drawable {
    val drawable = getDrawable(drawableId)
    drawable.mutate().applyColorFilter(color)
    drawable.mutate().alpha = alpha
    return drawable
}

fun Resources.getStatusBarHeight(): Int {
    val id = getIdentifier("status_bar_height", "dimen", "android")
    return if (id > 0) {
        getDimensionPixelSize(id)
    } else
        0
}

fun Resources.getNavBarHeight(): Int {
    val id = getIdentifier("navigation_bar_height", "dimen", "android")
    return if (id > 0) {
        getDimensionPixelSize(id)
    } else
        0
}
