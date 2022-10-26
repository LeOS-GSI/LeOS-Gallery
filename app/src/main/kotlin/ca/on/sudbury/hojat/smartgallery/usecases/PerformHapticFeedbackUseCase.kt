package ca.on.sudbury.hojat.smartgallery.usecases

import android.view.HapticFeedbackConstants
import android.view.View

/**
 * You give it a [View] and it performs haptic feedback with the context of that view.
 */
object PerformHapticFeedbackUseCase {
    operator fun invoke(owner: View) {
        owner.performHapticFeedback(
            HapticFeedbackConstants.VIRTUAL_KEY,
            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        )
    }
}