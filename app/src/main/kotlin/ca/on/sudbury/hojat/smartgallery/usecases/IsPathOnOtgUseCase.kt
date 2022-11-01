package ca.on.sudbury.hojat.smartgallery.usecases

import android.content.Context
import ca.on.sudbury.hojat.smartgallery.extensions.baseConfig

/**
 * You give it the owner in form of the [Context] and a path in form of [String].
 * If the path is on USB OTG, returns true.
 */
object IsPathOnOtgUseCase {
    operator fun invoke(owner: Context?, path: String): Boolean {
        if (owner == null) return false // if owner is null, we say path is not on OTG
        return (owner.baseConfig.OTGPath.isNotEmpty() && path.startsWith(owner.baseConfig.OTGPath))
    }
}