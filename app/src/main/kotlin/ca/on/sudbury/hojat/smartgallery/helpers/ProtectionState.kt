package ca.on.sudbury.hojat.smartgallery.helpers

sealed class ProtectionState {
    /**
     * I have no idea what this one means.
     */
    object WasHandled : ProtectionState() {
        const val id = "was_protection_handled"
    }

    /**
     * There's no protection set for the app.
     */
    object None : ProtectionState() {
        const val id = -1
    }

    /**
     * The protection set for the app is a pattern.
     */
    object Pattern : ProtectionState() {
        const val id = 0
    }

    /**
     * The protection set for the app is a 4-digit pin.
     */
    object Pin : ProtectionState() {
        const val id = 1
    }

    /**
     * The protection set for the app is fingerprint.
     */
    object FingerPrint : ProtectionState() {
        const val id = 2
    }
}
