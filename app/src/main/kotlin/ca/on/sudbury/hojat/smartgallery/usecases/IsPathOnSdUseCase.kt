package ca.on.sudbury.hojat.smartgallery.usecases

import android.content.Context
import ca.on.sudbury.hojat.smartgallery.extensions.baseConfig

/**
 * You give it the owner in form of the [Context] and a path in form of [String].
 * If the path is on SD card, returns true.
 */
object IsPathOnSdUseCase {
    operator fun invoke(owner: Context?, path: String): Boolean {
        if (owner == null) return false // if owner is null, we say path is not on SD card
        return (owner.baseConfig.sdCardPath.isNotEmpty() && path.startsWith(owner.baseConfig.sdCardPath))
    }
}