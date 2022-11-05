package ca.on.sudbury.hojat.smartgallery.helpers

/**
 * The level of alpha that you can apply to any Views.
 */
sealed class Alpha {
    /**
     * Higher level of alpha; but it's not totally opaque (100%)
     */
    object High : Alpha() {
        const val level = 0.75f
    }

    /**
     * half opaque - half transparent
     */
    object Medium : Alpha() {
        const val level = 0.5f
    }
}
