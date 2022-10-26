package ca.on.sudbury.hojat.smartgallery.extensions

import android.os.SystemClock
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver

fun View.beGoneIf(beGone: Boolean) = beVisibleIf(!beGone)

fun View.beInvisible() {
    visibility = View.INVISIBLE
}

fun View.beInvisibleIf(beInvisible: Boolean) = if (beInvisible) beInvisible() else visibility = View.VISIBLE

fun View.beVisible() {
    visibility = View.VISIBLE
}

fun View.beVisibleIf(beVisible: Boolean) = if (beVisible) visibility = View.VISIBLE else visibility = View.GONE

fun View.isGone() = visibility == View.GONE

fun View.isVisible() = visibility == View.VISIBLE

fun View.onGlobalLayout(callback: () -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            viewTreeObserver.removeOnGlobalLayoutListener(this)
            callback()
        }
    })
}

fun View.performHapticFeedback() = performHapticFeedback(
    HapticFeedbackConstants.VIRTUAL_KEY,
    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
)


fun View.sendFakeClick(x: Float, y: Float) {
    val uptime = SystemClock.uptimeMillis()
    val event = MotionEvent.obtain(uptime, uptime, MotionEvent.ACTION_DOWN, x, y, 0)
    dispatchTouchEvent(event)
    event.action = MotionEvent.ACTION_UP
    dispatchTouchEvent(event)
}