package ca.on.sudbury.hojat.smartgallery.gestures

import android.graphics.Matrix
import kotlin.math.atan2
import kotlin.math.hypot

class State {
    companion object {
        private const val EPSILON = 0.001f

        fun equals(v1: Float, v2: Float) = v1 >= v2 - EPSILON && v1 <= v2 + EPSILON

        fun compare(v1: Float) = if (v1 > 0f + EPSILON) 1 else if (v1 < 0f - EPSILON) -1 else 0
    }

    private val matrix = Matrix()
    private val matrixValues = FloatArray(9)

    var x = 0f
    var y = 0f
    var zoom = 1f
    var rotation = 0f

    operator fun get(matrix: Matrix) {
        matrix.set(this.matrix)
    }

    fun translateBy(dx: Float, dy: Float) {
        matrix.postTranslate(dx, dy)
        updateFromMatrix(updateZoom = false, updateRotation = false)
    }

    fun translateTo(x: Float, y: Float) {
        matrix.postTranslate(-this.x + x, -this.y + y)
        updateFromMatrix(updateZoom = false, updateRotation = false)
    }

    fun zoomBy(factor: Float, pivotX: Float, pivotY: Float) {
        matrix.postScale(factor, factor, pivotX, pivotY)
        updateFromMatrix(updateZoom = true, updateRotation = false)
    }

    fun zoomTo(zoom: Float, pivotX: Float, pivotY: Float) {
        matrix.postScale(zoom / this.zoom, zoom / this.zoom, pivotX, pivotY)
        updateFromMatrix(updateZoom = true, updateRotation = false)
    }

    fun rotateBy(angle: Float, pivotX: Float, pivotY: Float) {
        matrix.postRotate(angle, pivotX, pivotY)
        updateFromMatrix(updateZoom = false, updateRotation = true)
    }

    fun rotateTo(angle: Float, pivotX: Float, pivotY: Float) {
        matrix.postRotate(-rotation + angle, pivotX, pivotY)
        updateFromMatrix(updateZoom = false, updateRotation = true)
    }

    operator fun set(x: Float, y: Float, zoom: Float, rotation: Float) {
        var newRotation = rotation
        while (newRotation < -180f) {
            newRotation += 360f
        }

        while (newRotation > 180f) {
            newRotation -= 360f
        }

        this.x = x
        this.y = y
        this.zoom = zoom
        this.rotation = newRotation

        matrix.reset()
        if (zoom != 1f) {
            matrix.postScale(zoom, zoom)
        }

        if (newRotation != 0f) {
            matrix.postRotate(newRotation)
        }

        matrix.postTranslate(x, y)
    }

    fun set(other: State) {
        x = other.x
        y = other.y
        zoom = other.zoom
        rotation = other.rotation
        matrix.set(other.matrix)
    }

    fun copy(): State {
        val copy = State()
        copy.set(this)
        return copy
    }

    private fun updateFromMatrix(updateZoom: Boolean, updateRotation: Boolean) {
        matrix.getValues(matrixValues)
        x = matrixValues[2]
        y = matrixValues[5]
        if (updateZoom) {
            zoom = hypot(matrixValues[1].toDouble(), matrixValues[4].toDouble()).toFloat()
        }

        if (updateRotation) {
            rotation = Math.toDegrees(atan2(matrixValues[3].toDouble(), matrixValues[4].toDouble())).toFloat()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other == null || javaClass != other.javaClass) {
            return false
        }

        val state = other as State?

        return equals(state!!.x, x) && equals(state.y, y) && equals(state.zoom, zoom) && equals(state.rotation, rotation)
    }

    override fun hashCode(): Int {
        var result = if (x != 0f) java.lang.Float.floatToIntBits(x) else 0
        result = 31 * result + if (y != 0f) java.lang.Float.floatToIntBits(y) else 0
        result = 31 * result + if (zoom != 0f) java.lang.Float.floatToIntBits(zoom) else 0
        result = 31 * result + if (rotation != 0f) java.lang.Float.floatToIntBits(rotation) else 0
        return result
    }

    override fun toString() = "State(x=$x, y=$y, zoom=$zoom, rotation=$rotation)"
}
