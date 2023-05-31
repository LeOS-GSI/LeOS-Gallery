package ca.on.sudbury.hojat.smartgallery.gestures

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

class GestureFrameLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : FrameLayout(context, attrs, defStyle) {
    companion object {
        private fun getChildMeasureSpecFixed(spec: Int, extra: Int, childDimension: Int): Int {
            return if (childDimension == ViewGroup.LayoutParams.WRAP_CONTENT) {
                MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(spec), MeasureSpec.UNSPECIFIED)
            } else {
                ViewGroup.getChildMeasureSpec(spec, extra, childDimension)
            }
        }
    }

    val controller = GestureController(this)

    private val viewMatrix = Matrix()
    private val matrixInverse = Matrix()

    private val tmpPointArray = FloatArray(2)

    private var currentMotionEvent: MotionEvent? = null

    init {
        controller.addOnStateChangeListener(object : GestureController.OnStateChangeListener {
            override fun onStateChanged(state: State) {
                applyState(state)
            }
        })
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        currentMotionEvent = event
        val invertedEvent = applyMatrix(event, matrixInverse)
        try {
            return super.dispatchTouchEvent(invertedEvent)
        } finally {
            invertedEvent.recycle()
        }
    }

    override fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        super.requestDisallowInterceptTouchEvent(disallowIntercept)

        if (disallowIntercept) {
            val cancel = MotionEvent.obtain(currentMotionEvent)
            cancel.action = MotionEvent.ACTION_CANCEL
            controller.onInterceptTouch(this, cancel)
            cancel.recycle()
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return if (currentMotionEvent != null) {
            controller.onInterceptTouch(this, currentMotionEvent!!)
        } else {
            false
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (currentMotionEvent != null) {
            controller.onTouch(this, currentMotionEvent!!)
        } else {
            false
        }
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)

        controller.settings.setViewport(width - paddingLeft - paddingRight, height - paddingTop - paddingBottom)
        controller.updateState()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val child = if (childCount == 0) {
            null
        } else {
            getChildAt(0)
        }

        if (child != null) {
            controller.settings.setImageSize(child.measuredWidth.toFloat(), child.measuredHeight.toFloat())
            controller.updateState()
        }
    }

    override fun measureChildWithMargins(child: View, parentWidthMeasureSpec: Int, widthUsed: Int, parentHeightMeasureSpec: Int, heightUsed: Int) {
        val layoutParams = child.layoutParams as MarginLayoutParams
        val extraW = (paddingLeft + paddingRight + layoutParams.leftMargin + layoutParams.rightMargin + widthUsed)
        val extraH = (paddingTop + paddingBottom + layoutParams.topMargin + layoutParams.bottomMargin + heightUsed)
        child.measure(
            getChildMeasureSpecFixed(parentWidthMeasureSpec, extraW, layoutParams.width),
                getChildMeasureSpecFixed(parentHeightMeasureSpec, extraH, layoutParams.height)
        )
    }

    private fun applyState(state: State) {
        state[viewMatrix]
        viewMatrix.invert(matrixInverse)
        invalidate()
    }

    override fun dispatchDraw(canvas: Canvas) {
        canvas.save()
        canvas.concat(viewMatrix)
        super.dispatchDraw(canvas)
        canvas.restore()
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        if (childCount != 0) {
            throw IllegalArgumentException("GestureFrameLayout can contain only one child")
        }
        super.addView(child, index, params)
    }


    private fun applyMatrix(event: MotionEvent, matrix: Matrix): MotionEvent {
        tmpPointArray[0] = event.x
        tmpPointArray[1] = event.y
        matrix.mapPoints(tmpPointArray)

        val copy = MotionEvent.obtain(event)
        copy.setLocation(tmpPointArray[0], tmpPointArray[1])
        return copy
    }
}
