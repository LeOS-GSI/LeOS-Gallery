package ca.on.hojat.palette.patternlockview.utils

import android.content.Context
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat

/**
 * You can not instantiate this class. Use its
 * static utility methods instead.
 */
object ResourceUtils {

    /**
     * Get color from a resource id
     *
     * @param context  The context
     * @param colorRes The resource identifier of the color
     * @return The resolved color value
     */
    @JvmStatic
    fun getColor(context: Context, @ColorRes colorRes: Int) =
        ContextCompat.getColor(context, colorRes)

    /**
     * Get string from a resource id
     *
     * @param context   The context
     * @param stringRes The resource identifier of the string
     * @return The string value
     */
    @JvmStatic
    fun getString(context: Context, @StringRes stringRes: Int) = context.getString(stringRes)

    /**
     * Get dimension in pixels from its resource id
     *
     * @param context  The context
     * @param dimenRes The resource identifier of the dimension
     * @return The dimension in pixels
     */
    @JvmStatic
    fun getDimensionInPx(context: Context, @DimenRes dimenRes: Int) =
        context.resources.getDimension(dimenRes)
}