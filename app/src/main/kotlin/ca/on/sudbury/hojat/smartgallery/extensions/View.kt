package ca.on.sudbury.hojat.smartgallery.extensions

import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver

fun View.beInvisibleIf(beInvisible: Boolean) =
    if (beInvisible) visibility = View.INVISIBLE else visibility = View.VISIBLE

fun View.beVisibleIf(beVisible: Boolean) =
    if (beVisible) visibility = View.VISIBLE else visibility = View.GONE

fun View.onGlobalLayout(callback: () -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            viewTreeObserver.removeOnGlobalLayoutListener(this)
            callback()
        }
    })
}

fun View.sendFakeClick(x: Float, y: Float) {
    val uptime = SystemClock.uptimeMillis()
    val event = MotionEvent.obtain(uptime, uptime, MotionEvent.ACTION_DOWN, x, y, 0)
    dispatchTouchEvent(event)
    event.action = MotionEvent.ACTION_UP
    dispatchTouchEvent(event)
}