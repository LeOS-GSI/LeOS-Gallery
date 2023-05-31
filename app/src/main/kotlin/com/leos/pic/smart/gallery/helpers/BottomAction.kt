package ca.on.sudbury.hojat.smartgallery.helpers

import  ca.on.sudbury.hojat.smartgallery.photoview.PhotoActivity

/**
 * All the bottom actions that can be shown to user in [PhotoActivity] .
 */
sealed class BottomAction {
    object ToggleFavorite : BottomAction() {
        const val id = 1
    }

    object Edit : BottomAction() {
        const val id = 2
    }

    object Share : BottomAction() {
        const val id = 4
    }

    object Delete : BottomAction() {
        const val id = 8
    }

    object Rotate : BottomAction() {
        const val id = 16
    }

    object Properties : BottomAction() {
        const val id = 32
    }

    object ChangeOrientation : BottomAction() {
        const val id = 64
    }

    object SlideShow : BottomAction() {
        const val id = 128
    }

    object ShowOnMap : BottomAction() {
        const val id = 256
    }

    object ToggleVisibility : BottomAction() {
        const val id = 512
    }

    object Rename : BottomAction() {
        const val id = 1024
    }

    object SetAs : BottomAction() {
        const val id = 2048
    }

    object Copy : BottomAction() {
        const val id = 4096
    }

    object Move : BottomAction() {
        const val id = 8192
    }

    object Resize : BottomAction() {
        const val id = 16384
    }
}
