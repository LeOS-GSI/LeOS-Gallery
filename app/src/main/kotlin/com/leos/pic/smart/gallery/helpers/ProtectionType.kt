package ca.on.sudbury.hojat.smartgallery.helpers

/**
 * The type of protection that is set.
 */
sealed class ProtectionType {
    /**
     * I have no idea what this one means.
     */
    object WasHandled : ProtectionType() {
        const val id = "was_protection_handled"
    }

    /**
     * There's no protection set for the app.
     */
    object None : ProtectionType() {
        const val id = -1
    }

    /**
     * The protection set for the app is a pattern.
     */
    object Pattern : ProtectionType() {
        const val id = 0
    }

    /**
     * The protection set for the app is a 4-digit pin.
     */
    object Pin : ProtectionType() {
        const val id = 1
    }

    /**
     * The protection set for the app is fingerprint.
     */
    object FingerPrint : ProtectionType() {
        const val id = 2
    }
}
