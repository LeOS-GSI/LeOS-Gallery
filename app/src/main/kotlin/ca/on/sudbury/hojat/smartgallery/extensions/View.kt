package ca.on.sudbury.hojat.smartgallery.extensions

import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import com.simplemobiletools.commons.extensions.beGone

fun View.beGone() {
    visibility = View.GONE
}

fun View.beInvisible() {
    visibility = View.INVISIBLE
}

fun View.beVisible() {
    visibility = View.VISIBLE
}

fun View.beVisibleIf(beVisible: Boolean) = if (beVisible) beVisible() else beGone()

fun View.isGone() = visibility == View.GONE

fun View.isVisible() = visibility == View.VISIBLE

fun View.sendFakeClick(x: Float, y: Float) {
    val uptime = SystemClock.uptimeMillis()
    val event = MotionEvent.obtain(uptime, uptime, MotionEvent.ACTION_DOWN, x, y, 0)
    dispatchTouchEvent(event)
    event.action = MotionEvent.ACTION_UP
    dispatchTouchEvent(event)
}