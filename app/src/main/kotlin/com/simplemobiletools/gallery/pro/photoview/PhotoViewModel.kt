package com.simplemobiletools.gallery.pro.photoview

import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModel
import java.util.ArrayList

class PhotoViewModel : ViewModel() {

    fun degreesForRotation(orientation: Int) = when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_270 -> 270
        ExifInterface.ORIENTATION_ROTATE_180 -> 180
        ExifInterface.ORIENTATION_ROTATE_90 -> 90
        else -> 0
    }

    fun getCoverImageIndex(paths: ArrayList<String>): Int {
        var coverIndex = -1
        paths.forEachIndexed { index, path ->
            if (path.contains("cover", true)) {
                coverIndex = index
            }
        }

        if (coverIndex == -1) {
            paths.forEachIndexed { index, path ->
                if (path.isNotEmpty()) {
                    coverIndex = index
                }
            }
        }
        return coverIndex
    }

    companion object {
        // devices with good displays, but the rest of the hardware not good enough for them
        val WEIRD_DEVICES = arrayListOf(
            "motorola xt1685",
            "google nexus 5x"
        )
    }
}
