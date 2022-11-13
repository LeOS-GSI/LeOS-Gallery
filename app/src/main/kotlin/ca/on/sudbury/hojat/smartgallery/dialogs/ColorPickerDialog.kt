package ca.on.sudbury.hojat.smartgallery.dialogs

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintHelper
import androidx.core.widget.addTextChangedListener
import ca.on.sudbury.hojat.smartgallery.extensions.baseConfig
import ca.on.sudbury.hojat.smartgallery.extensions.copyToClipboard
import ca.on.sudbury.hojat.smartgallery.extensions.getAlertDialogBuilder
import ca.on.sudbury.hojat.smartgallery.extensions.getProperTextColor
import ca.on.sudbury.hojat.smartgallery.extensions.onGlobalLayout
import ca.on.sudbury.hojat.smartgallery.extensions.fillWithColor
import ca.on.sudbury.hojat.smartgallery.extensions.setupDialogStuff
import ca.on.sudbury.hojat.smartgallery.extensions.toHex
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.databinding.DialogColorPickerBinding
import ca.on.hojat.palette.views.ColorPickerSquare
import ca.on.sudbury.hojat.smartgallery.usecases.IsQPlusUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.ApplyColorFilterUseCase
import timber.log.Timber
import java.util.LinkedList

private const val RECENT_COLORS_NUMBER = 5

@SuppressLint("ClickableViewAccessibility", "SetTextI18n")
class ColorPickerDialog(
    val activity: Activity,
    color: Int,
    private val removeDimmedBackground: Boolean = false,
    showUseDefaultButton: Boolean = false,
    val currentColorCallback: ((color: Int) -> Unit)? = null,
    val callback: (wasPositivePressed: Boolean, color: Int) -> Unit
) {
    private var viewHue: View
    private var viewSatVal: ColorPickerSquare
    private var viewCursor: ImageView
    private var viewNewColor: ImageView
    private var viewTarget: ImageView
    private var newHexField: EditText
    private var viewContainer: ViewGroup
    private val baseConfig = activity.baseConfig
    private val currentColorHsv = FloatArray(3)
    private val backgroundColor = baseConfig.backgroundColor
    private var isHueBeingDragged = false
    private var wasDimmedBackgroundRemoved = false
    private var dialog: AlertDialog? = null

    init {
        Timber.d("Hojat Ghasemi : ColorPickerDialog was called")
        Color.colorToHSV(color, currentColorHsv)
        val binding = DialogColorPickerBinding.inflate(activity.layoutInflater).apply {
            if (IsQPlusUseCase()) {
                root.isForceDarkAllowed = false
            }

            viewHue = colorPickerHue
            viewSatVal = colorPickerSquare
            viewCursor = colorPickerHueCursor

            viewNewColor = colorPickerNewColor
            viewTarget = colorPickerCursor
            viewContainer = colorPickerHolder
            newHexField = colorPickerNewHex

            viewSatVal.setHue(getHue())

            viewNewColor.fillWithColor(getColor(), backgroundColor)
            colorPickerOldColor.fillWithColor(color, backgroundColor)

            val hexCode = getHexCode(color)
            colorPickerOldHex.text = "#$hexCode"
            colorPickerOldHex.setOnLongClickListener {
                activity.copyToClipboard(hexCode)
                true
            }
            newHexField.setText(hexCode)
            root.setupRecentColors()
        }

        viewHue.setOnTouchListener(OnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                isHueBeingDragged = true
            }

            if (event.action == MotionEvent.ACTION_MOVE || event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_UP) {
                var y = event.y
                if (y < 0f)
                    y = 0f

                if (y > viewHue.measuredHeight) {
                    y =
                        viewHue.measuredHeight - 0.001f // to avoid jumping the cursor from bottom to top.
                }
                var hue = 360f - 360f / viewHue.measuredHeight * y
                if (hue == 360f)
                    hue = 0f

                currentColorHsv[0] = hue
                updateHue()
                newHexField.setText(getHexCode(getColor()))

                if (event.action == MotionEvent.ACTION_UP) {
                    isHueBeingDragged = false
                }
                return@OnTouchListener true
            }
            false
        })

        viewSatVal.setOnTouchListener(OnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_MOVE || event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_UP) {
                var x = event.x
                var y = event.y

                if (x < 0f)
                    x = 0f
                if (x > viewSatVal.measuredWidth)
                    x = viewSatVal.measuredWidth.toFloat()
                if (y < 0f)
                    y = 0f
                if (y > viewSatVal.measuredHeight)
                    y = viewSatVal.measuredHeight.toFloat()

                currentColorHsv[1] = 1f / viewSatVal.measuredWidth * x
                currentColorHsv[2] = 1f - 1f / viewSatVal.measuredHeight * y

                moveColorPicker()
                viewNewColor.fillWithColor(getColor(), backgroundColor)
                newHexField.setText(getHexCode(getColor()))
                return@OnTouchListener true
            }
            false
        })


        newHexField.addTextChangedListener { editable ->
            val finalString = editable.toString()
            if (finalString.length == 6 && !isHueBeingDragged) {
                val newColor = Color.parseColor("#$finalString")
                Color.colorToHSV(newColor, currentColorHsv)
                updateHue()
                moveColorPicker()
            }
        }

        val textColor = activity.getProperTextColor()
        val builder = activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok) { _, _ -> confirmNewColor() }
            .setNegativeButton(R.string.cancel) { _, _ -> dialogDismissed() }
            .setOnCancelListener { dialogDismissed() }

        if (showUseDefaultButton) {
            builder.setNeutralButton(R.string.use_default) { _, _ -> useDefault() }
        }

        builder.apply {
            activity.setupDialogStuff(binding.root, this) { alertDialog ->
                dialog = alertDialog
                ApplyColorFilterUseCase(binding.colorPickerArrow, textColor)
                ApplyColorFilterUseCase(binding.colorPickerHexArrow, textColor)
                ApplyColorFilterUseCase(viewCursor, textColor)
            }
        }

        binding.root.onGlobalLayout {
            moveHuePicker()
            moveColorPicker()
        }
    }

    private fun View.setupRecentColors() {
        val recentColors = baseConfig.colorPickerRecentColors
        if (recentColors.isNotEmpty()) {
            findViewById<View>(R.id.recent_colors).visibility = View.VISIBLE
            val squareSize = context.resources.getDimensionPixelSize(R.dimen.colorpicker_hue_width)
            recentColors.take(RECENT_COLORS_NUMBER).forEach { recentColor ->
                val recentColorView = ImageView(context)
                recentColorView.id = View.generateViewId()
                recentColorView.layoutParams = ViewGroup.LayoutParams(squareSize, squareSize)
                recentColorView.fillWithColor(recentColor, backgroundColor)
                recentColorView.setOnClickListener { newHexField.setText(getHexCode(recentColor)) }
                findViewById<ViewGroup>(R.id.recent_colors).addView(recentColorView)
                findViewById<ConstraintHelper>(R.id.recent_colors_flow).addView(recentColorView)
            }
        }
    }

    private fun dialogDismissed() {
        callback(false, 0)
    }

    private fun confirmNewColor() {
        val hexValue = newHexField.text.toString().trim()
        val newColor = if (hexValue.length == 6) {
            Color.parseColor("#$hexValue")
        } else {
            getColor()
        }
        addRecentColor(newColor)

        callback(true, newColor)
    }

    private fun useDefault() {
        val defaultColor = baseConfig.defaultNavigationBarColor
        addRecentColor(defaultColor)

        callback(true, defaultColor)
    }

    private fun addRecentColor(color: Int) {
        var recentColors = baseConfig.colorPickerRecentColors

        recentColors.remove(color)
        if (recentColors.size >= RECENT_COLORS_NUMBER) {
            val numberOfColorsToDrop = recentColors.size - RECENT_COLORS_NUMBER + 1
            recentColors = LinkedList(recentColors.dropLast(numberOfColorsToDrop))
        }
        recentColors.addFirst(color)

        baseConfig.colorPickerRecentColors = recentColors
    }

    private fun getHexCode(color: Int) = color.toHex().substring(1)

    private fun updateHue() {
        viewSatVal.setHue(getHue())
        moveHuePicker()
        viewNewColor.fillWithColor(getColor(), backgroundColor)
        if (removeDimmedBackground && !wasDimmedBackgroundRemoved) {
            dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            wasDimmedBackgroundRemoved = true
        }

        currentColorCallback?.invoke(getColor())
    }

    private fun moveHuePicker() {
        var y = viewHue.measuredHeight - getHue() * viewHue.measuredHeight / 360f
        if (y == viewHue.measuredHeight.toFloat())
            y = 0f

        viewCursor.x = (viewHue.left - viewCursor.width).toFloat()
        viewCursor.y = viewHue.top + y - viewCursor.height / 2
    }

    private fun moveColorPicker() {
        val x = getSat() * viewSatVal.measuredWidth
        val y = (1f - getVal()) * viewSatVal.measuredHeight
        viewTarget.x = viewSatVal.left + x - viewTarget.width / 2
        viewTarget.y = viewSatVal.top + y - viewTarget.height / 2
    }

    private fun getColor() = Color.HSVToColor(currentColorHsv)
    private fun getHue() = currentColorHsv[0]
    private fun getSat() = currentColorHsv[1]
    private fun getVal() = currentColorHsv[2]
}
