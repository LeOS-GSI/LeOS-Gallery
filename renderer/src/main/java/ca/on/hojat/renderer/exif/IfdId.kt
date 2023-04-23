package ca.on.hojat.renderer.exif

/**
 * The constants of the IFD ID defined in EXIF spec.
 */
interface IfdId {
    companion object {
        const val TYPE_IFD_0 = 0
        const val TYPE_IFD_1 = 1
        const val TYPE_IFD_EXIF = 2
        const val TYPE_IFD_INTEROPERABILITY = 3
        const val TYPE_IFD_GPS = 4

        // This is used in ExifData to allocate enough IfdData
        const val TYPE_IFD_COUNT = 5
    }
}