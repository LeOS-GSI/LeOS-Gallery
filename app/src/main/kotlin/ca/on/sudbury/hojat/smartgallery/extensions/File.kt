package ca.on.sudbury.hojat.smartgallery.extensions

import com.simplemobiletools.commons.helpers.videoExtensions
import java.io.File

fun File.isVideoFast() = videoExtensions.any { absolutePath.endsWith(it, true) }
