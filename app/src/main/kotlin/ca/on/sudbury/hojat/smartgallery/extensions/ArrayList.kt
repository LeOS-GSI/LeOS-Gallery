package ca.on.sudbury.hojat.smartgallery.extensions

import ca.on.sudbury.hojat.smartgallery.helpers.TYPE_IMAGES
import ca.on.sudbury.hojat.smartgallery.helpers.TYPE_VIDEOS
import ca.on.sudbury.hojat.smartgallery.helpers.TYPE_GIFS
import ca.on.sudbury.hojat.smartgallery.helpers.TYPE_RAWS
import ca.on.sudbury.hojat.smartgallery.helpers.TYPE_SVGS
import ca.on.sudbury.hojat.smartgallery.helpers.TYPE_PORTRAITS
import ca.on.sudbury.hojat.smartgallery.models.Medium

fun ArrayList<Medium>.getDirMediaTypes(): Int {
    var types = 0
    if (any { it.isImage() }) {
        types += TYPE_IMAGES
    }

    if (any { it.isVideo() }) {
        types += TYPE_VIDEOS
    }

    if (any { it.isGIF() }) {
        types += TYPE_GIFS
    }

    if (any { it.isRaw() }) {
        types += TYPE_RAWS
    }

    if (any { it.isSVG() }) {
        types += TYPE_SVGS
    }

    if (any { it.isPortrait() }) {
        types += TYPE_PORTRAITS
    }

    return types
}

fun <T> ArrayList<T>.moveLastItemToFront() {
    val last = removeAt(size - 1)
    add(0, last)
}
