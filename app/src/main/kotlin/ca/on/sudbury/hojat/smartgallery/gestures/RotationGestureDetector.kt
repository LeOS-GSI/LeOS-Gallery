package ca.on.sudbury.hojat.smartgallery.gestures

import android.view.MotionEvent
import kotlin.math.abs
import kotlin.math.atan2

class RotationGestureDetector(private val listener: OnRotationGestureListener) {
    companion object {
        private const val ROTATION_SLOP = 5f
    }

    private var initialAngle = 0f
    private var currAngle = 0f
    private var prevAngle = 0f
    private var isInProgress = false
    private var isGestureAccepted = false
    var focusX = 0f
    var focusY = 0f

    fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> cancelRotation()
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.pointerCount == 2) {
                    currAngle = computeRotation(event)
                    prevAngle = currAngle
                    initialAngle = prevAngle
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount >= 2 && (!isInProgress || isGestureAccepted)) {
                    currAngle = computeRotation(event)
                    focusX = 0.5f * (event.getX(1) + event.getX(0))
                    focusY = 0.5f * (event.getY(1) + event.getY(0))
                    val isAlreadyStarted = isInProgress
                    tryStartRotation()
                    val isAccepted = !isAlreadyStarted || processRotation()
                    if (isAccepted) {
                        prevAngle = currAngle
                    }
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {
                if (event.pointerCount == 2) {
                    cancelRotation()
                }
            }
        }

        return true
    }

    private fun tryStartRotation() {
        if (isInProgress || abs(initialAngle - currAngle) < ROTATION_SLOP) {
            return
        }

        isInProgress = true
        isGestureAccepted = listener.onRotationBegin(this)
    }

    private fun cancelRotation() {
        if (!isInProgress) {
            return
        }

        isInProgress = false
        if (isGestureAccepted) {
            listener.onRotationEnd(this)
            isGestureAccepted = false
        }
    }

    private fun processRotation() = isInProgress && isGestureAccepted && listener.onRotate(this)

    private fun computeRotation(event: MotionEvent): Float {
        return Math.toDegrees(
            atan2(
                (event.getY(1) - event.getY(0)).toDouble(),
                (event.getX(1) - event.getX(0)).toDouble()
            )
        ).toFloat()
    }

    fun getRotationDelta() = currAngle - prevAngle

    interface OnRotationGestureListener {
        fun onRotate(detector: RotationGestureDetector): Boolean

        fun onRotationBegin(detector: RotationGestureDetector): Boolean

        fun onRotationEnd(detector: RotationGestureDetector)
    }
}
