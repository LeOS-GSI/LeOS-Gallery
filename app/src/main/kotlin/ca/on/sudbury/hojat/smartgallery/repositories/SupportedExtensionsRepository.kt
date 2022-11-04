package ca.on.sudbury.hojat.smartgallery.repositories

/**
 * All the file extensions currently supported by the app are accessed via this repository.
 * Right now, I'm defining it as an object, because it's just exposing a few data sources
 * and doesn't do anything else.
 */
object SupportedExtensionsRepository {
    val photoExtensions: Array<String>
        get() = arrayOf(
            ".jpg",
            ".png",
            ".jpeg",
            ".bmp",
            ".webp",
            ".heic",
            ".heif",
            ".apng",
            ".avif"
        )
    val videoExtensions: Array<String>
        get() = arrayOf(
            ".mp4",
            ".mkv",
            ".webm",
            ".avi",
            ".3gp",
            ".mov",
            ".m4v",
            ".3gpp"
        )
    val audioExtensions: Array<String>
        get() = arrayOf(
            ".mp3",
            ".wav",
            ".wma",
            ".ogg",
            ".m4a",
            ".opus",
            ".flac",
            ".aac"
        )
    val rawExtensions: Array<String>
        get() = arrayOf(
            ".dng",
            ".orf",
            ".nef",
            ".arw",
            ".rw2",
            ".cr2",
            ".cr3"
        )
}