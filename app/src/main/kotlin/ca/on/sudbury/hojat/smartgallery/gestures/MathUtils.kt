package ca.on.sudbury.hojat.smartgallery.gestures

import android.graphics.Matrix
import androidx.annotation.Size
import kotlin.math.abs

object MathUtils {
    private val tmpMatrix = Matrix()
    private val tmpMatrixInverse = Matrix()

    fun restrict(value: Float, minValue: Float, maxValue: Float) =
        minValue.coerceAtLeast(value.coerceAtMost(maxValue))

    private fun interpolate(start: Float, end: Float, factor: Float) =
        start + (end - start) * factor

    fun interpolate(out: State, start: State, end: State, factor: Float) {
        interpolate(out, start, start.x, start.y, end, end.x, end.y, factor)
    }

    fun interpolate(
        out: State,
        start: State,
        startPivotX: Float,
        startPivotY: Float,
        end: State,
        endPivotX: Float,
        endPivotY: Float,
        factor: Float
    ) {
        out.set(start)

        if (!State.equals(start.zoom, end.zoom)) {
            val zoom = interpolate(start.zoom, end.zoom, factor)
            out.zoomTo(zoom, startPivotX, startPivotY)
        }

        val startRotation = start.rotation
        val endRotation = end.rotation
        var rotation = java.lang.Float.NaN

        if (abs(startRotation - endRotation) <= 180f) {
            if (!State.equals(startRotation, endRotation)) {
                rotation = interpolate(startRotation, endRotation, factor)
            }
        } else {
            val startRotationPositive =
                if (startRotation < 0f) startRotation + 360f else startRotation
            val endRotationPositive = if (endRotation < 0f) endRotation + 360f else endRotation

            if (!State.equals(startRotationPositive, endRotationPositive)) {
                rotation = interpolate(startRotationPositive, endRotationPositive, factor)
            }
        }

        if (!java.lang.Float.isNaN(rotation)) {
            out.rotateTo(rotation, startPivotX, startPivotY)
        }

        val dx = interpolate(0f, endPivotX - startPivotX, factor)
        val dy = interpolate(0f, endPivotY - startPivotY, factor)
        out.translateBy(dx, dy)
    }

    fun computeNewPosition(@Size(2) point: FloatArray, initialState: State, finalState: State) {
        initialState[tmpMatrix]
        tmpMatrix.invert(tmpMatrixInverse)
        tmpMatrixInverse.mapPoints(point)
        finalState[tmpMatrix]
        tmpMatrix.mapPoints(point)
    }
}
