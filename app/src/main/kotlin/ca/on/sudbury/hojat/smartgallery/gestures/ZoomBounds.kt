package ca.on.sudbury.hojat.smartgallery.gestures

import android.graphics.Matrix
import android.graphics.RectF

class ZoomBounds(private val settings: Settings) {
    companion object {
        private val tmpMatrix = Matrix()
        private val tmpRectF = RectF()
    }

    var minZoom = 0f
    var maxZoom = 0f
    var fitZoom = 0f

    fun set(state: State): ZoomBounds {
        var imageWidth = settings.imageWidth
        var imageHeight = settings.imageHeight

        val areaWidth = settings.viewportWidth.toFloat()
        val areaHeight = settings.viewportHeight.toFloat()

        if (imageWidth == 0f || imageHeight == 0f || areaWidth == 0f || areaHeight == 0f) {
            fitZoom = 1f
            maxZoom = fitZoom
            minZoom = maxZoom
            return this
        }

        minZoom = fitZoom
        maxZoom = settings.maxZoom

        val rotation = state.rotation

        if (!State.equals(rotation, 0f)) {
            tmpMatrix.setRotate(rotation)
            tmpRectF.set(0f, 0f, imageWidth, imageHeight)
            tmpMatrix.mapRect(tmpRectF)
            imageWidth = tmpRectF.width()
            imageHeight = tmpRectF.height()
        }

        fitZoom = (areaWidth / imageWidth).coerceAtMost(areaHeight / imageHeight)

        if (maxZoom <= 0f) {
            maxZoom = fitZoom
        }

        if (fitZoom > maxZoom) {
            maxZoom = fitZoom
        }

        if (minZoom > maxZoom) {
            minZoom = maxZoom
        }

        if (fitZoom < minZoom) {
            minZoom = fitZoom
        }

        return this
    }

    fun restrict(zoom: Float, extraZoom: Float) =
        MathUtils.restrict(zoom, minZoom / extraZoom, maxZoom * extraZoom)
}
