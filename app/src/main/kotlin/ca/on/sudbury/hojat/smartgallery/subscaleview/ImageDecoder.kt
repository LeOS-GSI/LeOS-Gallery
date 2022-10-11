package ca.on.sudbury.hojat.smartgallery.subscaleview

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri

interface ImageDecoder {
    fun decode(context: Context, uri: Uri): Bitmap
}
