package ca.on.sudbury.hojat.smartgallery.helpers

/**
 * The level of alpha that you can apply to any Views.
 */
sealed class AlphaLevel {
    /**
     * Higher level of alpha; but it's not totally opaque (100%)
     */
    object High : AlphaLevel() {
        const val amount = 0.75f
    }

    /**
     * half opaque - half transparent
     */
    object Medium : AlphaLevel() {
        const val amount = 0.5f
    }
}
