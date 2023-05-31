package ca.on.sudbury.hojat.smartgallery.helpers

/**
 * The type of animation which can be applied to slideshow of pictures.
 */
sealed class SlideshowAnimation {
    object None : SlideshowAnimation() {
        const val id = 0
    }

    object Slide : SlideshowAnimation() {
        const val id = 1
    }

    object Fade : SlideshowAnimation() {
        const val id = 2
    }
}
