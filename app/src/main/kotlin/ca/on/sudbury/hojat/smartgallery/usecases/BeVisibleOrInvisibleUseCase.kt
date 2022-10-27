package ca.on.sudbury.hojat.smartgallery.usecases

import android.view.View

/**
 * You give it a [View] and a Boolean. If it's True, the view will be VISIBLE; and if it's false, the view will be INVISIBLE.
 */
object BeVisibleOrInvisibleUseCase {
    operator fun invoke(view: View?, beVisible: Boolean) {
        if (view == null) return
        if (beVisible) view.visibility = View.VISIBLE else view.visibility = View.INVISIBLE
    }
}