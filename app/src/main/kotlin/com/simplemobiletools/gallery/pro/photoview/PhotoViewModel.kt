package com.simplemobiletools.gallery.pro.photoview

import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModel

class PhotoViewModel() : ViewModel() {

    fun degreesForRotation(orientation: Int) = when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_270 -> 270
        ExifInterface.ORIENTATION_ROTATE_180 -> 180
        ExifInterface.ORIENTATION_ROTATE_90 -> 90
        else -> 0
    }
}
