package ca.on.sudbury.hojat.smartgallery.helpers

sealed class ProtectionState {
    /**
     * I have no idea what this one means.
     */
    object WasProtectionHandled : ProtectionState() {
        const val id = "was_protection_handled"
    }

    /**
     * There's no protection set for the app.
     */
    object ProtectionNone : ProtectionState() {
        const val id = -1
    }

    /**
     * The protection set for the app is a pattern.
     */
    object ProtectionPattern : ProtectionState() {
        const val id = 0
    }

    /**
     * The protection set for the app is a 4-digit pin.
     */
    object ProtectionPin : ProtectionState() {
        const val id = 1
    }

    /**
     * The protection set for the app is fingerprint.
     */
    object ProtectionFingerPrint : ProtectionState() {
        const val id = 2
    }
}
