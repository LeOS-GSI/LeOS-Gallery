package ca.on.sudbury.hojat.smartgallery.usecases

import android.content.Context
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.extensions.baseConfig

/**
 * If I'm not mistaken, this UseCase checks whether icon color is correctly
 * applied or not. If no, applies the color to icon according to what user
 * has previously chosen.
 */
object CheckAppIconColorUseCase {
    operator fun invoke(owner: Context) {

        val appId = owner.baseConfig.appId
        if (appId.isNotEmpty() && owner.baseConfig.lastIconColor != owner.baseConfig.appIconColor) {
            owner.resources.getIntArray(R.array.md_app_icon_colors).toCollection(ArrayList())
                .forEachIndexed { index, color ->
                    ToggleAppIconColorUseCase(owner, appId, index, color, false)
                }

            owner.resources.getIntArray(R.array.md_app_icon_colors).toCollection(ArrayList())
                .forEachIndexed { index, color ->
                    if (owner.baseConfig.appIconColor == color) {
                        ToggleAppIconColorUseCase(owner, appId, index, color, true)
                    }
                }
        }
    }
}