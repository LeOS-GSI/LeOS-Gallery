package ca.on.sudbury.hojat.smartgallery.helpers

/**
 * All the aspect ratios used in the editor for cropping an image.
 */
sealed class AspectRatio {
    object Free : AspectRatio() {
        const val id = 0
    }

    object OneOne : AspectRatio() {
        const val id = 1
    }

    object FourThree : AspectRatio() {
        const val id = 2
    }

    object SixteenNine : AspectRatio() {
        const val id = 3
    }

    object Other : AspectRatio() {
        const val id = 4
    }
}
