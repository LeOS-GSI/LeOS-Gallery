package ca.on.sudbury.hojat.smartgallery.gestures

import android.graphics.Matrix
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.view.Gravity
import kotlin.math.roundToInt

object GravityUtils {
    private val tmpMatrix = Matrix()
    private val tmpRectF = RectF()
    private val tmpRect1 = Rect()
    private val tmpRect2 = Rect()

    fun getImagePosition(state: State, settings: Settings, out: Rect) {
        state[tmpMatrix]
        getImagePosition(tmpMatrix, settings, out)
    }

    fun getImagePosition(matrix: Matrix, settings: Settings, out: Rect) {
        tmpRectF.set(0f, 0f, settings.imageWidth, settings.imageHeight)

        matrix.mapRect(tmpRectF)

        val width = tmpRectF.width().roundToInt()
        val height = tmpRectF.height().roundToInt()

        tmpRect1.set(0, 0, settings.viewportWidth, settings.viewportHeight)
        Gravity.apply(Gravity.CENTER, width, height, tmpRect1, out)
    }

    fun getMovementAreaPosition(settings: Settings, out: Rect) {
        tmpRect1.set(0, 0, settings.viewportWidth, settings.viewportHeight)
        Gravity.apply(
            Gravity.CENTER,
            settings.viewportWidth,
            settings.viewportHeight,
            tmpRect1,
            out
        )
    }

    fun getDefaultPivot(settings: Settings, out: Point) {
        getMovementAreaPosition(settings, tmpRect2)
        Gravity.apply(Gravity.CENTER, 0, 0, tmpRect2, tmpRect1)
        out.set(tmpRect1.left, tmpRect1.top)
    }
}
