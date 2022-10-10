package ca.on.sudbury.hojat.smartgallery.gestures

import android.os.SystemClock
import android.view.animation.AccelerateDecelerateInterpolator

class FloatScroller {
    private val interpolator = AccelerateDecelerateInterpolator()
    private var startValue = 0f
    private var startRtc = 0L
    private var final = 0f
    var isFinished = true
    var curr = 0f

    fun forceFinished() {
        isFinished = true
    }

    fun startScroll(startValue: Float, finalValue: Float) {
        isFinished = false
        startRtc = SystemClock.elapsedRealtime()

        this.startValue = startValue
        this.final = finalValue
        curr = startValue
    }

    fun computeScroll() {
        if (isFinished) {
            return
        }

        val elapsed = SystemClock.elapsedRealtime() - startRtc
        val duration = Settings.ANIMATIONS_DURATION
        if (elapsed >= duration) {
            isFinished = true
            curr = final
            return
        }

        val time = interpolator.getInterpolation(elapsed.toFloat() / duration)
        curr = interpolate(startValue, final, time)
    }

    private fun interpolate(x1: Float, x2: Float, state: Float) = x1 + (x2 - x1) * state
}
