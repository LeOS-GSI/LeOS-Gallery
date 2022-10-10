package ca.on.sudbury.hojat.smartgallery.gestures

import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import kotlin.math.roundToInt
import kotlin.math.sqrt

class StateController internal constructor(private val settings: Settings) {
    companion object {
        private val tmpState = State()
        private val tmpRect = Rect()
        private val tmpRectF = RectF()
        private val tmpPoint = Point()
        private val tmpPointF = PointF()
    }

    private var isResetRequired = true
    private var zoomPatch = 0f

    private val zoomBounds = ZoomBounds(settings)
    private val movBounds = MovementBounds(settings)

    fun resetState(state: State): Boolean {
        isResetRequired = true
        return updateState(state)
    }

    fun updateState(state: State): Boolean {
        return if (isResetRequired) {
            state[0f, 0f, zoomBounds.set(state).fitZoom] = 0f
            GravityUtils.getImagePosition(state, settings, tmpRect)
            state.translateTo(tmpRect.left.toFloat(), tmpRect.top.toFloat())

            isResetRequired = !settings.hasImageSize() || !settings.hasViewportSize()
            !isResetRequired
        } else {
            restrictStateBounds(
                state, state, java.lang.Float.NaN, java.lang.Float.NaN,
                allowOverzoom = false,
                restrictRotation = true
            )
            false
        }
    }

    fun setTempZoomPatch(factor: Float) {
        zoomPatch = factor
    }

    fun applyZoomPatch(state: State) {
        if (zoomPatch > 0f) {
            state[state.x, state.y, state.zoom * zoomPatch] = state.rotation
        }
    }

    fun toggleMinMaxZoom(state: State, pivotX: Float, pivotY: Float): State {
        zoomBounds.set(state)
        val minZoom = zoomBounds.fitZoom
        val maxZoom =
            if (settings.doubleTapZoom > 0f) settings.doubleTapZoom else zoomBounds.maxZoom

        val middleZoom = 0.5f * (minZoom + maxZoom)
        val targetZoom = if (state.zoom < middleZoom) maxZoom else minZoom

        val end = state.copy()
        end.zoomTo(targetZoom, pivotX, pivotY)
        return end
    }

    fun restrictStateBoundsCopy(
        state: State,
        prevState: State,
        pivotX: Float,
        pivotY: Float
    ): State? {
        tmpState.set(state)
        val changed = restrictStateBounds(
            tmpState, prevState, pivotX, pivotY,
            allowOverzoom = false,
            restrictRotation = true
        )
        return if (changed) tmpState.copy() else null
    }

    fun restrictStateBounds(
        state: State,
        prevState: State?,
        pivotX: Float,
        pivotY: Float,
        allowOverzoom: Boolean,
        restrictRotation: Boolean
    ): Boolean {
        var newPivotX = pivotX
        var newPivotY = pivotY
        if (java.lang.Float.isNaN(newPivotX) || java.lang.Float.isNaN(newPivotY)) {
            GravityUtils.getDefaultPivot(settings, tmpPoint)
            newPivotX = tmpPoint.x.toFloat()
            newPivotY = tmpPoint.y.toFloat()
        }

        var isStateChanged = false

        if (restrictRotation) {
            val rotation = (state.rotation / 90f).roundToInt() * 90f
            if (!State.equals(rotation, state.rotation)) {
                state.rotateTo(rotation, newPivotX, newPivotY)
                isStateChanged = true
            }
        }

        zoomBounds.set(state)
        val minZoom = zoomBounds.minZoom
        val maxZoom = zoomBounds.maxZoom

        val extraZoom = if (allowOverzoom) Settings.OVERZOOM_FACTOR else 1f
        var zoom = zoomBounds.restrict(state.zoom, extraZoom)

        if (prevState != null) {
            zoom = applyZoomResilience(zoom, prevState.zoom, minZoom, maxZoom, extraZoom)
        }

        if (!State.equals(zoom, state.zoom)) {
            state.zoomTo(zoom, newPivotX, newPivotY)
            isStateChanged = true
        }

        val extraX = 0f
        val extraY = 0f

        movBounds.set(state)
        movBounds.restrict(state.x, state.y, extraX, extraY, tmpPointF)
        var newX = tmpPointF.x
        var newY = tmpPointF.y

        if (zoom < minZoom) {
            var factor = (extraZoom * zoom / minZoom - 1f) / (extraZoom - 1f)
            factor = sqrt(factor.toDouble()).toFloat()

            movBounds.restrict(newX, newY, tmpPointF)
            val strictX = tmpPointF.x
            val strictY = tmpPointF.y

            newX = strictX + factor * (newX - strictX)
            newY = strictY + factor * (newY - strictY)
        }

        if (prevState != null) {
            movBounds.getExternalBounds(tmpRectF)
            newX =
                applyTranslationResilience(newX, prevState.x, tmpRectF.left, tmpRectF.right, extraX)
            newY =
                applyTranslationResilience(newY, prevState.y, tmpRectF.top, tmpRectF.bottom, extraY)
        }

        if (!State.equals(newX, state.x) || !State.equals(newY, state.y)) {
            state.translateTo(newX, newY)
            isStateChanged = true
        }

        return isStateChanged
    }

    private fun applyZoomResilience(
        zoom: Float,
        prevZoom: Float,
        minZoom: Float,
        maxZoom: Float,
        overzoom: Float
    ): Float {
        if (overzoom == 1f) {
            return zoom
        }

        val minZoomOver = minZoom / overzoom
        val maxZoomOver = maxZoom * overzoom

        var resilience = 0f

        if (zoom < minZoom && zoom < prevZoom) {
            resilience = (minZoom - zoom) / (minZoom - minZoomOver)
        } else if (zoom > maxZoom && zoom > prevZoom) {
            resilience = (zoom - maxZoom) / (maxZoomOver - maxZoom)
        }

        return if (resilience == 0f) {
            zoom
        } else {
            resilience = sqrt(resilience.toDouble()).toFloat()
            zoom + resilience * (prevZoom - zoom)
        }
    }

    private fun applyTranslationResilience(
        value: Float,
        prevValue: Float,
        boundsMin: Float,
        boundsMax: Float,
        overscroll: Float
    ): Float {
        if (overscroll == 0f) {
            return value
        }

        var resilience = 0f
        val avg = (value + prevValue) * 0.5f

        if (avg < boundsMin && value < prevValue) {
            resilience = (boundsMin - avg) / overscroll
        } else if (avg > boundsMax && value > prevValue) {
            resilience = (avg - boundsMax) / overscroll
        }

        return if (resilience == 0f) {
            value
        } else {
            if (resilience > 1f) {
                resilience = 1f
            }
            resilience = sqrt(resilience.toDouble()).toFloat()
            value - resilience * (value - prevValue)
        }
    }

    fun getMovementArea(state: State, out: RectF) {
        movBounds.set(state).getExternalBounds(out)
    }
}
