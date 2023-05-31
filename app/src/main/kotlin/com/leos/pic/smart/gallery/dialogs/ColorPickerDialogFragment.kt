package ca.on.sudbury.hojat.smartgallery.dialogs

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintHelper
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import ca.on.hojat.palette.views.ColorPickerSquare
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.databinding.DialogFragmentColorPickerBinding
import ca.on.sudbury.hojat.smartgallery.extensions.baseConfig
import ca.on.sudbury.hojat.smartgallery.extensions.copyToClipboard
import ca.on.sudbury.hojat.smartgallery.extensions.fillWithColor
import ca.on.sudbury.hojat.smartgallery.extensions.getProperTextColor
import ca.on.sudbury.hojat.smartgallery.extensions.onGlobalLayout
import ca.on.sudbury.hojat.smartgallery.extensions.toHex
import ca.on.sudbury.hojat.smartgallery.helpers.BaseConfig
import ca.on.sudbury.hojat.smartgallery.usecases.ApplyColorFilterUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsQPlusUseCase
import java.util.*

/**
 * This dialog can be called in various User stories of the app.
 *
 * 1- In the settings page, by clicking on "customize colors" you will go to customization page.
 * In that page, by clicking on "Text color", "Background color", or "Bottom navigation bar color",
 * the resulting dialog is created via this class.
 *
 * 2- While configuring the app widget on the launcher, if you click on either of the colored circles
 * in the bottom left of widget configuration page, the resulting dialog is created via this class.
 *
 * 3- In the editing page, when you want to choose the color of your pen to draw on the picture,
 * the resulting dialog is created via this class.
 */
class ColorPickerDialogFragment(
    val color: Int,
    private val removeDimmedBackground: Boolean = false,
    private val showUseDefaultButton: Boolean = false,
    private val currentColorCallback: ((color: Int) -> Unit)? = null,
   private val callback: (wasPositivePressed: Boolean, color: Int) -> Unit
) : DialogFragment() {

    // The binding
    private var _binding: DialogFragmentColorPickerBinding? = null
    private val binding get() = _binding!!

    // The configuration needed throughout the class
    private val recentColorsId = 5
    private lateinit var viewHue: View
    private lateinit var viewSatVal: ColorPickerSquare
    private lateinit var viewCursor: ImageView
    private lateinit var viewNewColor: ImageView
    private lateinit var viewTarget: ImageView
    private lateinit var newHexField: EditText
    private lateinit var viewContainer: ViewGroup
    private lateinit var baseConfig: BaseConfig
    private val currentColorHsv = FloatArray(3)
    private var backgroundColor = -1
    private var isHueBeingDragged = false
    private var wasDimmedBackgroundRemoved = false

    /**
     * Create the UI of the dialog
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFragmentColorPickerBinding.inflate(
            inflater,
            container,
            false
        )
        loadDialogUI()
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun loadDialogUI() {

        // need to load the configurations before drawing the UI
        baseConfig = requireActivity().baseConfig
        backgroundColor = baseConfig.backgroundColor
        Color.colorToHSV(color, currentColorHsv)

        with(binding) {
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

            newHexField.setText(hexCode)
            setupRecentColors(root)
            if (showUseDefaultButton) {
                btnNeutralColorPickerDialog.visibility = VISIBLE
            }
        }

        val textColor = requireActivity().getProperTextColor()
        ApplyColorFilterUseCase(binding.colorPickerArrow, textColor)
        ApplyColorFilterUseCase(binding.colorPickerHexArrow, textColor)
        ApplyColorFilterUseCase(viewCursor, textColor)
        binding.root.onGlobalLayout {
            moveHuePicker()
            moveColorPicker()
        }


    }

    /**
     * Register listeners for views.
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            colorPickerOldHex.setOnLongClickListener {
                val hexCode = getHexCode(color)
                requireActivity().copyToClipboard(hexCode)
                true
            }
            btnPositiveColorPickerDialog.setOnClickListener {
                confirmNewColor()
                dismiss()
            }
            btnNegativeColorPickerDialog.setOnClickListener {
                callback(false, 0)
                dismiss()
            }
            if (showUseDefaultButton) {
                btnNeutralColorPickerDialog.setOnClickListener { useDefault() }
            }
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

    }

    /**
     * Cleaning the stuff
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @JvmName("getColor1")
    private fun getColor() = Color.HSVToColor(currentColorHsv)

    private fun getHexCode(color: Int) = color.toHex().substring(1)

    private fun setupRecentColors(root: View) {
        val recentColors = baseConfig.colorPickerRecentColors
        if (recentColors.isNotEmpty()) {
            root.findViewById<View>(R.id.recent_colors).visibility = VISIBLE
            val squareSize =
                resources.getDimensionPixelSize(R.dimen.colorpicker_hue_width)
            recentColors.take(recentColorsId).forEach { recentColor ->
                val recentColorView = ImageView(root.context)
                recentColorView.id = View.generateViewId()
                recentColorView.layoutParams = ViewGroup.LayoutParams(squareSize, squareSize)
                recentColorView.fillWithColor(recentColor, backgroundColor)
                recentColorView.setOnClickListener { newHexField.setText(getHexCode(recentColor)) }
                root.findViewById<ViewGroup>(R.id.recent_colors).addView(recentColorView)
                root.findViewById<ConstraintHelper>(R.id.recent_colors_flow)
                    .addView(recentColorView)
            }
        }
    }

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

    private fun getHue() = currentColorHsv[0]

    private fun getSat() = currentColorHsv[1]

    private fun getVal() = currentColorHsv[2]

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

    private fun addRecentColor(color: Int) {
        var recentColors = baseConfig.colorPickerRecentColors

        recentColors.remove(color)
        if (recentColors.size >= recentColorsId) {
            val numberOfColorsToDrop = recentColors.size - recentColorsId + 1
            recentColors = LinkedList(recentColors.dropLast(numberOfColorsToDrop))
        }
        recentColors.addFirst(color)

        baseConfig.colorPickerRecentColors = recentColors
    }

    private fun useDefault() {
        val defaultColor = baseConfig.defaultNavigationBarColor
        addRecentColor(defaultColor)
        callback(true, defaultColor)
    }

    companion object {
        const val TAG = "ColorPickerDialogFragment"
    }
}