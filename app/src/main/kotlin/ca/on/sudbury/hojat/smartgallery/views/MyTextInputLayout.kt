package ca.on.sudbury.hojat.smartgallery.views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import com.google.android.material.textfield.TextInputLayout
import ca.on.sudbury.hojat.smartgallery.extensions.adjustAlpha
import ca.on.sudbury.hojat.smartgallery.helpers.Alpha

class MyTextInputLayout : TextInputLayout {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    // we need to use reflection to make some colors work well
    fun setColors(textColor: Int, accentColor: Int) {
        try {
            editText!!.setTextColor(textColor)
            editText!!.backgroundTintList = ColorStateList.valueOf(accentColor)

            val hintColor =
                if (editText!!.text.toString().trim()
                        .isEmpty()
                ) textColor.adjustAlpha(Alpha.High.level) else textColor
            val defaultTextColor =
                TextInputLayout::class.java.getDeclaredField("defaultHintTextColor")
            defaultTextColor.isAccessible = true
            defaultTextColor.set(
                this,
                ColorStateList(arrayOf(intArrayOf(0)), intArrayOf(hintColor))
            )

            val focusedTextColor = TextInputLayout::class.java.getDeclaredField("focusedTextColor")
            focusedTextColor.isAccessible = true
            focusedTextColor.set(
                this,
                ColorStateList(arrayOf(intArrayOf(0)), intArrayOf(accentColor))
            )

            val defaultHintTextColor = textColor.adjustAlpha(Alpha.Medium.level)
            val boxColorState = ColorStateList(
                arrayOf(
                    intArrayOf(android.R.attr.state_active),
                    intArrayOf(android.R.attr.state_focused)
                ),
                intArrayOf(
                    defaultHintTextColor,
                    accentColor
                )
            )

            setBoxStrokeColorStateList(boxColorState)
            defaultTextColor.set(
                this,
                ColorStateList(arrayOf(intArrayOf(0)), intArrayOf(defaultHintTextColor))
            )
        } catch (_: Exception) {
        }
    }
}
