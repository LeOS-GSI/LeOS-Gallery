package ca.on.sudbury.hojat.smartgallery.helpers

/**
 * The full screen media will be rotated by 3 different criteria:
 * 1- System setting
 * 2- Device rotation
 * 3- Aspect ratio
 */
sealed class RotationRule {
    object SystemSetting : RotationRule() {
        const val id = 0
    }

    object DeviceRotation : RotationRule() {
        const val id = 1
    }

    object AspectRatio : RotationRule() {
        const val id = 2
    }
}
