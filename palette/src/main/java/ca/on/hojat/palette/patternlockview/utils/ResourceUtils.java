package ca.on.hojat.palette.patternlockview.utils;

public class ResourceUtils {

    private ResourceUtils() {
        throw new AssertionError("You can not instantiate this class. Use its static utility " +
                "methods instead");
    }

    /**
     * Get color from a resource id
     *
     * @param context  The context
     * @param colorRes The resource identifier of the color
     * @return The resolved color value
     */
    public static int getColor(@androidx.annotation.NonNull android.content.Context context, @androidx.annotation.ColorRes int colorRes) {
        return androidx.core.content.ContextCompat.getColor(context, colorRes);
    }

    /**
     * Get string from a resource id
     *
     * @param context   The context
     * @param stringRes The resource identifier of the string
     * @return The string value
     */
    public static String getString(@androidx.annotation.NonNull android.content.Context context, @androidx.annotation.StringRes int stringRes) {
        return context.getString(stringRes);
    }

    /**
     * Get dimension in pixels from its resource id
     *
     * @param context  The context
     * @param dimenRes The resource identifier of the dimension
     * @return The dimension in pixels
     */
    public static float getDimensionInPx(@androidx.annotation.NonNull android.content.Context context, @androidx.annotation.DimenRes int dimenRes) {
        return context.getResources().getDimension(dimenRes);
    }
}
